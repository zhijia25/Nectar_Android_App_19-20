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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ListSingleInstanceModel {
    public StringRequest listSingleInstance(String fullURL, final String token, final Context mApplicationContext, final HttpRequest.VolleyCallback callback, final Context context){
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
                            String id = resp.getJSONObject("server").getString("id");
                            String zone = resp.getJSONObject("server").getString("OS-EXT-AZ:availability_zone");
                            String address = resp.getJSONObject("server").getString("accessIPv4");
                            String name = resp.getJSONObject("server").getString("name");
                            String status = resp.getJSONObject("server").getString("status");
                            String created = resp.getJSONObject("server").getString("created");
                            String image = resp.getJSONObject("server").getJSONObject("image").getString("id");
                            String key = resp.getJSONObject("server").getString("key_name");
                            if (key.equals("null")) {
                                key = "None";
                            }
                            JSONArray sgArray = resp.getJSONObject("server").getJSONArray("security_groups");
                            String sg = "";
                            if (sgArray.length() == 0) {
                                sg = "None";
                            } else {
                                for (int i = 0; i < sgArray.length(); i++) {
                                    JSONObject sgObject = (JSONObject) sgArray.get(i);
                                    if (i == 0) {
                                        sg = sgObject.getString("name");
                                    } else {
                                        sg = sg + ", " + sgObject.getString("name");
                                    }
                                }
                            }

                            JSONArray vArray = resp.getJSONObject("server").getJSONArray("os-extended-volumes:volumes_attached");
                            int vNum = vArray.length();
                            for (int j = 0; j < vNum; j++) {
                                JSONObject vObject = (JSONObject) vArray.get(j);
                                String volume = vObject.getString("id");
                                result.put("volume" + j, volume);
                            }
                            result.put("id", id);
                            result.put("zone", zone);
                            result.put("address", address);
                            result.put("name", name);
                            result.put("status", status);
                            result.put("created", created);
                            result.put("image", image);
                            result.put("key", key);
                            result.put("securityg", sg);
                            result.put("volNum", vNum);
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
