package com.example.techtalk;


import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AlertDialogLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.techtalk.model.Contacts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestsFragment extends Fragment {

    private View requestsView;
    private RecyclerView requestsList;
    private DatabaseReference requestsReference, usersReference, contactsReference, contacts2Reference;
    private FirebaseAuth mAuth;
    private String currentUserId, currentUserRole;


    public RequestsFragment() { }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        requestsView =  inflater.inflate(R.layout.fragment_requests, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        requestsReference = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        contactsReference = FirebaseDatabase.getInstance().getReference().child("Contacts");
        contacts2Reference = FirebaseDatabase.getInstance().getReference().child("Contacts2");

        requestsList = requestsView.findViewById(R.id.chat_requests_list);
        requestsList.setLayoutManager(new LinearLayoutManager(getContext()));

        usersReference.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUserRole = dataSnapshot.child("role").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return requestsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(requestsReference.child(currentUserId), Contacts.class).build();
        FirebaseRecyclerAdapter<Contacts, RequestsViewholder> adapter = new FirebaseRecyclerAdapter<Contacts, RequestsViewholder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestsViewholder holder, int position, @NonNull Contacts model) {
//                holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
//                holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);

                final String listUserId = getRef(position).getKey();
                DatabaseReference getTypeReference = getRef(position).child("request_type").getRef();
                getTypeReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            String type = dataSnapshot.getValue().toString();
                            if(type.equals("received")) {
                                usersReference.child(listUserId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild("image")) {
                                            String profileImage = dataSnapshot.child("image").getValue().toString();
                                            Picasso.get().load(profileImage).placeholder(R.drawable.profile_image).into(holder.userImage);
                                        }
                                        final String profileName = dataSnapshot.child("name").getValue().toString();
                                        final String profileRole = dataSnapshot.child("role").getValue().toString();
                                        final String profileStatus = dataSnapshot.child("status").getValue().toString();


                                        holder.userName.setText(profileName);
                                        holder.userRole.setText(profileRole);
                                        holder.userStatus.setText(profileStatus);

                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CharSequence options[] = new CharSequence[]{
                                                        "Accept", "Decline"
                                                };
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle("Confirm Request with " + profileName + " !");

                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int i) {
                                                        if(i == 0) {
                                                            contactsReference.child(currentUserId).child(listUserId).child("contact").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()) {
                                                                        contactsReference.child(listUserId).child(currentUserId).child("contact").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if(task.isSuccessful()) {
                                                                                    requestsReference.child(currentUserId).child(listUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if(task.isSuccessful()) {
                                                                                                requestsReference.child(listUserId).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        if(task.isSuccessful()) {
                                                                                                                        contacts2Reference.child(currentUserId).child(profileRole).child(listUserId).child("contact").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                            @Override
                                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                if(task.isSuccessful()) {
                                                                                                                                    contacts2Reference.child(listUserId).child(currentUserRole).child(currentUserId).child("contact").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                        @Override
                                                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                            if(task.isSuccessful()) {
                                                                                                                                                Toast.makeText(getContext(), "Contact Saved", Toast.LENGTH_SHORT).show();
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
                                                                                        }
                                                                                    });
                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });
                                                        }
                                                        if(i == 1) {
                                                            requestsReference.child(currentUserId).child(listUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()) {
                                                                        requestsReference.child(listUserId).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if(task.isSuccessful()) {
                                                                                    Toast.makeText(getContext(), "Contact Deleted", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                                builder.show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                            /*=================================================================================================================================*/
                            else if(type.equals("sent")) {
                                Button request_sent_btn = holder.itemView.findViewById(R.id.request_accept_btn);
                                request_sent_btn.setText("Request Sent");
                                holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.INVISIBLE);

                                usersReference.child(listUserId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild("image")) {
                                            String profileImage = dataSnapshot.child("image").getValue().toString();
                                            Picasso.get().load(profileImage).placeholder(R.drawable.profile_image).into(holder.userImage);
                                        }
                                        final String profileName = dataSnapshot.child("name").getValue().toString();
                                        String profileRole = dataSnapshot.child("role").getValue().toString();
                                        String profileStatus = dataSnapshot.child("status").getValue().toString();

                                        holder.userName.setText(profileName);
                                        holder.userRole.setText(profileRole);
                                        holder.userStatus.setText(profileStatus);

                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CharSequence options[] = new CharSequence[]{
                                                        "Cancel Request"
                                                };
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle("Already sent request");

                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int i) {
                                                        if(i == 0) {
                                                            requestsReference.child(currentUserId).child(listUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()) {
                                                                        requestsReference.child(listUserId).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if(task.isSuccessful()) {
                                                                                    Toast.makeText(getContext(), "You have cancelled the request", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                                builder.show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public RequestsViewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_diaplay, viewGroup, false);
                RequestsViewholder holder = new RequestsViewholder(view);
                return holder;
            }
        };

        requestsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestsViewholder extends RecyclerView.ViewHolder {

        private TextView userName, userStatus, userRole;
        private CircleImageView userImage;
        private Button userAccept, userCancel;

        public RequestsViewholder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            userRole = itemView.findViewById(R.id.user_role);
            userImage = itemView.findViewById(R.id.users_profile_image);
            userAccept = itemView.findViewById(R.id.request_accept_btn);
            userCancel = itemView.findViewById(R.id.request_cancel_btn);

            userAccept.setVisibility(View.VISIBLE);
            userCancel.setVisibility(View.VISIBLE);
        }

    }
}
