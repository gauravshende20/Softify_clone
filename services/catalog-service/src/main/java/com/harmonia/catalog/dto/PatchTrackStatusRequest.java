package com.harmonia.catalog.dto;

import com.harmonia.catalog.domain.TrackStatus;
import jakarta.validation.constraints.NotNull;

public record PatchTrackStatusRequest(@NotNull TrackStatus status) {
}
