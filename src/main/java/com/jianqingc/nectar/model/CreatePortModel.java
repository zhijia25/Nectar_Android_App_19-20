package com.jianqingc.nectar.model;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.jianqingc.nectar.activity.LoginActivity;
import com.jianqingc.nectar.httpRequest.HttpRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CreatePortModel {
    public JsonObjectRequest createPort(String fullURL, final String token, JSONObject json1, final Context mApplicationContext, final HttpRequest.VolleyCallback callback){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json1, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(mApplicationContext, "Create Port successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "Create Port failed", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                return headers;

            }
        };
        return jsonObjectRequest;
    }
}
