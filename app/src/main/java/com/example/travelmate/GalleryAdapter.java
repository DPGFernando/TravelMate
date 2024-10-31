package com.example.travelmate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {
    private Context context;
    private List<String> imageUrls; // List to hold all image URLs

    public GalleryAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls; // Initialize with all image URLs
    }

    public static class Gallery {
        List<String> imageUrl;

        public Gallery() {}

        public List<String> getImageUrl() { return imageUrl; }
    }
    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_mapping_card, parent, false);
        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        // Load the image URL using Picasso
        Picasso.get().load(imageUrl).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imageUrls.size(); // Return the total number of images
    }


    public static class GalleryViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public GalleryViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imagemapView); // Replace with your ImageView ID
        }
    }
}
