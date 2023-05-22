package com.example.blogapp.view;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.blogapp.R;
import com.example.blogapp.databinding.FragmentAuthorFollowerBinding;
import com.example.blogapp.model.User;
import com.example.blogapp.viewmodel.AuthorFollowerAdapter;
import com.example.blogapp.viewmodel.AuthorFollowingAdapter;
import com.example.blogapp.viewmodel.DBHelper;
import com.example.blogapp.viewmodel.FollowingAdapter;

import java.util.ArrayList;


public class AuthorFollowerFragment extends Fragment {

    private FragmentAuthorFollowerBinding binding;
    private DBHelper dbHelper;
    private String authorId;
    private User userLogin;
    private ArrayList<User> authorFollowers;
    private AuthorFollowerAdapter authorFollowerAdapter;

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
        binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.fragment_author_follower, container, false);
        View viewRoot = binding.getRoot();

        dbHelper = new DBHelper(viewRoot.getContext());
        binding.rvFollowers.setLayoutManager(new LinearLayoutManager(getContext()));
        reloadRV();

        return viewRoot;
    }

    public void reloadRV() {
        authorFollowers = new ArrayList<>();
        dbHelper.getFollowerListOfUser(authorId, userList -> {
            if (userList != null) {
                authorFollowers.clear();
                authorFollowers.addAll(userList);
                for(int i = 0; i < authorFollowers.size(); i++) {
                    Log.d("DEBUG", "author follower user id: " + authorFollowers.get(i).getUserId());
                }
                binding.layoutNoPerson.setVisibility(View.GONE);
                binding.rvFollowers.setVisibility(View.VISIBLE);
                authorFollowerAdapter = new AuthorFollowerAdapter(authorFollowers, authorId, userLogin);
                binding.rvFollowers.setAdapter(authorFollowerAdapter);
            }
            else {
                Log.d("DEBUG", "null author follower user");
                binding.layoutNoPerson.setVisibility(View.VISIBLE);
                binding.rvFollowers.setVisibility(View.GONE);
            }
        });
    }
}