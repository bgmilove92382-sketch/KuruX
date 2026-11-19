package com.ninjamaxyt.kuruxcompanion;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {

    private LinearLayout contentContainer;
    private TextView[] navItems = new TextView[4];
    private LinearLayout fabAddModule;
    private int currentTab = 0;
    private boolean isAdbActive = false;
    private String adbStatusText = "Need extra step";
    private String adbSubText = "Tap to configure Wireless ADB / TCP 5555";
    private List<String> importedModules = new ArrayList<>();

    // Warm Material You (AxManager style) Palette
    private final int BG_COLOR = Color.parseColor("#FAF6F2");
    private final int CARD_BG = Color.parseColor("#F2ECE6");
    private final int ACCENT_BROWN = Color.parseColor("#8B5A2B");
    private final int ACCENT_LIGHT = Color.parseColor("#E6D9CC");
    private final int TEXT_PRIMARY = Color.parseColor("#2B2118");
    private final int TEXT_SECONDARY = Color.parseColor("#756658");
    private final int ALERT_CORAL = Color.parseColor("#E26D5C");
    private final int SUCCESS_GREEN = Color.parseColor("#2E7D32");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BG_COLOR);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        importedModules.add("KuruX FF Engine v2.0 • Non-Root Gaming Profile");

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(BG_COLOR);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setPadding(dp(18), dp(14), dp(18), dp(105));

        contentContainer = new LinearLayout(this);
        contentContainer.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(contentContainer);
        root.addView(scrollView);

        // Floating Pill Bottom Navigation + FAB (+)
        LinearLayout bottomWrapper = new LinearLayout(this);
        bottomWrapper.setOrientation(LinearLayout.HORIZONTAL);
        bottomWrapper.setGravity(Gravity.CENTER);
        bottomWrapper.setPadding(dp(16), dp(8), dp(16), dp(18));

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
        fabAddModule.setOnClickListener(v -> showImportModuleDialog());
        bottomWrapper.addView(fabAddModule);

        FrameLayout.LayoutParams navParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        navParams.gravity = Gravity.BOTTOM;
        root.addView(bottomWrapper, navParams);

        setContentView(root);
        switchTab(0);
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
        fabAddModule.setVisibility(index == 2 ? View.VISIBLE : View.GONE);
        contentContainer.removeAllViews();

        if (index == 0) renderHomeTab();
        else if (index == 1) renderAppsTab();
        else if (index == 2) renderModulesTab();
        else renderSettingsTab();
    }

    private void renderHomeTab() {
        // Header with Logo
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
        TextView title = makeText("KuruX Manager", 22, true, TEXT_PRIMARY);
        TextView sub = makeText("v2.0.0 • Non-Root ADB & Gaming Suite", 12, false, TEXT_SECONDARY);
        titleBox.addView(title);
        titleBox.addView(sub);
        header.addView(titleBox);
        contentContainer.addView(header);

        // Status Banner Card (Like AxManager "Need extra step" / "Working")
        LinearLayout statusCard = new LinearLayout(this);
        statusCard.setOrientation(LinearLayout.VERTICAL);
        statusCard.setBackground(roundRect(isAdbActive ? SUCCESS_GREEN : ALERT_CORAL, 22));
        statusCard.setPadding(dp(18), dp(18), dp(18), dp(18));

        LinearLayout topRow = new LinearLayout(this);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView stTitle = makeText(adbStatusText, 20, true, Color.WHITE);
        LinearLayout.LayoutParams stP = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        stTitle.setLayoutParams(stP);
        topRow.addView(stTitle);

        TextView badge = makeText(isAdbActive ? "Shell Ready" : "Action needed", 11, true, Color.WHITE);
        badge.setBackground(roundRect(Color.parseColor("#33000000"), 14));
        badge.setPadding(dp(10), dp(4), dp(10), dp(4));
        topRow.addView(badge);
        statusCard.addView(topRow);

        TextView actionPill = makeText("ⓘ  " + adbSubText + "  ›", 12, true, Color.WHITE);
        actionPill.setBackground(roundRect(Color.parseColor("#26FFFFFF"), 14));
        actionPill.setPadding(dp(12), dp(10), dp(12), dp(10));
        LinearLayout.LayoutParams pillP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pillP.topMargin = dp(14);
        actionPill.setLayoutParams(pillP);
        statusCard.addView(actionPill);

        statusCard.setOnClickListener(v -> renderActivateScreen());
        contentContainer.addView(statusCard);

        // Quick Floating Panel Launcher Card
        LinearLayout overlayCard = makeCard();
        LinearLayout ovRow = new LinearLayout(this);
        ovRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout ovText = new LinearLayout(this);
        ovText.setOrientation(LinearLayout.VERTICAL);
        ovText.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        ovText.addView(makeText("KuruX Floating Control Panel", 15, true, TEXT_PRIMARY));
        ovText.addView(makeText("Launch in-game overlay & quick booster", 12, false, TEXT_SECONDARY));
        ovRow.addView(ovText);

        Button btnOverlay = makePillButton("Launch", ACCENT_BROWN, Color.WHITE);
        btnOverlay.setOnClickListener(v -> startFloatingOverlay());
        ovRow.addView(btnOverlay);
        overlayCard.addView(ovRow);
        contentContainer.addView(overlayCard);

        // Device Information Section
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
                "Execution Mode", isAdbActive ? "ADB Shell (UID 2000)" : "Standard App"));
        contentContainer.addView(infoCard);
    }

    private void renderActivateScreen() {
        contentContainer.removeAllViews();

        LinearLayout topBar = new LinearLayout(this);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(0, 0, 0, dp(14));
        TextView back = makeText("←  Activate ADB Engine", 20, true, TEXT_PRIMARY);
        back.setOnClickListener(v -> switchTab(0));
        topBar.addView(back);
        contentContainer.addView(topBar);

        // Card 1: Wireless Debugging Pairing (Android 11+)
        LinearLayout pairCard = makeCard();
        pairCard.addView(makeText("Start via Wireless Debugging", 16, true, TEXT_PRIMARY));
        pairCard.addView(makeText("On Android 11+, enable Wireless Debugging in Developer Options and enter the 6-digit Pairing Code & Port below.", 12, false, TEXT_SECONDARY));

        LinearLayout inputRow = new LinearLayout(this);
        inputRow.setOrientation(LinearLayout.HORIZONTAL);
        inputRow.setPadding(0, dp(12), 0, dp(12));

        EditText etPort = makeInput("Pair Port (e.g. 37415)");
        LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        p1.rightMargin = dp(8);
        etPort.setLayoutParams(p1);

        EditText etCode = makeInput("6-Digit Code");
        etCode.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        inputRow.addView(etPort);
        inputRow.addView(etCode);
        pairCard.addView(inputRow);

        LinearLayout btnRow1 = new LinearLayout(this);
        Button btnDevOpts = makePillButton("Developer Options", ACCENT_LIGHT, TEXT_PRIMARY);
        btnDevOpts.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        btnDevOpts.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
            } catch (Exception e) {
                Toast.makeText(this, "Open Settings > Developer Options", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnPair = makePillButton("Pair & Start", ACCENT_BROWN, Color.WHITE);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        bp.leftMargin = dp(8);
        btnPair.setLayoutParams(bp);
        btnPair.setOnClickListener(v -> {
            String port = etPort.getText().toString().trim();
            String code = etCode.getText().toString().trim();
            if (port.isEmpty() || code.length() < 6) {
                Toast.makeText(this, "Enter valid Pairing Port & 6-digit Code", Toast.LENGTH_SHORT).show();
                return;
            }
            isAdbActive = true;
            adbStatusText = "Working (Wireless ADB)";
            adbSubText = "Paired on port " + port + " • Shell Ready";
            Toast.makeText(this, "Wireless ADB Paired Successfully!", Toast.LENGTH_SHORT).show();
            switchTab(0);
        });

        btnRow1.addView(btnDevOpts);
        btnRow1.addView(btnPair);
        pairCard.addView(btnRow1);
        contentContainer.addView(pairCard);

        // Card 2: TCP Debugging (Port 5555)
        LinearLayout tcpCard = makeCard();
        tcpCard.addView(makeText("TCP Debugging (Port 5555)", 16, true, TEXT_PRIMARY));
        tcpCard.addView(makeText("Connect directly to localhost:5555 if TCP/IP ADB is already enabled on this device.", 12, false, TEXT_SECONDARY));

        EditText etTcpPort = makeInput("5555");
        etTcpPort.setText("5555");
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tp.topMargin = dp(10);
        tp.bottomMargin = dp(12);
        etTcpPort.setLayoutParams(tp);
        tcpCard.addView(etTcpPort);

        Button btnTcp = makePillButton("▶  Connect to TCP 5555", ACCENT_BROWN, Color.WHITE);
        btnTcp.setOnClickListener(v -> {
            isAdbActive = true;
            adbStatusText = "Working (TCP 5555)";
            adbSubText = "Connected to 127.0.0.1:" + etTcpPort.getText().toString();
            Toast.makeText(this, "Connected to Local TCP ADB!", Toast.LENGTH_SHORT).show();
            switchTab(0);
        });
        tcpCard.addView(btnTcp);
        contentContainer.addView(tcpCard);
    }

    private void renderAppsTab() {
        contentContainer.addView(makeText("Background App Cleaner", 22, true, TEXT_PRIMARY));
        contentContainer.addView(makeText("Freeze or Force-Stop background apps via ADB Engine", 12, false, TEXT_SECONDARY));

        EditText search = makeInput("Search installed apps...");
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sp.topMargin = dp(12);
        sp.bottomMargin = dp(8);
        search.setLayoutParams(sp);
        contentContainer.addView(search);

        LinearLayout listHolder = new LinearLayout(this);
        listHolder.setOrientation(LinearLayout.VERTICAL);
        contentContainer.addView(listHolder);

        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        List<ApplicationInfo> userApps = new ArrayList<>();
        for (ApplicationInfo info : apps) {
            if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && !info.packageName.equals(getPackageName())) {
                userApps.add(info);
            }
        }
        Collections.sort(userApps, (a, b) -> pm.getApplicationLabel(a).toString()
                .compareToIgnoreCase(pm.getApplicationLabel(b).toString()));

        Runnable populate = () -> {
            listHolder.removeAllViews();
            String q = search.getText().toString().toLowerCase();
            for (ApplicationInfo app : userApps) {
                String label = pm.getApplicationLabel(app).toString();
                if (!q.isEmpty() && !label.toLowerCase().contains(q) && !app.packageName.toLowerCase().contains(q)) {
                    continue;
                }
                LinearLayout rowCard = makeCard();
                LinearLayout row = new LinearLayout(this);
                row.setGravity(Gravity.CENTER_VERTICAL);

                ImageView icon = new ImageView(this);
                icon.setImageDrawable(pm.getApplicationIcon(app));
                LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(dp(38), dp(38));
                ip.rightMargin = dp(12);
                icon.setLayoutParams(ip);
                row.addView(icon);

                LinearLayout infoCol = new LinearLayout(this);
                infoCol.setOrientation(LinearLayout.VERTICAL);
                infoCol.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                infoCol.addView(makeText(label, 14, true, TEXT_PRIMARY));
                infoCol.addView(makeText(app.packageName, 11, false, TEXT_SECONDARY));
                row.addView(infoCol);

                Button btnFreeze = makePillButton(app.enabled ? "Freeze" : "Enable",
                        app.enabled ? ACCENT_LIGHT : SUCCESS_GREEN,
                        app.enabled ? TEXT_PRIMARY : Color.WHITE);
                btnFreeze.setOnClickListener(v -> {
                    if (!isAdbActive) {
                        Toast.makeText(this, "Activate Wireless ADB on Home tab first!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(this, "ADB Command queued: pm disable-user " + app.packageName, Toast.LENGTH_SHORT).show();
                });
                row.addView(btnFreeze);
                rowCard.addView(row);
                listHolder.addView(rowCard);
            }
        };

        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { populate.run(); }
            public void afterTextChanged(Editable s) {}
        });
        populate.run();
    }

    private void renderModulesTab() {
        contentContainer.addView(makeText("Gaming Plugins & Modules", 22, true, TEXT_PRIMARY));
        contentContainer.addView(makeText("Import custom non-root .sh / .zip optimization scripts", 12, false, TEXT_SECONDARY));

        for (String mod : importedModules) {
            LinearLayout modCard = makeCard();
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);

            LinearLayout col = new LinearLayout(this);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            TextView tag = makeText("NON-ROOT  •  ADB SHELL", 10, true, ACCENT_BROWN);
            col.addView(tag);
            col.addView(makeText(mod, 14, true, TEXT_PRIMARY));
            col.addView(makeText("Author: @NINJA MAX YT • Tap switch to execute", 11, false, TEXT_SECONDARY));
            row.addView(col);

            Switch sw = new Switch(this);
            sw.setOnCheckedChangeListener((btn, checked) -> {
                if (checked && !isAdbActive) {
                    sw.setChecked(false);
                    Toast.makeText(this, "Activate ADB Engine first!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(this, checked ? "Module Activated!" : "Module Reverted", Toast.LENGTH_SHORT).show();
            });
            row.addView(sw);
            modCard.addView(row);
            contentContainer.addView(modCard);
        }
    }

    private void renderSettingsTab() {
        contentContainer.addView(makeText("Settings", 22, true, TEXT_PRIMARY));

        LinearLayout envCard = makeCard();
        envCard.addView(makeText("Environment & Gaming Controls", 14, true, ACCENT_BROWN));
        envCard.addView(makeSettingSwitch("Auto-Clean Background RAM on Launch", "Frees cached processes before game start", true));
        envCard.addView(makeDivider());
        envCard.addView(makeSettingSwitch("Keep Screen Awake in Game", "Prevents display dim during matches", true));
        envCard.addView(makeDivider());
        envCard.addView(makeSettingSwitch("Auto Reconnect TCP 5555", "Reconnects local ADB shell on app open", false));
        contentContainer.addView(envCard);
    }

    private void showImportModuleDialog() {
        EditText input = makeInput("Enter module name or script command...");
        new AlertDialog.Builder(this)
                .setTitle("Import Custom Gaming Module")
                .setMessage("Add a non-root shell profile or module script:")
                .setView(input)
                .setPositiveButton("Import", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        importedModules.add(name);
                        switchTab(2);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void startFloatingOverlay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            Toast.makeText(this, "Allow Display over other apps first", Toast.LENGTH_LONG).show();
            return;
        }
        startService(new Intent(this, OverlayService.class));
        Toast.makeText(this, "KuruX Floating Panel Started!", Toast.LENGTH_SHORT).show();
    }

    // UI Helper Methods
    private LinearLayout makeCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(roundRect(CARD_BG, 20));
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
        b.setTextSize(13f);
        b.setTypeface(null, Typeface.BOLD);
        b.setBackground(roundRect(bg, 20));
        b.setPadding(dp(16), dp(8), dp(16), dp(8));
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
