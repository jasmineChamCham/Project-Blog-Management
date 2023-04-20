package com.example.blogapp.viewmodel;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Html;
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
import com.example.blogapp.model.LikedBlog;
import com.example.blogapp.model.User;
import com.example.blogapp.view.HomeFragment;

import java.util.List;

public class LikedBlogsAdapter extends RecyclerView.Adapter<LikedBlogsAdapter.LikedBlogHolder> {
    private DBHelper dbHelper;
    private List<Blog> likedBlogs;
    private User userLogin;

    public LikedBlogsAdapter(List<Blog> likedBlogs, User userLogin) {
        this.userLogin = userLogin;
        this.likedBlogs = likedBlogs;
    }

    @Override
    public LikedBlogsAdapter.LikedBlogHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewBlogItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                                        R.layout.view_blog_item,
                                        parent,
                                        false);
        dbHelper = new DBHelper(parent.getContext());
        return new LikedBlogHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LikedBlogHolder holder, @SuppressLint("RecyclerView") int position) {
        Blog blog = likedBlogs.get(position);
        holder.binding.setBlog(blog);
        holder.binding.setContent(Html.fromHtml(blog.getContent()));
        dbHelper.getUserById(blog.getUserId(), user -> {
            holder.binding.setUser(user);
        });
        dbHelper.isLiked(userLogin.getUserId(), blog.getBlogId(), isLiked -> {
            if (isLiked) {
                holder.binding.btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_thumb_up_alt_24, 0, 0, 0);
            }
            else {
                holder.binding.btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_thumb_up_off_alt_24, 0, 0, 0);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("userLogin", userLogin);
                bundle.putSerializable("blogItem", blog);
                Navigation.findNavController(view).navigate(R.id.detailBlogFragment, bundle);
            }
        });
        holder.binding.btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("userLogin", userLogin);
                bundle.putSerializable("blogItem", blog);
                Navigation.findNavController(view).navigate(R.id.commentFragment, bundle);
            }
        });
        holder.binding.btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.isLiked(userLogin.getUserId(), blog.getBlogId(), isLiked -> {
                    if (isLiked) {      // if like -> unlike
                        holder.binding.btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_thumb_up_off_alt_24, 0, 0, 0);
                        dbHelper.deleteLikedBlog(new LikedBlog(userLogin.getUserId(), blog.getBlogId()));
                        dbHelper.getLikedBlogs(userLogin.getUserId(), likedBlogList -> {
                            likedBlogs.clear();
                            likedBlogs.addAll(likedBlogList);
                            notifyDataSetChanged();
                        });
                    }
                    else {      // if not like -> like
                        holder.binding.btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_thumb_up_alt_24, 0, 0, 0);
                        dbHelper.addLikedBlog(userLogin.getUserId(), blog.getBlogId());
                        dbHelper.getLikedBlogs(userLogin.getUserId(), likedBlogList -> {
                            likedBlogs.clear();
                            likedBlogs.addAll(likedBlogList);
                            notifyDataSetChanged();
                        });
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return likedBlogs.size();
    }

    public static class LikedBlogHolder extends RecyclerView.ViewHolder {
        public ViewBlogItemBinding binding;
        public LikedBlogHolder(ViewBlogItemBinding itemsBinding) {
            super(itemsBinding.getRoot());
            this.binding = itemsBinding;
        }
    }
}
