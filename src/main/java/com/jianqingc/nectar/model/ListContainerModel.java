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
import com.jianqingc.nectar.data.ResponseParser;
import com.jianqingc.nectar.httpRequest.HttpRequest;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

public class ListContainerModel {
    public StringRequest listContainer(String fullURL, final String token, final Context mApplicationContext, final HttpRequest.VolleyCallback callback){
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONArray resultArray;
                resultArray = ResponseParser.getInstance(mApplicationContext).listcontainer(response);
                String result = resultArray.toString();
                callback.onSuccess(result);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    } else {
                        Toast.makeText(mApplicationContext, "Delete failed", Toast.LENGTH_SHORT).show();
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
