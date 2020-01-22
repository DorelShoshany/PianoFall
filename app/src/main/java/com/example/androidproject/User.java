package com.example.androidproject;


import androidx.annotation.NonNull;

public class User {

    public String username;
    public String password;
    public UserLocation location;
    public int points;

    public User(String username, String password, UserLocation location, int points) {
        this.username = username;
        this.password = password;
        this.location= location;
        this.points=points;
    }

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(com.example.androidproject.User.class)
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setLocation(UserLocation userLocation) {
        this.location = userLocation;
    }

    public void setPoints(int points) {
        this.points = points;
    }



    public int getPoints() {
        return points;
    }

    public UserLocation getLocation() {
        return location;
    }

    @NonNull
    @Override
    public String toString() {
        return "Name: " + username + ", From: "+ location + ", Points: "+ points;
    }
}
