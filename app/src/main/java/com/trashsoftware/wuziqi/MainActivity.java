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

    private Spinner overlinesSpinner;  // 长连选项
    private Switch aiFirstSwitch;
    private SeekBar difficultyBar;
    private Button pveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        aiFirstSwitch = findViewById(R.id.aiFirstSwitch);
        difficultyBar = findViewById(R.id.difficultyBar);
        pveButton = findViewById(R.id.pveButton);

        overlinesSpinner = findViewById(R.id.overlinesSpinner);
        overlinesSpinner.setSelection(RulesSet.OVERLINES_WINNING);
        overlinesSpinner.setOnItemSelectedListener(new OverlinesSelectionListener());
    }

    public void onPveButtonClicked(View view) {
        showGameView(true);
    }

    public void onPvpButtonClicked(View view) {
        showGameView(false);
    }

    private void showGameView(boolean isPve) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(PVE_KEY, isPve);
        intent.putExtra(OVERLINES_KEY, getOverlinesSelection());
        intent.putExtra(AI_FIRST_KEY, isAiFirst());
        intent.putExtra(DIFFICULTY_KEY, getDifficultyLevel());
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

    private class OverlinesSelectionListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (position == RulesSet.OVERLINES_WINNING) {
                pveButton.setEnabled(true);
            } else {
                pveButton.setEnabled(false);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }
}
