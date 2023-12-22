package com.googlesource.gerrit.plugins.chatgpt.integration;

import com.googlesource.gerrit.plugins.chatgpt.client.GerritClient;
import com.googlesource.gerrit.plugins.chatgpt.client.OpenAiClient;
import com.googlesource.gerrit.plugins.chatgpt.config.Configuration;
import com.googlesource.gerrit.plugins.chatgpt.client.AzureOpenAiClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Mockito.when;

@Ignore("This test suite is designed to demonstrate how to test the Gerrit and GPT interfaces in a real environment. " +
        "It is not intended to be executed during the regular build process")
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class CodeReviewPluginIT {
    @Mock
    private Configuration config;

    @InjectMocks
    private GerritClient gerritClient;

    @InjectMocks
    private OpenAiClient openAiClient;

    @InjectMocks
    private AzureOpenAiClient azureClient;

    @Test
    public void sayHelloToAzureOpenAi() throws Exception {
        when(config.getAzureEndpoint()).thenReturn(Configuration.AZURE_DEFAULT_ENDPOINT);
        when(config.getAzureOpenAiKey()).thenReturn("bc709d6234e04a80ab2d744eb2434086");
        when(config.getAzureModel()).thenReturn(Configuration.AZURE_DEFAULT_GPT_MODEL);
        when(config.getAzurePrompt()).thenReturn(Configuration.AZURE_DEFAULT_PROMPT);
        when(config.getAzureTemperature()).thenReturn(0.2);
        when(config.getAzureApiVersion()).thenReturn(Configuration.AZURE_DEFAULT_API_VERSION);

        String answer = azureClient.ask(config, "你是谁");
        log.info("answer: {}", answer);
        assertNotNull(answer);
    }

//    @Test
//    public void sayHelloToGPT() throws Exception {
//        when(config.getGptDomain()).thenReturn(Configuration.OPENAI_DOMAIN);
//        when(config.getGptToken()).thenReturn("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");
//        when(config.getGptModel()).thenReturn(Configuration.DEFAULT_GPT_MODEL);
//        when(config.getGptPrompt()).thenReturn(Configuration.DEFAULT_GPT_PROMPT);
//
//        String answer = openAiClient.ask(config, "hello");
//        log.info("answer: {}", answer);
//        assertNotNull(answer);
//    }


    @Test
    public void getPatchSet() throws Exception {
        when(config.getGerritAuthBaseUrl()).thenReturn("http://192.168.191.1:8082");
        when(config.getGerritUserName()).thenReturn("zhihui");
        when(config.getGerritPassword()).thenReturn("vdInkF18zytT96sCaAqDK+4OYHU9vSFiMX834D+XoA");
        String patchSet = gerritClient.getPatchSet(config, "I8dae7bd37f8f80c3daceb1cf0502c95ac7cbf43a");
        log.info("patchSet: {}", patchSet);
        assertNotNull(patchSet);
    }

    @Test
    public void postComment() throws Exception {
        when(config.getGerritAuthBaseUrl()).thenReturn("http://192.168.191.1:8082");
        when(config.getGerritUserName()).thenReturn("zhihui");
        when(config.getGerritPassword()).thenReturn("vdInkF18zytT96sCaAqDK+4OYHU9vSFiMX834D+XoA");

        gerritClient.postComment(config, "I8dae7bd37f8f80c3daceb1cf0502c95ac7cbf43a", "好好好","+2");
    }
}
