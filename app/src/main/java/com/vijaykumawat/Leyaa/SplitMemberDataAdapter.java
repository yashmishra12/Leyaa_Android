package com.vijaykumawat.Leyaa;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SplitMemberDataAdapter extends RecyclerView.Adapter<SplitMemberDataAdapter.MyViewHolder> {

    Context context;
    ArrayList<String> memberIDs;
    ArrayList<SplitMemberData> userArrayList;
    private OnItemClickListener listener;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String roomID = "ROOM ID";


    public SplitMemberDataAdapter(Context context, ArrayList<SplitMemberData> userArrayList, String roomID) {

        this.context = context;
        this.userArrayList = userArrayList;
        this.roomID = roomID;
    }

    @NonNull
    @Override
    public SplitMemberDataAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.bill_split_member_card, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SplitMemberDataAdapter.MyViewHolder holder, int position) {

        SplitMemberData user = userArrayList.get(position);
        holder.fullname.setText(user.getFullname());
        holder.memberID.setText(user.getUid());

        int resID = context.getResources().getIdentifier(user.avatar , "drawable", context.getPackageName());
        holder.avatar.setImageResource(resID);

       db.collection(roomID+"_BILLS")
                .whereEqualTo("contributor", user.getUid() )
                .whereEqualTo("payer", FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        final Double[] getItemPrice = {0.0};
                        List<DocumentSnapshot> documentSnapshotList = queryDocumentSnapshots.getDocuments();

                        for(DocumentSnapshot ds: documentSnapshotList) {
                            getItemPrice[0] = (Double) ds.get("itemPrice") + getItemPrice[0];
                            Log.d("TAG", "onSuccess: -----> "+(Double) ds.get("itemPrice"));
                        }

                        holder.get.setText(String.valueOf("$"+getItemPrice[0]));

                    }
                });


        db.collection(roomID+"_BILLS")
                .whereEqualTo("payer", user.getUid() )
                .whereEqualTo("contributor", FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        final Double[] payItemPrice = {0.0};
                        List<DocumentSnapshot> documentSnapshotList = queryDocumentSnapshots.getDocuments();

                        for(DocumentSnapshot ds: documentSnapshotList) {
                            payItemPrice[0] =+ (Double) ds.get("itemPrice");
                        }

                        holder.pay.setText(String.valueOf("$"+payItemPrice[0]));

                    }
                });



        holder.cardView.setOnClickListener(view -> {

            Intent intent = new Intent(context, SplitBill_Individual_Activity.class);
            intent.putExtra("memberID", user.getUid() );
            intent.putExtra("roomID", roomID );
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return userArrayList.size();
    }







    public  class MyViewHolder extends RecyclerView.ViewHolder {

        TextView fullname;
        ImageView avatar;
        TextView memberID;
        CardView cardView;
        TextView pay, get, net;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            fullname = itemView.findViewById(R.id.memberName);
            avatar = itemView.findViewById(R.id.memberDP);
            memberID = itemView.findViewById(R.id.memberID_SBI);
            cardView = itemView.findViewById(R.id.bill_split_member_card);
            pay = itemView.findViewById(R.id.pay);
            get = itemView.findViewById(R.id.get);



            itemView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view){
                    int position = getAbsoluteAdapterPosition();

                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(userArrayList.get(position));
                    }

                }
            });
        }
    }

    public void setOnItemClickListener(SplitMemberDataAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(SplitMemberData splitMemberData);

    }






}

