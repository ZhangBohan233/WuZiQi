package com.trashsoftware.wuziqi.programs;

public class Human extends Player {

    public Human(String name) {
        super(name);
    }

    @Override
    public boolean isAi() {
        return false;
    }
}
