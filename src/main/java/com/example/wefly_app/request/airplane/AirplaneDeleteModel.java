package com.example.wefly_app.request.airplane;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class AirplaneDeleteModel {
    @NotEmpty(message = "confirmation is required")
    @Pattern(regexp = "Delete Airplane Data", message = "To proceed with deletion, type 'Delete Airplane Data'")
    private String confirmation;
}
