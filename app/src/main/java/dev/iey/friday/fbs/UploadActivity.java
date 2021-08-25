package dev.iey.friday.fbs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class UploadActivity extends AppCompatActivity {
    private MaterialButton selectimg;
    private FloatingActionButton fab;
    private AppCompatImageView simg;
    private TextInputEditText title;
    private TextInputEditText description;
    private DatabaseReference mRef;
    private StorageReference storage;
    private FirebaseDatabase databse;
    final int IMAGE_REQUEST_CODE = 999;
    private Uri uri;
    private String key;
    FeedItem feedItem=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        selectimg=findViewById(R.id.btn_select);
        simg=findViewById(R.id.selekedimg);
        fab=findViewById(R.id.fab);
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        //path firebase storage
        storage= FirebaseStorage.getInstance().getReference("assets");
        databse = FirebaseDatabase.getInstance();
        mRef = databse.getReference("feed_item");
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
        selectimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(UploadActivity.this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},IMAGE_REQUEST_CODE);
            }
        });

        Intent intent = getIntent();
        key = intent.getStringExtra("key");
        if (!key.equals("-")){
            //to handling ui when update item
            mRef.child(key).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    feedItem = snapshot.getValue(FeedItem.class);
                    title.setText(feedItem.getTitle());
                    description.setText(feedItem.getDescription());
                    Glide.with(getApplicationContext()).load(feedItem.getThumbnailUrl()).into(simg);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==IMAGE_REQUEST_CODE){
            if (grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                Intent intent=new Intent(new Intent(Intent.ACTION_PICK));
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent,"select image"),IMAGE_REQUEST_CODE);

            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            uri = data.getData();
            Glide.with(this).load(uri).into(simg);
        }
    }
    private String getFileExtensoin (Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap map = MimeTypeMap.getSingleton();
        return map.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {
        String childKey= String.valueOf(System.currentTimeMillis());
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading.....");
        progressDialog.show();
        progressDialog.setCancelable(false);
        if (uri!=null) { //handling
            //path to save image
            final StorageReference storageReference;
            if (key.equals("-")){
                storageReference = storage.child(childKey + "." + getFileExtensoin(uri));
            } else {
                storageReference  = storage.child(feedItem.getKey() + "." + getFileExtensoin(uri));
            }
            UploadTask uploadTask = storageReference.putFile(uri);
            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                }
            });
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(UploadActivity.this, "Upload SuccsessFul", Toast.LENGTH_SHORT).show();
                        if (key.equals("-")){
                            FeedItem item = new FeedItem(
                                    childKey,
                                    title.getText().toString().trim(),
                                    description.getText().toString().trim(),
                                    task.getResult().toString()
                            );
                            mRef.child(item.getKey()).setValue(item).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                }
                            });
                        } else {
                            FeedItem item = new FeedItem(
                                    feedItem.getKey(),
                                    title.getText().toString().trim(),
                                    description.getText().toString().trim(),
                                    task.getResult().toString()
                            );
                            mRef.child(item.getKey()).setValue(item).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                }
                            });
                        }
                        progressDialog.dismiss();
                        onBackPressed();
                        //end
                    } else {
                        Toast.makeText(UploadActivity.this, "Image upload unsuccessful. Please try again."
                                , Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {//handling when user no upload image
            if (key.equals("-")){//add
                FeedItem item = new FeedItem(
                        childKey,
                        title.getText().toString().trim(),
                        description.getText().toString().trim(),
                        "-"
                );
                mRef.child(item.getKey()).setValue(item).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                });
            } else {//edit
                FeedItem item = new FeedItem(
                        feedItem.getKey(),
                        title.getText().toString().trim(),
                        description.getText().toString().trim(),
                        feedItem.getThumbnailUrl()
                );
                mRef.child(item.getKey()).setValue(item).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                });
            }
            progressDialog.dismiss();
            onBackPressed();
        }
    }
}