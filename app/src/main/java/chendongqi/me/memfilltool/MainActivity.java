package chendongqi.me.memfilltool;

import android.app.Activity;
import android.app.ActivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends Activity {

    private static String ACTION_LMK = "com.lge.mlt.service.intent.action.LMK_EVENT";
    private static final String KEY_LMK_PACKAGE = "LMK_Package";
    private static final String KEY_LMK_ADJ = "LMK_adj";

    private static final int MESSAGE_UPDATE_UI = 0;

    private Button mButtonFill = null;
    private Button mButtonFree = null;
    private TextView mTextLmkContent  = null;
    private TextView mTextMemContent = null;
    private StringBuffer mStringBufferLmk = new StringBuffer();
    private StringBuffer mStringBufferMem = new StringBuffer();
    private int mInfoNumber = 0;// 信息的序号

    private boolean isRunning = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButtonFill = (Button) findViewById(R.id.button_fill);
        mButtonFree = (Button) findViewById(R.id.button_free);
        mTextLmkContent = (TextView) findViewById(R.id.content_lmkinfo);
        mTextMemContent = (TextView) findViewById(R.id.content_meminfo);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_LMK);
        registerReceiver(mReceiver, intentFilter);

        // 写内存操作
        mButtonFill.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {

                if(isRunning == false) {
                    mFillThread.start();
                }
            }
        });

        // 释放内存操作
        mButtonFree.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                MemOpUtils.memfree();
                MemOpUtils.setflag();// notify JNI to stop malloc memory
            }
        });
    }

    @Override
    protected void onDestroy() {
        MemOpUtils.setflag();// notify JNI to stop malloc memory
        MemOpUtils.memfree();
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        MemOpUtils.setflag();// notify JNI to stop malloc memory
        MemOpUtils.memfree();
        super.onPause();
    }

    // need android.uid.system & permission DUMP
    /*
    private String getFreeMemFromMemDump() {
        String line = "";
        StringBuilder sb = new StringBuilder();
        String[] progArray = { "dumpsys", "meminfo" };

        Process dumpProc = null;
        BufferedReader reader = null;
        try {
            dumpProc = Runtime.getRuntime().exec(progArray);
            if (dumpProc == null) {
                return "";
            }
            reader = new BufferedReader(new InputStreamReader(dumpProc.getInputStream()), 1024);

            while ((line = reader.readLine()) != null && line.startWith("Free")) {// 获取Free Memory的那一行
                android.util.Log.d("chendongqi_lmk", "line = " + line);
                sb.append(line + "\n");
            }

            return sb.toString();

        } catch (Exception e) {
            return "";
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    */

    private StringBuffer getFreaMemoryFromMeminfo() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        manager.getMemoryInfo(memoryInfo);

        StringBuffer sb = new StringBuffer();
        sb.append("No.").append(mInfoNumber).
                append("-Total memory: ").append(memoryInfo.totalMem/1024/1024).append("M").
                append(", avaible memory: ").
                append(memoryInfo.availMem/1024/1024).append("M").
                append("\n");

        return sb;
    }

    private StringBuffer getLkmInfo(String packageName, int adj) {
        StringBuffer sb = new StringBuffer();
        sb.append("No.").append(mInfoNumber).
                append("-process name: ").append(packageName).
                append(", adj: ").append(adj).
                append("\n");
        return sb;
    }

    // 接收lmk上报的广播消息
    // 读取广播中的信息（进程名，adj的值）显示到界面中
    // 在收到lmk广播后dump当前的内存信息显示到界面中
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            android.util.Log.v("chendongqi_lmk", "receive broadcast from lmk reporter");
            String action = intent.getAction();
            Bundle bd = intent.getExtras();

            if(action.equals(ACTION_LMK)) {
                String packageName = bd.getString(KEY_LMK_PACKAGE);
                int adj = bd.getInt(KEY_LMK_ADJ);
                android.util.Log.d("chendongqi_lmk", "package = " + packageName + ", adj = " + adj);
                mStringBufferLmk.append(getLkmInfo(packageName, adj));
                mStringBufferMem.append(getFreaMemoryFromMeminfo());
                Message msg = new Message();
                msg.arg1 = MESSAGE_UPDATE_UI;
                mHandler.sendMessage(msg);
                mInfoNumber++;
                if(adj <= 2) {
                    android.util.Log.d("chendongqi_lmk", "notify JNI to set stop malloc flag true");
                    MemOpUtils.setflag();// notify JNI to stop malloc memory
                }
            }
        }
    };

    private Thread mFillThread = new Thread() {
        @Override
        public void run() {
            android.util.Log.v("chendongqi_lmk", "start to malloc memory");
            isRunning = true;
            MemOpUtils.memfill();// 每次10M
        }
    };

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.arg1) {
                case MESSAGE_UPDATE_UI:
                    android.util.Log.v("chendongqi_lmk", "receive message to update ui");
                    mTextLmkContent.setText(mStringBufferLmk.toString());
                    mTextMemContent.setText(mStringBufferMem.toString());
                    break;
                default:
                    break;
            }
        }
    };

}
