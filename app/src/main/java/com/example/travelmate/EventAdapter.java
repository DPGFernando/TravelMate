package com.example.travelmate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;

import java.util.List;

public class EventAdapter extends ArrayAdapter<Event> {

    private Context context;
    private List<Event> events;

    public EventAdapter(Context context, List<Event> events) {
        super(context, 0, events);
        this.context = context;
        this.events = events;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.event_list_item, parent, false);
        }

        Event event = events.get(position);

        ImageView eventImage = convertView.findViewById(R.id.eventImage);
        TextView eventName = convertView.findViewById(R.id.eventName);
        TextView eventDate = convertView.findViewById(R.id.eventDate);
        TextView eventStartsAt = convertView.findViewById(R.id.eventStartsAt);
        TextView eventEntranceFee = convertView.findViewById(R.id.eventEntranceFee);

        eventName.setText(event.geteventName());
        eventDate.setText("Date: " + event.getDate());
        eventStartsAt.setText("Time: " + event.getStartsAt());
        eventEntranceFee.setText("$" + event.getEntranceFee());

        Glide.with(context).load(event.getPhotoimg()).into(eventImage);

        return convertView;
    }
}
