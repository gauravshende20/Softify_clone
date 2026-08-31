package com.harmonia.analytics.repo;

import com.harmonia.analytics.domain.EntityOpenEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EntityOpenEventRepository extends JpaRepository<EntityOpenEvent, UUID> {

    List<EntityOpenEvent> findTop50ByOrderByOccurredAtDesc();
}
