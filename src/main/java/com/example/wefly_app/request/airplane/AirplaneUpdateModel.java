package com.example.wefly_app.request.airplane;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class AirplaneUpdateModel {
    @NotEmpty(message = "name is required")
    private String name;
    @NotEmpty(message = "type is required")
    private String type;
}
