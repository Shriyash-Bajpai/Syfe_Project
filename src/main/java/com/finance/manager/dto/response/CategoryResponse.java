package com.finance.manager.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.finance.manager.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for a category.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private String name;
    private TransactionType type;

    @JsonProperty("isCustom")
    private boolean isCustom;

    @JsonProperty("custom")
    public boolean isCustomAlias() {
        return isCustom;
    }
}
