package com.lawnscape;

/**
 * Created by Mellis on 2/17/2017.
 */

public class Job {
    private String title;
    private String location;
    private String description;
    private String userid;

    public Job(String t, String l, String u) {
        title = t;
        location = l;
        userid = u;
    }

    public Job(String t, String l, String d, String u) {
        title = t;
        location = l;
        description = d;
        userid = u;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public String getUserid() {
        return userid;
    }

    public String getLocation() {
        return location;
    }
}
