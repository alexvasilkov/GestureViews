package com.alexvasilkov.gestures.sample.base.settings;

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
import com.alexvasilkov.gestures.internal.GestureDebug;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.views.interfaces.GestureView;

public class SettingsMenu implements SettingsController {

    private static final float OVERSCROLL = 32f;
    private static final long SLOW_ANIMATIONS = 1500L;

    @InstanceState
    private boolean isPanEnabled = true;
    @InstanceState
    private boolean isZoomEnabled = true;
    @InstanceState
    private boolean isRotationEnabled = false;
    @InstanceState
    private boolean isRestrictRotation = false;
    @InstanceState
    private boolean isOverscrollEnabled = false;
    @InstanceState
    private boolean isOverzoomEnabled = true;
    @InstanceState
    private boolean isFillViewport = true;
    @InstanceState
    private Settings.Fit fitMethod = Settings.Fit.INSIDE;
    @InstanceState
    private Settings.Bounds boundsType = Settings.Bounds.NORMAL;
    @InstanceState
    private int gravity = Gravity.CENTER;
    @InstanceState
    private boolean isSlow = false;

    public void setValuesFrom(Settings settings) {
        isPanEnabled = settings.isPanEnabled();
        isZoomEnabled = settings.isZoomEnabled();
        isRotationEnabled = settings.isRotationEnabled();
        isRestrictRotation = settings.isRestrictRotation();
        isFillViewport = settings.isFillViewport();
        fitMethod = settings.getFitMethod();
        boundsType = settings.getBoundsType();
        gravity = settings.getGravity();
    }

    public void onSaveInstanceState(Bundle outState) {
        InstanceStateManager.saveInstanceState(this, outState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        InstanceStateManager.restoreInstanceState(this, savedInstanceState);
    }

    public void onCreateOptionsMenu(Menu menu) {
        addBoolMenu(menu, isPanEnabled, R.string.menu_enable_pan);
        addBoolMenu(menu, isZoomEnabled, R.string.menu_enable_zoom);
        addBoolMenu(menu, isRotationEnabled, R.string.menu_enable_rotation);
        addBoolMenu(menu, isRestrictRotation, R.string.menu_restrict_rotation);
        addBoolMenu(menu, isOverscrollEnabled, R.string.menu_enable_overscroll);
        addBoolMenu(menu, isOverzoomEnabled, R.string.menu_enable_overzoom);
        addBoolMenu(menu, isFillViewport, R.string.menu_fill_viewport);
        addSubMenu(menu, Settings.Fit.values(), fitMethod, R.string.menu_fit_method);
        addSubMenu(menu, Settings.Bounds.values(), boundsType, R.string.menu_bounds_type);
        addSubMenu(menu, GravityType.values(), GravityType.find(gravity), R.string.menu_gravity);
        addBoolMenu(menu, isSlow, R.string.menu_enable_slow);
        addBoolMenu(menu, GestureDebug.isDrawDebugOverlay(), R.string.menu_enable_overlay);
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
                isPanEnabled = !isPanEnabled;
                break;
            case R.string.menu_enable_zoom:
                isZoomEnabled = !isZoomEnabled;
                break;
            case R.string.menu_enable_rotation:
                isRotationEnabled = !isRotationEnabled;
                break;
            case R.string.menu_restrict_rotation:
                isRestrictRotation = !isRestrictRotation;
                break;
            case R.string.menu_enable_overscroll:
                isOverscrollEnabled = !isOverscrollEnabled;
                break;
            case R.string.menu_enable_overzoom:
                isOverzoomEnabled = !isOverzoomEnabled;
                break;
            case R.string.menu_fill_viewport:
                isFillViewport = !isFillViewport;
                break;
            case R.string.menu_fit_method:
                fitMethod = Settings.Fit.values()[item.getOrder()];
                break;
            case R.string.menu_bounds_type:
                boundsType = Settings.Bounds.values()[item.getOrder()];
                break;
            case R.string.menu_gravity:
                gravity = GravityType.values()[item.getOrder()].gravity;
                break;
            case R.string.menu_enable_slow:
                isSlow = !isSlow;
                break;
            case R.string.menu_enable_overlay:
                GestureDebug.setDrawDebugOverlay(!GestureDebug.isDrawDebugOverlay());
                break;
            default:
                return false;
        }

        return true;
    }

    @Override
    public void apply(GestureView view) {
        Context context = ((View) view).getContext();
        float overscroll = isOverscrollEnabled ? OVERSCROLL : 0f;
        float overzoom = isOverzoomEnabled ? Settings.OVERZOOM_FACTOR : 1f;

        view.getController().getSettings()
                .setPanEnabled(isPanEnabled)
                .setZoomEnabled(isZoomEnabled)
                .setDoubleTapEnabled(isZoomEnabled)
                .setRotationEnabled(isRotationEnabled)
                .setRestrictRotation(isRestrictRotation)
                .setOverscrollDistance(context, overscroll, overscroll)
                .setOverzoomFactor(overzoom)
                .setFillViewport(isFillViewport)
                .setFitMethod(fitMethod)
                .setBoundsType(boundsType)
                .setGravity(gravity)
                .setAnimationsDuration(isSlow ? SLOW_ANIMATIONS : Settings.ANIMATIONS_DURATION);
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
                if (type.gravity == gravity) {
                    return type;
                }
            }
            return null;
        }
    }

}
