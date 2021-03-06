package com.trashsoftware.wuziqi.programs;

import com.trashsoftware.wuziqi.GameActivity;
import com.trashsoftware.wuziqi.graphics.GuiInterface;

import java.util.ArrayDeque;
import java.util.Deque;

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
//    private int last2ChessR = -1, last2ChessC = -1;
//    private int lastChessR = -1, lastChessC = -1;
//    private int lastChessPlayer = 0;

    private Player player1;
    private Player player2;

    private boolean player1Moving = true;

    private boolean terminated = false;

    private RulesSet rulesSet;

    private GuiInterface parent;

    private int availableUndoCount = 0;

    private Deque<ChessHistory> chessHistory = new ArrayDeque<>();

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
//            lastChessPlayer = 0;  // clears the spot of the last chess
            return;
        }

        if (availableUndoCount < rulesSet.getUndoStepsCount()) availableUndoCount++;

        if (player1Moving) {
            if (player1.isAi()) {
                // eve case
                parent.runOnBackground(new Runnable() {
                    @Override
                    public void run() {
                        aiPlace(player1);
                    }
                });
            } else {
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
                        } else {
                            setUndoButtons();
                        }
                    }
                }
            }
        } else  {
            if (player2.isAi()) {
                // eve case
                parent.runOnBackground(new Runnable() {
                    @Override
                    public void run() {
                        aiPlace(player2);
                    }
                });
            } else {
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
                        } else {
                            setUndoButtons();
                        }
                    }
                }
            }
        }
    }

    public boolean isTerminated() {
        return terminated;
    }

    private void aiPlace(Player player) {
        parent.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setUndoButtons();
            }
        });
        AI ai = (AI) player;
        ai.aiMove(this, player2.isAi());
        parent.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                swapPlayer();
                parent.refreshView();
                checkWinning();
                setUndoButtons();
            }
        });
    }

    boolean innerPlace(int r, int c) {
        if (board[r][c] == 0) {
//            last2ChessR = lastChessR;
//            last2ChessC = lastChessC;
//            lastChessR = r;
//            lastChessC = c;

            if (player1Moving) {
                board[r][c] = 1;
//                lastChessPlayer = 1;
            } else {
                board[r][c] = 2;
//                lastChessPlayer = 2;
            }
            addHistory(r, c, player1Moving ? 1 : 2);
//            ChessHistory lastChess = new ChessHistory(r, c, player1Moving ? 1 : 2);
//            chessHistory.addLast(lastChess);
            return true;
        } else {
            return false;
        }
    }

    private void addHistory(int r, int c, int player) {
        ChessHistory lastChess = new ChessHistory(r, c, player);
        chessHistory.addLast(lastChess);
    }

    public int getChessAt(int r, int c) {
        return board[r][c];
    }

    private boolean isHumanPlaying() {
        return (player1Moving && player2.isAi()) || (!player1Moving && player1.isAi());
    }

    private void setUndoButtons() {
        if (terminated) {
            parent.updateUndoStatus(false, false);
            return;
        }
        if (rulesSet.getGameMode() == RulesSet.PVE) {
            if (!isHumanPlaying()) {
                parent.updateUndoStatus(false, false);
            } else {
                if (availableUndoCount > 0 && chessHistory.size() >= 2) {
                    parent.updateUndoStatus(!player1.isAi(), !player2.isAi());
                } else {
                    parent.updateUndoStatus(false, false);
                }
            }
        } else if (rulesSet.getGameMode() == RulesSet.PVP) {
            // For pvp, only 1 undo at most
            if (availableUndoCount > 0 &&
                    !chessHistory.isEmpty()) {
                parent.updateUndoStatus(!player1Moving, player1Moving);
            } else {
                parent.updateUndoStatus(false, false);
            }
        } else {  // EVE
            parent.updateUndoStatus(false, false);
        }
    }

    public void undo() {
        availableUndoCount--;
        if (rulesSet.isPve()) {
            ChessHistory ch1 = chessHistory.removeLast();
            ChessHistory ch2 = chessHistory.removeLast();
            board[ch1.r][ch1.c] = 0;
            board[ch2.r][ch2.c] = 0;
//            board[lastChessR][lastChessC] = 0;
//            board[last2ChessR][last2ChessC] = 0;
        } else {
            ChessHistory ch = chessHistory.removeLast();
            board[ch.r][ch.c] = 0;
//            board[lastChessR][lastChessC] = 0;
            swapPlayer();
        }
        parent.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setUndoButtons();
            }
        });
//        lastChessR = -1;
//        lastChessC = -1;
//        last2ChessR = -1;
//        last2ChessC = -1;
//        lastChessPlayer = 0;
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
        if (chessHistory.isEmpty()) return -1;
        else return chessHistory.getLast().r;
    }

    public int getLastChessC() {
        if (chessHistory.isEmpty()) return -1;
        else return chessHistory.getLast().c;
    }

    public int getLastChessPlayer() {
        if (chessHistory.isEmpty()) return 0;
        else return chessHistory.getLast().player;
    }

    public static class ChessHistory {
        final int r;
        final int c;
        final int player;

        ChessHistory(int r, int c, int player) {
            this.r = r;
            this.c = c;
            this.player = player;
        }
    }
}