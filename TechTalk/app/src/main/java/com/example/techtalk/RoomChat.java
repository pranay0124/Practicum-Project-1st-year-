package com.example.techtalk;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.techtalk.model.MessageAdapter;
import com.example.techtalk.model.Messages;
import com.example.techtalk.model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class RoomChat extends AppCompatActivity {

    private String tok;
    private String[] tokens;
    private String currentBatchName, currentRoomName, currentUserId, userRole, userName, currentDate, currentTime;
    private DatabaseReference reference, chatDataReference;
    private ImageButton sendMessageButton, sendFilesButton;
    private EditText userMessageInput;
    private TextView displayTextMessages;
    private ScrollView mScrollView;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;

    private String checker = "", myUrl = "";
    private StorageTask uploadTask;
    private Uri fileUri;
    private ProgressDialog loadingBar;

    private ImageButton findImages, findPdfs, findDocs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_chat);

        tok = getIntent().getExtras().get("roomname").toString();
        tokens = tok.split("pk");
        currentBatchName = tokens[0];
        currentRoomName = tokens[1];
        getSupportActionBar().setTitle(currentRoomName);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference();
        chatDataReference = FirebaseDatabase.getInstance().getReference().child("ChatData").child(currentBatchName).child(currentRoomName);

        reference.child("Users").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                userRole = user.getRole();
                userName = user.getName();
                invalidateOptionsMenu();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        sendMessageButton = findViewById(R.id.roomchat_send);
        userMessageInput = findViewById(R.id.roomchat_typemessage);
        sendFilesButton = findViewById(R.id.roomchat_sendfiles_btn);
//        displayTextMessages = findViewById(R.id.roomchat_textdisplay);
//        mScrollView = findViewById(R.id.my_scroll_view);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveMessagesToDatabase();
                userMessageInput.setText("");
