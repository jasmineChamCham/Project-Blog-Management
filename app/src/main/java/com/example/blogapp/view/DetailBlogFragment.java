package com.example.blogapp.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.blogapp.R;
import com.example.blogapp.databinding.FragmentDetailBlogBinding;
import com.example.blogapp.model.Blog;
import com.example.blogapp.model.User;
import com.example.blogapp.viewmodel.DBHelper;

public class DetailBlogFragment extends Fragment {

    private FragmentDetailBlogBinding binding;
    private DBHelper dbHelper;
    private User userLogin;
    private Blog blogItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userLogin = (User) getArguments().getSerializable("userLogin");
            blogItem = (Blog) getArguments().getSerializable("blogItem");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.fragment_detail_blog, container, false);
        View viewRoot = binding.getRoot();

        dbHelper = new DBHelper(viewRoot.getContext());
        dbHelper.getUserById(blogItem.getUserId(), user -> {
            binding.setUser(user);
        });
        binding.setBlog(blogItem);
        binding.tvContent.setText(Html.fromHtml(blogItem.getContent()));

        return viewRoot;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("userLogin", userLogin);
                bundle.putSerializable("blogItem", blogItem);
                Navigation.findNavController(v).navigate(R.id.commentFragment, bundle);
            }
        });
    }
}