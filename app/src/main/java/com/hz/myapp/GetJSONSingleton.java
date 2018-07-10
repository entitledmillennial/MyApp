package com.hz.myapp;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by zeee on 26-02-2018.
 */

public class GetJSONSingleton {

    private static final String TAG = GetJSONSingleton.class.getSimpleName();

    private static GetJSONSingleton getJSONSingleton;
    private RequestQueue requestQueue;
    private static Context context;

    private GetJSONSingleton(Context context) {
        GetJSONSingleton.context = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized GetJSONSingleton getInstance(Context context) {
        if (getJSONSingleton == null) {
            getJSONSingleton = new GetJSONSingleton(context);
        }
        return getJSONSingleton;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (requestQueue != null) {
            requestQueue.cancelAll(tag);
        }
    }
}
