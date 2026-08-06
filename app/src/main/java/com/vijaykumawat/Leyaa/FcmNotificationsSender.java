package com.vijaykumawat.Leyaa;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.vijaykumawat.Leyaa.network.FcmApiService;
import com.vijaykumawat.Leyaa.network.FcmPayload;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FcmNotificationsSender {

    private final String userFcmToken;
    private final String title;
    private final String body;
    private final Context mContext;
    private final Activity mActivity;

    private static final String BASE_URL = "https://us-central1-leyaa-7b042.cloudfunctions.net/";

    public FcmNotificationsSender(String userFcmToken, String title, String body,
                                  Context context, Activity activity) {
        this.userFcmToken = userFcmToken;
        this.title = title;
        this.body = body;
        this.mContext = context;
        this.mActivity = activity;
    }

    public void SendNotifications() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        FcmApiService apiService = retrofit.create(FcmApiService.class);
        FcmPayload payload = new FcmPayload(userFcmToken, title, body);

        apiService.sendNotification(payload).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d("FCM", "Success: " + response.code());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("FCM", "Error: " + t.getMessage());
            }
        });
    }
}
