package com.trashsoftware.wuziqi.programs;

public class RulesSet {

    static final int OVERLINES_NONE = 0;
    static final int OVERLINES_LOST = 1;
    public static final int OVERLINES_WINNING = 2;

    /**
     * Unlimited undo steps.
     * <p>
     * Any number greater than size of the chessboard has equivalent effect.
     */
    public static final int UNLIMITED_UNDO = 10000;

    private int overlinesRule;
    private boolean aiFirst;
    private boolean pve;
    private int difficultyLevel;
    private int undoStepsCount = 3;  // undo steps, 0 for no undo, large number for unlimited

    public RulesSet(int overlinesRule) {
        this.overlinesRule = overlinesRule;
    }

    public RulesSet(int overlinesRule, boolean aiFirst, int difficultyLevel) {
        this.overlinesRule = overlinesRule;
        this.aiFirst = aiFirst;
        this.difficultyLevel = difficultyLevel;
        this.pve = true;
    }

    private RulesSet() {

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

    int getUndoStepsCount() {
        return pve ? undoStepsCount : Math.min(1, undoStepsCount);
    }

    public static class RulesSetBuilder {
        private RulesSet rulesSet = new RulesSet();

        public RulesSetBuilder pve(boolean isPve) {
            rulesSet.pve = isPve;
            return this;
        }

        public RulesSetBuilder aiFirst(boolean aiFirst) {
            rulesSet.aiFirst = aiFirst;
            return this;
        }

        public RulesSetBuilder overlinesRule(int overlinesRule) {
            rulesSet.overlinesRule = overlinesRule;
            return this;
        }

        public RulesSetBuilder difficultyLevel(int difficultLevel) {
            rulesSet.difficultyLevel = difficultLevel;
            return this;
        }

        public RulesSetBuilder undoStepsCount(int undoStepsCount) {
            rulesSet.undoStepsCount = undoStepsCount;
            return this;
        }

        public RulesSet build() {
            return rulesSet;
        }
    }
}
