package com.vrajdesai.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ShowPlaces extends AppCompatActivity {

    private FirebaseFirestore firebaseFirestore;
    private RecyclerView placesList;
    private FirestoreRecyclerAdapter adapter;
    private TextView date, time;
    private Dialog dialog;
    private Button confirm;
    private Button cancel;
    private int hour1, minute1;
    private TextView title;
    Calendar myCalendar = Calendar.getInstance();
    FirebaseFirestore db;
    int myear, mmonth, mdate;
    boolean isBooked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_places);

        firebaseFirestore = FirebaseFirestore.getInstance();
        placesList = findViewById(R.id.listPlaces);

        dialog = new Dialog(ShowPlaces.this);

        dialog.setContentView(R.layout.booking_dialog);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_background));
        }
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.getWindow().getAttributes().windowAnimations = R.style.animation;

        date = dialog.findViewById(R.id.date);
        time = dialog.findViewById(R.id.time);
        confirm = dialog.findViewById(R.id.confirm);
        cancel = dialog.findViewById(R.id.cancel);
        title = dialog.findViewById(R.id.place_title);


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatePickerDialog.OnDateSetListener date1 = new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        // TODO Auto-generated method stub

                        myear = year;
                        mmonth = monthOfYear;
                        mdate = dayOfMonth;

                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateLabel();
                    }
                };

                new DatePickerDialog(ShowPlaces.this, date1, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(ShowPlaces.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        time.setText( selectedHour + ":" + selectedMinute);
                        myCalendar.set(Calendar.HOUR, selectedHour);
                        myCalendar.set(Calendar.MINUTE, selectedMinute);
                        myCalendar.set(Calendar.SECOND, 0);
                    }
                }, hour, minute, false);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });


        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isBooked = false;
                db = FirebaseFirestore.getInstance();
                CollectionReference cref = db.collection("BookingDetails");
                Query q1 = cref.whereEqualTo("Place_name",""+title.getText().toString());
                q1.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentSnapshot ds: queryDocumentSnapshots) {
                            Date dt = ds.getTimestamp("Timing").toDate();
                            System.out.println("Data From Firebase: "+dt);
                            System.out.println("User Input"+myCalendar.getTime());
                            if(dt.toString().compareTo(myCalendar.getTime().toString()) == 0) {
                                isBooked = true;
                                Toast.makeText(ShowPlaces.this, "Sorry already Booked", Toast.LENGTH_SHORT).show();
                            }
                        }
                        if(!isBooked) {
                            addBookingDetails();
                        }
                    }
                }).addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        addBookingDetails();
                    }
                });
            }
        });



        String msg = getIntent().getStringExtra("Category");
        Query query = firebaseFirestore.collection("Places").whereEqualTo("Category", "" + msg);

        FirestoreRecyclerOptions<PlacesModel> option = new FirestoreRecyclerOptions.Builder<PlacesModel>()
                .setQuery(query, PlacesModel.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<PlacesModel, PlacesViewHolder>(option) {
            @NonNull
            @Override
            public PlacesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_places, parent, false);
                return new PlacesViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull PlacesViewHolder holder, final int position, @NonNull PlacesModel model) {
                holder.place_name.setText(model.getName());
                holder.place_add.setText(model.getAddress());
                holder.place_open.setText(model.getOpen() + "");
                holder.place_close.setText(model.getClose() + "");
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String refid = getSnapshots().getSnapshot(position).getId();
                        dialog.show();
                        title.setText(getSnapshots().getSnapshot(position).getString("Name"));
                    }
                });
            }
        };

        placesList.setHasFixedSize(true);
        placesList.setLayoutManager(new LinearLayoutManager(this));
        placesList.setAdapter(adapter);
    }

    private void updateLabel() {
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        date.setText(sdf.format(myCalendar.getTime()));
    }

    private class PlacesViewHolder extends RecyclerView.ViewHolder {

        private TextView place_add;
        private TextView place_name;
        private TextView place_open;
        private TextView place_close;
        View mView;

        public PlacesViewHolder(@NonNull View itemView) {
            super(itemView);
            place_add = itemView.findViewById(R.id.place_address);
            place_name = itemView.findViewById(R.id.place_name);
            place_open = itemView.findViewById(R.id.open_time);
            place_close = itemView.findViewById(R.id.close_time);
            mView = itemView;
        }
    }

    private void addBookingDetails() {
        if(myCalendar.getTime().after(Calendar.getInstance().getTime())) {

            final Map<String,Object> addDetails = new HashMap<>();
            addDetails.put("Place_name", title.getText().toString());
            addDetails.put("UserId", FirebaseAuth.getInstance().getCurrentUser().getUid());
            addDetails.put("Timing", myCalendar.getTime());

            db.collection("BookingDetails").add(addDetails)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {

                            Toast.makeText(ShowPlaces.this,
                                    "Booked Successfully", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    });
        }
        else {
            Toast.makeText(ShowPlaces.this, "Sorry no booking available",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }
}

