package com.example.androidproject;

public class UserLocation {

    private double x;
    private double y;

    public UserLocation() {
    }

    public UserLocation(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString(){
        return "x: " + "y "+ y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }


}
