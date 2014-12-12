package com.imove.voipdemo.dummy;


/**
 * A dummy item representing a piece of content.
 */
public  class FriendItem {
    public String id;
    public String name;

    public FriendItem(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}