package com.vrajdesai.myapplication.ui.bookings;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.zxing.WriterException;
import com.vrajdesai.myapplication.R;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class Bookings extends Fragment {

    private BookingsViewModel mViewModel;
    private FirebaseFirestore firebaseFirestore;
    private FirestoreRecyclerAdapter adapter;
    private RecyclerView bookinglist;
    Dialog dialog;
    private ImageView qrimg;
    Button cancelBtn;

    public static Bookings newInstance() {
        return new Bookings();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View fragment = inflater.inflate(R.layout.bookings_fragment, container, false);

        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.qr_dialog);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.dialog_background));
        }
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.getWindow().getAttributes().windowAnimations = R.style.animation;

        qrimg = dialog.findViewById(R.id.qrCode);
        cancelBtn = dialog.findViewById(R.id.cancelQR);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        firebaseFirestore = FirebaseFirestore.getInstance();
        bookinglist = fragment.findViewById(R.id.bookinglist);

        Query query =firebaseFirestore.collection("BookingDetails").
                whereEqualTo("UserId", FirebaseAuth.getInstance().getCurrentUser().getUid());

        FirestoreRecyclerOptions<BookingModel> option = new FirestoreRecyclerOptions.Builder<BookingModel>()
                .setQuery(query, BookingModel.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<BookingModel, BookingViewHolder>(option) {

            @NonNull
            @Override
            public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.show_bookings,
                        parent, false);
                return new BookingViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull BookingViewHolder holder, final int position, @NonNull BookingModel model) {
                holder.booking_place.setText(model.getPlace_name());
                holder.booking_address.setText(model.getAddress());
                holder.booking_timing.setText(model.getTiming().toString());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.show();
                        String data = getSnapshots().getSnapshot(position).getId();
                        QRGEncoder qrgEncoder = new QRGEncoder(data,null, QRGContents.Type.TEXT,500);
                        Bitmap qrBits = qrgEncoder.getBitmap();
                        qrimg.setImageBitmap(qrBits);
                    }
                });
            }
        };

            bookinglist.setHasFixedSize(true);
            bookinglist.setLayoutManager(new LinearLayoutManager(getContext()));
            bookinglist.setAdapter(adapter);

        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(BookingsViewModel.class);
    }

    private class BookingViewHolder extends RecyclerView.ViewHolder {

        private TextView booking_place;
        private TextView booking_address;
        private TextView booking_timing;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            booking_place = itemView.findViewById(R.id.booking_place);
            booking_address = itemView.findViewById(R.id.booking_add);
            booking_timing = itemView.findViewById(R.id.booking_time);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }
}