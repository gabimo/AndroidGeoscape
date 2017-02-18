package com.lawnscape;

/**
 * Created by Mellis on 2/17/2017.
 */

public class User {
    private String name;
    private String location;
    private String jobid;

    public User(String n, String l){
        name = n;
        location = l;
    }
    public User(String n, String l, String j){
        name = n;
        location = l;
        jobid = j;
    }

    public String getName(){
        return name;
    }
    public String getLocation(){
        return location;
    }

    public void setJobID(String id){
        jobid = id;
    }
    public String getJobID(){
        return jobid;
    }

}
