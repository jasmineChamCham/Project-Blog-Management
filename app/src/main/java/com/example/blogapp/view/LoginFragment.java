package com.example.blogapp.view;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.blogapp.R;
import com.example.blogapp.model.User;
import com.example.blogapp.viewmodel.DBHelper;

public class LoginFragment extends Fragment {

    private DBHelper dbHelper;

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;

    private User userLogin = new User();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        dbHelper = new DBHelper(view.getContext());

        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        btnLogin = view.findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                dbHelper.authentication(email, password, user -> {
                    userLogin = user;
                    if (userLogin != null) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("userLogin", userLogin);
                        Navigation.findNavController(v).navigate(R.id.homeFragment,bundle);
                    }
                    else {
                        Toast.makeText(v.getContext(), "Login fail! Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        return view;
    }
}