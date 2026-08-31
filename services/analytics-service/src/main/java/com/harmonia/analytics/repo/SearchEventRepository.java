package com.harmonia.analytics.repo;

import com.harmonia.analytics.domain.SearchEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SearchEventRepository extends JpaRepository<SearchEvent, UUID> {

    List<SearchEvent> findTop50ByOrderByOccurredAtDesc();
}
