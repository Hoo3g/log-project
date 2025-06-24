package com.search_log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.RestClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.transport.rest_client.RestClientTransport;

import java.util.ArrayList;
import java.util.List;

public class LogSearcher {

    private static final String INDEX_NAME = "storage-events";
    private final OpenSearchClient client;

    public LogSearcher(OpenSearchClient client) {
        this.client = client;
    }

    public static OpenSearchClient initializeClient() {
        final String OPENSEARCH_HOST = "localhost";
        final int OPENSEARCH_PORT = 9200;

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("admin", "Hus@334nt"));

        RestClient restClient = RestClient.builder(
                new HttpHost(OPENSEARCH_HOST, OPENSEARCH_PORT, "http")
        ).setHttpClientConfigCallback(httpClientBuilder ->
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
        ).build();

        OpenSearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new OpenSearchClient(transport);
    }

    public List<ObjectNode> searchByFieldValue(String fieldName, String value) throws Exception {
        SearchResponse<ObjectNode> response = client.search(s -> s
                        .index(INDEX_NAME)
                        .query(q -> q
                                .match(m -> m
                                        .field(fieldName)
                                        .query(fieldValue -> fieldValue.stringValue(value))
                                )
                        ).size(20)
                        .sort(so -> so.field(f -> f.field("@timestamp").order(org.opensearch.client.opensearch._types.SortOrder.Desc)))
                , ObjectNode.class
        );
        return extractSources(response);
    }

    public List<ObjectNode> searchByNestedFieldValue(String nestedField, String value) throws Exception {
        String searchField = nestedField.endsWith(".keyword") ? nestedField : nestedField + ".keyword";
        SearchResponse<ObjectNode> response = client.search(s -> s
                        .index(INDEX_NAME)
                        .query(q -> q
                                .match(m -> m
                                        .field(searchField)
                                        .query(fieldValue -> fieldValue.stringValue(value))
                                )
                        ).size(20)
                        .sort(so -> so.field(f -> f.field("@timestamp").order(org.opensearch.client.opensearch._types.SortOrder.Desc)))
                , ObjectNode.class
        );
        return extractSources(response);
    }

    private List<ObjectNode> extractSources(SearchResponse<ObjectNode> response) {
        List<ObjectNode> results = new ArrayList<>();
        for (Hit<ObjectNode> hit : response.hits().hits()) {
            results.add(hit.source());
        }
        return results;
    }
}