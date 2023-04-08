package com.example.blogapp.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.blogapp.R;
import com.example.blogapp.databinding.FragmentPersonalBinding;
import com.example.blogapp.model.User;
import com.example.blogapp.viewmodel.DBHelper;

public class PersonalFragment extends Fragment {
    private User userLogin;
    FragmentPersonalBinding binding;
    DBHelper dbHelper;

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
                Intent intent = new Intent(getActivity(), StatisticsActivity.class);
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

                EditText etCurPw = myView.findViewById(R.id.et_cur_pw);
                EditText etNewPw = myView.findViewById(R.id.et_new_pw);
                EditText etConfirmPw = myView.findViewById(R.id.et_confirm_pw);
                Button butChangePass = myView.findViewById(R.id.but_change_pass);

                butChangePass.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (etCurPw.getText().toString().equals(etNewPw.getText().toString())) {
                            Toast.makeText(getActivity(),
                                    "New password is the same as your current password",
                                    Toast.LENGTH_LONG).show();
                            etCurPw.setText("");
                            etNewPw.setText("");
                            etConfirmPw.setText("");
                            etCurPw.requestFocus();
                        } else if (!etConfirmPw.getText().toString().equals(etNewPw.getText().toString())) {
                            Toast.makeText(getActivity(),
                                    "Your confirm password is not correct",
                                    Toast.LENGTH_LONG).show();
                            etCurPw.setText("");
                            etNewPw.setText("");
                            etConfirmPw.setText("");
                            etCurPw.requestFocus();
                        } else {
                            String newPw = etNewPw.getText().toString();

                            dbHelper.ChangePassword(userLogin, newPw);
                        }
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
                Navigation.findNavController(v).navigate(R.id.homeFragment, bundle);
            }
        });
        return view;
    }
}