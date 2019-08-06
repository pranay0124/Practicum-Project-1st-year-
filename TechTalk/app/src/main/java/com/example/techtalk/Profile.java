package com.example.techtalk;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.techtalk.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {

    private Button updateProfile;
    private TextView userName;
    private EditText userStatus;
    private CircleImageView userImage;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private String uid = "";
    private String uname = "";
    private String upassword = "";
    private String uphone = "";
    private String urole = "";
    private String ustatus = "";
    private String uimage = "";
    private static final int galleryPic = 1;
    private StorageReference userImageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userName = findViewById(R.id.profile_username);
        userStatus = findViewById(R.id.profile_status);
        userImage = findViewById(R.id.profile_image);
        updateProfile = findViewById(R.id.profile_update_button);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        userImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                uid = user.getId();
                uname = user.getName();
                upassword = user.getPassword();
                uphone = user.getPhone();
                urole = user.getRole();
                userName.setText(uname);
                if(ustatus != null) {
                    userStatus.setText(user.getStatus());
                }
                uimage = user.getImage();
                Log.d("Profile Activity", "uimage -> " + uimage);
                Picasso.get().load(uimage).placeholder(R.drawable.profile_image).into(userImage);

//                if (uimage != null) {
//                    uimage = user.getImage();
//                    Picasso.get().load(uimage).into(userImage);
//                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateProfile();
            }
        });
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, galleryPic);
            }
        });
    }

    private void UpdateProfile() {
        String ustatus = userStatus.getText().toString();
        if(TextUtils.isEmpty(ustatus)) {
            Toast.makeText(Profile.this, "Please provide the Institute name", Toast.LENGTH_SHORT).show();
        } else {
            reference.child("status").setValue(ustatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        Toast.makeText(Profile.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Profile.this, MainActivity.class));
                        finish();
                    } else {
                        String message = task.getException().toString();
                        Toast.makeText(Profile.this, "Error " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(Profile.this, MainActivity.class));
        finish();
    }

    //To crop the profile image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == galleryPic && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                StorageReference filePath = userImageRef.child(firebaseUser.getUid() + ".jpg");
                Log.d("Profile Activity", firebaseUser.getUid() + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(Profile.this, "Profile Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                            final String downloadUrl = task.getResult().getDownloadUrl().toString();
                            Log.d("Profile Activity", "download url -> " + downloadUrl);
                            reference.child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        Toast.makeText(Profile.this, "Image saved in database", Toast.LENGTH_SHORT).show();
                                    } else {
                                        String message = task.getException().toString();
                                        Toast.makeText(Profile.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            String message = task.getException().toString();
                            Toast.makeText(Profile.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }
}
