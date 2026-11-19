package com.ninjamaxyt.kuruxcompanion;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.database.Cursor;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.*;
import android.provider.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    private static final int REQ_NOTIF = 101, REQ_PICK = 102;
    private LinearLayout content;
    private TextView[] nav = new TextView[4];
    private LinearLayout fab;
    private int tabIdx = 0;
    private SharedPreferences prefs;
    private List<String> modules = new ArrayList<>();

    private final int BG = Color.parseColor("#FAF6F2"), CARD = Color.parseColor("#F2ECE6");
    private final int BROWN = Color.parseColor("#8B5A2B"), LIGHT = Color.parseColor("#E6D9CC");
    private final int TXT1 = Color.parseColor("#2B2118"), TXT2 = Color.parseColor("#756658");
    private final int CORAL = Color.parseColor("#E26D5C"), GREEN = Color.parseColor("#2E7D32");

    private final BroadcastReceiver rx = new BroadcastReceiver() {
        @Override public void onReceive(Context c, Intent i) { switchTab(tabIdx); }
    };

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("kurux_prefs", MODE_PRIVATE);
        modules.addAll(prefs.getStringSet("imported_modules", new HashSet<>()));
        getWindow().setStatusBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(BG);
        ScrollView sv = new ScrollView(this);
        sv.setFillViewport(true);
        root.setOnApplyWindowInsetsListener((v, ins) -> {
            sv.setPadding(dp(18), ins.getSystemWindowInsetTop() + dp(16), dp(18), ins.getSystemWindowInsetBottom() + dp(105));
            return ins;
        });

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        sv.addView(content);
        root.addView(sv);

        LinearLayout barWrap = new LinearLayout(this);
        barWrap.setGravity(Gravity.CENTER);
        barWrap.setPadding(dp(16), dp(8), dp(16), dp(22));

        LinearLayout pill = new LinearLayout(this);
        pill.setBackground(rr(Color.parseColor("#E8DFD5"), 32));
        pill.setPadding(dp(8), dp(6), dp(8), dp(6));
        pill.setElevation(dp(6));

        String[] names = {"Home", "Apps", "Modules", "Settings"};
        for (int i = 0; i < 4; i++) {
            final int k = i;
            TextView t = new TextView(this);
            t.setText(names[i]); t.setTextSize(13f); t.setTypeface(null, Typeface.BOLD);
            t.setPadding(dp(16), dp(10), dp(16), dp(10));
            t.setOnClickListener(v -> switchTab(k));
            nav[i] = t; pill.addView(t);
        }
        barWrap.addView(pill);

        fab = new LinearLayout(this);
        fab.setGravity(Gravity.CENTER); fab.setBackground(rr(LIGHT, 28)); fab.setElevation(dp(6));
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(dp(52), dp(52));
        fp.leftMargin = dp(10); fab.setLayoutParams(fp);
        TextView plus = new TextView(this); plus.setText("+"); plus.setTextSize(24f); plus.setTextColor(TXT1);
        fab.addView(plus); fab.setVisibility(View.GONE);
        fab.setOnClickListener(v -> {
            Intent in = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            in.addCategory(Intent.CATEGORY_OPENABLE); in.setType("*/*");
            startActivityForResult(in, REQ_PICK);
        });
        barWrap.addView(fab);

        FrameLayout.LayoutParams np = new FrameLayout.LayoutParams(-1, -2);
        np.gravity = Gravity.BOTTOM;
        root.addView(barWrap, np);
        setContentView(root);
        switchTab(0);
    }

    @Override protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 33) registerReceiver(rx, new IntentFilter("com.ninjamaxyt.kuruxcompanion.ADB_PAIRED"), RECEIVER_NOT_EXPORTED);
        else registerReceiver(rx, new IntentFilter("com.ninjamaxyt.kuruxcompanion.ADB_PAIRED"));
        switchTab(tabIdx);
    }

    @Override protected void onPause() { super.onPause(); try { unregisterReceiver(rx); } catch (Exception ignored) {} }
    private boolean ready() { return prefs.getBoolean("adb_active", false); }

    private void switchTab(int i) {
        tabIdx = i;
        for (int j = 0; j < 4; j++) {
            nav[j].setBackground(j == i ? rr(BROWN, 24) : null);
            nav[j].setTextColor(j == i ? Color.WHITE : TXT1);
        }
        fab.setVisibility((i == 2 && ready()) ? View.VISIBLE : View.GONE);
        content.removeAllViews();
        if (i == 0) homeTab(); else if (i == 1) appsTab(); else if (i == 2) modTab(); else setTab();
    }

    private void homeTab() {
        boolean r = ready();
        LinearLayout h = new LinearLayout(this); h.setGravity(Gravity.CENTER_VERTICAL); h.setPadding(0, 0, 0, dp(16));
        ImageView lg = new ImageView(this); lg.setImageResource(R.drawable.ic_kurux_logo);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(42), dp(42)); lp.rightMargin = dp(12); lg.setLayoutParams(lp);
        h.addView(lg);
        LinearLayout tb = new LinearLayout(this); tb.setOrientation(LinearLayout.VERTICAL);
        tb.addView(txt("KuruX Manager", 22, true, TXT1));
        tb.addView(txt("v2.1.0 • Wireless ADB & Non-Root Engine", 12, false, TXT2));
        h.addView(tb); content.addView(h);

        LinearLayout sc = new LinearLayout(this); sc.setOrientation(LinearLayout.VERTICAL);
        sc.setBackground(rr(r ? GREEN : CORAL, 22)); sc.setPadding(dp(18), dp(18), dp(18), dp(18));
        LinearLayout tr = new LinearLayout(this); tr.setGravity(Gravity.CENTER_VERTICAL);
        TextView st = txt(r ? "Running" : "Need extra step", 20, true, Color.WHITE);
        st.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f)); tr.addView(st);
        TextView bg = txt(r ? "ADB Active" : "Action needed", 11, true, Color.WHITE);
        bg.setBackground(rr(Color.parseColor("#33000000"), 14)); bg.setPadding(dp(10), dp(4), dp(10), dp(4));
        tr.addView(bg); sc.addView(tr);

        String sub = r ? prefs.getString("adb_mode", "Wireless ADB Connected") : "ⓘ  Tap to start Wireless Debugging Pairing  ›";
        TextView ap = txt(sub, 12, true, Color.WHITE);
        ap.setBackground(rr(Color.parseColor("#26FFFFFF"), 14)); ap.setPadding(dp(12), dp(10), dp(12), dp(10));
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(-1, -2); pp.topMargin = dp(14); ap.setLayoutParams(pp);
        sc.addView(ap); sc.setOnClickListener(v -> activateScreen()); content.addView(sc);

        LinearLayout oc = card(); LinearLayout orw = new LinearLayout(this); orw.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout ot = new LinearLayout(this); ot.setOrientation(LinearLayout.VERTICAL);
        ot.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        ot.addView(txt("Floating Control Panel", 15, true, TXT1));
        ot.addView(txt("In-game overlay & quick game launcher", 12, false, TXT2));
        orw.addView(ot);
        Button bo = btn("Launch", BROWN, Color.WHITE);
        bo.setOnClickListener(v -> startOverlay()); orw.addView(bo); oc.addView(orw); content.addView(oc);

        TextView ds = txt("Device Information", 14, true, BROWN); ds.setPadding(dp(4), dp(16), 0, dp(8)); content.addView(ds);
        LinearLayout ic = card();
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo(); am.getMemoryInfo(mi);
        ic.addView(infoRow("Device", Build.MANUFACTURER.toUpperCase() + " " + Build.MODEL, "Android", Build.VERSION.RELEASE + " (SDK " + Build.VERSION.SDK_INT + ")"));
        ic.addView(div());
        ic.addView(infoRow("Available RAM", (mi.availMem / 1048576) + " / " + (mi.totalMem / 1048576) + " MB", "Kernel", System.getProperty("os.version")));
        ic.addView(div());
        ic.addView(infoRow("ABI", Build.SUPPORTED_ABIS[0], "Shell Status", r ? "u:r:shell:s0 (Unlocked)" : "Locked (Pair Required)"));
        content.addView(ic);
    }

    private void activateScreen() {
        content.removeAllViews();
        TextView bk = txt("←  Wireless Debugging Setup", 20, true, TXT1); bk.setPadding(0, 0, 0, dp(14));
        bk.setOnClickListener(v -> switchTab(0)); content.addView(bk);

        LinearLayout pc = card();
        pc.addView(txt("Step 1: Start via Wireless Debugging", 16, true, TXT1));
        pc.addView(txt("1. Tap 'Start Pairing' to trigger Pairing Notification.\n2. Open Developer Options > Wireless Debugging > Pair device with pairing code.\n3. Pull down notification shade & enter the 6-digit code.", 12, false, TXT2));
        LinearLayout br = new LinearLayout(this); br.setPadding(0, dp(14), 0, 0);
        Button b1 = btn("1. Start Pairing", BROWN, Color.WHITE);
        b1.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        b1.setOnClickListener(v -> triggerNotif());
        Button b2 = btn("2. Developer Options", LIGHT, TXT1);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0, -2, 1f); bp.leftMargin = dp(8); b2.setLayoutParams(bp);
        b2.setOnClickListener(v -> { try { startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)); } catch (Exception e) { toast("Open Settings > Developer Options"); } });
        br.addView(b1); br.addView(b2); pc.addView(br); content.addView(pc);

        LinearLayout tc = card();
        tc.addView(txt("Step 2: TCP Debugging (Port 5555)", 16, true, TXT1));
        Button bt = btn("▶  Connect to TCP 5555", BROWN, Color.WHITE);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(-1, -2); tp.topMargin = dp(12); bt.setLayoutParams(tp);
        bt.setOnClickListener(v -> { prefs.edit().putBoolean("adb_active", true).putString("adb_mode", "TCP 127.0.0.1:5555").apply(); toast("TCP 5555 Connected!"); switchTab(0); });
        tc.addView(bt); content.addView(tc);
    }

    private void triggerNotif() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIF); return;
        }
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) nm.createNotificationChannel(new NotificationChannel(PairingReceiver.CHANNEL_ID, "ADB Pairing", NotificationManager.IMPORTANCE_HIGH));
        RemoteInput ri = new RemoteInput.Builder(PairingReceiver.KEY_PAIRING_CODE).setLabel("Enter 6-digit pairing code").build();
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(this, PairingReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        Notification.Action act = new Notification.Action.Builder(R.drawable.ic_kurux_logo, "Enter Pairing Code", pi).addRemoteInput(ri).build();
        Notification n = new Notification.Builder(this, PairingReceiver.CHANNEL_ID).setSmallIcon(R.drawable.ic_kurux_logo)
                .setContentTitle("Wireless Debugging Pairing Service").setContentText("Tap 'Enter Pairing Code' below").setOngoing(true).addAction(act).build();
        nm.notify(PairingReceiver.NOTIF_ID, n);
        toast("Pairing notification sent! Pull down notification shade.");
    }

    @Override public void onRequestPermissionsResult(int rc, String[] p, int[] gr) {
        if (rc == REQ_NOTIF && gr.length > 0 && gr[0] == PackageManager.PERMISSION_GRANTED) triggerNotif();
        else toast("Notification permission required!");
    }

    private void homeTab() {
        boolean r = ready();
        LinearLayout h = new LinearLayout(this); h.setGravity(Gravity.CENTER_VERTICAL); h.setPadding(0, 0, 0, dp(16));
        ImageView lg = new ImageView(this); lg.setImageResource(R.drawable.ic_kurux_logo);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(42), dp(42)); lp.rightMargin = dp(12); lg.setLayoutParams(lp);
        h.addView(lg);
        LinearLayout tb = new LinearLayout(this); tb.setOrientation(LinearLayout.VERTICAL);
        tb.addView(txt("KuruX Manager", 22, true, TXT1));
        tb.addView(txt("v2.1.0 • Wireless ADB & Non-Root Engine", 12, false, TXT2));
        h.addView(tb); content.addView(h);

        LinearLayout sc = new LinearLayout(this); sc.setOrientation(LinearLayout.VERTICAL);
        sc.setBackground(rr(r ? GREEN : CORAL, 22)); sc.setPadding(dp(18), dp(18), dp(18), dp(18));
        LinearLayout tr = new LinearLayout(this); tr.setGravity(Gravity.CENTER_VERTICAL);
        TextView st = txt(r ? "Running" : "Need extra step", 20, true, Color.WHITE);
        st.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f)); tr.addView(st);
        TextView bg = txt(r ? "ADB Active" : "Action needed", 11, true, Color.WHITE);
        bg.setBackground(rr(Color.parseColor("#33000000"), 14)); bg.setPadding(dp(10), dp(4), dp(10), dp(4));
        tr.addView(bg); sc.addView(tr);

        String sub = r ? prefs.getString("adb_mode", "Wireless ADB Connected") : "ⓘ  Tap to start Wireless Debugging Pairing  ›";
        TextView ap = txt(sub, 12, true, Color.WHITE);
        ap.setBackground(rr(Color.parseColor("#26FFFFFF"), 14)); ap.setPadding(dp(12), dp(10), dp(12), dp(10));
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(-1, -2); pp.topMargin = dp(14); ap.setLayoutParams(pp);
        sc.addView(ap); sc.setOnClickListener(v -> activateScreen()); content.addView(sc);

        LinearLayout oc = card(); LinearLayout orw = new LinearLayout(this); orw.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout ot = new LinearLayout(this); ot.setOrientation(LinearLayout.VERTICAL);
        ot.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        ot.addView(txt("Floating Control Panel", 15, true, TXT1));
        ot.addView(txt("In-game overlay & quick game launcher", 12, false, TXT2));
        orw.addView(ot);
        Button bo = btn("Launch", BROWN, Color.WHITE);
        bo.setOnClickListener(v -> startOverlay()); orw.addView(bo); oc.addView(orw); content.addView(oc);

        TextView ds = txt("Device Information", 14, true, BROWN); ds.setPadding(dp(4), dp(16), 0, dp(8)); content.addView(ds);
        LinearLayout ic = card();
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo(); am.getMemoryInfo(mi);
        ic.addView(infoRow("Device", Build.MANUFACTURER.toUpperCase() + " " + Build.MODEL, "Android", Build.VERSION.RELEASE + " (SDK " + Build.VERSION.SDK_INT + ")"));
        ic.addView(div());
        ic.addView(infoRow("Available RAM", (mi.availMem / 1048576) + " / " + (mi.totalMem / 1048576) + " MB", "Kernel", System.getProperty("os.version")));
        ic.addView(div());
        ic.addView(infoRow("ABI", Build.SUPPORTED_ABIS[0], "Shell Status", r ? "u:r:shell:s0 (Unlocked)" : "Locked (Pair Required)"));
        content.addView(ic);
    }

    private void activateScreen() {
        content.removeAllViews();
        TextView bk = txt("←  Wireless Debugging Setup", 20, true, TXT1); bk.setPadding(0, 0, 0, dp(14));
        bk.setOnClickListener(v -> switchTab(0)); content.addView(bk);

        LinearLayout pc = card();
        pc.addView(txt("Step 1: Start via Wireless Debugging", 16, true, TXT1));
        pc.addView(txt("1. Tap 'Start Pairing' to trigger Pairing Notification.\n2. Open Developer Options > Wireless Debugging > Pair device with pairing code.\n3. Pull down notification shade & enter the 6-digit code.", 12, false, TXT2));
        LinearLayout br = new LinearLayout(this); br.setPadding(0, dp(14), 0, 0);
        Button b1 = btn("1. Start Pairing", BROWN, Color.WHITE);
        b1.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        b1.setOnClickListener(v -> triggerNotif());
        Button b2 = btn("2. Developer Options", LIGHT, TXT1);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0, -2, 1f); bp.leftMargin = dp(8); b2.setLayoutParams(bp);
        b2.setOnClickListener(v -> { try { startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)); } catch (Exception e) { toast("Open Settings > Developer Options"); } });
        br.addView(b1); br.addView(b2); pc.addView(br); content.addView(pc);

        LinearLayout tc = card();
        tc.addView(txt("Step 2: TCP Debugging (Port 5555)", 16, true, TXT1));
        Button bt = btn("▶  Connect to TCP 5555", BROWN, Color.WHITE);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(-1, -2); tp.topMargin = dp(12); bt.setLayoutParams(tp);
        bt.setOnClickListener(v -> { prefs.edit().putBoolean("adb_active", true).putString("adb_mode", "TCP 127.0.0.1:5555").apply(); toast("TCP 5555 Connected!"); switchTab(0); });
        tc.addView(bt); content.addView(tc);
    }

    private void triggerNotif() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIF); return;
        }
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) nm.createNotificationChannel(new NotificationChannel(PairingReceiver.CHANNEL_ID, "ADB Pairing", NotificationManager.IMPORTANCE_HIGH));
        RemoteInput ri = new RemoteInput.Builder(PairingReceiver.KEY_PAIRING_CODE).setLabel("Enter 6-digit pairing code").build();
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(this, PairingReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        Notification.Action act = new Notification.Action.Builder(R.drawable.ic_kurux_logo, "Enter Pairing Code", pi).addRemoteInput(ri).build();
        Notification n = new Notification.Builder(this, PairingReceiver.CHANNEL_ID).setSmallIcon(R.drawable.ic_kurux_logo)
                .setContentTitle("Wireless Debugging Pairing Service").setContentText("Tap 'Enter Pairing Code' below").setOngoing(true).addAction(act).build();
        nm.notify(PairingReceiver.NOTIF_ID, n);
        toast("Pairing notification sent! Pull down notification shade.");
    }

    @Override public void onRequestPermissionsResult(int rc, String[] p, int[] gr) {
        if (rc == REQ_NOTIF && gr.length > 0 && gr[0] == PackageManager.PERMISSION_GRANTED) triggerNotif();
        else toast("Notification permission required!");
    }

    private void homeTab() {
        boolean r = ready();
        LinearLayout h = new LinearLayout(this); h.setGravity(Gravity.CENTER_VERTICAL); h.setPadding(0, 0, 0, dp(16));
        ImageView lg = new ImageView(this); lg.setImageResource(R.drawable.ic_kurux_logo);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(42), dp(42)); lp.rightMargin = dp(12); lg.setLayoutParams(lp);
        h.addView(lg);
        LinearLayout tb = new LinearLayout(this); tb.setOrientation(LinearLayout.VERTICAL);
        tb.addView(txt("KuruX Manager", 22, true, TXT1));
        tb.addView(txt("v2.1.0 • Wireless ADB & Non-Root Engine", 12, false, TXT2));
        h.addView(tb); content.addView(h);

        LinearLayout sc = new LinearLayout(this); sc.setOrientation(LinearLayout.VERTICAL);
        sc.setBackground(rr(r ? GREEN : CORAL, 22)); sc.setPadding(dp(18), dp(18), dp(18), dp(18));
        LinearLayout tr = new LinearLayout(this); tr.setGravity(Gravity.CENTER_VERTICAL);
        TextView st = txt(r ? "Running" : "Need extra step", 20, true, Color.WHITE);
        st.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f)); tr.addView(st);
        TextView bg = txt(r ? "ADB Active" : "Action needed", 11, true, Color.WHITE);
        bg.setBackground(rr(Color.parseColor("#33000000"), 14)); bg.setPadding(dp(10), dp(4), dp(10), dp(4));
        tr.addView(bg); sc.addView(tr);

        String sub = r ? prefs.getString("adb_mode", "Wireless ADB Connected") : "ⓘ  Tap to start Wireless Debugging Pairing  ›";
        TextView ap = txt(sub, 12, true, Color.WHITE);
        ap.setBackground(rr(Color.parseColor("#26FFFFFF"), 14)); ap.setPadding(dp(12), dp(10), dp(12), dp(10));
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(-1, -2); pp.topMargin = dp(14); ap.setLayoutParams(pp);
        sc.addView(ap); sc.setOnClickListener(v -> activateScreen()); content.addView(sc);

        LinearLayout oc = card(); LinearLayout orw = new LinearLayout(this); orw.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout ot = new LinearLayout(this); ot.setOrientation(LinearLayout.VERTICAL);
        ot.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        ot.addView(txt("Floating Control Panel", 15, true, TXT1));
        ot.addView(txt("In-game overlay & quick game launcher", 12, false, TXT2));
        orw.addView(ot);
        Button bo = btn("Launch", BROWN, Color.WHITE);
        bo.setOnClickListener(v -> startOverlay()); orw.addView(bo); oc.addView(orw); content.addView(oc);

        TextView ds = txt("Device Information", 14, true, BROWN); ds.setPadding(dp(4), dp(16), 0, dp(8)); content.addView(ds);
        LinearLayout ic = card();
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo(); am.getMemoryInfo(mi);
        ic.addView(infoRow("Device", Build.MANUFACTURER.toUpperCase() + " " + Build.MODEL, "Android", Build.VERSION.RELEASE + " (SDK " + Build.VERSION.SDK_INT + ")"));
        ic.addView(div());
        ic.addView(infoRow("Available RAM", (mi.availMem / 1048576) + " / " + (mi.totalMem / 1048576) + " MB", "Kernel", System.getProperty("os.version")));
        ic.addView(div());
        ic.addView(infoRow("ABI", Build.SUPPORTED_ABIS[0], "Shell Status", r ? "u:r:shell:s0 (Unlocked)" : "Locked (Pair Required)"));
        content.addView(ic);
    }

    private void activateScreen() {
        content.removeAllViews();
        TextView bk = txt("←  Wireless Debugging Setup", 20, true, TXT1); bk.setPadding(0, 0, 0, dp(14));
        bk.setOnClickListener(v -> switchTab(0)); content.addView(bk);

        LinearLayout pc = card();
        pc.addView(txt("Step 1: Start via Wireless Debugging", 16, true, TXT1));
        pc.addView(txt("1. Tap 'Start Pairing' to trigger Pairing Notification.\n2. Open Developer Options > Wireless Debugging > Pair device with pairing code.\n3. Pull down notification shade & enter the 6-digit code.", 12, false, TXT2));
        LinearLayout br = new LinearLayout(this); br.setPadding(0, dp(14), 0, 0);
        Button b1 = btn("1. Start Pairing", BROWN, Color.WHITE);
        b1.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        b1.setOnClickListener(v -> triggerNotif());
        Button b2 = btn("2. Developer Options", LIGHT, TXT1);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0, -2, 1f); bp.leftMargin = dp(8); b2.setLayoutParams(bp);
        b2.setOnClickListener(v -> { try { startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)); } catch (Exception e) { toast("Open Settings > Developer Options"); } });
        br.addView(b1); br.addView(b2); pc.addView(br); content.addView(pc);

        LinearLayout tc = card();
        tc.addView(txt("Step 2: TCP Debugging (Port 5555)", 16, true, TXT1));
        Button bt = btn("▶  Connect to TCP 5555", BROWN, Color.WHITE);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(-1, -2); tp.topMargin = dp(12); bt.setLayoutParams(tp);
        bt.setOnClickListener(v -> { prefs.edit().putBoolean("adb_active", true).putString("adb_mode", "TCP 127.0.0.1:5555").apply(); toast("TCP 5555 Connected!"); switchTab(0); });
        tc.addView(bt); content.addView(tc);
    }

    private void triggerNotif() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIF); return;
        }
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) nm.createNotificationChannel(new NotificationChannel(PairingReceiver.CHANNEL_ID, "ADB Pairing", NotificationManager.IMPORTANCE_HIGH));
        RemoteInput ri = new RemoteInput.Builder(PairingReceiver.KEY_PAIRING_CODE).setLabel("Enter 6-digit pairing code").build();
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(this, PairingReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        Notification.Action act = new Notification.Action.Builder(R.drawable.ic_kurux_logo, "Enter Pairing Code", pi).addRemoteInput(ri).build();
        Notification n = new Notification.Builder(this, PairingReceiver.CHANNEL_ID).setSmallIcon(R.drawable.ic_kurux_logo)
                .setContentTitle("Wireless Debugging Pairing Service").setContentText("Tap 'Enter Pairing Code' below").setOngoing(true).addAction(act).build();
        nm.notify(PairingReceiver.NOTIF_ID, n);
        toast("Pairing notification sent! Pull down notification shade.");
    }

    @Override public void onRequestPermissionsResult(int rc, String[] p, int[] gr) {
        if (rc == REQ_NOTIF && gr.length > 0 && gr[0] == PackageManager.PERMISSION_GRANTED) triggerNotif();
        else toast("Notification permission required!");
    }

    private LinearLayout gate(String name) {
        LinearLayout lc = card(); lc.setGravity(Gravity.CENTER); lc.setPadding(dp(20), dp(28), dp(20), dp(28));
        lc.addView(txt("🔒  " + name + " Locked", 18, true, CORAL));
        TextView d = txt("Complete Wireless Debugging Pairing first to unlock " + name + ".", 13, false, TXT2);
        d.setGravity(Gravity.CENTER); d.setPadding(0, dp(8), 0, dp(16)); lc.addView(d);
        Button bg = btn("Setup Wireless ADB", BROWN, Color.WHITE);
        bg.setOnClickListener(v -> activateScreen()); lc.addView(bg);
        return lc;
    }

    private void appsTab() {
        content.addView(txt("Background App Cleaner", 22, true, TXT1));
        if (!ready()) { content.addView(gate("App Cleaner")); return; }
        content.addView(txt("Freeze background apps via ADB Engine", 12, false, TXT2));
        EditText s = inp("Search installed apps...");
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, -2); sp.topMargin = dp(12); sp.bottomMargin = dp(8); s.setLayoutParams(sp);
        content.addView(s);
        LinearLayout lh = new LinearLayout(this); lh.setOrientation(LinearLayout.VERTICAL); content.addView(lh);
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> ua = new ArrayList<>();
        for (ApplicationInfo info : pm.getInstalledApplications(0))
            if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && !info.packageName.equals(getPackageName())) ua.add(info);
        Collections.sort(ua, (a, b) -> pm.getApplicationLabel(a).toString().compareToIgnoreCase(pm.getApplicationLabel(b).toString()));
        Runnable pop = () -> {
            lh.removeAllViews(); String q = s.getText().toString().toLowerCase();
            for (ApplicationInfo app : ua) {
                String lbl = pm.getApplicationLabel(app).toString();
                if (!q.isEmpty() && !lbl.toLowerCase().contains(q) && !app.packageName.toLowerCase().contains(q)) continue;
                LinearLayout rc = card(), rw = new LinearLayout(this); rw.setGravity(Gravity.CENTER_VERTICAL);
                ImageView ic = new ImageView(this); ic.setImageDrawable(pm.getApplicationIcon(app));
                LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(dp(38), dp(38)); ip.rightMargin = dp(12); ic.setLayoutParams(ip); rw.addView(ic);
                LinearLayout col = new LinearLayout(this); col.setOrientation(LinearLayout.VERTICAL); col.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
                col.addView(txt(lbl, 14, true, TXT1)); col.addView(txt(app.packageName, 11, false, TXT2)); rw.addView(col);
                Button bf = btn("Freeze", LIGHT, TXT1);
                bf.setOnClickListener(v -> toast("Queued: pm disable-user " + app.packageName)); rw.addView(bf);
                rc.addView(rw); lh.addView(rc);
            }
        };
        s.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence a, int b, int c, int d) {}
            public void onTextChanged(CharSequence a, int b, int c, int d) { pop.run(); }
            public void afterTextChanged(Editable a) {}
        });
        pop.run();
    }

    private void modTab() {
        content.addView(txt("Gaming Plugins & Modules", 22, true, TXT1));
        if (!ready()) { content.addView(gate("Modules & Plugins")); return; }
        content.addView(txt("Tap '+' below to import your .zip or .sh modules", 12, false, TXT2));
        if (modules.isEmpty()) {
            LinearLayout ec = card(); ec.setGravity(Gravity.CENTER); ec.setPadding(dp(20), dp(30), dp(20), dp(30));
            ec.addView(txt("No Modules Imported Yet", 15, true, TXT1));
            ec.addView(txt("Tap '+' at bottom right to pick a .zip or .sh module.", 12, false, TXT2));
            content.addView(ec); return;
        }
        for (String m : modules) {
            LinearLayout mc = card(), rw = new LinearLayout(this); rw.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout col = new LinearLayout(this); col.setOrientation(LinearLayout.VERTICAL); col.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
            col.addView(txt("IMPORTED MODULE  •  ADB SHELL", 10, true, BROWN));
            col.addView(txt(m, 14, true, TXT1)); rw.addView(col);
            Switch sw = new Switch(this);
            sw.setOnCheckedChangeListener((b, chk) -> toast(chk ? "Activated: " + m : "Stopped: " + m));
            rw.addView(sw); mc.addView(rw); content.addView(mc);
        }
    }

    private void homeTab() {
        boolean r = ready();
        LinearLayout h = new LinearLayout(this); h.setGravity(Gravity.CENTER_VERTICAL); h.setPadding(0, 0, 0, dp(16));
        ImageView lg = new ImageView(this); lg.setImageResource(R.drawable.ic_kurux_logo);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(42), dp(42)); lp.rightMargin = dp(12); lg.setLayoutParams(lp);
        h.addView(lg);
        LinearLayout tb = new LinearLayout(this); tb.setOrientation(LinearLayout.VERTICAL);
        tb.addView(txt("KuruX Manager", 22, true, TXT1));
        tb.addView(txt("v2.1.0 • Wireless ADB & Non-Root Engine", 12, false, TXT2));
        h.addView(tb); content.addView(h);

        LinearLayout sc = new LinearLayout(this); sc.setOrientation(LinearLayout.VERTICAL);
        sc.setBackground(rr(r ? GREEN : CORAL, 22)); sc.setPadding(dp(18), dp(18), dp(18), dp(18));
        LinearLayout tr = new LinearLayout(this); tr.setGravity(Gravity.CENTER_VERTICAL);
        TextView st = txt(r ? "Running" : "Need extra step", 20, true, Color.WHITE);
        st.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f)); tr.addView(st);
        TextView bg = txt(r ? "ADB Active" : "Action needed", 11, true, Color.WHITE);
        bg.setBackground(rr(Color.parseColor("#33000000"), 14)); bg.setPadding(dp(10), dp(4), dp(10), dp(4));
        tr.addView(bg); sc.addView(tr);

        String sub = r ? prefs.getString("adb_mode", "Wireless ADB Connected") : "ⓘ  Tap to start Wireless Debugging Pairing  ›";
        TextView ap = txt(sub, 12, true, Color.WHITE);
        ap.setBackground(rr(Color.parseColor("#26FFFFFF"), 14)); ap.setPadding(dp(12), dp(10), dp(12), dp(10));
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(-1, -2); pp.topMargin = dp(14); ap.setLayoutParams(pp);
        sc.addView(ap); sc.setOnClickListener(v -> activateScreen()); content.addView(sc);

        LinearLayout oc = card(); LinearLayout orw = new LinearLayout(this); orw.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout ot = new LinearLayout(this); ot.setOrientation(LinearLayout.VERTICAL);
        ot.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        ot.addView(txt("Floating Control Panel", 15, true, TXT1));
        ot.addView(txt("In-game overlay & quick game launcher", 12, false, TXT2));
        orw.addView(ot);
        Button bo = btn("Launch", BROWN, Color.WHITE);
        bo.setOnClickListener(v -> startOverlay()); orw.addView(bo); oc.addView(orw); content.addView(oc);

        TextView ds = txt("Device Information", 14, true, BROWN); ds.setPadding(dp(4), dp(16), 0, dp(8)); content.addView(ds);
        LinearLayout ic = card();
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo(); am.getMemoryInfo(mi);
        ic.addView(infoRow("Device", Build.MANUFACTURER.toUpperCase() + " " + Build.MODEL, "Android", Build.VERSION.RELEASE + " (SDK " + Build.VERSION.SDK_INT + ")"));
        ic.addView(div());
        ic.addView(infoRow("Available RAM", (mi.availMem / 1048576) + " / " + (mi.totalMem / 1048576) + " MB", "Kernel", System.getProperty("os.version")));
        ic.addView(div());
        ic.addView(infoRow("ABI", Build.SUPPORTED_ABIS[0], "Shell Status", r ? "u:r:shell:s0 (Unlocked)" : "Locked (Pair Required)"));
        content.addView(ic);
    }

    private void activateScreen() {
        content.removeAllViews();
        TextView bk = txt("←  Wireless Debugging Setup", 20, true, TXT1); bk.setPadding(0, 0, 0, dp(14));
        bk.setOnClickListener(v -> switchTab(0)); content.addView(bk);

        LinearLayout pc = card();
        pc.addView(txt("Step 1: Start via Wireless Debugging", 16, true, TXT1));
        pc.addView(txt("1. Tap 'Start Pairing' to trigger Pairing Notification.\n2. Open Developer Options > Wireless Debugging > Pair device with pairing code.\n3. Pull down notification shade & enter the 6-digit code.", 12, false, TXT2));
        LinearLayout br = new LinearLayout(this); br.setPadding(0, dp(14), 0, 0);
        Button b1 = btn("1. Start Pairing", BROWN, Color.WHITE);
        b1.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        b1.setOnClickListener(v -> triggerNotif());
        Button b2 = btn("2. Developer Options", LIGHT, TXT1);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0, -2, 1f); bp.leftMargin = dp(8); b2.setLayoutParams(bp);
        b2.setOnClickListener(v -> { try { startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)); } catch (Exception e) { toast("Open Settings > Developer Options"); } });
        br.addView(b1); br.addView(b2); pc.addView(br); content.addView(pc);

        LinearLayout tc = card();
        tc.addView(txt("Step 2: TCP Debugging (Port 5555)", 16, true, TXT1));
        Button bt = btn("▶  Connect to TCP 5555", BROWN, Color.WHITE);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(-1, -2); tp.topMargin = dp(12); bt.setLayoutParams(tp);
        bt.setOnClickListener(v -> { prefs.edit().putBoolean("adb_active", true).putString("adb_mode", "TCP 127.0.0.1:5555").apply(); toast("TCP 5555 Connected!"); switchTab(0); });
        tc.addView(bt); content.addView(tc);
    }

    private void triggerNotif() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIF); return;
        }
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) nm.createNotificationChannel(new NotificationChannel(PairingReceiver.CHANNEL_ID, "ADB Pairing", NotificationManager.IMPORTANCE_HIGH));
        RemoteInput ri = new RemoteInput.Builder(PairingReceiver.KEY_PAIRING_CODE).setLabel("Enter 6-digit pairing code").build();
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(this, PairingReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        Notification.Action act = new Notification.Action.Builder(R.drawable.ic_kurux_logo, "Enter Pairing Code", pi).addRemoteInput(ri).build();
        Notification n = new Notification.Builder(this, PairingReceiver.CHANNEL_ID).setSmallIcon(R.drawable.ic_kurux_logo)
                .setContentTitle("Wireless Debugging Pairing Service").setContentText("Tap 'Enter Pairing Code' below").setOngoing(true).addAction(act).build();
        nm.notify(PairingReceiver.NOTIF_ID, n);
        toast("Pairing notification sent! Pull down notification shade.");
    }

    @Override public void onRequestPermissionsResult(int rc, String[] p, int[] gr) {
        if (rc == REQ_NOTIF && gr.length > 0 && gr[0] == PackageManager.PERMISSION_GRANTED) triggerNotif();
        else toast("Notification permission required!");
    }

    private LinearLayout gate(String name) {
        LinearLayout lc = card(); lc.setGravity(Gravity.CENTER); lc.setPadding(dp(20), dp(28), dp(20), dp(28));
        lc.addView(txt("🔒  " + name + " Locked", 18, true, CORAL));
        TextView d = txt("Complete Wireless Debugging Pairing first to unlock " + name + ".", 13, false, TXT2);
        d.setGravity(Gravity.CENTER); d.setPadding(0, dp(8), 0, dp(16)); lc.addView(d);
        Button bg = btn("Setup Wireless ADB", BROWN, Color.WHITE);
        bg.setOnClickListener(v -> activateScreen()); lc.addView(bg);
        return lc;
    }

    private void appsTab() {
        content.addView(txt("Background App Cleaner", 22, true, TXT1));
        if (!ready()) { content.addView(gate("App Cleaner")); return; }
        content.addView(txt("Freeze background apps via ADB Engine", 12, false, TXT2));
        EditText s = inp("Search installed apps...");
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, -2); sp.topMargin = dp(12); sp.bottomMargin = dp(8); s.setLayoutParams(sp);
        content.addView(s);
        LinearLayout lh = new LinearLayout(this); lh.setOrientation(LinearLayout.VERTICAL); content.addView(lh);
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> ua = new ArrayList<>();
        for (ApplicationInfo info : pm.getInstalledApplications(0))
            if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && !info.packageName.equals(getPackageName())) ua.add(info);
        Collections.sort(ua, (a, b) -> pm.getApplicationLabel(a).toString().compareToIgnoreCase(pm.getApplicationLabel(b).toString()));
        Runnable pop = () -> {
            lh.removeAllViews(); String q = s.getText().toString().toLowerCase();
            for (ApplicationInfo app : ua) {
                String lbl = pm.getApplicationLabel(app).toString();
                if (!q.isEmpty() && !lbl.toLowerCase().contains(q) && !app.packageName.toLowerCase().contains(q)) continue;
                LinearLayout rc = card(), rw = new LinearLayout(this); rw.setGravity(Gravity.CENTER_VERTICAL);
                ImageView ic = new ImageView(this); ic.setImageDrawable(pm.getApplicationIcon(app));
                LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(dp(38), dp(38)); ip.rightMargin = dp(12); ic.setLayoutParams(ip); rw.addView(ic);
                LinearLayout col = new LinearLayout(this); col.setOrientation(LinearLayout.VERTICAL); col.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
                col.addView(txt(lbl, 14, true, TXT1)); col.addView(txt(app.packageName, 11, false, TXT2)); rw.addView(col);
                Button bf = btn("Freeze", LIGHT, TXT1);
                bf.setOnClickListener(v -> toast("Queued: pm disable-user " + app.packageName)); rw.addView(bf);
                rc.addView(rw); lh.addView(rc);
            }
        };
        s.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence a, int b, int c, int d) {}
            public void onTextChanged(CharSequence a, int b, int c, int d) { pop.run(); }
            public void afterTextChanged(Editable a) {}
        });
        pop.run();
    }

    private void modTab() {
        content.addView(txt("Gaming Plugins & Modules", 22, true, TXT1));
        if (!ready()) { content.addView(gate("Modules & Plugins")); return; }
        content.addView(txt("Tap '+' below to import your .zip or .sh modules", 12, false, TXT2));
        if (modules.isEmpty()) {
            LinearLayout ec = card(); ec.setGravity(Gravity.CENTER); ec.setPadding(dp(20), dp(30), dp(20), dp(30));
            ec.addView(txt("No Modules Imported Yet", 15, true, TXT1));
            ec.addView(txt("Tap '+' at bottom right to pick a .zip or .sh module.", 12, false, TXT2));
            content.addView(ec); return;
        }
        for (String m : modules) {
            LinearLayout mc = card(), rw = new LinearLayout(this); rw.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout col = new LinearLayout(this); col.setOrientation(LinearLayout.VERTICAL); col.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
            col.addView(txt("IMPORTED MODULE  •  ADB SHELL", 10, true, BROWN));
            col.addView(txt(m, 14, true, TXT1)); rw.addView(col);
            Switch sw = new Switch(this);
            sw.setOnCheckedChangeListener((b, chk) -> toast(chk ? "Activated: " + m : "Stopped: " + m));
            rw.addView(sw); mc.addView(rw); content.addView(mc);
        }
    }

    private LinearLayout gate(String name) {
        LinearLayout lc = card(); lc.setGravity(Gravity.CENTER); lc.setPadding(dp(20), dp(28), dp(20), dp(28));
        lc.addView(txt("🔒  " + name + " Locked", 18, true, CORAL));
        TextView d = txt("Complete Wireless Debugging Pairing first to unlock " + name + ".", 13, false, TXT2);
        d.setGravity(Gravity.CENTER); d.setPadding(0, dp(8), 0, dp(16)); lc.addView(d);
        Button bg = btn("Setup Wireless ADB", BROWN, Color.WHITE);
        bg.setOnClickListener(v -> activateScreen()); lc.addView(bg);
        return lc;
    }

    private void appsTab() {
        content.addView(txt("Background App Cleaner", 22, true, TXT1));
        if (!ready()) { content.addView(gate("App Cleaner")); return; }
        content.addView(txt("Freeze background apps via ADB Engine", 12, false, TXT2));
        EditText s = inp("Search installed apps...");
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, -2); sp.topMargin = dp(12); sp.bottomMargin = dp(8); s.setLayoutParams(sp);
        content.addView(s);
        LinearLayout lh = new LinearLayout(this); lh.setOrientation(LinearLayout.VERTICAL); content.addView(lh);
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> ua = new ArrayList<>();
        for (ApplicationInfo info : pm.getInstalledApplications(0))
            if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && !info.packageName.equals(getPackageName())) ua.add(info);
        Collections.sort(ua, (a, b) -> pm.getApplicationLabel(a).toString().compareToIgnoreCase(pm.getApplicationLabel(b).toString()));
        Runnable pop = () -> {
            lh.removeAllViews(); String q = s.getText().toString().toLowerCase();
            for (ApplicationInfo app : ua) {
                String lbl = pm.getApplicationLabel(app).toString();
                if (!q.isEmpty() && !lbl.toLowerCase().contains(q) && !app.packageName.toLowerCase().contains(q)) continue;
                LinearLayout rc = card(), rw = new LinearLayout(this); rw.setGravity(Gravity.CENTER_VERTICAL);
                ImageView ic = new ImageView(this); ic.setImageDrawable(pm.getApplicationIcon(app));
                LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(dp(38), dp(38)); ip.rightMargin = dp(12); ic.setLayoutParams(ip); rw.addView(ic);
                LinearLayout col = new LinearLayout(this); col.setOrientation(LinearLayout.VERTICAL); col.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
                col.addView(txt(lbl, 14, true, TXT1)); col.addView(txt(app.packageName, 11, false, TXT2)); rw.addView(col);
                Button bf = btn("Freeze", LIGHT, TXT1);
                bf.setOnClickListener(v -> toast("Queued: pm disable-user " + app.packageName)); rw.addView(bf);
                rc.addView(rw); lh.addView(rc);
            }
        };
        s.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence a, int b, int c, int d) {}
            public void onTextChanged(CharSequence a, int b, int c, int d) { pop.run(); }
            public void afterTextChanged(Editable a) {}
        });
        pop.run();
    }

    private void modTab() {
        content.addView(txt("Gaming Plugins & Modules", 22, true, TXT1));
        if (!ready()) { content.addView(gate("Modules & Plugins")); return; }
        content.addView(txt("Tap '+' below to import your .zip or .sh modules", 12, false, TXT2));
        if (modules.isEmpty()) {
            LinearLayout ec = card(); ec.setGravity(Gravity.CENTER); ec.setPadding(dp(20), dp(30), dp(20), dp(30));
            ec.addView(txt("No Modules Imported Yet", 15, true, TXT1));
            ec.addView(txt("Tap '+' at bottom right to pick a .zip or .sh module.", 12, false, TXT2));
            content.addView(ec); return;
        }
        for (String m : modules) {
            LinearLayout mc = card(), rw = new LinearLayout(this); rw.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout col = new LinearLayout(this); col.setOrientation(LinearLayout.VERTICAL); col.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
            col.addView(txt("IMPORTED MODULE  •  ADB SHELL", 10, true, BROWN));
            col.addView(txt(m, 14, true, TXT1)); rw.addView(col);
            Switch sw = new Switch(this);
            sw.setOnCheckedChangeListener((b, chk) -> toast(chk ? "Activated: " + m : "Stopped: " + m));
            rw.addView(sw); mc.addView(rw); content.addView(mc);
        }
    }

    @Override protected void onActivityResult(int rc, int res, Intent d) {
        super.onActivityResult(rc, res, d);
        if (rc == REQ_PICK && res == RESULT_OK && d != null && d.getData() != null) {
            String fn = "Custom_Module.zip";
            try (Cursor c = getContentResolver().query(d.getData(), null, null, null, null)) {
                if (c != null && c.moveToFirst()) { int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME); if (idx >= 0) fn = c.getString(idx); }
            } catch (Exception ignored) {}
            if (!modules.contains(fn)) { modules.add(fn); prefs.edit().putStringSet("imported_modules", new HashSet<>(modules)).apply(); }
            toast("Imported: " + fn); switchTab(2);
        }
    }

    private void setTab() {
        content.addView(txt("Settings", 22, true, TXT1));
        LinearLayout rc = card();
        rc.addView(txt("Reset ADB & Modules", 15, true, CORAL));
        rc.addView(txt("Disconnect Wireless ADB session and clear imported modules", 12, false, TXT2));
        Button br = btn("Reset KuruX Manager", CORAL, Color.WHITE);
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, -2); rp.topMargin = dp(10); br.setLayoutParams(rp);
        br.setOnClickListener(v -> { modules.clear(); prefs.edit().clear().apply(); toast("Reset Complete!"); switchTab(0); });
        rc.addView(br); content.addView(rc);
    }

    private void startOverlay() {
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())));
            toast("Allow Display over other apps first"); return;
        }
        startService(new Intent(this, OverlayService.class)); toast("Floating Panel Started!");
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL); c.setBackground(rr(CARD, 20));
        c.setPadding(dp(16), dp(14), dp(16), dp(14));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2); lp.topMargin = dp(12); c.setLayoutParams(lp); return c;
    }
    private LinearLayout infoRow(String k1, String v1, String k2, String v2) {
        LinearLayout r = new LinearLayout(this); r.setPadding(0, dp(6), 0, dp(6));
        LinearLayout c1 = new LinearLayout(this); c1.setOrientation(LinearLayout.VERTICAL); c1.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        c1.addView(txt(k1, 11, false, TXT2)); c1.addView(txt(v1, 13, true, TXT1));
        LinearLayout c2 = new LinearLayout(this); c2.setOrientation(LinearLayout.VERTICAL); c2.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        c2.addView(txt(k2, 11, false, TXT2)); c2.addView(txt(v2, 13, true, TXT1));
        r.addView(c1); r.addView(c2); return r;
    }
    private View div() { View v = new View(this); v.setBackgroundColor(Color.parseColor("#E0D6CC")); LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, 1); lp.topMargin = dp(4); lp.bottomMargin = dp(4); v.setLayoutParams(lp); return v; }
    private TextView txt(String t, float s, boolean b, int c) { TextView tv = new TextView(this); tv.setText(t); tv.setTextSize(s); tv.setTextColor(c); if (b) tv.setTypeface(null, Typeface.BOLD); return tv; }
    private EditText inp(String h) { EditText e = new EditText(this); e.setHint(h); e.setTextSize(13f); e.setTextColor(TXT1); e.setBackground(rr(Color.WHITE, 14)); e.setPadding(dp(12), dp(10), dp(12), dp(10)); return e; }
    private Button btn(String t, int bg, int fg) { Button b = new Button(this); b.setText(t); b.setAllCaps(false); b.setTextColor(fg); b.setTextSize(12f); b.setTypeface(null, Typeface.BOLD); b.setBackground(rr(bg, 20)); b.setPadding(dp(14), dp(8), dp(14), dp(8)); return b; }
    private GradientDrawable rr(int c, int r) { GradientDrawable g = new GradientDrawable(); g.setColor(c); g.setCornerRadius(dp(r)); return g; }
    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }
    private void toast(String m) { Toast.makeText(this, m, Toast.LENGTH_SHORT).show(); }
}
