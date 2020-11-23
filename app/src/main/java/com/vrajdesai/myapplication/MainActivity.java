package com.vrajdesai.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final int IMAGE_REQUEST = 2;
    private Button logout;
    private TextView msg;
    private EditText name;
    private Button add, home;
    private ListView listview;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logout = findViewById(R.id.logout);
        msg = findViewById(R.id.msg);
        name = findViewById(R.id.nameId);
        add = findViewById(R.id.addName);
        listview = findViewById(R.id.list);

        home = findViewById(R.id.home_btn);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, HomeActivity.class));
            }
        });


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final DocumentReference documentReference = FirebaseFirestore.getInstance().collection("Users").
                document(user.getUid());

        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error!=null){
                    Log.d("Error","Error:"+error.getMessage());
                }
                else {
                    msg.setText(value.getString("Name"));
                }
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(MainActivity.this, "Logged Out!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, StartActivity.class));
            }
    });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImage();
            }
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();

//        Map<String, Object>city = new HashMap<>();
//        city.put("City","Vadodara");
//        city.put("State","Gujarat");
//        city.put("Country","India");
//
//        db.collection("Cities").document("VG").set(city).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if(task.isSuccessful()) {
//                    Toast.makeText(MainActivity.this,"Values Added", Toast.LENGTH_LONG).show();
//                }
//            }
//        });

//        Map<String, Object>data = new HashMap<>();
//        data.put("Capital","Gandhinagar");
//
//        db.collection("Cities").document("VG").set(data, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if(task.isSuccessful()) {
//                    Toast.makeText(MainActivity.this, "Merged Successfully", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

//        Map<String, Object> dt = new HashMap<>();
//        dt.put("City","Tokyo");
//        dt.put("Country","Japan");
//
//        db.collection("Cities").add(dt).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentReference> task) {
//                if(task.isSuccessful()) {
//                    Toast.makeText(MainActivity.this, "Added Successfully", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

//        DocumentReference ref = FirebaseFirestore.getInstance().collection("Cities").document("VG");
//        ref.update("Capital","Vadodara");
//            DocumentReference DocRef = FirebaseFirestore.getInstance().collection("Cities").document("VG");
//            DocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                    DocumentSnapshot doc = task.getResult();
//                    if(doc.exists()) {
//                        Toast.makeText(MainActivity.this, ""+doc.getData().toString(), Toast.LENGTH_SHORT).show();
//                    }
//                    else {
//                        Toast.makeText(MainActivity.this, "No data", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            });
//        FirebaseFirestore.getInstance().collection("Cities").whereEqualTo("Country","India")
//        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if(task.isSuccessful()) {
//                    for(QueryDocumentSnapshot doc: task.getResult() ) {
//                        Log.d("Document", doc.getId()+"=>"+doc.getData());
//                    }
//                }
//            }
//        });
    }


    private void openImage() {
        Intent intent = new Intent();
        intent.setType("/image");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==IMAGE_REQUEST && resultCode == RESULT_OK) {
            imageUri = data.getData();
            uploadImage();
        }
    }

    private void uploadImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading");
        pd.show();

        if(imageUri != null) {
            final StorageReference fileRef = FirebaseStorage.getInstance().getReference().child("uploads").child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

            fileRef.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                             String url = uri.toString();
                             Log.d("DownloadUrl", url);
                             pd.dismiss();
                            Toast.makeText(MainActivity.this, "Image Upload Successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}