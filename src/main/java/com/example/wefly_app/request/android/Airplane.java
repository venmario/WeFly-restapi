package com.example.wefly_app.request.android;

import com.example.wefly_app.entity.AbstractDate;
import com.example.wefly_app.entity.Airline;
import com.example.wefly_app.entity.SeatConfig;
import lombok.Data;

import java.util.List;

@Data
public class Airplane extends AbstractDate {
    private Long id;
    private String name;
    private String type;
    private Airline airline;
    private List<SeatConfig> seatConfigs;
}
