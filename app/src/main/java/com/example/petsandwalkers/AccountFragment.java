package com.example.petsandwalkers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class AccountFragment extends Fragment implements View.OnClickListener {

    private TextView tv_signup, tv_forget_password;
    private Button btn_login;
    private EditText et_username, et_password;
    private DBOpenHelper DB;
    private ActivityResultLauncher<Intent> signUpActivityResultLauncher;

    public static AccountFragment newInstance() {
        return new AccountFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        tv_signup = view.findViewById(R.id.tv_signup);
        tv_signup.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        tv_signup.setOnClickListener(this);

        DB = new DBOpenHelper(getActivity());

        tv_forget_password = view.findViewById(R.id.tv_forget_password);
        tv_forget_password.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        tv_forget_password.setOnClickListener(this);

        btn_login = view.findViewById(R.id.btn_login);
        et_username = view.findViewById(R.id.et_username);
        et_password = view.findViewById(R.id.et_password);

        btn_login.setOnClickListener(this);

        signUpActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        NavController navController = Navigation.findNavController(view);
                        navController.navigate(R.id.myAccountFragment);
                    }
                }
        );

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_signup:
                Intent intent = new Intent(getActivity(), SignUpActivity.class);
                signUpActivityResultLauncher.launch(intent);
                break;

            case R.id.btn_login:
                String username = et_username.getText().toString();
                String password = et_password.getText().toString();

                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                    Toast.makeText(this.getContext(), "All fields Required!", Toast.LENGTH_LONG).show();
                } else {
                    Boolean checkUsernamePassword = DB.checkUsernamePassword(username, password);

                    if (checkUsernamePassword == true && checkUsernamePassword != null) {
                        Toast.makeText(this.getContext(), "Login Successfully!", Toast.LENGTH_LONG).show();

                        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("username", username);
                        editor.apply();

                        NavController navController = Navigation.findNavController(view);
                        navController.navigate(R.id.myAccountFragment);
                    } else {
                        Toast.makeText(this.getContext(), "Login Failed!", Toast.LENGTH_LONG).show();
                    }
                }
                break;

            case R.id.tv_forget_password:
                intent = new Intent(getActivity(), ForgetPassword.class);
                startActivity(intent);
                break;

            default:
                break;
        }
    }
}
