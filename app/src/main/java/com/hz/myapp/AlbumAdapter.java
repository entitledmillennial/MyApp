package com.hz.myapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.util.List;

/**
 * Created by zeee on 21-02-2018.
 */

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {

    private static final String TAG = AlbumAdapter.class.getSimpleName();

    private Context context;
    private List<String> imageFileNameList;

    private View albumView;

    public AlbumAdapter(Context context, List<String> imageFileNameList){
        this.context = context;
        this.imageFileNameList = imageFileNameList;
    }

    public class AlbumViewHolder extends RecyclerView.ViewHolder{
        protected ImageView albumThumbnail;
        protected ProgressBar progressBar;

        public AlbumViewHolder(View itemView) {
            super(itemView);
            albumThumbnail = itemView.findViewById(R.id.album_thumbnail);
            progressBar = itemView.findViewById(R.id.album_thumbnail_progressbar);
        }
    }

    @Override
    public AlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        albumView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_album_thumbnail, parent, false);

        return new AlbumViewHolder(albumView);
    }

    @Override
    public void onBindViewHolder(final AlbumViewHolder holder, int position) {
        holder.progressBar.setVisibility(View.VISIBLE);

        Glide.with(context).load(new File(context.getFilesDir(), imageFileNameList.get(position)))
                .centerCrop()
                .listener(new RequestListener<File, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, File model, Target<GlideDrawable> target, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        holder.albumThumbnail.setImageResource(R.drawable.placeholder_gif);
                        if(e != null) {
                            e.printStackTrace();
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, File model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(holder.albumThumbnail);
    }

    @Override
    public int getItemCount() {
        return (imageFileNameList != null ? imageFileNameList.size() : 0);
    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private AlbumAdapter.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final AlbumAdapter.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildAdapterPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
}
