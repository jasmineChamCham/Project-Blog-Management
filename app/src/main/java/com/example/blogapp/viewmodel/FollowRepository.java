package com.example.blogapp.viewmodel;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.blogapp.model.Follow;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class FollowRepository {
    static FirebaseDatabase database;
    public FirebaseRecyclerOptions<Follow> followOptions;
    private static DatabaseReference followRef;
    public static FirebaseAuth mAuth;
    public static ArrayList<Follow> followList = new ArrayList<>();

    public FollowRepository() {
        database = FirebaseDatabase.getInstance();
        followRef = database.getReference().child("follows");
        followOptions = new FirebaseRecyclerOptions.Builder<Follow>()
                .setQuery(followRef, Follow.class)
                .build();

        mAuth = FirebaseAuth.getInstance();

    }

    public static ArrayList<Follow> getFollowList(final FollowsListCallback callback){
        FirebaseDatabase.getInstance()
                .getReference().child("follows").addValueEventListener(new ValueEventListener() {
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
        database = FirebaseDatabase.getInstance();
        followRef = database.getReference().child("follows");
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
