package com.alexvasilkov.gestures.sample.ui.ex8;

import android.content.Context;

import com.alexvasilkov.gestures.sample.ui.ex.Painting;

import java.util.ArrayList;
import java.util.List;

class Item {

    final String text;
    final Painting[] paintings;

    private Item(String text) {
        this.text = text;
        this.paintings = null;
    }

    private Item(Painting... paintings) {
        this.text = null;
        this.paintings = paintings;
    }


    static List<Item> createItemsList(Context context) {
        final List<Item> items = new ArrayList<>();
        final Painting[] paintings = Painting.list(context.getResources());

        items.add(new Item(paintings[0]));
        items.add(new Item("Nunc non ipsum luctus, scelerisque risus interdum, vulputate neque."));
        items.add(new Item(paintings[1], paintings[2], paintings[3], paintings[4], paintings[0]));
        items.add(new Item("In hac habitasse platea dictumst. Quisque arcu sem, porta ut semper."));
        items.add(new Item(paintings[4]));
        items.add(new Item("Cras in arcu maximus, mattis quam sed, tempor neque. Duis sapien."));
        items.add(new Item("Sed efficitur efficitur justo, id sollicitudin sem tristique at."));

        return items;
    }

}
