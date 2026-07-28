package com.mvc.mock_project.dto.request.facility;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;
import java.math.BigDecimal;

@Data
public class BatchSavePriceRulesRequest {
    
    @NotNull(message = "facilitySportId is required")
    private Integer facilitySportId;

    @NotNull(message = "rows is required")
    private List<PriceRuleRow> rows;

    @Data
    public static class PriceRuleRow {
        private String startTime;
        private String endTime;
        private BigDecimal weekdayPrice;
        private BigDecimal weekendPrice;
    }
}
