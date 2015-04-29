package com.swift.ui.view.util;

import android.content.ContentResolver;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by sean on 29/04/15.
 */
public class MessageHistoryAdapter extends BaseAdapter {

    private final ContentResolver resolver;
    private final LayoutInflater layoutInflater;

    private final List<String> messages = Arrays.asList(new String[] {"One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten"});

    public MessageHistoryAdapter(final Context context) {
        this.resolver = context.getContentResolver();
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView view = (TextView) convertView;
        if (view == null) {
            view = (TextView) this.layoutInflater.inflate(android.R.layout.simple_list_item_1, null);
        }
        view.setText(messages.get(position));

        return view;
    }
}
