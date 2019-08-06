package com.example.techtalk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;


public class RegisterActivity extends AppCompatActivity {

    private EditText registerUserName, registerPhone, registerPassword;
    private Button registerSubmit;
    private RadioGroup registerRadioGroup;
    private RadioButton registerRadioButton;
    private RadioButton registerSrMentor, registerMentor, registerStudent;
    private String currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference usersReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerUserName = findViewById(R.id.register_username);
        registerPhone = findViewById(R.id.register_phone);
        registerPassword = findViewById(R.id.register_password);
        registerSubmit = findViewById(R.id.register_submit_button);
        registerRadioGroup = findViewById(R.id.register_radioGroup);
        mAuth = FirebaseAuth.getInstance();
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");

        registerSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectID = registerRadioGroup.getCheckedRadioButtonId();
                registerRadioButton = findViewById(selectID);

                if(TextUtils.isEmpty(registerUserName.getText().toString())) {
                    registerUserName.setHintTextColor(getResources().getColor(R.color.red));
                    registerUserName.setError("UserName is required!");
                } else if(TextUtils.isEmpty(registerPhone.getText().toString())) {
                    registerPhone.setHintTextColor(getResources().getColor(R.color.red));
                    registerPhone.setError("Phone number is required!");
                } else if(!Pattern.matches("^[7-9][0-9]{9}$", registerPhone.getText().toString())) {
                    registerPhone.setError("Phone number is not valid");
                } else if(TextUtils.isEmpty(registerPassword.getText().toString())) {
                    registerPassword.setHintTextColor(getResources().getColor(R.color.red));
                    registerPassword.setError("Password is required!");
                } else {
                    String str = registerUserName.getText().toString() + "pk" + registerPhone.getText().toString() + "pk" + registerPassword.getText().toString() + "pk" + registerRadioButton.getText();
                    Intent intent = new Intent(RegisterActivity.this, MobileRegister.class);
                    intent.putExtra("string", str);
                    startActivity(intent);
                }
            }
        });

    }



}
