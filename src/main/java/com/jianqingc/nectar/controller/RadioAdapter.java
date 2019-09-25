package com.jianqingc.nectar.controller;

/**
 * Created by HuangMengxue on 17/5/3.
 */

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import java.util.List;

public class RadioAdapter extends BaseAdapter {

    private List<String> authors;
    private Context c;

    public RadioAdapter(Context c, List<String> authors) {
        super();
        this.c = c;
        this.authors = authors;
    }

    @Override
    public int getCount() {
        return authors.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        //返回每一条Item的Id
        return arg0;
    }

    @Override
    public boolean hasStableIds() {
        //getCheckedItemIds()方法要求此处返回为真
        return true;
    }
    @Override
    public View getView(int arg0, View arg1, ViewGroup arg2) {

        ChoiceListItemView choiceListItemView = new ChoiceListItemView(c, null);
        choiceListItemView.setName(authors.get(arg0));
        return choiceListItemView;
    }

}
