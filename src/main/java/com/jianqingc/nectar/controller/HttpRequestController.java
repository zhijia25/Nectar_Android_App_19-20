package com.jianqingc.nectar.controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import com.android.volley.AuthFailureError;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.*;
import com.jianqingc.nectar.activity.LoginActivity;
import com.jianqingc.nectar.activity.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * Created by Jianqing Chen on 2016/10/2.
 */
public class HttpRequestController {
    private Context mApplicationContext;
    private static HttpRequestController mInstance;
    private SharedPreferences sharedPreferences;

    public interface VolleyCallback {
        void onSuccess(String result);

    }

    public static HttpRequestController getInstance(Context context) {
        if (mInstance == null)
            mInstance = new HttpRequestController(context);
        return mInstance;

    }

    public HttpRequestController(Context context) {
        this.mApplicationContext = context.getApplicationContext();
    }

    /**
     * Login Http Request sent to Keystone.
     *
     * @param tenantName
     * @param username
     * @param password
     * @param context
     */

    public void loginHttp(String tenantName, String username, String password, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String loginUri = "https://keystone.rc.nectar.org.au:5000/v3/auth/tokens";

        /**
         * Assemble Json Object According to NeCTAR API documentation
         */
        JSONObject identityDomain = new JSONObject();
        JSONObject user = new JSONObject();
        JSONObject passwordOuter = new JSONObject();
        JSONObject identity = new JSONObject();
        JSONObject scopeId = new JSONObject();
        JSONObject project = new JSONObject();
        JSONObject scope = new JSONObject();
        JSONObject auth = new JSONObject();
        JSONObject send = new JSONObject();
        JSONArray jsa = new JSONArray();
        jsa.put("password");


        try {
            identityDomain.put("id","default");
            user.put("password", password);
            user.put("name", username);

            user.put("domain", identityDomain);
            passwordOuter.put("user", user);
            identity.put("password", passwordOuter);

            identity.put("methods", jsa);

            scopeId.put("id","default");
            project.put("name",tenantName);
            project.put("domain", scopeId);
            scope.put("project",project);
            auth.put("identity", identity);
            auth.put("scope", scope);
            send.put("auth", auth);



        } catch (JSONException e) {
            e.printStackTrace();
        }
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
                        Log.d("code status", Integer.toString(error.networkResponse.statusCode));
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(request);
    }

