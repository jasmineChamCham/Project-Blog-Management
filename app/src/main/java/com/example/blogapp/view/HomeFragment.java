package com.example.blogapp.view;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.blogapp.R;
import com.example.blogapp.databinding.ActivityMainBinding;
import com.example.blogapp.databinding.EditBlogItemBinding;
import com.example.blogapp.databinding.FragmentHomeBinding;
import com.example.blogapp.model.Blog;
import com.example.blogapp.model.User;
import com.example.blogapp.viewmodel.BlogAdapter;
import com.example.blogapp.viewmodel.DBHelper;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private DBHelper dbHelper;

//    private ArrayList<Blog> blogList;
//    private ArrayList<User> userList;
//    private BlogAdapter blogAdapter;

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
        View view = binding.getRoot();

        dbHelper = new DBHelper(view.getContext());
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper.addUser("U00001", "HoaiAnh", "hoaianh@gmail.com", "123456", "10/1/2000");
//        blogList = new ArrayList<Blog>();
//        userList = new ArrayList<User>();
//        blogAdapter = new BlogAdapter(blogList, userList);
//        binding.rvBlogs.setAdapter(blogAdapter);
//        binding.rvBlogs.setLayoutManager(new LinearLayoutManager(getContext()));
//
//
//        // Test data
//        User user1 = new User("001", "HoaiAnh",
//                "anh@gmail.com", "123", "17/11/2002");
//        User user2 = new User("002", "Alex",
//                "alex@gmail.com", "123", "20/12/2002");
//        Blog blog = new Blog("1", "Hello", "My name is Alex",
//                "26/03/2023", "002", 0, 0,
//                "Short story", "Draft");
//        binding.setUser(user1);
//        blogList.add(blog);
//        userList.add(user2);
//        blogAdapter.notifyDataSetChanged();
        binding.rvBlogs.setLayoutManager(new LinearLayoutManager(getContext()));

//        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Blog, BlogHolder>(dbHelper.op) {
//            @NonNull
//            @Override
//            public BlogHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//                View view = LayoutInflater.from(parent.getContext())
//                        .inflate(R.layout.view_blog_item, parent, false);
//                return new BlogHolder(view);
//            }
//
//            @Override
//            public void onBindViewHolder(@NonNull BlogHolder holder, int position, Blog blog) {
//                String userId =
//                holder.tvUsername.setText(blog.getUserId());
//                holder.tvTitle.setText(blog.getTitle());
//                holder.tvContent.setText(blog.getContent());
//                holder.
//                holder.tvDate.setText(post.getDate());
//                holder.layoutNote.setBackgroundColor(Color.parseColor(post.getColor()));
//                holder.ivAction.setOnClickListener(new View.OnClickListener() {
//                    @RequiresApi(api = Build.VERSION_CODES.M)
//                    @Override
//                    public void onClick(View view) {
//                        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
//                        popupMenu.setGravity(Gravity.END);
//                        popupMenu.getMenu().add("Edit").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//                            @Override
//                            public boolean onMenuItemClick(@NonNull MenuItem item) {
//                                editNote((Post) getItem(position));
//                                String postID = ((Post) getItem(position)).getId();
////                                System.out.println("POST ID: " + postID);
//                                return false;
//                            }
//                        });
//                        popupMenu.getMenu().add("Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//                            @Override
//                            public boolean onMenuItemClick(@NonNull MenuItem item) {
//                                deleteNote((Post) getItem(position));
//                                return false;
//                            }
//                        });
//                        popupMenu.show();
//                    }
//                });
//            }
//        };
//        binding.rvBlogs.setAdapter(adapter);
//        adapter.startListening();

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

    public static class BlogHolder extends RecyclerView.ViewHolder {

        public ShapeableImageView ivAvatar;
        public TextView tvUsername;
        public TextView tvTime;
        public TextView tvTitle;
        public TextView tvContent;

        public BlogHolder(View view) {
            super(view);
            ivAvatar = view.findViewById(R.id.iv_avatar);
            tvUsername = view.findViewById(R.id.tv_username);
            tvTime = view.findViewById(R.id.tv_time);
            tvTitle = view.findViewById(R.id.tv_title);
            tvContent = view.findViewById(R.id.tv_content);
        }
    }
}