package com.github.johnnyhooyo.dsaiassist.service;

import com.github.johnnyhooyo.dsaiassist.model.ChatMessage;
import com.github.johnnyhooyo.dsaiassist.settings.PluginSettings;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * DeepSeek API 服务
 */
@Service
public final class DeepSeekService {
    
    private static final Logger LOG = Logger.getInstance(DeepSeekService.class);
    private static final String DEEPSEEK_API_URL = "https://api.deepseek.com/v1/chat/completions";
    
    private final HttpClient httpClient;
    private final Gson gson;
    
    public DeepSeekService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.gson = new Gson();
    }
    
    /**
     * 发送消息到DeepSeek API
     */
    public CompletableFuture<String> sendMessage(String message, String apiKey) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return sendMessageSync(message, apiKey);
            } catch (Exception e) {
                LOG.error("DeepSeek API调用失败", e);
                return "抱歉，AI服务暂时不可用：" + e.getMessage();
            }
        });
    }
    
    /**
     * 异步发送消息并通过回调返回结果
     */
    public void sendMessageAsync(String message, String apiKey, Consumer<String> onSuccess, Consumer<String> onError) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                String response = sendMessageSync(message, apiKey);
                ApplicationManager.getApplication().invokeLater(() -> onSuccess.accept(response));
            } catch (Exception e) {
                LOG.error("DeepSeek API调用失败", e);
                ApplicationManager.getApplication().invokeLater(() ->
                    onError.accept("抱歉，AI服务暂时不可用：" + e.getMessage()));
            }
        });
    }

    /**
     * 流式发送消息，实时返回响应片段
     */
    public void sendMessageStream(JsonArray message, String apiKey, Consumer<String> onChunk, Consumer<String> onComplete, Consumer<String> onError) {
        sendMessageStreamWithReasoning(message, apiKey, onChunk, null, onComplete, onError);
    }

    /**
     * 流式发送消息，支持推理内容和正式内容的区分（JsonArray版本）
     */
    public void sendMessageStreamWithReasoning(JsonArray messages, String apiKey,
                                             Consumer<String> onContentChunk,
                                             Consumer<String> onReasoningChunk,
                                             Consumer<String> onComplete,
                                             Consumer<String> onError) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                sendMessageStreamSyncWithReasoningArray(messages, apiKey, onContentChunk, onReasoningChunk, onComplete, onError);
            } catch (Exception e) {
                LOG.error("DeepSeek 流式API调用失败", e);
                ApplicationManager.getApplication().invokeLater(() ->
                    onError.accept("抱歉，AI服务暂时不可用：" + e.getMessage()));
            }
        });
    }

    /**
     * 流式发送消息，支持推理内容和正式内容的区分
     */
    public void sendMessageStreamWithReasoning(String message, String apiKey,
                                             Consumer<String> onContentChunk,
                                             Consumer<String> onReasoningChunk,
                                             Consumer<String> onComplete,
                                             Consumer<String> onError) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                sendMessageStreamSyncWithReasoning(message, apiKey, onContentChunk, onReasoningChunk, onComplete, onError);
            } catch (Exception e) {
                LOG.error("DeepSeek 流式API调用失败", e);
                ApplicationManager.getApplication().invokeLater(() ->
                    onError.accept("抱歉，AI服务暂时不可用：" + e.getMessage()));
            }
        });
    }

    /**
     * 转换消息到deepseek所需要的格式
     */
    public JsonArray parseMessages(List<ChatMessage> messages) {
        JsonArray jsonArray = new JsonArray();
        for (ChatMessage msg : messages) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("role", msg.isUser() ? "user" : "assistant");
            jsonObject.addProperty("content", msg.getContent());
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }
    
    /**
     * 同步发送消息到DeepSeek API（非流式）
     */
    private String sendMessageSync(String message, String apiKey) throws IOException, InterruptedException {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("DeepSeek API Key未设置，请在设置中配置");
        }

        // 构建请求体（非流式）
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", PluginSettings.getInstance().getDeepSeekModel());
        requestBody.addProperty("max_tokens", 2048);
        requestBody.addProperty("temperature", 0.7);
        requestBody.addProperty("stream", false); // 非流式

        JsonArray messages = new JsonArray();
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", message);
        messages.add(userMessage);

        requestBody.add("messages", messages);

        // 创建HTTP请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DEEPSEEK_API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey.trim())
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                .build();

        // 发送请求
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // 处理响应
        if (response.statusCode() == 200) {
            return parseResponse(response.body());
        } else {
            throw new IOException("API请求失败，状态码: " + response.statusCode() + ", 响应: " + response.body());
        }
    }

    /**
     * 流式发送消息到DeepSeek API
     */
    private void sendMessageStreamSync(JsonArray message, String apiKey, Consumer<String> onChunk, Consumer<String> onComplete, Consumer<String> onError) throws IOException, InterruptedException {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            ApplicationManager.getApplication().invokeLater(() ->
                onError.accept("DeepSeek API Key未设置，请在设置中配置"));
            return;
        }

        // 构建请求体（流式）
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", PluginSettings.getInstance().getDeepSeekModel());
        requestBody.addProperty("max_tokens", 2048);
        requestBody.addProperty("temperature", 0.7);
        requestBody.addProperty("stream", true); // 流式

        requestBody.add("messages", message);

        // 创建HTTP请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DEEPSEEK_API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey.trim())
                .header("Accept", "text/event-stream")
                .timeout(Duration.ofSeconds(120))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                .build();

        // 发送请求并处理流式响应
        HttpResponse<java.io.InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() == 200) {
            parseStreamResponseInputStream(response.body(), onChunk, onComplete, onError);
        } else {
            ApplicationManager.getApplication().invokeLater(() ->
                onError.accept("API请求失败，状态码: " + response.statusCode()));
        }
    }

    /**
     * 流式发送消息到DeepSeek API（支持推理内容）
     */
    private void sendMessageStreamSyncWithReasoning(String message, String apiKey,
                                                   Consumer<String> onContentChunk,
                                                   Consumer<String> onReasoningChunk,
                                                   Consumer<String> onComplete,
                                                   Consumer<String> onError) throws IOException, InterruptedException {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            ApplicationManager.getApplication().invokeLater(() ->
                onError.accept("DeepSeek API Key未设置，请在设置中配置"));
            return;
        }

        // 构建请求体（流式）
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", PluginSettings.getInstance().getDeepSeekModel());
        requestBody.addProperty("max_tokens", 2048);
        requestBody.addProperty("temperature", 0.7);
        requestBody.addProperty("stream", true); // 流式

        JsonArray messages = new JsonArray();
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", message);
        messages.add(userMessage);

        requestBody.add("messages", messages);

        // 创建HTTP请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DEEPSEEK_API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey.trim())
                .header("Accept", "text/event-stream")
                .timeout(Duration.ofSeconds(120))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                .build();

        // 发送请求并处理流式响应
        HttpResponse<java.io.InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() == 200) {
            parseStreamResponseWithReasoningInputStream(response.body(), onContentChunk, onReasoningChunk, onComplete, onError);
        } else {
            ApplicationManager.getApplication().invokeLater(() ->
                onError.accept("API请求失败，状态码: " + response.statusCode()));
        }
    }

    /**
     * 流式发送消息到DeepSeek API（支持推理内容，JsonArray版本）
     */
    private void sendMessageStreamSyncWithReasoningArray(JsonArray messages, String apiKey,
                                                       Consumer<String> onContentChunk,
                                                       Consumer<String> onReasoningChunk,
                                                       Consumer<String> onComplete,
                                                       Consumer<String> onError) throws IOException, InterruptedException {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            ApplicationManager.getApplication().invokeLater(() ->
                onError.accept("DeepSeek API Key未设置，请在设置中配置"));
            return;
        }

        // 构建请求体（流式）
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", PluginSettings.getInstance().getDeepSeekModel());
        requestBody.addProperty("max_tokens", 2048);
        requestBody.addProperty("temperature", 0.7);
        requestBody.addProperty("stream", true); // 流式

        requestBody.add("messages", messages);

        // 创建HTTP请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DEEPSEEK_API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey.trim())
                .header("Accept", "text/event-stream")
                .timeout(Duration.ofSeconds(120))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                .build();

        // 发送请求并处理流式响应
        HttpResponse<java.io.InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() == 200) {
            parseStreamResponseWithReasoningInputStream(response.body(), onContentChunk, onReasoningChunk, onComplete, onError);
        } else {
            ApplicationManager.getApplication().invokeLater(() ->
                onError.accept("API请求失败，状态码: " + response.statusCode()));
        }
    }
    
    /**
     * 解析API响应（非流式）
     */
    private String parseResponse(String responseBody) {
        try {
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

            if (jsonResponse.has("error")) {
                JsonObject error = jsonResponse.getAsJsonObject("error");
                String errorMessage = error.get("message").getAsString();
                throw new RuntimeException("API错误: " + errorMessage);
            }

            JsonArray choices = jsonResponse.getAsJsonArray("choices");
            if (!choices.isEmpty()) {
                JsonObject firstChoice = choices.get(0).getAsJsonObject();
                JsonObject message = firstChoice.getAsJsonObject("message");
                return message.get("content").getAsString();
            } else {
                throw new RuntimeException("API响应中没有找到回复内容");
            }
        } catch (Exception e) {
            LOG.error("解析API响应失败", e);
            throw new RuntimeException("解析API响应失败: " + e.getMessage());
        }
    }

    /**
     * 解析流式API响应
     */
    private void parseStreamResponse(String responseBody, Consumer<String> onChunk, Consumer<String> onComplete, Consumer<String> onError) {
        try {
            StringBuilder fullContent = new StringBuilder();
            BufferedReader reader = new BufferedReader(new StringReader(responseBody));
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data: ")) {
                    String data = line.substring(6); // 移除 "data: " 前缀

                    if ("[DONE]".equals(data.trim())) {
                        // 流式响应结束
                        ApplicationManager.getApplication().invokeLater(() ->
                            onComplete.accept(fullContent.toString()));
                        return;
                    }

                    try {
                        JsonObject jsonChunk = gson.fromJson(data, JsonObject.class);

                        if (jsonChunk.has("error")) {
                            JsonObject error = jsonChunk.getAsJsonObject("error");
                            String errorMessage = error.get("message").getAsString();
                            ApplicationManager.getApplication().invokeLater(() ->
                                onError.accept("API错误: " + errorMessage));
                            return;
                        }

                        JsonArray choices = jsonChunk.getAsJsonArray("choices");
                        if (!choices.isEmpty()) {
                            JsonObject firstChoice = choices.get(0).getAsJsonObject();
                            if (firstChoice.has("delta")) {
                                JsonObject delta = firstChoice.getAsJsonObject("delta");
                                if (delta.has("reasoning_content")) {
                                    String content = delta.get("reasoning_content").getAsString();
                                    fullContent.append(content);

                                    // 在UI线程中更新界面
                                    ApplicationManager.getApplication().invokeLater(() ->
                                            onChunk.accept(content));
                                } else if (delta.has("content")) {
                                    String content = delta.get("content").getAsString();
                                    fullContent.append(content);

                                    // 在UI线程中更新界面
                                    ApplicationManager.getApplication().invokeLater(() ->
                                        onChunk.accept(content));
                                }
                            }
                        }
                    } catch (Exception e) {
                        LOG.warn("解析流式响应片段失败: " + data, e);
                        // 继续处理下一个片段
                    }
                }
            }

            // 如果没有收到 [DONE] 信号，也要调用完成回调
            ApplicationManager.getApplication().invokeLater(() ->
                onComplete.accept(fullContent.toString()));

        } catch (Exception e) {
            LOG.error("解析流式API响应失败", e);
            ApplicationManager.getApplication().invokeLater(() ->
                onError.accept("解析流式API响应失败: " + e.getMessage()));
        }
    }

    /**
     * 解析流式API响应（支持推理内容）
     */
    private void parseStreamResponseWithReasoning(String responseBody,
                                                Consumer<String> onContentChunk,
                                                Consumer<String> onReasoningChunk,
                                                Consumer<String> onComplete,
                                                Consumer<String> onError) {
        try {
            StringBuilder fullContent = new StringBuilder();
            StringBuilder reasoningContent = new StringBuilder();
            BufferedReader reader = new BufferedReader(new StringReader(responseBody));
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data: ")) {
                    String data = line.substring(6); // 移除 "data: " 前缀

                    if ("[DONE]".equals(data.trim())) {
                        System.out.println("流式响应结束" + fullContent);
                        // 流式响应结束
                        ApplicationManager.getApplication().invokeLater(() ->
                            onComplete.accept(fullContent.toString()));
                        return;
                    }

                    try {
                        JsonObject jsonChunk = gson.fromJson(data, JsonObject.class);

                        if (jsonChunk.has("error")) {
                            JsonObject error = jsonChunk.getAsJsonObject("error");
                            String errorMessage = error.get("message").getAsString();
                            ApplicationManager.getApplication().invokeLater(() ->
                                onError.accept("API错误: " + errorMessage));
                            return;
                        }

                        JsonArray choices = jsonChunk.getAsJsonArray("choices");
                        if (!choices.isEmpty()) {
                            JsonObject firstChoice = choices.get(0).getAsJsonObject();
                            if (firstChoice.has("delta")) {
                                JsonObject delta = firstChoice.getAsJsonObject("delta");

                                // 处理推理内容
                                if (delta.has("reasoning_content")) {
                                    String content = delta.get("reasoning_content").getAsString();
                                    reasoningContent.append(content);

                                    // 只有在设置允许且有回调时才处理推理内容
                                    if (onReasoningChunk != null && PluginSettings.getInstance().isShowReasoningContent()) {
                                        ApplicationManager.getApplication().invokeLater(() ->
                                            onReasoningChunk.accept(content));
                                    }
                                }

                                // 处理正式内容
                                if (delta.has("content")) {
                                    String content = delta.get("content").getAsString();
                                    fullContent.append(content);
                                    System.out.println("正式内容: " + fullContent);

                                    // 在UI线程中更新界面
                                    ApplicationManager.getApplication().invokeLater(() ->
                                        onContentChunk.accept(content));
                                }
                            }
                        }
                    } catch (Exception e) {
                        LOG.warn("解析流式响应片段失败: " + data, e);
                        // 继续处理下一个片段
                    }
                }
            }
            System.out.println("没有收到 [DONE] 信号,流式响应结束" + fullContent);
            // 如果没有收到 [DONE] 信号，也要调用完成回调
            ApplicationManager.getApplication().invokeLater(() ->
                onComplete.accept(fullContent.toString()));

        } catch (Exception e) {
            LOG.error("解析流式API响应失败", e);
            ApplicationManager.getApplication().invokeLater(() ->
                onError.accept("解析流式API响应失败: " + e.getMessage()));
        }
    }
    
    /**
     * 测试API连接
     */
    public CompletableFuture<Boolean> testConnection(String apiKey) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                sendMessageSync("Hello", apiKey);
                return true;
            } catch (Exception e) {
                LOG.warn("DeepSeek API连接测试失败", e);
                return false;
            }
        });
    }
    
    /**
     * 真正的流式响应处理（使用InputStream实时处理）
     */
    private void parseStreamResponseInputStream(java.io.InputStream inputStream, Consumer<String> onChunk, Consumer<String> onComplete, Consumer<String> onError) {
        try {
            StringBuilder fullContent = new StringBuilder();
            BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8));
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data: ")) {
                    String data = line.substring(6); // 移除 "data: " 前缀

                    if ("[DONE]".equals(data.trim())) {
                        // 流式响应结束
                        ApplicationManager.getApplication().invokeLater(() ->
                            onComplete.accept(fullContent.toString()));
                        return;
                    }

                    try {
                        JsonObject jsonChunk = gson.fromJson(data, JsonObject.class);

                        if (jsonChunk.has("error")) {
                            JsonObject error = jsonChunk.getAsJsonObject("error");
                            String errorMessage = error.get("message").getAsString();
                            ApplicationManager.getApplication().invokeLater(() ->
                                onError.accept("API错误: " + errorMessage));
                            return;
                        }

                        JsonArray choices = jsonChunk.getAsJsonArray("choices");
                        if (!choices.isEmpty()) {
                            JsonObject firstChoice = choices.get(0).getAsJsonObject();
                            if (firstChoice.has("delta")) {
                                JsonObject delta = firstChoice.getAsJsonObject("delta");
                                if (delta.has("content")) {
                                    String content = delta.get("content").getAsString();
                                    fullContent.append(content);

                                    // 立即在UI线程中更新界面
                                    ApplicationManager.getApplication().invokeLater(() ->
                                        onChunk.accept(content));
                                }
                            }
                        }
                    } catch (Exception e) {
                        LOG.warn("解析流式响应片段失败: " + data, e);
                        // 继续处理下一个片段
                    }
                }
            }

            // 如果没有收到 [DONE] 信号，也要调用完成回调
            ApplicationManager.getApplication().invokeLater(() ->
                onComplete.accept(fullContent.toString()));

        } catch (Exception e) {
            LOG.error("解析流式API响应失败", e);
            ApplicationManager.getApplication().invokeLater(() ->
                onError.accept("解析流式API响应失败: " + e.getMessage()));
        }
    }

    /**
     * 真正的流式响应处理（支持推理内容，使用InputStream实时处理）
     */
    private void parseStreamResponseWithReasoningInputStream(java.io.InputStream inputStream,
                                                           Consumer<String> onContentChunk,
                                                           Consumer<String> onReasoningChunk,
                                                           Consumer<String> onComplete,
                                                           Consumer<String> onError) {
        try {
            StringBuilder fullContent = new StringBuilder();
            StringBuilder reasoningContent = new StringBuilder();
            BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8));
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data: ")) {
                    String data = line.substring(6); // 移除 "data: " 前缀

                    if ("[DONE]".equals(data.trim())) {
                        // 流式响应结束
                        ApplicationManager.getApplication().invokeLater(() ->
                            onComplete.accept(fullContent.toString()));
                        return;
                    }

                    try {
                        JsonObject jsonChunk = gson.fromJson(data, JsonObject.class);

                        if (jsonChunk.has("error")) {
                            JsonObject error = jsonChunk.getAsJsonObject("error");
                            String errorMessage = error.get("message").getAsString();
                            ApplicationManager.getApplication().invokeLater(() ->
                                onError.accept("API错误: " + errorMessage));
                            return;
                        }

                        JsonArray choices = jsonChunk.getAsJsonArray("choices");
                        if (!choices.isEmpty()) {
                            JsonObject firstChoice = choices.get(0).getAsJsonObject();
                            if (firstChoice.has("delta")) {
                                JsonObject delta = firstChoice.getAsJsonObject("delta");

                                // 处理推理内容
                                JsonElement reasoningResp = delta.get("reasoning_content");
                                if (delta.has("reasoning_content") && !reasoningResp.isJsonNull()) {
                                    String content = reasoningResp.getAsString();
                                    reasoningContent.append(content);

                                    // 只有在设置允许且有回调时才处理推理内容
                                    if (onReasoningChunk != null && PluginSettings.getInstance().isShowReasoningContent()) {
                                        ApplicationManager.getApplication().invokeLater(() ->
                                            onReasoningChunk.accept(content));
                                    }
                                }

                                // 处理正式内容
                                JsonElement element = delta.get("content");
                                if (delta.has("content") && !element.isJsonNull()) {
                                    String content = element.getAsString();
                                    fullContent.append(content);

                                    // 立即在UI线程中更新界面
                                    ApplicationManager.getApplication().invokeLater(() ->
                                            onContentChunk.accept(content));
                                }
                            }
                        }
                    } catch (Exception e) {
                        LOG.warn("解析流式响应片段失败: " + data, e);
                        // 继续处理下一个片段
                    }
                }
            }

            // 如果没有收到 [DONE] 信号，也要调用完成回调
            ApplicationManager.getApplication().invokeLater(() ->
                onComplete.accept(fullContent.toString()));

        } catch (Exception e) {
            LOG.error("解析流式API响应失败", e);
            ApplicationManager.getApplication().invokeLater(() ->
                onError.accept("解析流式API响应失败: " + e.getMessage()));
        }
    }

    /**
     * 验证API Key格式
     */
    public boolean isValidApiKey(String apiKey) {
        return apiKey != null &&
                apiKey.trim().startsWith("sk-") &&
                apiKey.trim().length() > 10;
    }
}
