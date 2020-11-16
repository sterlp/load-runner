package org.sterl.loadrunner.cmd;

import java.util.Base64;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.sterl.loadrunner.ApacheHttpBuilder;
import org.sterl.loadrunner.LoadTestReporter;
import org.sterl.loadrunner.LoadTestRunner;

import lombok.ToString;

/**
 * Simple example to create a basic load test, using here command line args.
 * 
 * @author sterlp
 */
@ToString // (exclude = "headers")
public class CommandLineLoadTest {
    // private static final Logger LOG = LoggerFactory.getLogger(CommandLineLoadTest.class);
    private HttpMethod method = HttpMethod.GET;
    private String url;
    private String basicAuth;
    private int clients = 100;
    private int requests = 10_000;
    private HttpHeaders headers;

    public void run(String[] args) throws Exception {
        if (parseOptions(args)) {
            
            try (LoadTestReporter reporter = new LoadTestReporter()) {
                final ExecutorService executorService = Executors.newFixedThreadPool(clients);
                final HttpComponentsClientHttpRequestFactory newSslHttpClient = ApacheHttpBuilder.newSslHttpClient(clients + 5);
                final RestTemplate restTemplate = new RestTemplate(newSslHttpClient);
                
                System.out.println(StringUtils.rightPad("Starting: " + this, 80, '='));
                long time = System.currentTimeMillis();
                for (int i = 0; i < requests; i++) {
                    executorService.submit(new Runner(reporter, restTemplate, url, method, headers));
                }
                
                executorService.shutdown();
                executorService.awaitTermination(Math.min(30 * 60, requests * 30), TimeUnit.SECONDS);
                time = System.currentTimeMillis() - time;
                
                System.out.println(StringUtils.rightPad(new Date() + " SVMS MappingStatus ", 80, '-'));
                System.out.println("Clients:       " + clients);
                System.out.println("Request count: " + requests); 
                System.out.println("Total time:    " + time + "ms." );
                System.out.println();
                
                ((CloseableHttpClient)newSslHttpClient.getHttpClient()).close();
            }

        }
    }
    
    
    private boolean parseOptions(String[] args) {
        final Options options = new Options();
        options.addOption("u", "url", true, "URL to load test");
        //options.addOption("m", "method", false, "HTTP method to use.");
        //options.addOption("p", "payload", false, "String payload to send.");
        options.addOption("b", "basic", true,
                "Use basic authentication user:password - will be encoded.");
        options.addOption(Option.builder("c").longOpt("clients").hasArg()
                .type(Integer.class).desc("Amount of concurrent threads to use (default 100)").build());
        options.addOption(Option.builder("r").longOpt("requests").hasArg()
                .type(Integer.class).desc("Amount of requests to send (default 10.000)").build());

        if (args == null || args.length > 1) {
            final CommandLineParser parser = new DefaultParser();
            try {
                final CommandLine cmd = parser.parse(options, args);
                this.url = cmd.getOptionValue('u');
                
                headers = new HttpHeaders();
                headers.add("Accept", "application/json");

                // basic
                if (cmd.hasOption('b')) {
                    basicAuth = cmd.getOptionValue('b');
                    headers.add("Authorization", "Basic " + Base64.getEncoder().encodeToString(basicAuth.getBytes()));
                }
                
                if (cmd.hasOption('c')) {
                    clients = Integer.valueOf(cmd.getOptionValue('c')).intValue();
                }
                if (cmd.hasOption('r')) {
                    requests = Integer.valueOf(cmd.getOptionValue('r')).intValue();
                }
                return true;
            } catch (Exception e) {
                System.err.println(e.getMessage());
                return false;
            }
        } else if (args.length == 1 && args[0].startsWith("http")) {
            this.url = args[0];
            return true;
        } else {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("LoadRunner", options);
            return false;
        }
    }
    
    private static class Runner extends LoadTestRunner<String, HttpEntity<String>> {
        private HttpHeaders headers;
        public Runner(LoadTestReporter reporter, RestTemplate restTemplate, String url, HttpMethod method, HttpHeaders headers) {
            super(reporter, restTemplate, url, method);
            this.headers = headers;
        }
        @Override
        protected HttpEntity<String> prepare() {
            return new HttpEntity<String>(this.headers);
        }
        @Override
        protected ResponseEntity<String> execute(HttpEntity<String> entity, RestTemplate restTemplate) throws Exception {
            return restTemplate.exchange(url,
                    this.method,
                    entity,
                    String.class
                );
        }
    }
}
