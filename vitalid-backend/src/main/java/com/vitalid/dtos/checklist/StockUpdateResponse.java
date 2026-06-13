package com.vitalid.dtos.checklist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdateResponse {

    private Long medicationId;
    private Integer purchasedQuantity;
    private Integer pillsRemaining;
    private Boolean lowStock;
}
