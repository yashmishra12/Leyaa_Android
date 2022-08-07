package com.vijaykumawat.Leyaa;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

public class RoomAdapter extends FirestoreRecyclerAdapter<Room_Title, RoomAdapter.RoomHolder> {

    private OnItemClickListener listener;

    public RoomAdapter(@NonNull FirestoreRecyclerOptions<Room_Title> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull RoomHolder holder, int position, @NonNull Room_Title model) {
        holder.room_title.setText(model.getTitle());

    }

    @NonNull
    @Override
    public RoomHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.room_title,parent,false);
        return new RoomHolder(view);
    }

    class RoomHolder extends RecyclerView.ViewHolder {
        TextView room_title;

        public RoomHolder(@NonNull View itemView) {
            super(itemView);
            room_title = itemView.findViewById(R.id.room_title);


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
