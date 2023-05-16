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
import com.example.blogapp.databinding.FragmentExploreBinding;
import com.example.blogapp.model.Blog;
import com.example.blogapp.model.User;
import com.example.blogapp.viewmodel.DBHelper;
import com.example.blogapp.viewmodel.ExploreAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public class ExploreFragment extends Fragment {

    private FragmentExploreBinding binding;
    private DBHelper dbHelper;
    private User userLogin;

    private ArrayList<Blog> exploreBlogs;
    private ExploreAdapter exploreAdapter;

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
        binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.fragment_explore, container, false);
        View viewRoot = binding.getRoot();

        dbHelper = new DBHelper(viewRoot.getContext());
        binding.setUser(userLogin);
        reloadExploreRV();

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
                reloadExploreRV();
                binding.categoryOption.setVisibility(View.GONE);
                binding.layoutNoBlog.setVisibility(View.GONE);
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
                if (exploreAdapter != null) {
                    exploreAdapter.getFilter().filter(newText);
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

    public void reloadExploreRV() {
        exploreBlogs = new ArrayList<>();
        dbHelper.getExploreBlogList(userLogin.getUserId(), blogList -> {
            if (blogList != null) {
                exploreBlogs.clear();
                exploreBlogs.addAll(blogList);
                for(int i = 0; i < exploreBlogs.size(); i++) {
                    Log.d("DEBUG", "explore blog id: " + exploreBlogs.get(i).getBlogId());
                }
                binding.layoutNoBlog.setVisibility(View.GONE);
                binding.rvBlogs.setVisibility(View.VISIBLE);
                exploreAdapter = new ExploreAdapter(exploreBlogs, userLogin);
                binding.rvBlogs.setAdapter(exploreAdapter);
            }
            else {
                Log.d("DEBUG", "null explore blog");
                binding.layoutNoBlog.setVisibility(View.VISIBLE);
                binding.rvBlogs.setVisibility(View.GONE);
            }
        });
    }

    public void filterByCategory(String category) {
        exploreBlogs = new ArrayList<>();
        dbHelper.getExploreBlogList(userLogin.getUserId(), blogList -> {
            if (blogList != null) {
                for (int i = 0; i < blogList.size(); i++) {
                    if (blogList.get(i).getCategory().equals(category)) {
                        exploreBlogs.add(blogList.get(i));
                    }
                }
                for(int i = 0; i < exploreBlogs.size(); i++) {
                    Log.d("DEBUG", "explore blog id: " + exploreBlogs.get(i).getBlogId());
                }
                binding.layoutNoBlog.setVisibility(View.GONE);
                binding.rvBlogs.setVisibility(View.VISIBLE);
                exploreAdapter = new ExploreAdapter(exploreBlogs, userLogin);
                binding.rvBlogs.setAdapter(exploreAdapter);
            }
            else {
                Log.d("DEBUG", "null explore blog");
                binding.layoutNoBlog.setVisibility(View.VISIBLE);
                binding.rvBlogs.setVisibility(View.GONE);
            }
        });
    }
}