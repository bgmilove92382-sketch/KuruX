package com.ninjamaxyt.kuruxcompanion;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + .5f); }
    private TextView text(String s, int size) { TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(0xff172033); t.setPadding(dp(4),dp(10),dp(4),dp(10)); return t; }
    private Button button(String s) { Button b = new Button(this); b.setText(s); return b; }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(22),dp(30),dp(22),dp(20)); root.setBackgroundColor(0xfff5f8ff);
        root.addView(text("KuruX Companion",26));
        root.addView(text("Real Gaming Floating Panel",16));
        root.addView(text("Allow display-over-other-apps permission, then start the floating button. Drag it anywhere; tap to open the panel.",14));
        Button permission = button("1. ALLOW DISPLAY OVER OTHER APPS");
        permission.setOnClickListener(v -> { if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) { Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName())); startActivity(i); } else startOverlay(); });
        root.addView(permission);
        Button start = button("2. START FLOATING PANEL"); start.setOnClickListener(v -> { if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) { startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName()))); } else startOverlay(); }); root.addView(start);
        Button stop = button("STOP FLOATING PANEL"); stop.setOnClickListener(v -> stopService(new Intent(this, OverlayService.class))); root.addView(stop);
        root.addView(text("Note: Game launch buttons work when the game is installed. Panel switches currently save local profile preferences only; they do not change CPU/GPU clocks, RAM policy, touch driver, or FPS. Those require a supported privileged AxManager/Shizuku bridge and device-specific testing.",13));
        setContentView(root);
    }
    private void startOverlay() { Intent i = new Intent(this, OverlayService.class); if (Build.VERSION.SDK_INT >= 26) startForegroundService(i); else startService(i); }
}
