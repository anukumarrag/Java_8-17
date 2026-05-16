package com.training.java817.module1.httpclient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * =============================================================================
 * MODULE 1 – HTTP CLIENT API (java.net.http, Java 11 – JEP 321)
 * =============================================================================
 *
 * THEORY
 * ------
 * Before Java 11, sending an HTTP request required either:
 *   • java.net.HttpURLConnection – verbose, stream-based, no JSON/body support.
 *   • Apache HttpClient / OkHttp / Spring RestTemplate – third-party dependency.
 *
 * Java 11 (JEP 321) shipped a new, modern HTTP client as part of the JDK:
 *   • Supports HTTP/1.1, HTTP/2, and WebSocket.
 *   • Synchronous AND asynchronous (non-blocking) request patterns.
 *   • Fluent builder API for requests and responses.
 *   • Built-in support for JSON, form data, binary bodies.
 *   • Secure by default (TLS 1.3 support).
 *   • Follows redirects automatically (optional).
 *
 * KEY CLASSES
 * -----------
 *   HttpClient        – the shared, reusable HTTP client (thread-safe)
 *   HttpRequest       – an immutable request (method, URI, headers, body)
 *   HttpResponse<T>   – the response (status code, headers, body of type T)
 *   HttpRequest.BodyPublishers  – helpers for building request bodies
 *   HttpResponse.BodyHandlers   – helpers for parsing response bodies
 *
 * NOTE
 * ----
 * The examples below demonstrate the API patterns.  In tests they use a
 * small embedded HTTP server (com.sun.net.httpserver) to avoid network calls.
 */
public class HttpClientExamples {

    // =========================================================================
    // BEFORE – HttpURLConnection (Java 7 style)
    // =========================================================================

    /**
     * Fetch a URL body using the legacy HttpURLConnection API.
     * Verbose, error-prone, requires manual stream management.
     */
    public String fetch_Before(String url) throws Exception {
        java.net.URL u = new java.net.URL(url);
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) u.openConnection();
        conn.setRequestMethod("GET");
        try (java.io.InputStream is = conn.getInputStream();
             java.io.InputStreamReader isr = new java.io.InputStreamReader(is);
             java.io.BufferedReader br = new java.io.BufferedReader(isr)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append("\n");
            return sb.toString().trim();
        }
    }

    // =========================================================================
    // AFTER – java.net.http.HttpClient (Java 11)
    // =========================================================================

    /** Build a shared HttpClient. In production: reuse one instance per application. */
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)       // prefer HTTP/2, fall back to 1.1
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // ---- Synchronous GET ----------------------------------------------------

    /**
     * Synchronous GET request.
     * Blocks the calling thread until the response arrives.
     * Simple, appropriate for scripts or sequential workflows.
     */
    public HttpResponse<String> getSync(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /** Extract only the response body string from a GET. */
    public String getBody(String url) throws Exception {
        return getSync(url).body();
    }

    /** Check the status code from a GET. */
    public int getStatusCode(String url) throws Exception {
        return getSync(url).statusCode();
    }

    // ---- Asynchronous GET ---------------------------------------------------

    /**
     * Asynchronous GET request using sendAsync().
     * Returns a CompletableFuture<HttpResponse<String>> – the calling thread
     * is NOT blocked; the response is processed in a callback.
     */
    public CompletableFuture<String> getAsync(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    // ---- POST with JSON body ------------------------------------------------

    /**
     * Synchronous POST request with a JSON body.
     * BodyPublishers.ofString() sends the string as UTF-8 by default.
     */
    public HttpResponse<String> postJson(String url, String jsonBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Accept",       "application/json")
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // ---- Asynchronous POST --------------------------------------------------

    public CompletableFuture<HttpResponse<String>> postJsonAsync(String url, String jsonBody) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    // ---- Parallel requests --------------------------------------------------

    /**
     * Fire N requests in parallel and collect all response bodies.
     * Uses sendAsync() + CompletableFuture.allOf().
     */
    public java.util.List<String> fetchAll(java.util.List<String> urls) {
        var futures = urls.stream()
                .map(this::getAsync)
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    // ---- Response headers ---------------------------------------------------

    public String getHeader(String url, String headerName) throws Exception {
        return getSync(url).headers().firstValue(headerName).orElse("MISSING");
    }

    // demo main (runs against httpbin.org for illustration – requires network)
    public static void main(String[] args) throws Exception {
        HttpClientExamples ex = new HttpClientExamples();

        System.out.println("--- Sync GET ---");
        var response = ex.getSync("https://httpbin.org/get");
        System.out.println("Status : " + response.statusCode());
        System.out.println("Body   : " + response.body().substring(0, 80) + "...");

        System.out.println("\n--- Async GET ---");
        String asyncBody = ex.getAsync("https://httpbin.org/uuid").join();
        System.out.println("Async  : " + asyncBody.strip());

        System.out.println("\n--- POST JSON ---");
        String jsonPayload = """
                {"employeeId": "E001", "name": "Alice", "department": "ENGINEERING", "salary": 150000}
                """;
        var postResp = ex.postJson("https://httpbin.org/post", jsonPayload);
        System.out.println("POST status : " + postResp.statusCode());
    }
}
