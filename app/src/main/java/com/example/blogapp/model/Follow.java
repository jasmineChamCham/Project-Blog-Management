package com.example.blogapp.model;

public class Follow {
    private String followerId;
    private String followedId;
    private Long time;

    public Follow() {

    }

    public Follow(String followerId, String followedId, Long time) {
        this.followerId = followerId;
        this.followedId = followedId;
        this.time = time;
    }

    public String getFollowerId() {
        return followerId;
    }

    public void setFollowerId(String followerId) {
        this.followerId = followerId;
    }

    public String getFollowedId() {
        return followedId;
    }

    public void setFollowedId(String followedId) {
        this.followedId = followedId;
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
                "follower='" + followerId + '\'' +
                ", followed='" + followedId + '\'' +
                ", time=" + time +
                '}';
    }
}
