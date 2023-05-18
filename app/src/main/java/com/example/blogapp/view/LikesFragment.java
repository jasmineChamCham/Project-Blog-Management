package com.example.blogapp.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

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
import com.example.blogapp.databinding.FragmentLikesBinding;
import com.example.blogapp.model.Blog;
import com.example.blogapp.model.User;
import com.example.blogapp.viewmodel.DBHelper;
import com.example.blogapp.viewmodel.LikesAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public class LikesFragment extends Fragment {

    private FragmentLikesBinding binding;
    private DBHelper dbHelper;
    private User userLogin;

    private ArrayList<Blog> likedBlogs;
    private LikesAdapter likedBlogsAdapter;

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
        binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.fragment_likes, container, false);
        View viewRoot = binding.getRoot();

        dbHelper = new DBHelper(viewRoot.getContext());
        binding.setUser(userLogin);
        reloadLikesRV();

        return viewRoot;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.rvBlogs.setLayoutManager(new LinearLayoutManager(getContext()));

        binding.btnFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("userLogin", userLogin);
                Navigation.findNavController(view).navigate(R.id.homeFragment, bundle);
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
                reloadLikesRV();
                binding.categoryOption.setVisibility(View.GONE);
                binding.layoutNoBlog.setVisibility(View.GONE);
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
                if (likedBlogsAdapter != null) {
                    likedBlogsAdapter.getFilter().filter(newText);
                }
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_logout:
                Bundle bundle = new Bundle();
                Navigation.findNavController(getView()).navigate(R.id.loginFragment, bundle);;
                break;
        }
        return true;
    }

    public void handleFilter(String category) {
        binding.categoryOption.setVisibility(View.VISIBLE);
        binding.categoryOption.setText(category);
        filterByCategory(category);
        // date picker
    }

    public void reloadLikesRV() {
        likedBlogs = new ArrayList<>();
        dbHelper.getLikedBlogs(userLogin.getUserId(), likedBlogList -> {
            if (likedBlogList != null) {
                likedBlogs.clear();
                likedBlogs.addAll(likedBlogList);
                for(int i = 0; i < likedBlogs.size(); i++) {
                    Log.d("DEBUG", "likesFragment_liked blog id: " + likedBlogs.get(i).getBlogId());
                }
                binding.layoutNoBlog.setVisibility(View.GONE);
                binding.rvBlogs.setVisibility(View.VISIBLE);
                likedBlogsAdapter = new LikesAdapter(likedBlogs, userLogin);
                binding.rvBlogs.setAdapter(likedBlogsAdapter);
            }
            else {
                Log.d("DEBUG", "null liked blog");
                binding.layoutNoBlog.setVisibility(View.VISIBLE);
                binding.rvBlogs.setVisibility(View.GONE);
            }
        });
    }

    public void filterByCategory(String category) {
        likedBlogs = new ArrayList<>();
        dbHelper.getLikedBlogs(userLogin.getUserId(), likedBlogList -> {
            if (likedBlogList != null) {
                for (int i = 0; i < likedBlogList.size(); i++) {
                    if (likedBlogList.get(i).getCategory().equals(category)) {
                        likedBlogs.add(likedBlogList.get(i));
                    }
                }
                for(int i = 0; i < likedBlogs.size(); i++) {
                    Log.d("DEBUG", "liked blog id: " + likedBlogs.get(i).getBlogId());
                }
                binding.layoutNoBlog.setVisibility(View.GONE);
                binding.rvBlogs.setVisibility(View.VISIBLE);
                likedBlogsAdapter = new LikesAdapter(likedBlogs, userLogin);
                binding.rvBlogs.setAdapter(likedBlogsAdapter);
            }
            else {
                Log.d("DEBUG", "null liked blog");
                binding.layoutNoBlog.setVisibility(View.VISIBLE);
                binding.rvBlogs.setVisibility(View.GONE);
            }
        });
    }
}