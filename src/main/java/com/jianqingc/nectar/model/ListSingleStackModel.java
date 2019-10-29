package com.jianqingc.nectar.model;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.jianqingc.nectar.activity.LoginActivity;
import com.jianqingc.nectar.httpRequest.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ListSingleStackModel {
    public StringRequest listSingleStack(String fullURL, final String token, final Context mApplicationContext, final HttpRequest.VolleyCallback callback, final Context context){

        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        /**
                         *  Pass the response of HTTP request to the Fragment
                         *  Server ID, AZ,IP address, Name, and Status
                         */
                        try {
                            JSONObject resp = new JSONObject(response);
                            JSONObject result = new JSONObject();
                            String id = resp.getJSONObject("stack").getString("id");
                            String creationTime = resp.getJSONObject("stack").getString("creation_time");
                            String description = resp.getJSONObject("s6tack").getString("description");
                            String disable_rollback = resp.getJSONObject("stack").getString("disable_rollback");
                            String name = resp.getJSONObject("stack").getString("stack_name");
                            String status = resp.getJSONObject("stack").getString("stack_status");
                            String statusReason = resp.getJSONObject("stack").getString("stack_status_reason");
                            result.put("id", id);
                            result.put("creationTime", creationTime);
                            result.put("description", description);
                            result.put("disable_rollback", disable_rollback);
                            result.put("name", name);
                            result.put("status", status);
                            result.put("statusReason", statusReason);
                            String stringResult = result.toString();
                            callback.onSuccess(stringResult);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        // Display the first 500 characters of the response string.
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 401) {
                    Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(mApplicationContext, LoginActivity.class);
                    context.startActivity(i);
                }
                Toast.makeText(mApplicationContext, "Listing Instances Failed", Toast.LENGTH_SHORT).show();
                callback.onSuccess("error");
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
