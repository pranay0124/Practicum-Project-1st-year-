package com.example.techtalk;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.techtalk.model.Group;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private String currentUserId;
    private View groupFragmentView;
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> arrayList = new ArrayList<>();

    private DatabaseReference reference, currentReference;
    private RecyclerView recyclerView;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        groupFragmentView = inflater.inflate(R.layout.fragment_chats, container, false);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("Batches").child(currentUserId);
        InitializeFields();

        RetrieveAndDisplayBatches();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String currentGroupName = adapterView.getItemAtPosition(position).toString();
                Intent intent = new Intent(getContext(), BatchChat.class);
                intent.putExtra("groupname", currentGroupName);
                startActivity(intent);
            }
        });
        return groupFragmentView;
    }

    private void InitializeFields() {
        listView = (ListView) groupFragmentView.findViewById(R.id.list_view);
        arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(arrayAdapter);
    }

    private void RetrieveAndDisplayBatches() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Set<String> set = new HashSet<>();
                Iterator iterator = dataSnapshot.getChildren().iterator();
                while(iterator.hasNext()) {
                    set.add(((DataSnapshot)iterator.next()).getKey());
                }

                arrayList.clear();
                arrayList.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }





//    void Init() {
//        recyclerView = groupFragmentView.findViewById(R.id.chat_fragment_recyclerview);
//        recyclerView.setLayoutManager(new LinearLayoutManager(groupFragmentView.getContext()));
//    }
//
//
//
//    @Override
//    public void onStart() {
//        super.onStart();
//        FirebaseRecyclerOptions<Group> options = new FirebaseRecyclerOptions.Builder<Group>().setQuery(reference.child("Batches").child(currentUserId), Group.class).build();
//        FirebaseRecyclerAdapter<Group, BatchNameViewHolder> adapter = new FirebaseRecyclerAdapter<Group, BatchNameViewHolder>(options) {
//            @Override
//            protected void onBindViewHolder(@NonNull BatchNameViewHolder holder, int position, @NonNull Group model) {
//                holder.name.setText(model.getName());
//
//            }
//
//            @NonNull
//            @Override
//            public BatchNameViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
//                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.simple_text_view, viewGroup, false);
//                BatchNameViewHolder viewHolder = new BatchNameViewHolder(view);
//                return viewHolder;
//            }
//        };
//        recyclerView.setAdapter(adapter);
//        adapter.startListening();
//    }
//
//    public static class BatchNameViewHolder extends RecyclerView.ViewHolder {
//
//        TextView name;
//
//        public BatchNameViewHolder(@NonNull View itemView) {
//            super(itemView);
//
//            name = itemView.findViewById(R.id.simple_text_textview);
//        }
//    }

}
