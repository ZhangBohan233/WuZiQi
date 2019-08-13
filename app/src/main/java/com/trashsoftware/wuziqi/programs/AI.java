package com.trashsoftware.wuziqi.programs;

import android.support.annotation.NonNull;


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public class AI extends Player {

    public AI(String name) {
        super(name);
        GameSimulator.initScoreList();
    }

    @Override
    public boolean isAi() {
        return true;
    }

    void aiMove(Game game, boolean aiIsPlayer2, int difficultyLevel) {
        int chess = aiIsPlayer2 ? 2 : 1;
        GameSimulator gameSimulator = new GameSimulator(game);

        Position pos;
        if (difficultyLevel == 0) {
            pos = gameSimulator.bestPositionLevel0(chess);
        } else {
            gameSimulator.setDifficultyLevel(difficultyLevel);
            pos = gameSimulator.bestPositionHighLevel(chess);
        }

        if (!game.innerPlace(pos.getY(), pos.getX())) {
            throw new RuntimeException("Unexpected error");
        }
    }
}

class GameSimulator {
    private int[][] board = new int[15][15];
    private Deque<int[]> placeSequence = new ArrayDeque<>();

    private int[] horScores1 = new int[15];
    private int[] horScores2 = new int[15];
    private int[] verScores1 = new int[15];
    private int[] verScores2 = new int[15];
    private int[] backSlashScores1 = new int[29];
    private int[] backSlashScores2 = new int[29];
    private int[] fwdSlashScores1 = new int[29];
    private int[] fwdSlashScores2 = new int[29];

    private static final SequenceHashTable scoreTable = new SequenceHashTable();

