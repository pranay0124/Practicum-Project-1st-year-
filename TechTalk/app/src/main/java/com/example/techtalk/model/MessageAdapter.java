package com.example.techtalk.model;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.techtalk.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Iterator;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference chatDataReference, usersReference;
    private String currentBatchName, currentRoomName;

    public MessageAdapter(List<Messages> userMessagesList, String currentBatchName, String currentRoomName) {
        this.userMessagesList = userMessagesList;
        this.currentBatchName = currentBatchName;
        this.currentRoomName = currentRoomName;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_messages_layout, viewGroup, false);
        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, final int position) {
        final Messages messages = userMessagesList.get(position);
//        String uName = messages.getName();
//        String uMessage = messages.getMessage();


        chatDataReference = FirebaseDatabase.getInstance().getReference().child("ChatData").child(currentBatchName).child(currentRoomName);
        chatDataReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("Message Adapter", "user id" + messages.getUid());
                usersReference = FirebaseDatabase.getInstance().getReference().child("Users").child(messages.getUid());
                usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild("image")) {
                            String uimage = dataSnapshot.child("image").getValue().toString();
                            Picasso.get().load(uimage).placeholder(R.drawable.profile_image).into(messageViewHolder.userImage);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                if(messages.getType().equals("text")) {
                    messageViewHolder.userMessage.setBackgroundResource(R.drawable.their_message);
                    messageViewHolder.userMessage.setText(messages.getMessage());
                    messageViewHolder.userName.setText(messages.getName());
                } else if(messages.getType().equals("image")) {
                    messageViewHolder.sentImage.setVisibility(View.VISIBLE);
                    messageViewHolder.userMessage.setVisibility(View.INVISIBLE);
                    messageViewHolder.sentImage.setBackgroundResource(R.drawable.their_message);
                    messageViewHolder.userName.setText(messages.getName());
                    Picasso.get().load(messages.getMessage()).into(messageViewHolder.sentImage);
                } else {
                    messageViewHolder.sentImage.setVisibility(View.VISIBLE);
                    messageViewHolder.userMessage.setVisibility(View.INVISIBLE);
                    if(messages.getType().equals("pdf")) {
                        messageViewHolder.sentImage.setBackgroundResource(R.drawable.file_pdf);
                    } else {
                        messageViewHolder.sentImage.setBackgroundResource(R.drawable.file_doc);
                    }
                    messageViewHolder.userName.setText(messages.getName());
                    messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                            messageViewHolder.itemView.getContext().startActivity(intent);
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public ImageView userImage, sentImage;
        public TextView userName, userMessage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            userImage = itemView.findViewById(R.id.custom_avatar);
            userName = itemView.findViewById(R.id.custom_name);
            userMessage = itemView.findViewById(R.id.custom_message_body);
            sentImage = itemView.findViewById(R.id.custom_image_view);
        }
    }
}
