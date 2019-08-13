package com.trashsoftware.wuziqi.programs;

public class RulesSet {

    static final int OVERLINES_NONE = 0;
    static final int OVERLINES_LOST = 1;
    public static final int OVERLINES_WINNING = 2;

    private int overlinesRule;
    private boolean aiFirst;
    private boolean pve;
    private int difficultyLevel;

    public RulesSet(int overlinesRule) {
        this.overlinesRule = overlinesRule;
    }

    public RulesSet(int overlinesRule, boolean aiFirst, int difficultyLevel) {
        this.overlinesRule = overlinesRule;
        this.aiFirst = aiFirst;
        this.difficultyLevel = difficultyLevel;
        this.pve = true;
    }

    int getOverlinesRule() {
        return overlinesRule;
    }

    public boolean isAiFirst() {
        return aiFirst;
    }

    int getDifficultyLevel() {
        return difficultyLevel;
    }

    boolean isPve() {
        return pve;
    }
}