    private Position nextMove;
//    private Position killMove;
    private int searchDepth = 3;
//    private int killDepth = 5;

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
        initializeScores();
    }

    private int getChessAt(int r, int c) {
        return board[r][c];
    }

    private GameSimulator() {
        board = new int[15][15];
    }

    void setDifficultyLevel(int difficultyLevel) {
        searchDepth = difficultyLevel;
    }

    void placeChess(int r, int c, int chess) {
        board[r][c] = chess;
        placeSequence.push(new int[]{r, c});
    }

    void undoLastMove() {
        int[] lastMove = placeSequence.pop();
        board[lastMove[0]][lastMove[1]] = 0;
    }

    Position bestPositionLevel0(int chess) {
        return bestPositionOfOneDepth(chess);
    }

    Position bestPositionHighLevel(int chess) {
//        calculateKill(killDepth, -10_000_000, 10_000_000, chess);
//        if (killMove == null) {
//            alphaBeta(searchDepth, -10_000_000, 10_000_000, chess);
//            return nextMove;
//        } else {
//            System.out.println(111);
//            return killMove;
//        }
        alphaBeta(searchDepth, -10_000_000, 10_000_000, chess);
        return nextMove;
    }

    private int alphaBeta(int depth, int alpha, int beta, int chess) {
        if (depth == 0) {
            return evaluate(chess);
        }
        List<Position> places = possiblePlaces(chess);
        Collections.sort(places);  // Sorts the positions to make alpha-cut happen early
        while (!places.isEmpty()) {
            Position next = places.remove(places.size() - 1);
            placeChess(next.getY(), next.getX(), chess);
            updateScores(next.getY(), next.getX());
            int val = -alphaBeta(depth - 1, -beta, -alpha, chess == 1 ? 2 : 1);
            undoLastMove();
            updateScores(next.getY(), next.getX());
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

//    private int calculateKill(int depth, int alpha, int beta, int chess) {
//        if (depth == 0) {
//            return evaluate(chess);
//        }
//        List<Position> places = killPositions(chess);
//        System.out.println(places);
//        Collections.sort(places);  // Sorts the positions to make alpha-cut happen early
//        while (!places.isEmpty()) {
//            Position next = places.remove(places.size() - 1);
//            placeChess(next.getY(), next.getX(), chess);
//            updateScores(next.getY(), next.getX());
//            int val = -calculateKill(depth - 1, -beta, -alpha, chess == 1 ? 2 : 1);
//            undoLastMove();
//            updateScores(next.getY(), next.getX());
//            if (val >= beta) {
//                return beta;
//            }
//            if (val > alpha) {
//                alpha = val;
//                if (depth == killDepth) {
//                    killMove = next;
//                }
//            }
//        }
//        return alpha;
//    }
//
//    private List<Position> killPositions(int chess) {
//        List<Position> killPos = new ArrayList<>();
//        for (int r = 0; r < 15; r++) {
//            for (int c = 0; c < 15; c++) {
//                if (board[r][c] == 0) {
//                    placeChess(r, c, chess);
//                    int pointScore = scoreOfPoint(r, c, chess);
//                    undoLastMove();
//                    if (pointScore >= 720) {  // is a killing point
//                        killPos.add(new Position(r, c, chess, this));
//                    }
//                }
//            }
//        }
//        return killPos;
//    }

    private Position bestPositionOfOneDepth(int chess) {
        Position currentBest;
        try {
            currentBest = getDefaultPosition(chess);
        } catch (NoPositionLeftException e) {
            throw new RuntimeException("Unexpected error. This method should not be called if no" +
                    "spaces left");
        }
        int highestScore = 0;
        for (int r = 0; r < 15; r++) {
            for (int c = 0; c < 15; c++) {
                if (board[r][c] == 0) {
                    placeChess(r, c, chess);
                    int pointScore = scoreOfPoint(r, c, chess);
                    undoLastMove();
                    if (pointScore > highestScore) {
                        currentBest = new Position(r, c, chess, this);
                        highestScore = pointScore;
                    }
                }
            }
        }
        return currentBest;
    }

    private void initializeScores() {
        for (int i = 0; i < 15; i++) {
            updateHorScore(i, 1);
            updateHorScore(i, 2);
            updateVerScore(i, 1);
            updateHorScore(i, 2);
        }
        for (int i = 0; i < 29; i++) {
            updateBackSlashScore(i, 1);
            updateBackSlashScore(i, 2);
            updateFwdSlashScores(i, 1);
            updateFwdSlashScores(i, 2);
        }
    }

    private int calculateTotalScoreOf(int chess) {
        int score = 0;
        if (chess == 1) {
            for (int s : horScores1) score += s;
            for (int s : verScores1) score += s;
            for (int s : backSlashScores1) score += s;
            for (int s : fwdSlashScores1) score += s;
        } else {
            for (int s : horScores2) score += s;
            for (int s : verScores2) score += s;
            for (int s : backSlashScores2) score += s;
            for (int s : fwdSlashScores2) score += s;
        }
        return score;
    }

    private int evaluate(int chess) {
        return calculateTotalScoreOf(chess) - calculateTotalScoreOf(chess == 1 ? 2 : 1);
    }

    private void updateScores(int r, int c) {
        updateHorScore(r, 1);
        updateHorScore(r, 2);
        updateVerScore(c, 1);
        updateVerScore(c, 2);
        int backSlashId = getBackSlashId(r, c);
        int fwdSlashId = getFwdSlashId(r, c);
        updateBackSlashScore(backSlashId, 1);
        updateBackSlashScore(backSlashId, 2);
        updateFwdSlashScores(fwdSlashId, 1);
        updateFwdSlashScores(fwdSlashId, 2);
    }

    private List<Position> possiblePlaces(int chess) {
        List<Position> places = new ArrayList<>();
        for (int r = 0; r < 15; r++) {
            for (int c = 0; c < 15; c++) {
                if (board[r][c] == 0) {  // is empty
                    CHECK_LOOP:
                    for (int i = r - 1; i < r + 2; i++)
                        for (int j = c - 1; j < c + 2; j++)
                            if (i != r || j != c)
                                if (Game.inBound(i, j))
                                    if (board[i][j] != 0) {
                                        // This position is adjacent to a chess
                                        places.add(new Position(r, c, chess, this));
                                        break CHECK_LOOP;
                                    }
                }
            }
        }
        if (places.isEmpty()) {
            try {
                places.add(getDefaultPosition(chess));
            } catch (NoPositionLeftException e) {
                // Do nothing
            }
        }
        return places;
    }

    private Position getDefaultPosition(int chess) throws NoPositionLeftException {
        if (getChessAt(7, 7) == 0) return new Position(7, 7, chess, this);
        else if (getChessAt(6, 7) == 0) return new Position(6, 7, chess, this);
        else {
            for (int r = 0; r < 15; r++)
                for (int c = 0; c < 15; c++) {
                    if (getChessAt(r, c) == 0) return new Position(r, c, chess, this);
                }
            throw new NoPositionLeftException();
        }
    }

    private void updateHorScore(int rowNumber, int chess) {
        int[] row = board[rowNumber];
        if (chess == 1) {
            horScores1[rowNumber] = getScore(row, chess);
        } else {
            horScores2[rowNumber] = getScore(row, chess);
        }
    }

    private void updateVerScore(int colNumber, int chess) {
        int[] col = new int[15];
        for (int r = 0; r < 15; r++) {
            col[r] = board[r][colNumber];
        }
        if (chess == 1) {
            verScores1[colNumber] = getScore(col, chess);
        } else {
            verScores2[colNumber] = getScore(col, chess);
        }
    }

    private void updateBackSlashScore(int slashId, int chess) {
        int[] slash = new int[lengthOfSlash(slashId)];  // from top-right corner to bottom-left

        int startR;
        if (slashId < 15) startR = 0;
        else startR = slashId - 14;

        int startC;
        if (slashId < 15) startC = 14 - slashId;
        else startC = 0;

        for (int j = 0; j < slash.length; j++) {
            int r = startR + j;
            int c = startC + j;
            slash[j] = board[r][c];
        }
        if (chess == 1) {
            backSlashScores1[slashId] = getScore(slash, chess);
        } else {
            backSlashScores2[slashId] = getScore(slash, chess);
        }
    }

    private void updateFwdSlashScores(int slashId, int chess) {
        int[] slash = new int[lengthOfSlash(slashId)];  // from top-left corner to bottom-right

        int startR;
        if (slashId < 15) startR = slashId;
        else startR = 14;

        int startC;
        if (slashId < 15) startC = 0;
        else startC = slashId - 14;

        for (int j = 0; j < slash.length; j++) {
            int r = startR - j;
            int c = startC + j;
            slash[j] = board[r][c];
        }
        if (chess == 1) {
            fwdSlashScores1[slashId] = getScore(slash, chess);
        } else {
            fwdSlashScores2[slashId] = getScore(slash, chess);
        }
    }

    private static int getBackSlashId(int r, int c) {
        return 14 + r - c;
    }

    private static int getFwdSlashId(int r, int c) {
        return r + c;
    }

    private static int lengthOfSlash(int x) {
        if (x < 15) return x + 1;
        else return 29 - x;
    }

    int scoreOfPoint(int r, int c, int chess) {

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
                ChessSequence matchedSequence =
                        scoreTable.get(ChessSequence.hashSequence(sequence, i, i + 5));
                if (matchedSequence != null) {
                    score += matchedSequence.getScore();
                }
            }

            for (int i = 0; i < sequence.length - 5; i++) {  // length 6 sub-sequences
                ChessSequence matchedSequence =
                        scoreTable.get(ChessSequence.hashSequence(sequence, i, i + 6));
                if (matchedSequence != null) {
                    score += matchedSequence.getScore();
                }
            }
        } else {
            for (int i = 0; i < sequence.length - 4; i++) {  // length 5 sub-sequences
                ChessSequence matchedSequence =
                        scoreTable.get(ChessSequence.hashSequenceOfChess2(sequence, i, i + 5));
                if (matchedSequence != null) {
                    score += matchedSequence.getScore();
                }
            }

            for (int i = 0; i < sequence.length - 5; i++) {  // length 6 sub-sequences
                ChessSequence matchedSequence =
                        scoreTable.get(ChessSequence.hashSequenceOfChess2(sequence, i, i + 6));
                if (matchedSequence != null) {
                    score += matchedSequence.getScore();
                }
            }
        }
        return score;
    }

    public static void main(String[] args) {
        initScoreList();
        GameSimulator gameSimulator = new GameSimulator();
        gameSimulator.setDifficultyLevel(1);
        gameSimulator.board[6][6] = 2;
        gameSimulator.board[7][5] = 2;
        gameSimulator.board[8][4] = 2;
        gameSimulator.board[7][6] = 1;
        gameSimulator.board[7][7] = 1;
        gameSimulator.initializeScores();
        System.out.println(gameSimulator.bestPositionHighLevel(1));
//        System.out.println(gameSimulator.totalScoreOfGame(2));
    }

}

class ChessSequence {

    private int score;
    private int[] sequence;

    ChessSequence(int score, int... sequence) {
        this.score = score;
        this.sequence = sequence;
    }

    int getScore() {
        return score;
    }

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
            int x = sequence[i] == 0 ? 0 : 3 - sequence[i];
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

class Position implements Comparable<Position> {
    private int y;
    private int x;
    private int chess;
    private int score = 1;
    private GameSimulator parent;

    Position(int y, int x, int chess, GameSimulator parent) {
        this.y = y;
        this.x = x;
        this.chess = chess;
        this.parent = parent;
    }

    private boolean notEvaluated() {
        return score == 1;
    }

    private void evaluateScore() {
        parent.placeChess(y, x, chess);
        score = parent.scoreOfPoint(y, x, chess);
        parent.undoLastMove();
    }

    int getY() {
        return y;
    }

    int getX() {
        return x;
    }

    @Override
    public int compareTo(Position o) {
        if (notEvaluated()) evaluateScore();
        if (o.notEvaluated()) o.evaluateScore();
        return this.score - o.score;
    }

    @NonNull
    @Override
    public String toString() {
        return "[" + y + ", " + x + "]" + (score == 1 ? "" : score);
    }
}

class NoPositionLeftException extends Exception {

}
