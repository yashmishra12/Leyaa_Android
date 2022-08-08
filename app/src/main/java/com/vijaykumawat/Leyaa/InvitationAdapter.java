package com.vijaykumawat.Leyaa;


import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class InvitationAdapter extends FirestoreRecyclerAdapter<Invitation_Data, InvitationAdapter.RoomHolder> {

    private OnItemClickListener listener;

    public InvitationAdapter(@NonNull FirestoreRecyclerOptions<Invitation_Data> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull RoomHolder holder, int position, @NonNull Invitation_Data model) {
        holder.senderName.setText(model.getSenderName());
        holder.roomName.setText(model.getRoomName());

    }

    @NonNull
    @Override
    public RoomHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.room_invitation,parent,false);

        return new RoomHolder(view);
    }

    class RoomHolder extends RecyclerView.ViewHolder {
        TextView senderName;
        TextView roomName;



        public RoomHolder(@NonNull View itemView) {
            super(itemView);
            senderName = itemView.findViewById(R.id.sendername);
            roomName = itemView.findViewById(R.id.room_title_inv);

            itemView.findViewById(R.id.imageButton2);




                //cancel
            itemView.findViewById(R.id.imageButton).setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    Toast.makeText(view.getContext(), "Rejected!" , Toast.LENGTH_SHORT ).show();


                }
            });

                //accept
            itemView.findViewById(R.id.imageButton2).setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){

                    Toast.makeText(view.getContext(), "Accepted!" , Toast.LENGTH_SHORT ).show();




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
