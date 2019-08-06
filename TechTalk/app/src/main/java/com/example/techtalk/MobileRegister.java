package com.example.techtalk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class MobileRegister extends AppCompatActivity {

    private EditText userMobileCode;
    private Button verify;
    private String username;
    private String phone;
    private String password;
    private String role;
    private String verificationCode;
    private String currentUserId;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_register);

        userMobileCode = findViewById(R.id.mobileregister_code);
        verify = findViewById(R.id.mobileregister_submit);
        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        String str = intent.getStringExtra("string");
        String[] tokens = str.split("pk");

        username = tokens[0];
        phone = "+91" + tokens[1];
        password = tokens[2];
        role = tokens[3];
        send_sms();

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifySignInCode();
            }
        });
    }

    private void send_sms() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phone,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallBack);        // OnVerificationStateChangedCallbacks
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

        }

        @Override
        public void onVerificationFailed(FirebaseException e) {

        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationCode = s;
        }
    };

    private void verifySignInCode() {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, userMobileCode.getText().toString());
        signInWithPhone(credential);
    }

    public void signInWithPhone(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "User signed in successfully", Toast.LENGTH_SHORT).show();
                    FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = mUser.getUid();
                    saveDetails(uid,username,password,phone,role);
                }
            }
        });
    }

    private void saveDetails(final String uid, final String username, final String password, final String phone, final String role) {
        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();
        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.child("Users").child(uid).exists()) {
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();
                    HashMap<String, Object> hmap = new HashMap<>();
                    hmap.put("id", uid);
                    hmap.put("name", username);
                    hmap.put("phone", phone);
                    hmap.put("password", password);
                    hmap.put("role", role);
                    hmap.put("device_token", deviceToken);

                    RootRef.child("Users").child(uid).updateChildren(hmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(MobileRegister.this, "Account has been created successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(MobileRegister.this, MainActivity.class);
                                startActivity(intent);
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
