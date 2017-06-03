package com.alexvasilkov.gestures.sample.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alexvasilkov.android.commons.ui.Views;
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
        recyclerView.setAdapter(new SampleAdapter(getActivitiesList()));
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
        if (resolveList == null) {
            return list;
        }

        for (ResolveInfo info : resolveList) {
            if (packageName.equals(info.activityInfo.packageName)) {
                list.add(info.activityInfo);
            }
        }

        return list;
    }


    private class SampleAdapter extends RecyclerView.Adapter<SampleAdapter.ViewHolder> {

        private final List<ActivityInfo> list;

        SampleAdapter(List<ActivityInfo> list) {
            this.list = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            return new ViewHolder(viewGroup);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            viewHolder.info = list.get(position);
            viewHolder.text.setText(viewHolder.info.loadLabel(getPackageManager()));
        }

        @Override
        public int getItemCount() {
            return list == null ? 0 : list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            final TextView text;
            ActivityInfo info;

            ViewHolder(ViewGroup parent) {
                super(Views.inflate(parent, R.layout.item_main));
                text = (TextView) itemView;
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(@NonNull View view) {
                onItemClicked(info);
            }
        }

    }

}
