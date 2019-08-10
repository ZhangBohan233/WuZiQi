package com.trashsoftware.wuziqi.programs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

public class AI extends Player {

    public AI() {
        GameSimulator.initScoreList();
    }

    @Override
    public boolean isAi() {
        return true;
    }

    public void aiMove(Game game, boolean aiIsPlayer2) {
        int chess = aiIsPlayer2 ? 2 : 1;
        GameSimulator gameSimulator = new GameSimulator(game);
//        int[] pos = gameSimulator.bestPositionLevel1(chess);
        Position pos2 = gameSimulator.bestPositionLevel2(chess);

        if (!game.innerPlace(pos2.getY(), pos2.getX())) {
            throw new RuntimeException("Unexpected error");
        }
    }
}

class GameSimulator {
    private int[][] board = new int[15][15];
    private Deque<int[]> placeSequence = new ArrayDeque<>();

    private static final SequenceHashTable scoreTable = new SequenceHashTable();

    private Position nextMove;
    private int searchDepth = 3;

    /**
     * Copied from
     * 董红安, 2005. 《计算机五子棋博弈系统的研究与实现》
     */
    static void initScoreList() {
        scoreTable.add(new ChessSequence(50000, 1, 1, 1, 1, 1));
        scoreTable.add(new ChessSequence(4320, 0, 1, 1, 1, 1, 0));
        scoreTable.add(new ChessSequence(720, 0, 1, 1, 1, 0, 0));
        scoreTable.add(new ChessSequence(720, 0, 0, 1, 1, 1, 0));
        scoreTable.add(new ChessSequence(720, 0, 1, 1, 0, 1, 0));
        scoreTable.add(new ChessSequence(720, 0, 1, 0, 1, 1, 0));
        scoreTable.add(new ChessSequence(720, 1, 1, 1, 1, 0));
        scoreTable.add(new ChessSequence(720, 0, 1, 1, 1, 1));
        scoreTable.add(new ChessSequence(720, 1, 1, 0, 1, 1));
        scoreTable.add(new ChessSequence(720, 1, 0, 1, 1, 1));
        scoreTable.add(new ChessSequence(720, 1, 1, 1, 0, 1));
        scoreTable.add(new ChessSequence(120, 0, 0, 1, 1, 0, 0));
        scoreTable.add(new ChessSequence(120, 0, 0, 1, 0, 1, 0));
        scoreTable.add(new ChessSequence(120, 0, 1, 0, 1, 0, 0));
        scoreTable.add(new ChessSequence(20, 0, 0, 0, 1, 0, 0));
        scoreTable.add(new ChessSequence(20, 0, 0, 1, 0, 0, 0));
    }

    GameSimulator(Game game) {
        for (int r = 0; r < 15; r++) {
            for (int c = 0; c < 15; c++) {
                board[r][c] = game.getChessAt(r, c);
            }
        }
    }

    private void placeChess(int r, int c, int chess) {
        board[r][c] = chess;
        placeSequence.push(new int[]{r, c});
    }

    private void undoLastMove() {
        int[] lastMove = placeSequence.pop();
        board[lastMove[0]][lastMove[1]] = 0;
    }

    Position bestPositionLevel1(int chess) {
        return bestPositionOfOneDepth(chess);  // temporary
    }

    Position bestPositionLevel2(int chess) {
        int val = alphaBeta(searchDepth, -10_000_000, 10_000_000, chess);
        return nextMove;
    }

//    private int[] getLastMove() {
//        return placeSequence.peek();
//    }

    private int alphaBeta(int depth, int alpha, int beta, int chess) {
        if (depth == 0) {
            return totalScoreOfGame(chess) - totalScoreOfGame(chess == 1 ? 2 : 1);
        }
        List<Position> places = possiblePlaces(chess);
//        System.out.println(places);
        while (!places.isEmpty()) {
            Position next = places.remove(places.size() - 1);
            placeChess(next.getY(), next.getX(), chess);
            int val = -alphaBeta(depth - 1, -beta, -alpha, chess == 1 ? 2 : 1);
            undoLastMove();
            if (val >= beta) {
                return beta;
            }
            if (val > alpha) {
                alpha = val;
                if (depth == searchDepth) {
                    nextMove = next;
                }
            }
        }
        return alpha;
    }

