package com.example.blogapp.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.blogapp.R;
import com.example.blogapp.databinding.ActivityMainBinding;
import com.example.blogapp.databinding.FragmentHomeBinding;
import com.example.blogapp.model.Blog;
import com.example.blogapp.model.User;
import com.example.blogapp.viewmodel.BlogAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private ArrayList<Blog> blogList;
    private ArrayList<User> userList;
    private BlogAdapter blogAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.fragment_home, container, false);
        View viewRoot = binding.getRoot();
        return viewRoot;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        blogList = new ArrayList<Blog>();
        userList = new ArrayList<User>();
        blogAdapter = new BlogAdapter(blogList, userList);
        binding.rvBlogs.setAdapter(blogAdapter);
        binding.rvBlogs.setLayoutManager(new LinearLayoutManager(getContext()));


        // Test data
        User user1 = new User("001", "HoaiAnh",
                "anh@gmail.com", "123", "17/11/2002");
        User user2 = new User("002", "Alex",
                "alex@gmail.com", "123", "20/12/2002");
        Blog blog = new Blog("1", "Hello", "My name is Alex",
                "26/03/2023", "002", 0, 0,
                "Short story", "Draft");
        binding.setUser(user1);
        blogList.add(blog);
        userList.add(user2);
        blogAdapter.notifyDataSetChanged();

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

                    }
                });
                ChipGroup chipGroup1 = mView.findViewById(R.id.chip_category);
                chipGroup1.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(ChipGroup group, int checkedId) {
                        Chip chip = group.findViewById(checkedId);
                        if (chip != null) {
                            String checkedCategory = chip.getText().toString();
                            Toast.makeText(getContext(), "Clicked " + checkedCategory, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog.show();
            }
        });
    }
}