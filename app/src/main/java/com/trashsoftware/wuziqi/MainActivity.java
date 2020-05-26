package com.trashsoftware.wuziqi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;

import com.trashsoftware.wuziqi.programs.RulesSet;

public class MainActivity extends AppCompatActivity {

    final static String OVERLINES_KEY = "overlines";
    final static String PVE_KEY = "isPve";
    final static String AI_FIRST_KEY = "aiFirst";
    final static String DIFFICULTY_KEY = "difficulty";
    final static String EVE_LEVELS_KEY = "aiLevels";

    private Spinner overlinesSpinner;  // 长连选项
    private Switch aiFirstSwitch;
    private SeekBar difficultyBar, ai1LevelBar, ai2LevelBar;
    private Button pveButton, eveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        aiFirstSwitch = findViewById(R.id.aiFirstSwitch);
        difficultyBar = findViewById(R.id.difficultyBar);
        ai1LevelBar = findViewById(R.id.ai1Level);
        ai2LevelBar = findViewById(R.id.ai2Level);
        pveButton = findViewById(R.id.pveButton);
        eveButton = findViewById(R.id.eveButton);

        overlinesSpinner = findViewById(R.id.overlinesSpinner);
        overlinesSpinner.setSelection(RulesSet.OVERLINES_WINNING);
        overlinesSpinner.setOnItemSelectedListener(new OverlinesSelectionListener());
    }

    public void onPveButtonClicked(View view) {
        showGameView(RulesSet.PVE);
    }

    public void onPvpButtonClicked(View view) {
        showGameView(RulesSet.PVP);
    }

    public void onEveButtonClicked(View view) {
        showGameView(RulesSet.EVE);
    }

    private void showGameView(int gameMode) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(PVE_KEY, gameMode);
        intent.putExtra(OVERLINES_KEY, getOverlinesSelection());
        intent.putExtra(AI_FIRST_KEY, isAiFirst());
        intent.putExtra(DIFFICULTY_KEY, getDifficultyLevel());
        intent.putExtra(EVE_LEVELS_KEY, getTwoAiLevels());
        startActivity(intent);
    }

    private int getOverlinesSelection() {
        return overlinesSpinner.getSelectedItemPosition();
    }

    private boolean isAiFirst() {
        return aiFirstSwitch.isChecked();
    }

    private int getDifficultyLevel() {
        return difficultyBar.getProgress();
    }

    private int[] getTwoAiLevels() {
        return new int[]{ai1LevelBar.getProgress(), ai2LevelBar.getProgress()};
    }

    private class OverlinesSelectionListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (position == RulesSet.OVERLINES_WINNING) {
                pveButton.setEnabled(true);
                eveButton.setEnabled(true);
            } else {
                pveButton.setEnabled(false);
                eveButton.setEnabled(false);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }
}