    private Position bestPositionOfOneDepth(int chess) {
        Position currentBest = getDefaultPosition();
        int highestScore = 0;
        for (int r = 0; r < 15; r++) {
            for (int c = 0; c < 15; c++) {
                if (board[r][c] == 0) {
                    placeChess(r, c, chess);
                    int pointScore = scoreOfPoint(r, c, chess);
                    undoLastMove();
                    if (pointScore > highestScore) {
                        currentBest = new Position(r, c);
                        highestScore = pointScore;
                    }
                }
            }
        }
        return currentBest;
    }

    private List<Position> possiblePlaces(int chess) {
        List<Position> places = new ArrayList<>();
        for (int r = 0; r < 15; r++) {
            for (int c = 0; c < 15; c++) {
                if (board[r][c] == 0) {
                    CHECK_LOOP:
                    for (int i = r - 1; i < r + 2; i++)
                        for (int j = c - 1; j < c + 2; j++)
                            if (i != r || j != c)
                                if (Game.inBound(i, j))
                                    if (board[i][j] != 0) {
                                        // This position is adjacent to a chess
                                        places.add(new Position(r, c));
                                        break CHECK_LOOP;
                                    }
                }
            }
        }
        if (places.isEmpty()) places.add(getDefaultPosition());
        return places;
    }

    private Position getDefaultPosition() {
        if (board[7][7] == 0) return new Position(7, 7);
        else if (board[6][7] == 0) return new Position(6, 7);
        else {
            for (int r = 0; r < 15; r++)
                for (int c = 0; c < 15; c++) {
                    if (board[r][c] == 0) return new Position(r, c);
                }
            return new Position(-1, -1);
        }
    }

    private int totalScoreOfGame(int chess) {
        int score = 0;

        for (int r = 0; r < 15; r++) {  // rows
            int[] row = board[r];
            score += getScore(row, chess);
        }

        for (int c = 0; c < 15; c++) {  // columns
            int[] col = new int[15];
            for (int r = 0; r < 15; r++) {
                col[r] = board[r][c];
            }
            score += getScore(col, chess);
        }

        for (int i = 0; i < 29; i++) {  // backward slashes, like \
            int[] slash = new int[lengthOfSlash(i)];  // from top-right corner to bottom-left

            int startR;
            if (i < 15) startR = 0;
            else startR = i - 14;

            int startC;
            if (i < 15) startC = 14 - i;
            else startC = 0;

            for (int j = 0; j < slash.length; j++) {
                int r = startR + j;
                int c = startC + j;
                slash[j] = board[r][c];
            }
            score += getScore(slash, chess);
        }

        for (int i = 0; i < 29; i++) {  // forward slashes, like /
            int[] slash = new int[lengthOfSlash(i)];  // from top-left corner to bottom-right

            int startR;
            if (i < 15) startR = i;
            else startR = 14;

            int startC;
            if (i < 15) startC = 0;
            else startC = i - 14;

            for (int j = 0; j < slash.length; j++) {
                int r = startR - j;
                int c = startC + j;
                slash[j] = board[r][c];
            }
            score += getScore(slash, chess);
        }

        return score;
    }

    private static int lengthOfSlash(int x) {
        if (x < 15) return x + 1;
        else return 29 - x;
    }

    private int scoreOfPoint(int r, int c, int chess) {

        int[] hor = new int[9];
        int[] ver = new int[9];
        int[] forwardSlash = new int[9];
        int[] backwardSlash = new int[9];

        int i = 0;
        for (int x = c - 4; x < c + 5; x++, i++) {
            if (Game.inBound(r, x)) {
                hor[i] = board[r][x];
            } else {
                hor[i] = -1;
            }
        }

        i = 0;
        for (int y = r - 4; y < r + 5; y++, i++) {
            if (Game.inBound(y, c)) {
                ver[i] = board[y][c];
            } else {
                ver[i] = -1;
            }
        }

        i = 0;
        for (int n = -4; n < 5; n++, i++) {
            int y = r + n;
            int x = c + n;
            if (Game.inBound(y, x)) {
                backwardSlash[i] = board[y][x];
            } else {
                backwardSlash[i] = -1;
            }
        }

        i = 0;
        for (int n = -4; n < 5; n++, i++) {
            int y = r - n;
            int x = c + n;
            if (Game.inBound(y, x)) {
                forwardSlash[i] = board[y][x];
            } else {
                forwardSlash[i] = -1;
            }
        }

        return getScore(hor, chess)
                + getScore(ver, chess)
                + getScore(forwardSlash, chess)
                + getScore(backwardSlash, chess);
    }

