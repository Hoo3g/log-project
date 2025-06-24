package com.search_log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.IndexRequest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class LogIngestor {

    private static final String DB_URL = "jdbc:postgresql://localhost:1234/storage_event";
    private static final String DB_USER = "admin";
    private static final String DB_PASSWORD = "Hus@334nt";
    private static final String INDEX_NAME = "storage-events";

    // Phương thức này sẽ được gọi bởi API Handler
    public int runIngest() throws Exception {
        // Client được tạo mỗi lần gọi, hoặc bạn có thể truyền nó vào
        OpenSearchClient client = LogSearcher.initializeClient();
        ObjectMapper jsonMapper = new ObjectMapper();
        int count = 0;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM storage_event ORDER BY created_at")) {

            System.out.println("Starting to ingest logs from PostgreSQL to OpenSearch via API call...");

            while (rs.next()) {
                Map<String, Object> eventDocument = new HashMap<>();
                String eventId = rs.getString("id");

                eventDocument.put("event_id", eventId);
                eventDocument.put("target_type", rs.getString("target_type"));
                eventDocument.put("target_id", rs.getString("target_id"));
                eventDocument.put("subject_type", rs.getString("subject_type"));
                eventDocument.put("subject_id", rs.getString("subject_id"));
                eventDocument.put("action", rs.getString("action"));
                eventDocument.put("correlation_id", rs.getString("correlation_id"));

                long createdAtMillis = rs.getLong("created_at");
                eventDocument.put("@timestamp", Instant.ofEpochMilli(createdAtMillis).toString());

                String jsonData = rs.getString("data");
                if (jsonData != null && !jsonData.isEmpty()) {
                    Map<String, Object> dataMap = jsonMapper.readValue(jsonData, new TypeReference<>() {});
                    eventDocument.put("data", dataMap);
                }

                IndexRequest<Map<String, Object>> request = new IndexRequest.Builder<Map<String, Object>>()
                        .index(INDEX_NAME)
                        .id(eventId)
                        .document(eventDocument)
                        .build();

                client.index(request);
                count++;
                System.out.println("Indexed event: " + eventId);
            }
            System.out.println("------------------------------------------");
            System.out.println("Successfully ingested " + count + " log events.");

        }
        return count;
    }
}