package com.googlesource.gerrit.plugins.chatgpt.client;

public class UriResourceLocator {

    private UriResourceLocator() {
        throw new IllegalStateException("Utility class");
    }

    public static String gerritPatchSetUri(String fullChangeId) {
        return "/a/changes/" + fullChangeId + "/revisions/current/patch";
    }

    public static String gerritCommentUri(String fullChangeId) {
        return "/a/changes/" + fullChangeId + "/revisions/current/review";
    }

    public static String chatCompletionsUri() {
        return "/v1/chat/completions";
    }

    public static String Azure_Uri(String endPoint,String modelName,String apiVersion) {
        return endPoint+"openai/deployments/"+ modelName+ "/chat/completions?api-version=" + apiVersion;
    }
}
