package com.example.wefly_app.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AirportDeleteModel {
    @NotNull(message = "id is required")
    private Long id;
}
