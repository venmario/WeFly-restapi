package com.example.wefly_app.request.checkin;

import lombok.Data;

@Data
public class CheckinRequestModel {
    private String bookingCode;
    private String ordererLastName;
}
