package com.xds.p;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.TextView;

import com.xds.p.bean.XdsBean;

import java.util.ArrayList;

public class DeleteDialog extends Dialog implements View.OnClickListener {

    private Context context;
    private TextView mTvTitle;
    private int mIndex = 0;
    private RadioButton mRb1, mRb2, mRb3, mRb4, mRb5, mRb6;
    private ClickListenerInterface clickListenerInterface;

    private ArrayList<XdsBean> mList;

    public interface ClickListenerInterface {
        void onSelect(XdsBean xdsBean);
    }

    public DeleteDialog(Context context, ArrayList<XdsBean> list) {
        super(context, R.style.DialogTheme);
        this.context = context;
        this.mList = list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        init();
    }

    public void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.confirm_dialog, null);
        setContentView(view);

        mTvTitle = view.findViewById(R.id.title);
        TextView tvConfirm = view.findViewById(R.id.confirm);
        TextView tvCancel = view.findViewById(R.id.cancel);

        mRb1 = view.findViewById(R.id.radioButton1);
        mRb2 = view.findViewById(R.id.radioButton2);
        mRb3 = view.findViewById(R.id.radioButton3);
        mRb4 = view.findViewById(R.id.radioButton4);
        mRb5 = view.findViewById(R.id.radioButton5);
        mRb6 = view.findViewById(R.id.radioButton6);

        dealRadioGroup();

        tvConfirm.setOnClickListener(this);
        tvCancel.setOnClickListener(this);

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics(); // 获取屏幕宽、高用
        lp.width = (int) (d.widthPixels * 0.8); // 高度设置为屏幕的0.6
        dialogWindow.setAttributes(lp);
    }

    private void dealRadioGroup() {
        mRb1.setOnClickListener(this);
        mRb2.setOnClickListener(this);
        mRb3.setOnClickListener(this);
        mRb4.setOnClickListener(this);
        mRb5.setOnClickListener(this);
        mRb6.setOnClickListener(this);

        mRb1.setVisibility(View.GONE);
        mRb2.setVisibility(View.GONE);
        mRb3.setVisibility(View.GONE);
        mRb4.setVisibility(View.GONE);
        mRb5.setVisibility(View.GONE);
        mRb6.setVisibility(View.GONE);

        for (int i = 0; i < mList.size(); i++) {
            switch (i) {
                case 0:
                    mRb1.setText(mList.get(i).getNum());
                    mRb1.setVisibility(View.VISIBLE);
                    break;
                case 1:
                    mRb2.setText(mList.get(i).getNum());
                    mRb2.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    mRb3.setText(mList.get(i).getNum());
                    mRb3.setVisibility(View.VISIBLE);
                    break;
                case 3:
                    mRb4.setText(mList.get(i).getNum());
                    mRb4.setVisibility(View.VISIBLE);
                    break;
                case 4:
                    mRb5.setText(mList.get(i).getNum());
                    mRb5.setVisibility(View.VISIBLE);
                    break;
                case 5:
                    mRb6.setText(mList.get(i).getNum());
                    mRb6.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    public void setClicklistener(ClickListenerInterface clickListenerInterface) {
        this.clickListenerInterface = clickListenerInterface;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.radioButton1:
                mIndex = 0;
                break;
            case R.id.radioButton2:
                mIndex = 1;
                break;
            case R.id.radioButton3:
                mIndex = 2;
                break;
            case R.id.radioButton4:
                mIndex = 3;
                break;
            case R.id.radioButton5:
                mIndex = 4;
                break;
            case R.id.radioButton6:
                mIndex = 5;
                break;
            case R.id.confirm:
//                clickListenerInterface.onSelect();
                clickListenerInterface.onSelect(mList.get(mIndex));
                this.dismiss();
                break;
            case R.id.cancel:
                this.dismiss();
                break;
        }
        if (mIndex < mList.size()) {
            mTvTitle.setText("你要删除的是:" + mList.get(mIndex).getNum());
        }
    }

}
