package com.alexvasilkov.gestures.sample.activities;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
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

    private List<ActivityInfo> getInfosList() {
        List<ActivityInfo> list = new ArrayList<ActivityInfo>();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_SAMPLE_CODE);

        List<ResolveInfo> resolveList = getPackageManager().queryIntentActivities(mainIntent, 0);
        if (resolveList == null) return list;

        for (ResolveInfo info : resolveList) {
            list.add(info.activityInfo);
        }

        return list;
    }

    private BaseAdapter getSampleAdapter() {
        return new SampleAdapter(this, getInfosList());
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
