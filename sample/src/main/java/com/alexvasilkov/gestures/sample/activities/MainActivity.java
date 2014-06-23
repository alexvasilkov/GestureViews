package com.alexvasilkov.gestures.sample.activities;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.alexvasilkov.android.commons.adapters.ItemsAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(getSampleAdapter());
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        ActivityInfo info = (ActivityInfo) l.getItemAtPosition(position);
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(this, info.name));
        startActivity(intent);
    }

    private BaseAdapter getSampleAdapter() {
        List<ActivityInfo> items = new ArrayList<ActivityInfo>();

        try {
            ActivityInfo[] activitiesInfo = getPackageManager().getPackageInfo(getPackageName(),
                    PackageManager.GET_ACTIVITIES).activities;

            for (ActivityInfo info : activitiesInfo) {
                if (!getClass().getName().equals(info.name)) items.add(info);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return new SampleAdapter(this, items);
    }

    private static class SampleAdapter extends ItemsAdapter<ActivityInfo> {

        public SampleAdapter(Context context, List<ActivityInfo> list) {
            super(context);
            setItemsList(list);
        }

        @Override
        protected View createView(ActivityInfo item, int pos, ViewGroup parent, LayoutInflater inflater) {
            return inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        @Override
        protected void bindView(ActivityInfo item, int pos, View convertView) {
            TextView tv = (TextView) convertView;
            if (TextUtils.isEmpty(item.nonLocalizedLabel)) {
                tv.setText(item.labelRes);
            } else {
                tv.setText(item.nonLocalizedLabel);
            }
        }

    }

}
