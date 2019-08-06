package com.example.techtalk;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.techtalk.model.Group;
import com.example.techtalk.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BatchChat extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private String currentBatchName, currentUserId;
    String userRole, srMentor;

    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> arrayList = new ArrayList<>();

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batch_chat);

        currentBatchName = getIntent().getExtras().get("groupname").toString();
        getSupportActionBar().setTitle(currentBatchName);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference();

        reference.child("Users").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                userRole = user.getRole();
                invalidateOptionsMenu();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
//        InitializeFields();
//        RetrieveAndDisplayRooms();
        recyclerView = findViewById(R.id.batch_activity_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu2, menu);
        if(userRole != null) {
            if(userRole.equals("SrMentor")) {
                menu.findItem(R.id.add_mentors).setVisible(true);
                menu.findItem(R.id.list_mentors).setVisible(true);
            } else if(userRole.equals("Mentor")) {
                menu.findItem(R.id.create_room).setVisible(true);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.add_mentors:
                Intent showMentorsIntent = new Intent(BatchChat.this, AddMentors.class);
                showMentorsIntent.putExtra("groupname", currentBatchName);
                startActivity(showMentorsIntent);
                finish();
                return true;

            case R.id.list_mentors:
                Intent listMentorsIntent = new Intent(BatchChat.this, ShowMentors.class);
                listMentorsIntent.putExtra("groupname", currentBatchName);
                startActivity(listMentorsIntent);
                finish();
                return true;

            case R.id.create_room:
                AlertDialog.Builder builder = new AlertDialog.Builder(BatchChat.this, R.style.AlertDialog);
                builder.setTitle("Enter Room Name:");
                final EditText batchName = new EditText(BatchChat.this);
                batchName.setHint("Subject");
                builder.setView(batchName);

                builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String room = batchName.getText().toString();
                        if(TextUtils.isEmpty(room)){
                            Toast.makeText(BatchChat.this, "Please provide the room name", Toast.LENGTH_SHORT).show();
                        } else{
                            CreateNewRoom(room);
                        }
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();

                    }
                });

                builder.show();
                return true;
        }
        return false;
    }

    private void CreateNewRoom(final String room) {
        reference.child("Rooms").child(currentUserId).child(currentBatchName).child(room).child("name").setValue(room).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    reference.child("BatchData").child(currentBatchName).child("Rooms").child(room).child(currentUserId).child(room).setValue(room).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(BatchChat.this, room + " room is created", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        reference.child("BatchData").child(currentBatchName).child("SrMentors").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    srMentor = ds.getKey();
                    break;
                }
                reference.child("Rooms").child(srMentor).child(currentBatchName).child(room).child("name").setValue(room).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(BatchChat.this, room + " room added in db", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

//    void InitializeFields() {
//        listView = (ListView) findViewById(R.id.list_view_2);
//        Log.d("Batch Chat", "Initialize Fields ArrayList ------> " + arrayList);
//        arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, arrayList);
//        Log.d("Batch Chat", "Initialize Fields ArrayList ------> " + arrayList);
//        listView.setAdapter(arrayAdapter);
//    }
//
//    void RetrieveAndDisplayRooms() {
//        reference.child("Rooms").child(currentUserId).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Set<String> set = new HashSet<>();
//                Iterator iterator = dataSnapshot.getChildren().iterator();
//                while(iterator.hasNext()) {
//                    set.add(((DataSnapshot)iterator.next()).getKey());
//                }
//
//                arrayList.clear();
//                arrayList.addAll(set);
//                Log.d("Batch Chat", "Retrive and display ArrayList ------> " + arrayList);
//                arrayAdapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Group> options = new FirebaseRecyclerOptions.Builder<Group>().setQuery(reference.child("Rooms").child(currentUserId).child(currentBatchName), Group.class).build();
        FirebaseRecyclerAdapter<Group, RoomNameViewHolder> adapter = new FirebaseRecyclerAdapter<Group, RoomNameViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RoomNameViewHolder holder, int position, @NonNull Group model) {
                final String currentRoomName = model.getName();
                holder.name.setText(currentRoomName);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), RoomChat.class);
                        intent.putExtra("roomname", currentBatchName + "pk" + currentRoomName);
                        startActivity(intent);
                        finish();
                    }
                });
            }

            @NonNull
            @Override
            public RoomNameViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.simple_text_view, viewGroup, false);
                RoomNameViewHolder viewHolder = new RoomNameViewHolder(view);
                return viewHolder;
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RoomNameViewHolder extends RecyclerView.ViewHolder {

        TextView name;

        public RoomNameViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.simple_text_textview);
        }
    }
}
