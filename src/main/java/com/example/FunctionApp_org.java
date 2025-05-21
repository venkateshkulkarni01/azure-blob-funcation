// package com.example;

// import java.io.BufferedReader;
// import java.io.InputStreamReader;
// import java.nio.charset.StandardCharsets;
// import java.util.Optional;

// import com.azure.storage.blob.BlobClientBuilder;
// import com.azure.storage.queue.QueueClient;
// import com.azure.storage.queue.QueueClientBuilder;
// import com.microsoft.azure.functions.ExecutionContext;
// import com.microsoft.azure.functions.HttpMethod;
// import com.microsoft.azure.functions.HttpRequestMessage;
// import com.microsoft.azure.functions.HttpResponseMessage;
// import com.microsoft.azure.functions.HttpStatus;
// import com.microsoft.azure.functions.annotation.AuthorizationLevel;
// import com.microsoft.azure.functions.annotation.FunctionName;
// import com.microsoft.azure.functions.annotation.HttpTrigger;

// public class FunctionApp_org {

//     @FunctionName("RouteFileBasedOnSize")
//     public HttpResponseMessage run(
//             @HttpTrigger(name = "req", methods = {
//                     HttpMethod.POST }, authLevel = AuthorizationLevel.FUNCTION) HttpRequestMessage<Optional<BlobEventRequest>> request,
//             final ExecutionContext context) {
//         context.getLogger().info("Function triggered to route file based on line count.");

//         BlobEventRequest blobRequest = request.getBody()
//                 .orElseThrow(() -> new RuntimeException("Invalid request body"));

//         String blobUrl = blobRequest.getBlobUrl();
//         String fileName = blobUrl.substring(blobUrl.lastIndexOf("/") + 1);

//         try {
//             long lineCount = countLines(blobUrl);

//             String queueName;
//             if (lineCount < 50_000)
//                 queueName = "file-small-queue";
//             else if (lineCount <= 500_000)
//                 queueName = "file-medium-queue";
//             else
//                 queueName = "file-large-queue";

//             String queueMessage = String.format("{\"fileName\":\"%s\", \"lineCount\":%d, \"blobUrl\":\"%s\"}", fileName,
//                     lineCount, blobUrl);

//             QueueClient queueClient = new QueueClientBuilder()
//                     .connectionString(System.getenv("AZURE_STORAGE_CONNECTION_STRING"))
//                     .queueName(queueName)
//                     .buildClient();

//             queueClient.sendMessage(queueMessage);
//             context.getLogger().info("File routed to: " + queueName);

//             return request.createResponseBuilder(HttpStatus.OK)
//                     .body("File routed to queue: " + queueName)
//                     .build();
//         } catch (Exception e) {
//             context.getLogger().severe("Error: " + e.getMessage());
//             return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
//                     .body("Error processing file: " + e.getMessage())
//                     .build();
//         }
//     }

//     private long countLines(String blobUrl) throws Exception {
//         try (BufferedReader reader = new BufferedReader(new InputStreamReader(
//                 new BlobClientBuilder().endpoint(blobUrl).buildClient().openInputStream(),
//                 StandardCharsets.UTF_8))) {
//             return reader.lines().count();
//         }
//     }
// }
