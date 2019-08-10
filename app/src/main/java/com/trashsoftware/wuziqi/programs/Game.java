package com.trashsoftware.wuziqi.programs;

import com.trashsoftware.wuziqi.GameActivity;
import com.trashsoftware.wuziqi.graphics.GuiInterface;

public class Game {

    /**
     * The matrix of chess.
     * <p>
     * Integer representations:
     * 0: Empty
     * 1: Player 1's chess
     * 2: Player 2's chess
     */
    private int[][] board = new int[15][15];
    private int lastChessR, lastChessC;
    private int lastChessPlayer = 0;

    private Player player1;
    private Player player2;

    private boolean player1Moving = true;

    private boolean terminated = false;

    private RulesSet rulesSet;

    private GuiInterface parent;

    public Game(Player p1, Player p2, RulesSet rulesSet, GameActivity parent) {
        this.player1 = p1;
        this.player2 = p2;
        this.rulesSet = rulesSet;
        this.parent = parent;

        if (p1.isAi()) {
            aiPlace(p1);
        }
    }

    /**
     * Place a chess in the board, only for human player.
     *
     * @param r row index
     * @param c column index
     */
    public void playerPlace(int r, int c) {
        if (terminated) {
            lastChessPlayer = 0;  // clears the spot of the last chess
            return;
        }
        if (player1Moving && !player1.isAi()) {
            if (innerPlace(r, c)) {
                int wins = checkWinning();
                if (wins == 0) {
                    swapPlayer();
                    if (player2.isAi()) {
                        parent.runOnBackground(new Runnable() {
                            @Override
                            public void run() {
                                aiPlace(player2);
                            }
                        });
                    }
                }
            }
        } else if (!player1Moving && !player2.isAi()) {
            if (innerPlace(r, c)) {
                int wins = checkWinning();
                if (wins == 0) {
                    swapPlayer();
                    if (player1.isAi()) {
                        parent.runOnBackground(new Runnable() {
                            @Override
                            public void run() {
                                aiPlace(player1);
                            }
                        });
                    }
                }
            }
        }
    }

    private void aiPlace(Player player) {
        AI ai = (AI) player;
        ai.aiMove(this, player2.isAi(), rulesSet.getDifficultyLevel());
        parent.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                swapPlayer();
                parent.refreshView();
                checkWinning();
            }
        });
    }

    boolean innerPlace(int r, int c) {
        if (board[r][c] == 0) {
            if (player1Moving) {
                board[r][c] = 1;
                lastChessR = r;
                lastChessC = c;
                lastChessPlayer = 1;
            } else {
                board[r][c] = 2;
                lastChessR = r;
                lastChessC = c;
                lastChessPlayer = 2;
            }
            return true;
        } else {
            return false;
        }
    }

    public int getChessAt(int r, int c) {
        return board[r][c];
    }

    private void swapPlayer() {
        player1Moving = !player1Moving;
        parent.setActivePlayer(player1Moving);
    }

    /**
     * @return 0 if no one wining, 1 if p1 winning, 2 if p2 winning, 3 if tie
     */
    private int checkWinning() {
        int res = traverseCheckWin();
        if (res != 0) {
            if (res == 3) {
                parent.showTie();
            } else {
                Player winningPlayer = res == 1 ? player1 : player2;
                parent.showWin(res, winningPlayer);
            }
            terminated = true;
        }
        return res;
    }

    private int traverseCheckWin() {
        int blanks = 0;
        for (int r = 0; r < 15; r++) {
            for (int c = 0; c < 15; c++) {
                int chess = board[r][c];
                if (chess == 0) {  // Empty point
                    blanks++;
                    continue;
                }
                boolean[] stillConnected = new boolean[]{true, true, true, true};
                for (int x = 1; x < 5; x++) {
                    int increasedR = r + x;
                    int increasedC = c + x;
                    int decreasedC = c - x;

                    if (stillConnected[0] &&
                            !(inBound(r, increasedC) && board[r][increasedC] == chess)) {
                        stillConnected[0] = false;  // to the right
                    }

                    if (stillConnected[1] &&
                            !(inBound(increasedR, increasedC) &&
                                    board[increasedR][increasedC] == chess)) {
                        stillConnected[1] = false;  // to the right-down
                    }

                    if (stillConnected[2] &&
                            !(inBound(increasedR, c) && board[increasedR][c] == chess)) {
                        stillConnected[2] = false;  // downward
                    }

                    if (stillConnected[3] &&
                            !(inBound(increasedR, decreasedC) &&
                                    board[increasedR][decreasedC] == chess)) {
                        stillConnected[3] = false;  // to the down-left
                    }
                }

                boolean[] overlines = checkOverlines(stillConnected, r, c, chess);

                if (rulesSet.getOverlinesRule() == RulesSet.OVERLINES_NONE) {
                    int res = overlinesNone(stillConnected, overlines, chess);
                    if (res != 0) return res;
                } else if (rulesSet.getOverlinesRule() == RulesSet.OVERLINES_LOST) {
                    return overlinesLost(overlines, chess);
                } else {
                    if (anyOfBoolean(stillConnected)) {
                        return chess;  // Since this program automatically treats overlines as 5
                    }
                }
            }
        }
        return blanks == 0 ? 3 : 0;
    }

    private boolean[] checkOverlines(boolean[] stillConnected, int r, int c, int player) {
        boolean[] overlines = new boolean[4];
        if (stillConnected[0]) {  //  --
            if ((inBound(r, c - 1) && board[r][c - 1] == player) ||
                    (inBound(r, c + 5) && board[r][c + 5] == player))
                overlines[0] = true;
        }
        if (stillConnected[1]) {  //  \
            if ((inBound(r - 1, c - 1) && board[r - 1][c - 1] == player) ||
                    (inBound(r + 5, c + 5) && board[r + 5][c + 5] == player))
                overlines[1] = true;
        }
        if (stillConnected[2]) {  //  |
            if ((inBound(r - 1, c) && board[r - 1][c] == player) ||
                    (inBound(r + 5, c) && board[r + 5][c] == player))
                overlines[2] = true;
        }
        if (stillConnected[3]) {  //  /
            if ((inBound(r - 1, c + 1) && board[r - 1][c + 1] == player) ||
                    (inBound(r + 5, c - 5) && board[r + 5][c - 5] == player))
                overlines[3] = true;
        }
        return overlines;
    }

    private static int overlinesNone(boolean[] stillConnected, boolean[] overlines, int player) {
        for (int i = 0; i < 4; i++) {
            if (stillConnected[i]) {
                if (!overlines[i]) return player;
            }
        }
        return 0;
    }

    private static int overlinesLost(boolean[] overlines, int player) {
        for (int i = 0; i < 4; i++) {
            if (overlines[i]) {
                if (player == 1) return 2;
                else if (player == 2) return 1;
            }
        }
        return 0;
    }

    private static boolean anyOfBoolean(boolean[] array) {
        for (boolean b : array) {
            if (b) return true;
        }
        return false;
    }

    static boolean inBound(int r, int c) {
        return r >= 0 && r < 15 && c >= 0 && c < 15;
    }

    public int getLastChessR() {
        return lastChessR;
    }

    public int getLastChessC() {
        return lastChessC;
    }

    public int getLastChessPlayer() {
        return lastChessPlayer;
    }
}
