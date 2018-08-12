package com.xds.p;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.xds.p.bean.XdsBean;
import com.xds.p.db.DbHelper;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private Button mBtnAdd, mBtnQuery;
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
        setContentView(R.layout.activity_main);

        mBtnAdd = findViewById(R.id.btn_add);
        mBtnQuery = findViewById(R.id.btn_query);

        mBtnAdd.setOnClickListener(this);
        mBtnQuery.setOnClickListener(this);

        tv_TheFirstTwoNumbers = findViewById(R.id.tv_TheFirstTwoNumbers);
        tv_TheFirstTwoNumbers.setVisibility(View.GONE);

        mEtAdd = findViewById(R.id.et_add);
        mLvDisplay = findViewById(R.id.lv_display);

        mXdsAdapter = new XdsAdapter(this);

        mDbHelper = new DbHelper(MainActivity.this);
        createHt();
        createHandlerCallBack();
    }

    @Override
    protected void onResume() {
        super.onResume();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEtAdd.getWindowToken(), 0);
    }

    private int weiHaoTiaoJian;
    private StringBuilder weiHaoTiaoJianSB = new StringBuilder();

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add:
                add();
                break;

            case R.id.btn_query:
                mHandler.sendEmptyMessage(0);
                break;
        }
    }

    private ArrayList<XdsBean> mAllList;
    private ArrayList<ArrayList<XdsBean>> mAllListParse = new ArrayList<>();

    private void add() {
        if (null != mDbHelper) {
            String numS = mEtAdd.getText().toString();
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
                tv_TheFirstTwoNumbers.setVisibility(View.GONE);
            }
            mEtAdd.setText("");
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
                mAllList.add(xdsBean);
                mHandler.sendEmptyMessage(1);
            }
            for (XdsBean b : mAllList) {
                Log.d(TAG, "###############" + b.toString());
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
            mAllList = mDbHelper.query();
            for (XdsBean b : mAllList) {
                Log.d(TAG, "************" + b.toString());
            }
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

            int zushu = count / 5;
            int yushu = count % 5;
            if (yushu > 0) {
                zushu++;
            }

            for (int i = 0; i < zushu; i++) {
                int startIndex = i * 5;
                int endIndex = startIndex + 5;
                Log.d(TAG, "startIndex:" + startIndex + " endIndex:" + endIndex + " count:" + count);
                if (endIndex > count) {
                    endIndex = startIndex + yushu;
                }
                List<XdsBean> l = mAllList.subList(startIndex, endIndex);
                ArrayList<XdsBean> l2 = new ArrayList<>();
                for (int j = 0; j < l.size(); j++) {
                    Log.d(TAG, j + "###" + l.get(j));
                    l2.add(l.get(j));
                }
                mAllListParse.add(l2);
            }
            mHandlerCallBack.sendEmptyMessage(0);
        }
    }

    private void displayAllList() {
        mXdsAdapter.update(mAllListParse);
        mLvDisplay.setAdapter(mXdsAdapter);
    }

    /**
     * 创建HandlerThread
     */
    private void createHt() {
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
        mHandlerCallBack = new Handler() {
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
