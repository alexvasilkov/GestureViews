package com.alexvasilkov.gestures.sample.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.alexvasilkov.android.commons.state.InstanceStateManager;
import com.alexvasilkov.events.Events;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InstanceStateManager.restoreInstanceState(this, savedInstanceState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Events.register(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        InstanceStateManager.saveInstanceState(this, outState);
        super.onSaveInstanceState(outState);
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

    @NonNull
    public ActionBar getSupportActionBarNotNull() {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            throw new NullPointerException("Action bar was not initialized");
        }
        return actionBar;
    }

}
