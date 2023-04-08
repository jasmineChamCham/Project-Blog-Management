package com.example.blogapp.model;

import java.io.Serializable;

public class Blog implements Serializable {
    private String blogId;
    private String title;
    private String content;
    private String createdTime;
    private String userId;
    private int likesNumber;
    private int viewsNumber;
    private String category;
    private String status;

    public Blog() {

    }

    public Blog(String blogId, String title, String content, String createdTime, String userId, int likesNumber, int viewsNumber, String category, String status) {
        this.blogId = blogId;
        this.title = title;
        this.content = content;
        this.createdTime = createdTime;
        this.userId = userId;
        this.likesNumber = likesNumber;
        this.viewsNumber = viewsNumber;
        this.category = category;
        this.status = status;
    }

    public String getBlogId() {
        return blogId;
    }

    public void setBlogId(String blogId) {
        this.blogId = blogId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getLikesNumber() {
        return likesNumber;
    }

    public void setLikesNumber(int likesNumber) {
        this.likesNumber = likesNumber;
    }

    public int getViewsNumber() {
        return viewsNumber;
    }

    public void setViewsNumber(int viewsNumber) {
        this.viewsNumber = viewsNumber;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
