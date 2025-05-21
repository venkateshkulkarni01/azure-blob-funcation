package com.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

public class FunctionApp {

    @FunctionName("RouteFileBasedOnSize")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<BlobEventRequest>> request,
            final ExecutionContext context) {
        context.getLogger().info("[Local Simulation] Function triggered to route file based on line count.");

        BlobEventRequest blobRequest = request.getBody()
                .orElseThrow(() -> new RuntimeException("Invalid request body"));

        String filePath = blobRequest.getBlobUrl();
        String fileName = new File(filePath).getName();

        try {
            long lineCount = countLines(filePath);

            String queueName;
            if (lineCount < 50_000)
                queueName = "file-small-queue";
            else if (lineCount <= 500_000)
                queueName = "file-medium-queue";
            else
                queueName = "file-large-queue";

            String queueMessage = String.format("{\"fileName\":\"%s\", \"lineCount\":%d, \"blobUrl\":\"%s\"}", fileName,
                    lineCount, filePath);

            context.getLogger().info("Simulated Routing -> Would send to: " + queueName);
            context.getLogger().info("Payload: " + queueMessage);

            return request.createResponseBuilder(HttpStatus.OK)
                    .body("Simulated routing to queue: " + queueName)
                    .build();

        } catch (Exception e) {
            context.getLogger().severe("Error: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing file: " + e.getMessage())
                    .build();
        }
    }

    private long countLines(String filePath) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new java.io.InputStreamReader(new java.io.FileInputStream(filePath), StandardCharsets.UTF_8))) {
            return reader.lines().count();
        }
    }
}
