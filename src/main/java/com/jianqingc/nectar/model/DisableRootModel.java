package com.jianqingc.nectar.model;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.jianqingc.nectar.activity.LoginActivity;
import com.jianqingc.nectar.httpRequest.HttpRequest;

import java.util.HashMap;
import java.util.Map;

public class DisableRootModel {
    public StringRequest disableRoot(String fullURL, final String token, final Context mApplicationContext, final HttpRequest.VolleyCallback callback){

        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, fullURL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        callback.onSuccess("success");

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(mApplicationContext, "Delete  successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    } else {
                        Toast.makeText(mApplicationContext, "Disable Root ", Toast.LENGTH_SHORT).show();
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
        return stringRequest;
    }
}
