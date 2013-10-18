package com.icc.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.icc.R;
import com.icc.model.Account;

import java.util.List;

/**
 * @author Rob Powell
 */
public class AccountAdapter extends BaseAdapter {

    private Context context;
    private List<Account> accounts;
    private static LayoutInflater layoutInflater;

    public AccountAdapter(final Context context, final List<Account> accounts) {
        this.context = context;
        this.accounts = accounts;
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        return accounts.size();
    }

    @Override
    public Object getItem(int i) {
        return accounts.get(i);
    }

    @Override
    public long getItemId(int i) {
        return accounts.get(i).getId();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        View v = view;
        TextView textViewAccountName;

        if(v == null) {
            v = layoutInflater.inflate(R.layout.manage_account_list_item, null);
            textViewAccountName = (TextView)v.findViewById(R.id.textview_manage_account_name);
        }

        return v;
    }
}