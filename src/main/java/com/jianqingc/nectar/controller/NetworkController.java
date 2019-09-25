package com.jianqingc.nectar.controller;


import android.content.Context;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Jianqing Chen on 2016/9/28.
 * The NetworkController is designed to maintain Volley ASYNC HTTP request queue service
 */
public class NetworkController {
    private Context mApplicationContext;
    private static NetworkController mInstance ;
    private RequestQueue mRequestQueue = null;


    public static NetworkController getInstance(Context context) {
        if (mInstance == null)
            mInstance = new NetworkController(context);
        return mInstance;
    }

    public RequestQueue getRequestQueue() {

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> request) {
        getRequestQueue().add(request);
    }

    public void cancelPendingQueue(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public NetworkController(Context context) {
        mApplicationContext = context.getApplicationContext();
        mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());

    }

}

