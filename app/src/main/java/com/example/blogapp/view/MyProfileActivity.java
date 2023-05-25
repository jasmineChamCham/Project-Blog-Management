package com.example.blogapp.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.example.blogapp.databinding.ActivityMyProfileBinding;
import com.example.blogapp.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class MyProfileActivity extends AppCompatActivity {
    private ActivityMyProfileBinding binding;
    private DatabaseReference userRef;
    private DatabaseReference followRef;
    private DatabaseReference avaRef;
    private String userId;
    private String imgPath;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public MyProfileActivity(){
        userRef = FirebaseDatabase.getInstance().getReference("users");
        followRef = FirebaseDatabase.getInstance().getReference("followers");
        avaRef = FirebaseDatabase.getInstance().getReference("ava");
//        Intent receivedIntent = getIntent();
//        if (receivedIntent != null) {
//            Bundle bundle = receivedIntent.getBundleExtra("userBundle");
//            User userLogin = (User) bundle.getSerializable("userLogin");
//            userId = userLogin.getUserId();
//        } else {
//            userId = "-NRlYm-P-HVbQtt_G2Zm";
//        }
        userId = "-NRlYm-P-HVbQtt_G2Zm";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyProfileBinding.inflate(getLayoutInflater());
        setContentView( binding.getRoot());

        binding.butChangeAvaMp.setOnClickListener(v -> {
            Log.d("DEBUG", "CLICK ON AVA");
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("*/*");
            startActivityForResult(Intent.createChooser(intent, "Choose File"), 1);
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
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        getUserInfo();
    }

    public void getUserInfo(){
        Log.d("DEBUG", "userRef: " + userRef);

        userRef.get().addOnCompleteListener(t->{
            if (t.isSuccessful()) {
                Log.d("DEBUG", "into userRef");
                DataSnapshot dataSnapshot = t.getResult();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Log.d("DEBUG", "snapshot user info : " + snapshot.getKey());
                    if (snapshot.getKey().equals(userId)){
                        String name = snapshot.child("name").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);
                        String birthday = snapshot.child("birthday").getValue(String.class);
                        Log.d("DEBUG", "name: " + name);
                        Log.d("DEBUG", "email: " + email);
                        Log.d("DEBUG", "birthday: " + birthday);

                        binding.etNameMp.setText(name);
                        binding.etEmailMp.setText(email);
                        binding.etBirthdayMp.setText(birthday);

                        String encodedData = snapshot.child("ava").getValue(String.class);
                        if (encodedData != null){
                            byte[] data = Base64.decode(encodedData, Base64.DEFAULT);
                            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                            binding.ivAvaMp.setImageBitmap(bmp);
                        }
                    }
                }
            }
        });

        followRef.get().addOnCompleteListener(t -> {
           if (t.isSuccessful()){
               DataSnapshot dataSnapshot = t.getResult();
               long countPeopleFollowingUser = 0;
               long countUserFollowing;

               for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                   String followerId = snapshot.getKey();
                   if (followerId.equals(userId)) {
                       countUserFollowing = snapshot.getChildrenCount();
                       Log.d("DEBUG", "countUserFollowing = "  + countUserFollowing);
                       binding.tvNumFollowerMp.setText("" + countUserFollowing);
                   }

                   for (DataSnapshot i : snapshot.getChildren()) {
                       String followedId = i.getKey();
                       if (followedId.equals(userId)){
                           countPeopleFollowingUser++;
                       }
                   }
                   binding.tvNumFollowerMp.setText("" + countPeopleFollowingUser);
               }
           }
        });
    }

    private void modifyUserInfo(Map<String, String> map){
        String name = map.get("name");
        String email = map.get("email");
        String birthday = map.get("birthday");

        userRef.child(userId).child("name").setValue(name);
        userRef.child(userId).child("email").setValue(email);
        userRef.child(userId).child("birthday").setValue(birthday)
                .addOnSuccessListener(t->{
                    Toast.makeText(getApplicationContext(),
                            "Update user info successfully",
                            Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(),
                            "Update password failed",
                            Toast.LENGTH_LONG).show();
                    Log.d("DEBUG", "Update password failed: " + e.getMessage());
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 1) {
            Uri uri = data.getData();
            Log.d("DEBUG", "File URI: " + uri);
            imgPath = getPathFromUri(uri);
            Log.d("DEBUG", "File path: " + imgPath);
            File imgFile = new File(imgPath);
            Log.d("DEBUG", "File existed: " + imgFile.exists());

            binding.ivAvaMp.setImageURI(uri);

            int permission = ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            );

            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE
                );
            } else {
                saveImage();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d("DEBUG", "grantResults.length = "+ grantResults.length);

        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Log.d("Debug", "let's save image to db");
                saveImage();
            } else {
                // Permission denied
                Log.d("Debug", "permission denied");
            }
        }
    }

    private void saveImage() {
        Log.d("DEBUG", "saveImage path = " + imgPath);
        File imgFile = new File(imgPath);
        try {
            FileInputStream stream = new FileInputStream(imgFile);
            byte[] data = new byte[(int) imgFile.length()];
            stream.read(data);
            Log.d("DEBUG", "Data after steam.read() : " + data.toString());
            // Convert the byte array to a Base64 encoded string
            String encodedData = Base64.encodeToString(data, Base64.DEFAULT);
            Map<String, String> map = new HashMap<>();
            map.put("ava", encodedData);
            userRef.child(userId).child("ava")
                    .setValue(map)
                    .addOnCompleteListener(t -> Log.d("DEBUG", "Save ava successully"));

            stream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getPathFromUri(Uri uri) {
        String filePath = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            filePath = cursor.getString(columnIndex);
            cursor.close();
        }
        return filePath;
    }
}