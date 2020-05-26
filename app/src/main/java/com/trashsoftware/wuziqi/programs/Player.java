package com.trashsoftware.wuziqi.programs;

public abstract class Player {

    private String name;

    Player(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract boolean isAi();
}
