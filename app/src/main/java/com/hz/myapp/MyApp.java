package com.hz.myapp;

import android.app.Application;
import android.content.Intent;
import android.os.Process;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

/**
 * Created by zeee on 09-04-2018.
 */

public class MyApp extends Application {

    private static final String TAG = MyApp.class.getSimpleName();

    private InterstitialAd interstitialAd;
    private static boolean interstitialAdCloseEventOccurred;
    private String responseString;

    @Override
    public void onCreate() {
        super.onCreate();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                                                      @Override
                                                      public void uncaughtException(Thread thread, Throwable throwable) {
                                                          throwable.printStackTrace();
                                                          Intent intent = new Intent(getApplicationContext(), CrashActivity.class);
                                                          intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                          startActivity(intent);

                                                          Process.killProcess(Process.myPid());
                                                      }
                                                  });

                MobileAds.initialize(this, getString(R.string.app_id));
    }

    public synchronized void setInterstitialAd(){
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.interstitial_id));
        AdRequest adRequest = new AdRequest.Builder().build();
        interstitialAd.loadAd(adRequest);
    }

    public synchronized InterstitialAd getInterstitialAd(){
        return interstitialAd;
    }

    public String getResponseString(){
        if(responseString == null || responseString == ""){
            volleyStringRequest(getString(R.string.json_url_1));
        }
        return responseString;
    }

    public void setResponseString(String responseString){
        this.responseString = responseString;
    }

    private void volleyStringRequest(String url){
        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                setResponseString(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        });
        // Adding String request to request queue
        GetJSONSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    public boolean isInterstitialAdCloseEventOccurred() {
        return interstitialAdCloseEventOccurred;
    }

    public void setInterstitialAdCloseEventOccurred(boolean interstitialAdCloseEventOccurred) {
        this.interstitialAdCloseEventOccurred = interstitialAdCloseEventOccurred;
    }
}
