package com.vijaykumawat.Leyaa;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class MemberDataAdapter extends RecyclerView.Adapter<MemberDataAdapter.MyViewHolder> {

    Context context;
    ArrayList<MemberData> userArrayList;

    public MemberDataAdapter(Context context, ArrayList<MemberData> userArrayList) {

        this.context = context;
        this.userArrayList = userArrayList;
    }

    @NonNull
    @Override
    public MemberDataAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.member_item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberDataAdapter.MyViewHolder holder, int position) {
        MemberData user = userArrayList.get(position);

        holder.fullname.setText(user.fullname);
        holder.email.setText(user.email);

        int resID = context.getResources().getIdentifier(user.avatar , "drawable", context.getPackageName());
//
        holder.avatar.setImageResource(resID);

    }


    @Override
    public int getItemCount() {
        return userArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView fullname, email;
        ImageView avatar;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            fullname = itemView.findViewById(R.id.memberName);
            email = itemView.findViewById(R.id.memberEmail);
            avatar = itemView.findViewById(R.id.memberDP);
        }
    }


}
