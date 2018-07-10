package com.hz.myapp;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.youtube.player.YouTubeApiServiceUtil;
import com.google.android.youtube.player.YouTubeInitializationResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zeee on 26-02-2018.
 */

public class ContentActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    private static final String TAG = ContentActivity.class.getSimpleName();

    /** The request code when calling startActivityForResult to recover from an API service error. */
    private static final int RECOVERY_DIALOG_REQUEST = 1;

    private ConnectivityReceiver connectivityReceiver = ConnectivityReceiver.getInstance();


    private boolean internetConnected = false;
    private Toolbar toolbar;

    private TextView internetTextView;
    private TabLayout tabLayout;
    private CustomViewPager customViewPager;

    private AlbumFragment albumFragment = new AlbumFragment();
    private StarletFragment starletFragment = new StarletFragment();
    private VideosFragment videosFragment = new VideosFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        this.registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        internetTextView = findViewById(R.id.mainactivity_internet_textview);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        internetTextView.setSelected(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        customViewPager = findViewById(R.id.customviewpager);
        setupViewPager(customViewPager);

        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(customViewPager);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getText() == "VIDEOS") {
                    checkYouTubeApi();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                InterstitialAd interstitialAd = ((MyApp)getApplication()).getInterstitialAd();
                interstitialAd.setAdListener(new AdListener(){
                    @Override
                    public void onAdClosed() {
                        ((MyApp)getApplication()).setInterstitialAd();
                        ((MyApp)getApplication()).setInterstitialAdCloseEventOccurred(true);
                        super.onAdClosed();
                        finish();
                    }
                });
                if(interstitialAd != null && interstitialAd.isLoaded()){
                    interstitialAd.show();
                }
                else{
                    NavUtils.navigateUpFromSameTask(this);
                }
                return true;

            case R.id.action_reload:
                if(internetConnected) {
                    recreate();
                }
                return true;

            case R.id.action_rating:
                Uri uri = Uri.parse("market://details?id=" + this.getPackageName());
                Intent openPlayStore = new Intent(Intent.ACTION_VIEW, uri);
                //To go directly to our app instead of having to press back multiple times
                openPlayStore.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(openPlayStore);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + this.getPackageName())));
                }
                return true;

            case R.id.action_feedback:
                String[] address = {getString(R.string.email_address)};
                Intent openEmail = new Intent(Intent.ACTION_SENDTO);
                //To go directly to our app instead of having to press back multiple times
                openEmail.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                openEmail.setData(Uri.parse("mailto:")); // only email apps should handle this
                openEmail.putExtra(Intent.EXTRA_EMAIL, address);
                openEmail.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_email_subject));
                if (openEmail.resolveActivity(getPackageManager()) != null) {
                    startActivity(openEmail);
                }
                else{
                    Toast.makeText(this, getString(R.string.email_app_not_found), Toast.LENGTH_SHORT).show();
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void checkYouTubeApi() {
        YouTubeInitializationResult errorReason = YouTubeApiServiceUtil.isYouTubeApiServiceAvailable(this);
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show();
        } else if (errorReason != YouTubeInitializationResult.SUCCESS) {
            String errorMessage = String.format(getString(R.string.error_player), errorReason.toString());
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    private void setupViewPager(CustomViewPager customViewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(albumFragment, "ALBUM");
        adapter.addFragment(starletFragment,"STARLET");
        adapter.addFragment(videosFragment,"VIDEOS");
        customViewPager.setOffscreenPageLimit(2);
        customViewPager.setPagingEnabled(true);
        customViewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public void onBackPressed() {
        InterstitialAd interstitialAd = ((MyApp)getApplication()).getInterstitialAd();
        interstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                ((MyApp)getApplication()).setInterstitialAd();
                ((MyApp)getApplication()).setInterstitialAdCloseEventOccurred(true);
                super.onAdClosed();
                finish();
            }
        });
        if(interstitialAd != null && interstitialAd.isLoaded()){
            interstitialAd.show();
        }
        else{
            super.onBackPressed();
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        internetConnected = isConnected;
        if(isConnected){
            internetTextView.setVisibility(View.GONE);
        }
        else{
            internetTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        connectivityReceiver.setConnectivityReceiverListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        this.unregisterReceiver(connectivityReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
