package com.lawnscape;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Map;

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
    private Map<String,String> activeworkers;
    private Map<String,String> requesters;
    private String date;
    private String latitude, longitude;

    public Job(String postDate, String t, String l, String u, String lat, String lng) {
        date = postDate;
        title = t;
        location = l;
        userid = u;
        postid = "";
        latitude = lat;
        longitude = lng;
    }

    public Job(String postDate, String t, String l, String d, String u, String lat, String lng) {
        date = postDate;
        title = t;
        location = l;
        description = d;
        userid = u;
        postid = "";
        latitude = lat;
        longitude = lng;
    }
    public Job(String postDate, String t, String l, String d, String u, String p, String lat, String lng) {
        date = postDate;
        title = t;
        location = l;
        description = d;
        userid = u;
        postid = p;
        latitude = lat;
        longitude = lng;
    }

    public String getLatitude(){return latitude;}
    public String getLongitude(){return longitude;}
    public String getPostid(){ return postid; }

    public Map<String,String> getActiveworkers(){return activeworkers;}
    public Map<String,String> getRequesters(){return requesters;}

    public void setActiveworker(String key, String userid){activeworkers.put(key,userid); }

    public String getDescription() {
        return description;
    }
    public void setDescription(String d){ description = d;}

    public String getTitle() {
        return title;
    }
    public void setTitle(String t){
        title = t;
    }
    public String getUserid() {
        return userid;
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String l){
        location = l;
    }

    public String getDate() { return date;}
    public void setDate(String newDate){date = newDate;}


/***************** PARCEL PORTION *****************/
//Assumes the job has a post id assigned
    public Job(Parcel in){
        String[] data= new String[8];

        in.readStringArray(data);
        this.title= data[0];
        this.location= data[1];
        this.description= data[2];
        this.userid= data[3];
        this.postid= data[4];
        this.date= data[5];
        this.latitude = data[6];
        this.longitude= data[7];
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
                this.postid,
                this.date,
                this.latitude,
                this.longitude
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
