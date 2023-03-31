package com.example.blogapp.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.blogapp.R;
import com.example.blogapp.databinding.FragmentBlogListBinding;
import com.example.blogapp.databinding.FragmentEditBlogBinding;
import com.example.blogapp.model.Blog;
import com.example.blogapp.viewmodel.DBHelper;
import com.example.blogapp.viewmodel.DBHelper;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EditBlogFragment extends Fragment {
    FragmentEditBlogBinding binding;
    DBHelper repository;
    Blog blog = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            blog = (Blog) getArguments().getSerializable("blog");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditBlogBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        repository = new DBHelper(view.getContext());

        if (blog != null){
            binding.etTitle.setText(blog.getTitle());
            binding.etContent.setText(blog.getContent());
        }
        else {
            Log.d("DEBUG","there is no id");
        }

        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.edit_blog_menu, menu);
                if (getActivity() != null) {
                    ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
                }
            }
            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.item_done) {
                    final Dialog dialog = new Dialog(view.getContext());
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.bottom_sheet_layout);

                    Button btnDone = dialog.findViewById(R.id.btn_done);
                    ChipGroup chipGroup = dialog.findViewById(R.id.cg_category);
                    RadioGroup radioGroup = dialog.findViewById(R.id.rg_status);

                    if (blog.getStatus() != null && !blog.getStatus().isEmpty()){
                        for (int i = 0; i < radioGroup.getChildCount(); i++) {
                            RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i);
                            if (radioButton.getText().equals(blog.getStatus())) {
                                radioButton.setChecked(true);
                                break;
                            }
                        }
                    }
                    if (blog.getCategory() != null && !blog.getCategory().isEmpty()){
                        for (int i = 0; i < chipGroup.getChildCount(); i++) {
                            Chip chip = (Chip) chipGroup.getChildAt(i);
                            if (chip.getText().equals(blog.getCategory())) {
                                chip.setChecked(true);
                                break;
                            }
                        }
                    }

                    btnDone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String category, status;
                            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                            int checkedStatusId = radioGroup.getCheckedRadioButtonId();
                            if (checkedStatusId != -1) {
                                RadioButton checkedRadioButton = dialog.findViewById(checkedStatusId);
                                status = checkedRadioButton.getText().toString();
                            } else {
                                status = "Published";
                            }
                            int checkedCategoryId = chipGroup.getCheckedChipId();
                            if (checkedCategoryId != 1){
                                Chip chip = dialog.findViewById(checkedCategoryId);
                                category = chip.getText().toString();
                            }
                            else
                                category = "None";
                            if (blog != null)
                                repository.updateNote(blog.getBlogId(),
                                        binding.etTitle.getText().toString(),
                                        binding.etContent.getText().toString(),
                                        isoFormat.format(new Date()),
                                        blog.getUserId(),
                                        blog.getLikesNumber(),
                                        blog.getViewsNumber(),
                                        category,
                                        status);
                            else
                                repository.addBlog(binding.etTitle.getText().toString(),
                                        binding.etContent.getText().toString(),
                                        isoFormat.format(new Date()),
                                        "123",
                                        0,
                                        0,
                                        category,
                                        status);
                            dialog.dismiss();
                            NavController navController = Navigation.findNavController(view);
                            navController.popBackStack();
                        }
                    });
                    dialog.show();
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                    dialog.getWindow().setGravity(Gravity.BOTTOM);
                }
                else if (item.getItemId() == android.R.id.home){
                    getActivity().onBackPressed();
                    return true;
                }
                return false;
            }
        },getViewLifecycleOwner(), Lifecycle.State.RESUMED);
        return view;
    }
}