    private static int getScore(int[] sequence, int chess) {
        int score = 0;

        if (chess == 1) {
            for (int i = 0; i < sequence.length - 4; i++) {  // length 5 sub-sequences
                ChessSequence matchedSequence = scoreTable.get(ChessSequence.hashSequence(sequence, i, i + 5));
                if (matchedSequence != null) {
                    score += matchedSequence.getScore();
                }
            }

            for (int i = 0; i < sequence.length - 5; i++) {  // length 6 sub-sequences
                ChessSequence matchedSequence = scoreTable.get(ChessSequence.hashSequence(sequence, i, i + 6));
                if (matchedSequence != null) {
                    score += matchedSequence.getScore();
                }
            }
        } else {
            for (int i = 0; i < sequence.length - 4; i++) {  // length 5 sub-sequences
                ChessSequence matchedSequence = scoreTable.get(ChessSequence.hashSequenceOfChess2(sequence, i, i + 5));
                if (matchedSequence != null) {
                    score += matchedSequence.getScore();
                }
            }

            for (int i = 0; i < sequence.length - 5; i++) {  // length 6 sub-sequences
                ChessSequence matchedSequence = scoreTable.get(ChessSequence.hashSequenceOfChess2(sequence, i, i + 6));
                if (matchedSequence != null) {
                    score += matchedSequence.getScore();
                }
            }
        }
//        for (ChessSequence chessSequence : scoreTable) {
//            if (containsSequence(chessSequence.getSequence(), sequence, chess)) {
//                score += chessSequence.getScore();
//            }
//        }
        return score;
    }

//    private static int hashSequence(int[])

    private static boolean containsSequence(int[] targetSequence, int[] sequence, int chess) {
        OUT:
        for (int i = 0; i < sequence.length - targetSequence.length + 1; i++) {
            for (int j = 0; j < targetSequence.length; j++) {
                if (chess == 1) {
                    if (targetSequence[j] != sequence[i + j])
                        continue OUT;
                } else if (chess == 2) {
                    int x = targetSequence[j] == 1 ? 2 : 0;
                    if (x != sequence[i + j])
                        continue OUT;
                }
            }
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        initScoreList();
//        for (ChessSequence chessSequence : scoreTable) {
//            System.out.println(chessSequence.hashCode());
//        }
        ChessSequence cs1 = new ChessSequence(500, 0, 1, 1, 0, 1, 0);
        ChessSequence cs2 = new ChessSequence(500, 0, 1, 1, 0, 1, 0);
        System.out.println(cs1.hashCode());
        System.out.println(cs2.hashCode());
        System.out.println(getScore(new int[]{0, 2, 2, 2, 2, 2, 0}, 2));
    }

}

class ChessSequence {

    private int score;
    private int[] sequence;

    ChessSequence(int score, int... sequence) {
        this.score = score;
        this.sequence = sequence;
    }

    int[] getSequence() {
        return sequence;
    }

    int getScore() {
        return score;
    }

//    @Override
//    public boolean equals(@Nullable Object obj) {
//        return obj instanceof ChessSequence && this.hashCode() == obj.hashCode();
//    }

    @Override
    public int hashCode() {
        return hashSequence(sequence, 0, sequence.length);
    }

    static int hashSequence(int[] sequence, int from, int to) {
        int code = 1;
        for (int i = from; i < to; i++) {
            int x = sequence[i];
            code = (code << 2) | x;
        }
        return code;
    }

    static int hashSequenceOfChess2(int[] sequence, int from, int to) {
        int code = 1;
        for (int i = from; i < to; i++) {
            int x;
            if (sequence[i] == 1) x = 2;
            else if (sequence[i] == 2) x = 1;
            else x = 0;
            code = (code << 2) | x;
        }
        return code;
    }
}

class SequenceHashTable {

    private static final int RANGE = 8192;
    private ChessSequence[] table = new ChessSequence[RANGE];

    void add(ChessSequence chessSequence) {
        int code = chessSequence.hashCode();
        table[code] = chessSequence;
    }

    ChessSequence get(int hashCode) {
        if (hashCode >= 0 && hashCode < RANGE)
            return table[hashCode];
        else return null;
    }
}

class Position {
    private int y;
    private int x;
    private int score;

    Position(int y, int x) {
        this.y = y;
        this.x = x;
    }

    int getY() {
        return y;
    }

    int getX() {
        return x;
    }

    @NonNull
    @Override
    public String toString() {
        return "[" + y + ", " + x + "]";
    }
}
