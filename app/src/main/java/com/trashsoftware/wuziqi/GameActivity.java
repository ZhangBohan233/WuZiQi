package com.trashsoftware.wuziqi;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

        Intent intent = getIntent();
        boolean isPve = intent.getBooleanExtra(MainActivity.PVE_KEY, false);

        if (isPve) {
            RulesSet rulesSet = new RulesSet(
                    intent.getIntExtra(MainActivity.OVERLINES_KEY, RulesSet.OVERLINES_WINNING),
                    intent.getBooleanExtra(MainActivity.AI_FIRST_KEY, false),
                    intent.getIntExtra(MainActivity.DIFFICULTY_KEY, 0)
            );
            startGameOnePlayer(rulesSet);
        } else {
            RulesSet rulesSet = new RulesSet(
                    intent.getIntExtra(MainActivity.OVERLINES_KEY, RulesSet.OVERLINES_WINNING)
            );
            startGameTwoPlayers(rulesSet);
        }
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

    private void startGameOnePlayer(RulesSet rulesSet) {
        setTextStyles(true);
        Game game;
        if (rulesSet.isAiFirst()) {
            p1Text.setText(getString(R.string.computer));
            p2Text.setText(getString(R.string.player));
            game = new Game(new AI(getString(R.string.computer)),
                    new Human(getString(R.string.player)), rulesSet, this);
        } else {
            p1Text.setText(getString(R.string.player));
            p2Text.setText(getString(R.string.computer));
            game = new Game(new Human(getString(R.string.player)),
                    new AI(getString(R.string.computer)), rulesSet, this);
        }
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
