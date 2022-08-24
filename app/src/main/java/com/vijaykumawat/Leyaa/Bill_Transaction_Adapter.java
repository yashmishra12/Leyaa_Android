package com.vijaykumawat.Leyaa;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;


import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Bill_Transaction_Adapter extends FirestoreRecyclerAdapter<Bill_Transaction_Data, Bill_Transaction_Adapter.RoomHolder> {

    private OnItemClickListener listener;
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    public Bill_Transaction_Adapter(@NonNull FirestoreRecyclerOptions<Bill_Transaction_Data> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull RoomHolder holder, int position, @NonNull Bill_Transaction_Data model) {
        holder.item_name.setText(model.getItemName());
        holder.item_price.setText(String.valueOf(model.getItemPrice()));


        Date date = model.getTimestamp();
        String pattern = "dd MMM, h:mm a";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String dateToShow = simpleDateFormat.format(date);

        holder.timestamp.setText(dateToShow);


    }

    @NonNull
    @Override
    public RoomHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bill_split_transaction_card,parent,false);
        return new RoomHolder(view);
    }


    public void deleteRequest(int position){
        getSnapshots().getSnapshot(position).getReference().delete();
    }
    class RoomHolder extends RecyclerView.ViewHolder {
        TextView item_name;
        TextView item_price;
        TextView timestamp;


        public RoomHolder(@NonNull View itemView) {
            super(itemView);
            item_name = itemView.findViewById(R.id.bill_item_name);
            item_price = itemView.findViewById(R.id.amount);
            timestamp = itemView.findViewById(R.id.date_time);


            itemView.findViewById(R.id.bill_del_flt_btn).setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    deleteRequest(getAbsoluteAdapterPosition());
                    Toast.makeText(view.getContext(), "Bill Deleted", Toast.LENGTH_SHORT ).show();
                }
            });
            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    int position = getAbsoluteAdapterPosition();


                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }

                }
            });
        }


    }
    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }


    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

}
