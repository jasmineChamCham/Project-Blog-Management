package com.example.blogapp.view;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.blogapp.R;

public class ProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Button butStatistics = findViewById(R.id.but_statistics);
        Button butNewfeed = findViewById(R.id.but_newfeed);
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

                EditText etCurPw = myView.findViewById(R.id.et_cur_pw);
                EditText etNewPw = myView.findViewById(R.id.et_new_pw);
                Button butChangePass = myView.findViewById(R.id.but_change_pass);

                butChangePass.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (etCurPw.getText().toString().equals(etNewPw.getText().toString())){
                            Toast.makeText(ProfileActivity.this, "New password is the same as the current one", Toast.LENGTH_LONG).show();
                            etCurPw.setText("");
                            etNewPw.setText("");
                            etCurPw.requestFocus();
                        } else {
                            // CHANGE PASSWORD IN FIREBASE
                            // ....

//                    myRef.child(model.getId()).setValue(new Post(model.getId(),title,content,getRandomColor(),model.getDate(), emotion))
//                            .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Void> task) {
//                                    if (task.isSuccessful()){
//                                        Toast.makeText(MainActivity.this, "Update note successfully", Toast.LENGTH_LONG).show();
//                                    } else {
//                                        Toast.makeText(MainActivity.this, "Update note failed", Toast.LENGTH_LONG).show();
//                                    }
//                                }
//                            });

                            dialog.dismiss();
                        }
                    }
                });

                dialog.show();

            }
        });

        butLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // myRef => logout

                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}