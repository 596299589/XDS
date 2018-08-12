package com.xds.p;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xds.p.bean.XdsBean;
import com.xds.p.db.DbHelper;

import java.util.ArrayList;


public class XdsAdapter extends BaseAdapter {

    private final String TAG = "XdsAdapter";
    private Context mContext;

    public XdsAdapter(Context context) {
        mContext = context;
    }


    private ArrayList<ArrayList<XdsBean>> mList;

    public void update(ArrayList<ArrayList<XdsBean>> list) {
        mList = list;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder vh;
        if (null == view) {
            vh = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.xds_adapter, null);
            vh.item1 = view.findViewById(R.id.tv_item1);
            vh.item2 = view.findViewById(R.id.tv_item2);
            vh.item3 = view.findViewById(R.id.tv_item3);
            vh.item4 = view.findViewById(R.id.tv_item4);
            vh.item5 = view.findViewById(R.id.tv_item5);
            view.setTag(vh);
        } else {
            vh = (ViewHolder) view.getTag();
        }

        setBackgroundColor(vh.item1, null, "");
        setBackgroundColor(vh.item2, null, "");
        setBackgroundColor(vh.item3, null, "");
        setBackgroundColor(vh.item4, null, "");
        setBackgroundColor(vh.item5, null, "");


        ArrayList<XdsBean> list = mList.get(i);
        for (int j = 0; j < list.size(); j++){
            int yu = j % 5;
            if (yu == 0) {
                setBackgroundColor(vh.item1, list.get(j).getYangSe(), list.get(j).getNum());
            } else if (yu == 1) {
                setBackgroundColor(vh.item2, list.get(j).getYangSe(), list.get(j).getNum());
            } else if (yu == 2) {
                setBackgroundColor(vh.item3, list.get(j).getYangSe(), list.get(j).getNum());
            } else if (yu == 3) {
                setBackgroundColor(vh.item4, list.get(j).getYangSe(), list.get(j).getNum());
            } else if (yu == 4) {
                setBackgroundColor(vh.item5, list.get(j).getYangSe(), list.get(j).getNum());
            }
        }
        return view;
    }

    class ViewHolder {
        private TextView item1;
        private TextView item2;
        private TextView item3;
        private TextView item4;
        private TextView item5;
    }

    private void setBackgroundColor(TextView v, String yanSe, String num) {
        if (null != yanSe) {
            if (DbHelper.red.contains(yanSe)) {
                v.setBackground(mContext.getResources().getDrawable(R.drawable.red));
            } else if (DbHelper.green.contains(yanSe)) {
                v.setBackground(mContext.getResources().getDrawable(R.drawable.green));
            } else if (DbHelper.blue.contains(yanSe)) {
                v.setBackground(mContext.getResources().getDrawable(R.drawable.blue));
            }
        } else {
            v.setBackgroundColor(Color.TRANSPARENT);
        }
        v.setText(num);
    }
}
