package com.bubbaTech.api.user;

import com.fasterxml.jackson.annotation.JsonValue;

public enum FollowingStatus {
    FOLLOWING(2),
    REQUESTED(1),
    NONE(0);

    private final int intValue;

    FollowingStatus(int value) {
        this.intValue = value;
    }

    @JsonValue
    public int getIntValue() {
        return intValue;
    }

}
