package com.example.app;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Telephony;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> smsList = new ArrayList<>();
    private ArrayAdapter<String> adapter;  // Declare the adapter as a class-level variable
    private ListView listView;
    private static final int READ_SMS_PERMISSION_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, smsList);
        listView.setAdapter(adapter);

        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS) !=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_SMS}, READ_SMS_PERMISSION_CODE);
        }else{
            readSms();
        }

        // Register receiver for SMS
        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsUpdateReceiver, filter);

        Button testButton = findViewById(R.id.testButton);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Trigger the test broadcast
                Intent testIntent = new Intent("com.example.app.SMS_RECEIVED_TEST");
                testIntent.putExtra("smsSender", "TestSender");
                testIntent.putExtra("smsBody", "This is a test message");
                sendBroadcast(testIntent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(smsUpdateReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == READ_SMS_PERMISSION_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d("MainActivity", "SMS permission granted");
                readSms();
            } else {
                Log.d("MainActivity", "SMS permission denied");
            }
        }
    }

    private void  readSms(){
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(Telephony.Sms.CONTENT_URI, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                smsList.clear(); // Clear the list before adding new messages
                do {
                    String address = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                    String body = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY));
                    smsList.add("Sender: " + address + "\nMessage: " + body);
                } while (cursor.moveToNext());

                // Notify adapter after adding all SMS
                runOnUiThread(() -> adapter.notifyDataSetChanged());
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception to identify any issues
        } finally {
            if (cursor != null) {
                cursor.close(); // Ensure the cursor is closed to avoid memory leaks
            }
        }
    }
    private final BroadcastReceiver smsUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("SMS_RECEIVED_ACTION")) {
                String sender = intent.getStringExtra("smsSender");
                String body = intent.getStringExtra("smsBody");

                smsList.add("Sender: " + sender + "\nMessage: " + body);
                adapter.notifyDataSetChanged();
            }
        }
    };

}