    /**
     * List Overview Http Request
     * Pass the String response to Overview Fragment. Overview Fragment can then draw graphs based on the response.
     *
     * @param callback
     * @param context
     */
    public void listOverview(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/limits";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        callback.onSuccess(response);
                        // Display the first 500 characters of the response string.
                        //Toast.makeText(mApplicationContext, "Listing limits Succeed", Toast.LENGTH_SHORT).show();
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /**
     * List Instance Http Request showing the servers detail
     *
     * @param callback
     * @param context
     */
    public void listInstance(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/servers/detail";
        System.out.println("hahaha");
        System.out.println(sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL"));
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray resultArray;
                        resultArray = ResponseParser.getInstance(mApplicationContext).listInstance(response);
                        String result = resultArray.toString();
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
                    Toast.makeText(mApplicationContext, "Listing Instances Failed", Toast.LENGTH_SHORT).show();
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /**
     * List flavor Http Request showing the available flavors
     *
     * @param callback
     * @param context
     */

    public void listFlavor(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/flavors";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray resultArray;
                        resultArray = ResponseParser.getInstance(mApplicationContext).listFlavor(response);
                        String result = resultArray.toString();
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
                    Toast.makeText(mApplicationContext, "Getting flavor Failed", Toast.LENGTH_SHORT).show();
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /**
     * List key pair Http Request showing the available key pairs
     *
     * @param callback
     * @param context
     */
    public void listKeyPair(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        System.out.println(computeServiceURL);
        String fullURL = computeServiceURL + "/os-keypairs";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        System.out.println(token);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray resultArray;
                        resultArray = ResponseParser.getInstance(mApplicationContext).listKeyPair(response);
                        String result = resultArray.toString();
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
                    Toast.makeText(mApplicationContext, "Getting key pairs Failed", Toast.LENGTH_SHORT).show();
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * List rules for the specific security group the user clicks in the AccessAndSecurityFragment listview
     *
     * @param callback
     * @param context
     * @param kpName
     */
    public void showKeyPairDetail(final VolleyCallback callback, final Context context, String kpName) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/os-keypairs/" + kpName;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        JSONObject resultObject;
                        resultObject = ResponseParser.getInstance(mApplicationContext).listkeypairDetail(response);
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
                }
                Toast.makeText(mApplicationContext, "Getting the details of this key pair Failed", Toast.LENGTH_SHORT).show();
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * Delete key pair
     *
     * @param callback
     * @param kpName
     */
    public void deleteKeyPair(final VolleyCallback callback, String kpName) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/os-keypairs/" + kpName;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

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
                        Toast.makeText(mApplicationContext, "Delete Key pair failed", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * List availability zone Http Request showing the availability zones
     *
     * @param callback
     * @param context
     */
    public void listAvailabilityZone(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = "https://nova.rc.nectar.org.au:8774/v2.1/abb64025bf354c8da099da4f5666dda3" + "/os-availability-zone";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray resultArray;
                        resultArray = ResponseParser.getInstance(mApplicationContext).listAvabilityZone(response);
                        String result = resultArray.toString();
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
                    Toast.makeText(mApplicationContext, "Getting avability zones Failed", Toast.LENGTH_SHORT).show();
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /**
     * List security group Http Request showing the available security groups
     *
     * @param callback
     * @param context
     */
    public void listSecurityGroup(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        System.out.println(networkServiceURL);
        String fullURL = networkServiceURL + "/v2.0/security-groups";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray resultArray;
                        resultArray = ResponseParser.getInstance(mApplicationContext).listSecurityGroup(response);
                        String result = resultArray.toString();
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
                    Toast.makeText(mApplicationContext, "Getting security groups Failed", Toast.LENGTH_SHORT).show();
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * Delete security group
     *
     * @param callback
     * @param sgID
     */
    public void deleteSecurityGroup(final VolleyCallback callback, String sgID) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/v2.0/security-groups/" + sgID;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

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
                    //Toast.makeText(mApplicationContext, "Delete  successfully", Toast.LENGTH_SHORT).show();
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * List rules for the specific security group the user clicks in the AccessAndSecurityFragment listview
     *
     * @param callback
     * @param context
     * @param sgId
     */
    public void listManageRuleSG(final VolleyCallback callback, final Context context, String sgId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/v2.0/security-groups/" + sgId;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        JSONArray resultArray;
                        resultArray = ResponseParser.getInstance(mApplicationContext).listRulesSG(response);
                        String result = resultArray.toString();
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
                }
                Toast.makeText(mApplicationContext, "Getting Rules Failed", Toast.LENGTH_SHORT).show();
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * Delete a rule
     *
     * @param callback
     * @param ruleID
     */
    public void deleteRuleSG(final VolleyCallback callback, String ruleID) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/v2.0/security-group-rules/" + ruleID;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /**
     * List alarm Http Request showing the created alarms of current project
     *
     * @param callback
     * @param context
     */
    public void listAlarmProject(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String alarmingServiceURL = sharedPreferences.getString("alarmingServiceURL", "Error Getting Compute URL");
        String tenant = sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String fullURL = alarmingServiceURL + "/v2/alarms";
        System.out.println("alarm_full: " + fullURL);

        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONArray resultArray;
                resultArray = ResponseParser.getInstance(mApplicationContext).listAlarm(response);
                String result = resultArray.toString();
                //System.out.println("result:aaaaa");
                //System.out.println(result);
                callback.onSuccess(result);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 401) {
                    Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(mApplicationContext, LoginActivity.class);
                    context.startActivity(i);
                    // contect extend long time
                } else {
                    Toast.makeText(mApplicationContext, "Getting Alarms Failed", Toast.LENGTH_SHORT).show();
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * List image Http Request showing the available images of current project
     *
     * @param callback
     * @param context
     */


    public void listImageProject(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String imageServiceURL = sharedPreferences.getString("imageServiceURL", "Error Getting Compute URL");
        String tenant = sharedPreferences.getString("tenantId", "Error Getting Compute URL");


        //String fullURL = imageServiceURL + "/v2/images?owner="+tenant;
        // api might be changed, the request is not response
        String fullURL = imageServiceURL + "/v2/images";

        System.out.println(fullURL);
        //System.out.println(sharedPreferences.getString("tenantId", "Error Getting Compute URL"));
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        JSONArray resultArray;
                        resultArray = ResponseParser.getInstance(mApplicationContext).listImage(response);
                        String result = resultArray.toString();
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
                    Toast.makeText(mApplicationContext, "Getting images Failed", Toast.LENGTH_SHORT).show();
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /**
     * List image Http Request showing the available images of NECTAR Official
     *
     * @param callback
     * @param context
     */

    public void listImageOfficial(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String imageServiceURL = sharedPreferences.getString("imageServiceURL", "Error Getting Compute URL");
        String owner = "28eadf5ad64b42a4929b2fb7df99275c";
        String fullURL = imageServiceURL + "/v2/images?owner=" + owner;

        //System.out.println(sharedPreferences.getString("imageServiceURL", "Error Getting Compute URL"));
        //System.out.println(sharedPreferences.getString("tenantId", "Error Getting Compute URL"));
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray resultArray;
                        resultArray = ResponseParser.getInstance(mApplicationContext).listImageOfficial(response);
                        String result = resultArray.toString();
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
                    Toast.makeText(mApplicationContext, "Getting images Failed", Toast.LENGTH_SHORT).show();
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /**
     * Get the detailed info of a specific image
     *
     * @param callback
     * @param context
     * @param id
     */

    public void showImageDetail(final VolleyCallback callback, final Context context, String id) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String imageServiceURL = sharedPreferences.getString("imageServiceURL", "Error Getting Compute URL");
        String fullURL = imageServiceURL + "/v2/images/" + id;

        //System.out.println(sharedPreferences.getString("imageServiceURL", "Error Getting Compute URL"));
        //System.out.println(sharedPreferences.getString("tenantId", "Error Getting Compute URL"));
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * Delete image
     *
     * @param callback
     * @param imageID
     */
    public void deleteImage(final VolleyCallback callback, String imageID) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("imageServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/v2/images/" + imageID;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

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
                        Toast.makeText(mApplicationContext, "Delete image failed", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * List Instance Detail for the specific instance the user clicks in the InstanceFragment listview
     *
     * @param callback
     * @param context
     * @param instanceId
     */
    public void listSingleInstance(final VolleyCallback callback, final Context context, String instanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/servers/" + instanceId;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /**
     * List available volume type
     *
     * @param callback
     * @param context
     */
    public void listVolumeType(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String tenant = sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String volumeV3ServiceURL = "https://cinder.rc.nectar.org.au:8776/v3/" + tenant;
        String fullURL = volumeV3ServiceURL + "/types";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        System.out.println(token);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray resultArray;
                        resultArray = ResponseParser.getInstance(mApplicationContext).listVolumeType(response);
                        String result = resultArray.toString();
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
                }
                Toast.makeText(mApplicationContext, "Listing Volumes Types Failed", Toast.LENGTH_SHORT).show();

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * List Volume Snapshot Http Request
     *
     * @param callback
     * @param context
     */
    public void listVolumeSnapshot(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String tenant = sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String volumeV3ServiceURL = "https://cinder.rc.nectar.org.au:8776/v3/" + tenant;
        String fullURL = volumeV3ServiceURL + "/snapshots/detail";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray resultArray;
                        resultArray = ResponseParser.getInstance(mApplicationContext).listSnapshot(response);
                        String result = resultArray.toString();
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
                }
                Toast.makeText(mApplicationContext, "Listing Snapshots Failed", Toast.LENGTH_SHORT).show();

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * List Volume Http Request
     *
     * @param callback
     * @param context
     */
    public void listVolume(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String tenant = sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String volumeV3ServiceURL = "https://cinder.rc.nectar.org.au:8776/v3/" + tenant;
        String fullURL = volumeV3ServiceURL + "/volumes/detail";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray resultArray;
                        resultArray = ResponseParser.getInstance(mApplicationContext).listVolume(response);
                        String result = resultArray.toString();
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
                }
                Toast.makeText(mApplicationContext, "Listing Volumes Failed", Toast.LENGTH_SHORT).show();

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * Get the detailed info of a specific volume snapshot
     *
     * @param callback
     * @param context
     * @param snapshotid
     */

    public void showVolumeSnapshotDetail(final VolleyCallback callback, final Context context, String snapshotid) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String tenant = sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String volumeV3ServiceURL = "https://cinder.rc.nectar.org.au:8776/v3/" + tenant;
        String fullURL = volumeV3ServiceURL + "/snapshots/" + snapshotid;

        //System.out.println(sharedPreferences.getString("imageServiceURL", "Error Getting Compute URL"));
        //System.out.println(sharedPreferences.getString("tenantId", "Error Getting Compute URL"));
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject resultObject;
                        resultObject = ResponseParser.getInstance(mApplicationContext).listVolumeSnapshotDetail(response);
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
                    Toast.makeText(mApplicationContext, "Getting snapshot detail Failed", Toast.LENGTH_SHORT).show();
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * Get the detailed info of a specific volume
     *
     * @param callback
     * @param context
     * @param volumeid
     */

    public void showVolumeDetail(final VolleyCallback callback, final Context context, String volumeid) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String tenant = sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String volumeV3ServiceURL = "https://cinder.rc.nectar.org.au:8776/v3/" + tenant;
        String fullURL = volumeV3ServiceURL + "/volumes/" + volumeid;

        //System.out.println(sharedPreferences.getString("imageServiceURL", "Error Getting Compute URL"));
        //System.out.println(sharedPreferences.getString("tenantId", "Error Getting Compute URL"));
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject resultObject;
                        resultObject = ResponseParser.getInstance(mApplicationContext).listVolumeDetail(response);
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * Server action: Pause Http Request
     *
     * @param callback
     * @param instanceId
     */
    public void pause(final VolleyCallback callback, String instanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/servers/" + instanceId + "/action";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json = new JSONObject();
        try {
            json.put("pause", JSONObject.NULL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                /**
                 * Successful Response is null so we have to separate it from the real Errors
                 */
                if (error.networkResponse == null) {
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    } else {
                        Toast.makeText(mApplicationContext, "Network Error", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * Server action: Unpause Http Request
     *
     * @param callback
     * @param instanceId
     */
    public void unpause(final VolleyCallback callback, String instanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/servers/" + instanceId + "/action";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json = new JSONObject();
        try {
            json.put("unpause", JSONObject.NULL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
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
                        Toast.makeText(mApplicationContext, "Network Error", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    /**
     * Server action: Stop Http Request
     *
     * @param callback
     * @param instanceId
     */
    public void stop(final VolleyCallback callback, String instanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/servers/" + instanceId + "/action";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json = new JSONObject();
        try {
            json.put("os-stop", JSONObject.NULL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
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
                        Toast.makeText(mApplicationContext, "Network Error", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    /**
     * Server action: Pause Start Request
     *
     * @param callback
     * @param instanceId
     */
    public void start(final VolleyCallback callback, String instanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/servers/" + instanceId + "/action";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json = new JSONObject();
        try {
            json.put("os-start", JSONObject.NULL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
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
                        Toast.makeText(mApplicationContext, "Network Error", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * Server action: Suspend Http Request
     *
     * @param callback
     * @param instanceId
     */
    public void suspend(final VolleyCallback callback, String instanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/servers/" + instanceId + "/action";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json = new JSONObject();
        try {
            json.put("suspend", JSONObject.NULL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
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
                        Toast.makeText(mApplicationContext, "Network Error", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    /**
     * Server action: Resume Http Request
     *
     * @param callback
     * @param instanceId
     */
    public void resume(final VolleyCallback callback, String instanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/servers/" + instanceId + "/action";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json = new JSONObject();
        try {
            json.put("resume", JSONObject.NULL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
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
                        Toast.makeText(mApplicationContext, "Network Error", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * Server action: Reboot Http Request
     *
     * @param callback
     * @param instanceId
     */
    public void reboot(final VolleyCallback callback, String instanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/servers/" + instanceId + "/action";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("type", "HARD");
            json1.put("reboot", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json1,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
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
                        Toast.makeText(mApplicationContext, "Network Error", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * Server action: Delete Http Request
     *
     * @param callback
     * @param instanceId
     */
    public void delete(final VolleyCallback callback, String instanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/servers/" + instanceId + "/action";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json = new JSONObject();
        try {
            json.put("forceDelete", JSONObject.NULL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
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
                        Toast.makeText(mApplicationContext, "Network Error", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * Server action: Snapshot Http Request
     *
     * @param callback
     * @param instanceId
     * @param snapshotName
     */
    public void snapshot(final VolleyCallback callback, String instanceId, String snapshotName) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/servers/" + instanceId + "/action";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        JSONObject json3 = new JSONObject();
        try {
            json3.put("meta_var", "meta_val");
            json2.put("metadata", json3);
            json2.put("name", snapshotName);
            json1.put("createImage", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json1,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
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
                        Toast.makeText(mApplicationContext, "Network Error", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * Luanch a new server
     *
     * @param callback
     * @param name
     * @param flavor
     * @param image
     * @param kp
     * @param az
     * @param sg
     */
    public void launchServer(final VolleyCallback callback, String name, String flavor, String image, String kp, String az, List<String> sg) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/servers";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONArray sgArray = new JSONArray();
        ;
        if (sg.size() != 0) {
            for (int i = 0; i < sg.size(); i++) {
                JSONObject sgChoose = new JSONObject();
                try {
                    sgChoose.put("name", sg.get(i));
                    sgArray.put(sgChoose);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("name", name);
            json2.put("imageRef", image);
            json2.put("flavorRef", flavor);
            if (az != "Select Availability Zone please") {
                json2.put("availability_zone", az);
            }
            if (kp != "Select Key pair please") {
                json2.put("key_name", kp);
            }
            if (sg.size() != 0) {
                json2.put("security_groups", sgArray);
            }
            json1.put("server", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(json1);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json1,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("dedededededededddededed");
                        callback.onSuccess("success");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    System.out.println("dedededededededddededed");
                    Toast.makeText(mApplicationContext, "Create instance successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    } else {
                        Toast.makeText(mApplicationContext, "Fail to create instance", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * create a new security group
     *
     * @param callback
     * @param name
     * @param description
     */
    public void createSecurityGroup(final VolleyCallback callback, String name, String description) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/v2.0/security-groups";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("name", name);
            json2.put("description", description);
            json1.put("security_group", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(json1);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json1,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("dedededededededddededed");
                        callback.onSuccess("success");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    System.out.println("dedededededededddededed2");
                    //Toast.makeText(mApplicationContext, "Create successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    } else {
                        Toast.makeText(mApplicationContext, "Fail to create", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * edit an existing security group
     *
     * @param callback
     * @param sgid
     * @param name
     * @param description
     */
    public void editSecurityGroup(final VolleyCallback callback, String sgid, String name, String description) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/v2.0/security-groups/" + sgid;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("name", name);
            json2.put("description", description);
            json1.put("security_group", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(json1);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, fullURL, json1,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("dedededededededddededed");
                        callback.onSuccess("success");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    System.out.println("dedededededededddededed");
                    Toast.makeText(mApplicationContext, "Create successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    } else {
                        Toast.makeText(mApplicationContext, "Fail to create", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * import a key pair with a public key
     *
     * @param callback
     * @param name
     * @param publicKey
     */
    public void importKeyPair(final VolleyCallback callback, String name, String publicKey) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/os-keypairs";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("name", name);
            json2.put("public_key", publicKey);
            json1.put("keypair", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(json1);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json1,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //System.out.println("dedededededededddededed");
                        callback.onSuccess("success");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    System.out.println("dedededededededddededed");
                    Toast.makeText(mApplicationContext, "Import successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    } else {
                        Toast.makeText(mApplicationContext, "Fail to import", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    /**
     * Create a new key pair
     *
     * @param callback
     * @param name
     */
    public void createKeyPair(final VolleyCallback callback, String name) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/os-keypairs";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("name", name);
            json1.put("keypair", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(json1);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json1,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //System.out.println("dedededededededddededed");
                        callback.onSuccess("success");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    System.out.println("dedededededededddededed");
                    Toast.makeText(mApplicationContext, "Create successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    } else {
                        Toast.makeText(mApplicationContext, "Fail to create", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * Luanch a new server
     *
     * @param callback
     * @param sgID
     * @param protocol
     * @param dir
     * @param minPort
     * @param maxPort
     * @param cidr
     * @param ethertype
     */
    public void addNewRule(final VolleyCallback callback, String sgID, String protocol, String dir, String minPort, String maxPort, String cidr, String ethertype) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/v2.0/security-group-rules";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        System.out.println(fullURL);
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("security_group_id", sgID);
            json2.put("protocol", protocol);
            json2.put("direction", dir);
            json2.put("port_range_min", minPort);
            json2.put("port_range_max", maxPort);
            json2.put("remote_ip_prefix", cidr);
            json2.put("ethertype", ethertype);

            json1.put("security_group_rule", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(json1.toString());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json1,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("dedededededededddededed");
                        callback.onSuccess("success");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    System.out.println("dedededededededddededed");
                    Toast.makeText(mApplicationContext, "Add successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    } else {
                        Toast.makeText(mApplicationContext, "Fail to add", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    /**
     * attach a volume to a instance
     *
     * @param callback
     * @param instanceID
     * @param mountpoint
     * @param volumeid
     */
    public void attachVolume(final VolleyCallback callback, String instanceID, String mountpoint, String volumeid) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/servers/" + instanceID + "/os-volume_attachments";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("volumeId", volumeid);
            json2.put("device", mountpoint);
            json1.put("volumeAttachment", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(json1.toString());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json1,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //System.out.println("dedededededededddededed");
                        callback.onSuccess("success");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    System.out.println("dedededededededddededed");
                    //Toast.makeText(mApplicationContext, "Attach successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    } else {
                        Toast.makeText(mApplicationContext, "Fail to attach", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    /**
     * attach a volume to a instance
     *
     * @param callback
     * @param attachID
     * @param serverid
     */
    public void detachVolume(final VolleyCallback callback, String attachID, String serverid) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/servers/" + serverid + "/os-volume_attachments/" + attachID;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        //final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

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
                    Toast.makeText(mApplicationContext, "Detach  successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    } else {
                        Toast.makeText(mApplicationContext, "Detach failed", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * edit an existing volume
     *
     * @param callback
     * @param name
     * @param description
     * @param volumeid
     */
    public void editVolume(final VolleyCallback callback, String name, String description, String volumeid) {
        String tenant = sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String volumeV3ServiceURL = "https://cinder.rc.nectar.org.au:8776/v3/" + tenant;
        String fullURL = volumeV3ServiceURL + "/volumes/" + volumeid;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("name", name);
            json2.put("description", description);
            json1.put("volume", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(json1);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, fullURL, json1,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("dedededededededddededed");
                        callback.onSuccess("success");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    System.out.println("dedededededededddededed");
                    Toast.makeText(mApplicationContext, "Edit V successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    } else {
                        Toast.makeText(mApplicationContext, "Fail to edit V", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    /**
     * extend the size of a volume
     *
     * @param callback
     * @param newSize
     * @param volumeid
     */
    public void extendVolume(final VolleyCallback callback, int newSize, String volumeid) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String tenant = sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String volumeV3ServiceURL = "https://cinder.rc.nectar.org.au:8776/v3/" + tenant;
        String fullURL = volumeV3ServiceURL + "/volumes/" + volumeid + "/action";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("new_size", newSize);
            json1.put("os-extend", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(json1.toString());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json1,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //System.out.println("dedededededededddededed");
                        callback.onSuccess("success");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    System.out.println("dedededededededddededed");
                    //Toast.makeText(mApplicationContext, "Extend V successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    } else {
                        Toast.makeText(mApplicationContext, "Fail to extend V", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    /**
     * Delete volume
     *
     * @param callback
     * @param volumeID
     */
    public void deleteVolume(final VolleyCallback callback, String volumeID) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String tenant = sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String volumeV3ServiceURL = "https://cinder.rc.nectar.org.au:8776/v3/" + tenant;
        String fullURL = volumeV3ServiceURL + "/volumes/" + volumeID;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

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
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /**
     * create a snapshot based on an existing volume
     *
     * @param callback
     * @param name
     * @param description
     * @param volumeid
     */
    public void createVolumeSnapshot(final VolleyCallback callback, String name, String description, String volumeid) {
        String tenant = sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String volumeV3ServiceURL = "https://cinder.rc.nectar.org.au:8776/v3/" + tenant;
        String fullURL = volumeV3ServiceURL + "/snapshots";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("name", name);
            json2.put("volume_id", volumeid);
            json2.put("description", description);
            json1.put("snapshot", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(json1);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json1,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("dedededededededddededed");
                        callback.onSuccess("success");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    System.out.println("dedededededededddededed");
                    Toast.makeText(mApplicationContext, "create snapshot successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    } else {
                        Toast.makeText(mApplicationContext, "Fail to create snapshot", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    /**
     * Luanch a new server
     *
     * @param callback
     * @param name
     * @param description
     * @param size
     * @param zone
     * @param type
     */
    public void createVolume(final VolleyCallback callback, String name, String description, int size, String zone, String type) {
        String tenant = sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String volumeV3ServiceURL = "https://cinder.rc.nectar.org.au:8776/v3/" + tenant;
        String fullURL = volumeV3ServiceURL + "/volumes";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        System.out.println(token);
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("size", size);
            json2.put("availability_zone", zone);
            json2.put("description", description);
            json2.put("name", name);
            json2.put("volume_type", type);
            JSONObject json3 = new JSONObject();
            json2.put("metadata", json3);
            json2.put("consistencygroup_id", null);

            json1.put("volume", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(json1.toString());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json1,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        callback.onSuccess("success");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {

                    Toast.makeText(mApplicationContext, "Create successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    } else {
                        Toast.makeText(mApplicationContext, "Fail to Create", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    /**
     * Delete volume snapshot
     *
     * @param callback
     * @param snapshotID
     */
    public void deleteVolumeSnapshot(final VolleyCallback callback, String snapshotID) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String tenant = sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String volumeV3ServiceURL = "https://cinder.rc.nectar.org.au:8776/v3/" + tenant;
        String fullURL = volumeV3ServiceURL + "/snapshots/" + snapshotID;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

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
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * edit an existing volume snapshot
     *
     * @param callback
     * @param name
     * @param description
     * @param volumeSnapshotid
     */
    public void editVolumeSnapshot(final VolleyCallback callback, String name, String description, String volumeSnapshotid) {
        String tenant = sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String volumeV3ServiceURL = "https://cinder.rc.nectar.org.au:8776/v3/" + tenant;
        String fullURL = volumeV3ServiceURL + "/snapshots/" + volumeSnapshotid;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("name", name);
            json2.put("description", description);
            json1.put("snapshot", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(json1);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, fullURL, json1,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("dedededededededddededed");
                        callback.onSuccess("success");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    //System.out.println("dedededededededddededed");
                    Toast.makeText(mApplicationContext, "Edit VS successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    } else {
                        Toast.makeText(mApplicationContext, "Fail to edit VS", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    public void createAlarm(final VolleyCallback callback, String name, String description, String type,
                            String metric, int threshold, String method, String operator, int granularity, String state,
                            String severity) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String alarmServiceURL = sharedPreferences.getString("alarmingServiceURL", "Error Getting alarmServiceURL");
        String fullURL = alarmServiceURL + "/v2/alarms";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        System.out.println("alarm_token: " + token);
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();

        try {
            json1.put("name", name);
            json1.put("description", description);
            json1.put("type", type);


            json2.put("metric", metric);
            json2.put("resource_id", "INSTANCE_ID");
            json2.put("resource_type", "instance");
            json2.put("threshold", threshold);
            json2.put("aggregation_method", method);
            json2.put("comparison_operator", operator);
            json2.put("granularity", granularity);
            json2.put("evaluation_periods", 3);

            json1.put("gnocchi_resources_threshold_rule", json2);
            json1.put("state", state);
            json1.put("severity", severity);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        System.out.println("create alarm: " + json1);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json1, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                System.out.println("dedededededededddedededaaaaaa");
                callback.onSuccess("success");
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
                        Toast.makeText(mApplicationContext, "Fail to create", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                //headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        System.out.println("work here");
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);


    }

    public void deleteAlarm(final VolleyCallback callback, String alarmID) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String alarmingServiceURL = sharedPreferences.getString("alarmingServiceURL", "Error Getting Compute URL");
        String fullURL = alarmingServiceURL + "/v2/alarms/" + alarmID;
        System.out.println(fullURL);
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, fullURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.onSuccess("success");

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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);

    }

    public void listContainer(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String objectStorageServiceURL = sharedPreferences.getString("objectStorageServiceURL", "Error Getting Compute URL");
        String fullURL = objectStorageServiceURL + "?format=json";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        System.out.println("container: " + fullURL);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONArray resultArray;
                resultArray = ResponseParser.getInstance(mApplicationContext).listcontainer(response);
                String result = resultArray.toString();
                System.out.println("Con_Result: " + result);
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    public void publicContainer(final VolleyCallback callback, String containerName) {

        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String objectStorageServiceURL = sharedPreferences.getString("objectStorageServiceURL", "Error Getting Compute URl");
        String fullURL = objectStorageServiceURL + "/" + containerName;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        StringRequest stringRequest = new StringRequest(Request.Method.PUT, fullURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(mApplicationContext, "Public successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "Public container failed", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("X-Container-Read", ".r:*");
                return headers;
            }
        };

        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);


    }

    public void privateContainer(final VolleyCallback callback, String containerName) {

        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String objectStorageServiceURL = sharedPreferences.getString("objectStorageServiceURL", "Error Getting Compute URl");
        String fullURL = objectStorageServiceURL + "/" + containerName;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        StringRequest stringRequest = new StringRequest(Request.Method.PUT, fullURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(mApplicationContext, "Private successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "Private container failed", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("X-Container-Read", "");
                return headers;
            }
        };

        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);


    }

    public void deleteContainer(final VolleyCallback callback, String containerName) {

        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String objectStorageServiceURL = sharedPreferences.getString("objectStorageServiceURL", "Error Getting Compute URl");
        String fullURL = objectStorageServiceURL + "/" + containerName;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, fullURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(mApplicationContext, "Delete successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "Delete container failed", Toast.LENGTH_SHORT).show();
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

        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);


    }

    public void listObject(final VolleyCallback callback, final Context context, String containerName) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String objectStorageServiceURL = sharedPreferences.getString("objectStorageServiceURL", "Error Getting Compute URL");
        String fullURL = objectStorageServiceURL + "/" + containerName + "?format=json";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        System.out.println("con_token: " + token);
        System.out.println("container: " + fullURL);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONArray resultArray;
                resultArray = ResponseParser.getInstance(mApplicationContext).listObject(response);
                String result = resultArray.toString();
                System.out.println("Con_Result: " + result);
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
                        Toast.makeText(mApplicationContext, "list failed", Toast.LENGTH_SHORT).show();
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    public void deleteObject(final VolleyCallback callback, String containerName, String ObjectName) {

        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String objectStorageServiceURL = sharedPreferences.getString("objectStorageServiceURL", "Error Getting Compute URl");
        String fullURL = objectStorageServiceURL + "/" + containerName + "/" + ObjectName;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, fullURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(mApplicationContext, "Delete successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "Delete object failed", Toast.LENGTH_SHORT).show();
                        System.out.println("deleteFail");
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

        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);


    }

    public void createFolder(final VolleyCallback callback, String containerName, String ObjectName) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String objectStorageServiceURL = sharedPreferences.getString("objectStorageServiceURL", "Error Getting Compute URl");
        String fullURL = objectStorageServiceURL + "/" + containerName + "/" + ObjectName;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        StringRequest stringRequest = new StringRequest(Request.Method.PUT, fullURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(mApplicationContext, "Create successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "create object failed", Toast.LENGTH_SHORT).show();
                        System.out.println("createFail");
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

        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);

    }

    public void createContainer(final VolleyCallback callback, String containerName, String access) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String objectStorageServiceURL = sharedPreferences.getString("objectStorageServiceURL", "Error Getting Compute URl");
        String fullURL = objectStorageServiceURL + "/" + containerName;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        final String accessValue;
        if (access.equals("Private")) {
            accessValue = "";
        } else {
            accessValue = ".r:*";
        }

        StringRequest stringRequest = new StringRequest(Request.Method.PUT, fullURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(mApplicationContext, "Create successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "create object failed", Toast.LENGTH_SHORT).show();
                        System.out.println("createFail");
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("X-Container-Read", accessValue);
                return headers;
            }
        };

        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);

    }

    public void createObjectFile(final VolleyCallback callback, String containerName, String ObjectName) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String objectStorageServiceURL = sharedPreferences.getString("objectStorageServiceURL", "Error Getting Compute URl");
        String fullURL = objectStorageServiceURL + "/" + containerName + "/" + ObjectName;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        StringRequest stringRequest = new StringRequest(Request.Method.PUT, fullURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(mApplicationContext, "Create successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "create object failed", Toast.LENGTH_SHORT).show();
                        System.out.println("createFail");
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

        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);

    }

    public void copyObject(final VolleyCallback callback, final String preContainer, final String preObjectName, String destionationContainer, String path, String newObjectName) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String objectStorageServiceURL = sharedPreferences.getString("objectStorageServiceURL", "Error Getting Compute URl");
        String fullURL = objectStorageServiceURL + "/" + destionationContainer + "/" + path + newObjectName;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        StringRequest stringRequest = new StringRequest(Request.Method.PUT, fullURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(mApplicationContext, "Create successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "create object failed", Toast.LENGTH_SHORT).show();
                        System.out.println("createFail");
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("X-Copy-From", preContainer + "/" + preObjectName);
                return headers;
            }
        };

        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);

    }

    public void listFloatingIP(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String tenant = sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String fullURL = networkServiceURL + "v2.0/floatingips";

        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONArray resultArray;
                resultArray = ResponseParser.getInstance(mApplicationContext).listFloatingIP(response);
                String result = resultArray.toString();
                callback.onSuccess(result);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 401) {
                    Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(mApplicationContext, LoginActivity.class);
                    context.startActivity(i);
                } else {
                    Toast.makeText(mApplicationContext, "Getting Floating IP Failed", Toast.LENGTH_SHORT).show();

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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    public void deleteFloatingIP(final VolleyCallback callback, String floatingID) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        System.out.println("floathaha: " + token);
        String fullURL = networkServiceURL + "v2.0/floatingips/" + floatingID;

        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, fullURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(mApplicationContext, "Release successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "Release Floating IP failed", Toast.LENGTH_SHORT).show();
                        System.out.println("releaseFail");
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

        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    public void createFloatingIP(final VolleyCallback callback, String floating_network_id) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String fullURL = networkServiceURL + "v2.0/floatingips";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();


        try {
            json2.put("floating_network_id", floating_network_id);
            json1.put("floatingip", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json1, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(mApplicationContext, "Allocate successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "Allocate Floating IP failed", Toast.LENGTH_SHORT).show();
                        System.out.println("createFail");
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    public void listRouter(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String tenant = sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String fullURL = networkServiceURL + "v2.0/routers";

        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONArray resultArray;
                resultArray = ResponseParser.getInstance(mApplicationContext).listRouter(response);
                String result = resultArray.toString();
                callback.onSuccess(result);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 401) {
                    Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(mApplicationContext, LoginActivity.class);
                    context.startActivity(i);
                } else {
                    Toast.makeText(mApplicationContext, "Getting Router  Failed", Toast.LENGTH_SHORT).show();

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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    public void deleteRouter(final VolleyCallback callback, String routerID) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        String fullURL = networkServiceURL + "v2.0/routers/" + routerID;

        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, fullURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(mApplicationContext, "Delete Router successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "Delete  failed", Toast.LENGTH_SHORT).show();

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

        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    public void createRouter(final VolleyCallback callback, String routerName, String networkID, boolean admin_state) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String fullURL = networkServiceURL + "v2.0/routers";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        JSONObject json3 = new JSONObject();

        try {
            json3.put("network_id", networkID);
            json2.put("external_gateway_info", json3);
            json2.put("name", routerName);
            json2.put("admin_state_up", admin_state);
            json1.put("router", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json1, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(mApplicationContext, "Create Router successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "Create Router failed", Toast.LENGTH_SHORT).show();
                        //System.out.println("createFail");
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);

    }

    public void listNetwork(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String tenant = sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String fullURL = networkServiceURL + "v2.0/networks";

        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONArray resultArray;
                resultArray = ResponseParser.getInstance(mApplicationContext).listNetwork(response);
                String result = resultArray.toString();
                callback.onSuccess(result);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 401) {
                    Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(mApplicationContext, LoginActivity.class);
                    context.startActivity(i);
                } else {
                    Toast.makeText(mApplicationContext, "Getting Network  Failed", Toast.LENGTH_SHORT).show();

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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    public void listSubnet(final VolleyCallback callback, final Context context, final String networkID) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String tenant = sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String fullURL = networkServiceURL + "v2.0/subnets";

        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONArray resultArray;
                resultArray = ResponseParser.getInstance(mApplicationContext).listSubnet(response, networkID);
                System.out.println("networkID: " + networkID);
                String result = resultArray.toString();
                callback.onSuccess(result);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 401) {
                    Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(mApplicationContext, LoginActivity.class);
                    context.startActivity(i);
                } else {
                    Toast.makeText(mApplicationContext, "Getting Subnet  Failed", Toast.LENGTH_SHORT).show();

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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    public void deleteNetwork(final VolleyCallback callback, String networkID) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        String fullURL = networkServiceURL + "v2.0/networks/" + networkID;

        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, fullURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(mApplicationContext, "Delete successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "Delete Router failed", Toast.LENGTH_SHORT).show();

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

        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    public void createNetwork(final VolleyCallback callback, String networkName, boolean admin_state) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String fullURL = networkServiceURL + "v2.0/networks";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();


        try {

            json2.put("name", networkName);
            json2.put("admin_state_up", admin_state);
            json1.put("network", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json1, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(mApplicationContext, "Create Network successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "Create Network failed", Toast.LENGTH_SHORT).show();
                        //System.out.println("createFail");
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);

    }

    public void deleteSubnet(final VolleyCallback callback, String SubnetID) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        String fullURL = networkServiceURL + "v2.0/subnets/" + SubnetID;

        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, fullURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(mApplicationContext, "Delete Subnet successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "Delete  failed", Toast.LENGTH_SHORT).show();

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

        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    public void createSubnet(final VolleyCallback callback, String subnetName, String networkID, String networkAddress, int version) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String fullURL = networkServiceURL + "v2.0/subnets";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();


        try {

            json2.put("name", subnetName);
            json2.put("cidr", networkAddress);
            json2.put("ip_version", version);
            json2.put("network_id", networkID);
            json1.put("subnet", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json1, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(mApplicationContext, "Create Subnet successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "Create Subnet failed", Toast.LENGTH_SHORT).show();
                        //System.out.println("createFail");
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);

    }


    ////////////////////////
    // test for update


    /*
    * list ports
    * */
    public void listPort(final VolleyCallback callback, final Context context, final String networkID) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String tenant = sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String fullURL = networkServiceURL + "v2.0/ports";

        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONArray resultArray;
                resultArray = ResponseParser.getInstance(mApplicationContext).listPort(response, networkID);
                System.out.println("networkID: " + networkID);
                String result = resultArray.toString();
                callback.onSuccess(result);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 401) {
                    Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(mApplicationContext, LoginActivity.class);
                    context.startActivity(i);
                } else {
                    Toast.makeText(mApplicationContext, "Getting Subnet  Failed", Toast.LENGTH_SHORT).show();

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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /*
    * delete port
    * */
    public void deletePort(final VolleyCallback callback, String portID) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        String fullURL = networkServiceURL + "v2.0/ports/" + portID;

        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, fullURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(mApplicationContext, "Delete Subnet successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "Delete  failed", Toast.LENGTH_SHORT).show();
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

        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /*
    * create port
    * */
    public void createPort(final VolleyCallback callback, String portName, String networkID, boolean admin_state) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String fullURL = networkServiceURL + "v2.0/ports";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();


        try {
            json2.put("name", portName);
            json2.put("admin_state_up", admin_state);
            json2.put("network_id", networkID);
            json1.put("port", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
                        //System.out.println("createFail");
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);

    }



    // show resource type detail
    public void showResourceTypesDetail(final VolleyCallback callback, final Context context, String id) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String orchestrationServiceURL = sharedPreferences.getString("orchestrationServiceURL", "Error Getting Compute URL");
        String fullURL = orchestrationServiceURL + "/resource_types/" + id;

        //System.out.println(sharedPreferences.getString("imageServiceURL", "Error Getting Compute URL"));
        //System.out.println(sharedPreferences.getString("tenantId", "Error Getting Compute URL"));
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        System.out.println(response);

                        //////////////////
                        String path = context.getFilesDir().getPath().toString() ;
                        Log.d(TAG, path);
//                        writeTxtToFile(response,path,"test.txt");
                        JSONObject resultObject;
                        resultObject = ResponseParser.getInstance(mApplicationContext).listResourceTypesDetail(response);
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /////////////////////////////////////////
    //////list resource types
    public void listResourceTypes(final VolleyCallback callback, final Context context)   {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String tenant = sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String orchestrationServiceURL = sharedPreferences.getString("orchestrationServiceURL", "ERROR");
        String resourceTypesURL = orchestrationServiceURL;
        System.out.println("resourcesType "+resourceTypesURL);

        String fullURL = orchestrationServiceURL + "/resource_types";


        System.out.println(fullURL);
        //System.out.println(sharedPreferences.getString("tenantId", "Error Getting Compute URL"));
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");


        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        JSONArray resultArray;
//                        System.out.println(response);
                        //////////////////
                        //write json data to txt file
//                        String path = context.getFilesDir().getPath().toString() ;
//                        Log.d(TAG, path);
//                        writeTxtToFile(response,path,"test.txt");
                        ////////////////////

                        resultArray = ResponseParser.getInstance(mApplicationContext).listResourceTypes(response);

                        String result = resultArray.toString();
//                        System.out.print("testoutput" + result);
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
                    Toast.makeText(mApplicationContext, "Getting images Failed", Toast.LENGTH_SHORT).show();
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /////////////////
    // show template versions

    public void listTemplateVersions(final VolleyCallback callback, final Context context)     {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String tenant = sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String orchestrationServiceURL = sharedPreferences.getString("orchestrationServiceURL", "ERROR");
        String templateVersionsURL = orchestrationServiceURL;
        System.out.println("templateVersions "+templateVersionsURL);

        String fullURL = templateVersionsURL + "/template_versions";


        System.out.println(fullURL);
        //System.out.println(sharedPreferences.getString("tenantId", "Error Getting Compute URL"));
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");


        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        JSONArray resultArray;
//                        System.out.println(response);
                        //////////////////
                        //write json data to txt file
//                        String path = context.getFilesDir().getPath().toString() ;
//                        Log.d(TAG, path);
//                        writeTxtToFile(response,path,"test.txt");
                        ////////////////////

                        resultArray = ResponseParser.getInstance(mApplicationContext).listTemplateVersions(response);

                        String result = resultArray.toString();
//                        System.out.print("testoutput" + result);
                        callback.onSuccess(result);// Display the first 500 characters of the response string.
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 401) {
                    Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(mApplicationContext, LoginActivity.class);
                    context.startActivity(i);
                } else {
                    Toast.makeText(mApplicationContext, "Getting images Failed", Toast.LENGTH_SHORT).show();
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    ////////////////////
    // show template version detail
    public void showTemplateVersionDetail(final VolleyCallback callback, final Context context, String id) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String orchestrationServiceURL = sharedPreferences.getString("orchestrationServiceURL", "Error Getting Compute URL");
        String fullURL = orchestrationServiceURL + "/template_versions/" + id + "/functions";

        //System.out.println(sharedPreferences.getString("imageServiceURL", "Error Getting Compute URL"));
        //System.out.println(sharedPreferences.getString("tenantId", "Error Getting Compute URL"));
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


//                        System.out.println(response);


                        JSONArray resultArray;

                        //////////////////
                        //write json data to txt file
//                        String path = context.getFilesDir().getPath().toString() ;
//                        Log.d(TAG, path);
//                        writeTxtToFile(response,path,"test.txt");
                        ////////////////////

                        resultArray = ResponseParser.getInstance(mApplicationContext).listTemplateVersionsDetail(response);

                        String result = resultArray.toString();
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /////////////////////
    //show stacks list
    public void listStacks(final VolleyCallback callback, final Context context)     {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String tenant = sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String orchestrationServiceURL = sharedPreferences.getString("orchestrationServiceURL", "ERROR");

        String fullURL = orchestrationServiceURL + "/stacks";


//        System.out.println(fullURL);
        //System.out.println(sharedPreferences.getString("tenantId", "Error Getting Compute URL"));
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");


        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        JSONArray resultArray;
//                        System.out.println(response);
                        //////////////////
                        //write json data to txt file
//                        String path = context.getFilesDir().getPath().toString() ;
//                        Log.d(TAG, path);
//                        writeTxtToFile(response,path,"test.txt");
                        ////////////////////

                        resultArray = ResponseParser.getInstance(mApplicationContext).listStacks(response);

                        String result = resultArray.toString();
//                        System.out.print("testoutput" + result);
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
                    Toast.makeText(mApplicationContext, "Getting images Failed", Toast.LENGTH_SHORT).show();
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    ////////////////////////
    // list stack detail and actions
    public void listSingleStack(final VolleyCallback callback, final Context context, String stackName, String stackId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String orchestrationServiceURL = sharedPreferences.getString("orchestrationServiceURL", "Error Getting Compute URL");
        String fullURL = orchestrationServiceURL + "/stacks/" + stackName +"/"+stackId;
        Log.d("FULLURL_stack detail", fullURL);
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
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
                            String description = resp.getJSONObject("stack").getString("description");
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
//                            result.put("key", key);
//                            result.put("securityg", sg);
//                            result.put("volNum", vNum);
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }



    /////////////////////
    //suspend stack
    public void suspendStack(final VolleyCallback callback, String stackName, String stackId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String orchestrationServiceURL = sharedPreferences.getString("orchestrationServiceURL", "Error Getting Compute URL");
        String fullURL = orchestrationServiceURL + "/stacks/" + stackName +"/"+stackId +"/actions";
        Log.d("FULLURL_stack detail", fullURL);
//        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json = new JSONObject();
        try {
            json.put("suspend", JSONObject.NULL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String jsontest = json.toString();
        Log.d("Json_sent", jsontest);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
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
                        Toast.makeText(mApplicationContext, "Network Error", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
//                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    ///////////////
    // resume stack
    public void resumeStack(final VolleyCallback callback, String stackName, String stackId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String orchestrationServiceURL = sharedPreferences.getString("orchestrationServiceURL", "Error Getting Compute URL");
//        String fullURL = orchestrationServiceURL + "/servers/" + instanceId + "/action";
        String fullURL = orchestrationServiceURL + "/stacks/" + stackName +"/"+stackId + "/actions";
        Log.d("FULLURL_stack detail", fullURL);
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json = new JSONObject();
        try {
            json.put("resume", JSONObject.NULL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
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
                        Toast.makeText(mApplicationContext, "Network Error", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
//                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    //////////////////
    // check stack
    public void checkStack(final VolleyCallback callback, String stackName, String stackId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String orchestrationServiceURL = sharedPreferences.getString("orchestrationServiceURL", "Error Getting Compute URL");
        String fullURL = orchestrationServiceURL + "/stacks/" + stackName +"/"+stackId + "/actions";
        Log.d("FULLURL_stack detail", fullURL);
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json = new JSONObject();
        try {
            json.put("check", JSONObject.NULL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
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
                        Toast.makeText(mApplicationContext, "Network Error", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
//                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    //////////////////
    // delete stack
    public void deleteStack(final VolleyCallback callback, String stackName, String stackId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String orchestrationServiceURL = sharedPreferences.getString("orchestrationServiceURL", "Error Getting Compute URL");
        String fullURL = orchestrationServiceURL + "/stacks/" + stackName +"/"+stackId ;
        Log.d("FULLURL_stack detail", fullURL);
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
//        JSONObject json = new JSONObject();
//        try {
//            json.put("forceDelete", JSONObject.NULL);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json,
//                new Response.Listener<JSONObject>() {
//
//                    @Override
//                    public void onResponse(JSONObject response) {
//                    }
//                }, new Response.ErrorListener() {
        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, fullURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
//                    Toast.makeText(mApplicationContext, "Delete Stack successfully", Toast.LENGTH_SHORT).show();
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
//                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }



    ////////////////////
    // create stack
    public void createStack(final VolleyCallback callback, String stackName, String templateSource, Integer timeoutMins) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String orchestrationServiceURL = sharedPreferences.getString("orchestrationServiceURL", "Error Getting Compute URL");
        String fullURL = orchestrationServiceURL + "/stacks";
        Log.d("FULLURL_stack detail", fullURL);
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        JSONObject json3 = new JSONObject();

        try {
//            json3.put("network_id", networkID);
            json2.put("stack_name", stackName);
            json2.put("template_url", templateSource);
            json2.put("timeout_mins", timeoutMins);
//            json1.put("router", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json2, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(mApplicationContext, "Create Stack successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "Create Stack failed", Toast.LENGTH_SHORT).show();
                        //System.out.println("createFail");
                        System.out.println(error.networkResponse);
                        callback.onSuccess("error");
                    }
                }

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
//                headers.put("Content-Type", "application/json");
                return headers;

            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);

    }

    ///////////////////
    // list database instance
    public void listDatabaseInstances(final VolleyCallback callback, final Context context)     {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String tenant = sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "ERROR");

        String fullURL = databaseServiceURL + "/instances";


        System.out.println(fullURL);
        //System.out.println(sharedPreferences.getString("tenantId", "Error Getting Compute URL"));
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");


        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        JSONArray resultArray;
//                        System.out.println(response);
                        //////////////////
                        //write json data to txt file
//                        String path = context.getFilesDir().getPath().toString() ;
//                        Log.d(TAG, path);
//                        writeTxtToFile(response,path,"test.txt");
                        ////////////////////

                        resultArray = ResponseParser.getInstance(mApplicationContext).listDatabaseInstances(response);
//
                        String result = resultArray.toString();
//                        System.out.print("testoutput" + result);
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
                    Toast.makeText(mApplicationContext, "Getting instances Failed", Toast.LENGTH_SHORT).show();
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    ////////////////////
    // list database instance detail and actions
    public void listSingleDatabaseInstance(final VolleyCallback callback, final Context context, String instanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String fullURL = databaseServiceURL + "/instances/" + instanceId;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        System.out.println(fullURL);
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
//                            if (key.equals("null")) {
//                                key = "None";
//                            }
//                            JSONArray sgArray = resp.getJSONObject("server").getJSONArray("security_groups");
//                            String sg = "";
//                            if (sgArray.length() == 0) {
//                                sg = "None";
//                            } else {
//                                for (int i = 0; i < sgArray.length(); i++) {
//                                    JSONObject sgObject = (JSONObject) sgArray.get(i);
//                                    if (i == 0) {
//                                        sg = sgObject.getString("name");
//                                    } else {
//                                        sg = sg + ", " + sgObject.getString("name");
//                                    }
//                                }
//                            }

//                            JSONArray vArray = resp.getJSONObject("server").getJSONArray("os-extended-volumes:volumes_attached");
//                            int vNum = vArray.length();
//                            for (int j = 0; j < vNum; j++) {
//                                JSONObject vObject = (JSONObject) vArray.get(j);
//                                String volume = vObject.getString("id");
//                                result.put("volume" + j, volume);
//                            }
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }



    /////////////////
    // restart database instance volume
    public void databaseInstanceRestart(final VolleyCallback callback, String instanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String fullURL = databaseServiceURL + "/instances/" + instanceId + "/action";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json = new JSONObject();
        JSONObject json1 = new JSONObject();
        try {
            json.put("restart", json1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("jsonValue", json.toString());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
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
                        Toast.makeText(mApplicationContext, "Network Error", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
//                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    /*
    * resize database instance volume
    * */
    public void resizeDatabaseInstanceVolume(final VolleyCallback callback, int newSize, String databaseInstanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String tenant = sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String fullURL = databaseServiceURL + "/instances/" + databaseInstanceId + "/action";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        JSONObject json3 = new JSONObject();
        try {
            json2.put("size", newSize);
            json1.put("volume", json2);
            json3.put("resize", json1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(json1.toString());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json3,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //System.out.println("dedededededededddededed");
                        callback.onSuccess("success");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    System.out.println("dedededededededddededed");
                    //Toast.makeText(mApplicationContext, "Extend V successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    } else {
                        Toast.makeText(mApplicationContext, "Fail to extend V", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
//                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    /*
    * attach configuration group to database instance
    * */
    public void attachConfigGroup(final VolleyCallback callback, String databaseInstanceId, String configGroupId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String fullURL = databaseServiceURL + "/instances/" + databaseInstanceId;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("configuration", configGroupId);
            json1.put("instance", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(json1.toString());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, fullURL, json1,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //System.out.println("dedededededededddededed");
                        callback.onSuccess("success");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    System.out.println("dedededededededddededed");
                    //Toast.makeText(mApplicationContext, "Attach successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    } else {
                        Toast.makeText(mApplicationContext, "Fail to attach", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
//                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    /*
    * detach configuration group
    * */
    public void detachConfigGroup(final VolleyCallback callback, String databaseInstanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String fullURL = databaseServiceURL + "/instances/" + databaseInstanceId;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json1.put("instance", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(json1.toString());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, fullURL, json1,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //System.out.println("dedededededededddededed");
                        callback.onSuccess("success");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    System.out.println("dedededededededddededed");
                    //Toast.makeText(mApplicationContext, "Attach successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    } else {
                        Toast.makeText(mApplicationContext, "Fail to attach", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
//                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    /*
    * delete database instance
    * */
    public void deleteDatabaseInstance(final VolleyCallback callback, String databaseInstanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String tenant = sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String fullURL = databaseServiceURL + "/instances/" + databaseInstanceId;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

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
//                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /*
    * create database instance
    * */
    public void createdatabaseInstance(final VolleyCallback callback, String setName, String availabilityZone, String datastoreVersion, String datastoreType, int volumeSize, String locality, String flavorRef) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String fullURL = databaseServiceURL + "/instances";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        JSONObject json3 = new JSONObject();
        JSONObject json4 = new JSONObject();
        JSONObject json5 = new JSONObject();
        JSONObject json6 = new JSONObject();
        Integer temp = 1;
//        String newDatastore = selectDatastoreName.toLowerCase();

        try {
            json2.put("name",setName);
            json2.put("flavorRef", flavorRef);
            json2.put("availability_zone", availabilityZone);
            json4.put("version", datastoreVersion);
            json4.put("type", datastoreType);

            json2.put("datastore",json4);
            json1.put("size", volumeSize);
            json2.put("volume",json1);

            if (locality != "None")
            {
                json2.put("locality", locality);
            }
            json5.put("instance", json2);


            Log.d("createInstance", json5.toString());
//            json1.put("subnet", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json5, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(mApplicationContext, "Create instance successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "Create Instance failed", Toast.LENGTH_SHORT).show();
                        //System.out.println("createFail");
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);

    }


    /*
    * list configuration group and action
    * */
    public void listConfigGroup(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String fullURL = databaseServiceURL + "/configurations";

        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray resultArray;
                        resultArray = ResponseParser.getInstance(mApplicationContext).listConfigGroups(response);
                        String result = resultArray.toString();
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
                }
                Toast.makeText(mApplicationContext, "Listing Volumes Failed", Toast.LENGTH_SHORT).show();

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    //////////////////
    // delete configuration group
    public void deleteConfigGroup(final VolleyCallback callback, String configGroupId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String tenant = sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String fullURL = databaseServiceURL + "/configurations/" + configGroupId;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

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
//                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    //////////
    // show configuration group detail
    public void showConfigGroupDetail(final VolleyCallback callback, final Context context, String configGroupId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String tenant = sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String fullURL = databaseServiceURL + "/configurations/" + configGroupId;

        //System.out.println(sharedPreferences.getString("imageServiceURL", "Error Getting Compute URL"));
        //System.out.println(sharedPreferences.getString("tenantId", "Error Getting Compute URL"));
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject resultObject;
                        resultObject = ResponseParser.getInstance(mApplicationContext).listConfigGroupDetail(response);
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /*
    * list configuration group instance
    * */
    public void listConfigGroupInstances(final VolleyCallback callback, final Context context, String configGroupId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String tenant = sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String fullURL = databaseServiceURL + "/configurations/" + configGroupId + "/instances";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray resultObject;
                        resultObject = ResponseParser.getInstance(mApplicationContext).listConfigGroupInstances(response);
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /*
    * create configuration group
    * */
    public void createConfigGroup(final VolleyCallback callback, String setName, String selectDatastoreName, String setDescription) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String fullURL = databaseServiceURL + "/configurations";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        JSONObject json3 = new JSONObject();
        JSONObject json4 = new JSONObject();
//        JSONObject json5 = new JSONObject();
//        JSONObject json6 = new JSONObject();
        Integer temp = 1;
        String newDatastore = selectDatastoreName.toLowerCase();

        try {
            json1.put("type", newDatastore);
            json2.put("datastore",json1);
//            json3.put("sync_binlog",temp);
            json2.put("values", json3);
            json2.put("name",setName);
            if (setDescription != null)
            {
                json2.put("description", setDescription);
            }
            json4.put("configuration",json2);

            Log.d("createConfig", json4.toString());
//            json1.put("subnet", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json4, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(mApplicationContext, "Create Subnet successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "Create Subnet failed", Toast.LENGTH_SHORT).show();
                        //System.out.println("createFail");
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);

    }


    /*
    * list data store
    * */
    public void listDatastores(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String fullURL = databaseServiceURL + "/datastores";

        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray resultArray;

                        resultArray = ResponseParser.getInstance(mApplicationContext).listDatastores(response);
                        String result = resultArray.toString();
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
                }
                Toast.makeText(mApplicationContext, "Listing Volumes Failed", Toast.LENGTH_SHORT).show();

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /*
    * list flavor
    * */
    public void listDatabaselistDatabaseFlavor(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String fullURL = databaseServiceURL + "/flavors";

        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray resultArray;

                        resultArray = ResponseParser.getInstance(mApplicationContext).listDatabaseFlavors(response);
                        String result = resultArray.toString();
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
                }
                Toast.makeText(mApplicationContext, "Listing Volumes Failed", Toast.LENGTH_SHORT).show();

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /*
    * list database backup
    * */
    public void listDatabaseBackup(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String fullURL = databaseServiceURL + "/backups";

        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray resultArray;
                        resultArray = ResponseParser.getInstance(mApplicationContext).listDatabaseBackups(response);
                        String result = resultArray.toString();
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
                }
                Toast.makeText(mApplicationContext, "Listing Volumes Failed", Toast.LENGTH_SHORT).show();

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /*
    * delete database backup
    * */
    public void deleteDatabaseBackup(final VolleyCallback callback, String backupId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String tenant = sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String fullURL = databaseServiceURL + "/backups/" + backupId;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

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
//                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /*
    * show database backup detail
    * */
    public void showDatabaseBackupDetail(final VolleyCallback callback, final Context context, String backupId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String tenant = sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String fullURL = databaseServiceURL + "/backups/" + backupId;

        //System.out.println(sharedPreferences.getString("imageServiceURL", "Error Getting Compute URL"));
        //System.out.println(sharedPreferences.getString("tenantId", "Error Getting Compute URL"));
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject resultObject;
                        resultObject = ResponseParser.getInstance(mApplicationContext).listDatabaseBackupDetail(response);
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /*
    * create instance backup
    * */
    public void createInstanceBackup(final VolleyCallback callback, String setName, String selectInstanceId, String setDescription) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String fullURL = databaseServiceURL + "/backups";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        JSONObject json3 = new JSONObject();
        JSONObject json4 = new JSONObject();
//        JSONObject json5 = new JSONObject();
//        JSONObject json6 = new JSONObject();
        Integer temp = 0;
//        String newDatastore = selectDatastoreName.toLowerCase();

        try {
//            json1.put("incremental", temp);
            json2.put("incremental",temp);
//            json3.put("sync_binlog",temp);
            json2.put("instance", selectInstanceId);
            json2.put("name",setName);
            if (setDescription != null)
            {
                json2.put("description", setDescription);
            }
            json4.put("backup",json2);

            Log.d("createBackup", json4.toString());
//            json1.put("subnet", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json4, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(mApplicationContext, "Create Subnet successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "Create Subnet failed", Toast.LENGTH_SHORT).show();
                        //System.out.println("createFail");
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);

    }

    /*
    * show root manage detail
    * */
    public void showManageRootDetail(final VolleyCallback callback, final Context context, String databaseInstanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String tenant = sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String fullURL = databaseServiceURL + "/instances/" + databaseInstanceId + "/root";

        //System.out.println(sharedPreferences.getString("imageServiceURL", "Error Getting Compute URL"));
        //System.out.println(sharedPreferences.getString("tenantId", "Error Getting Compute URL"));
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject resultObject;
//                        Log.d("root", response);
                        resultObject = ResponseParser.getInstance(mApplicationContext).showRoot(response);
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /*
    * enable instance root
     * */
    public void enableRoot(final VolleyCallback callback, final Context context, String instanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String tenant = sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String fullURL = databaseServiceURL + "/instances/" + instanceId + "/root";

        //System.out.println(sharedPreferences.getString("imageServiceURL", "Error Getting Compute URL"));
        //System.out.println(sharedPreferences.getString("tenantId", "Error Getting Compute URL"));
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.POST, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject resultObject;
                        resultObject = ResponseParser.getInstance(mApplicationContext).enableRoot(response);
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /*
    * disable instance root
    * */
    public void disableRoot(final VolleyCallback callback, String instanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String tenant = sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String fullURL = databaseServiceURL + "/instances/" + instanceId + "/root";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

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
//                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /*
    * edit router
    * */
    public void editRouter(final VolleyCallback callback, String editRouterName, boolean admin_state, String routerID, String routerName) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String fullURL = networkServiceURL + "v2.0/routers/" + routerID;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        JSONObject json3 = new JSONObject();
//        Log.d("editinputedit",editRouterName);
//        Log.d("editinputROuter", routerName);

        try {
//            json3.put("network_id", networkID);
//            json2.put("external_gateway_info", json3);
            if (editRouterName.equals("")) {
                json2.put("name", routerName);
            }
            else {
                json2.put("name", editRouterName);
            }
            json2.put("admin_state_up", admin_state);
            json1.put("router", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("editEdit", editRouterName);
        Log.d("editOri",routerName);
        Log.d("editROuter", json1.toString());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, fullURL, json1, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(mApplicationContext, "Edit Router successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "Edit Router failed", Toast.LENGTH_SHORT).show();
                        //System.out.println("createFail");
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);

    }

    /*
    Contariner Service
     **/
    public void listCluster(final VolleyCallback callback, final Context context, final String clusterID) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String containerInfraURL = sharedPreferences.getString("containerInfraURL", "Error Getting Compute URL");
        String tenant = sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String fullURL = containerInfraURL + "v1/clusters";

        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONArray resultArray;
                resultArray = ResponseParser.getInstance(mApplicationContext).listCluster(response, clusterID);
                System.out.println("clusterID: " + clusterID);
                String result = resultArray.toString();
                callback.onSuccess(result);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 401) {
                    Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(mApplicationContext, LoginActivity.class);
                    context.startActivity(i);
                } else {
                    Toast.makeText(mApplicationContext, "Getting Clusters List  Failed", Toast.LENGTH_SHORT).show();

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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /*
    Create a new Container Cluster.
    **/
    public void createCluster(final VolleyCallback callback, String clusterName, String disUrl, String clusterTemplateID, String kytPair, String flavorID, String masterFlavorID, final int masterCount, final int nodeCount, final int createTimeout) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String containerInfraURL = sharedPreferences.getString("containerInfraURL", "Error Getting Compute URL");
        String fullURL = containerInfraURL + "v1/clusters";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json1.put("name", clusterName);
            json1.put("discovery_url", disUrl);
            json1.put("master_count", masterCount);
            json1.put("cluster_template_id", clusterTemplateID);
            json1.put("node_count", nodeCount);
            json1.put("create_timeout", createTimeout);
            json1.put("keypair", kytPair);
            json1.put("master_flavor_id", masterFlavorID);
            json1.put("labels", json2);
//            json2.put(labels);
            json1.put("flavor_id", flavorID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json1, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(mApplicationContext, "Create instance successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    } else {
                        Toast.makeText(mApplicationContext, "Create cluster failed", Toast.LENGTH_SHORT).show();
                        //System.out.println("createFail");
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
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);

    }


















    ///////////////////////////////////////
// 将字符串写入到文本文件中
    public void writeTxtToFile(String strcontent, String filePath, String fileName) {
        //生成文件夹之后，再生成文件，不然会出错
        makeFilePath(filePath, fileName);

        String strFilePath = filePath + fileName;
        // 每次写入时，都换行写
        String strContent = strcontent + "\r\n";
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
        }
    }

    // 生成文件
    public File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    // 生成文件夹
    public static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }
        } catch (Exception e) {
            Log.i("error:", e + "");
        }


    }

    public static boolean createFile(File fileName)throws Exception{
        boolean flag=false;
        try{
            if(!fileName.exists()){
                fileName.createNewFile();
                flag=true;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return true;
    }


}
