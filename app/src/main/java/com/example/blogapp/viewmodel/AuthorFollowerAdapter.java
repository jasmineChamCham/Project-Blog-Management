package com.example.blogapp.viewmodel;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blogapp.R;
import com.example.blogapp.databinding.UserItemBinding;
import com.example.blogapp.model.User;

import java.util.List;

public class AuthorFollowerAdapter extends RecyclerView.Adapter<AuthorFollowerAdapter.UserHolder> {
    private DBHelper dbHelper;
    private List<User> authorFollowers;
    private String authorId;
    private User userLogin;

    public AuthorFollowerAdapter(List<User> authorFollowers, String authorId, User userLogin) {
        this.authorFollowers = authorFollowers;
        this.authorId = authorId;
        this.userLogin = userLogin;
    }

    @Override
    public AuthorFollowerAdapter.UserHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        UserItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.user_item, parent, false);

        dbHelper = new DBHelper(parent.getContext());
        return new UserHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserHolder holder, @SuppressLint("RecyclerView") int position) {

        User user = authorFollowers.get(position);

        if (user.getUserId().equals(userLogin.getUserId())) {
            holder.binding.setUserName("You");
            holder.binding.btnFollow.setVisibility(View.GONE);
        }
        else {
            holder.binding.setUserName(user.getName());
            dbHelper.isFollowing(userLogin.getUserId(), user.getUserId(), isFollowing ->  {
                if (isFollowing) {
                    holder.binding.btnFollow.setText("Following");
                }
                else {
                    holder.binding.btnFollow.setText("Follow");
                }
            });
            holder.binding.tvUsername.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("userLogin", userLogin);
                    bundle.putSerializable("authorId", user.getUserId());
                    Navigation.findNavController(v).navigate(R.id.authorProfileFragment, bundle);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return authorFollowers.size();
    }

    public static class UserHolder extends RecyclerView.ViewHolder {
        public UserItemBinding binding;
        public UserHolder(UserItemBinding itemsBinding) {
            super(itemsBinding.getRoot());
            this.binding = itemsBinding;
        }
    }
}
