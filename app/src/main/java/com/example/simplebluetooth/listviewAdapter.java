package com.example.simplebluetooth;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class listviewAdapter extends BaseAdapter {
    private ArrayList<Model> productList;
    private Activity activity;
    public listviewAdapter(Activity activity, ArrayList<Model> productList) {
        super();
        this.activity = activity;
        this.productList = productList;
    }

    @Override
    public int getCount() {
        return productList.size();
    }

    @Override
    public Object getItem(int position) {
        return productList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder {
        TextView mNum;
        TextView mDate;
        TextView mSample;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        LayoutInflater inflater = activity.getLayoutInflater();
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listview_row, null);
            holder = new ViewHolder();
            holder.mNum = convertView.findViewById(R.id.num);
            holder.mDate = convertView.findViewById(R.id.date);
            holder.mSample = convertView.findViewById(R.id.sample);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Model item = productList.get(position);
        holder.mNum.setText(item.getNum());
        holder.mDate.setText(item.getDate());
        holder.mSample.setText(item.getSample());
        return convertView;
    }
}