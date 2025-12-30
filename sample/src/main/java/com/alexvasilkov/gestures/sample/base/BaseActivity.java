package com.alexvasilkov.gestures.sample.base;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alexvasilkov.android.commons.state.InstanceStateManager;
import com.alexvasilkov.android.commons.ui.Views;
import com.alexvasilkov.events.Events;
import com.alexvasilkov.gestures.sample.R;
import com.google.android.material.color.MaterialColors;

import java.util.Objects;

public abstract class BaseActivity extends AppCompatActivity {

    protected boolean edgeToEdge = false;

    private int infoTextId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InstanceStateManager.restoreInstanceState(this, savedInstanceState);

        if (!edgeToEdge) {
            View rootView = findViewById(android.R.id.content);
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                return WindowInsetsCompat.CONSUMED;
            });
        }

        OnBackPressedCallback internalBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!onBackPressedInternal()) {
                    setEnabled(false); // Temporarily disable to avoid recursion
                    getOnBackPressedDispatcher().onBackPressed();
                    setEnabled(true);
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, internalBackPressedCallback);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Events.register(this);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        InstanceStateManager.saveInstanceState(this, outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Events.unregister(this);
    }

    /**
     * @return true if the child handled the back press.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean onBackPressedInternal() {
        return false;
    }

    protected void sendBackPress() {
        getOnBackPressedDispatcher().onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (infoTextId != 0) {
            final Context context = getSupportActionBarNotNull().getThemedContext();

            MenuItem item = menu.add(Menu.NONE, R.id.menu_info, Menu.NONE, R.string.menu_info);
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

            Drawable ic = ContextCompat.getDrawable(context, R.drawable.ic_info_outline_white_24dp);
            Objects.requireNonNull(ic);
            int colorId = com.google.android.material.R.attr.colorOnSurface;
            ic.setTint(MaterialColors.getColor(context, colorId, "Error"));
            item.setIcon(ic);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            sendBackPress();
            return true;
        } else if (itemId == R.id.menu_info) {
            showInfoDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    protected ActionBar getSupportActionBarNotNull() {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            throw new NullPointerException("Action bar was not initialized");
        }
        return actionBar;
    }

    protected void setInfoText(@StringRes int textId) {
        infoTextId = textId;
        invalidateOptionsMenu();
    }

    private void showInfoDialog() {
        final View layout = Views.inflate(this, R.layout.info_dialog);
        final TextView text = layout.findViewById(R.id.info_text);
        text.setText(getText(infoTextId));

        new AlertDialog.Builder(this)
                .setView(layout)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

}
