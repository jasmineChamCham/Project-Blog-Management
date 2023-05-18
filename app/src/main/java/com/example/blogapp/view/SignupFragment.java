package com.example.blogapp.view;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.blogapp.R;
import com.example.blogapp.model.User;
import com.example.blogapp.viewmodel.DBHelper;

public class SignupFragment extends Fragment {

    private DBHelper dbHelper;
    private EditText etFullname;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPw;
    private Button btnSignup;
    private TextView tvLogin;
    private User userLogin;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        dbHelper = new DBHelper(view.getContext());

        etFullname = view.findViewById(R.id.et_full_name);
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        etConfirmPw = view.findViewById(R.id.et_confirm_pw);
        btnSignup = view.findViewById(R.id.btn_sign_up);
        tvLogin = view.findViewById(R.id.tv_log_in);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullname = etFullname.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String confirmPw = etConfirmPw.getText().toString().trim();
                if (!password.equals("") && !confirmPw.equals("") && !password.equals(confirmPw)) {
                    Toast.makeText(getContext(), "Confirm password is not match the password.", Toast.LENGTH_LONG).show();
                }
                else if (fullname.equals("") || email.equals("") || password.equals("") || confirmPw.equals("")) {
                    Toast.makeText(getContext(), "Please enter all required information.", Toast.LENGTH_LONG).show();
                }
                else {
                    dbHelper.isExistAccount(email, isExist -> {
                        Log.d("DEBUG", "main_isExistAccount " + String.valueOf(isExist));
                        if (isExist) {
                            Toast.makeText(getContext(), "You already have an account.", Toast.LENGTH_LONG).show();
                        }
                        else {
                            dbHelper.addUser(fullname, email, confirmPw, "01/01/1997");
                            dbHelper.authentication(email, confirmPw, user -> {
                                userLogin = user;
                                if (userLogin != null) {
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("userLogin", userLogin);
                                    Navigation.findNavController(v).navigate(R.id.homeFragment, bundle);
                                }
                                else {
                                    Toast.makeText(getContext(), "Login fail! Your email or password is not correct.", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });
                }
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                Navigation.findNavController(view).navigate(R.id.loginFragment, bundle);
            }
        });

        return view;
    }
}