package com.example.techtalk;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindPeopleProfile extends AppCompatActivity {

    private CircleImageView userImage;
    private TextView userName, userStatus, userRole;
    private Button userRequestButton, cancelRequestButton;
    private String senderUserId, receiverUserId, current_state;
    private DatabaseReference reference, chatreference, contactreference, notificationReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_people_profile);

        mAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        chatreference = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactreference = FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationReference = FirebaseDatabase.getInstance().getReference().child("Notifications");
        receiverUserId = getIntent().getExtras().get("receiverUserId").toString();
        senderUserId = mAuth.getCurrentUser().getUid();

        userImage = findViewById(R.id.find_profile_image);
        userName = findViewById(R.id.find_profile_username);
        userStatus = findViewById(R.id.find_profile_status);
        userRole = findViewById(R.id.find_profile_role);
        userRequestButton = findViewById(R.id.find_profile_request_button);
        cancelRequestButton = findViewById(R.id.find_profile_cancelrequest_button);
        current_state = "new";

        getUserInfo();

    }

    void getUserInfo() {
        reference.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if((dataSnapshot.exists()) && (dataSnapshot.hasChild("image"))) {
                    String uimage = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(uimage).placeholder(R.drawable.profile_image).into(userImage);
                    userName.setText(dataSnapshot.child("name").getValue().toString());
                    userStatus.setText(dataSnapshot.child("status").getValue().toString());
                    userRole.setText(dataSnapshot.child("role").getValue().toString());
                    ManageChatRequests();
                } else {
                    userName.setText(dataSnapshot.child("name").getValue().toString());
                    userRole.setText(dataSnapshot.child("role").getValue().toString());
                    ManageChatRequests();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    void ManageChatRequests() {
        chatreference.child(senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(receiverUserId)) {
                    String request_type = dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();
                    if(request_type.equals("sent")) {
                        current_state = "request_sent";
                        userRequestButton.setText(getString(R.string.cancel));
                    } else if(request_type.equals("received")) {
                        current_state = "request_received";
                        userRequestButton.setText("Accept Request");

                        cancelRequestButton.setVisibility(View.VISIBLE);
                        cancelRequestButton.setEnabled(true);
                        cancelRequestButton.setText("Decline Request");
                        cancelRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelChatRequest();
                            }
                        });
                    }
                } else {
                    contactreference.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(receiverUserId)) {
                                current_state = "friends";
                                userRequestButton.setText("Remove Contact");

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(!senderUserId.equals(receiverUserId)) {
            userRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    userRequestButton.setEnabled(false);
                    if(current_state.equals("new")) {
                        SendChatRequest();
                    }
                    if(current_state.equals("request_sent")) {
                        CancelChatRequest();
                    }
                    if(current_state.equals("request_received")) {
                        AcceptChatRequest();
                    }
                    if(current_state.equals("friends")) {
                        RemoveContact();
                    }
                }
            });
        } else {
            userRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    void SendChatRequest() {
        chatreference.child(senderUserId).child(receiverUserId).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    chatreference.child(receiverUserId).child(senderUserId).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                HashMap<String, String> chatNotificationMap = new HashMap<>();
                                chatNotificationMap.put("from", senderUserId);
                                chatNotificationMap.put("type", "request");
                                notificationReference.child(receiverUserId).push().setValue(chatNotificationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            userRequestButton.setEnabled(true);
                                            current_state = "request_sent";
                                            userRequestButton.setText(getString(R.string.cancel));
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    void CancelChatRequest() {
        chatreference.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                chatreference.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            userRequestButton.setEnabled(true);
                            current_state = "new";
                            userRequestButton.setText(getString(R.string.request));

                            cancelRequestButton.setVisibility(View.INVISIBLE);
                            cancelRequestButton.setEnabled(false);
                        }
                    }
                });
            }
        });
    }

    void AcceptChatRequest() {
        contactreference.child(senderUserId).child(receiverUserId).child("contact").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    contactreference.child(receiverUserId).child(senderUserId).child("contact").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                chatreference.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            chatreference.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    userRequestButton.setEnabled(true);
                                                    current_state = "friends";
                                                    userRequestButton.setText("Remove Contact");

                                                    cancelRequestButton.setVisibility(View.INVISIBLE);
                                                    cancelRequestButton.setEnabled(false);
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    void RemoveContact() {
        contactreference.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                contactreference.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            userRequestButton.setEnabled(true);
                            current_state = "new";
                            userRequestButton.setText(getString(R.string.request));

                            cancelRequestButton.setVisibility(View.INVISIBLE);
                            cancelRequestButton.setEnabled(false);
                        }
                    }
                });
            }
        });
    }
}
