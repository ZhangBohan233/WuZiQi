package com.trashsoftware.wuziqi.graphics;

import com.trashsoftware.wuziqi.programs.Player;

public interface GuiInterface {

    /**
     * Shows the winning dialog
     *
     * @param playerNum     which player wins, 1 or 2
     * @param winningPlayer the {@code Player} instance who wins
     */
    void showWin(int playerNum, Player winningPlayer);

    /**
     * Shows the tie dialog
     */
    void showTie();

    /**
     * Refreshes the chessboard view
     */
    void refreshView();

    /**
     * Sets the current playing player.
     *
     * @param isPlayer1 {@code true} iff player1 (black) is going to play
     */
    void setActivePlayer(boolean isPlayer1);

    /**
     * Sets the enabled status for undo buttons.
     *
     * @param p1Enable whether the undo button of p1 should be enabled
     * @param p2Enable whether the undo button of p2 should be enabled
     */
    void updateUndoStatus(boolean p1Enable, boolean p2Enable);

    /**
     * Sets the enabled status for draw buttons.
     *
     * @param p1Enable whether the draw button of p1 should be enabled
     * @param p2Enable whether the draw button of p2 should be enabled
     */
    void updateDrawStatus(boolean p1Enable, boolean p2Enable);

    void runOnUiThread(Runnable runnable);

    void runOnBackground(Runnable runnable);

    void currentPlayerAskDraw();

    void showToastMsg(int... resIds);
}
