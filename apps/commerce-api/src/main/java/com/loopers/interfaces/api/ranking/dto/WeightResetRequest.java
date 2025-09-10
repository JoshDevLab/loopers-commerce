package com.loopers.interfaces.api.ranking.dto;

import com.loopers.domain.ranking.WeightResetCommand;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WeightResetRequest {
    private String reason;

    public WeightResetCommand toCommand() {
        return new WeightResetCommand(reason);
    }
}
