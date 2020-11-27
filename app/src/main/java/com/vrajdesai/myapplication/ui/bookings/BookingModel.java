package com.vrajdesai.myapplication.ui.bookings;

import java.util.Date;

public class BookingModel {
    public BookingModel(String place_name, String address, Date timing) {
        Place_name = place_name;
        this.address = address;
        Timing = timing;
    }

    public BookingModel() {}

    public String getPlace_name() {
        return Place_name;
    }

    public void setPlace_name(String place_name) {
        Place_name = place_name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getTiming() {
        return Timing;
    }

    public void setTiming(Date timing) {
        Timing = timing;
    }

    private String Place_name;
    private String address;
    private Date Timing;
}
