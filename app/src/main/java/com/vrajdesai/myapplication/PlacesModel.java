package com.vrajdesai.myapplication;

public class PlacesModel {
    public PlacesModel(String name, String address, int open, int close) {
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

    public int getOpen() {
        return open;
    }

    public void setOpen(int open) {
        this.open = open;
    }

    private int open;
    private int close;

    public int getClose() {
        return close;
    }

    public void setClose(int close) {
        this.close = close;
    }

}
