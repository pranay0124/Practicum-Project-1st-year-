package com.example.techtalk;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.techtalk.model.ContactsTwo;
import com.example.techtalk.model.Messages;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;

public class DisplayData extends AppCompatActivity {

    private String tok;
    private String[] tokens;
    private String currentBatchName, currentRoomName, toDisplay;
    private RecyclerView recyclerList;
    private DatabaseReference chatDataReference;
    private List<Messages> userMessagesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_data);

        tok = getIntent().getExtras().get("roomname").toString();
        tokens = tok.split("pk");
        currentBatchName = tokens[0];
        currentRoomName = tokens[1];
        toDisplay = tokens[2];


        recyclerList = (RecyclerView) findViewById(R.id.display_data_recyclerList);
        recyclerList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        chatDataReference = FirebaseDatabase.getInstance().getReference().child("ChatData").child(currentBatchName).child(currentRoomName);
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
        FirebaseRecyclerOptions<Messages> options = new FirebaseRecyclerOptions.Builder<Messages>().setQuery(chatDataReference, Messages.class).build();
        FirebaseRecyclerAdapter<Messages, DisplayDataViewHolder> adapter = new FirebaseRecyclerAdapter<Messages, DisplayDataViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull DisplayDataViewHolder holder, int position, @NonNull Messages model) { ;
                if(model.getType().equals(toDisplay)) {
                    if(model.getType().equals("pdf")) {
                        Picasso.get().load(R.drawable.file_pdf).into(holder.fileImage);
                    } else if (model.getType().equals("docx")) {
                        Picasso.get().load(R.drawable.file_doc).into(holder.fileImage);
                    } else {
                        Picasso.get().load(model.getMessage()).into(holder.fileImage);
                    }
                    holder.fileName.setText(model.getIname());
                    holder.fileSenderName.setText("by " + model.getName() + "  |  on : " + model.getDate() + "  |  at : " + model.getTime());
                } else {
//                    holder.itemView.setVisibility(View.VISIBLE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0,0));
                }
            }

            @NonNull
            @Override
            public DisplayDataViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_file_layout, viewGroup, false);
                DisplayDataViewHolder viewHolder = new DisplayDataViewHolder(view);
                return viewHolder;
            }


        };
        recyclerList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class DisplayDataViewHolder extends RecyclerView.ViewHolder{

        ImageView fileImage;
        TextView fileName, fileSenderName;

        public DisplayDataViewHolder(@NonNull View itemView) {
            super(itemView);

            fileImage = itemView.findViewById(R.id.custom_file_image);
            fileName = itemView.findViewById(R.id.custom_file_filename);
            fileSenderName = itemView.findViewById(R.id.custom_file_uname);
        }
    }
}
