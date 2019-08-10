package com.trashsoftware.wuziqi.programs;

public class RulesSet {

    public final static int OVERLINES_NONE = 0;
    public final static int OVERLINES_LOST = 1;
    public final static int OVERLINES_WINNING = 2;

    private int overlinesRule;
    private boolean aiFirst;

    public RulesSet(int overlinesRule, boolean aiFirst) {
        this.overlinesRule = overlinesRule;
        this.aiFirst = aiFirst;
    }

    public int getOverlinesRule() {
        return overlinesRule;
    }

    public boolean isAiFirst() {
        return aiFirst;
    }
}
