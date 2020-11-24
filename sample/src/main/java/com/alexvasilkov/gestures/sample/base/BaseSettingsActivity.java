package com.alexvasilkov.gestures.sample.base;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.alexvasilkov.gestures.Settings;
import com.alexvasilkov.gestures.sample.base.settings.SettingsController;
import com.alexvasilkov.gestures.sample.base.settings.SettingsMenu;

public abstract class BaseSettingsActivity extends BaseActivity {

    private final SettingsMenu settingsMenu = new SettingsMenu();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsMenu.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getSupportActionBarNotNull().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        settingsMenu.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        settingsMenu.onCreateOptionsMenu(menu);
        return true;
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

    protected SettingsController getSettingsController() {
        return settingsMenu;
    }

    protected abstract void onSettingsChanged();

    protected void setDefaultSettings(Settings settings) {
        settingsMenu.setValuesFrom(settings);
        invalidateOptionsMenu();
    }

}
