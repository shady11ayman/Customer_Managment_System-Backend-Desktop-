package com.javap.customermanagmentdesktop.service;

import com.javap.customermanagmentdesktop.model.Customer;
import com.javap.customermanagmentdesktop.model.ApiResponse;
import com.javap.customermanagmentdesktop.util.AppConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import okhttp3.*;


import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class CustomerApiService {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public CustomerApiService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }


    public ApiResponse<List<Customer>> getAllCustomers() {
        Request request = buildGet(AppConfig.CUSTOMERS_ENDPOINT);
        try (Response response = httpClient.newCall(request).execute()) {
            int code = response.code();
            String body = response.body() != null ? response.body().string() : "";


            System.out.println("Response code: " + code);
            System.out.println("Response body: " + body);

            if (response.isSuccessful()) {
                List<Customer> customers = objectMapper.readValue(
                        body, new TypeReference<List<Customer>>() {});
                return ApiResponse.success(customers, code);
            }
            String errorMsg = extractErrorMessageFromBody(body, code);
            return ApiResponse.failure(errorMsg, code);
        } catch (IOException e) {

            System.out.println("IO Exception: " + e.getMessage());
            return ApiResponse.failure("Cannot connect to the server. Is the backend running?", 0);
        }
    }


    public ApiResponse<List<Customer>> searchCustomers(String query) {
        String url = AppConfig.CUSTOMERS_ENDPOINT + "?search=" + encodeQuery(query);
        Request request = buildGet(url);
        return executeListRequest(request);
    }


    public ApiResponse<Customer> getCustomerById(long id) {
        String url = AppConfig.CUSTOMERS_ENDPOINT + "/" + id;
        Request request = buildGet(url);
        return executeSingleRequest(request);
    }


    public ApiResponse<Customer> createCustomer(Customer customer) {
        try {
            String json = objectMapper.writeValueAsString(customer);
            RequestBody body = RequestBody.create(json, JSON);
            Request request = new Request.Builder()
                    .url(AppConfig.CUSTOMERS_ENDPOINT)
                    .addHeader("Authorization", "Bearer " + AppConfig.API_TOKEN)
                    .post(body)
                    .build();
            return executeSingleRequest(request);
        } catch (Exception e) {
            return ApiResponse.failure("Failed to serialize request: " + e.getMessage(), 0);
        }
    }


    public ApiResponse<Customer> updateCustomer(long id, Customer customer) {
        try {
            String url = AppConfig.CUSTOMERS_ENDPOINT + "/" + id;
            String json = objectMapper.writeValueAsString(customer);
            RequestBody body = RequestBody.create(json, JSON);
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + AppConfig.API_TOKEN)
                    .put(body)
                    .build();
            return executeSingleRequest(request);
        } catch (Exception e) {
            return ApiResponse.failure("Failed to serialize request: " + e.getMessage(), 0);
        }
    }


    public ApiResponse<Void> deleteCustomer(long id) {
        String url = AppConfig.CUSTOMERS_ENDPOINT + "/" + id;
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + AppConfig.API_TOKEN)
                .delete()
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            int code = response.code();
            if (code == 204) {
                return ApiResponse.success(null, code);
            }
            String errorMsg = extractErrorMessage(response);
            return ApiResponse.failure(errorMsg, code);
        } catch (IOException e) {
            return ApiResponse.failure("Cannot connect to the server. Is the backend running?", 0);
        }
    }



    private Request buildGet(String url) {
        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + AppConfig.API_TOKEN)
                .get()
                .build();
    }

    private ApiResponse<Customer> executeSingleRequest(Request request) {
        try (Response response = httpClient.newCall(request).execute()) {
            int code = response.code();
            String body = response.body() != null ? response.body().string() : "";

            if (response.isSuccessful()) {
                Customer customer = objectMapper.readValue(body, Customer.class);
                return ApiResponse.success(customer, code);
            }
            String errorMsg = extractErrorMessageFromBody(body, code);
            return ApiResponse.failure(errorMsg, code);
        } catch (IOException e) {
            return ApiResponse.failure("Cannot connect to the server. Is the backend running?", 0);
        }
    }

    private ApiResponse<List<Customer>> executeListRequest(Request request) {
        try (Response response = httpClient.newCall(request).execute()) {
            int code = response.code();
            String body = response.body() != null ? response.body().string() : "";

            if (response.isSuccessful()) {
                List<Customer> customers = objectMapper.readValue(
                        body, new TypeReference<List<Customer>>() {});
                return ApiResponse.success(customers, code);
            }
            String errorMsg = extractErrorMessageFromBody(body, code);
            return ApiResponse.failure(errorMsg, code);
        } catch (IOException e) {
            return ApiResponse.failure("Cannot connect to the server. Is the backend running?", 0);
        }
    }

    private String extractErrorMessage(Response response) throws IOException {
        String body = response.body() != null ? response.body().string() : "";
        return extractErrorMessageFromBody(body, response.code());
    }

    private String extractErrorMessageFromBody(String body, int code) {
        try {
            if (!body.isBlank()) {
                JsonNode node = objectMapper.readTree(body);
                if (node.has("message")) {
                    return node.get("message").asText();
                }
            }
        } catch (Exception ignored) {}
        return "HTTP Error " + code;
    }

    private String encodeQuery(String query) {
        try {
            return java.net.URLEncoder.encode(query, "UTF-8");
        } catch (Exception e) {
            return query;
        }
    }
}
