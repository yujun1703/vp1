package com.imove.voipdemo.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class FriendContent {
    /**
     * An array of sample (dummy) items.
     */
    public  List<FriendItem> ITEMS;
    /**
     * A map of sample (dummy) items, by ID.
     */
    public  Map<String, FriendItem> ITEM_MAP;

    public FriendContent()
    {
        ITEMS = new ArrayList<FriendItem>();
        ITEM_MAP = new HashMap<String, FriendItem>();
    }

    public void addItem(FriendItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    public List<FriendItem> getItem()
    {
        return ITEMS;
    }

}
