package com.example.blogapp.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Blog implements Serializable {
    @SerializedName("blogId")
    private String blogId;

    @SerializedName("title")
    private String title;

    @SerializedName("content")
    private String content;

    @SerializedName("createdTime")
    private String createdTime;

    @SerializedName("userId")
    private String userId;

    @SerializedName("likesNumber")
    private int likesNumber;

    @SerializedName("viewsNumber")
    private int viewsNumber;

    @SerializedName("category")
    private String category;

    @SerializedName("status")
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
