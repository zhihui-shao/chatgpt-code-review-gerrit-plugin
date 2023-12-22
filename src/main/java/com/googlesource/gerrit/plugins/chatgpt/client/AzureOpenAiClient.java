package com.googlesource.gerrit.plugins.chatgpt.client;

import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;
import com.google.inject.Singleton;
import com.googlesource.gerrit.plugins.chatgpt.config.Configuration;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

@Slf4j
@Singleton
public class AzureOpenAiClient {
    private final Gson gson = new Gson();
    private final HttpClientWithRetry httpClientWithRetry = new HttpClientWithRetry();

    public String ask(Configuration config, String patchSet) throws Exception {
        HttpRequest request = createRequest(config, patchSet);

        log.info("request: {}",request);


        HttpResponse<String> response = httpClientWithRetry.execute(request);

        String body = response.body();
//        log.info("body: {}",body);
        if (body == null) {
            throw new IOException("responseBody is null");
        }

        StringBuilder finalContent = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new StringReader(body))) {
            String line;
            while ((line = reader.readLine()) != null) {
                extractContentFromLine(line).ifPresent(finalContent::append);
            }
        }
        return finalContent.toString();
    }

    private HttpRequest createRequest(Configuration config, String patchSet) {
        String requestBody = createRequestBody(config, patchSet);
        log.info("requestBody: {}",requestBody);

        return HttpRequest.newBuilder()
                .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                .header("api-key" ,config.getAzureOpenAiKey())
                .uri(URI.create(UriResourceLocator.Azure_Uri(config.getAzureEndpoint(),config.getAzureModel(),config.getAzureApiVersion())))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
    }

    private String createRequestBody(Configuration config, String patchSet) {
        ChatCompletionRequest.Message systemMessage = ChatCompletionRequest.Message.builder()
                .role("system")
                .content(config.getAzurePrompt())
                .build();
        ChatCompletionRequest.Message userMessage = ChatCompletionRequest.Message.builder()
                .role("user")
                .content(patchSet)
                .build();

        List<ChatCompletionRequest.Message> messages = List.of(systemMessage, userMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .messages(messages)
                .temperature(config.getAzureTemperature())
                .stream(true)
                .build();

        return gson.toJson(chatCompletionRequest);
    }

    private Optional<String> extractContentFromLine(String line) {
        String dataPrefix = "data: {\"id\"";

        if (!line.startsWith(dataPrefix)) {
            return Optional.empty();
        }
        ChatCompletionResponse chatCompletionResponse =
                gson.fromJson(line.substring("data: ".length()), ChatCompletionResponse.class);
        if(!chatCompletionResponse.getChoices().isEmpty()){
            String content = chatCompletionResponse.getChoices().get(0).getDelta().getContent();
            return Optional.ofNullable(content);
        }else{
            return Optional.empty();
        }
    }

}