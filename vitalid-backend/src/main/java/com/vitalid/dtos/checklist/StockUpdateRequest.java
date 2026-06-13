package com.vitalid.dtos.checklist;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StockUpdateRequest {

    private Integer quantity;
    private String notes;
}
