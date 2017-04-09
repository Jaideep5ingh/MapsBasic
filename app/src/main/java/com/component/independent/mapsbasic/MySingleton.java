package com.component.independent.mapsbasic;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by jai00 on 05-04-2017.
 */

public class MySingleton {
    static MySingleton mInstance;
    RequestQueue requestQueue;
    Context mCtx;

    MySingleton(Context context) {
        mCtx = context;
        requestQueue = getRequestQueue();
    }

    public RequestQueue getRequestQueue() {
        if(requestQueue==null)
        {
            requestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }

        return requestQueue;
    }


    public static synchronized MySingleton getInstance(Context context){
        if(mInstance==null){
            mInstance = new MySingleton(context);
        }
        return mInstance;
    }


    public<T> void addToQueue(Request<T> request){
        requestQueue.add(request);
    }
}
