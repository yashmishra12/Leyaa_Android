package com.vijaykumawat.Leyaa;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemHolder> {

    ArrayList <HashMap<String, String>> data_models;
    private Context context;

    public ItemAdapter(ArrayList<HashMap<String, String>> data_models) {
        this.data_models = data_models;
    }


    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_view, parent, false);
        context = parent.getContext();
        return new ItemHolder(v);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        holder.item_name.setText(data_models.get(position).get("name"));
        holder.item_qty.setText(data_models.get(position).get("qty"));
        holder.item_desc.setText(data_models.get(position).get("desc"));

        String avatar = SanitizeItemName.sanitizeItemName(data_models.get(position).get("name"));
        int resID = context.getResources().getIdentifier(avatar , "drawable", context.getPackageName());

        holder.item_image.setImageResource(resID);

//        String itemID = data_models.get(position).get("id");
//
//        holder.item_del_flt_btn.setOnClickListener(view -> {
//            FirebaseFirestore db = FirebaseFirestore.getInstance();
//            db.collection("rooms").document()
//        });



    }

    @Override
    public int getItemCount() {
        return data_models.size();
    }

    class ItemHolder extends RecyclerView.ViewHolder {

        TextView item_name;
        TextView item_qty;
        TextView item_desc;
        ImageView item_image;
        FloatingActionButton item_del_flt_btn;
        FloatingActionButton item_edit_flt_btn;


        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            item_name = itemView.findViewById(R.id.item_name);
            item_qty = itemView.findViewById(R.id.item_qty);
            item_desc = itemView.findViewById(R.id.item_desc);
            item_image = itemView.findViewById(R.id.item_image);
            item_del_flt_btn = itemView.findViewById(R.id.item_del_flt_btn);
            item_edit_flt_btn = itemView.findViewById(R.id.item_edit_flt_btn);

        }
    }
}
