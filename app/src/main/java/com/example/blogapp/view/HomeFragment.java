package com.example.blogapp.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.blogapp.R;
import com.example.blogapp.databinding.FragmentHomeBinding;
import com.example.blogapp.databinding.ViewBlogItemBinding;
import com.example.blogapp.model.Blog;
import com.example.blogapp.model.User;
import com.example.blogapp.viewmodel.DBHelper;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private DBHelper dbHelper;
    private User userLogin;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userLogin = (User) getArguments().getSerializable("userLogin");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.fragment_home, container, false);
        View viewRoot = binding.getRoot();

        dbHelper = new DBHelper(viewRoot.getContext());
//        dbHelper.addUser("Hoang Nam", "hoangnam@gmail.com", "123456", "19/9/1997");
//        dbHelper.addComment("Thanks for the great content.", "-NRvCltmrz4IV7fxwLv5", "-NRXKh1SLM6_dKT3bwfY");
//        dbHelper.addLikedBlog("-NRvCltohTQIOXQ7kY_O", "-NRXKh1SLM6_dKT3bwfY");

        binding.setUser(userLogin);
        reloadRecyclerView(dbHelper.optionFollowing);
        return viewRoot;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.rvBlogs.setLayoutManager(new LinearLayoutManager(getContext()));

        binding.btnFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadRecyclerView(dbHelper.optionFollowing);
                binding.btnFollowing.setBackgroundColor(getResources().getColor(R.color.main_color));
                binding.btnExplore.setBackgroundColor(getResources().getColor(R.color.white));
                binding.btnLikes.setBackgroundColor(getResources().getColor(R.color.white));
                binding.btnFollowing.setTextColor(getResources().getColor(R.color.white));
                binding.btnExplore.setTextColor(getResources().getColor(R.color.main_color));
                binding.btnLikes.setTextColor(getResources().getColor(R.color.main_color));
                binding.categoryOption.setVisibility(View.GONE);
            }
        });

        binding.btnExplore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadRecyclerView(dbHelper.optionExplore);
                binding.btnFollowing.setBackgroundColor(getResources().getColor(R.color.white));
                binding.btnExplore.setBackgroundColor(getResources().getColor(R.color.main_color));
                binding.btnLikes.setBackgroundColor(getResources().getColor(R.color.white));
                binding.btnFollowing.setTextColor(getResources().getColor(R.color.main_color));
                binding.btnExplore.setTextColor(getResources().getColor(R.color.white));
                binding.btnLikes.setTextColor(getResources().getColor(R.color.main_color));
                binding.categoryOption.setVisibility(View.GONE);
            }
        });

        binding.btnLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadRecyclerView(dbHelper.optionAll);
                binding.btnFollowing.setBackgroundColor(getResources().getColor(R.color.white));
                binding.btnExplore.setBackgroundColor(getResources().getColor(R.color.white));
                binding.btnLikes.setBackgroundColor(getResources().getColor(R.color.main_color));
                binding.btnFollowing.setTextColor(getResources().getColor(R.color.main_color));
                binding.btnExplore.setTextColor(getResources().getColor(R.color.main_color));
                binding.btnLikes.setTextColor(getResources().getColor(R.color.white));
                binding.categoryOption.setVisibility(View.GONE);
            }
        });

        binding.btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder mDialog = new AlertDialog.Builder(v.getContext());
                LayoutInflater inflater = LayoutInflater.from(v.getContext());
                View mView = inflater.inflate(R.layout.filter_dialog, null);
                mDialog.setView(mView);

                AlertDialog dialog = mDialog.create();
                dialog.setCancelable(true);

                Button btnDone = mView.findViewById(R.id.btn_done);
                btnDone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ChipGroup cgCategory = mView.findViewById(R.id.cg_category);
                        int checkedId = cgCategory.getCheckedChipId();
                        if (checkedId != -1) {
                            Chip checkedChip = cgCategory.findViewById(checkedId);
                            String checkedCategory = checkedChip.getText().toString();
                            handleFilter(checkedCategory);
                        }
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        binding.btnPersonal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("userLogin", userLogin);
                Navigation.findNavController(v).navigate(R.id.personalFragment, bundle);
            }
        });
    }

    public void handleFilter(String category) {
        binding.categoryOption.setVisibility(View.VISIBLE);
        binding.categoryOption.setText(category);
        reloadRecyclerView(dbHelper.getBlogsByCategory(category));
        // date picker
    }

    public void reloadRecyclerView(FirebaseRecyclerOptions<Blog> options) {
        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Blog, BlogHolder>(options) {
            @NonNull
            @Override
            public BlogHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                ViewBlogItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                                                R.layout.view_blog_item,
                                                parent,
                                                false);
                return new BlogHolder(binding);
            }

            @Override
            protected void onBindViewHolder(@NonNull BlogHolder holder, int position, @NonNull Blog blog) {
                holder.binding.setBlog(blog);
                holder.binding.setContent(Html.fromHtml(blog.getContent()));
                dbHelper.getUserById(blog.getUserId(), user -> {
                    holder.binding.setUser(user);
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
            }
        };
        binding.rvBlogs.setAdapter(adapter);
        adapter.startListening();
    }

    public class BlogHolder extends RecyclerView.ViewHolder {
        public ViewBlogItemBinding binding;

        BlogHolder(ViewBlogItemBinding itemsBinding) {
            super(itemsBinding.getRoot());
            this.binding = itemsBinding;
        }
    }
}