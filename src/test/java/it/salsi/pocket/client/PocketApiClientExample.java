/***************************************************************************
 *
 * Pocket web backend
 * Copyright (C) 2018/2025 Antonio Salsi <passy.linux@zresa.it>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 ***************************************************************************/

package it.salsi.pocket.client;

import it.salsi.pocket.models.Container;
import it.salsi.pocket.models.Device;
import it.salsi.pocket.models.User;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Example usage of the PocketApiClient for testing and development.
 * This class demonstrates how to use the client to interact with the Pocket API.
 */
public class PocketApiClientExample {

    private final PocketApiClient client;
    
    // Test credentials - these would normally come from configuration or user input
    private static final String API_BASE_URL = "http://localhost:8081";
    private static final String DEVICE_UUID = "12345678-1234-1234-1234-123456789012";
    private static final String CRYPT_TOKEN = "exampleCryptToken123";

    public PocketApiClientExample() {
        this.client = new PocketApiClient(API_BASE_URL);
    }

    /**
     * Demonstrates basic API operations.
     */
    public void demonstrateBasicOperations() {
        System.out.println("=== Pocket API Client Demo ===\n");

        // Test connection
        System.out.println("1. Testing API connection...");
        boolean connected = client.testConnection();
        System.out.printf("   Connection status: %s\n\n", connected ? "SUCCESS" : "FAILED");

        if (!connected) {
            System.out.println("   API server not available. Make sure the server is running on " + API_BASE_URL);
            return;
        }

        // Get health status
        System.out.println("2. Getting health status...");
        PocketApiClient.ApiResponse<String> healthResponse = client.getHealth();
        if (healthResponse.isSuccess()) {
            System.out.printf("   Health Status: %s\n", healthResponse.getData());
        } else {
            System.out.printf("   Health Check Failed: %s\n", healthResponse.getErrorMessage());
        }
        System.out.println();

        // Get session data
        System.out.println("3. Getting session data...");
        PocketApiClient.ApiResponse<Container> getResponse = client.getData(DEVICE_UUID, CRYPT_TOKEN);
        if (getResponse.isSuccess()) {
            Container container = getResponse.getData();
            System.out.printf("   Retrieved session data: %s\n", container);
        } else {
            System.out.printf("   Failed to get session data: %s\n", getResponse.getErrorMessage());
        }
        System.out.println();

        // Post session data
        System.out.println("4. Posting session data...");
        Container testContainer = createTestContainer();
        PocketApiClient.ApiResponse<Container> postResponse = client.postData(DEVICE_UUID, CRYPT_TOKEN, testContainer);
        if (postResponse.isSuccess()) {
            System.out.printf("   Posted session data successfully: %s\n", postResponse.getData());
        } else {
            System.out.printf("   Failed to post session data: %s\n", postResponse.getErrorMessage());
        }
        System.out.println();

        // Check cache record
        System.out.println("5. Checking cache record...");
        PocketApiClient.ApiResponse<Void> checkResponse = client.checkCacheRecord(DEVICE_UUID, CRYPT_TOKEN);
        if (checkResponse.isSuccess()) {
            System.out.println("   Cache record check successful");
        } else {
            System.out.printf("   Cache record check failed: %s\n", checkResponse.getErrorMessage());
        }
        System.out.println();
    }

