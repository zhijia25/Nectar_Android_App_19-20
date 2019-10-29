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

public class ListSingleDatabaseInstanceModel {
    public StringRequest listSingleDatabaseInstance(String fullURL, final String token, final Context mApplicationContext, final HttpRequest.VolleyCallback callback, final Context context){

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
                            String name = resp.getJSONObject("instance").getString("name");
                            String id = resp.getJSONObject("instance").getString("id");
                            String datastore = resp.getJSONObject("instance").getJSONObject("datastore").getString("type");
                            String version = resp.getJSONObject("instance").getJSONObject("datastore").getString("version");
                            String created = resp.getJSONObject("instance").getString("created");
                            String updated = resp.getJSONObject("instance").getString("updated");
                            String status = resp.getJSONObject("instance").getString("status");
                            Integer volumeInt = resp.getJSONObject("instance").getJSONObject("volume").getInt("size");
                            String volume = volumeInt.toString();
                            result.put("name", name);
                            result.put("id", id);
                            result.put("datastore", datastore);
                            result.put("version", version);
                            result.put("created", created);
                            result.put("updated", updated);
                            result.put("status", status);
                            result.put("volume", volume);
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
