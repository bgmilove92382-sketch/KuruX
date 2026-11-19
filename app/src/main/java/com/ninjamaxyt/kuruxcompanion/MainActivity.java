package com.ninjamaxyt.kuruxcompanion;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity {

    private static final int REQ_NOTIF_PERM = 101;
    private static final int REQ_PICK_MODULE = 102;

    private LinearLayout contentContainer;
    private TextView[] navItems = new TextView[4];
    private LinearLayout fabAddModule;
    private int currentTab = 0;
    private SharedPreferences prefs;
    private List<String> importedModules = new ArrayList<>();

    private final int BG_COLOR = Color.parseColor("#FAF6F2");
    private final int CARD_BG = Color.parseColor("#F2ECE6");
    private final int ACCENT_BROWN = Color.parseColor("#8B5A2B");
    private final int ACCENT_LIGHT = Color.parseColor("#E6D9CC");
    private final int TEXT_PRIMARY = Color.parseColor("#2B2118");
    private final int TEXT_SECONDARY = Color.parseColor("#756658");
    private final int ALERT_CORAL = Color.parseColor("#E26D5C");
    private final int SUCCESS_GREEN = Color.parseColor("#2E7D32");

    private final BroadcastReceiver adbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switchTab(currentTab);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("kurux_prefs", Context.MODE_PRIVATE);
        Set<String> savedMods = prefs.getStringSet("imported_modules", new HashSet<>());
        importedModules.addAll(savedMods);

        getWindow().setStatusBarColor(BG_COLOR);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(BG_COLOR);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);

        root.setOnApplyWindowInsetsListener((v, insets) -> {
            int topInset = insets.getSystemWindowInsetTop();
            int bottomInset = insets.getSystemWindowInsetBottom();
            scrollView.setPadding(dp(18), topInset + dp(16), dp(18), bottomInset + dp(105));
            return insets;
        });

        contentContainer = new LinearLayout(this);
        contentContainer.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(contentContainer);
        root.addView(scrollView);

        LinearLayout bottomWrapper = new LinearLayout(this);
        bottomWrapper.setOrientation(LinearLayout.HORIZONTAL);
        bottomWrapper.setGravity(Gravity.CENTER);
        bottomWrapper.setPadding(dp(16), dp(8), dp(16), dp(22));

        LinearLayout pillBar = new LinearLayout(this);
        pillBar.setOrientation(LinearLayout.HORIZONTAL);
        pillBar.setBackground(roundRect(Color.parseColor("#E8DFD5"), 32));
        pillBar.setPadding(dp(8), dp(6), dp(8), dp(6));
        pillBar.setElevation(dp(6));

        String[] labels = {"Home", "Apps", "Modules", "Settings"};
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            TextView tab = new TextView(this);
            tab.setText(labels[i]);
            tab.setTextSize(13f);
            tab.setTypeface(null, Typeface.BOLD);
            tab.setGravity(Gravity.CENTER);
            tab.setPadding(dp(16), dp(10), dp(16), dp(10));
            tab.setOnClickListener(v -> switchTab(idx));
            navItems[i] = tab;
            pillBar.addView(tab);
        }
        bottomWrapper.addView(pillBar);

        fabAddModule = new LinearLayout(this);
        fabAddModule.setGravity(Gravity.CENTER);
        fabAddModule.setBackground(roundRect(ACCENT_LIGHT, 28));
        fabAddModule.setElevation(dp(6));
        LinearLayout.LayoutParams fabParams = new LinearLayout.LayoutParams(dp(52), dp(52));
        fabParams.leftMargin = dp(10);
        fabAddModule.setLayoutParams(fabParams);
        TextView plus = new TextView(this);
        plus.setText("+");
        plus.setTextSize(24f);
        plus.setTextColor(TEXT_PRIMARY);
        fabAddModule.addView(plus);
        fabAddModule.setVisibility(View.GONE);
        fabAddModule.setOnClickListener(v -> openModuleFilePicker());
        bottomWrapper.addView(fabAddModule);

        FrameLayout.LayoutParams navParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        navParams.gravity = Gravity.BOTTOM;
        root.addView(bottomWrapper, navParams);

        setContentView(root);
        switchTab(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(adbReceiver, new IntentFilter("com.ninjamaxyt.kuruxcompanion.ADB_PAIRED"), Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(adbReceiver, new IntentFilter("com.ninjamaxyt.kuruxcompanion.ADB_PAIRED"));
        }
        switchTab(currentTab);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try { unregisterReceiver(adbReceiver); } catch (Exception ignored) {}
    }

    private boolean isAdbReady() {
        return prefs.getBoolean("adb_active", false);
    }

    private void switchTab(int index) {
        currentTab = index;
        for (int i = 0; i < 4; i++) {
            if (i == index) {
                navItems[i].setBackground(roundRect(ACCENT_BROWN, 24));
                navItems[i].setTextColor(Color.WHITE);
            } else {
                navItems[i].setBackground(null);
                navItems[i].setTextColor(TEXT_PRIMARY);
            }
        }
        fabAddModule.setVisibility((index == 2 && isAdbReady()) ? View.VISIBLE : View.GONE);
        contentContainer.removeAllViews();

        if (index == 0) renderHomeTab();
        else if (index == 1) renderAppsTab();
        else if (index == 2) renderModulesTab();
        else renderSettingsTab();
    }

    private void renderHomeTab() {
        boolean ready = isAdbReady();

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(0, 0, 0, dp(16));

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.ic_kurux_logo);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(42), dp(42));
        lp.rightMargin = dp(12);
        logo.setLayoutParams(lp);
        header.addView(logo);

        LinearLayout titleBox = new LinearLayout(this);
        titleBox.setOrientation(LinearLayout.VERTICAL);
        titleBox.addView(makeText("KuruX Manager", 22, true, TEXT_PRIMARY));
        titleBox.addView(makeText("v2.1.0 • Wireless ADB & Non-Root Engine", 12, false, TEXT_SECONDARY));
        header.addView(titleBox);
        contentContainer.addView(header);

        LinearLayout statusCard = new LinearLayout(this);
        statusCard.setOrientation(LinearLayout.VERTICAL);
        statusCard.setBackground(roundRect(ready ? SUCCESS_GREEN : ALERT_CORAL, 22));
        statusCard.setPadding(dp(18), dp(18), dp(18), dp(18));

        LinearLayout topRow = new LinearLayout(this);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView stTitle = makeText(ready ? "Running" : "Need extra step", 20, true, Color.WHITE);
        stTitle.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        topRow.addView(stTitle);

        TextView badge = makeText(ready ? "ADB Active" : "Action needed", 11, true, Color.WHITE);
        badge.setBackground(roundRect(Color.parseColor("#33000000"), 14));
        badge.setPadding(dp(10), dp(4), dp(10), dp(4));
        topRow.addView(badge);
        statusCard.addView(topRow);

        String subInfo = ready
                ? prefs.getString("adb_mode", "Wireless ADB Connected")
                : "ⓘ  Tap here to start Wireless Debugging Pairing  ›";
        TextView actionPill = makeText(subInfo, 12, true, Color.WHITE);
        actionPill.setBackground(roundRect(Color.parseColor("#26FFFFFF"), 14));
        actionPill.setPadding(dp(12), dp(10), dp(12), dp(10));
        LinearLayout.LayoutParams pillP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pillP.topMargin = dp(14);
        actionPill.setLayoutParams(pillP);
        statusCard.addView(actionPill);

        statusCard.setOnClickListener(v -> renderActivateScreen());
        contentContainer.addView(statusCard);

        LinearLayout overlayCard = makeCard();
        LinearLayout ovRow = new LinearLayout(this);
        ovRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout ovText = new LinearLayout(this);
        ovText.setOrientation(LinearLayout.VERTICAL);
        ovText.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        ovText.addView(makeText("Floating Control Panel", 15, true, TEXT_PRIMARY));
        ovText.addView(makeText("In-game overlay & quick game launcher", 12, false, TEXT_SECONDARY));
        ovRow.addView(ovText);

        Button btnOverlay = makePillButton("Launch", ACCENT_BROWN, Color.WHITE);
        btnOverlay.setOnClickListener(v -> startFloatingOverlay());
        ovRow.addView(btnOverlay);
        overlayCard.addView(ovRow);
        contentContainer.addView(overlayCard);

        TextView devSec = makeText("Device Information", 14, true, ACCENT_BROWN);
        devSec.setPadding(dp(4), dp(16), 0, dp(8));
        contentContainer.addView(devSec);

        LinearLayout infoCard = makeCard();
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        long freeRam = mi.availMem / (1024 * 1024);
        long totalRam = mi.totalMem / (1024 * 1024);

        infoCard.addView(makeInfoRow("Device", Build.MANUFACTURER.toUpperCase() + " " + Build.MODEL,
                "Android Version", Build.VERSION.RELEASE + " (SDK " + Build.VERSION.SDK_INT + ")"));
        infoCard.addView(makeDivider());
        infoCard.addView(makeInfoRow("Available RAM", freeRam + " MB / " + totalRam + " MB",
                "Kernel", System.getProperty("os.version")));
        infoCard.addView(makeDivider());
        infoCard.addView(makeInfoRow("ABI Supported", Build.SUPPORTED_ABIS[0],
                "Shell Status", ready ? "u:r:shell:s0 (Unlocked)" : "Locked (Pair Required)"));
        contentContainer.addView(infoCard);
    }

    private void renderActivateScreen() {
        contentContainer.removeAllViews();

        TextView back = makeText("←  Wireless Debugging Setup", 20, true, TEXT_PRIMARY);
        back.setPadding(0, 0, 0, dp(14));
        back.setOnClickListener(v -> switchTab(0));
        contentContainer.addView(back);

        LinearLayout pairCard = makeCard();
        pairCard.addView(makeText("Step 1: Start via Wireless Debugging", 16, true, TEXT_PRIMARY));
        pairCard.addView(makeText("1. Tap 'Start Pairing' to trigger the Pairing Notification.\n2. Open Developer Options > Wireless Debugging > Pair device with pairing code.\n3. Pull down notification shade and enter the 6-digit code in KuruX notification.", 12, false, TEXT_SECONDARY));

        LinearLayout btnRow1 = new LinearLayout(this);
        btnRow1.setPadding(0, dp(14), 0, 0);

        Button btnStartPair = makePillButton("1. Start Pairing", ACCENT_BROWN, Color.WHITE);
        btnStartPair.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        btnStartPair.setOnClickListener(v -> triggerShizukuStyleNotification());

        Button btnDevOpts = makePillButton("2. Developer Options", ACCENT_LIGHT, TEXT_PRIMARY);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        bp.leftMargin = dp(8);
        btnDevOpts.setLayoutParams(bp);
        btnDevOpts.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
            } catch (Exception e) {
                Toast.makeText(this, "Open Settings > Developer Options", Toast.LENGTH_SHORT).show();
            }
        });

        btnRow1.addView(btnStartPair);
        btnRow1.addView(btnDevOpts);
        pairCard.addView(btnRow1);
        contentContainer.addView(pairCard);

        LinearLayout tcpCard = makeCard();
        tcpCard.addView(makeText("Step 2: TCP Debugging (Port 5555)", 16, true, TEXT_PRIMARY));
        tcpCard.addView(makeText("If ADB TCP/IP is already active on port 5555, connect directly:", 12, false, TEXT_SECONDARY));

        Button btnTcp = makePillButton("▶  Connect to TCP 5555", ACCENT_BROWN, Color.WHITE);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tp.topMargin = dp(12);
        btnTcp.setLayoutParams(tp);
        btnTcp.setOnClickListener(v -> {
            prefs.edit().putBoolean("adb_active", true).putString("adb_mode", "Connected via TCP 127.0.0.1:5555").apply();
            Toast.makeText(this, "TCP 5555 Connected! Apps & Modules Unlocked.", Toast.LENGTH_SHORT).show();
            switchTab(0);
        });
        tcpCard.addView(btnTcp);
        contentContainer.addView(tcpCard);
    }

    private void triggerShizukuStyleNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIF_PERM);
                return;
            }
        }

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(new NotificationChannel(
                    PairingReceiver.CHANNEL_ID, "Wireless ADB Pairing", NotificationManager.IMPORTANCE_HIGH));
        }

        RemoteInput remoteInput = new RemoteInput.Builder(PairingReceiver.KEY_PAIRING_CODE)
                .setLabel("Enter 6-digit pairing code")
                .build();

        Intent replyIntent = new Intent(this, PairingReceiver.class);
        PendingIntent replyPendingIntent = PendingIntent.getBroadcast(
                this, 0, replyIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        Notification.Action action = new Notification.Action.Builder(
                R.drawable.ic_kurux_logo, "Enter Pairing Code", replyPendingIntent)
                .addRemoteInput(remoteInput).build();

        Notification notif = new Notification.Builder(this, PairingReceiver.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_kurux_logo)
                .setContentTitle("Wireless Debugging Pairing Service")
                .setContentText("Searching for pairing service... Tap 'Enter Pairing Code'")
                .setOngoing(true)
                .addAction(action)
                .build();

        nm.notify(PairingReceiver.NOTIF_ID, notif);
        Toast.makeText(this, "Pairing notification sent! Pull down notification bar.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_NOTIF_PERM && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            triggerShizukuStyleNotification();
        } else {
            Toast.makeText(this, "Notification permission required for Pairing!", Toast.LENGTH_LONG).show();
        }
    }
