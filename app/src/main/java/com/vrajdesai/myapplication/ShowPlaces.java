package com.vrajdesai.myapplication;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ShowPlaces extends AppCompatActivity {

    private FirebaseFirestore firebaseFirestore;
    private RecyclerView placesList;
    private FirestoreRecyclerAdapter adapter;
    private TextView date, time;
    private Dialog dialog;
    private Button confirm;
    private Button cancel;
    private TextView title;
    Calendar myCalendar = Calendar.getInstance();
    FirebaseFirestore db;
    int myear, mmonth, mdate;
    boolean isBooked = false;
    private String addr, email;
    private boolean status = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_places);

        firebaseFirestore = FirebaseFirestore.getInstance();
        placesList = findViewById(R.id.listPlaces);

        checkStatus();

        Toolbar toolbar = (Toolbar) findViewById(R.id.booking_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

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
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show(); }
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
            protected void onBindViewHolder(@NonNull final PlacesViewHolder holder, final int position, @NonNull PlacesModel model) {
                holder.place_name.setText(model.getName());
                holder.place_add.setText(model.getAddress());
                holder.place_open.setText("Timing: "+model.getOpen() + " To "+model.getClose());
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addr = getSnapshots().getSnapshot(position).getString("address");
                        email = getSnapshots().getSnapshot(position).getString("Email");
                        dialog.show();
                        title.setText(getSnapshots().getSnapshot(position).getString("Name"));
                    }
                });

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Places")
                        .child(getSnapshots().getSnapshot(position).getId()+"."+"jpg");
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String imageUrl = uri.toString();
                        Glide.with(getApplicationContext()).load(imageUrl).into(holder.placePhoto);
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
        private ImageView placePhoto;
        View mView;

        public PlacesViewHolder(@NonNull View itemView) {
            super(itemView);
            place_add = itemView.findViewById(R.id.place_address);
            place_name = itemView.findViewById(R.id.place_name);
            place_open = itemView.findViewById(R.id.time);
            placePhoto = itemView.findViewById(R.id.place_photo);
            mView = itemView;
        }
    }

    private void addBookingDetails() {
        if(myCalendar.getTime().after(Calendar.getInstance().getTime())) {

            if(checkStatus()) {
                addReminderInCalendar(title.getText().toString(), title.getText().toString() + "\n" + addr);
            }

            final Map<String,Object> addDetails = new HashMap<>();
            addDetails.put("Place_name", title.getText().toString());
            addDetails.put("UserId", FirebaseAuth.getInstance().getCurrentUser().getUid());
            addDetails.put("Timing", myCalendar.getTime());
            addDetails.put("address", addr);
            addDetails.put("Business_email", email);

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

    @GlideModule
    public class MyAppGlideModule extends AppGlideModule implements com.vrajdesai.myapplication.MyAppGlideModule {
    }

    private void addReminderInCalendar(String name, String desc) {
        Calendar cal = Calendar.getInstance();
        Uri EVENTS_URI = Uri.parse(getCalendarUriBase(true) + "events");
        ContentResolver cr = getContentResolver();
        TimeZone timeZone = TimeZone.getDefault();

        /** Inserting an event in calendar. */
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.CALENDAR_ID, 1);
        values.put(CalendarContract.Events.TITLE, name);
        values.put(CalendarContract.Events.DESCRIPTION, desc);
        values.put(CalendarContract.Events.ALL_DAY, 0);
        // event starts at 11 minutes from now
        values.put(CalendarContract.Events.DTSTART, myCalendar.getTimeInMillis());
        // ends 60 minutes from now
        values.put(CalendarContract.Events.DTEND, myCalendar.getTimeInMillis() + 60 * 60 * 1000);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.getID());
        values.put(CalendarContract.Events.HAS_ALARM, 1);
        Uri event = cr.insert(EVENTS_URI, values);

        // Display event id
        /** Adding reminder for event added. */
        Uri REMINDERS_URI = Uri.parse(getCalendarUriBase(true) + "reminders");
        values = new ContentValues();
        values.put(CalendarContract.Reminders.EVENT_ID, Long.parseLong(event.getLastPathSegment()));
        values.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
        values.put(CalendarContract.Reminders.MINUTES, 10);
        cr.insert(REMINDERS_URI, values);
    }

    /** Returns Calendar Base URI, supports both new and old OS. */
    private String getCalendarUriBase(boolean eventUri) {

        Uri calendarURI = null;
        try {
            if (android.os.Build.VERSION.SDK_INT <= 7) {
                calendarURI = (eventUri) ? Uri.parse("content://calendar/") : Uri.parse("content://calendar/calendars");
            } else {
                calendarURI = (eventUri) ? Uri.parse("content://com.android.calendar/") : Uri
                        .parse("content://com.android.calendar/calendars");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return calendarURI.toString();
    }

    private boolean checkStatus()
    {
        int result = getBaseContext().checkCallingOrSelfPermission(Manifest.permission.READ_CALENDAR);
        int result1 = getBaseContext().checkCallingOrSelfPermission(Manifest.permission.WRITE_CALENDAR);
        if(result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED)
        {
            status = true;
        }
        else {
            ActivityCompat.requestPermissions(ShowPlaces.this,
                    new String[]{Manifest.permission.READ_CALENDAR,
                            Manifest.permission.WRITE_CALENDAR},
                    1);
            result = getBaseContext().checkCallingOrSelfPermission(Manifest.permission.READ_CALENDAR);
            result1 = getBaseContext().checkCallingOrSelfPermission(Manifest.permission.WRITE_CALENDAR);
            if(result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED)
            {
                status = true;
            }
        }
        return status;
    }
}

