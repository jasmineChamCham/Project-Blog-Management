package com.example.blogapp.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.blogapp.R;
import com.example.blogapp.databinding.EditBlogItemBinding;
import com.example.blogapp.databinding.FragmentAuthorProfileBinding;
import com.example.blogapp.databinding.ViewBlogItemBinding;
import com.example.blogapp.model.Blog;
import com.example.blogapp.model.LikedBlog;
import com.example.blogapp.model.User;
import com.example.blogapp.viewmodel.AuthorBlogAdapter;
import com.example.blogapp.viewmodel.DBHelper;
import com.example.blogapp.viewmodel.FollowingAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AuthorProfileFragment extends Fragment {
    FragmentAuthorProfileBinding binding;
    private DBHelper dbHelper;
    private User userLogin;
    private String authorId;
    private ArrayList<Blog> authorBlogs;
    private AuthorBlogAdapter authorBlogAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userLogin = (User) getArguments().getSerializable("userLogin");
            authorId = (String) getArguments().getSerializable("authorId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.fragment_author_profile, container, false);
        View viewRoot = binding.getRoot();

        dbHelper = new DBHelper(viewRoot.getContext());

        dbHelper.getUserById(authorId, user -> {
            binding.setUser(user);
        });
        dbHelper.isFollowing(userLogin.getUserId(), authorId, isFollowing ->  {
            if (isFollowing) {
                binding.btnFollow.setText("Following");
            }
            else {
                binding.btnFollow.setText("Follow");
            }
        });
        dbHelper.getBlogCountOfUser(authorId, count -> {
            binding.tvBlogCount.setText(String.valueOf(count));
        });
        dbHelper.getFollowerCountOfUser(authorId, count -> {
            binding.tvFollowerCount.setText(String.valueOf(count));
        });
        dbHelper.getFollowingCountOfUser(authorId, count -> {
            binding.tvFollowingCount.setText(String.valueOf(count));
        });

        binding.btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbHelper.isFollowing(userLogin.getUserId(), authorId, isFollowing ->  {
                    if (isFollowing) {      // if follow -> unfollow
                        binding.btnFollow.setText("Follow");
                        int curCount = Integer.parseInt((String) binding.tvFollowerCount.getText());
                        binding.tvFollowerCount.setText(String.valueOf(curCount - 1));
                        dbHelper.deleteFollowRecord(userLogin.getUserId(), authorId);
                    }
                    else {      // if do not follow -> follow
                        binding.btnFollow.setText("Following");
                        int curCount = Integer.parseInt((String) binding.tvFollowerCount.getText());
                        binding.tvFollowerCount.setText(String.valueOf(curCount + 1));
                        dbHelper.addFollowRecord(userLogin.getUserId(), authorId);
                    }
                });
            }
        });

        binding.rvBlogs.setLayoutManager(new LinearLayoutManager(getContext()));

        reloadRV();

        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.search_menu, menu);
                if (getActivity() != null) {
                    ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
                }
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == android.R.id.home){
                    getActivity().onBackPressed();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);;
        return viewRoot;
    }

    public void reloadRV() {
        authorBlogs = new ArrayList<>();
        dbHelper.getBlogsByUserId(authorId, blogList -> {
            if (blogList != null) {
                authorBlogs.addAll(blogList);
                for(int i = 0; i < authorBlogs.size(); i++) {
                    Log.d("DEBUG", "main_author's blog id: " + authorBlogs.get(i).getBlogId());
                }
                binding.layoutNoBlog.setVisibility(View.GONE);
                binding.rvBlogs.setVisibility(View.VISIBLE);
                authorBlogAdapter = new AuthorBlogAdapter(authorBlogs, userLogin);
                binding.rvBlogs.setAdapter(authorBlogAdapter);
            }
            else {
                Log.d("DEBUG", "null author blog");
                binding.layoutNoBlog.setVisibility(View.VISIBLE);
                binding.rvBlogs.setVisibility(View.GONE);
            }
        });
    }
}