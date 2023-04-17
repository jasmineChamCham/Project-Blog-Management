package com.example.blogapp.viewmodel;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.blogapp.model.Blog;
import com.example.blogapp.model.Comment;
import com.example.blogapp.model.User;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class DBHelper {
    private FirebaseDatabase database;
    public FirebaseRecyclerOptions<Blog> optionAll, optionPublished, optionDrafts, optionTrash,
                                        optionFollowing, optionExplore, optionLikes, optionWithCategory;
    public FirebaseRecyclerOptions<Comment> optionComment;
    private DatabaseReference blogsRef, usersRef, commentsRef;
    private Context context;

    public DBHelper(Context context) {
        this.context = context;
        database = FirebaseDatabase.getInstance();
        blogsRef = database.getReference().child("blogs");
        usersRef = database.getReference().child("users");
        commentsRef = database.getReference().child("comments");
        optionAll = new FirebaseRecyclerOptions.Builder<Blog>()
                .setQuery(blogsRef, Blog.class)
                .build();

        optionPublished = new FirebaseRecyclerOptions.Builder<Blog>()
                .setQuery(blogsRef.orderByChild("status").equalTo("Published"), Blog.class)
                .build();

        optionDrafts = new FirebaseRecyclerOptions.Builder<Blog>()
                .setQuery(blogsRef.orderByChild("status").equalTo("Drafts"), Blog.class)
                .build();

        optionTrash = new FirebaseRecyclerOptions.Builder<Blog>()
                .setQuery(blogsRef.orderByChild("status").equalTo("Trash"), Blog.class)
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

    public void updateNote(String id, String title, String content, String createTime, String userID, int likeNumber, int viewNumber, String category, String status) {
        blogsRef.child(id).setValue(new Blog(id, title, content, createTime, userID, likeNumber, viewNumber, category, status))
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

    public void authentication(String email, String password, onUserListener listener) {
        Query emailQuery = usersRef.orderByChild("email").equalTo(email);
        emailQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user.getPassword().equals(password)) {
                        // user exists with email and password
                        Log.d("DEBUG", "Exist user to login");
                        listener.onUserRetrieved(user);
                        break;
                    }
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
                        } else {

                            Log.d("DEBUG","Add comment fail!");}
                    }
                });
    }

    public FirebaseRecyclerOptions<Comment> getCommentsByBlogId(String blogId) {
            Query query = commentsRef.child(blogId).orderByChild("createdTime").limitToLast(10);
            optionComment = new FirebaseRecyclerOptions.Builder<Comment>()
                    .setQuery(query, Comment.class)
                    .build();
            return optionComment;
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

}