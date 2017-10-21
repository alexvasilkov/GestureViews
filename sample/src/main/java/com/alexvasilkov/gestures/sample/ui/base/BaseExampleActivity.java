package com.alexvasilkov.gestures.sample.ui.base;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.ui.base.settings.SettingsMenu;
import com.alexvasilkov.gestures.sample.ui.base.settings.SettingsSetupListener;

public abstract class BaseExampleActivity extends BaseActivity {

    private final SettingsMenu settingsMenu = new SettingsMenu();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsMenu.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Initializing toolbar
        if (getSupportActionBar() == null) {
            final Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
        }
        getSupportActionBarNotNull().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        settingsMenu.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return settingsMenu.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (settingsMenu.onOptionsItemSelected(item)) {
            supportInvalidateOptionsMenu();
            onSettingsChanged();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    protected SettingsSetupListener getSettingsListener() {
        return settingsMenu;
    }

    protected abstract void onSettingsChanged();

}
