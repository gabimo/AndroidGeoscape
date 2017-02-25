package com.lawnscape;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Mellis on 2/17/2017.
 */

public class User implements Parcelable{
    private String name;
    private String location;
    private String userid;

    public User(String n, String l){
        name = n;
        location = l;
    }
    //for chats
    public User(String n, String l, String u){
        name = n;
        location = l;
        userid = u;
    }

    public String getName(){
        return name;
    }
    public String getLocation(){
        return location;
    }
    public String getUserid() { return userid;}

    /***************** PARCEL PORTION *****************/
//Assumes the job has a post id assigned
    public User(Parcel in){
        String[] data= new String[3];

        in.readStringArray(data);
        this.name= data[0];
        this.location= data[1];
        this.userid = data[2];
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{
                this.name,
                this.location,
                this.userid
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

