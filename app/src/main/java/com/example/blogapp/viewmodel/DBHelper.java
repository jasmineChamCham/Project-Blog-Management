package com.example.blogapp.viewmodel;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.example.blogapp.model.Blog;
import com.example.blogapp.model.Comment;
import com.example.blogapp.model.Follow;
import com.example.blogapp.model.User;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DBHelper {
    private FirebaseDatabase database;
    public FirebaseRecyclerOptions<Blog> optionFollowing, optionExplore, optionLikes, optionWithCategory;
    public FirebaseRecyclerOptions<Comment> optionComment;
    public FirebaseRecyclerOptions<Follow> followOptions;
    private static DatabaseReference followRef;
    private DatabaseReference blogsRef, usersRef, commentsRef;
    public static ArrayList<Follow> followList = new ArrayList<>();
    private Context context;

    public DBHelper(Context context) {
        this.context = context;
        database = FirebaseDatabase.getInstance();

        blogsRef = database.getReference().child("blogs");
        usersRef = database.getReference().child("users");
        commentsRef = database.getReference().child("comments");
        followRef = database.getReference().child("follows");

        followOptions = new FirebaseRecyclerOptions.Builder<Follow>()
                .setQuery(followRef, Follow.class)
                .build();

        optionFollowing = new FirebaseRecyclerOptions.Builder<Blog>()
                .setQuery(blogsRef, Blog.class)
                .build();

        optionExplore = new FirebaseRecyclerOptions.Builder<Blog>()
                .setQuery(blogsRef, Blog.class)
                .build();

        optionLikes = new FirebaseRecyclerOptions.Builder<Blog>()
                .setQuery(blogsRef, Blog.class)
                .build();
    }

    public void addBlog(String title, String content, String createdTime, String userID, int likeNumber, int viewNumber, String category, String status) {

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

    public FirebaseRecyclerOptions<Blog> getBlogOptionByUserId(String userId) {
        Query query = blogsRef.orderByChild("userId").equalTo(userId);
        FirebaseRecyclerOptions<Blog> optionBlogByUserId = new FirebaseRecyclerOptions.Builder<Blog>()
                .setQuery(query, Blog.class)
                .build();
        return optionBlogByUserId;
    }

    public Blog getBlogById(String id) {
        DatabaseReference getRef = database.getReference().child("blogs").child(id);
        final Blog[] data = {null};
        getRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                data[0] = dataSnapshot.getValue(Blog.class);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Toast.makeText(context,"Id does not exits",Toast.LENGTH_SHORT);
            }
        });
        return data[0];
    }

    public void updateBlog(String id, String title, String content, String createdTime, String userID, int likeNumber, int viewNumber, String category, String status) {
        blogsRef.child(id).setValue(new Blog(id, title, content, createdTime, userID, likeNumber, viewNumber, category, status))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(context,"Edit blog successful!", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(context,"Edit blog fail!", Toast.LENGTH_SHORT).show();
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

    public void addUser(String name, String email, String pass, String birthday) {
        String id = usersRef.push().getKey();
        usersRef.child(id).setValue(new User(id, name, email, pass, birthday))
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

    public FirebaseRecyclerOptions<Blog> getBlogsByCategory(String category) {
        Query query = blogsRef.orderByChild("category").equalTo(category);
        optionWithCategory = new FirebaseRecyclerOptions.Builder<Blog>()
                .setQuery(query, Blog.class)
                .build();
        return optionWithCategory;
    }

    public void addComment(String commentContent, String userId, String blogId) {
        String commentId = commentsRef.push().getKey();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();  //get current date
        String createdTime = dateFormat.format(date);
        commentsRef.child(blogId).child(commentId).setValue(new Comment(commentId, commentContent, userId, blogId, createdTime))
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

    public void getCommentsByBlogId(String blogId, onOptionListener listener) {
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

    public interface onOptionListener {
        void onOptionCommentRetrieved(FirebaseRecyclerOptions<Comment> options);
    }

    public void ChangePassword(User user, String newPassword){
        usersRef.child(user.getUserId()).setValue(new User(user.getUserId(),
                user.getName(),user.getEmail(), newPassword, user.getBirthday()))
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

    public static ArrayList<Follow> getFollowList(final FollowsListCallback callback){
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

    public static void addFollowInstance(String follower, String followed, Long time){
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