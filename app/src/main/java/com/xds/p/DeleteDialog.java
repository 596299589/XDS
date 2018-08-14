package com.xds.p;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.xds.p.bean.XdsBean;

import java.util.ArrayList;

public class DeleteDialog extends Dialog implements View.OnClickListener{

    private Context context;
    private ClickListenerInterface clickListenerInterface;

    public interface ClickListenerInterface {

//        void doConfirm();
//
//        void doCancel();
        void onSelect(XdsBean xdsBean);
    }

    public DeleteDialog(Context context, ArrayList<XdsBean> list) {
        super(context, R.style.DialogTheme);
        this.context = context;
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

        TextView tvConfirm = view.findViewById(R.id.confirm);
        TextView tvCancel = view.findViewById(R.id.cancel);

        tvConfirm.setOnClickListener(this);
        tvCancel.setOnClickListener(this);

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics(); // 获取屏幕宽、高用
        lp.width = (int) (d.widthPixels * 0.8); // 高度设置为屏幕的0.6
        dialogWindow.setAttributes(lp);
    }

    public void setClicklistener(ClickListenerInterface clickListenerInterface) {
        this.clickListenerInterface = clickListenerInterface;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.confirm:
//                clickListenerInterface.onSelect();
                break;
            case R.id.cancel:
                break;
        }
    }

}
