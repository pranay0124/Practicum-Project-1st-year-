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

import okhttp3.internal.Util;

public class AddStudents extends AppCompatActivity {

    private RecyclerView recyclerList;
    private DatabaseReference contacts2Reference, usersReference, batchReference, batchDataReference, roomReference;
    FirebaseAuth mAuth;
    String currentUserId, currentBatchName, currentRoomName;
    private String tok;
    private String[] tokens;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_students);

        recyclerList = (RecyclerView) findViewById(R.id.add_students_recyclerList);
        recyclerList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        tok = getIntent().getExtras().get("roomname").toString();
        tokens = tok.split("pk");
        currentBatchName = tokens[0];
        currentRoomName = tokens[1];

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        contacts2Reference = FirebaseDatabase.getInstance().getReference().child("Contacts2");
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        batchReference = FirebaseDatabase.getInstance().getReference().child("Batches");
        roomReference = FirebaseDatabase.getInstance().getReference().child("Rooms");
        batchDataReference = FirebaseDatabase.getInstance().getReference().child("BatchData");
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), RoomChat.class);
        intent.putExtra("roomname", currentBatchName + "pk" + currentRoomName);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<ContactsTwo> options = new FirebaseRecyclerOptions.Builder<ContactsTwo>().setQuery(contacts2Reference.child(currentUserId).child("Student"), ContactsTwo.class).build();
        FirebaseRecyclerAdapter<ContactsTwo, AddStudentsViewHolder> adapter = new FirebaseRecyclerAdapter<ContactsTwo, AddStudentsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final AddStudentsViewHolder holder, int position, @NonNull ContactsTwo model) {
                final String studentId = getRef(position).getKey();
                usersReference.child(studentId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        if(dataSnapshot.child("role").getValue().equals("Student")) {
                            holder.userName.setText(dataSnapshot.child("name").getValue().toString());
                            holder.userPhone.setText(dataSnapshot.child("phone").getValue().toString());
                            holder.userRole.setText(dataSnapshot.child("role").getValue().toString());

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    CharSequence options[] = new CharSequence[]{
                                            "Confirm"
                                    };
                                    AlertDialog.Builder builder = new AlertDialog.Builder(AddStudents.this, R.style.AlertDialog);
                                    builder.setTitle("Add the user !");
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int i) {
                                            if(i == 0) {
                                                batchReference.child(studentId).child(currentBatchName).setValue(currentBatchName).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()) {
                                                            roomReference.child(studentId).child(currentBatchName).child(currentRoomName).child("name").setValue(currentRoomName).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        batchDataReference.child(currentBatchName).child("Students").child(studentId).child("room").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                Toast.makeText(getApplicationContext(), "Student added to db", Toast.LENGTH_SHORT).show();
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
                                    builder.show();
                                }
                            });

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                if(holder.itemView.getVisibility() == View.INVISIBLE) {
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0,0));
                }
            }

            @NonNull
            @Override
            public AddStudentsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_two, viewGroup, false);
                AddStudentsViewHolder viewHolder = new AddStudentsViewHolder(view);
                return viewHolder;
            }
        };

        recyclerList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class AddStudentsViewHolder extends RecyclerView.ViewHolder {

        private TextView userName, userPhone, userRole;

        public AddStudentsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.display2_name);
            userPhone = itemView.findViewById(R.id.display2_phone);
            userRole = itemView.findViewById(R.id.display2_role);
        }
    }
}
