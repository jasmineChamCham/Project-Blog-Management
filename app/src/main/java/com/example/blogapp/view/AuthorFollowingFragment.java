package com.example.blogapp.view;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.blogapp.R;
import com.example.blogapp.databinding.FragmentAuthorFollowingBinding;
import com.example.blogapp.databinding.FragmentHomeBinding;
import com.example.blogapp.model.Blog;
import com.example.blogapp.model.User;
import com.example.blogapp.viewmodel.AuthorFollowingAdapter;
import com.example.blogapp.viewmodel.DBHelper;
import com.example.blogapp.viewmodel.FollowingAdapter;

import java.util.ArrayList;


public class AuthorFollowingFragment extends Fragment {

    private FragmentAuthorFollowingBinding binding;
    private DBHelper dbHelper;
    private String authorId;
    private User userLogin;
    private ArrayList<User> authorFollowings;
    private AuthorFollowingAdapter authorFollowingAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            authorId = (String) getArguments().getString("authorId");
            userLogin = (User) getArguments().getSerializable("userLogin");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.fragment_author_following, container, false);
        View viewRoot = binding.getRoot();

        dbHelper = new DBHelper(viewRoot.getContext());
        binding.rvFollowings.setLayoutManager(new LinearLayoutManager(getContext()));
        reloadRV();
        return viewRoot;
    }

    public void reloadRV() {
        authorFollowings = new ArrayList<>();
        dbHelper.getFollowingListOfUser(authorId, followingList -> {
            if (followingList != null) {
                authorFollowings.clear();
                authorFollowings.addAll(followingList);
                for(int i = 0; i < authorFollowings.size(); i++) {
                    Log.d("DEBUG", "author following user id: " + authorFollowings.get(i).getUserId());
                }
                binding.layoutNoPerson.setVisibility(View.GONE);
                binding.rvFollowings.setVisibility(View.VISIBLE);
                authorFollowingAdapter = new AuthorFollowingAdapter(authorFollowings, authorId, userLogin);
                binding.rvFollowings.setAdapter(authorFollowingAdapter);
            }
            else {
                Log.d("DEBUG", "null author following user");
                binding.layoutNoPerson.setVisibility(View.VISIBLE);
                binding.rvFollowings.setVisibility(View.GONE);
            }
        });
    }
}