package com.example.blogapp.model;

public class LikedBlog {
    private String userId;
    private String blogId;

    public LikedBlog() {

    }

    public LikedBlog(String userId, String blogId) {
        this.userId = userId;
        this.blogId = blogId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBlogId() {
        return blogId;
    }

    public void setBlogId(String blogId) {
        this.blogId = blogId;
    }
}
