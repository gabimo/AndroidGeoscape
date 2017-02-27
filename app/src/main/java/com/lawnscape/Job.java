package com.lawnscape;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Mellis on 2/17/2017.
 *
 * By being a parcelable object, any Job object can be passed as an intent extra
 * See the bottom half of this class file to understand
 */

public class Job implements Parcelable {
    private String title;
    private String location;
    private String description;
    private String userid;
    private String postid;
    private String activeworker;
    private String date;

    public Job(String postDate, String t, String l, String u) {
        date = postDate;
        title = t;
        location = l;
        userid = u;
        postid = "";
    }

    public Job(String postDate, String t, String l, String d, String u) {
        date = postDate;
        title = t;
        location = l;
        description = d;
        userid = u;
        postid = "";
        activeworker = "";
    }
    public Job(String postDate, String t, String l, String d, String u, String p) {
        date = postDate;
        title = t;
        location = l;
        description = d;
        userid = u;
        postid = p;
        activeworker = "";
    }

    public void setPostid(String p){
        postid=p;
    }

    public String getPostid(){ return postid; }

    public String getActiveworker(){return activeworker;}

    public void setActiveworker(String worker){activeworker = worker; }

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

    public String getDate() { return date;}
    public void setDate(String newDate){date = newDate;}


/***************** PARCEL PORTION *****************/
//Assumes the job has a post id assigned
    public Job(Parcel in){
        String[] data= new String[5];

        in.readStringArray(data);
        this.title= data[0];
        this.location= data[1];
        this.description= data[2];
        this.userid= data[3];
        this.postid= data[4];
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{
                this.title,
                this.location,
                this.description,
                this.userid,
                this.postid
        });
    }

    public static final Parcelable.Creator<Job> CREATOR= new Parcelable.Creator<Job>() {
        @Override
        public Job createFromParcel(Parcel in) {
            //using parcelable constructor
            return new Job(in);
        }
        @Override
        public Job[] newArray(int size) {
            return new Job[size];
        }
    };
}
