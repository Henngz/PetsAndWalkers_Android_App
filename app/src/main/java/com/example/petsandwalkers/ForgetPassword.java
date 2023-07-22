package com.example.petsandwalkers;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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

public class ForgetPassword extends AppCompatActivity implements View.OnClickListener {
    private EditText et_username, et_password, et_password_confirm;
    private Button btn_reset_password;
    private TextView tv_signup;
    private DBOpenHelper DB;

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
    String update_time = dateFormat.format(new Date());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        getSupportActionBar().setTitle("Forget Password");

        DB = new DBOpenHelper(this);

        initializeView();
    }

    private void initializeView() {
        et_username = findViewById(R.id.et_username);
        et_password = findViewById(R.id.et_password);
        et_password_confirm = findViewById(R.id.et_password_confirm);
        btn_reset_password = findViewById(R.id.btn_reset_password);
        btn_reset_password.setOnClickListener(this);

        tv_signup = findViewById(R.id.tv_signup);
        tv_signup.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        tv_signup.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_signup:
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(intent);

                break;

            case R.id.btn_reset_password:
                String username = et_username.getText().toString();
                String password = et_password.getText().toString();
                String password_confirm = et_password_confirm.getText().toString();

                if (TextUtils.isEmpty(username) ||
                        TextUtils.isEmpty(password) ||
                        TextUtils.isEmpty(password_confirm)){
                    Toast.makeText(this, "All fields Required!", Toast.LENGTH_LONG).show();
                }else if (!password.equals(password_confirm)){
                    Toast.makeText(this, "Passwords are not matching!", Toast.LENGTH_LONG).show();
                }else {
                    Boolean checkUsername = DB.checkUsername(username);

                    if(checkUsername == false){
                        Toast.makeText(this, "The account is NOT registered. Please Register!", Toast.LENGTH_LONG).show();
                    }else {
                        Boolean updatePassword = DB.updatePassword(username, password, update_time);

                        if (updatePassword == true){
                            Toast.makeText(this, "Password reset Succeeded!", Toast.LENGTH_LONG).show();
                            intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                        }else {
                            Toast.makeText(this, "Password reset Failed!", Toast.LENGTH_LONG).show();
                        }
                    }
                }
        }
    }
}