package com.googlesource.gerrit.plugins.chatgpt.client;

import com.github.rholder.retry.*;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.net.ProxySelector;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static java.net.HttpURLConnection.HTTP_OK;

@Singleton
@Slf4j
public class HttpClientWithRetry {
    private final Retryer<HttpResponse<String>> retryer;

    ProxySelector proxySelector = ProxySelector.of(new InetSocketAddress("127.0.0.1", 7890));

    private final HttpClient httpClient = HttpClient.newBuilder()
            .proxy(proxySelector)
            .connectTimeout(Duration.ofMinutes(5))
            .build();

    public HttpClientWithRetry() {
        //Attention, 'com.github.rholder.retry.RetryListener' is marked unstable with @Beta annotation
        RetryListener listener = new RetryListener() {
            @Override
            public <V> void onRetry(Attempt<V> attempt) {
                if (attempt.hasException()) {
                    log.error("Retry failed with exception: " + attempt.getExceptionCause());
                }
            }
        };

        this.retryer = RetryerBuilder.<HttpResponse<String>>newBuilder()
                .retryIfException()
                .retryIfResult(response -> {
                    if (response.statusCode() != HTTP_OK) {
                        log.error("Retry because HTTP status code is not 200. The status code is: " + response.statusCode());
                        return true;
                    } else {
                        return false;
                    }
                }).withWaitStrategy(WaitStrategies.fixedWait(20, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(10))
                .withRetryListener(listener)
                .build();
    }

    public HttpResponse<String> execute(HttpRequest request) throws ExecutionException, RetryException {
        return retryer.call(() -> httpClient.send(request, HttpResponse.BodyHandlers.ofString()));
    }

}

