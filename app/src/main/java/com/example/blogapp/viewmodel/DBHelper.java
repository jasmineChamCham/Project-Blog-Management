package com.example.blogapp.viewmodel;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;

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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBHelper {
    private FirebaseDatabase database;
    public FirebaseRecyclerOptions<LikedBlog> optionLikes;
    public FirebaseRecyclerOptions<Blog> optionFollowing, optionExplore, optionWithCategory;
    public FirebaseRecyclerOptions<Comment> optionComment;
    public FirebaseRecyclerOptions<Follow> followOptions;
    private DatabaseReference blogsRef, usersRef, commentsRef, followRef, likedBlogsRef;
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

        followOptions = new FirebaseRecyclerOptions.Builder<Follow>()
                .setQuery(followRef, Follow.class)
                .build();

        optionFollowing = new FirebaseRecyclerOptions.Builder<Blog>()
                .setQuery(blogsRef, Blog.class)
                .build();

        optionExplore = new FirebaseRecyclerOptions.Builder<Blog>()
                .setQuery(blogsRef, Blog.class)
                .build();

        optionLikes = new FirebaseRecyclerOptions.Builder<LikedBlog>()
                .setQuery(likedBlogsRef, LikedBlog.class)
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
    public FirebaseRecyclerOptions<Blog> getBlogByLiked(){
        Query query = blogsRef.orderByChild("liked/" + "456").equalTo(true);
        FirebaseRecyclerOptions<Blog> optionBlogByUserId = new FirebaseRecyclerOptions.Builder<Blog>()
                .setQuery(query, Blog.class)
                .build();
        return optionBlogByUserId;
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
                listener.onLikedBlogRetrieved(likedBlogIds);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("DEBUG", "Database error: " + databaseError.getMessage());
            }
        });
    }
    public interface onLikedBlogIdsListener {
        void onLikedBlogRetrieved(List<String> likedBlogIds);
    }

    public void getLikedBlogs(String userId, onLikedBlogsListener listener) {
        List<Blog> likedBlogList = new ArrayList<>();
        getLikedBlogIdsByUserId(userId, likedBlogIds -> {
            for (int i = 0; i < likedBlogIds.size(); i++) {
                blogsRef.child(likedBlogIds.get(i)).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Blog blog = dataSnapshot.getValue(Blog.class);
                        likedBlogList.add(blog);
                        if (likedBlogList.size() == likedBlogIds.size()) {
                            listener.onLikedBlogsRetrieved(likedBlogList);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("DEBUG", "Database error: " + databaseError.getMessage());
                    }
                });
            }
        });
    }
    public interface onLikedBlogsListener {
        void onLikedBlogsRetrieved(List<Blog> likedBlogList);
    }

    public void getCommentCounts(Date startDate, Date endDate, final OnCommentCountsRetrievedListener listener) {
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

    public interface OnCommentCountsRetrievedListener {
        void onCommentCountsRetrieved(Map<Date, Integer> commentCounts);
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