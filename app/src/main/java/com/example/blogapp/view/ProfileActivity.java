package com.example.blogapp.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.blogapp.R;
import com.example.blogapp.viewmodel.BlogRepository;
import com.example.blogapp.viewmodel.FollowRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Date;
import java.util.GregorianCalendar;

public class ProfileActivity extends AppCompatActivity {
    private FirebaseUser user;
    EditText etCurPw;
    EditText etNewPw;
    EditText etConfirmPw;
    TextView tvName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Test with jasmine user
        BlogRepository.mAuth = FirebaseAuth.getInstance();
        BlogRepository.mAuth.signInWithEmailAndPassword("jasminebkdn@gmail.com", "jasminebkdn123")
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Log.d("DEBUG", "login successful");
                        } else {
                            Log.d("DEBUG", "login failed");
                        }
                    }
                });

        user = BlogRepository.mAuth.getCurrentUser();

//        FollowRepository.addFollowInstance("Ngoc Tram", "Ngoc Linh", new GregorianCalendar(2023, 3, 5).getTime().getTime());
//        FollowRepository.addFollowInstance("Micheal", "Ngoc Tram", new GregorianCalendar(2023, 3, 5).getTime().getTime());
//        FollowRepository.addFollowInstance("Hoai Anh", "Ngoc Tram", new GregorianCalendar(2023, 3, 6).getTime().getTime());
//        FollowRepository.addFollowInstance("Michelle", "Ngoc Tram", new GregorianCalendar(2023, 3, 7).getTime().getTime());
//        FollowRepository.addFollowInstance("Minh Suong", "Ngoc Tram", new GregorianCalendar(2023, 3, 8).getTime().getTime());
//        FollowRepository.addFollowInstance("Minh Suong", "Ngoc Tram", new GregorianCalendar(2023, 3, 9).getTime().getTime());
//        FollowRepository.addFollowInstance("Minh Suong", "Ngoc Tram", new GregorianCalendar(2023, 3, 10).getTime().getTime());
//        FollowRepository.addFollowInstance("Minh Suong", "Ngoc Tram", new GregorianCalendar(2023, 3, 8).getTime().getTime());
//        FollowRepository.addFollowInstance("Minh Suong", "Ngoc Tram", new GregorianCalendar(2023, 3, 9).getTime().getTime());
//        FollowRepository.addFollowInstance("Minh Suong", "Ngoc Tram", new GregorianCalendar(2023, 3, 8).getTime().getTime());
//        FollowRepository.addFollowInstance("Minh Suong", "Ngoc Tram", new GregorianCalendar(2023, 3, 6).getTime().getTime());



        tvName = findViewById(R.id.tv_name);
        tvName.setText(user.getEmail());

        Button butStatistics = findViewById(R.id.but_statistics);
        Button butPosts = findViewById(R.id.but_posts);
        Button butProfile = findViewById(R.id.but_profile);
        Button butChangePw = findViewById(R.id.but_change_pw);
        Button butLogout = findViewById(R.id.but_logout);

        butStatistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, StatisticsActivity.class);
                startActivity(intent);
            }
        });

        butChangePw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder mDialog = new AlertDialog.Builder(ProfileActivity.this);
                LayoutInflater inflater = LayoutInflater.from(ProfileActivity.this);
                View myView = inflater.inflate(R.layout.change_password, null);
                mDialog.setView(myView);

                AlertDialog dialog = mDialog.create();
                dialog.setCancelable(true);

                etCurPw = myView.findViewById(R.id.et_cur_pw);
                etNewPw = myView.findViewById(R.id.et_new_pw);
                etConfirmPw = myView.findViewById(R.id.et_confirm_pw);
                Button butChangePass = myView.findViewById(R.id.but_change_pass);

                butChangePass.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (etCurPw.getText().toString().equals(etNewPw.getText().toString())){
                            Toast.makeText(ProfileActivity.this,
                                    "New password is the same as the current one",
                                    Toast.LENGTH_LONG).show();
                            resetChangePasswordDialog();
                            etCurPw.requestFocus();
                        } else if (!etConfirmPw.getText().toString().equals(etNewPw.getText().toString())){
                            Toast.makeText(ProfileActivity.this,
                                    "Your confirmation password is not correct",
                                    Toast.LENGTH_LONG).show();
                            resetChangePasswordDialog();
                            etCurPw.requestFocus();
                        } else {
                            // CHANGE PASSWORD IN FIREBASE
                            String email = user.getEmail();
                            String curPw = etCurPw.getText().toString();
                            String newPw = etNewPw.getText().toString();

                            AuthCredential credential = EmailAuthProvider.getCredential(email, curPw);
                            user.reauthenticate(credential)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    user.updatePassword(newPw)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> taskUpdatePw) {
                                                                    if (taskUpdatePw.isSuccessful()) {
                                                                        Toast.makeText(ProfileActivity.this,
                                                                                "Update password successfully",
                                                                                Toast.LENGTH_LONG).show();
                                                                        dialog.dismiss();
                                                                    } else {
                                                                        Toast.makeText(ProfileActivity.this,
                                                                                "Failed to update password",
                                                                                Toast.LENGTH_LONG).show();
                                                                        resetChangePasswordDialog();
                                                                    }
                                                                }
                                                            });
                                                }
                                                else {
                                                    Toast.makeText(ProfileActivity.this,
                                                            "Wrong current password",
                                                            Toast.LENGTH_LONG).show();
                                                    resetChangePasswordDialog();
                                                }
                                        }
                                        });
                        }
                    }
                });
                dialog.show();
            }
        });

        butLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BlogRepository.mAuth.signOut();
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void resetChangePasswordDialog(){
        etCurPw.setText("");
        etNewPw.setText("");
        etConfirmPw.setText("");
    }
}