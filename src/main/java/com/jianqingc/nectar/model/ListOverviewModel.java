package com.jianqingc.nectar.model;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

public class ListOverviewModel {
    public StringRequest listOverview(final HttpRequest.VolleyCallback callback, final Context context, final Context mApplicationContext, final SharedPreferences sharedPreferences){
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String partURL = "/limits";
        String fullURL = computeServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        callback.onSuccess(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error){
                if (error.networkResponse.statusCode == 401) {
                    Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(mApplicationContext, LoginActivity.class);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    /**
                     * Enable auto-login function
                     */
                    editor.putBoolean("isSignedOut", true);
                    editor.apply();
                    context.startActivity(i);
                } else {
                    Toast.makeText(mApplicationContext, "Listing limits Failed", Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            /**
             * Set Token inside  the Http Request Header，，
             * @return
             * @throws AuthFailureError
             */
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
