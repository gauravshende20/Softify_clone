package com.harmonia.search.index;

import com.harmonia.search.config.SearchProperties;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.mapping.Property;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class SearchIndexInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SearchIndexInitializer.class);

    private final OpenSearchClient client;
    private final SearchProperties properties;
    private volatile boolean ensured;

    public SearchIndexInitializer(OpenSearchClient client, SearchProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            ensureIndex();
        } catch (Exception e) {
            log.warn("OpenSearch unavailable at startup; {} will be created on first successful call",
                    properties.getIndex(), e);
        }
    }

    public synchronized void ensureIndex() throws Exception {
        if (ensured) {
            return;
        }
        String index = properties.getIndex();
        boolean exists = client.indices().exists(e -> e.index(index)).value();
        if (exists) {
            ensured = true;
            return;
        }
        TypeMapping mappings = new TypeMapping.Builder()
                .properties("id", Property.of(p -> p.keyword(k -> k)))
                .properties("type", Property.of(p -> p.keyword(k -> k)))
                .properties("title", Property.of(p -> p.text(t -> t
                        .fields("keyword", Property.of(f -> f.keyword(k -> k.ignoreAbove(256)))))))
                .properties("subtitle", Property.of(p -> p.text(t -> t)))
                .properties("text", Property.of(p -> p.text(t -> t)))
                .properties("popularity", Property.of(p -> p.integer(i -> i)))
                .properties("genre", Property.of(p -> p.keyword(k -> k)))
                .properties("createdAt", Property.of(p -> p.date(d -> d)))
                .build();
        client.indices().create(new CreateIndexRequest.Builder()
                .index(index)
                .mappings(mappings)
                .build());
        ensured = true;
        log.info("Created OpenSearch index {}", index);
    }
}
