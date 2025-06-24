package com.search_log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.opensearch.client.RestClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.rest_client.RestClientTransport;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class LogApiServer {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        int serverPort = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(serverPort), 0);

        // Khởi tạo OpenSearch Client một lần và tái sử dụng
        OpenSearchClient client = createOpenSearchClient();

        // Tạo context cho API tìm kiếm
        server.createContext("/api/logs/search", new LogSearchHandler(client));

        // Tạo context cho API nạp dữ liệu
        server.createContext("/api/logs/ingest", new LogIngestHandler());

        // Sử dụng một thread pool để xử lý nhiều request đồng thời
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("=========================================================");
        System.out.println("Log API Server is running on port " + serverPort);
        System.out.println("=========================================================");
        System.out.println("Endpoints available:");
        System.out.println("  - Search: GET http://localhost:8080/api/logs/search?field=<field>&value=<value>");
        System.out.println("  - Ingest: POST http://localhost:8080/api/logs/ingest (Nạp dữ liệu từ DB)");
        System.out.println("Press Ctrl+C to stop the server.");
    }

    /**
     * Khởi tạo và cấu hình OpenSearchClient.
     * Logic này được lấy từ lớp LogSearcher ban đầu.
     */
    private static OpenSearchClient createOpenSearchClient() {
        final String OPENSEARCH_HOST = "localhost";
        final int OPENSEARCH_PORT = 9200;
        final String OPENSEARCH_USER = "admin";
        final String OPENSEARCH_PASS = "Hus@334nt";

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(OPENSEARCH_USER, OPENSEARCH_PASS));

        RestClient restClient = RestClient.builder(
                new HttpHost(OPENSEARCH_HOST, OPENSEARCH_PORT, "http")
        ).setHttpClientConfigCallback(httpClientBuilder ->
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
        ).build();

        OpenSearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new OpenSearchClient(transport);
    }

    /**
     * Lớp nội bộ xử lý các request tìm kiếm log.
     */
    static class LogSearchHandler implements HttpHandler {
        private final LogSearcher searcher;

        public LogSearchHandler(OpenSearchClient client) {
            this.searcher = new LogSearcher(client);
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Thiết lập CORS headers để cho phép React App giao tiếp
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "http://localhost:5173");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");

            // Xử lý request OPTIONS (pre-flight request từ trình duyệt)
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 204, "");
                return;
            }

            // Chỉ chấp nhận phương thức GET cho việc tìm kiếm
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendErrorResponse(exchange, 405, "Method Not Allowed");
                return;
            }

            try {
                URI requestURI = exchange.getRequestURI();
                Map<String, String> params = queryToMap(requestURI.getQuery());

                String field = params.get("field");
                String value = params.get("value");
                boolean isNested = "true".equalsIgnoreCase(params.get("isNested"));

                if (field == null || value == null || field.trim().isEmpty() || value.trim().isEmpty()) {
                    sendErrorResponse(exchange, 400, "Parameters 'field' and 'value' are required.");
                    return;
                }

                System.out.println("Received search request: field=" + field + ", value=" + value + ", isNested=" + isNested);

                List<ObjectNode> results;
                if (isNested) {
                    results = searcher.searchByNestedFieldValue(field, value);
                } else {
                    results = searcher.searchByFieldValue(field, value);
                }

                String jsonResponse = objectMapper.writeValueAsString(results);
                sendJsonResponse(exchange, 200, jsonResponse);

            } catch (Exception e) {
                e.printStackTrace();
                sendErrorResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
            }
        }
    }

    /**
     * Lớp nội bộ xử lý request nạp dữ liệu từ DB vào OpenSearch.
     */
    static class LogIngestHandler implements HttpHandler {
        private final LogIngestor ingestor = new LogIngestor();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "http://localhost:5173");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 204, "");
                return;
            }

            if (!"POST".equals(exchange.getRequestMethod())) {
                sendErrorResponse(exchange, 405, "Method Not Allowed. Use POST to ingest data.");
                return;
            }

            try {
                System.out.println("Received request to ingest data from database...");
                int count = ingestor.runIngest();
                String message = String.format("Successfully ingested %d log events.", count);
                sendJsonResponse(exchange, 200, String.format("{\"message\": \"%s\"}", message));
                System.out.println(message);
            } catch (Exception e) {
                e.printStackTrace();
                sendErrorResponse(exchange, 500, "Failed to ingest data: " + e.getMessage());
            }
        }
    }

    // --- CÁC PHƯƠNG THỨC HỖ TRỢ (HELPER METHODS) ---

    /**
     * Chuyển đổi một chuỗi query của URL thành một Map.
     * Ví dụ: "field=action&value=UserLogin" -> {field=action, value=UserLogin}
     */
    public static Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null) {
            return result;
        }
        for (String param : query.split("&")) {
            String[] entry = param.split("=", 2);
            if (entry.length > 1) {
                try {
                    // Decode giá trị để xử lý các ký tự đặc biệt như %20 (dấu cách)
                    result.put(entry[0], URLDecoder.decode(entry[1], StandardCharsets.UTF_8.name()));
                } catch (UnsupportedEncodingException e) {
                    // Sẽ không xảy ra với UTF-8
                    e.printStackTrace();
                }
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }

    /**
     * Gửi phản hồi HTTP với mã trạng thái và nội dung văn bản.
     */
    private static void sendResponse(HttpExchange exchange, int statusCode, String responseText) throws IOException {
        byte[] responseBytes = responseText.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    /**
     * Gửi phản hồi dạng JSON với Content-Type phù hợp.
     */
    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        sendResponse(exchange, statusCode, json);
    }

    /**
     * Gửi phản hồi lỗi dạng JSON.
     */
    private static void sendErrorResponse(HttpExchange exchange, int statusCode, String errorMessage) throws IOException {
        String jsonError = String.format("{\"error\": \"%s\"}", errorMessage.replace("\"", "\\\""));
        sendJsonResponse(exchange, statusCode, jsonError);
    }
}