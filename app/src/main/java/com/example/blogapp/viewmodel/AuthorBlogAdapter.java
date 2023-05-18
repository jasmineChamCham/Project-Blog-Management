package com.example.blogapp.viewmodel;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blogapp.R;
import com.example.blogapp.databinding.ViewBlogItemBinding;
import com.example.blogapp.model.Blog;
import com.example.blogapp.model.LikedBlog;
import com.example.blogapp.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AuthorBlogAdapter extends RecyclerView.Adapter<AuthorBlogAdapter.AuthorBlogHolder> {
    private DBHelper dbHelper;
    private List<Blog> authorBlogs;
    private User userLogin;


    public AuthorBlogAdapter(List<Blog> authorBlogs, User userLogin) {
        this.authorBlogs = authorBlogs;
        this.userLogin = userLogin;
    }

    @Override
    public AuthorBlogAdapter.AuthorBlogHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewBlogItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.view_blog_item,
                parent,
                false);
        dbHelper = new DBHelper(parent.getContext());
        return new AuthorBlogHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AuthorBlogHolder holder, @SuppressLint("RecyclerView") int position) {
        Blog blog = authorBlogs.get(position);
        Date date = new Date(blog.getCreatedTime());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String dateString = dateFormat.format(date);
        holder.binding.setCreatedTime(dateString);
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
        holder.binding.btnFollow.setVisibility(View.GONE);
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
                        dbHelper.deleteLikedBlog(new LikedBlog(userLogin.getUserId(), blog.getBlogId()));
                        holder.binding.btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_thumb_up_off_alt_24, 0, 0, 0);
                    }
                    else {      // if not like -> like
                        dbHelper.addLikedBlog(userLogin.getUserId(), blog.getBlogId());
                        holder.binding.btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_thumb_up_alt_24, 0, 0, 0);
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return authorBlogs.size();
    }

    public static class AuthorBlogHolder extends RecyclerView.ViewHolder {
        public ViewBlogItemBinding binding;
        public AuthorBlogHolder(ViewBlogItemBinding itemsBinding) {
            super(itemsBinding.getRoot());
            this.binding = itemsBinding;
        }
    }
}