//                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
            }
        });

        StartHere();
        messageAdapter = new MessageAdapter(messagesList, currentBatchName, currentRoomName);
        userMessagesList = (RecyclerView) findViewById(R.id.room_chat_recyclerList);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);
        loadingBar = new ProgressDialog(this);

        sendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectFiles();
            }
        });
        FindData();

    }

    void StartHere() {
        chatDataReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages messages = dataSnapshot.getValue(Messages.class);
                messagesList.add(messages);
                messageAdapter.notifyDataSetChanged();
                userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Messages messages = dataSnapshot.getValue(Messages.class);
                messagesList.add(messages);
                messageAdapter.notifyDataSetChanged();
                userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), BatchChat.class);
        intent.putExtra("groupname", currentBatchName);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu2, menu);
        if(userRole != null) {
            if(userRole.equals("Mentor")) {
                menu.findItem(R.id.add_students).setVisible(true);
                menu.findItem(R.id.list_students).setVisible(true);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.add_students:
                Intent showStudentsIntent = new Intent(RoomChat.this, AddStudents.class);
                showStudentsIntent.putExtra("roomname", currentBatchName + "pk" + currentRoomName);
                startActivity(showStudentsIntent);
                finish();
                return true;

            case R.id.list_students:
                Intent listStudentsIntent = new Intent(RoomChat.this, ShowStudents.class);
                listStudentsIntent.putExtra("roomname", currentBatchName + "pk" + currentRoomName);
                startActivity(listStudentsIntent);
                finish();
                return true;
        }
        return false;
    }


    void SaveMessagesToDatabase() {
        String message = userMessageInput.getText().toString();
        String messageKey = chatDataReference.push().getKey();
        if(TextUtils.isEmpty(message)) {
            Toast.makeText(getApplicationContext(), "Enter message", Toast.LENGTH_SHORT).show();
        } else {
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = currentDateFormat.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calForTime.getTime());

//            HashMap<String, Object> hmap = new HashMap<>();
//            chatDataReference.updateChildren(hmap);

            HashMap<String, Object> hmap2 = new HashMap<>();
            hmap2.put("uid", currentUserId);
            hmap2.put("name", userName);
            hmap2.put("message", message);
            hmap2.put("messageid", messageKey);
            hmap2.put("type", "text");
            hmap2.put("date", currentDate);
            hmap2.put("time", currentTime);
            chatDataReference.child(messageKey).updateChildren(hmap2);
        }
    }

    void DisplayMessages(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();
        while (iterator.hasNext()) {
            String chatDate = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot) iterator.next()).getValue();

            displayTextMessages.append(chatName + ":\n" + chatMessage + "\nDate: " + chatDate + "\nTime: " + chatTime + "\n\n\n");

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    void SelectFiles() {
        CharSequence[] options = new CharSequence[] {
                "Images",
                "PDF Files",
                "Word Files"
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(RoomChat.this);
        builder.setTitle("Select the file ");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if(i == 0) {
                    checker = "image";
                    Intent imageIntent = new Intent();
                    imageIntent.setAction(Intent.ACTION_GET_CONTENT);
                    imageIntent.setType("image/*");
                    startActivityForResult(imageIntent.createChooser(imageIntent, "Select Image"), 101);
                }
                if(i == 1) {
                    checker = "pdf";
                    Intent pdfIntent = new Intent();
                    pdfIntent.setAction(Intent.ACTION_GET_CONTENT);
                    pdfIntent.setType("application/pdf");
                    startActivityForResult(pdfIntent.createChooser(pdfIntent, "Select PDF File"), 101);
                }
                if(i == 2) {
                    checker = "docx";
                    Intent docIntent = new Intent();
                    docIntent.setAction(Intent.ACTION_GET_CONTENT);
                    docIntent.setType("application/msword");
                    startActivityForResult(docIntent.createChooser(docIntent, "Select Document"), 101);
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 101 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            loadingBar.setTitle("Sending File");
            loadingBar.setMessage("Please wait, we are sending the file...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            fileUri = data.getData();
            if(!checker.equals("image")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");

                DatabaseReference userMessageKeyRef = chatDataReference.push();
                final String messagePushId = userMessageKeyRef.getKey();

//                final StorageReference filePath = storageReference.child(fileUri.getLastPathSegment() + "." + checker);
                final StorageReference filePath = storageReference.child(fileUri.getLastPathSegment());
                filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()) {
                            Calendar calForDate = Calendar.getInstance();
                            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
                            currentDate = currentDateFormat.format(calForDate.getTime());

                            Calendar calForTime = Calendar.getInstance();
                            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
                            currentTime = currentTimeFormat.format(calForTime.getTime());

                            HashMap<String, Object> docHmap = new HashMap<>();
                            docHmap.put("uid", currentUserId);
                            docHmap.put("name", userName);
                            docHmap.put("message", task.getResult().getDownloadUrl().toString());
                            docHmap.put("messageid", messagePushId);
                            docHmap.put("iname", fileUri.getLastPathSegment());
                            docHmap.put("type", checker);
                            docHmap.put("date", currentDate);
                            docHmap.put("time", currentTime);
                            chatDataReference.child(messagePushId).updateChildren(docHmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        loadingBar.dismiss();
                                        messageAdapter.notifyDataSetChanged();
                                        Toast.makeText(getApplicationContext(), "File Uploaded in db", Toast.LENGTH_SHORT).show();
                                    } else {
                                        loadingBar.dismiss();
                                        messageAdapter.notifyDataSetChanged();
                                        Toast.makeText(getApplicationContext(), "Error occurred while uploading the file", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double p = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        loadingBar.setMessage((int)p + " % Uploaded...");
                    }
                });

            } else if (checker.equals("image")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                DatabaseReference userMessageKeyRef = chatDataReference.push();
                final String messagePushId = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushId + ".jpg");
                uploadTask = filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if(!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()) {
                            Uri downloadUrl = task.getResult();
                            myUrl = downloadUrl.toString();

                            Calendar calForDate = Calendar.getInstance();
                            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
                            currentDate = currentDateFormat.format(calForDate.getTime());

                            Calendar calForTime = Calendar.getInstance();
                            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
                            currentTime = currentTimeFormat.format(calForTime.getTime());

                            HashMap<String, Object> imageHmap = new HashMap<>();
                            imageHmap.put("uid", currentUserId);
                            imageHmap.put("name", userName);
                            imageHmap.put("message", myUrl);
                            imageHmap.put("messageid", messagePushId);
                            imageHmap.put("iname", fileUri.getLastPathSegment());
                            imageHmap.put("type", checker);
                            imageHmap.put("date", currentDate);
                            imageHmap.put("time", currentTime);
                            chatDataReference.child(messagePushId).updateChildren(imageHmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        loadingBar.dismiss();
                                        messageAdapter.notifyDataSetChanged();
                                        Toast.makeText(getApplicationContext(), "Image Uploaded in db", Toast.LENGTH_SHORT).show();
                                    } else {
                                        loadingBar.dismiss();
                                        messageAdapter.notifyDataSetChanged();
                                        Toast.makeText(getApplicationContext(), "Error occurred while uploading the image", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                });

            } else {
                loadingBar.dismiss();
                messageAdapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), "Nothing Selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void FindData() {
        findImages = findViewById(R.id.roomchat_find_image);
        findPdfs = findViewById(R.id.roomchat_find_pdf);
        findDocs = findViewById(R.id.roomchat_find_doc);

        findImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DisplayData.class);
                intent.putExtra("roomname", currentBatchName+ "pk" + currentRoomName + "pk" +  "image");
                startActivity(intent);
                finish();
            }
        });

        findPdfs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DisplayData.class);
                intent.putExtra("roomname", currentBatchName+ "pk" + currentRoomName + "pk" +  "pdf");
                startActivity(intent);
                finish();
            }
        });

        findDocs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DisplayData.class);
                intent.putExtra("roomname", currentBatchName+ "pk" + currentRoomName + "pk" +  "docx");
                startActivity(intent);
                finish();
            }
        });
    }
}
