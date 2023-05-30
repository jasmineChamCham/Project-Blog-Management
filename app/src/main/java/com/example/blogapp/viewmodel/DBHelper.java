package com.example.blogapp.viewmodel;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;

import com.example.blogapp.model.Blog;
import com.example.blogapp.model.Comment;
import com.example.blogapp.model.Follow;
import com.example.blogapp.model.LikedBlog;
import com.example.blogapp.model.User;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBHelper {
    private FirebaseDatabase database;
    public FirebaseRecyclerOptions<Blog> optionAllBlogs, optionExplore, optionBlogByUserId;
    public FirebaseRecyclerOptions<Comment> optionComment;
    public FirebaseRecyclerOptions<Follow> followOptions;
    private DatabaseReference blogsRef, usersRef, commentsRef, followRef, likedBlogsRef, followersRef;
    public static ArrayList<Follow> followList = new ArrayList<>();
    private Context context;

    public DBHelper(Context context) {
        this.context = context;
        database = FirebaseDatabase.getInstance();

        blogsRef = database.getReference().child("blogs");
        usersRef = database.getReference().child("users");
        commentsRef = database.getReference().child("comments");
        followRef = database.getReference().child("follows");
        likedBlogsRef = database.getReference().child("likedBlogs");
        followersRef = database.getReference().child("followers");

        followOptions = new FirebaseRecyclerOptions.Builder<Follow>()
                .setQuery(followRef, Follow.class)
                .build();

        optionAllBlogs = new FirebaseRecyclerOptions.Builder<Blog>()
                .setQuery(blogsRef, Blog.class)
                .build();

        optionExplore = new FirebaseRecyclerOptions.Builder<Blog>()
                .setQuery(blogsRef, Blog.class)
                .build();
    }

    public void addBlog(String title, String content, Long createdTime, String userID, int likeNumber, int viewNumber, String category, String status) {

        String blogId = blogsRef.push().getKey();
        blogsRef.child(blogId).setValue(new Blog(blogId, title, content, createdTime, userID, likeNumber, viewNumber, category, status))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("DEBUG","Add blog successful!");
                        } else {
                            Log.d("DEBUG","Add blog fail!");
                        }
                    }
                });
    }

    public FirebaseRecyclerOptions<Blog> getOptionBlogByUserId(String userId) {
        Query query = blogsRef.orderByChild("userId").equalTo(userId);
        optionBlogByUserId = new FirebaseRecyclerOptions.Builder<Blog>()
                .setQuery(query, Blog.class)
                .build();
        return optionBlogByUserId;
    }

    public void getBlogsByUserId(String userId, onBlogsListener listener) {
        List<Blog> blogs = new ArrayList<>();
        blogsRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChildren()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                        Log.d("DEBUG", dataSnapshot.getKey());
                        Blog blog = dataSnapshot.getValue(Blog.class);
                        if (blog.getStatus().equals("Published")) {
                            blogs.add(blog);
                        }
                    }
                    sortBlogByCreatedTime(blogs);
                    for (int i = 0; i < blogs.size(); i++) {
                        Log.d("DEBUG", "sorted author's blog: " + blogs.get(i).getBlogId());
                    }
                    listener.onBlogsRetrieved(blogs);
                }
                else {
                    listener.onBlogsRetrieved(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("DEBUG", "Database error: " + databaseError.getMessage());
            }
        });
    }

    public void updateBlog(String blogId, String title, String content, Long createdTime, String userID, int likeNumber, int viewNumber, String category, String status) {
        blogsRef.child(blogId).setValue(new Blog(blogId, title, content, createdTime, userID, likeNumber, viewNumber, category, status))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d("DEBUG","Edit blog successful!");
                    }
                    else {
                        Log.d("DEBUG","Edit blog fail!");
                    }
                }
        });
    }

    public void deleteBlog(String id){
        blogsRef.child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(context,"Delete blog successful!", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(context,"Delete blog fail!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void getBlogById(String blogId, onBlogListener listener) {
        DatabaseReference oneBlogRef = blogsRef.child(blogId);
        oneBlogRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Blog blog = dataSnapshot.getValue(Blog.class);
                listener.onBlogRetrieved(blog);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("DEBUG", "Database error: " + databaseError.getMessage());
            }
        });
    }
    public interface onBlogListener {
        void onBlogRetrieved(Blog blog);
    }

    public void sortBlogByCreatedTime(List<Blog> blogList) {
        for (int i = 0; i < blogList.size(); i++) {
            for (int j = i+1; j < blogList.size(); j++) {
                if (blogList.get(i).getCreatedTime() < blogList.get(j).getCreatedTime()) {
                    Blog temp = blogList.get(i);
                    blogList.set(i, blogList.get(j));
                    blogList.set(j, temp);
                }
            }
        }
    }

    public void getFollowingBlogList(String userId, onBlogsListener listener) {
        List<Blog> blogs = new ArrayList<>();
        blogsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                    String blogId = dataSnapshot.getKey();
                    Blog blog = dataSnapshot.getValue(Blog.class);
                    if (!blog.getUserId().equals(userId) && blog.getStatus().equals("Published")) {     // pass over my blog
                        isFollowing(userId, blog.getUserId(), isFollowing -> {
                            Log.d("DEBUG", "userLogin follow " + blog.getUserId() + ": " + isFollowing);
                            if (isFollowing) {
                                blogs.add(blog);
                                sortBlogByCreatedTime(blogs);
                                listener.onBlogsRetrieved(blogs);
                            }
                        });
                    }
                    listener.onBlogsRetrieved(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("DEBUG", "Database error: " + databaseError.getMessage());
            }
        });
    }
    public interface onBlogsListener {
        void onBlogsRetrieved(List<Blog> blogList);
    }

    public void getExploreBlogList(String userId, onBlogsListener listener) {
        List<Blog> blogs = new ArrayList<>();
        blogsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                    String blogId = dataSnapshot.getKey();
                    Blog blog = dataSnapshot.getValue(Blog.class);
                    if (!blog.getUserId().equals(userId) && blog.getStatus().equals("Published")) {     // pass over my blog
                        isFollowing(userId, blog.getUserId(), isFollowing -> {
                            Log.d("DEBUG", "userLogin follow " + blog.getUserId() + ": " + isFollowing);
                            if (!isFollowing) {
                                blogs.add(blog);
                                sortBlogByCreatedTime(blogs);
                                listener.onBlogsRetrieved(blogs);
                            }
                        });
                    }
                    listener.onBlogsRetrieved(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("DEBUG", "Database error: " + databaseError.getMessage());
            }
        });
    }

    public void authentication(String email, String password, onUserListener listener) {
        Query emailQuery = usersRef.orderByChild("email").equalTo(email);
        emailQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    // email exists in the database
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user.getPassword().equals(password)) {
                            // user exists with email and password
                            Log.d("DEBUG", "Exist user to login");
                            listener.onUserRetrieved(user);
                            return;
                        }
                    }
                    // email exists but password is incorrect
                    Log.d("DEBUG", "Incorrect password");
                    listener.onUserRetrieved(null);
                }
                else {
                    // email does not exist in the database
                    Log.d("DEBUG", "Email does not exist in the database");
                    listener.onUserRetrieved(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("DEBUG", "Database error: " + databaseError.getMessage());
            }
        });
    }

    public void isExistAccount(String email, onIsExistAccountListener listener) {
        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    Log.d("DEBUG", "isExistAccount: " + String.valueOf(dataSnapshot.hasChildren()));
                    listener.onIsExistAccountRetrieved(true);
                }
                else {
                    Log.d("DEBUG", "isExistAccount: " + String.valueOf(dataSnapshot.hasChildren()));
                    listener.onIsExistAccountRetrieved(false);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("DEBUG", "Database error: " + databaseError.getMessage());
            }
        });
    }
    public interface onIsExistAccountListener {
        void onIsExistAccountRetrieved(boolean isExist);
    }

    public void addUser(String name, String email, String pass, String birthday, String ava) {
        String id = usersRef.push().getKey();
        usersRef.child(id).setValue(new User(id, name, email, pass, birthday, ava))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("DEBUG","Add user successful!");
                        } else {

                            Log.d("DEBUG","Add user fail!");}
                    }
                });
    }

    public void getUserById(String userId, onUserListener listener) {
        DatabaseReference oneUserRef = usersRef.child(userId);
        oneUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get the user data as a User object
                User user = dataSnapshot.getValue(User.class);
                listener.onUserRetrieved(user);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("DEBUG", "Database error: " + databaseError.getMessage());
            }
        });
    }
    public interface onUserListener {
        void onUserRetrieved(User user);
    }

    public void updateUser(String userId, String name, String email, String password,
                           String birthday, String ava) {
        usersRef.child(userId).setValue(new User(userId, name, email, password, birthday, ava))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("DEBUG","Update user successful!");
                        }
                        else {
                            Log.d("DEBUG","Update user fail!");
                        }
                    }
                });
    }

    public void addComment(String commentContent, String userId, String blogId) {
        String commentId = commentsRef.push().getKey();
        commentsRef.child(blogId).child(commentId).setValue(new Comment(commentId, commentContent, userId, blogId, System.currentTimeMillis()))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("DEBUG","Add comment successful!");
                        }
                        else {
                            Log.d("DEBUG","Add comment fail!");
                        }
                    }
                });
    }

    public void updateComment(Comment comment) {
        commentsRef.child(comment.getBlogId()).child(comment.getCommentId()).setValue(new Comment(comment.getCommentId(), comment.getCommentContent(),
                                                            comment.getUserId(), comment.getBlogId(), comment.getCreatedTime()))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("DEBUG","Update comment successful!");
                        }
                        else {
                            Log.d("DEBUG","Update comment fail!");
                        }
                    }
                });
    }

    public void deleteComment(Comment comment) {
        commentsRef.child(comment.getBlogId()).child(comment.getCommentId()).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("DEBUG","Delete comment successful!");
                        }
                        else {
                            Log.d("DEBUG","Delete comment fail!");
                        }
                    }
                });
    }

    public FirebaseRecyclerOptions<Comment> getOptionCommentByBlogId(String blogId){
        Query query = commentsRef.child(blogId).orderByChild("createdTime").limitToLast(10);
        optionComment = new FirebaseRecyclerOptions.Builder<Comment>()
                .setQuery(query, Comment.class)
                .build();
        return optionComment;
    }

    public void getCommentsByBlogId(String blogId, onOptionCommentListener listener) {
        Query query = commentsRef.child(blogId).orderByChild("createdTime").limitToLast(10);
        optionComment = new FirebaseRecyclerOptions.Builder<Comment>()
                .setQuery(query, Comment.class)
                .build();
        listener.onOptionCommentRetrieved(optionComment);
        commentsRef.child(blogId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() && snapshot.getChildrenCount() == 0) {
                    // Comments not found
                    Log.d("DEBUG", "comment not found");
                    listener.onOptionCommentRetrieved(null);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("DEBUG", "Error retrieving comments for blog post " + blogId, error.toException());
            }
        });
    }
    public interface onOptionCommentListener {
        void onOptionCommentRetrieved(FirebaseRecyclerOptions<Comment> options);
    }

    public void isLiked(String userId, String blogId, onIsLikedListener listener) {
        likedBlogsRef.child(blogId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isExistUserId = snapshot.hasChild(userId);
                if (isExistUserId) {
                    listener.onIsLikedRetrieved(true);
                }
                else {
                    listener.onIsLikedRetrieved(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("DEBUG", "Database error: " + databaseError.getMessage());
            }
        });
    }
    public interface onIsLikedListener {
        void onIsLikedRetrieved(boolean isLiked);
    }

    public void addLikedBlog(String userId, String blogId) {
        getBlogById(blogId, blog -> {
            int likeCount = blog.getLikesNumber()+1;    // change likesNumber
            updateBlog(blog.getBlogId(), blog.getTitle(), blog.getContent(),
                    blog.getCreatedTime(), blog.getUserId(), likeCount,
                    blog.getViewsNumber(), blog.getCategory(), blog.getStatus());
        });
        isLiked(userId, blogId, isLiked -> {
            if (!isLiked) {
                likedBlogsRef.child(blogId).child(userId).setValue(new LikedBlog(userId, blogId))
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d("DEBUG","Add liked blog successful!");
                                }
                                else {
                                    Log.d("DEBUG","Add liked blog fail!");
                                }
                            }
                        });
            }
        });
    }

    public void deleteLikedBlog(LikedBlog likedBlog) {
        String blogId = likedBlog.getBlogId();
        getBlogById(blogId, blog -> {
            int likeCount = blog.getLikesNumber()-1;    // change likesNumber
            updateBlog(blog.getBlogId(), blog.getTitle(), blog.getContent(),
                        blog.getCreatedTime(), blog.getUserId(), likeCount,
                        blog.getViewsNumber(), blog.getCategory(), blog.getStatus());
        });
        likedBlogsRef.child(likedBlog.getBlogId()).child(likedBlog.getUserId()).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("DEBUG","Delete liked blog successful!");
                        }
                        else {
                            Log.d("DEBUG","Delete liked blog fail!");
                        }
                    }
                });
    }

    public void getLikedBlogIdsByUserId(String userId, onLikedBlogIdsListener listener) {
        List<String> likedBlogIds = new ArrayList<>();
        likedBlogsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    boolean isExistUserId = snapshot.hasChild(userId);
                    Log.d("DEBUG", Boolean.toString(isExistUserId));
                    if (isExistUserId) {
                        String blogId = snapshot.getKey();
                        likedBlogIds.add(blogId);
                    }
                }
                if (likedBlogIds.isEmpty()) {
                    listener.onLikedBlogIdsRetrieved(null);
                }
                else {
                    listener.onLikedBlogIdsRetrieved(likedBlogIds);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("DEBUG", "Database error: " + databaseError.getMessage());
            }
        });
    }
    public interface onLikedBlogIdsListener {
        void onLikedBlogIdsRetrieved(List<String> likedBlogIds);
    }

    public void getLikedBlogs(String userId, onLikedBlogsListener listener) {
        List<Blog> likedBlogList = new ArrayList<>();
        getLikedBlogIdsByUserId(userId, likedBlogIds -> {
            if (likedBlogIds != null) {
                for (int i = 0; i < likedBlogIds.size(); i++) {
                    blogsRef.child(likedBlogIds.get(i)).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Blog blog = dataSnapshot.getValue(Blog.class);
                            if (blog.getStatus().equals("Published")) {
                                likedBlogList.add(blog);
                                for (int i = 0; i < likedBlogList.size(); i++) {
                                    Log.d("DEBUG", "dbHelper_liked blog id: " + likedBlogList.get(i).getBlogId());
                                }
                                sortBlogByCreatedTime(likedBlogList);
                                listener.onLikedBlogsRetrieved(likedBlogList);
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d("DEBUG", "Database error: " + databaseError.getMessage());
                        }
                    });
                }
            }
            else {
                listener.onLikedBlogsRetrieved(null);
            }
        });
    }
    public interface onLikedBlogsListener {
        void onLikedBlogsRetrieved(List<Blog> likedBlogList);
    }

    public void getFollowedIdsByFollowerId(String followerId, onFollowedIdsListener listener) {
        List<String> followedIdsList = new ArrayList<>();
        followersRef.child(followerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Log.d("DEBUG", "followed id: " + dataSnapshot.getKey());
                    followedIdsList.add(dataSnapshot.getKey());
                }
                if (followedIdsList.isEmpty()) {
                    listener.onFollowedIdsRetrieved(null);
                }
                else {
                    listener.onFollowedIdsRetrieved(followedIdsList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("DEBUG", "Database error: " + databaseError.getMessage());
            }
        });
    }
    public interface onFollowedIdsListener {
        void onFollowedIdsRetrieved(List<String> followedIds);
    }

    public void getFollowingListOfUser(String userId, onUserListListener listener) {
        List<User> followingList = new ArrayList<>();
        getFollowedIdsByFollowerId(userId, followedIds -> {
            if (followedIds != null) {
                for (int i = 0; i < followedIds.size(); i++) {
                    usersRef.child(followedIds.get(i)).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);
                            followingList.add(user);
                            for (int i = 0; i < followingList.size(); i++) {
                                Log.d("DEBUG", "dbHelper_following user id: " + followingList.get(i).getUserId());
                            }
                            listener.onUserListRetrieved(followingList);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.d("DEBUG", "Database error: " + databaseError.getMessage());
                        }
                    });
                }
            }
            else {
                listener.onUserListRetrieved(null);
            }
        });
    }
    public interface onUserListListener {
        void onUserListRetrieved(List<User> userList);
    }

    public void getFollowerIdsByFollowedId(String followedId, onFollowerIdsListener listener) {
        List<String> followerIdsList = new ArrayList<>();
        followersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        if (dataSnapshot1.getKey().equals(followedId)) {
                            Log.d("DEBUG", "follower id: " + dataSnapshot.getKey());
                            followerIdsList.add(dataSnapshot.getKey());
                        }
                    }
                }
                if (followerIdsList.isEmpty()) {
                    listener.onFollowerIdsRetrieved(null);
                }
                else {
                    listener.onFollowerIdsRetrieved(followerIdsList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("DEBUG", "Database error: " + databaseError.getMessage());
            }
        });
    }
    public interface onFollowerIdsListener {
        void onFollowerIdsRetrieved(List<String> followerIds);
    }

    public void getFollowerListOfUser(String userId, onUserListListener listener) {
        List<User> followerList = new ArrayList<>();
        getFollowerIdsByFollowedId(userId, followerIds -> {
            if (followerIds != null) {
                for (int i = 0; i < followerIds.size(); i++) {
                    usersRef.child(followerIds.get(i)).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);
                            followerList.add(user);
                            for (int i = 0; i < followerList.size(); i++) {
                                Log.d("DEBUG", "dbHelper_follower user id: " + followerList.get(i).getUserId());
                            }
                            listener.onUserListRetrieved(followerList);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.d("DEBUG", "Database error: " + databaseError.getMessage());
                        }
                    });
                }
            }
            else {
                listener.onUserListRetrieved(null);
            }
        });
    }

    public void isFollowing(String followerId, String followedId, onIsFollowingListener listener) {
        followersRef.child(followerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isExistFollowedId = snapshot.hasChild(followedId);
                if (isExistFollowedId) {
                    listener.onIsFollowingRetrieved(true);
                }
                else {
                    listener.onIsFollowingRetrieved(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("DEBUG", "Database error: " + databaseError.getMessage());
            }
        });
    }
    public interface onIsFollowingListener {
        void onIsFollowingRetrieved(boolean isFollowing);
    }

    public void addFollowRecord(String followerId, String followedId) {
        followersRef.child(followerId).child(followedId)
                .setValue(new Follow(followerId, followedId, System.currentTimeMillis()))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("DEBUG","Add follow record successful!");
                        }
                        else {
                            Log.d("DEBUG","Add follow record fail!");
                        }
                    }
                });
    }

    public void deleteFollowRecord(String followerId, String followedId) {
        followersRef.child(followerId).child(followedId).removeValue()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d("DEBUG","Delete follow record successful!");
                                }
                                else {
                                    Log.d("DEBUG","Delete follow record fail!");
                                }
                            }
                        });
    }

    public void getBlogCountOfUser(String userId, onCountListener listener) {
        blogsRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                Log.d("DEBUG", "total " + String.valueOf(snapshot.getChildrenCount()));
                int count = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Blog blog = dataSnapshot.getValue(Blog.class);
                    if (blog.getStatus().equals("Published")) {
                        count++;
                    }
                }
                Log.d("DEBUG", "count blog: " + count);
                listener.onCountRetrieved(count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("DEBUG", "Database error: " + databaseError.getMessage());
            }
        });
    }
    public interface onCountListener {
        void onCountRetrieved(int count);
    }

    public void getFollowingCountOfUser(String userId, onCountListener listener) {
        followersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                Log.d("DEBUG", "count following: " + snapshot.getChildrenCount());
                int count = (int) snapshot.getChildrenCount();
                listener.onCountRetrieved(count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("DEBUG", "Database error: " + databaseError.getMessage());
            }
        });
    }

    public void getFollowerCountOfUser(String userId, onCountListener listener) {
        followersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
//                        Log.d("DEBUG", dataSnapshot1.getKey());
                        if (dataSnapshot1.getKey().equals(userId)) {
                            count++;
                        }
                    }
                }
                listener.onCountRetrieved(count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("DEBUG", "Database error: " + databaseError.getMessage());
            }
        });
    }

    public void getCommentCounts(Date startDate, Date endDate, final OnCommentCountsListener listener) {
        commentsRef.orderByChild("created_time")
                .startAt(startDate.getTime())
                .endAt(endDate.getTime())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Same onDataChange code as step 3
                        Map<Date, Integer> commentCounts = new HashMap<>();
                        for (DataSnapshot commentSnapshot : dataSnapshot.getChildren()) {
                            Comment comment = commentSnapshot.getValue(Comment.class);
                            Date createdTime = new Date(comment.getCreatedTime());
                            Date date = new Date(createdTime.getYear(), createdTime.getMonth(), createdTime.getDate());
                            if (commentCounts.containsKey(date)) {
                                commentCounts.put(date, commentCounts.get(date) + 1);
                            } else {
                                commentCounts.put(date, 1);
                            }
                        }

                        // Pass the comment counts data to the callback listener
                        listener.onCommentCountsRetrieved(commentCounts);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle error
                    }
                });
    }

    public interface OnCommentCountsListener {
        void onCommentCountsRetrieved(Map<Date, Integer> commentCounts);
    }

    public void ChangePassword(User user, String newPassword){
        usersRef.child(user.getUserId()).setValue(new User(user.getUserId(),
                user.getName(),user.getEmail(), newPassword, user.getBirthday(), user.getAva()))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(context,"Change password successful!", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(context,"Change password fail!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public ArrayList<Follow> getFollowList(final FollowsListCallback callback){
        followRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            for (DataSnapshot i : snapshot.getChildren()){
                                Follow follow = i.getValue(Follow.class);
                                if (follow != null){
                                    followList.add(follow);
                                }
                            }
                            callback.onFollowsListReady(followList);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("TAG", "Database error: " + error.getMessage());
                    }
                });
        return followList;
    }

    public void addFollowInstance(String follower, String followed, Long time){
        String id = followRef.push().getKey();
        followRef.child(id).setValue(new Follow(follower, followed, time))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("DEBUG","success add follow instance");
                        } else {
                            Log.d("DEBUG","success add follow instance");
                        }
                    }
                });
    }
    public interface FollowsListCallback {
        void onFollowsListReady(ArrayList<Follow> followsList);
    }

}