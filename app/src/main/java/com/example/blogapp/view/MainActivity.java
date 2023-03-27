package com.example.blogapp.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.blogapp.R;
import com.example.blogapp.databinding.ActivityMainBinding;
import com.example.blogapp.model.Blog;
import com.example.blogapp.model.User;
import com.example.blogapp.viewmodel.BlogAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private RecyclerView rvBlogs;
    private ArrayList<Blog> blogList;
    private ArrayList<User> userList;
    private BlogAdapter blogAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        rvBlogs = findViewById(R.id.rv_blogs);
        blogList = new ArrayList<Blog>();
        userList = new ArrayList<User>();
        blogAdapter = new BlogAdapter(blogList, userList);
        rvBlogs.setAdapter(blogAdapter);
        rvBlogs.setLayoutManager(new LinearLayoutManager(this));


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
                Button btnOK = mView.findViewById(R.id.btn_OK);
                btnOK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                dialog.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}