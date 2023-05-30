package com.example.blogapp.view;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.blogapp.R;
import com.example.blogapp.databinding.FragmentMyProfileBinding;
import com.example.blogapp.model.User;
import com.example.blogapp.viewmodel.DBHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MyProfileFragment extends Fragment {

    private FragmentMyProfileBinding binding;
    private DatabaseReference userRef;
    private User userLogin;
    private String imgPath;
    private DBHelper dbHelper;

    private static final int REQUEST_EXTERNAL_STORAGE = 100;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userLogin = (User) getArguments().getSerializable("userLogin");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.fragment_my_profile, container, false);
        View viewRoot = binding.getRoot();

        dbHelper = new DBHelper(requireActivity().getApplicationContext());
        userRef = FirebaseDatabase.getInstance().getReference("users");
//        userId = userLogin.getUserId();

        binding.setUser(userLogin);
        String imgString = userLogin.getAva();
        if (imgString != null && !imgString.equals("")) {
            File imgFile = new File(imgString);
            Log.d("DEBUG", "Start file existed: " + imgFile.exists());
            Log.d("DEBUG", "Start image file: " + imgFile.getAbsoluteFile());

            Picasso.get().load(imgFile.getAbsoluteFile()).into(binding.ivAvaMp);
        }
        else {
            Drawable drawable = getResources().getDrawable(R.drawable.person_avatar);
            binding.ivAvaMp.setImageDrawable(drawable);
        }

        dbHelper.getFollowerCountOfUser(userLogin.getUserId(), count -> {
            binding.tvNumFollowerMp.setText(String.valueOf(count));
        });
        dbHelper.getFollowingCountOfUser(userLogin.getUserId(), count -> {
            binding.tvNumFollowingMp.setText(String.valueOf(count));
        });

        binding.butChangeAvaMp.setOnClickListener(v -> {
            checkStoragePermission();
        });

        binding.butModifyNameMp.setOnClickListener(v -> {
            binding.etNameMp.setEnabled(true);
            binding.etNameMp.setFocusable(true);
            binding.etNameMp.setFocusableInTouchMode(true);
            binding.etNameMp.requestFocus();
        });

        binding.butModifyEmailMp.setOnClickListener(v -> {
            binding.etEmailMp.setEnabled(true);
            binding.etEmailMp.setFocusable(true);
            binding.etEmailMp.setFocusableInTouchMode(true);
            binding.etEmailMp.requestFocus();
        });

        binding.butModifyBirthdayMp.setOnClickListener(v -> {
            binding.etBirthdayMp.setEnabled(true);
            binding.etBirthdayMp.setFocusable(true);
            binding.etBirthdayMp.setFocusableInTouchMode(true);
            binding.etBirthdayMp.requestFocus();
        });

        binding.butSaveMp.setOnClickListener(v -> {
            String name = binding.etNameMp.getText().toString();
            String email = binding.etEmailMp.getText().toString();
            String birthday = binding.etBirthdayMp.getText().toString();
            Map<String, String> map = new HashMap<>();
            map.put("name", name);
            map.put("email", email);
            map.put("birthday", birthday);
            modifyUserInfo(map);
            binding.etNameMp.setEnabled(false);
            binding.etEmailMp.setEnabled(false);
            binding.etBirthdayMp.setEnabled(false);
        });

        binding.tvNumFollowerMp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("authorId", userLogin.getUserId());
                bundle.putSerializable("userLogin", userLogin);
                Navigation.findNavController(v).navigate(R.id.authorFollowerFragment, bundle);
            }
        });

        binding.tvNumFollowingMp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("authorId", userLogin.getUserId());
                bundle.putSerializable("userLogin", userLogin);
                Navigation.findNavController(v).navigate(R.id.authorFollowingFragment, bundle);
            }
        });

        binding.btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.getUserById(userLogin.getUserId(), user -> {
                    userLogin = user;
                    Log.d("DEBUG", userLogin.getName() + " " + userLogin.getAva());
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("userLogin", userLogin);
                    Navigation.findNavController(v).navigate(R.id.homeFragment, bundle);
                });
            }
        });

        return viewRoot;
    }

    private void modifyUserInfo(Map<String, String> map){
        String name = map.get("name");
        String email = map.get("email");
        String birthday = map.get("birthday");

        userRef.child(userLogin.getUserId()).child("name").setValue(name);
        userRef.child(userLogin.getUserId()).child("email").setValue(email);
        userRef.child(userLogin.getUserId()).child("birthday").setValue(birthday)
                .addOnSuccessListener(t->{
                    Toast.makeText(requireContext(),
                            "Update user info successfully",
                            Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(),
                            "Update user info failed",
                            Toast.LENGTH_LONG).show();
                    Log.d("DEBUG", "Update user info failed: " + e.getMessage());
                });
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Request access using Storage Access Framework (SAF)
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("*/*");
            startActivityForResult(intent, REQUEST_EXTERNAL_STORAGE);
        } else {
            // Request legacy storage permissions
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted, request it
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_EXTERNAL_STORAGE);
            } else {
                // Permission already granted
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("*/*");
                startActivityForResult(intent, REQUEST_EXTERNAL_STORAGE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == requireActivity().RESULT_OK && requestCode == REQUEST_EXTERNAL_STORAGE) {
            Uri uri = data.getData();
            Log.d("DEBUG", "Image URI: " + uri);
            imgPath = getPathFromUri(uri);
            Log.d("DEBUG", "Image path: " + imgPath);

            if (imgPath != null) {
                File imgFile = new File(imgPath);
                Log.d("DEBUG", "File existed: " + imgFile.exists());
                Log.d("DEBUG", "Image file: " + imgFile.getAbsoluteFile());

                Picasso.get().load(imgFile.getAbsoluteFile()).into(binding.ivAvaMp);
                saveAvatar();
            }
            else {
                Log.d("DEBUG", "Failed to get file path from URI");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d("DEBUG", "grantResults.length = " + grantResults.length);

        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Log.d("Debug", "let's save image to db");
                saveAvatar();
            } else {
                // Permission denied
                Log.d("Debug", "permission denied");
            }
        }
    }

    private void saveAvatar() {
        Log.d("DEBUG", "saved image path = " + imgPath);
        dbHelper.getUserById(userLogin.getUserId(), user -> {
            userLogin = user;
            dbHelper.updateUser(userLogin.getUserId(),
                    userLogin.getName(),
                    userLogin.getEmail(),
                    userLogin.getPassword(),
                    userLogin.getBirthday(),
                    imgPath);
        });
    }

    private String getPathFromUri(Uri uri) {
        String imagePath = null;
        if (uri != null) {
            if (DocumentsContract.isDocumentUri(requireContext(), uri)) {
                // DocumentProvider
                String documentId = DocumentsContract.getDocumentId(uri);
                if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                    // MediaProvider
                    String[] split = documentId.split(":");
                    String type = split[0];
                    Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    String selection = MediaStore.Images.Media._ID + "=?";
                    String[] selectionArgs = new String[]{split[1]};
                    imagePath = getDataColumn(contentUri, selection, selectionArgs);
                } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                    // DownloadsProvider
                    Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.parseLong(documentId));
                    imagePath = getDataColumn(contentUri, null, null);
                }
            } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                // MediaStore (and general)
                imagePath = getDataColumn(uri, null, null);
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                // File
                imagePath = uri.getPath();
            }
        }
        return imagePath;
    }

    private String getDataColumn(Uri uri, String selection, String[] selectionArgs) {
        String[] projection = {MediaStore.Images.Media.DATA};
        try (Cursor cursor = requireActivity().getContentResolver().query(uri, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                return cursor.getString(columnIndex);
            }
        }
        return null;
    }
}