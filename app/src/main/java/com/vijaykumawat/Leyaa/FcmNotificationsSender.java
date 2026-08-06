package com.vijaykumawat.Leyaa;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class FcmNotificationsSender {

    private final String userFcmToken;
    private final String title;
    private final String body;
    private final Context mContext;
    private final Activity mActivity;

    // 🔹 Use your deployed function URL here
    private static final String BACKEND_URL =
            "https://us-central1-leyaa-7b042.cloudfunctions.net/sendNotification";

    public FcmNotificationsSender(String userFcmToken, String title, String body,
                                  Context context, Activity activity) {
        this.userFcmToken = userFcmToken;
        this.title = title;
        this.body = body;
        this.mContext = context;
        this.mActivity = activity;
    }

    public void SendNotifications() {
        try {
            JSONObject payload = new JSONObject();
            payload.put("token", userFcmToken);
            payload.put("title", title);
            payload.put("body", body);

            RequestQueue queue = Volley.newRequestQueue(mContext);

            JsonObjectRequest req = new JsonObjectRequest(
                    Request.Method.POST,
                    BACKEND_URL,
                    payload,
                    response -> Log.d("FCM", "Success: " + response),
                    error -> Log.e("FCM", "Error: " + error.toString())
            );

            queue.add(req);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
