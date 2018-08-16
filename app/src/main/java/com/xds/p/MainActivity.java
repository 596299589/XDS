package com.xds.p;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.xds.p.bean.XdsBean;
import com.xds.p.db.DbHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private Button mBtnAdd;
    private EditText mEtAdd;
    private ListView mLvDisplay;
    private TextView tv_TheFirstTwoNumbers;

    private Handler mHandler;
    private Handler mHandlerCallBack;
    private DbHelper mDbHelper;
    private XdsAdapter mXdsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPermissions();
        setContentView(R.layout.activity_main);

        mBtnAdd = findViewById(R.id.btn_add);

        mBtnAdd.setOnClickListener(this);

        tv_TheFirstTwoNumbers = findViewById(R.id.tv_TheFirstTwoNumbers);
        tv_TheFirstTwoNumbers.setVisibility(View.INVISIBLE);

        mEtAdd = findViewById(R.id.et_add);
        mLvDisplay = findViewById(R.id.lv_display);
        mLvDisplay.setOnItemLongClickListener(onItemLongClickListener);

        mXdsAdapter = new XdsAdapter(this);

        mDbHelper = new DbHelper(MainActivity.this);
        createHandlerThread();
        createHandlerCallBack();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mXdsAdapter.update(mAllListParse);
        mLvDisplay.setAdapter(mXdsAdapter);
        mHandler.sendEmptyMessageDelayed(0, 200);
    }

    private int weiHaoTiaoJian;
    private StringBuilder weiHaoTiaoJianSB = new StringBuilder();

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add:
                add();
                break;
        }
    }

    private AdapterView.OnItemLongClickListener onItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Log.d(TAG, "onItemLongClick position = " + position);
            ArrayList<XdsBean> list = mAllListParse.get(position);
            DeleteDialog confirmDialog = new DeleteDialog(MainActivity.this, list);
            confirmDialog.setClicklistener(new DeleteDialog.ClickListenerInterface() {
                @Override
                public void onSelect(XdsBean xdsBean) {
                    Log.d(TAG, "onSelect:" + xdsBean.toString());
                    if (null != mDbHelper){
                        mDbHelper.deleteDatas("_id=?", new String[]{String.valueOf(xdsBean.get_id())});
                    }
                }
            });
            confirmDialog.show();
            return false;
        }
    };



    private ArrayList<XdsBean> mAllList;
    private ArrayList<ArrayList<XdsBean>> mAllListParse = new ArrayList<>();

    private void add() {
        if (null != mDbHelper) {
            String numS = mEtAdd.getText().toString();
            copyDb(numS);
            mEtAdd.setText("");
            if (weiHaoTiaoJian != 0) {
                weiHaoTiaoJian--;
            }
            String numS1 = numS;
            if (numS.length() == 1) {
                numS1 = "0" + numS1;
            }
            Log.d(TAG, "weiHaoTiaoJianSB = " + weiHaoTiaoJianSB.toString() + "  numS1 = " + numS1);

            if (weiHaoTiaoJian == 0 || weiHaoTiaoJianSB.toString().contains(numS1)) {
                weiHaoTiaoJian = 0;
                tv_TheFirstTwoNumbers.setVisibility(View.INVISIBLE);
            }

            if (TextUtils.isEmpty(numS)) {
                Toast.makeText(MainActivity.this, "您没有输入数字", Toast.LENGTH_SHORT).show();
                return;
            }
            int num = Integer.parseInt(numS);
            if (num < 1 || num > 49) {
                Toast.makeText(MainActivity.this, "输入的数字非1-49范围", Toast.LENGTH_SHORT).show();
                return;
            }

            ContentValues cv = new ContentValues();
            cv.put(DbHelper.XdsDb.NUM, num);
            cv.put(DbHelper.XdsDb.TIME, System.currentTimeMillis());
            mDbHelper.insertData(cv);

            XdsBean xdsBean = mDbHelper.queryLastData();

            if (null != mAllList) {
                if (mAllList.size() == 30) {
                    mAllList.remove(0);
                }
                mAllList.add(xdsBean);
                mHandler.sendEmptyMessage(1);
            }

            if (xdsBean != null) {
                Log.d(TAG, "*******" + xdsBean.toString());
                ArrayList<XdsBean> list = mDbHelper.queryTheFirstTwoNumbers(xdsBean.get_id());
                if (null != list && list.size() == 2) {
                    XdsBean b1 = list.get(0);
                    XdsBean b2 = list.get(1);
                    String num1 = b1.getNum().substring(1);
                    String num2 = b2.getNum().substring(1);
                    Log.d(TAG, "num1:" + num1 + "  num2:" + num2);
                    if (num1.equals(num2)) {
                        weiHaoTiaoJianSB = new StringBuilder();
                        if ("0".equals(num1)) {
                            weiHaoTiaoJianSB.append(" 1" + num1 + "  2" + num1 + "  3" + num1 + "  4" + num1);
                        } else {
                            weiHaoTiaoJianSB.append(" 0" + num1 + "  1" + num1 + "  2" + num1 + "  3" + num1 + "  4" + num1);
                        }
                        weiHaoTiaoJian = 3;
                        tv_TheFirstTwoNumbers.setText("尾数条件" + num1 + ":" + weiHaoTiaoJianSB.toString());
                        tv_TheFirstTwoNumbers.setVisibility(View.VISIBLE);
                    }
                }
            }

        } else {
            Toast.makeText(MainActivity.this, "请初始化后再操作", Toast.LENGTH_SHORT).show();
        }
    }

    private void queryAll() {
        if (null != mDbHelper) {
            mAllList = mDbHelper.query(30);
            Collections.reverse(mAllList);
            parseAllList();
        }
    }

    /**
     * 转换所有数据为adapter显示需要的数据
     */
    private void parseAllList() {
        if (null != mAllList && !mAllList.isEmpty()) {
            int count = mAllList.size();
            mAllListParse.clear();

            int zushu = count / 6;
            int yushu = count % 6;
            if (yushu > 0) {
                zushu++;
            }

            for (int i = 0; i < zushu; i++) {
                int startIndex = i * 6;
                int endIndex = startIndex + 6;
                Log.d(TAG, "startIndex:" + startIndex + " endIndex:" + endIndex + " count:" + count);
                if (endIndex > count) {
                    endIndex = startIndex + yushu;
                }
                List<XdsBean> l = mAllList.subList(startIndex, endIndex);
                ArrayList<XdsBean> l2 = new ArrayList<>();
                for (int j = 0; j < l.size(); j++) {
//                    Log.d(TAG, j + "###" + l.get(j));
                    l2.add(l.get(j));
                }
                mAllListParse.add(l2);
            }
            mHandlerCallBack.sendEmptyMessage(0);
        }
    }

    private void displayAllList() {
        Log.d(TAG, "displayAllList");
        mXdsAdapter.update(mAllListParse);
    }

    private void getPermissions() {
        //动态获取权限，Android 6.0 新特性，一些保护权限，除了要在AndroidManifest中声明权限，还要使用如下代码动态获取
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 101;
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};
            //验证是否许可权限
            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                    return;
                }
            }
        }
    }

    private void copyDb(String s) {
        if ("5656".equals(s)) {
            File db = new File("/data/data/com.xds.p/databases/xdsdata.db");
            if (db.exists()) {
                try {
                    File toFile = new File("/sdcard/xdsdata.db");
                    if (toFile.exists()) {
                        toFile.delete();
                    } else {
                        toFile.createNewFile();
                    }
                    InputStream fosfrom = new FileInputStream(db);
                    OutputStream fosto = new FileOutputStream(toFile);
                    byte bt[] = new byte[1024];
                    int c;
                    while ((c = fosfrom.read(bt)) > 0) {
                        fosto.write(bt, 0, c);
                    }
                    fosfrom.close();
                    fosto.close();
                    Log.d(TAG, "拷贝成功");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void createHandlerThread() {
        HandlerThread ht = new HandlerThread("xds_thread");
        ht.start();
        mHandler = new Handler(ht.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        queryAll();
                        break;
                    case 1:
                        parseAllList();
                        break;
                }
            }
        };
    }

    private void createHandlerCallBack() {
        mHandlerCallBack = new Handler(MainActivity.this.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        displayAllList();
                        break;
                }
            }
        };
    }
}
