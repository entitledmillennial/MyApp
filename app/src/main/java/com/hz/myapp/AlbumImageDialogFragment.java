package com.hz.myapp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

/**
 * Created by zeee on 26-02-2018.
 */

public class AlbumImageDialogFragment extends AppCompatDialogFragment {

    private String TAG = AlbumImageDialogFragment.class.getSimpleName();
    private View view;
    private ViewPager albumViewPager;
    private TextView countTextView;

    protected AlbumPagerAdapter albumPagerAdapter = new AlbumPagerAdapter();;
    private List<String> imageFileNameList;
    private int position = 0;

    static AlbumImageDialogFragment newInstance() {
        AlbumImageDialogFragment albumImageDialogFragment = new AlbumImageDialogFragment();
        return albumImageDialogFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_album_dialog, container, false);
        albumViewPager = view.findViewById(R.id.album_viewpager);
        countTextView = view.findViewById(R.id.count_textview);

        imageFileNameList = (List<String>)getArguments().getSerializable("imageFileNameList");
        position = getArguments().getInt("position");

        albumViewPager.setAdapter(albumPagerAdapter);
        albumViewPager.addOnPageChangeListener(pageChangeListener);

        setCurrentItem(position);

        return view;
    }

    private void setCurrentItem(int position) {
        albumViewPager.setCurrentItem(position, false);
        displayMetaInfo(position);
    }

    ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            displayMetaInfo(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private void displayMetaInfo(int position) {
        countTextView.setText((position + 1) + " of " + imageFileNameList.size());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(AppCompatDialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    public class AlbumPagerAdapter extends PagerAdapter {

        private LayoutInflater layoutInflater;

        public AlbumPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.layout_album_image_fullscreen, container, false);

            ImageView fullscreenImageView = view.findViewById(R.id.album_image_fullscreen);

            String imageFileName = imageFileNameList.get(position);

            Glide.with(getContext()).load(new File(getActivity().getFilesDir(), imageFileName))
                    .into(fullscreenImageView);

            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return (imageFileNameList != null ? imageFileNameList.size() : 0);
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == ((View) obj);
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
