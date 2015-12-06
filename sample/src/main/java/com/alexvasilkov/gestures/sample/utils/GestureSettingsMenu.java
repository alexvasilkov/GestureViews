package com.alexvasilkov.gestures.sample.utils;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import com.alexvasilkov.android.commons.state.InstanceState;
import com.alexvasilkov.android.commons.state.InstanceStateManager;
import com.alexvasilkov.gestures.Settings;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.views.interfaces.GestureView;

public class GestureSettingsMenu implements GestureSettingsSetupListener {

    @InstanceState
    private boolean mIsPanEnabled = true;
    @InstanceState
    private boolean mIsZoomEnabled = true;
    @InstanceState
    private boolean mIsRotationEnabled = false;
    @InstanceState
    private boolean mIsOverscrollXEnabled = false;
    @InstanceState
    private boolean mIsOverscrollYEnabled = false;
    @InstanceState
    private boolean mIsOverzoomEnabled = true;
    @InstanceState
    private boolean mIsFitViewport = true;
    @InstanceState
    private Settings.Fit mFitMethod = Settings.Fit.INSIDE;
    @InstanceState
    private int mGravity = Gravity.CENTER;

    public void onSaveInstanceState(Bundle outState) {
        InstanceStateManager.saveInstanceState(this, outState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        InstanceStateManager.restoreInstanceState(this, savedInstanceState);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        addBoolMenu(menu, mIsPanEnabled, R.string.menu_enable_pan);
        addBoolMenu(menu, mIsZoomEnabled, R.string.menu_enable_zoom);
        addBoolMenu(menu, mIsRotationEnabled, R.string.menu_enable_rotation);
        addBoolMenu(menu, mIsOverscrollXEnabled, R.string.menu_enable_overscroll_x);
        addBoolMenu(menu, mIsOverscrollYEnabled, R.string.menu_enable_overscroll_y);
        addBoolMenu(menu, mIsOverzoomEnabled, R.string.menu_enable_overzoom);
        addBoolMenu(menu, mIsFitViewport, R.string.menu_fit_viewport);
        addSubMenu(menu, Settings.Fit.values(), mFitMethod, R.string.menu_fit_method);
        addSubMenu(menu, GravityType.values(), GravityType.find(mGravity), R.string.menu_gravity);
        return true;
    }

    private void addBoolMenu(Menu menu, boolean checked, @StringRes int titleId) {
        MenuItem item = menu.add(Menu.NONE, titleId, 0, titleId);
        item.setCheckable(true);
        item.setChecked(checked);
    }

    private <T> void addSubMenu(Menu menu, T[] items, T selected, @StringRes int titleId) {
        SubMenu sub = menu.addSubMenu(titleId);
        sub.setGroupCheckable(Menu.NONE, true, true);

        for (int i = 0; i < items.length; i++) {
            MenuItem item = sub.add(Menu.NONE, titleId, i, items[i].toString());
            item.setCheckable(true);
            item.setChecked(items[i] == selected);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.string.menu_enable_pan:
                mIsPanEnabled = !mIsPanEnabled;
                break;
            case R.string.menu_enable_zoom:
                mIsZoomEnabled = !mIsZoomEnabled;
                break;
            case R.string.menu_enable_rotation:
                mIsRotationEnabled = !mIsRotationEnabled;
                break;
            case R.string.menu_enable_overscroll_x:
                mIsOverscrollXEnabled = !mIsOverscrollXEnabled;
                break;
            case R.string.menu_enable_overscroll_y:
                mIsOverscrollYEnabled = !mIsOverscrollYEnabled;
                break;
            case R.string.menu_enable_overzoom:
                mIsOverzoomEnabled = !mIsOverzoomEnabled;
                break;
            case R.string.menu_fit_viewport:
                mIsFitViewport = !mIsFitViewport;
                break;
            case R.string.menu_fit_method:
                mFitMethod = Settings.Fit.values()[item.getOrder()];
                break;
            case R.string.menu_gravity:
                mGravity = GravityType.values()[item.getOrder()].gravity;
                break;
            default:
                return false;
        }

        return true;
    }

    @Override
    public void onSetupGestureView(GestureView view) {
        Context context = ((View) view).getContext();
        float overscrollX = mIsOverscrollXEnabled ? 32f : 0f;
        float overscrollY = mIsOverscrollYEnabled ? 32f : 0f;
        float overzoom = mIsOverzoomEnabled ? Settings.OVERZOOM_FACTOR : 1f;

        view.getController().getSettings()
                .setPanEnabled(mIsPanEnabled)
                .setZoomEnabled(mIsZoomEnabled)
                .setDoubleTapEnabled(mIsZoomEnabled)
                .setOverscrollDistance(context, overscrollX, overscrollY)
                .setOverzoomFactor(overzoom)
                .setRotationEnabled(mIsRotationEnabled)
                .setFillViewport(mIsFitViewport)
                .setFitMethod(mFitMethod)
                .setGravity(mGravity);
    }

    private enum GravityType {
        CENTER(Gravity.CENTER),
        TOP(Gravity.TOP),
        BOTTOM(Gravity.BOTTOM),
        START(Gravity.START),
        END(Gravity.END),
        TOP_START(Gravity.TOP | Gravity.START),
        BOTTOM_END(Gravity.BOTTOM | Gravity.END);

        public final int gravity;

        GravityType(int gravity) {
            this.gravity = gravity;
        }

        public static GravityType find(int gravity) {
            for (GravityType type : values()) {
                if (type.gravity == gravity) return type;
            }
            return null;
        }
    }

}
