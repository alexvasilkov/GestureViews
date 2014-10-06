package com.alexvasilkov.gestures.sample.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.alexvasilkov.android.commons.utils.Views;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.widgets.GestureImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.googlecode.flickrjandroid.photos.Photo;

public class FlickrImageActivity extends BaseActivity {

    private static final String EXTRA_PHOTO = "photo";

    public static void start(Context context, Photo photo) {
        Intent intent = new Intent(context, FlickrImageActivity.class);
        intent.putExtra(EXTRA_PHOTO, photo);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flickr_image);

        final GestureImageView imageView = Views.find(this, R.id.flickr_image);

        imageView.getController().getSettings()
                .setFillViewport(true)
                .setMaxZoom(3f);

        // Disabling touch controls
        imageView.getController().getSettings()
                .setPanEnabled(false)
                .setZoomEnabled(false)
                .setDoubleTapEnabled(false);

        final Photo photo = (Photo) getIntent().getSerializableExtra(EXTRA_PHOTO);
        final String photoUrl = photo.getLargeSize() == null
                ? photo.getMediumUrl() : photo.getLargeUrl();

        Glide.with(this)
                .load(photoUrl)
                .thumbnail(Glide.with(this)
                        .load(photo.getThumbnailUrl())
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE))
                .dontTransform()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String url,
                                               Target<GlideDrawable> target,
                                               boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String url,
                                                   Target<GlideDrawable> target,
                                                   boolean isFromMemoryCache,
                                                   boolean isFirstResource) {
                        if (url.equals(photoUrl)) {
                            // Enabling touch controls
                            imageView.getController().getSettings()
                                    .setPanEnabled(true)
                                    .setZoomEnabled(true)
                                    .setDoubleTapEnabled(true);
                        }
                        return false;
                    }
                })
                .into(imageView);
    }

}
