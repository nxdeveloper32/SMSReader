package com.example.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.Objects;

public class SMSReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("SMSReceiver", "onReceive called with action: " + intent.getAction());
        if (Objects.equals(intent.getAction(), "android.provider.Telephony.SMS_RECEIVED")) {
            Log.d("SMSReceiver", "SMS received");
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null) {
                    for (Object pdu : pdus) {
                        SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                        String sender = smsMessage.getDisplayOriginatingAddress();
                        String messageBody = smsMessage.getMessageBody();
                        Log.d("SMSReceiver", "Sender: " + sender + ", Message: " + messageBody);

                        // Forward the SMS data to the activity or handle it here
                    }
                } else {
                    Log.d("SMSReceiver", "No PDUs found");
                }
            } else {
                Log.d("SMSReceiver", "No extras in intent");
            }
        }
    }
}
