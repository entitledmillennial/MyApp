package com.hz.myapp;

/**
 * Created by zeee on 31-01-2018.
 */

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class VideosFragment extends Fragment {

    private static final String TAG = VideosFragment.class.getSimpleName();

    private YouTubeThumbnailView youTubeThumbnailView;
    private YouTubeThumbnailLoader youTubeThumbnailLoader;
    private RecyclerView recyclerView;
    private VideoAdapter videoAdapter;
    private EndlessRecyclerViewScrollListener scrollListener;
    private ProgressBar progressBar;
    private TextView reloadTextView;

    private List<byte[]> byteArrayList = new ArrayList<>();
    private List<String> videoIDList = new ArrayList<>();

    private String videoID = "";
    private String playlistID = "";
    private int playlistIndexChecked = 0;
    private int consecutiveThumbnailErrors = 0;     //limit for no thumbnails in a playlist.
    private int totalThumbnailErrors = 0;
    private int notifyAtIndex = 0;
    private static final int NUMBER_OF_INACCESSIBLE_THUMBNAILS_PERMITTED_IN_A_PLAYLIST = 10;
    private boolean youtubeThumbnailLoaderReleased = false;
    private boolean playlistEnded = false;

    public VideosFragment(){
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_videos, container, false);

        progressBar = view.findViewById(R.id.video_fragment_progressbar);
        reloadTextView = view.findViewById(R.id.reload_advice_textview);

        recyclerView = view.findViewById(R.id.recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if(!playlistEnded) {
                    notifyAtIndex = page;
                    progressBar.setVisibility(View.VISIBLE);
                    reloadTextView.setVisibility(View.VISIBLE);
                    loadNextThumbnail();
                }
            }
        };
        recyclerView.addOnScrollListener(scrollListener);

        videoAdapter = new VideoAdapter(byteArrayList, videoIDList, getActivity());
        recyclerView.setAdapter(videoAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(100);

        youTubeThumbnailView = new YouTubeThumbnailView(getActivity());

        new GetPlaylistID().execute();      //youTubeThumbnailView initialized in this task's post execute.

        return view;
    }

    private class InitializationListener implements YouTubeThumbnailView.OnInitializedListener {

        @Override
        public void onInitializationSuccess(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader youTubeThumbnailLoader) {
            //Log.e(TAG, "inside onInitializationSuccess");
            VideosFragment.this.youTubeThumbnailLoader = youTubeThumbnailLoader;
            VideosFragment.this.youTubeThumbnailLoader.setOnThumbnailLoadedListener(new ThumbnailListener());

            if (VideosFragment.this.youTubeThumbnailLoader != null && playlistID != "") {
                VideosFragment.this.youTubeThumbnailLoader.setPlaylist(playlistID); // loading the first thumbnail will kick off demo
            }
        }

        @Override
        public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView, YouTubeInitializationResult youTubeInitializationResult) {
            //Log.e(TAG, "inside onInitializationFailure");
        }
    }

    private void loadNextThumbnail() {
        if(!youtubeThumbnailLoaderReleased) {
            if (youTubeThumbnailLoader.hasNext()) {
                youTubeThumbnailLoader.next();
            } else {
                playlistEnded = true;
                progressBar.setVisibility(View.GONE);
                reloadTextView.setVisibility(View.GONE);
                //Log.e(TAG, "hasNext()=false, playlistIndexChecked=" + playlistIndexChecked);
            }
        }
    }


    private class ThumbnailListener implements YouTubeThumbnailLoader.OnThumbnailLoadedListener{

        @Override
        public void onThumbnailLoaded(YouTubeThumbnailView youTubeThumbnailView, String s) {
            consecutiveThumbnailErrors = 0;
            youTubeThumbnailView.setTag(s);

            new GetThumbnail().execute(youTubeThumbnailView);

        }

        @Override
        public void onThumbnailError(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader.ErrorReason errorReason) {
            //Log.e(TAG, "inside onThumbnailError");
            totalThumbnailErrors++;
            consecutiveThumbnailErrors++;

            if (consecutiveThumbnailErrors < NUMBER_OF_INACCESSIBLE_THUMBNAILS_PERMITTED_IN_A_PLAYLIST && youTubeThumbnailLoader != null && playlistID != "") {
                youTubeThumbnailLoader.setPlaylist(playlistID, ++playlistIndexChecked);
            }
            else{
                loadNextThumbnail();
            }
        }
    }

    private class GetThumbnail extends AsyncTask<YouTubeThumbnailView, Void, byte[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected byte[] doInBackground(YouTubeThumbnailView... youTubeThumbnailViews) {
            videoID = (String)youTubeThumbnailViews[0].getTag();

            byte[] byteArray;
            try {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) youTubeThumbnailViews[0].getDrawable();
                Bitmap bitmapOriginal = bitmapDrawable.getBitmap();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmapOriginal.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byteArray = byteArrayOutputStream.toByteArray();
            }
            catch(OutOfMemoryError error){
                error.printStackTrace();
                return null;
            }

            return byteArray;
        }

        @Override
        protected void onPostExecute(byte[] byteArray) {
            super.onPostExecute(byteArray);

            if(byteArray != null) {
                byteArrayList.add(byteArray);
                videoIDList.add(videoID);
            }

            //Log.e(TAG, "playlist checked ="+ playlistIndexChecked);
            //Log.e(TAG, "total errors ="+ (totalThumbnailErrors));
            //Log.e(TAG, "notify item at (from formula) = "+ (playlistIndexChecked-totalThumbnailErrors));
            //Log.e(TAG, "notify item at (from scroll listener callback) = "+ notifyAtIndex);

            videoAdapter.notifyItemInserted(notifyAtIndex);
            playlistIndexChecked++;
            progressBar.setVisibility(View.GONE);
            reloadTextView.setVisibility(View.GONE);
        }
    }

    private class GetPlaylistID extends AsyncTask<Void, Void, Void> {

        private String TAG = GetPlaylistID.class.getSimpleName();

        private String jsonString;

        @Override
        protected Void doInBackground(Void... voids) {
            jsonString = ((MyApp)getActivity().getApplication()).getResponseString();

            if(jsonString != null){
                try{
                    JSONObject topmostJSONObject = new JSONObject(jsonString);
                    JSONObject playlist = topmostJSONObject.getJSONObject("playlist");

                    playlistID = playlist.getString("id");
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

            youTubeThumbnailView.initialize(getString(R.string.developer_key), new InitializationListener());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(youTubeThumbnailLoader != null) {
            youtubeThumbnailLoaderReleased = true;
            youTubeThumbnailLoader.release();
        }
    }
}
