package com.example.blogapp.viewmodel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blogapp.R;
import com.example.blogapp.databinding.ViewBlogItemBinding;
import com.example.blogapp.model.Blog;
import com.example.blogapp.model.User;
import com.example.blogapp.view.ListBlogActivity;

import java.util.ArrayList;

public class BlogAdapter extends RecyclerView.Adapter<BlogAdapter.ViewHolder> {

    private ArrayList<Blog> blogList;
    private ArrayList<User> userList;

    public BlogAdapter(ArrayList<Blog> blogList, ArrayList<User> userList) {
        this.blogList = blogList;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewBlogItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.view_blog_item, parent,false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.binding.setBlog(blogList.get(position));
        holder.binding.setUser(userList.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.detailBlogFragment);
            }
        });
        holder.binding.btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.commentFragment);
            }
        });
    }

    @Override
    public int getItemCount() {
        return blogList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewBlogItemBinding binding;

        public ViewHolder(ViewBlogItemBinding itemBinding) {
            super(itemBinding.getRoot());
            this.binding = itemBinding;
        }
    }
}


