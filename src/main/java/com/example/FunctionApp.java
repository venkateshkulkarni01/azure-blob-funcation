package com.example;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * Azure Function triggered by a new blob in 'input-container'.
 * It reads the file content, estimates line count, and sends routing info
 * to one of three Azure Storage Queues based on the file size:
 *  - file-small-queue: < 50,000 lines
 *  - file-medium-queue: 50,000–500,000 lines
 *  - file-large-queue: > 500,000 lines
 */
public class FunctionApp {

    /**
     * Function method triggered on blob upload to 'input-container'.
     * Determines file size by line count and routes a message to the correct queue.
     *
     * @param content   the file content in bytes
     * @param fileName  the name of the uploaded file
     * @param context   function execution context for logging
     */
    @FunctionName("RouteBlobFileBySize")
    public void run(
        @BlobTrigger(name = "blob", path = "input-container/{name}", dataType = "binary",
                     connection = "AzureWebJobsStorage") byte[] content,
        @BindingName("name") String fileName,
        final ExecutionContext context
    ) {
        Logger log = context.getLogger();
        log.info("[Trigger] Blob file received: " + fileName);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new java.io.ByteArrayInputStream(content), StandardCharsets.UTF_8))) {

            long lineCount = reader.lines().count();
            log.info("[Info] Line count estimated: " + lineCount);

            String queueName;
            if (lineCount < 50_000) queueName = "file-small-queue";
            else if (lineCount <= 500_000) queueName = "file-medium-queue";
            else queueName = "file-large-queue";

            String blobUrl = "https://<your-storage-account>.blob.core.windows.net/input-container/" + fileName;

            String queueMessage = String.format("{\"fileName\":\"%s\", \"lineCount\":%d, \"blobUrl\":\"%s\"}",
                    fileName, lineCount, blobUrl);

            log.info("[Routing] Sending message to: " + queueName);
            log.info("[Payload] " + queueMessage);

            QueueClient queueClient = new QueueClientBuilder()
                    .connectionString(System.getenv("AZURE_STORAGE_CONNECTION_STRING"))
                    .queueName(queueName)
                    .buildClient();

            queueClient.sendMessage(queueMessage);
            log.info("[Success] Message sent to queue successfully.");

        } catch (Exception e) {
            log.severe("[Error] Failed to process blob: " + e.getMessage());
        }
    }
}
