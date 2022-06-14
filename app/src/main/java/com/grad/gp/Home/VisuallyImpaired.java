package com.grad.gp.Home;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.grad.gp.Common.APIService;
import com.grad.gp.Common.WebServiceClient;
import com.grad.gp.Home.Dialogs.ResultDialog;
import com.grad.gp.Models.ImageResponse;
import com.grad.gp.R;
import com.grad.gp.Utils.CustomProgress;
import com.grad.gp.Utils.ImageResizer;

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

public class VisuallyImpaired extends AppCompatActivity {

    final static int Gallery_Pick = 1;
    ImageView mFaceDetection, mImageCaptioning,
            mObjectDetection, mLabelsDetection,
            mCurrencyDetection, mTextReader;
    CustomProgress mCustomProgress = CustomProgress.getInstance();
    TextToSpeech ttsEN;
    String Output = "";
    public static String opCode = "";
    Bundle bundle = new Bundle();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.visually_impaired);

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
        mFaceDetection = findViewById(R.id.face_detection_btn);
        mObjectDetection = findViewById(R.id.object_detection_btn);
        mImageCaptioning = findViewById(R.id.image_captioning_btn);
        mLabelsDetection = findViewById(R.id.labels_detection_btn);
        mCurrencyDetection = findViewById(R.id.currency_detection_btn);
        mTextReader = findViewById(R.id.text_reader_btn);




        mFaceDetection.setOnClickListener(v -> FaceDetectionAPI());
        mObjectDetection.setOnClickListener(v -> ObjectDetectionAPI());
        mImageCaptioning.setOnClickListener(v -> ImageCaptioningAPI());
        mLabelsDetection.setOnClickListener(v -> LabelsDetectionAPI());
        mCurrencyDetection.setOnClickListener(v -> CurrencyDetectionAPI());
        mTextReader.setOnClickListener(v -> TextReaderAPI());



    }

    private void ImageCaptioningAPI() {
        opCode = "1";
        showSelectDialog();
    }

    private void ObjectDetectionAPI() {
        opCode = "2";
        showSelectDialog();
    }

    private void FaceDetectionAPI() {
        opCode = "3";
        showSelectDialog();
    }

    private void TextReaderAPI() {
        opCode = "4";
        showSelectDialog();
    }


    private void CurrencyDetectionAPI() {
        opCode = "5";
        showSelectDialog();
    }


    private void LabelsDetectionAPI() {
        opCode = "6";
        showSelectDialog();
    }




    public static String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        encodedImage = encodedImage.replace("\n", "");

        return encodedImage;
    }

    public void requestOutput(Bitmap bitmap, String Operation) {
        ResultDialog resultDialog = new ResultDialog();
        mCustomProgress.showProgress(this, "Please Wait, The image is Under Processing", false);

//        BitMapImage = BitmapFactory.decodeResource(this.getResources(),
//                Resource);

        Map<String, String> map = new HashMap<>();
        map.put("image", getStringImage(bitmap));
        map.put("operation", Operation);


        APIService apiService = WebServiceClient.getRetrofit().create(APIService.class);
        Call<ImageResponse> call = apiService.VisuallyImpaired(map);

        call.enqueue(new Callback<ImageResponse>() {
            @Override
            public void onResponse(Call<ImageResponse> call, Response<ImageResponse> response) {
                try {
                    Log.e("Success", "onResponse: " + response.code());
                    Log.e("Success", "onResponse: " + response.toString());
                    Log.e("Success", "onResponse: " + response.body().getOutput());
                    Output = response.body().getOutput();
                    mCustomProgress.hideProgress();
                    bundle.putString("result", Output);
                    resultDialog.setArguments(bundle);
                    ttsEN.speak(Output, TextToSpeech.QUEUE_FLUSH, null);
                } catch (Exception e) {
                    mCustomProgress.hideProgress();
                    Output = "There is no Result";
                    bundle.putString("result", "There is no Result");
                    resultDialog.setArguments(bundle);
                    ttsEN.speak(Output, TextToSpeech.QUEUE_FLUSH, null);
                }
                resultDialog.show(getSupportFragmentManager(), "ResultDialog");
            }

            @Override
            public void onFailure(Call<ImageResponse> call, Throwable t) {
                Log.e("Fail", "onFailure: " + t.getMessage());
                mCustomProgress.hideProgress();
                bundle.putString("result", Output);
                resultDialog.setArguments(bundle);
                resultDialog.show(getSupportFragmentManager(), "ResultDialog");
            }

        });
    }

    private void showSelectDialog() {
        AlertDialog.Builder selectionDialog = new AlertDialog.Builder(VisuallyImpaired.this);
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
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivity(intent);
    }

    @AfterPermissionGranted(101)
    private void choosePhotoFromGallery() {

        String[] galleryPermission = new String[0];
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            galleryPermission = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (EasyPermissions.hasPermissions(VisuallyImpaired.this, galleryPermission)) {
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
                        Bitmap reducedBits = ImageResizer.reduceBitmapSize(bits , 240000);
                        requestOutput(reducedBits, opCode);
                    } catch (Exception e) {
                        Log.e("Error", "onActivityResult: " + e.getMessage());
                    }
                }
            }
        }
    }


}