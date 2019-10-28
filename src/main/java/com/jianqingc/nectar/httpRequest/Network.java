package com.jianqingc.nectar.httpRequest;


import android.content.Context;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Jianqing Chen on 2016/9/28.
 * The Network is designed to maintain Volley ASYNC HTTP request queue service
 */
public class Network {
    private Context mApplicationContext;
    private static Network mInstance ;
    private RequestQueue mRequestQueue = null;


    public static Network getInstance(Context context) {
        if (mInstance == null)
            mInstance = new Network(context);
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

    public Network(Context context) {
        mApplicationContext = context.getApplicationContext();
        mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());

    }

}

