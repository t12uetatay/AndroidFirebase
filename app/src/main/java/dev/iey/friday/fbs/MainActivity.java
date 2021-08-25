package dev.iey.friday.fbs;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements FeedsAdapter.AdapterOnClick {
    private DatabaseReference mRef;
    private FirebaseDatabase databse;
    private RecyclerView recyclerView;
    private FeedsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView=findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        recyclerView.addItemDecoration(new VerticalSpace(20));
        databse = FirebaseDatabase.getInstance();
        //call path on firebase database
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, UploadActivity.class);
                intent.putExtra("key", "-");
                startActivity(intent);
            }
        });
    }

    public void onResume(){
        super.onResume();
        mRef = databse.getReference("feed_item");
        mRef.addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot){
                List<FeedItem> list = new ArrayList<>();
                for (DataSnapshot mDataSnapshot : dataSnapshot.getChildren()){
                    FeedItem feedItem = mDataSnapshot.getValue(FeedItem.class);
                    list.add(feedItem);
                }
                adapter = new FeedsAdapter(MainActivity.this, list);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError){
                // TODO: Implement this method
                Toast.makeText(MainActivity.this, databaseError.getDetails()+" "+databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //handling adapter when on klik
    @Override
    public void onKlik(FeedItem feedItem, int position) {
        CharSequence[] items = {"Edit", "Delete"};
        new MaterialAlertDialogBuilder(MainActivity.this, R.style.Theme_MaterialComponents_DayNight_Dialog_Alert)
                .setTitle(feedItem.getTitle())
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            Intent intent = new Intent(MainActivity.this, UploadActivity.class);
                            intent.putExtra("key", feedItem.getKey());
                            startActivity(intent);
                        } else {
                            new MaterialAlertDialogBuilder(MainActivity.this , R.style.Theme_MaterialComponents_DayNight_Dialog_Alert)
                                    .setTitle("Alert!")
                                    .setMessage("Are you sure to remove "+feedItem.getTitle()+"?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            removeItem(feedItem);
                                        }
                                    })
                                    .setNeutralButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                        }
                                    })
                                    .show();
                        }
                    }
                }).show();
    }

    private void removeItem(final FeedItem feedItem){
        if(mRef != null){
            mRef.child(feedItem.getKey()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>(){
                @Override
                public void onSuccess(Void mVoid){
                    Toast.makeText(MainActivity.this,"Removed successfull !", Toast.LENGTH_LONG).show();
                    removeFile(feedItem.getThumbnailUrl());
                }
            });
        }
    }

    public boolean removeFile(String mImageUrl){
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReferenceFromUrl(mImageUrl);
        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.e("Picture","#deleted");
            }
        });
        return false;
    }
}