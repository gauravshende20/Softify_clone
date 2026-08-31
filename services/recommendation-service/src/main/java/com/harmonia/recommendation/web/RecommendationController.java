package com.harmonia.recommendation.web;

import com.harmonia.common.api.error.ErrorCode;
import com.harmonia.common.api.error.HarmoniaException;
import com.harmonia.common.security.CurrentUser;
import com.harmonia.recommendation.dto.HomeRecommendations;
import com.harmonia.recommendation.service.RecommendationService;
import com.harmonia.recommendation.spi.Recommendation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public List<Recommendation> recommendations(CurrentUser user) {
        return recommendationService.recommendations(requireUser(user));
    }

    @GetMapping("/home")
    public HomeRecommendations home(CurrentUser user) {
        return recommendationService.home(requireUser(user));
    }

    @GetMapping("/made-for-you")
    public List<Recommendation> madeForYou(CurrentUser user) {
        return home(user).madeForYou();
    }

    @GetMapping("/trending")
    public List<com.harmonia.recommendation.dto.TrackSnapshot> trending(CurrentUser user) {
        return home(user).trending();
    }

    @GetMapping("/new-releases")
    public List<com.harmonia.recommendation.dto.AlbumSnapshot> newReleases(CurrentUser user) {
        return home(user).newReleases();
    }

    @GetMapping("/popular-artists")
    public List<com.harmonia.recommendation.dto.ArtistSnapshot> popularArtists(CurrentUser user) {
        return home(user).popularArtists();
    }

    private static UUID requireUser(CurrentUser user) {
        if (user == null || user.id() == null) {
            throw HarmoniaException.unauthorized(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
        return user.id();
    }
}
