package com.harmonia.recommendation.spi;

import java.util.List;

public interface RecommendationEngine {

    List<Recommendation> recommend(RecommendationContext ctx);
}
