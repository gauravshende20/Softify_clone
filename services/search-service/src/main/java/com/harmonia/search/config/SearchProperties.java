package com.harmonia.search.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "harmonia.search")
public class SearchProperties {

    /**
     * OpenSearch cluster URL. Override with OPENSEARCH_URL.
     */
    private String url = "http://localhost:9200";
    private String index = "harmonia-search";

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }
}
