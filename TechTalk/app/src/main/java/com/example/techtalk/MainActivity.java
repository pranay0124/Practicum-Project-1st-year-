package com.example.techtalk;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.techtalk.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private String currentUserId;
    private TextView username;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference, ref;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private TabsAccessorAdapter tabsAccessorAdapter;

    String role = "";
    String name = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        username = findViewById(R.id.main_username);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();
        ref = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid());

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getName());
                name = user.getName();
                role = user.getRole();
                Log.d("Main Activity", "User ---> " + user.getName());
                Log.d("Main Activity", "Role ---> " + user.getRole());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        viewPager = (ViewPager) findViewById(R.id.main_tabs_pager);
        tabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabsAccessorAdapter);
        tabLayout = (TabLayout) findViewById(R.id.main_tabs);
        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(MainActivity.this, MainActivity.class));
        finish();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        Log.d("Main Activity", "Role ---> " + role);
        if(role.equals("SrMentor")) {
            menu.findItem(R.id.profile).setVisible(true);
            menu.findItem(R.id.findtechies).setVisible(true);
            menu.findItem(R.id.create_batch).setVisible(true);
            menu.findItem(R.id.logout).setVisible(true);
        } else if(role.equals("Mentor")) {
            menu.findItem(R.id.profile).setVisible(true);
            menu.findItem(R.id.findtechies).setVisible(true);
            menu.findItem(R.id.create_batch).setVisible(false);
            menu.findItem(R.id.logout).setVisible(true);
        } else {
            menu.findItem(R.id.profile).setVisible(true);
            menu.findItem(R.id.findtechies).setVisible(true);
            menu.findItem(R.id.create_batch).setVisible(false);
            menu.findItem(R.id.logout).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout :
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, StartActivity.class));
                finish();
                return true;

            case R.id.profile :
                Intent intent = new Intent(MainActivity.this, Profile.class);
                startActivity(intent);
                finish();
                return true;

            case R.id.findtechies :
                Intent techieintent = new Intent(MainActivity.this, FindPeople.class);
                startActivity(techieintent);
                finish();
                return true;

            case R.id.create_batch :
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
                builder.setTitle("Enter Batch Name:");
                final EditText batchName = new EditText(MainActivity.this);
                batchName.setHint("eg: 2019 - 2020");
                builder.setView(batchName);

                builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String batch = batchName.getText().toString();
                        if(TextUtils.isEmpty(batch)){
                            Toast.makeText(MainActivity.this, "Please provide the batch name", Toast.LENGTH_SHORT).show();
                        } else{
                            CreateNewBatch(batch);
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

    private void CreateNewBatch(final String batch) {
        reference.child("Batches").child(currentUserId).child(batch).setValue(batch).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    reference.child("BatchData").child(batch).child("SrMentors").child(currentUserId).child(batch).setValue(batch).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(MainActivity.this, batch+" batch is created", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

}
