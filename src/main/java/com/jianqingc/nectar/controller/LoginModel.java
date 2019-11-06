package com.jianqingc.nectar.controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.jianqingc.nectar.activity.LoginActivity;
import com.jianqingc.nectar.activity.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginModel {
    private String loginUri;
    private JSONObject send;
    Context context;
    Context mApplicationContext;
    public LoginModel(String loginUri, JSONObject send, Context context, Context mApplicationContext){
        this.loginUri = loginUri;
        this.send = send;
        this.context = context;
        this.mApplicationContext = mApplicationContext;
    }
    public  JsonRequest loginRequest(){
    JsonRequest request = new JsonRequest
            (Request.Method.POST, loginUri, send, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONObject body= response.getJSONObject("body");
                        JSONObject header = response.getJSONObject("header");
                        ResponseParser.getInstance(mApplicationContext).loginParser(header,body);
                        Intent i = new Intent(mApplicationContext, MainActivity.class);
                        SharedPreferences sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        /**
                         * Enable auto-login function
                         */
                        editor.putBoolean("isSignedOut", false);
                        editor.apply();
                        context.startActivity(i);
                        Toast.makeText(mApplicationContext, "Login Succeed", Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
//                            Log.e(LOG_TAG, Log.getStackTraceString(e));
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error.networkResponse.statusCode == 401) {

                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        context.startActivity(i);
                    }
                    Log.i("error", "onErrorResponse: ");
                    Toast.makeText(mApplicationContext, "              Login Failed\nPlease check the required fields", Toast.LENGTH_SHORT).show();
                }
            })

    {
        @Override
        public String getBodyContentType() {
            return "application/json";
        }
    };
    return request;
}}
