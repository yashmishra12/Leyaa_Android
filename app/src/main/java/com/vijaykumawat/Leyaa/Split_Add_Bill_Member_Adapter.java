package com.vijaykumawat.Leyaa;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


public class Split_Add_Bill_Member_Adapter extends RecyclerView.Adapter<Split_Add_Bill_Member_Adapter.MyViewHolder> {

    Context context;
    ArrayList<SplitBill_Add_Bill_MemberData> userArrayList;

    public Split_Add_Bill_Member_Adapter(Context context, ArrayList<SplitBill_Add_Bill_MemberData> userArrayList) {

        this.context = context;
        this.userArrayList = userArrayList;
    }

    @NonNull
    @Override
    public Split_Add_Bill_Member_Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.split_add_bill_member_card, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Split_Add_Bill_Member_Adapter.MyViewHolder holder, int position) {
        SplitBill_Add_Bill_MemberData user = userArrayList.get(position);

        holder.fullname.setText(user.fullname);


        int resID = context.getResources().getIdentifier(user.avatar , "drawable", context.getPackageName());
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

            avatar = itemView.findViewById(R.id.memberDP);
        }
    }


}
