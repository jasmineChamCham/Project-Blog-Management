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

public class ExploreAdapter extends RecyclerView.Adapter<ExploreAdapter.ExploreHolder> implements Filterable {
    private DBHelper dbHelper;
    private List<Blog> exploreBlogs;
    private List<Blog> copyOfExploreBlogs;
    private User userLogin;

    public ExploreAdapter(List<Blog> exploreBlogs, User userLogin) {
        this.exploreBlogs = exploreBlogs;
        this.copyOfExploreBlogs = exploreBlogs;
        this.userLogin = userLogin;
    }

    @Override
    public ExploreAdapter.ExploreHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewBlogItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.view_blog_item,
                parent,
                false);
        dbHelper = new DBHelper(parent.getContext());
        return new ExploreHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ExploreHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.binding.btnFollow.setText("Follow");
        Blog blog = exploreBlogs.get(position);
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
                dbHelper.addFollowRecord(userLogin.getUserId(), blog.getUserId());
                dbHelper.getExploreBlogList(userLogin.getUserId(), blogList -> {
                    if (blogList != null) {
                        exploreBlogs.clear();
                        exploreBlogs.addAll(blogList);
                        notifyDataSetChanged();
                    }
                    else {
                        exploreBlogs.clear();
                        notifyDataSetChanged();
                    }
                });
            }});
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
                    }
                    else {      // if not like -> like
                        holder.binding.btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_thumb_up_alt_24, 0, 0, 0);
                        dbHelper.addLikedBlog(userLogin.getUserId(), blog.getBlogId());
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return exploreBlogs.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String searchString = constraint.toString().toLowerCase();
                List<Blog> filteredList = new ArrayList<>();
                if (searchString.isEmpty()) {
                    filteredList.addAll(copyOfExploreBlogs);
                }
                else {
                    for (Blog blog : copyOfExploreBlogs) {
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
                exploreBlogs = (List<Blog>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public static class ExploreHolder extends RecyclerView.ViewHolder {
        public ViewBlogItemBinding binding;
        public ExploreHolder(ViewBlogItemBinding itemsBinding) {
            super(itemsBinding.getRoot());
            this.binding = itemsBinding;
        }
    }
}
