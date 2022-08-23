package com.vijaykumawat.Leyaa;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class SplitMemberDataAdapter extends RecyclerView.Adapter<SplitMemberDataAdapter.MyViewHolder> {

    Context context;
    ArrayList<String> memberIDs;
    ArrayList<SplitMemberData> userArrayList;
    private OnItemClickListener listener;
    FirebaseFirestore db = FirebaseFirestore.getInstance();



    public SplitMemberDataAdapter(Context context, ArrayList<SplitMemberData> userArrayList) {

        this.context = context;
        this.userArrayList = userArrayList;
    }

    @NonNull
    @Override
    public SplitMemberDataAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.bill_split_members, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SplitMemberDataAdapter.MyViewHolder holder, int position) {

        SplitMemberData user = userArrayList.get(position);
        holder.fullname.setText(user.getFullname());
        holder.memberID.setText(user.getUid());

        int resID = context.getResources().getIdentifier(user.avatar , "drawable", context.getPackageName());
        holder.avatar.setImageResource(resID);
    }


    @Override
    public int getItemCount() {
        return userArrayList.size();
    }







    public  class MyViewHolder extends RecyclerView.ViewHolder {

        TextView fullname;
        ImageView avatar;
        TextView memberID;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            fullname = itemView.findViewById(R.id.memberName);
            avatar = itemView.findViewById(R.id.memberDP);
            memberID = itemView.findViewById(R.id.memberID_SBI);

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

