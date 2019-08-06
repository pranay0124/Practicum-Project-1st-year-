package com.example.techtalk;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindPeople extends AppCompatActivity {

    private RecyclerView recyclerList;
    private DatabaseReference reference, chatreference;
    private FirebaseAuth mAuth;
    private String senderUserId, receiverUserId, current_state;


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(reference, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, FindPeopleViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, FindPeopleViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FindPeopleViewHolder holder,final int position, @NonNull Contacts model) {
                if(model.getImage() != null && model.getStatus() != null) {
                    holder.userName.setText(model.getName());
                    holder.userStatus.setText(model.getStatus());
                    holder.userRole.setText(model.getRole());
                    Picasso.get().load(model.getImage()).into(holder.userImage);
                } else {
                    holder.userName.setText(model.getName());
                    holder.userRole.setText(model.getRole());
                    holder.userStatus.setText("no status");
                }
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        receiverUserId = getRef(position).getKey();
                        Log.d("Find People Activity", "receiverUserId -> " + receiverUserId);
                        Intent profileintent = new Intent(FindPeople.this, FindPeopleProfile.class);
                        profileintent.putExtra("receiverUserId", receiverUserId);
                        startActivity(profileintent);
                    }
                });
            }

            @NonNull
            @Override
            public FindPeopleViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_diaplay, viewGroup, false);
                FindPeopleViewHolder viewHolder = new FindPeopleViewHolder(view);
                return viewHolder;
            }
        };

        recyclerList.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_people);

        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        chatreference = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        recyclerList = findViewById(R.id.find_people_recyclerList);
        recyclerList.setLayoutManager(new LinearLayoutManager(this));
        mAuth = FirebaseAuth.getInstance();
        senderUserId = mAuth.getCurrentUser().getUid();

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(FindPeople.this, MainActivity.class));
        finish();
    }


    public class FindPeopleViewHolder extends RecyclerView.ViewHolder {

        private TextView userName, userStatus, userRole;
        private CircleImageView userImage;
        private Button userRequest;

        public FindPeopleViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            userRole = itemView.findViewById(R.id.user_role);
            userImage = itemView.findViewById(R.id.users_profile_image);
        }
    }
}
