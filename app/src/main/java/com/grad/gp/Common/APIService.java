package com.grad.gp.Common;

import com.grad.gp.Models.ImageResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {


    @Headers({"Content-Type:application/json",
            "Accept:text/plain"})
    @POST("/VisuallyImpaired")
    Call<ImageResponse> VisuallyImpaired(@Body Map<String, String> files);

    @Headers({"Content-Type:application/json",
            "Accept:text/plain"})
    @POST("/Translation")
    Call<ImageResponse> Translation(@Body Map<String, String> files);

    @Headers({"Content-Type:application/json",
            "Accept:text/plain"})
    @POST("/FaceRecognitionTesting")
    Call<ImageResponse> FaceRecognitionTesting(@Body Map<String, String> files);

    @Headers({"Content-Type:application/json",
            "Accept:text/plain"})
    @POST("/FaceRecognitionTraining")
    Call<ImageResponse> FaceRecognitionTraining(@Body Map<String, String> files);


}
