package com.alexvasilkov.gestures.sample.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alexvasilkov.android.commons.utils.Views;
import com.alexvasilkov.gestures.sample.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = Views.find(this, R.id.main_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new SampleAdapter(this, getActivitiesList()));
    }

    private void onItemClicked(ActivityInfo info) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(this, info.name));
        startActivity(intent);
    }

    private List<ActivityInfo> getActivitiesList() {
        List<ActivityInfo> list = new ArrayList<>();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_SAMPLE_CODE);

        String packageName = getApplicationInfo().packageName;
        List<ResolveInfo> resolveList = getPackageManager().queryIntentActivities(mainIntent, 0);
        if (resolveList == null) return list;

        for (ResolveInfo info : resolveList) {
            if (packageName.equals(info.activityInfo.packageName)) list.add(info.activityInfo);
        }

        return list;
    }

    private static class SampleAdapter extends RecyclerView.Adapter<SampleAdapter.ViewHolder> {

        private final PackageManager mPackageManager;
        private final List<ActivityInfo> mList;

        public SampleAdapter(Context context, List<ActivityInfo> list) {
            super();
            mPackageManager = context.getPackageManager();
            mList = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new ViewHolder(viewGroup);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            viewHolder.info = mList.get(i);
            viewHolder.text.setText(viewHolder.info.loadLabel(mPackageManager));
        }

        @Override
        public int getItemCount() {
            return mList == null ? 0 : mList.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            final TextView text;
            ActivityInfo info;

            public ViewHolder(ViewGroup parent) {
                super(Views.inflate(parent, R.layout.item_main));
                text = (TextView) itemView;
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(@NonNull View v) {
                ((MainActivity) v.getContext()).onItemClicked(info);
            }
        }

    }

}
