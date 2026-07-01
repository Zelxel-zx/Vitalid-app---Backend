package com.vitalid.dtos.health;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para response de glucosa en sangre
 * 
 * TODO: Implement BloodSugarResponse with:
 * - date (date of measurement)
 * - value (glucose level in mg/dl or mmol/L)
 */
@Data
@NoArgsConstructor
public class BloodSugarResponse {
    private String date;
    private Double value;

}



