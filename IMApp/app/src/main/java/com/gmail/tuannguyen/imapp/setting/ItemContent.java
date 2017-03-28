package com.gmail.tuannguyen.imapp.setting;

import com.gmail.tuannguyen.imapp.util.Common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 */
public class ItemContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<Item> ITEMS = new ArrayList<Item>();

    public static final Map<Integer, String> MENU_ITEM_MAP = new HashMap<>();
    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, Item> ITEM_MAP = new HashMap<String, Item>();

    private static final int COUNT = 25;

    static {
        MENU_ITEM_MAP.put(Integer.valueOf(1), Common.ITEM_ACCOUNT_INFORMATION);
        MENU_ITEM_MAP.put(Integer.valueOf(2), Common.ITEM_SECURITY_PRIVACY);
        MENU_ITEM_MAP.put(Integer.valueOf(3), Common.ITEM_HELP);
        MENU_ITEM_MAP.put(Integer.valueOf(4), Common.ITEM_LOG_OUT);
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createItem(i));
        }
    }

    private static void addItem(Item item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static Item createItem(int position) {
        return new Item(String.valueOf(position), MENU_ITEM_MAP.get(Integer.valueOf(position)),
                makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class Item {
        public final String id;
        public final String content;
        public final String details;

        public Item(String id, String content, String details) {
            this.id = id;
            this.content = content;
            this.details = details;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
