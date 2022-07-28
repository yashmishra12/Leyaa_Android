package com.vijaykumawat.Leyaa;

import android.content.ClipData;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vijaykumawat.Leyaa.databinding.ActivityMainBinding;

import android.content.ClipboardManager;
import android.widget.Toast;


@RequiresApi(api = Build.VERSION_CODES.N)
public class ProfileFragment extends Fragment {

    ActivityMainBinding binding;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    View view;


    String[] itemName = SanitizeItemName.realAssetName;
    int[] itemImages = {R.drawable.almond, R.drawable.apple, R.drawable.avacado, R.drawable.bagel, R.drawable.banana,
                         R.drawable.bandaid, R.drawable.battery, R.drawable.beer, R.drawable.bellpepper, R.drawable.biscuit, R.drawable.bodylotion,
                         R.drawable.bodywash, R.drawable.boot, R.drawable.bottle, R.drawable.bread, R.drawable.broccoli, R.drawable.broom, R.drawable.bucket,
                         R.drawable.bugspray, R.drawable.bulb, R.drawable.butter, R.drawable.cabbage};




//    view.

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        GridAdapter gridAdapter = new GridAdapter(getActivity(), itemName ,itemImages);
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        TextView email = (TextView) view.findViewById(R.id.email);
        TextView fullname = (TextView) view.findViewById(R.id.fullName);

        GridView gridView = (GridView) view.findViewById(R.id.gridView);

        gridView.setAdapter(gridAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = SanitizeItemName.sanitizeItemName(itemName[position]);
                Toast.makeText(getActivity(),"You Clicked on "+ name,Toast.LENGTH_SHORT).show();

            }
        });

        ImageView profileAvatar = (ImageView) view.findViewById(R.id.profileAvatar);

        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference documentReference = db.collection("users").document(userID);

//LOADING NAME AND EMAIL
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc.exists()) {
                            Log.d("TAG", "DocumentSnapshot data: " + doc.get("email"));
                            String userEmail  = (String) doc.get("email");
                            email.setText(userEmail);

                            String userName  = (String) doc.get("fullname");
                            fullname.setText(userName);

                            String userAvatar = (String) doc.get("avatar");
                            int resID = getResources().getIdentifier(userAvatar , "drawable", getActivity().getPackageName());

                            profileAvatar.setImageResource(resID);

                        } else {
                            Log.d("TAG", "DocumentSnapshot No such document");
                        }
                    }
                    else {
                        Log.d("TAG", "DocumentSnapshot get failed with ", task.getException());
                    }
                }
            });



        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Edit Text", email.getText().toString());
                clipboardManager.setPrimaryClip(clip);

                Toast.makeText(getActivity(), "Copied", Toast.LENGTH_SHORT).show();
            }
        });


        fullname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Edit Text", fullname.getText().toString());
                clipboardManager.setPrimaryClip(clip);

                Toast.makeText(getActivity(), "Copied", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}