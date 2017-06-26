package com.alexvasilkov.gestures.sample.ui.ex8;

import com.alexvasilkov.gestures.sample.R;

import java.util.ArrayList;
import java.util.List;

class Item {

    final String text;
    final int[] images;

    private Item(String text) {
        this.text = text;
        this.images = null;
    }

    private Item(int... images) {
        this.text = null;
        this.images = images;
    }


    static List<Item> createItemsList() {
        final List<Item> items = new ArrayList<>();

        items.add(new Item(R.drawable.painting_01));
        items.add(new Item("Nunc non ipsum luctus, scelerisque risus interdum, vulputate neque."));
        items.add(new Item(R.drawable.painting_02, R.drawable.painting_03, R.drawable.painting_04,
                R.drawable.painting_01, R.drawable.painting_05));
        items.add(new Item("In hac habitasse platea dictumst. Quisque arcu sem, porta ut semper."));
        items.add(new Item(R.drawable.painting_05));
        items.add(new Item("Cras in arcu maximus, mattis quam sed, tempor neque. Duis sapien."));
        items.add(new Item("Sed efficitur efficitur justo, id sollicitudin sem tristique at."));

        return items;
    }

}
