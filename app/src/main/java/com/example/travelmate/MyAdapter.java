package com.example.travelmate;

import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.core.Context;

import java.util.ArrayList;

public class MyAdapter extends BaseAdapter {

        private ArrayList<DataClass> dataList;
        private Context Context ;
        LayoutInflater layoutInflater;

    public MyAdapter(com.google.firebase.database.core.Context context) {
        Context = context;
    }

    public MyAdapter(ArrayList<DataClass> dataList) {
        this.dataList = dataList;
    }

    public MyAdapter() {
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(layoutInflater==null){

            layoutInflater=(layoutInflater) Context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }
        if (view==null){
            view=layoutInflater.inflate(R.layout.grid_item,null);

        }
        ImageView gridImage = view.findViewById(R.id.gridImage);
        TextView gridCaption = view.findViewById(R.id.gridCaption);

        Glide.with(context).load(dataList.get(i).getImageURL())into(gridImage);
        gridCaption.setText(dataList.get(i).getCaption());
        return view;

        return null;
    }
}
