package com.example.wefly_app.request.airplane;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class SeatConfigRegisterModel {
    @NotEmpty(message = "seatClass is required")
    @Pattern(regexp = "ECONOMY|BUSINESS", message = "seatClass must be one of the following: ECONOMY or BUSINESS")
    private String seatClass;
    @NotNull(message = "number of seat row is required")
    private Integer numberOfRow;
    @NotNull(message = "number of seat column is required")
    private Integer numberOfColumn;
}
