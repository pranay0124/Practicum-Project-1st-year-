package com.example.techtalk;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.techtalk.model.ContactsTwo;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ShowMentors extends AppCompatActivity {

    private RecyclerView recyclerList;
    private DatabaseReference usersReference, batchDataReference;
    FirebaseAuth mAuth;
    String currentUserId, currentGroupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_mentors);

        recyclerList = (RecyclerView) findViewById(R.id.show_mentors_recyclerList);
        recyclerList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        currentGroupName = getIntent().getExtras().get("groupname").toString();

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        batchDataReference = FirebaseDatabase.getInstance().getReference().child("BatchData").child(currentGroupName).child("Mentors");
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

        FirebaseRecyclerOptions<ContactsTwo> options = new FirebaseRecyclerOptions.Builder<ContactsTwo>().setQuery(batchDataReference, ContactsTwo.class).build();
        FirebaseRecyclerAdapter<ContactsTwo, AddedMentorViewHolder> adapter = new FirebaseRecyclerAdapter<ContactsTwo, AddedMentorViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final AddedMentorViewHolder holder, int position, @NonNull ContactsTwo model) {
                String mentorId = getRef(position).getKey();
                usersReference.child(mentorId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        holder.userName.setText(dataSnapshot.child("name").getValue().toString());
                        holder.userPhone.setText(dataSnapshot.child("phone").getValue().toString());
                        holder.userRole.setText(dataSnapshot.child("role").getValue().toString());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public AddedMentorViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_two, viewGroup, false);
                AddedMentorViewHolder viewHolder = new AddedMentorViewHolder(view);
                return viewHolder;
            }
        };
        recyclerList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class AddedMentorViewHolder extends RecyclerView.ViewHolder {

        private TextView userName, userPhone, userRole;

        public AddedMentorViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.display2_name);
            userPhone = itemView.findViewById(R.id.display2_phone);
            userRole = itemView.findViewById(R.id.display2_role);
        }
    }
}
