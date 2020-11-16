package org.sterl.loadrunner;

import java.util.concurrent.Callable;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class LoadTestRunner<T, V> implements Callable<T> {

    protected abstract V prepare();
    /**
     * return <code>null</code> to indicate an error occurred
     */
    protected abstract ResponseEntity<T> execute(V value, RestTemplate restTemplate) throws Exception;

    protected final LoadTestReporter reporter;
    protected final RestTemplate restTemplate;
    protected final String url;
    protected final HttpMethod method;

    private final Counter failedCounter;
    private final Timer timer;

    public LoadTestRunner(LoadTestReporter reporter, RestTemplate restTemplate, String url, HttpMethod method) {
        this(reporter, restTemplate, url, method, 
                reporter.failedCounter(method, url), reporter.timer(method, url));
    }

    @Override
    public T call() throws Exception {
        V v = prepare();
        
        final Context t = timer.time();
        try {
            final ResponseEntity<T> result = execute(v, restTemplate);

            if (result == null) {
                failedCounter.inc();
            } else {
                t.close();
                reporter.getRegistry().counter(MetricRegistry.name(result.getStatusCodeValue() + "", method.name(), url)).inc();
            }

            return result == null ? null : result.getBody();
        } catch (RestClientResponseException e) {
            t.close();
            reporter.getRegistry().counter(MetricRegistry.name(e.getRawStatusCode() + "", method.name(), url)).inc();
            throw e;
        } catch (Exception e) {
            failedCounter.inc();
            throw e;
        }
    }
}
