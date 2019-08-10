package com.trashsoftware.wuziqi;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.trashsoftware.wuziqi.programs.AI;
import com.trashsoftware.wuziqi.programs.Game;
import com.trashsoftware.wuziqi.programs.Human;
import com.trashsoftware.wuziqi.programs.Player;
import com.trashsoftware.wuziqi.graphics.ChessboardView;
import com.trashsoftware.wuziqi.graphics.MsDialogFragment;
import com.trashsoftware.wuziqi.programs.RulesSet;

public class GameActivity extends AppCompatActivity {

    private ChessboardView chessboardView;

    private TextView p1Text, p2Text;

    private MsDialogFragment dialogFragment = new MsDialogFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        chessboardView = findViewById(R.id.chessboardView);
        p1Text = findViewById(R.id.p1Text);
        p2Text = findViewById(R.id.p2Text);

        Intent intent = getIntent();
        boolean isPve = intent.getBooleanExtra(MainActivity.PVE_KEY, false);
        RulesSet rulesSet = new RulesSet(
                intent.getIntExtra(MainActivity.OVERLINES_KEY, RulesSet.OVERLINES_WINNING),
                intent.getBooleanExtra(MainActivity.AI_FIRST_KEY, false)
        );
        if (isPve) {
            startGameOnePlayer(rulesSet);
        } else {
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
        String playerName;
        if (winningPlayer.isAi()) playerName = getString(R.string.computer);
        else {
            if (playerNum == 1) playerName = getString(R.string.p1);
            else playerName = getString(R.string.p2);
        }
        String msg = playerName + " " + getString(R.string.winMsg);
        dialogFragment.show(getSupportFragmentManager(), msg);
    }

    private void startGameOnePlayer(RulesSet rulesSet) {
        p1Text.setText(getString(R.string.p1));
        p2Text.setText(getString(R.string.computer));
        setTextStyles(true);
        Game game;
        if (rulesSet.isAiFirst()) {
            game = new Game(new AI(), new Human(), rulesSet, this);
        } else {
            game = new Game(new Human(), new AI(), rulesSet, this);
        }
        chessboardView.setGame(game);
    }

    private void startGameTwoPlayers(RulesSet rulesSet) {
        p1Text.setText(getString(R.string.p1));
        p2Text.setText(getString(R.string.p2));
        setTextStyles(true);
        Game game = new Game(new Human(), new Human(), rulesSet, this);
        chessboardView.setGame(game);
    }

    private void setTextStyles(boolean isP1) {
        if (isP1) {
            p1Text.setTextAppearance(R.style.FocusedText);
            p2Text.setTextAppearance(R.style.UnfocusedText);
        } else {
            p1Text.setTextAppearance(R.style.UnfocusedText);
            p2Text.setTextAppearance(R.style.FocusedText);
        }
    }
}
