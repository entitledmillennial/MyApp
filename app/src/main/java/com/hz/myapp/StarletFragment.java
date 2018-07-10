package com.hz.myapp;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

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
 * Created by zeee on 01-02-2018.
 */

public class StarletFragment extends Fragment implements ConnectivityReceiver.ConnectivityReceiverListener{

    private static final String TAG = StarletFragment.class.getSimpleName();

    private ConnectivityReceiver connectivityReceiver = ConnectivityReceiver.getInstance();

    private View view;
    private ImageView starletImageView;
    private TextView messageTextView;
    private AdView adView;
    private boolean bannerFailedToLoad = false;
    private boolean bannerListenerTriggered = false;
    private ProgressBar progressBar;

    private String starletURL = "";
    private String messageText = "";
    private boolean isDownloading = false;
    private boolean isItDestroyed = false;

    public StarletFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_starlet, container, false);
        starletImageView = view.findViewById(R.id.starlet_imageview);
        Glide.with(getActivity())
                .load(R.drawable.placeholder_starlet)
                .into(starletImageView);

        messageTextView = view.findViewById(R.id.message);
        progressBar = view.findViewById(R.id.starlet_progressbar);

        adView = view.findViewById(R.id.starletfragment_banner_ad);
        adView.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                bannerListenerTriggered = true;
                bannerFailedToLoad = false;
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                bannerListenerTriggered = true;
                bannerFailedToLoad = true;
            }
        });

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        new GetMessage().execute();
        new GetStarlet().execute();

        return view;
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (bannerListenerTriggered) {
            if (bannerFailedToLoad) {
                AdRequest bannerAdRequest = new AdRequest.Builder().build();
                adView.loadAd(bannerAdRequest);
            }
        } else {
            AdRequest bannerAdRequest = new AdRequest.Builder().build();
            adView.loadAd(bannerAdRequest);
        }
    }

    private class GetMessage extends AsyncTask<Void, Void, Void> {

        private String TAG = GetMessage.class.getSimpleName();

        private String jsonString;

        @Override
        protected Void doInBackground(Void... voids) {
            jsonString = ((MyApp)getActivity().getApplication()).getResponseString();

            if(jsonString != null){
                try{
                    JSONObject topmostJSONObject = new JSONObject(jsonString);
                    JSONObject message = topmostJSONObject.getJSONObject("starlet");

                    messageText = message.getString("text");
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

            if(messageText != "") {
                messageTextView.setText(messageText);
            }
        }
    }

    private class GetStarlet extends AsyncTask<Void, Void, Void> {

        private String TAG = GetStarlet.class.getSimpleName();

        private String jsonString;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if(!isItDestroyed) {
                progressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if(!isItDestroyed) {
                jsonString = ((MyApp) getActivity().getApplication()).getResponseString();

                if (jsonString != null) {
                    try {
                        JSONObject topmostJSONObject = new JSONObject(jsonString);
                        JSONObject message = topmostJSONObject.getJSONObject("starlet");
                        starletURL = message.getString("image");

                        HttpURLConnection tempCon = (HttpURLConnection) new URL(starletURL).openConnection();
                        int tempConLength = tempCon.getContentLength();
                        if (tempCon != null) {
                            tempCon.disconnect();
                        }

                        File tempFile = new File(getActivity().getFilesDir(), starletURL.replaceAll("/", ""));
                        if (tempFile.exists()) {
                            isDownloading = (int) tempFile.length() != tempConLength;
                            if (isDownloading) {
                                //Log.e(TAG, "File name " + starletURL.replaceAll("/", "") + " exists, Partially.");
                                downloadStarlet(starletURL);
                            } else {
                                //Log.e(TAG, "File name " + starletURL.replaceAll("/", "") + " exists, Fully.");
                            }
                        } else {
                            //Log.e(TAG, "File name " + starletURL.replaceAll("/", "") + " doesn't exist.Downloading...");
                            downloadStarlet(starletURL);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        //Log.e(TAG, "Json parsing error: " + e.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
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
                if (isDownloading) {
                    new GetStarlet().execute();
                } else {
                    Glide.with(getContext()).load(starletURL)
                            .listener(new RequestListener<String, GlideDrawable>() {
                                @Override
                                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                    e.printStackTrace();
                                    new GetStarlet().execute();
                                    //progressBar.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                    progressBar.setVisibility(View.GONE);
                                    return false;
                                }
                            })
                            .placeholder(R.drawable.placeholder_starlet)
                            .into(starletImageView);
                }
            }
        }
    }

    private void downloadStarlet(String url){
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
                File file = new File(getActivity().getFilesDir(), url.replaceAll("/", ""));
                if (file.exists()) {
                    downloaded = (int) file.length();
                    connection.setRequestProperty("Range", "bytes=" + (file.length()) + "-");
                }
            } else {
                connection.setRequestProperty("Range", "bytes=" + downloaded + "-");
            }

            bufferedInputStream = new BufferedInputStream(connection.getInputStream(), 8192);
            fileOutputStream = (downloaded == 0) ? new FileOutputStream(new File(getActivity().getFilesDir(), url.replaceAll("/", ""))) :
                                        new FileOutputStream(new File(getActivity().getFilesDir(), url.replaceAll("/", "")), true);
            bufferedOutputStream = new BufferedOutputStream(fileOutputStream, 8192);
            byte bytes[] = new byte[8192];

            while ((count = bufferedInputStream.read(bytes, 0, 8192)) >= 0) {
                isDownloading = true;
                if(isItDestroyed){
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
    public void onResume() {
        super.onResume();

        getActivity().registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        connectivityReceiver.setConnectivityReceiverListener(this);

        if(adView != null){
            adView.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        getActivity().unregisterReceiver(connectivityReceiver);

        if(adView != null){
            adView.pause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isItDestroyed = true;
        if(adView != null){
            adView.destroy();
        }
    }
}
