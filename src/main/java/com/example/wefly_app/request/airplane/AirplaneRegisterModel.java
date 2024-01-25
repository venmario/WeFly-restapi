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
    @NotEmpty(message = "name is required")
    private String name;
    @NotEmpty(message = "type is required")
    private String type;
    @NotNull(message = "seats configuration is required")
    @Valid
    private List<AirplaneSeatRegisterModel> seats;
    @NotNull(message = "airline id is required")
    private Long airlineId;
}
