package com.nhnacademy.front.core.dto.team;

public enum TeamRole {
    OWNER,
    ADMIN,
    NORMAL;

    public boolean isManager() {
        return this == OWNER || this == ADMIN;
    }

    public boolean isOwner() {
        return this == OWNER;
    }
}
