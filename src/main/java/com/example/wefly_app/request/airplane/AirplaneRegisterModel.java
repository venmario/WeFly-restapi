package com.example.wefly_app.request.airplane;

import lombok.Data;
import lombok.Getter;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.validation.constraints.NotEmpty;

@Data
public class AirplaneRegisterModel {
    @NotEmpty(message = "name is required")
    private String name;
    @NotEmpty(message = "type is required")
    private String type;
}
