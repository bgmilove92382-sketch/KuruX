package com.ninjamaxyt.kuruxcompanion;

import android.app.*;
import android.content.*;
import android.graphics.PixelFormat;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.IBinder;
import android.view.*;
import android.widget.*;

public class OverlayService extends Service {
    private WindowManager wm; private View bubble, panel; private WindowManager.LayoutParams bubbleLp, panelLp; private boolean expanded=false;
    private int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable bg(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));d.setStroke(dp(1),0xffc9d9f2);return d;}
    @Override public void onCreate(){super.onCreate();startForeground(41,notification());wm=(WindowManager)getSystemService(WINDOW_SERVICE);showBubble();}
    private Notification notification(){String ch="kurux_overlay";if(Build.VERSION.SDK_INT>=26){NotificationChannel c=new NotificationChannel(ch,"KuruX Floating Panel",NotificationManager.IMPORTANCE_LOW);((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c);} Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,ch):new Notification.Builder(this);return b.setContentTitle("KuruX Companion active").setContentText("Floating gaming panel is ready").setSmallIcon(android.R.drawable.ic_menu_view).build();}
    private int type(){return Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE;}
    private void showBubble(){if(bubble!=null)return; TextView t=new TextView(this);t.setText("KX");t.setTextColor(Color.WHITE);t.setTextSize(14);t.setGravity(Gravity.CENTER);t.setBackground(bg(0xff246dcc,28));bubble=t; bubbleLp=new WindowManager.LayoutParams(dp(54),dp(54),type(),WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);bubbleLp.gravity=Gravity.TOP|Gravity.RIGHT; bubbleLp.x=dp(12);bubbleLp.y=dp(220);t.setOnTouchListener(new View.OnTouchListener(){float sx,sy;int ox,oy;long down;public boolean onTouch(View v,MotionEvent e){if(e.getAction()==MotionEvent.ACTION_DOWN){sx=e.getRawX();sy=e.getRawY();ox=bubbleLp.x;oy=bubbleLp.y;down=System.currentTimeMillis();return true;}if(e.getAction()==MotionEvent.ACTION_MOVE){bubbleLp.x=ox+(int)(sx-e.getRawX());bubbleLp.y=oy+(int)(e.getRawY()-sy);wm.updateViewLayout(bubble,bubbleLp);return true;}if(e.getAction()==MotionEvent.ACTION_UP){if(Math.abs(e.getRawX()-sx)<dp(8)&&Math.abs(e.getRawY()-sy)<dp(8))togglePanel();return true;}return true;}});wm.addView(bubble,bubbleLp);}
    private void togglePanel(){if(expanded){hidePanel();return;}showPanel();}
    private void hidePanel(){if(panel!=null){try{wm.removeView(panel);}catch(Exception ignored){}panel=null;}expanded=false;}
    private void addToggle(LinearLayout box,String title,String key){android.content.SharedPreferences p=getSharedPreferences("profiles",0);Switch s=new Switch(this);s.setText(title);s.setTextColor(0xff18243a);s.setPadding(dp(8),dp(8),dp(8),dp(8));s.setChecked(p.getBoolean(key,false));s.setOnCheckedChangeListener((v,on)->p.edit().putBoolean(key,on).apply());box.addView(s);}
    private void launch(String pkg){try{Intent i=getPackageManager().getLaunchIntentForPackage(pkg);if(i==null){Toast.makeText(this,"Game not installed",Toast.LENGTH_SHORT).show();return;}i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(i);}catch(Exception e){Toast.makeText(this,"Unable to launch game",Toast.LENGTH_SHORT).show();}}
    private void addGame(LinearLayout box,String name,String pkg){Button b=new Button(this);b.setText("LAUNCH "+name);b.setOnClickListener(v->launch(pkg));box.addView(b);}
    private void showPanel(){if(panel!=null)return;LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(12),dp(10),dp(12),dp(10));box.setBackground(bg(0xfaf7faff,22));TextView head=new TextView(this);head.setText("KuruX  •  GAMING TOOLS      ✕");head.setTextSize(16);head.setTextColor(0xff1a3761);head.setPadding(dp(8),dp(8),dp(8),dp(12));head.setOnClickListener(v->hidePanel());box.addView(head);
        addToggle(box,"Game Mode profile","game_mode");addToggle(box,"Performance profile","performance");addToggle(box,"Touch profile","touch");addToggle(box,"RAM profile","ram");
        TextView sizeLabel=new TextView(this);sizeLabel.setText("Floating button size");sizeLabel.setTextColor(0xff172033);box.addView(sizeLabel);SeekBar size=new SeekBar(this);size.setMax(3);size.setProgress(1);box.addView(size);size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar b,int p,boolean u){if(u&&bubble!=null){int[] a={dp(42),dp(54),dp(66),dp(78)};bubbleLp.width=a[p];bubbleLp.height=a[p];wm.updateViewLayout(bubble,bubbleLp);}}public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}});
        addGame(box,"FREE FIRE","com.dts.freefireth");addGame(box,"FREE FIRE MAX","com.dts.freefiremax");addGame(box,"BGMI","com.pubg.imobile");Button close=new Button(this);close.setText("CLOSE PANEL");close.setOnClickListener(v->hidePanel());box.addView(close);
        panel=box;panelLp=new WindowManager.LayoutParams(dp(300),WindowManager.LayoutParams.WRAP_CONTENT,type(),WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);panelLp.gravity=Gravity.CENTER;wm.addView(panel,panelLp);expanded=true;}
    @Override public int onStartCommand(Intent i,int flags,int id){return START_STICKY;}
    @Override public void onDestroy(){hidePanel();if(bubble!=null){try{wm.removeView(bubble);}catch(Exception ignored){}bubble=null;}super.onDestroy();}
    @Override public IBinder onBind(Intent i){return null;}
}
