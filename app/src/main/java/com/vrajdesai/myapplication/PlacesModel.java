package com.vrajdesai.myapplication;

public class PlacesModel {
    public PlacesModel(String name, String address, String open, String close) {
        Name = name;
        this.address = address;
        this.open = open;
        this.close = close;
    }

    public PlacesModel() { }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    private String Name;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    private String address;

    private String open;

    public String getOpen() {
        return open;
    }

    public void setOpen(String open) {
        this.open = open;
    }

    public String getClose() {
        return close;
    }

    public void setClose(String close) {
        this.close = close;
    }

    private String close;


}
