package com.example.travelmate;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class MapsAdapter extends RecyclerView.Adapter<MapsAdapter.MapViewHolder> {
    private List<MapData> mapDataList;
    private Context context;


    public MapsAdapter(List<MapData> mapDataList, Context context) {
        this.mapDataList = mapDataList;
        this.context = context;
    }

    public static class MapData {
        String name;
        List<String> imageUrl;

        public MapData() {}

        public String getName() { return name; }
        public List<String> getImageUrl() { return imageUrl; }
    }

    @NonNull
    @Override
    public MapViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_mapping_card, parent, false);
        return new MapViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MapViewHolder holder, int position) {
        MapData mapData = mapDataList.get(position);

        holder.placeName.setText(mapData.getName());

        if (mapData.getImageUrl() != null && !mapData.getImageUrl().isEmpty()) {
            String firstImageUrl = mapData.getImageUrl().get(0);
            Picasso.get().load(firstImageUrl).into(holder.placeImage);
        } else {
            // Set a placeholder or handle the case where there are no URLs
            holder.placeImage.setImageResource(R.drawable.img_4);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, gallery.class); // Replace with your target activity
            intent.putExtra("documentId", mapData.getName()); // Pass document ID
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mapDataList.size();
    }

    public static class MapViewHolder extends RecyclerView.ViewHolder {
        TextView placeName;
        ImageView placeImage;

        public MapViewHolder(@NonNull View itemView) {
            super(itemView);
            placeName = itemView.findViewById(R.id.location);
            placeImage = itemView.findViewById(R.id.imagemapView);
        }
    }
}

