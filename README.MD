
[![Java CI with Maven](https://github.com/sterlp/load-runner/actions/workflows/maven.yml/badge.svg)](https://github.com/sterlp/load-runner/actions/workflows/maven.yml)

# LoadRunner
```
 -b,--basic <arg>      Use basic authentication user:password - will be
                       encoded.
 -c,--clients <arg>    Amount of concurrent threads to use (default 100)
 -m,--method <arg>     HTTP method to use (GET/POST/PUT).
 -p,--payload <arg>    String payload to send.
 -r,--requests <arg>   Amount of requests to send (default 10.000)
 -u,--url <arg>        URL to load test
```

## Examples

### GET on Google

- URL: https://google.de
- Method: GET
- Clients: 100
- Requests: 10.000

#### Request

`java -jar target/load-runner.jar https://google.de`


#### Result
```
-- Counters --------------------------------------------------------------------
200.GET.https://google.de
             count = 5881
429.GET.https://google.de
             count = 4119
failed.GET.https://google.de
             count = 0

-- Timers ----------------------------------------------------------------------
GET.https://google.de
             count = 10000
         mean rate = 124,85 calls/second
     1-minute rate = 110,95 calls/second
     5-minute rate = 93,35 calls/second
    15-minute rate = 87,46 calls/second
               min = 88,43 milliseconds
               max = 6727,60 milliseconds
              mean = 832,39 milliseconds
            stddev = 723,81 milliseconds
            median = 661,30 milliseconds
              75% <= 888,01 milliseconds
              95% <= 2183,26 milliseconds
              98% <= 3446,39 milliseconds
              99% <= 4382,18 milliseconds
            99.9% <= 5909,52 milliseconds

```

### POST

- URL: http://localhost:8080/foo
- Method: POST
- Clients: 250
- Payload: fooooo
- Requests: 10.000

`java -jar target/load-runner.jar -c 250 -u http://localhost:8080/foo -m post -p "fooooo"`
