package com.example.wefly_app.request.airplane;

import lombok.Data;
import lombok.Getter;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class AirplaneRegisterModel {
    @NotEmpty(message = "type is required")
    private String type;
    @NotEmpty(message = "code is required")
    private String code;
    @NotNull(message = "seats configuration is required")
    @Valid
    private List<SeatConfigRegisterModel> seats;
    @NotNull(message = "airline id is required")
    private Long airlineId;
}
