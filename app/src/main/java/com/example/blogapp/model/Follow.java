package com.example.blogapp.model;

import java.util.Date;

public class Follow {
    private String follower;
    private String followed;
    private Long time;

    public Follow() {

    }

    public Follow(String follower, String followed, Long time) {
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

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "Follow{" +
                "follower='" + follower + '\'' +
                ", followed='" + followed + '\'' +
                ", time=" + time +
                '}';
    }
}