    /**
     * Demonstrates asynchronous API operations.
     */
    public void demonstrateAsyncOperations() {
        System.out.println("=== Async Operations Demo ===\n");

        try {
            // Single async request
            System.out.println("1. Making async request...");
            CompletableFuture<PocketApiClient.ApiResponse<Container>> future = client.getDataAsync(DEVICE_UUID, CRYPT_TOKEN);
            
            PocketApiClient.ApiResponse<Container> response = future.get(10, TimeUnit.SECONDS);
            if (response.isSuccess()) {
                System.out.printf("   Async request successful: %s\n", response.getData());
            } else {
                System.out.printf("   Async request failed: %s\n", response.getErrorMessage());
            }
            System.out.println();

            // Batch requests
            System.out.println("2. Making batch requests...");
            int batchSize = 5;
            CompletableFuture<PocketApiClient.ApiResponse<Container>>[] futures = client.createBatchRequests(DEVICE_UUID, CRYPT_TOKEN, batchSize);
            
            System.out.printf("   Created %d concurrent requests\n", batchSize);
            
            // Wait for all to complete
            long startTime = System.currentTimeMillis();
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures);
            allFutures.get(30, TimeUnit.SECONDS);
            long endTime = System.currentTimeMillis();
            
            // Analyze results
            int successCount = 0;
            int errorCount = 0;
            for (CompletableFuture<PocketApiClient.ApiResponse<Container>> f : futures) {
                PocketApiClient.ApiResponse<Container> r = f.get();
                if (r.isSuccess()) {
                    successCount++;
                } else {
                    errorCount++;
                }
            }
            
            System.out.printf("   Batch completed in %d ms\n", (endTime - startTime));
            System.out.printf("   Success: %d, Errors: %d\n", successCount, errorCount);
            
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.out.printf("   Async operation failed: %s\n", e.getMessage());
        }
        System.out.println();
    }

    /**
     * Demonstrates error handling scenarios.
     */
    public void demonstrateErrorHandling() {
        System.out.println("=== Error Handling Demo ===\n");

        // Test with invalid UUID
        System.out.println("1. Testing with invalid UUID...");
        PocketApiClient.ApiResponse<Container> invalidUuidResponse = client.getData("invalid-uuid", CRYPT_TOKEN);
        System.out.printf("   Response: %s\n", invalidUuidResponse);
        System.out.println();

        // Test with empty parameters
        System.out.println("2. Testing with empty parameters...");
        PocketApiClient.ApiResponse<Container> emptyParamsResponse = client.getData("", "");
        System.out.printf("   Response: %s\n", emptyParamsResponse);
        System.out.println();

        // Test with invalid base URL
        System.out.println("3. Testing with invalid server URL...");
        PocketApiClient invalidClient = new PocketApiClient("http://invalid-server:9999");
        PocketApiClient.ApiResponse<String> invalidServerResponse = invalidClient.getHealth();
        System.out.printf("   Response: %s\n", invalidServerResponse);
        System.out.println();
    }

    /**
     * Demonstrates load testing capabilities.
     */
    public void demonstrateLoadTesting() {
        System.out.println("=== Load Testing Demo ===\n");

        if (!client.testConnection()) {
            System.out.println("Server not available for load testing");
            return;
        }

        int[] batchSizes = {1, 5, 10, 20};
        
        for (int batchSize : batchSizes) {
            System.out.printf("Testing with %d concurrent requests...\n", batchSize);
            
            try {
                long startTime = System.currentTimeMillis();
                
                CompletableFuture<PocketApiClient.ApiResponse<Container>>[] futures = client.createBatchRequests(DEVICE_UUID, CRYPT_TOKEN, batchSize);
                CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures);
                allFutures.get(60, TimeUnit.SECONDS);
                
                long endTime = System.currentTimeMillis();
                double avgTime = (double) (endTime - startTime) / batchSize;
                
                System.out.printf("   Total time: %d ms, Average per request: %.2f ms\n", (endTime - startTime), avgTime);
                
            } catch (Exception e) {
                System.out.printf("   Load test failed: %s\n", e.getMessage());
            }
        }
        System.out.println();
    }

    /**
     * Creates a test container for demonstration purposes.
     */
    private Container createTestContainer() {
        User testUser = new User("Demo User", "demo@example.com", "demoPassword");
        testUser.setId(999L);

        Device testDevice = new Device(testUser);
        testDevice.setId(999L);
        testDevice.setUuid(DEVICE_UUID);

        return new Container(
                System.currentTimeMillis(),
                testUser,
                testDevice,
                List.of(), // groups
                List.of(), // groupFields
                List.of()  // fields
        );
    }

    /**
     * Main method to run the demonstration.
     */
    public static void main(String[] args) {
        PocketApiClientExample example = new PocketApiClientExample();
        
        try {
            example.demonstrateBasicOperations();
            example.demonstrateAsyncOperations();
            example.demonstrateErrorHandling();
            example.demonstrateLoadTesting();
            
        } catch (Exception e) {
            System.err.printf("Demo failed with error: %s\n", e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== Demo Complete ===");
    }
}