package com.hz.myapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import java.util.List;
import android.widget.Space;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

/**
 * Created by zeee on 31-03-2018.
 */

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.MyViewHolder> {

    private List<byte[]> byteArrayList;
    private List<String> videoIDList;
    private Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder{

        public ImageView thumbnailView;
        public Space space;
        public ProgressBar progressBar;

        public MyViewHolder(View itemView) {
            super(itemView);
            thumbnailView = itemView.findViewById(R.id.thumbnail_imageview);
            space = itemView.findViewById(R.id.thumbnail_videoid_space);
            progressBar = itemView.findViewById(R.id.video_thumbnail_progressbar);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, PlayerActivity.class);
                    intent.putExtra("videoID", (String) space.getTag());
                    context.startActivity(intent);
                }
            });
        }
    }

    public VideoAdapter(List<byte[]> byteArrayList, List<String> videoIDList, Context context){
        this.byteArrayList = byteArrayList;
        this.videoIDList = videoIDList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_video_thumbnail, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        byte[] tempByteArray = byteArrayList.get(position);
        String tag = videoIDList.get(position);

        holder.progressBar.setVisibility(View.VISIBLE);

        holder.space.setTag(tag);

        Glide.with(context)
                .load(tempByteArray)
                .asBitmap()
                .listener(new RequestListener<byte[], Bitmap>() {
                    @Override
                    public boolean onException(Exception e, byte[] model, Target<Bitmap> target, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, byte[] model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(holder.thumbnailView);
    }

    @Override
    public int getItemCount() {
        return (byteArrayList != null ? byteArrayList.size() : 0);
    }
}


