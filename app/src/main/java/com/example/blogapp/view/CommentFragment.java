package com.example.blogapp.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.blogapp.R;
import com.example.blogapp.databinding.CommentItemBinding;
import com.example.blogapp.databinding.FragmentCommentBinding;
import com.example.blogapp.databinding.ViewBlogItemBinding;
import com.example.blogapp.model.Blog;
import com.example.blogapp.model.Comment;
import com.example.blogapp.model.User;
import com.example.blogapp.viewmodel.DBHelper;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class CommentFragment extends Fragment {

    private FragmentCommentBinding binding;
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
        binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.fragment_comment, container, false);
        View viewRoot = binding.getRoot();

        dbHelper = new DBHelper(viewRoot.getContext());
        binding.setUser(userLogin);
        reloadRecyclerView(dbHelper.getCommentsByBlogId(blogItem.getBlogId()));
        return viewRoot;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.rvComments.setLayoutManager(new LinearLayoutManager(getContext()));

        binding.btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentContent = binding.etCommentContent.getText().toString();
                String userId = userLogin.getUserId();
                String blogId = blogItem.getBlogId();
                dbHelper.addComment(commentContent, userId, blogId);
                binding.etCommentContent.setText("");
            }
        });
    }

    public void reloadRecyclerView(FirebaseRecyclerOptions<Comment> options) {
        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Comment, CommentFragment.CommentHolder>(options) {
            @NonNull
            @Override
            public CommentFragment.CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                CommentItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.comment_item,
                        parent,
                        false);
                return new CommentFragment.CommentHolder(binding);
            }

            @Override
            protected void onBindViewHolder(@NonNull CommentFragment.CommentHolder holder, int position, @NonNull Comment comment) {
                holder.binding.setComment(comment);
                dbHelper.getUserById(comment.getUserId(), user -> {
                    holder.binding.setUser(user);
                });
            }
        };
        binding.rvComments.setAdapter(adapter);
        adapter.startListening();
    }

    public class CommentHolder extends RecyclerView.ViewHolder {
        public CommentItemBinding binding;

        CommentHolder(CommentItemBinding itemsBinding) {
            super(itemsBinding.getRoot());
            this.binding = itemsBinding;
        }
    }
}