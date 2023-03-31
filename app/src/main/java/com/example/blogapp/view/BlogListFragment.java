package com.example.blogapp.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.blogapp.R;
import com.example.blogapp.databinding.EditBlogItemBinding;
import com.example.blogapp.databinding.FragmentBlogListBinding;
import com.example.blogapp.model.Blog;
import com.example.blogapp.viewmodel.DBHelper;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.text.SimpleDateFormat;

public class BlogListFragment extends Fragment {
    FragmentBlogListBinding binding;
    DBHelper repository;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBlogListBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        repository = new DBHelper(view.getContext());

        binding.rvBlogs.setLayoutManager(new LinearLayoutManager(view.getContext()));
        binding.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                Navigation.findNavController(view).navigate(R.id.editBlogFragment,bundle);
            }
        });
        binding.btnPublished.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reload(repository.optionPublished);
                binding.btnPublished.setBackgroundColor(getResources().getColor(R.color.main_color));
                binding.btnTrashed.setBackgroundColor(getResources().getColor(R.color.white));
                binding.btnDrafts.setBackgroundColor(getResources().getColor(R.color.white));
                binding.btnPublished.setTextColor(getResources().getColor(R.color.white));
                binding.btnTrashed.setTextColor(getResources().getColor(R.color.main_color));
                binding.btnDrafts.setTextColor(getResources().getColor(R.color.main_color));
            }
        });
        binding.btnDrafts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reload(repository.optionDrafts);
                binding.btnPublished.setBackgroundColor(getResources().getColor(R.color.white));
                binding.btnTrashed.setBackgroundColor(getResources().getColor(R.color.white));
                binding.btnDrafts.setBackgroundColor(getResources().getColor(R.color.main_color));
                binding.btnPublished.setTextColor(getResources().getColor(R.color.main_color));
                binding.btnTrashed.setTextColor(getResources().getColor(R.color.main_color));
                binding.btnDrafts.setTextColor(getResources().getColor(R.color.white));
            }
        });
        binding.btnTrashed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reload(repository.optionTrashed);
                binding.btnPublished.setBackgroundColor(getResources().getColor(R.color.white));
                binding.btnTrashed.setBackgroundColor(getResources().getColor(R.color.main_color));
                binding.btnDrafts.setBackgroundColor(getResources().getColor(R.color.white));
                binding.btnPublished.setTextColor(getResources().getColor(R.color.main_color));
                binding.btnTrashed.setTextColor(getResources().getColor(R.color.white));
                binding.btnDrafts.setTextColor(getResources().getColor(R.color.main_color));
            }
        });
        reload(repository.optionPublished);
        return view;
    }

    public void reload(FirebaseRecyclerOptions<Blog> options){
        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Blog, BlogHolder>(options) {
            @NonNull
            @Override
            public BlogHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                EditBlogItemBinding binding =
                        DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                                R.layout.edit_blog_item,
                                parent,
                                false);
                return new BlogHolder(binding);
            }

            @Override
            protected void onBindViewHolder(@NonNull BlogHolder holder, int position, @NonNull Blog model) {
                holder.binding.tvTitle.setText(model.getTitle());
                holder.binding.tvContent.setText(model.getContent());
                holder.binding.tvTime.setText(model.getCreatedTime());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("blog", model);
                        Navigation.findNavController(binding.getRoot()).navigate(R.id.editBlogFragment,bundle);
                    }
                });
            }
        };
        binding.rvBlogs.setAdapter(adapter);
        adapter.startListening();
    }

    public class BlogHolder extends RecyclerView.ViewHolder {
        public EditBlogItemBinding binding;

        BlogHolder(EditBlogItemBinding itemsBinding) {
            super(itemsBinding.getRoot());
            this.binding = itemsBinding;
        }
    }
}