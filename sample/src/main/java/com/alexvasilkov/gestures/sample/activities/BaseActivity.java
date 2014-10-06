package com.alexvasilkov.gestures.sample.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

import com.alexvasilkov.events.Events;

public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActionBar() != null) getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Events.register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Events.unregister(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
