package com.alexvasilkov.gestures.sample.ex.layout.pager;

import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alexvasilkov.android.commons.nav.Navigate;
import com.alexvasilkov.android.commons.texts.SpannableBuilder;
import com.alexvasilkov.android.commons.ui.Views;
import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.base.settings.SettingsController;
import com.alexvasilkov.gestures.sample.ex.utils.GlideHelper;
import com.alexvasilkov.gestures.sample.ex.utils.Painting;
import com.alexvasilkov.gestures.views.GestureFrameLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

class LayoutsPagerAdapter extends RecyclePagerAdapter<LayoutsPagerAdapter.ViewHolder> {

    private final ViewPager viewPager;
    private final Painting[] paintings;
    private final SettingsController settingsController;

    LayoutsPagerAdapter(ViewPager pager, Painting[] paintings, SettingsController listener) {
        this.viewPager = pager;
        this.paintings = paintings;
        this.settingsController = listener;
    }

    @Override
    public int getCount() {
        return paintings.length;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup container) {
        final ViewHolder holder = new ViewHolder(container);
        holder.layout.getController().enableScrollInViewPager(viewPager);
        holder.button.setOnClickListener(this::onButtonClick);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        settingsController.apply(holder.layout);

        Painting painting = paintings[position];
        GlideHelper.loadFull(holder.image, painting.imageId, painting.thumbId);

        final CharSequence titleText = new SpannableBuilder(holder.itemView.getContext())
                .createStyle().setFont(Typeface.DEFAULT_BOLD).apply()
                .append(paintings[position].author).append("\n")
                .clearStyle()
                .append(paintings[position].title)
                .build();
        holder.title.setText(titleText);

        holder.button.setTag(paintings[position].link);
    }

    @Override
    public void onRecycleViewHolder(@NonNull ViewHolder holder) {
        // Resetting to initial image state
        holder.layout.getController().resetState();
        GlideHelper.clear(holder.image);
    }

    private void onButtonClick(@NonNull View button) {
        final String url = (String) button.getTag();
        Navigate.from(button.getContext()).external().browser().url(url).start();
    }


    static class ViewHolder extends RecyclePagerAdapter.ViewHolder {
        final GestureFrameLayout layout;
        final ImageView image;
        final TextView title;
        final View button;

        ViewHolder(ViewGroup container) {
            super(Views.inflate(container, R.layout.layout_pager_item));
            layout = itemView.findViewById(R.id.frame_item_layout);
            image = layout.findViewById(R.id.frame_item_image);
            title = layout.findViewById(R.id.frame_item_title);
            button = layout.findViewById(R.id.frame_item_button);
        }
    }

}
