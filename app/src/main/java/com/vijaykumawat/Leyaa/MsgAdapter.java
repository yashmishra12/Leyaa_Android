package com.vijaykumawat.Leyaa;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class MsgAdapter extends FirestoreRecyclerAdapter<Message_Data, MsgAdapter.MsgHolder> {

    Context context;
    String roomID;

    public MsgAdapter(@NonNull FirestoreRecyclerOptions<Message_Data> options, Context context, String roomID) {
        super(options);
        this.context = context;
        this.roomID = roomID;
    }


    @Override
    protected void onBindViewHolder(@NonNull MsgHolder holder, int position, @NonNull Message_Data model) {
        holder.msg_text.setText(model.getText());
        Date date = model.getTimestamp();
        String pattern = "dd MMM, h:mm a";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String dateToShow = simpleDateFormat.format(date);
        holder.msg_date.setText(dateToShow);


        String current_user_id = FirebaseAuth.getInstance().getUid();
        String senderID = model.getSenderID();

        if (Objects.equals(current_user_id, senderID)) {
            holder.msg_sender.setText("You");
            holder.msg_text.setBackgroundResource(R.color.my_msg);
        }

        FirebaseFirestore.getInstance().collection("users").document(senderID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String avatar = (String)documentSnapshot.get("avatar");
                String fullname = (String)documentSnapshot.get("fullname");
                int resID = context.getResources().getIdentifier(avatar , "drawable", context.getPackageName());

                holder.profile_pic.setImageResource(resID);

                if (!Objects.equals(current_user_id, senderID)) {
                    holder.msg_sender.setText(fullname);
                    holder.msg_text.setBackgroundResource(R.color.others_msg);
                }

            }
        });

        holder.profile_pic.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
               if (Objects.equals(model.getSenderID(), FirebaseAuth.getInstance().getUid())) {
                   FirebaseFirestore.getInstance().collection(roomID).document(model.getId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {

                       @Override
                       public void onComplete(@NonNull Task<Void> task) {

                           Toast.makeText(context, "Message Deleted", Toast.LENGTH_SHORT).show();
                       }
                   });
               } else {
                   Toast.makeText(context, "Cannot delete other's message", Toast.LENGTH_SHORT).show();
               }
                return false;
            }
        });

    }

    @NonNull
    @Override
    public MsgHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_card, parent, false);
        return new MsgHolder(v);
    }

    class MsgHolder extends RecyclerView.ViewHolder {
        TextView msg_sender;
        TextView msg_text;
        TextView msg_date;
        ImageView profile_pic;


        public MsgHolder(@NonNull View itemView) {
            super(itemView);
            msg_text = itemView.findViewById(R.id.msg_text);
            msg_sender = itemView.findViewById(R.id.msg_sender);
            msg_date = itemView.findViewById(R.id.msg_date);
            profile_pic = itemView.findViewById(R.id.profile_pic);
        }
    }
}
