package com.harmonia.search.service;

import com.harmonia.common.api.error.ErrorCode;
import com.harmonia.common.api.error.HarmoniaException;
import com.harmonia.search.config.SearchProperties;
import com.harmonia.search.document.SearchDocument;
import com.harmonia.search.dto.GroupedSearchResponse;
import com.harmonia.search.dto.SearchHit;
import com.harmonia.search.index.SearchIndexInitializer;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class SearchQueryService {

    private static final Logger log = LoggerFactory.getLogger(SearchQueryService.class);

    private final OpenSearchClient client;
    private final SearchProperties properties;
    private final SearchIndexInitializer initializer;

    public SearchQueryService(OpenSearchClient client,
                              SearchProperties properties,
                              SearchIndexInitializer initializer) {
        this.client = client;
        this.properties = properties;
        this.initializer = initializer;
    }

    public GroupedSearchResponse search(String q, String type, String genre, int page, int size) {
        if (q == null || q.isBlank()) {
            return GroupedSearchResponse.empty();
        }
        try {
            initializer.ensureIndex();
            Query query = buildSearchQuery(q.trim(), type, genre);
            SearchResponse<SearchDocument> response = client.search(s -> s
                            .index(properties.getIndex())
                            .from(Math.max(page, 0) * size)
                            .size(size)
                            .query(query)
                            .sort(so -> so.score(sc -> sc.order(SortOrder.Desc))),
                    SearchDocument.class);
            return GroupedSearchResponse.of(toHits(response));
        } catch (HarmoniaException e) {
            throw e;
        } catch (Exception e) {
            log.error("OpenSearch query failed for q={}", q, e);
            throw HarmoniaException.serviceUnavailable(ErrorCode.SEARCH_UNAVAILABLE, "Search is unavailable", e);
        }
    }

    public GroupedSearchResponse suggest(String q) {
        if (q == null || q.isBlank()) {
            return GroupedSearchResponse.empty();
        }
        String queryText = q.trim();
        try {
            initializer.ensureIndex();
            Query query = Query.of(root -> root.bool(b -> b
                    .should(s -> s.prefix(p -> p
                            .field("title.keyword")
                            .value(queryText)
                            .caseInsensitive(true)))
                    .should(s -> s.matchPhrasePrefix(m -> m.field("title").query(queryText)))
                    .should(s -> s.matchPhrasePrefix(m -> m.field("text").query(queryText)))
                    .minimumShouldMatch("1")));
            SearchResponse<SearchDocument> response = client.search(s -> s
                            .index(properties.getIndex())
                            .size(10)
                            .query(query),
                    SearchDocument.class);
            return GroupedSearchResponse.of(toHits(response));
        } catch (HarmoniaException e) {
            throw e;
        } catch (Exception e) {
            log.error("OpenSearch suggest failed for q={}", q, e);
            throw HarmoniaException.serviceUnavailable(ErrorCode.SEARCH_UNAVAILABLE, "Search is unavailable", e);
        }
    }

    private Query buildSearchQuery(String q, String type, String genre) {
        return Query.of(root -> root.bool(b -> {
            b.must(m -> m.multiMatch(mm -> mm
                    .query(q)
                    .fields("title^2", "text")
                    .fuzziness("AUTO")));
            if (type != null && !type.isBlank()) {
                b.filter(f -> f.term(t -> t.field("type").value(FieldValue.of(type.toLowerCase(Locale.ROOT)))));
            }
            if (genre != null && !genre.isBlank()) {
                b.filter(f -> f.term(t -> t.field("genre").value(FieldValue.of(genre))));
            }
            return b;
        }));
    }

    private static List<SearchHit> toHits(SearchResponse<SearchDocument> response) {
        List<SearchHit> hits = new ArrayList<>();
        if (response == null || response.hits() == null || response.hits().hits() == null) {
            return hits;
        }
        for (Hit<SearchDocument> hit : response.hits().hits()) {
            SearchDocument source = hit.source();
            if (source == null) {
                continue;
            }
            double score = hit.score() == null ? 0.0 : hit.score();
            hits.add(new SearchHit(source.id(), source.type(), source.title(), source.subtitle(), score));
        }
        return hits;
    }
}
