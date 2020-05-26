package com.trashsoftware.wuziqi.programs;

public class RulesSet {

    static final int OVERLINES_NONE = 0;
    static final int OVERLINES_LOST = 1;
    public static final int OVERLINES_WINNING = 2;

    public static final int PVE = 1;
    public static final int PVP = 2;
    public static final int EVE = 3;

    /**
     * Unlimited undo steps.
     * <p>
     * Any number greater than size of the chessboard has equivalent effect.
     */
    public static final int UNLIMITED_UNDO = 10000;

    private int overlinesRule;
    private boolean aiFirst;
    private int gameMode;
    private int difficultyLevel;
    private int[] eveAiLevels;
    private int undoStepsCount = 3;  // undo steps, 0 for no undo, large number for unlimited

    private RulesSet() {

    }

    int getGameMode() {
        return gameMode;
    }

    int getOverlinesRule() {
        return overlinesRule;
    }

    public boolean isPveAiFirst() {
        return aiFirst;
    }

    public int getPveDifficultyLevel() {
        return difficultyLevel;
    }

    public int[] getEveAiLevels() {
        return eveAiLevels;
    }

    boolean isPve() {
        return gameMode == PVE;
    }

    int getUndoStepsCount() {
        return isPve() ? undoStepsCount : Math.min(1, undoStepsCount);
    }

    public static class RulesSetBuilder {
        private RulesSet rulesSet = new RulesSet();

        public RulesSetBuilder pveAiFirst(boolean aiFirst) {
            rulesSet.aiFirst = aiFirst;
            return this;
        }

        public RulesSetBuilder overlinesRule(int overlinesRule) {
            rulesSet.overlinesRule = overlinesRule;
            return this;
        }

        public RulesSetBuilder pveDifficulty(int difficultLevel) {
            rulesSet.difficultyLevel = difficultLevel;
            return this;
        }

        public RulesSetBuilder eveAiLevels(int[] aiLevels) {
            rulesSet.eveAiLevels = aiLevels;
            return this;
        }

        public RulesSetBuilder undoStepsCount(int undoStepsCount) {
            rulesSet.undoStepsCount = undoStepsCount;
            return this;
        }

        public RulesSetBuilder gameMode(int gameMode) {
            rulesSet.gameMode = gameMode;
            return this;
        }

        public RulesSet build() {
            return rulesSet;
        }
    }
}
