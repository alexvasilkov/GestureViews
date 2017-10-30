package com.alexvasilkov.gestures.sample.ex.list.complex;

import android.content.Context;

import com.alexvasilkov.gestures.sample.ex.utils.Painting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ListItem {

    final String text;
    final List<Painting> paintings;

    private ListItem(String text) {
        this.text = text;
        this.paintings = null;
    }

    private ListItem(Painting... paintings) {
        this.text = null;
        this.paintings = Arrays.asList(paintings);
    }


    /*******************
     * Mock data
     *******************/

    static List<ListItem> createItemsV1(Context context) {
        final List<ListItem> items = new ArrayList<>();
        final Painting[] paintings = Painting.list(context.getResources());

        items.add(new ListItem("Donec sem arcu, feugiat sit amet purus ut, cursus tristique justo."
                + " Quisque eu ligula sed massa tristique elementum eu vel augue."));
        items.add(new ListItem(paintings[0], paintings[1]));
        items.add(new ListItem(paintings[2]));
        items.add(new ListItem("Vivamus orci nulla, euismod ac purus id, porttitor vulputate purus."
                + " Proin at aliquam justo. Integer eget eros vitae metus ornare lacinia eu sit"
                + " amet justo. Integer aliquam sit amet diam ac laoreet. Cras tincidunt dolor ut"
                + " nisl aliquet."));
        items.add(new ListItem("Nulla sed eleifend quam"));
        items.add(new ListItem(paintings[3], paintings[4]));
        items.add(new ListItem("Suspendisse dignissim pretium nisi nec tincidunt. In ut finibus"
                + " arcu. Nunc pretium purus a eros convallis finibus. Vestibulum eleifend"
                + " efficitur arcu, ut fringilla lorem molestie nec."));
        items.add(new ListItem(paintings[0]));
        items.add(new ListItem(paintings[1], paintings[2]));
        return items;
    }

    static List<ListItem> createItemsV2(Context context) {
        final List<ListItem> items = new ArrayList<>();
        final Painting[] paintings = Painting.list(context.getResources());

        items.add(new ListItem("Donec sem arcu, feugiat sit amet purus ut, cursus tristique justo."
                + " Quisque eu ligula sed massa tristique elementum eu vel augue."));
        items.add(new ListItem(paintings[0], paintings[1], paintings[2], paintings[3]));
        items.add(new ListItem("Vivamus orci nulla, euismod ac purus id, porttitor vulputate purus."
                + " Proin at aliquam justo. Integer eget eros vitae metus ornare lacinia eu sit"
                + " amet justo. Integer aliquam sit amet diam ac laoreet. Cras tincidunt dolor ut"
                + " nisl aliquet."));
        items.add(new ListItem("Nulla sed eleifend quam"));
        items.add(new ListItem(paintings[0], paintings[1], paintings[2]));
        items.add(new ListItem("Suspendisse dignissim pretium nisi nec tincidunt. In ut finibus"
                + " arcu. Nunc pretium purus a eros convallis finibus. Vestibulum eleifend"
                + " efficitur arcu, ut fringilla lorem molestie nec."));
        items.add(new ListItem(paintings[0]));
        items.add(new ListItem(paintings[0], paintings[1]));
        return items;
    }

}
