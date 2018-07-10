package com.hz.myapp;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zeee on 18-02-2018.
 */

public class AlbumFragment extends Fragment implements ConnectivityReceiver.ConnectivityReceiverListener{

    private static final String TAG = AlbumFragment.class.getSimpleName();

    private ConnectivityReceiver connectivityReceiver = ConnectivityReceiver.getInstance();

    private View view;
    private RecyclerView albumRecyclerView;
    private ProgressBar progressBar;
    private AdView adView;
    private boolean bannerFailedToLoad = false;
    private boolean bannerListenerTriggered = false;

    private EndlessRecyclerViewScrollListener scrollListener;

    private List<String> imageFileNameList = new ArrayList<>();

    private AlbumImageDialogFragment albumImageDialogFragment = AlbumImageDialogFragment.newInstance();
    private AlbumAdapter albumAdapter;
    private String jsonString;
    private JSONArray jsonArrayAlbum;
    private int jsonArrayAlbumLength = 0;
    private int notifyAtIndex = 0;
    private boolean isDownloading = true;
    private boolean isItDestroyed = false;
    private boolean connectedToInternet = false;

    public AlbumFragment(){
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        jsonString = ((MyApp)getActivity().getApplication()).getResponseString();
        if(jsonString != null){
            try{
                JSONObject topmostJSONObject = new JSONObject(jsonString);
                jsonArrayAlbum = topmostJSONObject.getJSONArray("album");

                jsonArrayAlbumLength = jsonArrayAlbum.length();
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
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_album, container,false);

        adView = view.findViewById(R.id.albumfragment_banner_ad);
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

        progressBar = view.findViewById(R.id.album_progressbar);
        albumRecyclerView = view.findViewById(R.id.album_recyclerview);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);    //RecyclerView.LayoutManager
        albumRecyclerView.setLayoutManager(gridLayoutManager);
        scrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                notifyAtIndex = page;
                //Log.e(TAG, "notify at index ---"+notifyAtIndex);
                if (notifyAtIndex < jsonArrayAlbumLength) {
                        new GetImages().execute();

                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        };
        albumRecyclerView.addOnScrollListener(scrollListener);
        albumRecyclerView.setHasFixedSize(true);
        albumRecyclerView.setItemViewCacheSize(500);

        albumAdapter = new AlbumAdapter(getActivity(), imageFileNameList);
        albumRecyclerView.setAdapter(albumAdapter);

        albumRecyclerView.addOnItemTouchListener(new AlbumAdapter.RecyclerTouchListener(getContext(), albumRecyclerView, new AlbumAdapter.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("imageFileNameList", (Serializable) imageFileNameList);
                bundle.putInt("position", position);

                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                albumImageDialogFragment = AlbumImageDialogFragment.newInstance();
                albumImageDialogFragment.setArguments(bundle);
                albumImageDialogFragment.show(fragmentTransaction, "slideshow");
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        new GetImages().execute();

        return view;
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        connectedToInternet = isConnected;

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

    private class GetImages extends AsyncTask<Void, Void, Void> {

        private String TAG = GetImages.class.getSimpleName();

        String photoURL = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if(!isItDestroyed) {
                if(connectedToInternet) {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }

        }

        @Override
        protected Void doInBackground(Void... voids) {
            if(!isItDestroyed) {
                if(connectedToInternet) {
                    try {
                        JSONObject singleImage = jsonArrayAlbum.getJSONObject(notifyAtIndex);
                        photoURL = singleImage.getString("id");

                        HttpURLConnection tempCon = (HttpURLConnection) new URL(photoURL).openConnection();
                        int tempConLength = tempCon.getContentLength();
                        if (tempCon != null) {
                            tempCon.disconnect();
                        }

                        File tempFile = new File(getActivity().getFilesDir(), photoURL.replaceAll("/", ""));
                        if (tempFile.exists()) {
                            isDownloading = (int) tempFile.length() != tempConLength;
                            if (isDownloading) {
                                //Log.e(TAG, "File name " + photoURL.replaceAll("/", "") + " exists, Partially.");
                                downloadPhoto(photoURL);
                            } else {
                                //Log.e(TAG, "File name " + photoURL.replaceAll("/", "") + " exists, Fully.");
                            }
                        } else {
                            //Log.e(TAG, "File name " + photoURL.replaceAll("/", "") + " doesn't exist.Downloading...");
                            downloadPhoto(photoURL);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        //Log.e(TAG, "Json parsing error: " + e.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                        //Log.e(TAG, "Json parsing error: " + e.getMessage());
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if(!isItDestroyed) {
                if(connectedToInternet) {
                    if (isDownloading) {
                        new GetImages().execute();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        imageFileNameList.add(photoURL.replaceAll("/", ""));
                        albumAdapter.notifyItemInserted(notifyAtIndex);
                        albumImageDialogFragment.albumPagerAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }

    private void downloadPhoto(String url) {
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
