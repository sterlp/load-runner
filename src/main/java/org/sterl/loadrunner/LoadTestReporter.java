package org.sterl.loadrunner;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.springframework.http.HttpMethod;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import lombok.Getter;

/**
 * This class provides a way to report metrics during a load test to the report engine.
 * @author sterlp
 */
public class LoadTestReporter implements Closeable {

    @Getter
    final MetricRegistry registry = new MetricRegistry();
    final ConsoleReporter reporter;
    
    public LoadTestReporter() {
        reporter = ConsoleReporter.forRegistry(registry)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(1, TimeUnit.MINUTES);
    }
    
    public Timer timer(String url) {
        return registry.timer(MetricRegistry.name(url));
    }
    public Counter failedCounter(String url) {
        return registry.counter(MetricRegistry.name("failed", url));
    }
    public Timer timer(HttpMethod method, String url) {
        return registry.timer(MetricRegistry.name(method.name(), url));
    }
    public Counter failedCounter(HttpMethod method, String url) {
        return registry.counter(MetricRegistry.name("failed", method.name(), url));
    }
    
    public void report() {
        reporter.report();
    }

    @Override
    public void close() throws IOException {
        reporter.close();
    }
}
