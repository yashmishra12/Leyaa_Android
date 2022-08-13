package com.vijaykumawat.Leyaa;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class GridAdapter extends BaseAdapter {
    Context context;
    String[] items;
    int[] image;

    LayoutInflater inflater;

    public GridAdapter(Context context, String[] items, int[] image) {
        this.context = context;
        this.items = items;
        this.image = image;
    }

    @Override
    public int getCount() {
        return items.length;
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
    public View getView(int position, View convertView, ViewGroup parent) {


        if (inflater == null)
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null){

            convertView = inflater.inflate(R.layout.grid_item,null);

        }




        ImageView imageView = convertView.findViewById(R.id.grid_image);

//        if (position!=169){
//
//        }
        imageView.setImageResource(image[position]);

//       try {
//           imageView.setImageResource(image[position]);
//       } catch (Error e) {
//           System.out.println(e);
//       }

        return convertView;
    }
}
