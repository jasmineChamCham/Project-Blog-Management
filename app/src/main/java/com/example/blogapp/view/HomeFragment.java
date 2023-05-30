package com.example.blogapp.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;

import com.example.blogapp.R;
import com.example.blogapp.databinding.FragmentHomeBinding;
import com.example.blogapp.model.Blog;
import com.example.blogapp.model.User;
import com.example.blogapp.viewmodel.DBHelper;
import com.example.blogapp.viewmodel.FollowingAdapter;
import com.example.blogapp.viewmodel.LikesAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private DBHelper dbHelper;
    private User userLogin;
    private ArrayList<Blog> followingBlogs;
    private FollowingAdapter followingAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userLogin = (User) getArguments().getSerializable("userLogin");
        }
        setHasOptionsMenu(true);
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
        String imgString = userLogin.getAva();
        if (imgString != null && !imgString.equals("")) {
            File imgFile = new File(imgString);
            Log.d("DEBUG", "Start file existed: " + imgFile.exists());
            Log.d("DEBUG", "Start image file: " + imgFile.getAbsoluteFile());

            Picasso.get().load(imgFile.getAbsoluteFile()).into(binding.ivAvatar);
        }
        else {
            Drawable drawable = getResources().getDrawable(R.drawable.person_avatar);
            binding.ivAvatar.setImageDrawable(drawable);
        }

        reloadFollowingRV();
        return viewRoot;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.rvBlogs.setLayoutManager(new LinearLayoutManager(getContext()));

        binding.btnFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadFollowingRV();
                binding.categoryOption.setVisibility(View.GONE);
                binding.layoutNoBlog.setVisibility(View.GONE);
            }
        });

        binding.btnExplore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("userLogin", userLogin);
                Navigation.findNavController(view).navigate(R.id.exploreFragment, bundle);
            }
        });

        binding.btnLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("userLogin", userLogin);
                Navigation.findNavController(view).navigate(R.id.likesFragment, bundle);
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


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_menu, menu);

        // Get the search view and set up the query listener
        MenuItem itemSearch = menu.findItem(R.id.item_search);
        SearchView searchView = (SearchView) itemSearch.getActionView();
        searchView.setQueryHint("Search here...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (followingAdapter != null) {
                    followingAdapter.getFilter().filter(newText);
                }
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public void handleFilter(String category) {
        binding.categoryOption.setVisibility(View.VISIBLE);
        binding.categoryOption.setText(category);
        filterByCategory(category);
    }

    public void reloadFollowingRV() {
        followingBlogs = new ArrayList<>();
        dbHelper.getFollowingBlogList(userLogin.getUserId(), blogList -> {
            if (blogList != null) {
                followingBlogs.clear();
                followingBlogs.addAll(blogList);
                for(int i = 0; i < followingBlogs.size(); i++) {
                    Log.d("DEBUG", "following blog id: " + followingBlogs.get(i).getBlogId());
                }
                binding.layoutNoBlog.setVisibility(View.GONE);
                binding.rvBlogs.setVisibility(View.VISIBLE);
                followingAdapter = new FollowingAdapter(followingBlogs, userLogin);
                binding.rvBlogs.setAdapter(followingAdapter);
            }
            else {
                Log.d("DEBUG", "null following blog");
                binding.layoutNoBlog.setVisibility(View.VISIBLE);
                binding.rvBlogs.setVisibility(View.GONE);
            }
        });
    }

    public void filterByCategory(String category) {
        followingBlogs = new ArrayList<>();
        dbHelper.getFollowingBlogList(userLogin.getUserId(), blogList -> {
            if (blogList != null) {
                followingBlogs.clear();
                for (int i = 0; i < blogList.size(); i++) {
                    if (blogList.get(i).getCategory().equals(category)) {
                        followingBlogs.add(blogList.get(i));
                    }
                }
                for(int i = 0; i < followingBlogs.size(); i++) {
                    Log.d("DEBUG", "following blog id: " + followingBlogs.get(i).getBlogId());
                }
                binding.layoutNoBlog.setVisibility(View.GONE);
                binding.rvBlogs.setVisibility(View.VISIBLE);
                followingAdapter = new FollowingAdapter(followingBlogs, userLogin);
                binding.rvBlogs.setAdapter(followingAdapter);
            }
            else {
                Log.d("DEBUG", "null following blog");
                binding.layoutNoBlog.setVisibility(View.VISIBLE);
                binding.rvBlogs.setVisibility(View.GONE);
            }
        });
    }
}