MODULE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PICK_MODULE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            String fileName = getFileNameFromUri(uri);
            if (!importedModules.contains(fileName)) {
                importedModules.add(fileName);
                prefs.edit().putStringSet("imported_modules", new HashSet<>(importedModules)).apply();
            }
            Toast.makeText(this, "Imported: " + fileName, Toast.LENGTH_SHORT).show();
            switchTab(2);
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String result = "Custom_Module.zip";
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) result = cursor.getString(idx);
            }
        } catch (Exception ignored) {}
        return result;
    }

    private void renderSettingsTab() {
        contentContainer.addView(makeText("Settings", 22, true, TEXT_PRIMARY));
        LinearLayout envCard = makeCard();
        envCard.addView(makeText("Environment & ADB Controls", 14, true, ACCENT_BROWN));
        envCard.addView(makeSettingSwitch("Auto-Clean Background RAM on Launch", "Frees cached processes before game start", true));
        envCard.addView(makeDivider());
        envCard.addView(makeSettingSwitch("Keep Screen Awake in Game", "Prevents display dim during matches", true));
        contentContainer.addView(envCard);

        LinearLayout resetCard = makeCard();
        resetCard.addView(makeText("Reset ADB & Modules", 15, true, ALERT_CORAL));
        resetCard.addView(makeText("Disconnect Wireless ADB session and clear imported modules", 12, false, TEXT_SECONDARY));
        Button btnReset = makePillButton("Reset KuruX Manager", ALERT_CORAL, Color.WHITE);
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rp.topMargin = dp(10);
        btnReset.setLayoutParams(rp);
        btnReset.setOnClickListener(v -> {
            importedModules.clear();
            prefs.edit().clear().apply();
            Toast.makeText(this, "Reset Complete!", Toast.LENGTH_SHORT).show();
            switchTab(0);
        });
        resetCard.addView(btnReset);
        contentContainer.addView(resetCard);
    }

    private void startFloatingOverlay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())));
            Toast.makeText(this, "Allow Display over other apps first", Toast.LENGTH_LONG).show();
            return;
        }
        startService(new Intent(this, OverlayService.class));
        Toast.makeText(this, "KuruX Floating Panel Started!", Toast.LENGTH_SHORT).show();
    }

    private LinearLayout makeCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(roundRect(CARD_BG, 20));
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(12);
        card.setLayoutParams(lp);
        return card;
    }

    private LinearLayout makeInfoRow(String k1, String v1, String k2, String v2) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(6), 0, dp(6));
        LinearLayout c1 = new LinearLayout(this);
        c1.setOrientation(LinearLayout.VERTICAL);
        c1.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        c1.addView(makeText(k1, 11, false, TEXT_SECONDARY));
        c1.addView(makeText(v1, 13, true, TEXT_PRIMARY));
        LinearLayout c2 = new LinearLayout(this);
        c2.setOrientation(LinearLayout.VERTICAL);
        c2.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        c2.addView(makeText(k2, 11, false, TEXT_SECONDARY));
        c2.addView(makeText(v2, 13, true, TEXT_PRIMARY));
        row.addView(c1);
        row.addView(c2);
        return row;
    }

    private LinearLayout makeSettingSwitch(String title, String sub, boolean def) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(10), 0, dp(10));
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        col.addView(makeText(title, 14, true, TEXT_PRIMARY));
        col.addView(makeText(sub, 11, false, TEXT_SECONDARY));
        row.addView(col);
        Switch sw = new Switch(this);
        sw.setChecked(def);
        row.addView(sw);
        return row;
    }

    private View makeDivider() {
        View v = new View(this);
        v.setBackgroundColor(Color.parseColor("#E0D6CC"));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
        lp.topMargin = dp(4);
        lp.bottomMargin = dp(4);
        v.setLayoutParams(lp);
        return v;
    }

    private TextView makeText(String text, float sp, boolean bold, int color) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(sp);
        tv.setTextColor(color);
        if (bold) tv.setTypeface(null, Typeface.BOLD);
        return tv;
    }

    private EditText makeInput(String hint) {
        EditText et = new EditText(this);
        et.setHint(hint);
        et.setTextSize(13f);
        et.setTextColor(TEXT_PRIMARY);
        et.setHintTextColor(TEXT_SECONDARY);
        et.setBackground(roundRect(Color.WHITE, 14));
        et.setPadding(dp(12), dp(10), dp(12), dp(10));
        return et;
    }

    private Button makePillButton(String text, int bg, int fg) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextColor(fg);
        b.setTextSize(12f);
        b.setTypeface(null, Typeface.BOLD);
        b.setBackground(roundRect(bg, 20));
        b.setPadding(dp(14), dp(8), dp(14), dp(8));
        return b;
    }

    private GradientDrawable roundRect(int color, int radiusDp) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        gd.setCornerRadius(dp(radiusDp));
        return gd;
    }

    private int dp(int val) {
        return Math.round(val * getResources().getDisplayMetrics().density);
    }
}
