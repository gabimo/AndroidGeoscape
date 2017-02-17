package com.lawnscape;

/**
 * Created by Mellis on 2/17/2017.
 */

public class Job {
    public String title;
    public String location;
    public String description;

    public Job(String t, String l) {
        title = t;
        location = l;
    }

    public Job(String t, String l, String d) {
        title = t;
        location = l;
        description = d;
    }

}
