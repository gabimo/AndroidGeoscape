package com.lawnscape;

/**
 * Created by Mellis on 2/17/2017.
 */

public class User {
    private String name;
    private String location;

    public User(String n, String l){
        name = n;
        location = l;
    }

    public String getName(){
        return name;
    }
    public String getLocation(){
        return location;
    }

}
