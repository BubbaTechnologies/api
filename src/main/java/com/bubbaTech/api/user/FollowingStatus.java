package com.bubbaTech.api.user;

public enum FollowingStatus {
    FOLLOWING(2),
    REQUESTED(1),
    NONE(0);

    private final int intValue;

    FollowingStatus(int value) {
        this.intValue = value;
    }

    public int getIntValue() {
        return intValue;
    }

}
