package com.example.blogapp.model;

import java.util.Date;

public class Follow {
    private String follower;
    private String followed;
    private Date time;

    public Follow() {

    }

    public Follow(String follower, String followed, Date time) {
        this.follower = follower;
        this.followed = followed;
        this.time = time;
    }

    public String getFollower() {
        return follower;
    }

    public void setFollower(String follower) {
        this.follower = follower;
    }

    public String getFollowed() {
        return followed;
    }

    public void setFollowed(String followed) {
        this.followed = followed;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }


}
