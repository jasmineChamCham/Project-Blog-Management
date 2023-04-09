package com.example.blogapp.model;

import com.google.android.gms.tasks.OnCompleteListener;

public class Comment {
    private String commentId;
    private String commentContent;
    private String userId;
    private String blogId;
    private Long createdTime;

    public Comment() {

    }

    public Comment(String commentId, String commentContent, String userId, String blogId, Long createdTime) {
        this.commentId = commentId;
        this.commentContent = commentContent;
        this.userId = userId;
        this.blogId = blogId;
        this.createdTime = createdTime;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent;
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

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }
}
