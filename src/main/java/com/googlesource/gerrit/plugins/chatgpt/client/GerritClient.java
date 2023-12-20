package com.googlesource.gerrit.plugins.chatgpt.client;

import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;
import com.google.inject.Singleton;
import com.googlesource.gerrit.plugins.chatgpt.config.Configuration;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_OK;

@Slf4j
@Singleton
public class GerritClient {
    private final Gson gson = new Gson();
    private final HttpClientWithRetry httpClientWithRetry = new HttpClientWithRetry();

    public String getPatchSet(Configuration config, String fullChangeId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .header(HttpHeaders.AUTHORIZATION, generateBasicAuth(config.getGerritUserName(),
                        config.getGerritPassword()))
                .uri(URI.create(config.getGerritAuthBaseUrl()
                        + UriResourceLocator.gerritPatchSetUri(fullChangeId)))
                .build();

        log.info("getPatchSet request is : {}",request);
        HttpResponse<String> response = httpClientWithRetry.execute(request);

        if (response.statusCode() != HTTP_OK) {
            log.error("Failed to get patch. Response: {}", response);
            throw new IOException("Failed to get patch from Gerrit");
        }

        String responseBody = response.body();
        log.info("Successfully obtained patch. Decoding response body.");
        log.info("responseBody is : {}",responseBody);
        org.apache.commons.codec.binary.Base64 base64 = new org.apache.commons.codec.binary.Base64();
        byte[] debytes = base64.decodeBase64(responseBody);

        log.info("Decoded responseBody is : {}",new String(debytes));
        return new String(debytes);

        // return new String(Base64.getMimeDecoder().decode(responseBody));
    }

    private String generateBasicAuth(String username, String password) {
        String auth = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    }

    public void postComment(Configuration config, String fullChangeId, String message,String code_review_score) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("message", message);
        Map<String, Object> labelsMap = new HashMap<>();
        labelsMap.put("Code-Review", code_review_score);
        map.put("labels", labelsMap);

        String json = gson.toJson(map);

        log.info("json : {}",json);

        HttpRequest request = HttpRequest.newBuilder()
                .header(HttpHeaders.AUTHORIZATION, generateBasicAuth(config.getGerritUserName(),
                        config.getGerritPassword()))
                .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                .uri(URI.create(config.getGerritAuthBaseUrl()
                        + UriResourceLocator.gerritCommentUri(fullChangeId)))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        log.info("Request Method: {}", request.method());
        log.info("Request URI: {}", request.uri());
        log.info("Request Headers: {}", request.headers().map());
        log.info("Request Body: {}", json);  // Assuming json contains the request body

        HttpResponse<String> response = httpClientWithRetry.execute(request);

        if (response.statusCode() != HTTP_OK) {
            log.error("Review post failed with status code: {}", response.statusCode());
        }
    }
}
