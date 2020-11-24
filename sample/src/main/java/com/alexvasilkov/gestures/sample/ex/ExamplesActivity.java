package com.alexvasilkov.gestures.sample.ex;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alexvasilkov.android.commons.ui.Views;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.base.BaseActivity;
import com.alexvasilkov.gestures.sample.ex.animations.ImageAnimationActivity;
import com.alexvasilkov.gestures.sample.ex.animations.RoundImageAnimationActivity;
import com.alexvasilkov.gestures.sample.ex.animations.cross.ImageCrossAnimationActivity;
import com.alexvasilkov.gestures.sample.ex.image.control.ImageControlActivity;
import com.alexvasilkov.gestures.sample.ex.image.crop.ImageCropActivity;
import com.alexvasilkov.gestures.sample.ex.image.pager.ViewPagerActivity;
import com.alexvasilkov.gestures.sample.ex.image.viewer.ImageViewerActivity;
import com.alexvasilkov.gestures.sample.ex.layout.pager.LayoutsInPagerActivity;
import com.alexvasilkov.gestures.sample.ex.layout.viewer.LayoutViewerActivity;
import com.alexvasilkov.gestures.sample.ex.other.markers.ImageMarkersActivity;
import com.alexvasilkov.gestures.sample.ex.other.scene.SceneActivity;
import com.alexvasilkov.gestures.sample.ex.other.text.CustomViewActivity;
import com.alexvasilkov.gestures.sample.ex.transitions.complex.ListAnyToAllActivity;
import com.alexvasilkov.gestures.sample.ex.transitions.complex.ListAnyToAnyActivity;
import com.alexvasilkov.gestures.sample.ex.transitions.recycler.RecyclerToPagerActivity;
import com.alexvasilkov.gestures.sample.ex.utils.Painting;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ExamplesActivity extends BaseActivity {

    private Painting[] paintings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.examples_screen);
        getSupportActionBarNotNull().setDisplayHomeAsUpEnabled(true);

        RecyclerView recyclerView = findViewById(R.id.main_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ExamplesAdapter(getExamplesList()));

        paintings = Painting.list(getResources());
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Warming up thumbnails cache for smoother experience
        for (Painting painting : paintings) {
            Glide.with(this).load(painting.thumbId).preload();
        }
    }

    private static List<?> getExamplesList() {
        List<Object> items = new ArrayList<>();

        items.add(new ExampleGroup(R.string.example_group_image));

        items.add(new Example(
                ImageViewerActivity.class,
                R.string.example_image_viewer,
                R.drawable.ic_ex_image));

        items.add(new Example(
                ImageControlActivity.class,
                R.string.example_image_state_control,
                R.drawable.ic_ex_state_control));

        items.add(new Example(
                ImageCropActivity.class,
                R.string.example_image_cropping,
                R.drawable.ic_ex_crop));

        items.add(new Example(
                ViewPagerActivity.class,
                R.string.example_images_in_pager,
                R.drawable.ic_ex_pager));

        items.add(new ExampleGroup(R.string.example_group_image_animations));

        items.add(new Example(
                ImageAnimationActivity.class,
                R.string.example_image_animation,
                R.drawable.ic_ex_image_animation));

        items.add(new Example(
                ImageCrossAnimationActivity.class,
                R.string.example_image_animation_cross,
                R.drawable.ic_ex_image_animation));

        items.add(new Example(
                RoundImageAnimationActivity.class,
                R.string.example_image_animation_circular,
                R.drawable.ic_ex_image_animation));

        items.add(new ExampleGroup(R.string.example_group_list_transitions));

        items.add(new Example(
                RecyclerToPagerActivity.class,
                R.string.example_list_transitions_1_N,
                R.drawable.ic_ex_list));

        items.add(new Example(
                ListAnyToAllActivity.class,
                R.string.example_list_transitions_n_N,
                R.drawable.ic_ex_complex_list));

        items.add(new Example(
                ListAnyToAnyActivity.class,
                R.string.example_list_transitions_n_n,
                R.drawable.ic_ex_complex_list));

        items.add(new ExampleGroup(R.string.example_group_layout));

        items.add(new Example(
                LayoutViewerActivity.class,
                R.string.example_layout_viewer,
                R.drawable.ic_ex_layout));

        items.add(new Example(
                LayoutsInPagerActivity.class,
                R.string.example_layouts_in_pager,
                R.drawable.ic_ex_pager));

        items.add(new ExampleGroup(R.string.example_group_other));

        items.add(new Example(
                CustomViewActivity.class,
                R.string.example_other_custom,
                R.drawable.ic_ex_custom_text));

        items.add(new Example(
                ImageMarkersActivity.class,
                R.string.example_other_markers,
                R.drawable.ic_ex_markers));

        items.add(new Example(
                SceneActivity.class,
                R.string.example_other_objects,
                R.drawable.ic_ex_objects_control));

        return items;
    }


    private class ExamplesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int viewTypeHeader = 0;
        private static final int viewTypeExample = 1;

        private final List<?> list;

        ExamplesAdapter(List<?> list) {
            this.list = list;
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        @Override
        public int getItemViewType(int position) {
            Object item = list.get(position);
            if (item instanceof Example) {
                return viewTypeExample;
            } else if (item instanceof ExampleGroup) {
                return viewTypeHeader;
            } else {
                throw new IllegalArgumentException("Unknown item");
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == viewTypeExample) {
                return new ExampleHolder(parent);
            } else if (viewType == viewTypeHeader) {
                return new HeaderHolder(parent);
            } else {
                throw new IllegalArgumentException("Unknown view type");
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ExampleHolder) {
                ((ExampleHolder) holder).bind((Example) list.get(position));
            } else if (holder instanceof HeaderHolder) {
                ((HeaderHolder) holder).bind((ExampleGroup) list.get(position));
            }
        }


        class ExampleHolder extends RecyclerView.ViewHolder {
            private final TextView text;
            private Example info;

            ExampleHolder(ViewGroup parent) {
                super(Views.inflate(parent, R.layout.examples_list_item));
                text = (TextView) itemView;
                itemView.setOnClickListener(
                        view -> startActivity(new Intent(ExamplesActivity.this, info.screen)));
            }

            void bind(Example item) {
                this.info = item;
                text.setText(item.titleId);
                text.setCompoundDrawablesWithIntrinsicBounds(item.iconId, 0, 0, 0);
            }
        }

        class HeaderHolder extends RecyclerView.ViewHolder {
            private final TextView text;

            HeaderHolder(ViewGroup parent) {
                super(Views.inflate(parent, R.layout.examples_list_header));
                text = (TextView) itemView;
            }

            void bind(ExampleGroup item) {
                text.setText(item.titleId);
            }
        }
    }


    private static class Example {
        final Class<? extends Activity> screen;
        final int titleId;
        final int iconId;

        Example(Class<? extends Activity> screen, int titleId, int iconId) {
            this.screen = screen;
            this.titleId = titleId;
            this.iconId = iconId;
        }
    }

    private static class ExampleGroup {
        final int titleId;

        ExampleGroup(int titleId) {
            this.titleId = titleId;
        }
    }

}
