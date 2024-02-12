package com.example.wefly_app.request.airplane;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class AirplaneUpdateModel {
    @NotEmpty(message = "code is required")
    private String code;
    @NotEmpty(message = "type is required")
    private String type;
}
