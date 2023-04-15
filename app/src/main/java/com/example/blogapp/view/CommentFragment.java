package com.example.blogapp.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.example.blogapp.R;
import com.example.blogapp.databinding.CommentItemBinding;
import com.example.blogapp.databinding.FragmentCommentBinding;
import com.example.blogapp.model.Blog;
import com.example.blogapp.model.Comment;
import com.example.blogapp.model.User;
import com.example.blogapp.viewmodel.DBHelper;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.text.SimpleDateFormat;
import java.util.Date;

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
//        dbHelper.getCommentsByBlogId(blogItem.getBlogId(), options -> {
//            if (options != null) {
//                binding.layoutNoComment.setVisibility(View.GONE);
//                binding.rvComments.setVisibility(View.VISIBLE);
//                reloadRecyclerView(options);
//            }
//            else {
//                binding.layoutNoComment.setVisibility(View.VISIBLE);
//                binding.rvComments.setVisibility(View.GONE);
//            }
//        });
        return viewRoot;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper.getCommentsByBlogId(blogItem.getBlogId(), options -> {
            if (options != null) {
                binding.layoutNoComment.setVisibility(View.GONE);
                binding.rvComments.setVisibility(View.VISIBLE);
                reloadRecyclerView(options);
            }
            else {
                binding.layoutNoComment.setVisibility(View.VISIBLE);
                binding.rvComments.setVisibility(View.GONE);
            }
        });

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
                Date date = new Date(comment.getCreatedTime());
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                String dateString = dateFormat.format(date);
                holder.binding.setCreatedTime(dateString);
                dbHelper.getUserById(comment.getUserId(), user -> {
                    holder.binding.setUser(user);
                    if (user.getUserId().equals(userLogin.getUserId())) {
                        holder.binding.btnModifyComment.setVisibility(View.VISIBLE);
                    }
                });
                holder.binding.btnModifyComment.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(View view) {
                        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
                        popupMenu.setGravity(Gravity.END);
                        Menu menu = popupMenu.getMenu();
                        menu.add("Edit").setIcon(R.drawable.ic_baseline_edit_24)
                                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(@NonNull MenuItem item) {
//                                System.out.println("edit comment: " + comment.getCommentContent());
                                holder.binding.layoutEditComment.setVisibility(View.VISIBLE);
                                holder.binding.tvCommentContent.setVisibility(View.GONE);
                                holder.binding.btnDone.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String updateContent = holder.binding.etCommentContent.getText().toString();
                                        Comment updateComment = new Comment(comment.getCommentId(), updateContent,
                                                comment.getUserId(), comment.getBlogId(), comment.getCreatedTime());
                                        dbHelper.updateComment(updateComment);
                                        holder.binding.layoutEditComment.setVisibility(View.GONE);
                                        holder.binding.tvCommentContent.setVisibility(View.VISIBLE);
                                    }
                                });
                                return false;
                            }
                        });
                        menu.add("Delete").setIcon(R.drawable.ic_baseline_delete_24)
                                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(@NonNull MenuItem item) {
//                                System.out.println("delete comment: " + comment.getCommentContent());
                                AlertDialog.Builder builder = new AlertDialog.Builder(binding.getRoot().getContext());
                                builder.setTitle("Confirmation");
                                builder.setMessage("Are you sure you want to delete this comment?");
                                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // User clicked Yes button
                                        dbHelper.deleteComment(comment);
                                    }
                                });
                                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // User clicked No button
                                        dialog.dismiss();
                                    }
                                });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                                return false;
                            }
                        });
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            popupMenu.setForceShowIcon(true);
                        }
                        popupMenu.show();
                    }
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