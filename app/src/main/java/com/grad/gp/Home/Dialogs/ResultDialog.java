package com.grad.gp.Home.Dialogs;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.grad.gp.Common.APIService;
import com.grad.gp.Common.WebServiceClient;
import com.grad.gp.Models.ImageResponse;
import com.grad.gp.R;
import com.grad.gp.Utils.ArabicTTS;
import com.grad.gp.Utils.CustomProgress;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResultDialog extends DialogFragment {

    View view;
    ImageView mTranslation;
    TextView mResponse;
    String result ="";
    TextToSpeech ttsAR;
    ArabicTTS ttsAAR;
    CustomProgress mCustomProgress = CustomProgress.getInstance();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.result_dialog_fragment, container, false);
        result = getArguments().getString("result");
        initViews();

        return view;
    }

    private void initViews() {
        mTranslation = view.findViewById(R.id.translation_btn);
        mResponse = view.findViewById(R.id.response);

        mTranslation.setOnClickListener(v -> TranslationAPI());

        ttsAAR = new ArabicTTS();
        ttsAAR.prepare(getContext());

        ttsAR = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR)
                    ttsAR.setLanguage(Locale.forLanguageTag("ar"));
            }
        });

        mResponse.setText(result);

    }


    public void TranslationAPI() {
        if (result.equals("")) {
            Toast.makeText(getContext(), "There is No Data to Translate", Toast.LENGTH_LONG).show();
        } else {
            mCustomProgress.showProgress(getContext(), "Please Wait", false);

            Map<String, String> map = new HashMap<>();
            map.put("text", result);


            APIService apiService = WebServiceClient.getRetrofit().create(APIService.class);
            Call<ImageResponse> call = apiService.Translation(map);

            call.enqueue(new Callback<ImageResponse>() {
                @Override
                public void onResponse(Call<ImageResponse> call, Response<ImageResponse> response) {
                    try {
                        Log.e("Success", "onResponse: " + response.code());
                        Log.e("Success", "onResponse: " + response.toString());
                        Log.e("Success", "onResponse: " + response.body().getOutput());
                        result = response.body().getOutput();
                        mResponse.setText(result);
                        mCustomProgress.hideProgress();
                        ttsAR.speak(result, TextToSpeech.QUEUE_FLUSH, null);
                    } catch (Exception e) {
                        result = "حدث خطأ في الترجمة" ;
                        mResponse.setText("حدث خطأ في الترجمة");
                        mCustomProgress.hideProgress();
//                        ttsAR.speak(result, TextToSpeech.QUEUE_FLUSH, null);
                        ttsAAR.talk(result);
                    }
                }

                @Override
                public void onFailure(Call<ImageResponse> call, Throwable t) {
                    Log.e("Fail", "onFailure: " + t.getMessage());
                }
            });

        }
    }






}
