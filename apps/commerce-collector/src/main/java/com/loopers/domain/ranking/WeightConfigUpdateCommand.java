package com.loopers.domain.ranking;

import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@Builder
public class WeightConfigUpdateCommand {
    private final String eventId;
    private final double viewWeight;
    private final double likeWeight;
    private final double orderWeight;
    private final ZonedDateTime occurredAt;
}
