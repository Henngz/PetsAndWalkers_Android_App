package com.example.petsandwalkers;

import androidx.appcompat.app.AppCompatActivity;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_signin;
    private EditText et_username, et_password, et_password_confirm, et_email, et_phone;
    private Button btn_signup;
    private DBOpenHelper DB;

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
    String create_time = dateFormat.format(new Date());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        getSupportActionBar().setTitle("Registration");

        initializeView();
    }

    private void initializeView() {
        tv_signin = findViewById(R.id.tv_signin);
        tv_signin.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        tv_signin.setOnClickListener(this);

        DB = new DBOpenHelper(this);

        et_username = findViewById(R.id.et_username);
        et_password = findViewById(R.id.et_password);
        et_password_confirm = findViewById(R.id.et_password_confirm);
        et_email = findViewById(R.id.et_email);
        et_phone = findViewById(R.id.et_phone);
        btn_signup = findViewById(R.id.btn_reset_password);

        btn_signup.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_signin:
                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                startActivity(intent);
                break;
//                if(getFragmentManager().getBackStackEntryCount() > 0) {
//                    getFragmentManager().popBackStack();
//                }
//                else {
//                    super.onBackPressed();
//                }
//                break;

            // This case is used when user click the "SIGN UP" button.
            case R.id.btn_reset_password:
                String username = et_username.getText().toString();
                String password = et_password.getText().toString();
                String password_confirm = et_password_confirm.getText().toString();
                String email = et_email.getText().toString();
                String phone = et_phone.getText().toString();

                if (TextUtils.isEmpty(username) ||
                        TextUtils.isEmpty(password) ||
                        TextUtils.isEmpty(password_confirm) ||
                        TextUtils.isEmpty(email) ||
                        TextUtils.isEmpty(phone)) {
                    Toast.makeText(this, "All fields required!", Toast.LENGTH_LONG).show();
                } else if (!password.equals(password_confirm)) {
                    Toast.makeText(this, "Passwords are not matching!", Toast.LENGTH_LONG).show();
                } else {
                    Boolean checkUsername = DB.checkUsername(username);
                    if (checkUsername == false) {
                        Boolean insertData = DB.insertData(username, password, email, phone, create_time);

                        if (insertData == true) {
                            boolean insertAccount = DB.insertAccountInfo(username, "", "", "", "", 0.0, 0.0, "", "", "");

                            if (insertAccount) {
                                Toast.makeText(this, "Registered Successfully!", Toast.LENGTH_LONG).show();

                                // Update the username in SharedPreferences
                                SharedPreferences sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("username", username);
                                editor.apply();

                                // Set the result for AccountFragment
                                setResult(Activity.RESULT_OK);

                                // Close the SignUpActivity
                                finish();
                            } else {
                                Toast.makeText(this, "Error creating account info!", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(this, "Registration Failed!", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this, "User already Exists!", Toast.LENGTH_LONG).show();
                    }
                }
                break;

            default:
                break;
        }
    }
}
