package com.trashsoftware.wuziqi.programs;

import com.trashsoftware.wuziqi.GameActivity;
import com.trashsoftware.wuziqi.R;
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
    private final int[][] board = new int[15][15];

    private final Player player1;
    private final Player player2;

    private boolean player1Moving = true;
    private boolean p1CanDraw = true;
    private boolean p2CanDraw = true;

    private boolean terminated = false;

    private final RulesSet rulesSet;

    private final GuiInterface parent;

    private int availableUndoCount = 0;

    private final Deque<ChessHistory> chessHistory = new ArrayDeque<>();

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
                    } else {
                        setUndoButtons();
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
                    } else {
                        setUndoButtons();
                    }
                }
            }
        }
    }

    public boolean isTerminated() {
        return terminated;
    }

    private void aiPlace(final Player player) {
        int aiId = player1Moving ? 1 : 2;
        boolean aiWinnable = winnable(aiId);
        boolean aiCanDraw = aiId == 1 ? p1CanDraw : p2CanDraw;
        if (aiWinnable || !aiCanDraw) {
            aiPlaceEssential(player);
        } else {
            currentPlayerAskDraw();
        }
    }

    public void terminate() {
        terminated = true;
    }

    /**
     * Current playing player asks draw, wait for another player to confirm.
     */
    public void currentPlayerAskDraw() {
        Player opponent = getOpponentPlayer();
        if (opponent.isAi()) {
            if (opponentAiDrawDecision()) {
                opponentPlayerAcceptDraw();
            } else {
                opponentPlayerRefuseDraw(false);
            }
        } else {
            parent.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    parent.currentPlayerAskDraw();
                }
            });
        }
    }

    /**
     * The current not playing player accepts draw, game is a tie.
     */
    public void opponentPlayerAcceptDraw() {
        terminate();
        parent.showToastMsg(player1Moving ? R.string.white : R.string.black, R.string.acceptsDraw);
        parent.showTie();
    }

    /**
     * The current not playing player refuses draw, game continues.
     *
     * @param alwaysRefuse whether the current not playing always refuse draw in this game
     */
    public void opponentPlayerRefuseDraw(boolean alwaysRefuse) {
        parent.showToastMsg(player1Moving ? R.string.white : R.string.black, R.string.refusesDraw);

        if (alwaysRefuse) {  // opponent player always refuse
            if (player1Moving) {
                p1CanDraw = false;  // current player cannot draw from now on
            } else {
                p2CanDraw = false;
            }
        }

        Player movingPlayer = getCurrentPlayer();
        if (movingPlayer.isAi()) {
            aiPlaceEssential(movingPlayer);
        }
    }

    private boolean opponentAiDrawDecision() {
        int askerId = player1Moving ? 1 : 2;
        int deciderId = player1Moving ? 2 : 1;
        int askerWinCount = winnableCount(askerId);
        int deciderWinCount = winnableCount(deciderId);
        System.out.printf("Asker has %d chances and decider has %d chances.\n",
                askerWinCount, deciderWinCount);
        return deciderWinCount < 200 && deciderWinCount < askerWinCount;
    }

    private void aiPlaceEssential(Player player) {
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
            if (player1Moving) {
                board[r][c] = 1;
            } else {
                board[r][c] = 2;
            }
            addHistory(r, c, player1Moving ? 1 : 2);
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
            parent.updateDrawStatus(false, false);
            return;
        }
        if (rulesSet.getGameMode() == RulesSet.PVE) {
            if (!isHumanPlaying()) {
                parent.updateUndoStatus(false, false);
                parent.updateDrawStatus(false, false);
            } else {
                if (availableUndoCount > 0 && chessHistory.size() >= 2) {
                    parent.updateUndoStatus(!player1.isAi(), !player2.isAi());
                } else {
                    parent.updateUndoStatus(false, false);
                }
                parent.updateDrawStatus(!player1.isAi() && p1CanDraw,
                        !player2.isAi() && p2CanDraw);
            }
        } else if (rulesSet.getGameMode() == RulesSet.PVP) {
            // For pvp, only 1 undo at most
            if (availableUndoCount > 0 &&
                    !chessHistory.isEmpty()) {
                parent.updateUndoStatus(!player1Moving, player1Moving);
            } else {
                parent.updateUndoStatus(false, false);
            }
            parent.updateDrawStatus(player1Moving && p1CanDraw, !player1Moving && p2CanDraw);
        } else {  // EVE
            parent.updateUndoStatus(false, false);
            parent.updateDrawStatus(false, false);
        }
    }

    public void undo() {
        availableUndoCount--;
        if (rulesSet.isPve()) {
            ChessHistory ch1 = chessHistory.removeLast();
            ChessHistory ch2 = chessHistory.removeLast();
            board[ch1.r][ch1.c] = 0;
            board[ch2.r][ch2.c] = 0;
        } else {
            ChessHistory ch = chessHistory.removeLast();
            board[ch.r][ch.c] = 0;
            swapPlayer();
        }
        parent.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setUndoButtons();
            }
        });
    }

    private void swapPlayer() {
        player1Moving = !player1Moving;
        parent.setActivePlayer(player1Moving);
    }

    boolean winnable(int player) {
        int winnableCount = winnableCount(player);
        System.out.println("Player " + player + " has " + winnableCount + " spaces to win.");
        return winnableCount > 0;
    }

    /**
     * @return 0 if no one wining, 1 if p1 winning, 2 if p2 winning, 3 if tie
     */
    private int checkWinning() {
        int res = traverseCheckWin();
        if (res != 0) {
            terminated = true;
            if (res == 3) {
                parent.showTie();
            } else {
                Player winningPlayer = res == 1 ? player1 : player2;
                parent.showWin(res, winningPlayer);
            }
        }
        return res;
    }

    private int winnableCount(int player) {
        int count = 0;
        int otherPlayer = player == 1 ? 2 : 1;
        for (int r = 0; r < 15; r++) {
            for (int c = 0; c < 15; c++) {
                int chess = board[r][c];
                if (chess == otherPlayer) continue;  // if not empty or player's chess, continue

                if (c < 11) {
                    boolean toRight = true;
                    for (int x = 1; x < 5; x++) {
                        if (board[r][c + x] == otherPlayer) {
                            toRight = false;
                            break;
                        }
                    }
                    if (toRight) count++;

                    if (r < 11) {
                        boolean toRightDown = true;
                        for (int x = 1; x < 5; x++) {
                            if (board[r + x][c + x] == otherPlayer) {
                                toRightDown = false;
                                break;
                            }
                        }
                        if (toRightDown) count++;
                    }
                }

                if (r < 11) {
                    boolean toDown = true;
                    for (int x = 1; x < 5; x++) {
                        if (board[r + x][c] == otherPlayer) {
                            toDown = false;
                            break;
                        }
                    }
                    if (toDown) count++;

                    if (c >= 4) {
                        boolean toLeftDown = true;
                        for (int x = 1; x < 5; x++) {
                            if (board[r + x][c - x] == otherPlayer) {
                                toLeftDown = false;
                                break;
                            }
                        }
                        if (toLeftDown) count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * @return the id of player won, 0 if no one won
     */
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

    public int getLastChessPlayerId() {
        if (chessHistory.isEmpty()) return 0;
        else return chessHistory.getLast().player;
    }

    public boolean isBlackMoving() {
        return player1Moving;
    }

    public Player getCurrentPlayer() {
        return player1Moving ? player1 : player2;
    }

    public Player getOpponentPlayer() {
        return player1Moving ? player2 : player1;
    }

    public boolean canPlayer1Draw() {
        return p1CanDraw;
    }

    public boolean canPlayer2Draw() {
        return p2CanDraw;
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