package com.example.blogapp.viewmodel;


import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
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
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LikesAdapter extends RecyclerView.Adapter<LikesAdapter.LikedBlogHolder> implements Filterable {
    private DBHelper dbHelper;
    private List<Blog> likedBlogs;
    private List<Blog> copyOfLikedBlogs;
    private User userLogin;

    public LikesAdapter(List<Blog> likedBlogs, User userLogin) {
        this.likedBlogs = likedBlogs;
        this.copyOfLikedBlogs = likedBlogs;
        this.userLogin = userLogin;
    }

    @Override
    public LikesAdapter.LikedBlogHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
        Date date = new Date(blog.getCreatedTime());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String dateString = dateFormat.format(date);
        holder.binding.setCreatedTime(dateString);
        holder.binding.setBlog(blog);
        holder.binding.setContent(Html.fromHtml(blog.getContent()));
        dbHelper.getUserById(blog.getUserId(), user -> {
            holder.binding.setUser(user);
            String imgString = user.getAva();
            if (imgString != null && !imgString.equals("")) {
                File imgFile = new File(imgString);
                Log.d("DEBUG", "Start file existed: " + imgFile.exists());
                Log.d("DEBUG", "Start image file: " + imgFile.getAbsoluteFile());

                Picasso.get().load(imgFile.getAbsoluteFile()).into(holder.binding.ivAvatar);
            }
            else {
                holder.binding.ivAvatar.setImageResource(R.drawable.person_avatar);
            }
        });
        dbHelper.isFollowing(userLogin.getUserId(), blog.getUserId(), isFollowing ->  {
            if (isFollowing) {
                holder.binding.btnFollow.setText("Following");
            }
            else {
                holder.binding.btnFollow.setText("Follow");
            }
        });
        dbHelper.isLiked(userLogin.getUserId(), blog.getBlogId(), isLiked -> {
            if (isLiked) {
                holder.binding.btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_thumb_up_alt_24, 0, 0, 0);
            }
            else {
                holder.binding.btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_thumb_up_off_alt_24, 0, 0, 0);
            }
        });
        holder.binding.tvUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("userLogin", userLogin);
                bundle.putSerializable("authorId", blog.getUserId());
                Navigation.findNavController(view).navigate(R.id.authorProfileFragment, bundle);
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
        holder.binding.btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.isFollowing(userLogin.getUserId(), blog.getUserId(), isFollowing ->  {
                    if (isFollowing) {      // if follow -> unfollow
                        holder.binding.btnFollow.setText("Follow");
                        dbHelper.deleteFollowRecord(userLogin.getUserId(), blog.getUserId());
                    }
                    else {      // if do not follow -> follow
                        holder.binding.btnFollow.setText("Following");
                        dbHelper.addFollowRecord(userLogin.getUserId(), blog.getUserId());
                    }
                });
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
                            if (likedBlogList != null) {
                                likedBlogs.clear();
                                likedBlogs.addAll(likedBlogList);
                                notifyDataSetChanged();
                            }
                            else {
                                likedBlogs.clear();
                                notifyDataSetChanged();
                            }
                        });
                    }
                    else {      // if not like -> like
                        holder.binding.btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_thumb_up_alt_24, 0, 0, 0);
                        dbHelper.addLikedBlog(userLogin.getUserId(), blog.getBlogId());
                        dbHelper.getLikedBlogs(userLogin.getUserId(), likedBlogList -> {
                            if (likedBlogList != null) {
                                likedBlogs.clear();
                                likedBlogs.addAll(likedBlogList);
                                notifyDataSetChanged();
                            }
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

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String searchString = constraint.toString().toLowerCase();
                List<Blog> filteredList = new ArrayList<>();
                if (searchString.isEmpty()) {
                    filteredList.addAll(copyOfLikedBlogs);
                }
                else {
                    for (Blog blog : copyOfLikedBlogs) {
                        if (blog.getTitle().toLowerCase().contains(searchString)
                                || blog.getContent().toLowerCase().contains(searchString))
                        {
                            filteredList.add(blog);
                        }
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                likedBlogs = (List<Blog>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public static class LikedBlogHolder extends RecyclerView.ViewHolder {
        public ViewBlogItemBinding binding;
        public LikedBlogHolder(ViewBlogItemBinding itemsBinding) {
            super(itemsBinding.getRoot());
            this.binding = itemsBinding;
        }
    }
}
