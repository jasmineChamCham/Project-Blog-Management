package com.example.blogapp.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.blogapp.R;
import com.example.blogapp.databinding.EditBlogItemBinding;
import com.example.blogapp.databinding.FragmentBlogListBinding;
import com.example.blogapp.model.Blog;
import com.example.blogapp.model.User;
import com.example.blogapp.viewmodel.DBHelper;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BlogListFragment extends Fragment {
    FragmentBlogListBinding binding;
    DBHelper dbHelper;
    private User userLogin;
    int blogStatus = 1;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        Intent intent = getActivity().getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            userLogin = (User) bundle.getSerializable("userLogin");
        }
        // temporarily for faster testing, will delete later when complete
        else {
            userLogin = new User("-NRvClt0Ahu_stjh3Z5G", "Ngoc Linh", "ngoclinh@gmail.com", "123456", "1/2/1999");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBlogListBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        dbHelper = new DBHelper(view.getContext());

        binding.rvBlogs.setLayoutManager(new LinearLayoutManager(view.getContext()));
        if (blogStatus == 1){
            binding.btnPublished.setBackgroundColor(getResources().getColor(R.color.main_color));
            binding.btnTrash.setBackgroundColor(getResources().getColor(R.color.white));
            binding.btnDrafts.setBackgroundColor(getResources().getColor(R.color.white));
            binding.btnPublished.setTextColor(getResources().getColor(R.color.white));
            binding.btnTrash.setTextColor(getResources().getColor(R.color.main_color));
            binding.btnDrafts.setTextColor(getResources().getColor(R.color.main_color));
        }
        else if (blogStatus == 2){
            binding.btnPublished.setBackgroundColor(getResources().getColor(R.color.white));
            binding.btnTrash.setBackgroundColor(getResources().getColor(R.color.white));
            binding.btnDrafts.setBackgroundColor(getResources().getColor(R.color.main_color));
            binding.btnPublished.setTextColor(getResources().getColor(R.color.main_color));
            binding.btnTrash.setTextColor(getResources().getColor(R.color.main_color));
            binding.btnDrafts.setTextColor(getResources().getColor(R.color.white));
        }
        else if (blogStatus == 3){
            binding.btnPublished.setBackgroundColor(getResources().getColor(R.color.white));
            binding.btnTrash.setBackgroundColor(getResources().getColor(R.color.main_color));
            binding.btnDrafts.setBackgroundColor(getResources().getColor(R.color.white));
            binding.btnPublished.setTextColor(getResources().getColor(R.color.main_color));
            binding.btnTrash.setTextColor(getResources().getColor(R.color.white));
            binding.btnDrafts.setTextColor(getResources().getColor(R.color.main_color));
        }
        FirebaseRecyclerOptions<Blog> options = dbHelper.getOptionBlogByUserId(userLogin.getUserId());
        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Blog, BlogHolder>(options) {
            @NonNull
            @Override
            public BlogHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                EditBlogItemBinding binding =
                        DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                                R.layout.edit_blog_item,
                                parent,
                                false);
                return new BlogHolder(binding);
            }

            @Override
            protected void onBindViewHolder(@NonNull BlogHolder holder, int position, @NonNull Blog model) {
                Date date = new Date(model.getCreatedTime());
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                String dateString = dateFormat.format(date);
                holder.binding.setCreatedTime(dateString);
                if ((blogStatus == 1 && model.getStatus().equals("Published")) ||
                        (blogStatus == 2 && model.getStatus().equals("Drafts"))  ||
                        (blogStatus == 3 && model.getStatus().equals("Trash"))) {
                    holder.itemView.setVisibility(View.VISIBLE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                }
                else {
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                }
                holder.binding.setBlog(model);
                holder.binding.setContent(Html.fromHtml(model.getContent()));
                if (blogStatus == 3) {
                    holder.binding.llEdit.setVisibility(View.GONE);
                    holder.binding.llTrash.setVisibility(View.VISIBLE);
                } else {
                    holder.binding.llEdit.setVisibility(View.VISIBLE);
                    holder.binding.llTrash.setVisibility(View.GONE);
                }
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("blog", model);
                        bundle.putSerializable("userLogin", userLogin);
                        Navigation.findNavController(binding.getRoot()).navigate(R.id.editBlogFragment, bundle);
                    }
                });
                holder.binding.btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("blog", model);
                        bundle.putSerializable("userLogin", userLogin);
                        Navigation.findNavController(binding.getRoot()).navigate(R.id.editBlogFragment, bundle);
                    }
                });
                holder.binding.btnMore.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(View v) {
                        PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                        popupMenu.setGravity(Gravity.END);
                        Menu menu = popupMenu.getMenu();
                        menu.add("Comment").setIcon(R.drawable.ic_baseline_chat_bubble_outline_24)
                                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(@NonNull MenuItem item) {
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable("userLogin", userLogin);
                                        bundle.putSerializable("blogItem", model);
                                        Navigation.findNavController(binding.getRoot()).navigate(R.id.commentFragment2, bundle);
                                        return true;
                                    }
                                });
                        menu.add("Move to Trash").setIcon(R.drawable.ic_baseline_delete_24)
                                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(@NonNull MenuItem item) {
                                        dbHelper.updateBlog(model.getBlogId(),
                                                model.getTitle(),
                                                model.getContent(),
                                                model.getCreatedTime(),
                                                model.getUserId(),
                                                model.getLikesNumber(),
                                                model.getViewsNumber(),
                                                model.getCategory(),
                                                "Trash");
                                        return true;
                                    }
                                });
                        menu.add("Chart").setIcon(R.drawable.ic_baseline_bar_chart_24)
                                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(@NonNull MenuItem item) {
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable("userLogin", userLogin);
                                        bundle.putSerializable("blogItem", model);
                                        Navigation.findNavController(view).navigate(R.id.blogStatisticsFragment,bundle);
                                        return true;
                                    }
                                });
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            popupMenu.setForceShowIcon(true);
                        }
                        popupMenu.show();
                    }
                });
                holder.binding.btnRestore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dbHelper.updateBlog(model.getBlogId(),
                                model.getTitle(),
                                model.getContent(),
                                model.getCreatedTime(),
                                model.getUserId(),
                                model.getLikesNumber(),
                                model.getViewsNumber(),
                                model.getCategory(),
                                "Drafts");
                    }
                });
                holder.binding.btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(binding.getRoot().getContext());
                        builder.setTitle("Confirmation");
                        builder.setMessage("Are you sure you want to delete this blog permanently?");

                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User clicked Yes button
                                dbHelper.deleteBlog(model.getBlogId());
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User clicked No button
                                dialog.dismiss();
                            }
                        });

                        // Create and show the AlertDialog
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });
                holder.binding.btnModify.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Dialog dialog = new Dialog(binding.getRoot().getContext());
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.bottom_sheet_layout);

                        Button btnDone = dialog.findViewById(R.id.btn_done);
                        ChipGroup chipGroup = dialog.findViewById(R.id.cg_category);
                        RadioGroup radioGroup = dialog.findViewById(R.id.rg_status);

                        if (model != null) {
                            if (model.getStatus() != null && !model.getStatus().isEmpty()) {
                                for (int i = 0; i < radioGroup.getChildCount(); i++) {
                                    RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i);
                                    if (radioButton.getText().equals(model.getStatus())) {
                                        radioButton.setChecked(true);
                                        break;
                                    }
                                }
                            }
                            if (model.getCategory() != null && !model.getCategory().isEmpty()) {
                                for (int i = 0; i < chipGroup.getChildCount(); i++) {
                                    Chip chip = (Chip) chipGroup.getChildAt(i);
                                    if (chip.getText().equals(model.getCategory())) {
                                        chip.setChecked(true);
                                        break;
                                    }
                                }
                            }
                        }
                        btnDone.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String category, status;
                                int checkedStatusId = radioGroup.getCheckedRadioButtonId();
                                if (checkedStatusId != -1) {
                                    RadioButton checkedRadioButton = dialog.findViewById(checkedStatusId);
                                    status = checkedRadioButton.getText().toString();
                                } else {
                                    status = "Published";
                                }
                                int checkedCategoryId = chipGroup.getCheckedChipId();
                                if (checkedCategoryId != 1) {
                                    Chip chip = dialog.findViewById(checkedCategoryId);
                                    category = chip.getText().toString();
                                } else
                                    category = "None";
                                if (model != null)
                                    dbHelper.updateBlog(model.getBlogId(),
                                            model.getTitle(),
                                            model.getContent(),
                                            model.getCreatedTime(),
                                            model.getUserId(),
                                            model.getLikesNumber(),
                                            model.getViewsNumber(),
                                            category,
                                            status);
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                        dialog.getWindow().setGravity(Gravity.BOTTOM);
                    }
                });
            }
        };
        binding.rvBlogs.setAdapter(adapter);
        adapter.startListening();

        binding.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("userLogin", userLogin);
                Navigation.findNavController(view).navigate(R.id.editBlogFragment,bundle);
            }
        });
        binding.btnPublished.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                reload(repository.getBlogOptionByUserId(userLogin.getUserId()));
                blogStatus = 1;
                adapter.notifyDataSetChanged();
                binding.btnPublished.setBackgroundColor(getResources().getColor(R.color.main_color));
                binding.btnTrash.setBackgroundColor(getResources().getColor(R.color.white));
                binding.btnDrafts.setBackgroundColor(getResources().getColor(R.color.white));
                binding.btnPublished.setTextColor(getResources().getColor(R.color.white));
                binding.btnTrash.setTextColor(getResources().getColor(R.color.main_color));
                binding.btnDrafts.setTextColor(getResources().getColor(R.color.main_color));
            }
        });
        binding.btnDrafts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                reload(repository.getBlogOptionByUserId(userLogin.getUserId()));
                blogStatus = 2;
                adapter.notifyDataSetChanged();
                binding.btnPublished.setBackgroundColor(getResources().getColor(R.color.white));
                binding.btnTrash.setBackgroundColor(getResources().getColor(R.color.white));
                binding.btnDrafts.setBackgroundColor(getResources().getColor(R.color.main_color));
                binding.btnPublished.setTextColor(getResources().getColor(R.color.main_color));
                binding.btnTrash.setTextColor(getResources().getColor(R.color.main_color));
                binding.btnDrafts.setTextColor(getResources().getColor(R.color.white));
            }
        });
        binding.btnTrash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                reload(repository.getBlogOptionByUserId(userLogin.getUserId()));
                blogStatus = 3;
                adapter.notifyDataSetChanged();
                binding.btnPublished.setBackgroundColor(getResources().getColor(R.color.white));
                binding.btnTrash.setBackgroundColor(getResources().getColor(R.color.main_color));
                binding.btnDrafts.setBackgroundColor(getResources().getColor(R.color.white));
                binding.btnPublished.setTextColor(getResources().getColor(R.color.main_color));
                binding.btnTrash.setTextColor(getResources().getColor(R.color.white));
                binding.btnDrafts.setTextColor(getResources().getColor(R.color.main_color));
            }
        });
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.search_menu, menu);
                if (getActivity() != null) {
                    ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
                }
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == android.R.id.home){
                    getActivity().onBackPressed();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
        return view;
    }

    public class BlogHolder extends RecyclerView.ViewHolder {
        public EditBlogItemBinding binding;

        BlogHolder(EditBlogItemBinding itemsBinding) {
            super(itemsBinding.getRoot());
            this.binding = itemsBinding;
        }
    }
}