package com.example.blogapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.blogapp.databinding.ActivityListBlogBinding;
import com.example.blogapp.databinding.EditBlogItemBinding;
import com.example.blogapp.model.Blog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ListBlogActivity extends AppCompatActivity {
    ActivityListBlogBinding binding;
    BlogRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_list_blog);
        View view = binding.getRoot();
        setContentView(view);
        repository = new BlogRepository();

        binding.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                repository.addNote("Some Title", "Some Content", isoFormat.format(new Date()), "1234",0,0,"Novel","Publishes");
            }
        });

        binding.rvBlogs.setLayoutManager(new LinearLayoutManager(this));

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Blog, BlogHolder>(repository.options) {
            @NonNull
            @Override
            public BlogHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                EditBlogItemBinding binding =
                        DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                                R.layout.edit_blog_item,
                                parent,
                                false);
                Log.d("DEBUG","onCreateViewHolder ok");
                return new BlogHolder(binding);
            }

            @Override
            protected void onBindViewHolder(@NonNull BlogHolder holder, int position, @NonNull Blog model) {
                holder.binding.tvTitle.setText(model.getTitle());
                holder.binding.tvContent.setText(model.getContent());
                holder.binding.tvTime.setText(model.getCreatedTime());
            }
        };
        binding.rvBlogs.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    public class BlogHolder extends RecyclerView.ViewHolder {
        public EditBlogItemBinding binding;

        BlogHolder(EditBlogItemBinding itemsBinding) {
            super(itemsBinding.getRoot());
            this.binding = itemsBinding;
        }
    }
}