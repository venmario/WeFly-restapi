package com.example.wefly_app.request.checkin;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class CheckinRequestModel {
    @NotEmpty(message = "Booking Code Must Not Null")
    private String bookingCode;
    @NotEmpty(message = "Orderer Last Name Must Not Null")
    private String ordererLastName;
}
