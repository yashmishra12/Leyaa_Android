package com.vijaykumawat.Leyaa;


import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class InvitationAdapter extends FirestoreRecyclerAdapter<Invitation_Data, InvitationAdapter.RoomHolder> {

    private OnItemClickListener listener;
    private OnItemClickListener listener2;
    FirebaseAuth mAuth;
    private FirebaseFirestore mstore = FirebaseFirestore.getInstance();

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

    public void deleteRequest(int position){
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    public void acceptRequest(int position){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Invitation_Data data=new Invitation_Data();
        String document_id  =  getSnapshots().getSnapshot(position).getReference().getId();
        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();
        final String[] roomID = new String[1];

        DocumentReference docRef = db.collection("roomRequest").document(document_id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {

                        roomID[0] = (String) documentSnapshot.get("roomID");
                        db.collection("rooms").document(roomID[0]).update("members",FieldValue.arrayUnion(userId));


                    }
                }
            }

        });


    }

    class RoomHolder extends RecyclerView.ViewHolder {
        TextView senderName;
        TextView roomName;



        public RoomHolder(@NonNull View itemView) {
            super(itemView);
            senderName = itemView.findViewById(R.id.sendername);
            roomName = itemView.findViewById(R.id.room_title_inv);


                //cancel
            itemView.findViewById(R.id.imageButton).setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    //deleteRequest(getAbsoluteAdapterPosition());
                    Toast.makeText(view.getContext(), "Rejected!" , Toast.LENGTH_SHORT ).show();

                }
            });

//            itemView.findViewById(R.id.imageButton).setOnClickListener(new View.OnClickListener(){
//                @Override
//                public void onClick(View view){
//                    int position = getAbsoluteAdapterPosition();
//
//
//
//                    if (position != RecyclerView.NO_POSITION && listener != null) {
//                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
//                    }
//
//
//
//                }
//            });



//          accept
            itemView.findViewById(R.id.imageButton2).setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    acceptRequest(getAbsoluteAdapterPosition());
                    Toast.makeText(view.getContext(), "Accepted!" , Toast.LENGTH_SHORT ).show();





                }
            });




//            itemView.findViewById(R.id.imageButton2).setOnClickListener(new View.OnClickListener(){
//                @Override
//                public void onClick(View view){
//                    int position = getAbsoluteAdapterPosition();
//
//                    if (position != RecyclerView.NO_POSITION && listener2 != null) {
//                        listener2.onItemClick(getSnapshots().getSnapshot(position), position);
//                    }
//
//
//
//                }
//            });

        }




    }
    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public interface OnItemClickListener2 {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    public void setOnItemClickListener2(OnItemClickListener listener2) {
        this.listener2 = listener2;
    }



}