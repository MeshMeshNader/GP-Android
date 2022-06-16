package com.grad.gp.Home.Gallery;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.grad.gp.R;

import java.util.ArrayList;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {

    Context context;
    ArrayList<String> personNames;
    ArrayList<String> personUrls;

    public GalleryAdapter(Context context, ArrayList<String> personNames, ArrayList<String> personUrls) {
        this.context = context;
        this.personNames = personNames;
        this.personUrls = personUrls;
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_gallery, parent, false);
        GalleryViewHolder galleryViewHolder = new GalleryViewHolder(view);
        return galleryViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
        holder.mPersonName.setText(personNames.get(position));
        try {
            Glide.with(context).load(personUrls.get(position)).into(holder.mPersonImage);
        } catch (Exception e) {
            Log.e("Gallery Adapter", "onBindViewHolder: " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return personNames.size();
    }

    class GalleryViewHolder extends RecyclerView.ViewHolder {
        ImageView mPersonImage;
        TextView mPersonName;

        public GalleryViewHolder(@NonNull View itemView) {
            super(itemView);
            mPersonImage = itemView.findViewById(R.id.item_gallery_image);
            mPersonName = itemView.findViewById(R.id.item_gallery_name);
        }
    }

}
