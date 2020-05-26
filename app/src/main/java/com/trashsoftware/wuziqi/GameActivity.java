package com.trashsoftware.wuziqi;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.trashsoftware.wuziqi.graphics.GuiInterface;
import com.trashsoftware.wuziqi.programs.AI;
import com.trashsoftware.wuziqi.programs.Game;
import com.trashsoftware.wuziqi.programs.Human;
import com.trashsoftware.wuziqi.programs.Player;
import com.trashsoftware.wuziqi.graphics.ChessboardView;
import com.trashsoftware.wuziqi.graphics.MsDialogFragment;
import com.trashsoftware.wuziqi.programs.RulesSet;

public class GameActivity extends AppCompatActivity implements GuiInterface {

    private ChessboardView chessboardView;

    private TextView p1Text, p2Text, blackText, whiteText;

    private Button p1UndoBtn, p2UndoBtn;

    private MsDialogFragment dialogFragment = new MsDialogFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        chessboardView = findViewById(R.id.chessboardView);
        p1Text = findViewById(R.id.p1Text);
        p2Text = findViewById(R.id.p2Text);
        blackText = findViewById(R.id.blackText);
        whiteText = findViewById(R.id.whiteText);
        p1UndoBtn = findViewById(R.id.p1UndoBtn);
        p2UndoBtn = findViewById(R.id.p2UndoBtn);

        Intent intent = getIntent();
        int gameMode = intent.getIntExtra(MainActivity.PVE_KEY, 0);

        RulesSet rulesSet;
        switch (gameMode) {
            case RulesSet.PVE:
//                rulesSet = new RulesSet(
//                        intent.getIntExtra(MainActivity.OVERLINES_KEY, RulesSet.OVERLINES_WINNING),
//                        intent.getBooleanExtra(MainActivity.AI_FIRST_KEY, false),
//                        intent.getIntExtra(MainActivity.DIFFICULTY_KEY, 0)
//                );
                rulesSet = new RulesSet.RulesSetBuilder()
                        .gameMode(RulesSet.PVE)
                        .overlinesRule(intent.getIntExtra(MainActivity.OVERLINES_KEY, RulesSet.OVERLINES_WINNING))
                        .pveAiFirst(intent.getBooleanExtra(MainActivity.AI_FIRST_KEY, false))
                        .pveDifficulty(intent.getIntExtra(MainActivity.DIFFICULTY_KEY, 0))
                        .build();
                startGameOnePlayer(rulesSet);
                break;
            case RulesSet.PVP:
//                rulesSet = new RulesSet(
//                        intent.getIntExtra(MainActivity.OVERLINES_KEY, RulesSet.OVERLINES_WINNING)
//                );
                rulesSet = new RulesSet.RulesSetBuilder()
                        .gameMode(RulesSet.PVP)
                        .overlinesRule(intent.getIntExtra(MainActivity.OVERLINES_KEY, RulesSet.OVERLINES_WINNING))
                        .build();
                startGameTwoPlayers(rulesSet);
                break;
            case RulesSet.EVE:
                rulesSet = new RulesSet.RulesSetBuilder()
                        .gameMode(RulesSet.EVE)
                        .overlinesRule(intent.getIntExtra(MainActivity.OVERLINES_KEY, RulesSet.OVERLINES_WINNING))
                        .eveAiLevels(intent.getIntArrayExtra(MainActivity.EVE_LEVELS_KEY))
                        .build();
                startGameEvE(rulesSet);
                break;
            default:
                throw new RuntimeException("No such game mode.");
        }
    }

    @Override
    public void updateUndoStatus(boolean p1Enable, boolean p2Enable) {
        p1UndoBtn.setEnabled(p1Enable);
        p2UndoBtn.setEnabled(p2Enable);
//        System.out.println(String.format("Set to p1 undo: %b, p2 undo: %b",
//                p1UndoBtn.isEnabled(), p2UndoBtn.isEnabled()));
    }

    public void p1Undo(View view) {
        chessboardView.getGame().undo();
        chessboardView.invalidate();
//        updateUndoStatus(false, false);
    }

    public void p2Undo(View view) {
        chessboardView.getGame().undo();
        chessboardView.invalidate();
//        updateUndoStatus(false, false);
    }

    public void setActivePlayer(boolean isP1) {
        setTextStyles(isP1);
    }

    public void refreshView() {
        chessboardView.invalidate();
    }

    public void showWin(int playerNum, Player winningPlayer) {
        String playerName = winningPlayer.getName();
        String msg = playerName + " " + getString(R.string.winMsg);
        dialogFragment.show(getSupportFragmentManager(), msg);
    }

    public void showTie() {
        String msg = getString(R.string.tie);
        dialogFragment.show(getSupportFragmentManager(), msg);
    }

    @Override
    public void runOnBackground(Runnable runnable) {
        AsyncTask.execute(runnable);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.pleaseConfirm)
                .setMessage(R.string.confirmExit)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        GameActivity.super.onBackPressed();
                    }
                }).setNegativeButton(R.string.cancel, null).show();
    }

    private void startGameOnePlayer(RulesSet rulesSet) {
        setTextStyles(true);
        Game game;
        if (rulesSet.isPveAiFirst()) {
            p1Text.setText(getString(R.string.computer));
            p2Text.setText(getString(R.string.player));
            game = new Game(new AI(getString(R.string.computer), rulesSet.getPveDifficultyLevel()),
                    new Human(getString(R.string.player)),
                    rulesSet,
                    this);
        } else {
            p1Text.setText(getString(R.string.player));
            p2Text.setText(getString(R.string.computer));
            game = new Game(new Human(getString(R.string.player)),
                    new AI(getString(R.string.computer), rulesSet.getPveDifficultyLevel()),
                    rulesSet,
                    this);
        }
        chessboardView.setGame(game);
    }

    private void startGameEvE(RulesSet rulesSet) {
        p1Text.setText(getString(R.string.c1));
        p2Text.setText(getString(R.string.c2));
        setTextStyles(true);
        Game game = new Game(new AI(getString(R.string.c1), rulesSet.getEveAiLevels()[0]),
                new AI(getString(R.string.c2), rulesSet.getEveAiLevels()[1]),
                rulesSet,
                this);
        chessboardView.setGame(game);
    }

    private void startGameTwoPlayers(RulesSet rulesSet) {
        p1Text.setText(getString(R.string.p1));
        p2Text.setText(getString(R.string.p2));
        setTextStyles(true);
        Game game = new Game(new Human(getString(R.string.p1)),
                new Human(getString(R.string.p2)), rulesSet, this);
        chessboardView.setGame(game);
    }

    private void setTextStyles(boolean isP1) {
        if (isP1) {
            p1Text.setTextAppearance(R.style.FocusedText);
            blackText.setTextAppearance(R.style.FocusedText);
            p2Text.setTextAppearance(R.style.UnfocusedText);
            whiteText.setTextAppearance(R.style.UnfocusedText);
        } else {
            p1Text.setTextAppearance(R.style.UnfocusedText);
            blackText.setTextAppearance(R.style.UnfocusedText);
            p2Text.setTextAppearance(R.style.FocusedText);
            whiteText.setTextAppearance(R.style.FocusedText);
        }
    }
}
