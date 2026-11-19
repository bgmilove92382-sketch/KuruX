package com.ninjamaxyt.kuruxcompanion;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

public class PairingReceiver extends BroadcastReceiver {
    public static final String KEY_PAIRING_CODE = "key_pairing_code";
    public static final String CHANNEL_ID = "kurux_adb_pairing";
    public static final int NOTIF_ID = 2001;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            CharSequence code = remoteInput.getCharSequence(KEY_PAIRING_CODE);
            if (code != null && code.toString().trim().length() >= 6) {
                String cleanCode = code.toString().trim();
                SharedPreferences prefs = context.getSharedPreferences("kurux_prefs", Context.MODE_PRIVATE);
                prefs.edit()
                        .putBoolean("adb_active", true)
                        .putString("adb_mode", "Wireless ADB Paired (Code: " + cleanCode + ")")
                        .apply();

                NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    nm.createNotificationChannel(new NotificationChannel(CHANNEL_ID, "ADB Pairing", NotificationManager.IMPORTANCE_HIGH));
                }
                Notification done = new Notification.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_kurux_logo)
                        .setContentTitle("KuruX ADB Engine Running")
                        .setContentText("Paired (" + cleanCode + ") • Apps & Modules Unlocked")
                        .setAutoCancel(true)
                        .build();
                nm.notify(NOTIF_ID, done);

                Intent uiUpdate = new Intent("com.ninjamaxyt.kuruxcompanion.ADB_PAIRED");
                uiUpdate.setPackage(context.getPackageName());
                context.sendBroadcast(uiUpdate);
            }
        }
    }
}
