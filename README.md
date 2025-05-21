# Azure Blob Function

This project is an Azure Function that reads files from Azure Blob Storage, preserving the folder structure and providing detailed information about each file alos to inform whoch queue to send msg.

## Project Structure

```
azure-blob-function
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── example
│   │   │           ├── BlobFunction.java
│   │   │           └── BlobEventRequest.java
│   │   └── resources
│   │       └── application.properties
├── host.json
├── local.settings.json
├── pom.xml
└── README.md
```

## Prerequisites

- Java 19
- Maven
- Azure Functions Core Tools
- Azure Storage Account

## Setup Instructions

1. **Configure Azure Storage:**
   - Create an Azure Storage Account if you don't have one.
   - Obtain the connection string from the Azure portal.

2. **Update `application.properties`:**
   - Set the connection string for Azure Blob Storage in `src/main/resources/application.properties`.

3. **Build the project:**
   ```
   mvn clean package
   ```

4. **Run the function locally:**
   ```
   mvn azure-functions:run 

   curl -X POST http://localhost:7071/api/RouteFileBasedOnSize  -H "Content-Type: application/json"  -d "{\"blobUrl\":\"D:/Workspace/azure-blob-function/test-data/sample.csv\"}"

   ```

## Usage

- The Azure Function can be triggered via HTTP requests.
- It retrieves the list of files from the specified Blob Storage container and returns details such as file name, size, and URL.
