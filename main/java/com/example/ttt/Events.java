package com.example.ttt;

public class Events {

    String EVENT;
    String TIME;
    String DAY;
    String MONTH;
    String YEAR;


    public Events(String EVENT, String TIME, String DAY, String MONTH, String YEAR) {
        this.EVENT = EVENT;
        this.TIME = TIME;
        this.DAY = DAY;
        this.MONTH = MONTH;
        this.YEAR = YEAR;
    }


    public String getEVENT() {
        return EVENT;
    }

    public String getTIME() {
        return TIME;
    }

    public String getDAY() {
        return DAY;
    }

    public String getMONTH() {
        return MONTH;
    }

    public String getYEAR() {
        return YEAR;
    }
}
