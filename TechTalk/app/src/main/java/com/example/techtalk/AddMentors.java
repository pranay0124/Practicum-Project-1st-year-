package com.example.techtalk;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.techtalk.model.ContactsTwo;
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

public class AddMentors extends AppCompatActivity {

    private RecyclerView recyclerList;
    private DatabaseReference contacts2Reference, usersReference, batchReference, batchDataReference;
    FirebaseAuth mAuth;
    String currentUserId, currentGroupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_mentors);

        recyclerList = (RecyclerView) findViewById(R.id.add_mentors_recyclerList);
        recyclerList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        currentGroupName = getIntent().getExtras().get("groupname").toString();

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        contacts2Reference = FirebaseDatabase.getInstance().getReference().child("Contacts2");
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        batchReference = FirebaseDatabase.getInstance().getReference().child("Batches");
        batchDataReference = FirebaseDatabase.getInstance().getReference().child("BatchData");

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), BatchChat.class);
        intent.putExtra("groupname", currentGroupName);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<ContactsTwo> options = new FirebaseRecyclerOptions.Builder<ContactsTwo>().setQuery(contacts2Reference.child(currentUserId).child("Mentor"), ContactsTwo.class).build();
        FirebaseRecyclerAdapter<ContactsTwo, MentorViewHolder> adapter = new FirebaseRecyclerAdapter<ContactsTwo, MentorViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final MentorViewHolder holder, int position, @NonNull ContactsTwo model) {
                final String mentorId = getRef(position).getKey();
                Log.d("Show Mentors", "On Bind view holder -> position : " + position);
                usersReference.child(mentorId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        if(dataSnapshot.child("role").getValue().equals("Mentor")) {
                            holder.userName.setText(dataSnapshot.child("name").getValue().toString());
                            holder.userPhone.setText(dataSnapshot.child("phone").getValue().toString());
                            holder.userRole.setText(dataSnapshot.child("role").getValue().toString());
//                        }

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d("Show Mentors", "holder clicked");
                                CharSequence options[] = new CharSequence[]{
                                        "Confirm"
                                };
                                AlertDialog.Builder builder = new AlertDialog.Builder(AddMentors.this, R.style.AlertDialog);
                                builder.setTitle("Add the user?");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int i) {
                                        if(i == 0) {
                                            batchReference.child(mentorId).child(currentGroupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()) {
                                                        batchDataReference.child(currentGroupName).child("Mentors").child(mentorId).child("batch").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                Toast.makeText(getApplicationContext(), "User Added in the batch", Toast.LENGTH_SHORT).show();
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

            @NonNull
            @Override
            public MentorViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_two, viewGroup, false);
                MentorViewHolder viewHolder = new MentorViewHolder(view);
                return viewHolder;
            }
        };

        recyclerList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class MentorViewHolder extends RecyclerView.ViewHolder {

        private TextView userName, userPhone, userRole;

        public MentorViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.display2_name);
            userPhone = itemView.findViewById(R.id.display2_phone);
            userRole = itemView.findViewById(R.id.display2_role);
        }
    }
}
