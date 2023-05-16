package com.example.blogapp.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.blogapp.R;
import com.example.blogapp.databinding.FragmentPersonalBinding;
import com.example.blogapp.model.User;
import com.example.blogapp.viewmodel.DBHelper;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PersonalFragment extends Fragment {
    private FirebaseUser userDB;
    private User userLogin;
    FragmentPersonalBinding binding;
    DBHelper dbHelper;
    EditText etCurPw, etNewPw, etConfirmPw;
    Button butChangePass;

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
        binding = FragmentPersonalBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        dbHelper = new DBHelper(getContext());
        binding.setUser(userLogin);

        binding.btnStatistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("userLogin", userLogin);
                Intent intent = new Intent(getActivity(), StatisticsActivity.class);
                intent.putExtra("userBundle", bundle);
                startActivity(intent);
            }
        });
        binding.btnMyBlogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BlogManagerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("userLogin", userLogin);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        binding.btnChangePw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder mDialog = new AlertDialog.Builder(getContext());
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                View myView = inflater.inflate(R.layout.change_password, null);
                mDialog.setView(myView);

                AlertDialog dialog = mDialog.create();
                dialog.setCancelable(true);

                etCurPw = myView.findViewById(R.id.et_cur_pw);
                etNewPw = myView.findViewById(R.id.et_new_pw);
                etConfirmPw = myView.findViewById(R.id.et_confirm_pw);
                butChangePass = myView.findViewById(R.id.but_change_pass);

                butChangePass.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        changePwUserLogin(dialog);
                    }
                });
                dialog.show();
            }
        });

        binding.btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                Navigation.findNavController(v).navigate(R.id.loginFragment, bundle);
            }
        });

        binding.btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("userLogin", userLogin);
                Navigation.findNavController(v).navigate(R.id.homeFragment, bundle);
            }
        });

        binding.btnMyProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("userLogin", userLogin);
                Intent intent = new Intent(getActivity(), MyProfileActivity.class);
                intent.putExtra("userBundle", bundle);
                startActivity(intent);
            }
        });
        return view;
    }

    private void resetChangePasswordDialog(){
        etCurPw.setText("");
        etNewPw.setText("");
        etConfirmPw.setText("");
    }

    private void changePwUserLogin(AlertDialog dialog){
        if (etCurPw.getText().toString().equals(etNewPw.getText().toString())){
            Toast.makeText(getContext(),
                    "New password is the same as the current one",
                    Toast.LENGTH_LONG).show();
            resetChangePasswordDialog();
            etCurPw.requestFocus();
        } else if (!etConfirmPw.getText().toString().equals(etNewPw.getText().toString())){
            Toast.makeText(getContext(),
                    "Your confirmation password is not correct",
                    Toast.LENGTH_LONG).show();
            resetChangePasswordDialog();
            etCurPw.requestFocus();
        } else {
            String email = userLogin.getEmail();
            String curPw = etCurPw.getText().toString();
            String newPw = etNewPw.getText().toString();

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");

            userRef.get().addOnCompleteListener(t -> {
                DataSnapshot ds = t.getResult();
                for (DataSnapshot snapshot : ds.getChildren()){
                    String userId = snapshot.getKey();
                    String rightEmail = snapshot.child("email").getValue(String.class);
                    if (rightEmail.equals(email)) {
                        String righPw = snapshot.child("password").getValue(String.class);
                        if (righPw.equals(curPw)){
                            userRef.child(userId).child("password").setValue(newPw)
                                    .addOnCompleteListener(task -> {
                                        Toast.makeText(getContext(),
                                                "Update password successfully",
                                                Toast.LENGTH_LONG).show();
                                        dialog.dismiss();
                                    });
                        } else {
                            Toast.makeText(getContext(),
                                    "Wrong current password",
                                    Toast.LENGTH_LONG).show();
                            resetChangePasswordDialog();
                            etCurPw.requestFocus();
                        }
                    }
                }
            });
        }
    }


}