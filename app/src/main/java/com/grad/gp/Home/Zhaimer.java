package com.grad.gp.Home;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.grad.gp.Common.APIService;
import com.grad.gp.Common.WebServiceClient;
import com.grad.gp.Home.Dialogs.ConfirmPerson;
import com.grad.gp.Home.Gallery.GalleryActivity;
import com.grad.gp.Models.ImageResponse;
import com.grad.gp.Models.UserDataModel;
import com.grad.gp.R;
import com.grad.gp.Utils.CustomProgress;
import com.grad.gp.Utils.ImageResizer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Zhaimer extends AppCompatActivity implements ConfirmPerson.getDataDialogListener {

    ImageView mBackBtn;
    final static int Gallery_Pick = 1;
    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    ImageView mFaceRecBtn;
    ImageView mPersonResponse;
    TextView mFaceRecResponse;
    Button mGalleryBtn;
    CustomProgress mCustomProgress = CustomProgress.getInstance();
    final String TAG = "Zhaimer Activity";
    TextToSpeech ttsEN;
    String currentUserID;
    FirebaseUser user;
    DatabaseReference mUsersRef;
    FirebaseAuth mAuth;
    StorageReference mUserPersonsImageRef;
    UserDataModel userData;
    Map<String, String> mImagesURL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zhaimer);

        initViews();
        ttsEN = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR)
                    ttsEN.setLanguage(Locale.forLanguageTag("en"));
            }
        });
    }

    private void initViews() {
        mBackBtn=findViewById(R.id.edit_back);
        mBackBtn.setOnClickListener(v -> onBackPressed());
        mFaceRecBtn = findViewById(R.id.face_recognition_btn);
        mFaceRecResponse = findViewById(R.id.face_recognition_response);
        mPersonResponse = findViewById(R.id.person_image_response);
        mGalleryBtn = findViewById(R.id.gallery_btn);
        mGalleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Zhaimer.this, GalleryActivity.class);
                startActivity(intent);
            }
        });


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        mUsersRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserID);
        mUserPersonsImageRef = FirebaseStorage.getInstance().getReference().child(currentUserID);
        mImagesURL = new HashMap<>();
        getData();

        mFaceRecBtn.setOnClickListener(v -> callFaceRecApi());


    }

    private void getData() {

        mCustomProgress.showProgress(this, "Please Wait... Loading!!!", true);

        mUsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userData = snapshot.getValue(UserDataModel.class);
                    mCustomProgress.hideProgress();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void callFaceRecApi() {
        showSelectDialog();
    }

    private void showSelectDialog() {
        AlertDialog.Builder selectionDialog = new AlertDialog.Builder(Zhaimer.this);
        selectionDialog.setTitle("Select Action");
        String[] selectDialogItem = {
                "Take Photo",
                "Choose Photo From Gallery"
        };
        selectionDialog.setItems(selectDialogItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        takePhoto();
                        break;
                    case 1:
                        choosePhotoFromGallery();
                        break;
                }
            }
        });
        selectionDialog.show();
    }

    private void takePhoto() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
        } else {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        }
    }

    @AfterPermissionGranted(101)
    private void choosePhotoFromGallery() {

        String[] galleryPermission = new String[0];
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            galleryPermission = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (EasyPermissions.hasPermissions(Zhaimer.this, galleryPermission)) {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(galleryIntent, Gallery_Pick);
        } else {
            EasyPermissions.requestPermissions(this, "Access for Storage",
                    101, galleryPermission);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
        {
            if (grantResults.length > 0) {
                if (grantResults.toString().equals(Gallery_Pick)) {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(galleryIntent, Gallery_Pick);
                }
            }
        }
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }


    public void requestOutput(Bitmap bitmap) {
        mCustomProgress.showProgress(this, "Please Wait, The image is Under Processing", false);

        JSONObject obj = new JSONObject();
        String encodings = "";
        Map<String, String> encodingMap = userData.getEncodings();
        Map<String, String> mPersonsData = userData.getPersonsData();
        Map<String, String> mPersonsImages = userData.getImagesURLs();
        Log.e(TAG, "requestOutput: " + mPersonsData.get("Yasmeen"));


        if (encodingMap != null) {
            for (Map.Entry<String, String> set : encodingMap.entrySet()) {
                try {
                    obj.put(set.getKey(), set.getValue());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            encodings = obj.toString();
            encodings = encodings.replace(":\"", ":");
            encodings = encodings.replace("\",", ",");
            encodings = encodings.replace("]\"", "]");
        } else {
            encodings = "";
        }

        Log.e(TAG, "requestOutput: " + encodings);
        Map<String, String> map = new HashMap<>();
        map.put("image", getStringImage(bitmap));
        map.put("encodings", encodings);


        APIService apiService = WebServiceClient.getRetrofit().create(APIService.class);
        Call<ImageResponse> call = apiService.FaceRecognitionTesting(map);

        call.enqueue(new Callback<ImageResponse>() {
            @Override
            public void onResponse(Call<ImageResponse> call, Response<ImageResponse> response) {
                try {
                    Log.e("Success", "onResponse: " + response.code());
                    Log.e("Success", "onResponse: " + response.toString());
                    Log.e("Success", "onResponse: " + response.body().getOutput());


                    if (response.body().getOutput().equals("I Found Unknown Person")) {
                        mCustomProgress.hideProgress();
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        DialogFragment dialogFragment = new ConfirmPerson();

                        Bundle args = new Bundle();
                        args.putParcelable("image", bitmap);

                        dialogFragment.setArguments(args);
                        dialogFragment.show(ft, "dialog");
                    } else {
                        String personNameResponse = response.body().getOutput();
                        personNameResponse = personNameResponse.replace("I Found ", "");

                        String url = mPersonsImages.get(personNameResponse);

                        try {
                            Glide.with(getApplicationContext()).load(url).into(mPersonResponse);
                        } catch (Exception e) {
                            Log.e(TAG, "onResponse: " + e.getMessage());
                        }

                        String resultRelative = mPersonsData.get(personNameResponse);
                        String finalResult = response.body().getOutput() + " your " + resultRelative;
                        ttsEN.speak(finalResult, TextToSpeech.QUEUE_FLUSH, null);
                        mFaceRecResponse.setText(finalResult);
                        mCustomProgress.hideProgress();
                    }


                } catch (Exception e) {
                    mCustomProgress.hideProgress();
                }
            }

            @Override
            public void onFailure(Call<ImageResponse> call, Throwable t) {
                Log.e("Fail", "onFailure: " + t.getMessage());
                mCustomProgress.hideProgress();

            }

        });
    }


    public static String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        encodedImage = encodedImage.replace("\n", "");

        return encodedImage;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null) {
            if (resultCode == RESULT_OK) {
                if (data.getData() != null) {
                    try {
                        InputStream inputStream = this.getContentResolver().openInputStream(data.getData());
                        Bitmap bits = BitmapFactory.decodeStream(inputStream);
                        Bitmap reducedBits = ImageResizer.reduceBitmapSize(bits, 240000);
                        requestOutput(reducedBits);
                    } catch (Exception e) {
                        Log.e("Error", "onActivityResult: " + e.getMessage());
                    }
                }
            }
        } else if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            requestOutput(photo);
        }
    }


    @Override
    public void onFinishDialog(String name, String relativeRElation, Bitmap bitmap) {

        mCustomProgress.showProgress(this, "Please Wait, The image is Under Processing", false);
        mImagesURL = userData.getImagesURLs();
        if (mImagesURL == null) {
            mImagesURL = new HashMap<>();
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] data = stream.toByteArray();


        final StorageReference filePath = mUserPersonsImageRef.child(name + ".jpg");
        Log.e(TAG, "onActivityResult: From AddReview The filepath is " + filePath);
        final UploadTask uploadTask = filePath.putBytes(data);


        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String message = e.toString();
                Toast.makeText(Zhaimer.this, "Error Occurred : " + message, Toast.LENGTH_SHORT).show();
            }
        });
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.e(TAG, "Uploaded Successfully..");

                Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            mImagesURL.put(name, task.getResult().toString());
                            requestTraining(name, relativeRElation);
                            Log.e(TAG, "onComplete: Image Uploaded Successfully" + task.getResult().toString());
                            mCustomProgress.hideProgress();
                        }
                    }
                });
                mCustomProgress.hideProgress();
            }
        });
    }


    public void requestTraining(String name, String personRelativeRelation) {
        mUsersRef.child("ImagesURLs").setValue(mImagesURL).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    Map<String, String> map = new HashMap<>();

                    JSONObject obj = new JSONObject();
                    for (Map.Entry<String, String> set : mImagesURL.entrySet()) {
                        try {
                            obj.put(set.getKey(), set.getValue());
                            Log.e(TAG, "onComplete: " + set.getValue());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    map.put("urls", obj.toString());
                    Log.e(TAG, "onComplete: " + obj.toString());


                    APIService apiService = WebServiceClient.getRetrofit().create(APIService.class);
                    Call<ImageResponse> call = apiService.FaceRecognitionTraining(map);

                    call.enqueue(new Callback<ImageResponse>() {
                        @Override
                        public void onResponse(Call<ImageResponse> call, Response<ImageResponse> response) {
                            try {
                                Log.e("Success", "onResponse: " + response.code());
                                Log.e("Success", "onResponse: " + response.toString());
                                Log.e("Success", "onResponse: " + response.body().getOutput());

                                JSONObject newData = new JSONObject(response.body().getOutput());
                                Map<String, String> map = userData.getEncodings();
                                if (map == null) {
                                    map = new HashMap<>();
                                }
                                map.put(name, newData.getString(name));
                                mUsersRef.child("encodings").setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Log.e(TAG, "onComplete: Training and Saving Data Done Successfully");
                                    }
                                });

                                Map<String, String> map2 = userData.getPersonsData();
                                if (map2 == null) {
                                    map2 = new HashMap<>();
                                }
                                map2.put(name, personRelativeRelation);
                                mUsersRef.child("personsData").setValue(map2).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Log.e(TAG, "onComplete: Training and Saving Data Done Successfully");
                                    }
                                });
                                Toast.makeText(Zhaimer.this, "Data Saved Successfully, Now you can use it", Toast.LENGTH_LONG).show();
                                mCustomProgress.hideProgress();
                            } catch (Exception e) {

                                mCustomProgress.hideProgress();
                            }
                        }

                        @Override
                        public void onFailure(Call<ImageResponse> call, Throwable t) {
                            Log.e("Fail", "onFailure: " + t.getMessage());
                            mCustomProgress.hideProgress();
                        }

                    });
                    mCustomProgress.hideProgress();

                } else {
                    Log.e("EditProfile", "onComplete: Error on Saving Data " + task.getException().toString());
                    mCustomProgress.hideProgress();
                }
            }
        });
    }


}