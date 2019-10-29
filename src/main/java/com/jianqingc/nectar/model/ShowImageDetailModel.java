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

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ShowImageDetailModel {
    public StringRequest showImageDetail(String fullURL, final String token, final Context mApplicationContext, final HttpRequest.VolleyCallback callback, final Context context){
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject resultObject;
                        resultObject = ResponseParser.getInstance(mApplicationContext).listImageDetail(response);
                        String result = resultObject.toString();
                        callback.onSuccess(result);
                        // Display the first 500 characters of the response string.
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 401) {
                    Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(mApplicationContext, LoginActivity.class);
                    context.startActivity(i);
                } else {
                    Toast.makeText(mApplicationContext, "Getting image detail Failed", Toast.LENGTH_SHORT).show();
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
