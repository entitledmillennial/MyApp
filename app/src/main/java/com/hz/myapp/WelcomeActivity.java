package com.hz.myapp;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * Created by zeee on 26-02-2018.
 */

public class WelcomeActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener{

    private static final String TAG = WelcomeActivity.class.getSimpleName();

    private ConnectivityReceiver connectivityReceiver = ConnectivityReceiver.getInstance();

    private String gifURL = "";
    private String message = "";
    private boolean isDownloading = false;
    private boolean isPausedOrDestroyed= false;
    private boolean isItDestroyed = false;
    private boolean doOnlyOnce = true;
    private int serverCounter = 0;

    private Button mainActivityButton;
    private ImageButton shareButton;
    private ProgressBar progressBar;
    private ImageView gifImageView;
    private TextView messageTextView;
    private TextView internetTextView;

    private ImageView noInternetImageView;

    private InterstitialAd interstitialAd;
    private boolean interstitialFailedToLoad = false;
    private boolean interstitialListenerTriggered = false;

    private AdView adView;
    private boolean bannerFailedToLoad = false;
    private boolean bannerListenerTriggered = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        ((MyApp)getApplication()).setInterstitialAd();
        interstitialAd = ((MyApp)getApplication()).getInterstitialAd();
        interstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                interstitialListenerTriggered = true;
                interstitialFailedToLoad = false;
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                interstitialListenerTriggered = true;
                interstitialFailedToLoad = true;
            }
        });

        adView = findViewById(R.id.welcome_activity_banner_ad);
        adView.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                bannerListenerTriggered = true;
                if(doOnlyOnce){
                    volleyStringRequest(getString(R.string.json_url_1));
                    doOnlyOnce = false;
                }
                bannerFailedToLoad = false;
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                bannerListenerTriggered = true;
                if(doOnlyOnce){
                    volleyStringRequest(getString(R.string.json_url_1));
                    doOnlyOnce = false;
                }
                bannerFailedToLoad = true;
            }
        });
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        noInternetImageView = findViewById(R.id.no_internet_imageview);
        if(!connectivityReceiver.isConnected(this)){
            noInternetImageView.setVisibility(View.VISIBLE);
        }

        this.registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        gifImageView = findViewById(R.id.gif_imageview);
        Glide.with(getApplicationContext())
                .load(R.drawable.placeholder_gif)
                .into(gifImageView);

        volleyStringRequest(getString(R.string.json_url_1));

        progressBar = findViewById(R.id.progressbar);
        mainActivityButton = findViewById(R.id.main_activity_button);
        shareButton = findViewById(R.id.share_button);
        messageTextView = findViewById(R.id.message_textview);
        internetTextView = findViewById(R.id.welcomeactivity_internet_textview);

        internetTextView.setSelected(true);

        mainActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isPausedOrDestroyed = true;
                Intent intent = new Intent(WelcomeActivity.this, ContentActivity .class);
                startActivity(intent);
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message) + getString(R.string.playstore_url_without_id)
                                    + WelcomeActivity.this.getPackageName());

                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_title)));

            }
        });

        mainActivityButton.setVisibility(View.GONE);
        messageTextView.setMovementMethod(new ScrollingMovementMethod());
    }

    private void volleyStringRequest(String url){

        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ((MyApp)getApplication()).setResponseString(response);
                new GetMessage().execute();
                if(bannerListenerTriggered) {
                    new GetGif().execute();
                }
                serverCounter = 0;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());

                Glide.with(getApplicationContext())
                        .load(R.drawable.placeholder_gif)
                        .into(gifImageView);

                messageTextView.setText(getString(R.string.placeholder_message));
                if(error.networkResponse != null) {
                    //Log.e(TAG, "ERROR RESPONSE CODE = " + error.networkResponse.statusCode);
                    switch (serverCounter) {
                        case 0:
                            volleyStringRequest(getString(R.string.json_url_2));
                            //Log.e(TAG, "" + serverCounter);
                            serverCounter++;
                            break;
                        default:
                            //Log.e(TAG, "DEFAULT CASE OF SWITCH");
                            //Log.e(TAG, "" + serverCounter);
                            serverCounter = 0;
                            break;
                    }
                }
                else{
                    //Log.e(TAG, "No internet connection...");
                }
            }
        });
        // Adding String request to request queue
        GetJSONSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        //interstitial ads listeners are often never called even when the ad is loaded. So the following correction.
        boolean adCloseEventOccurred = ((MyApp) getApplication()).isInterstitialAdCloseEventOccurred();
        if (!adCloseEventOccurred) {
            if (interstitialListenerTriggered) {
                if (interstitialFailedToLoad) {
                    ((MyApp) getApplication()).setInterstitialAd();
                    interstitialAd = ((MyApp) getApplication()).getInterstitialAd();

                }
            } else {
                ((MyApp) getApplication()).setInterstitialAd();
                interstitialAd = ((MyApp) getApplication()).getInterstitialAd();
            }
        }

        if (bannerListenerTriggered) {
            if (bannerFailedToLoad) {
                AdRequest bannerAdRequest = new AdRequest.Builder().build();
                adView.loadAd(bannerAdRequest);
            }
        } else {
            AdRequest bannerAdRequest = new AdRequest.Builder().build();
            adView.loadAd(bannerAdRequest);
        }

        if (isConnected) {
            noInternetImageView.setVisibility(View.GONE);
            internetTextView.setVisibility(View.INVISIBLE);

            volleyStringRequest(getString(R.string.json_url_1));

        } else {
            internetTextView.setVisibility(View.VISIBLE);
        }
    }

    private class GetMessage extends AsyncTask<Void, Void, Void> {

        private String TAG = GetMessage.class.getSimpleName();

        private String jsonString;

        @Override
        protected Void doInBackground(Void... voids) {
            jsonString = ((MyApp)getApplication()).getResponseString();

            if(jsonString != null){
                try{
                    JSONObject topmostJSONObject = new JSONObject(jsonString);
                    JSONObject welcome = topmostJSONObject.getJSONObject("welcome");

                    message = welcome.getString("message");
                }
                catch(JSONException e){
                    e.printStackTrace();
                    //Log.e(TAG, "Json parsing error: " + e.getMessage());
                }
                catch (Exception e){
                    e.printStackTrace();
                    //Log.e(TAG, "Json parsing error: " + e.getMessage());
                }
            }
            else{
                //Log.e(TAG, "Couldn't get JSON from the server.");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            messageTextView.setText(message);

            progressBar.setVisibility(View.INVISIBLE);
            mainActivityButton.setVisibility(View.VISIBLE);
        }
    }

    private class GetGif extends AsyncTask<Void, Void, Void> {

        private String TAG = GetGif.class.getSimpleName();

        private String jsonString;

        @Override
        protected Void doInBackground(Void... voids) {
            if(!isPausedOrDestroyed) {
                jsonString = ((MyApp) getApplication()).getResponseString();

                if (jsonString != null) {
                    try {
                        JSONObject topmostJSONObject = new JSONObject(jsonString);
                        JSONObject welcome = topmostJSONObject.getJSONObject("welcome");
                        gifURL = welcome.getString("gif");

                        HttpURLConnection tempCon = (HttpURLConnection) new URL(gifURL).openConnection();
                        int tempConLength = tempCon.getContentLength();
                        if(tempCon != null){
                            tempCon.disconnect();
                        }

                        File tempFile = new File(WelcomeActivity.this.getFilesDir(), gifURL.replaceAll("/", ""));
                        if (tempFile.exists()) {
                            isDownloading = (int)tempFile.length() != tempConLength;
                            if (isDownloading) {
                                //Log.e(TAG, "File name " + gifURL.replaceAll("/", "") + " exists, Partially.");
                                downloadGif(gifURL);
                            }
                            else{
                                //Log.e(TAG, "File name " + gifURL.replaceAll("/", "") + " exists, Fully.");
                            }
                        } else {
                            //Log.e(TAG, "File name " + gifURL.replaceAll("/", "") + " doesn't exist.Downloading...");
                            downloadGif(gifURL);
                        }

                    } catch (JSONException e) {
                        //Log.e(TAG, "Json parsing error: " + e.getMessage());
                    } catch (Exception e) {
                        //Log.e(TAG, "Json parsing error: " + e.getMessage());
                    }
                } else {
                    //Log.e(TAG, "Couldn't get JSON from the server.");
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if(!isItDestroyed) {
                if (!isDownloading) {
                    Glide.with(getApplicationContext())
                            .load(new File(getFilesDir(), gifURL.replaceAll("/", "")))
                            .asGif()
                            .placeholder(R.drawable.placeholder_gif)
                            .into(gifImageView);
                }
            }
        }
    }

    private void downloadGif(String url) {
        HttpURLConnection connection = null;
        BufferedInputStream bufferedInputStream = null;
        FileOutputStream fileOutputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        int downloaded = 0, count = 0;

        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setReadTimeout(10000);
            connection.setDoInput(true);

            if (isDownloading) {
                File file = new File(getFilesDir(), url.replaceAll("/", ""));
                if (file.exists()) {
                    downloaded = (int) file.length();
                    connection.setRequestProperty("Range", "bytes=" + (file.length()) + "-");
                }
            } else {
                connection.setRequestProperty("Range", "bytes=" + downloaded + "-");
            }

            bufferedInputStream = new BufferedInputStream(connection.getInputStream(), 8192);
            fileOutputStream = (downloaded == 0) ? new FileOutputStream(new File(getFilesDir(), url.replaceAll("/", ""))) :
                                                   new FileOutputStream(new File(getFilesDir(), url.replaceAll("/", "")), true);
            bufferedOutputStream = new BufferedOutputStream(fileOutputStream, 8192);
            byte bytes[] = new byte[8192];

            while ((count = bufferedInputStream.read(bytes, 0, 8192)) >= 0) {
                isDownloading = true;
                if(isPausedOrDestroyed){
                    throw new SocketTimeoutException("manual");
                }
                bufferedOutputStream.write(bytes, 0, count);
                downloaded += count;
                //Log.e(TAG, "downloaded = "+downloaded);
            }

            if (count == -1) {
                isDownloading = false;
            }
        }
        catch (SocketTimeoutException e){
            if(e.getMessage() == "manual"){
                //Log.e(TAG, "Manually threw a SocketTimeoutException !");
            }
            else {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            try {
                if (connection != null)
                    connection.disconnect();
                if (bufferedOutputStream != null) {
                    bufferedOutputStream.flush();
                    bufferedOutputStream.close();
                }
                if (bufferedInputStream != null)
                    bufferedInputStream.close();
                if (fileOutputStream != null)
                    fileOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        isPausedOrDestroyed = false;
        if(adView != null){
            adView.resume();
        }
        this.registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        connectivityReceiver.setConnectivityReceiverListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        isPausedOrDestroyed = true;
        if(adView != null){
            adView.pause();
        }
        this.unregisterReceiver(connectivityReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        isItDestroyed = true;
        isPausedOrDestroyed = true;
        if(adView != null){
            adView.destroy();
        }
    }
}

