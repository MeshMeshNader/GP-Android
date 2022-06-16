package com.grad.gp.Home.Gallery;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.grad.gp.Models.UserDataModel;
import com.grad.gp.R;
import com.grad.gp.Utils.CustomProgress;

import java.util.ArrayList;
import java.util.Map;

public class GalleryActivity extends AppCompatActivity {

    ImageView mBackBtn;
    CustomProgress mCustomProgress = CustomProgress.getInstance();
    RecyclerView mGalleryRecycler;
    GalleryAdapter mGalleryAdapter;
    ArrayList<String> personNames;
    ArrayList<String> personUrls;

    String currentUserID;
    DatabaseReference mUsersRef;
    FirebaseAuth mAuth;
    UserDataModel userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        initViews();


    }

    private void initViews() {


        mBackBtn=findViewById(R.id.edit_back);
        mBackBtn.setOnClickListener(v -> onBackPressed());
        personNames = new ArrayList<>();
        personUrls = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        mUsersRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserID);

        mGalleryRecycler = findViewById(R.id.gallery_items_recyclerview);
        mGalleryRecycler.setHasFixedSize(true);
        mGalleryRecycler.setLayoutManager(new GridLayoutManager(this, 2));

        getData();
    }


    private void getData() {

        mCustomProgress.showProgress(this, "Please Wait... Loading!!!", true);

        mUsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userData = snapshot.getValue(UserDataModel.class);


                    for (Map.Entry<String, String> entry : userData.getPersonsData().entrySet()) {
                        personNames.add(entry.getKey() + " your " + entry.getValue());
                    }
                    for (Map.Entry<String, String> entry : userData.getImagesURLs().entrySet()) {
                        personUrls.add(entry.getValue());
                    }

                    mGalleryAdapter = new GalleryAdapter(GalleryActivity.this, personNames, personUrls);
                    mGalleryRecycler.setAdapter(mGalleryAdapter);

                    mCustomProgress.hideProgress();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}