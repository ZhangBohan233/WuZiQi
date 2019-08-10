package com.trashsoftware.wuziqi;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    final static String OVERLINES_KEY = "overlines";
    final static String PVE_KEY = "isPve";
    final static String AI_FIRST_KEY = "aiFirst";

    private Spinner overlinesSpinner;
    private Switch aiFirstSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        overlinesSpinner = findViewById(R.id.overlinesSpinner);
        aiFirstSwitch = findViewById(R.id.aiFirstSwitch);
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
        startActivity(intent);
    }

    private int getOverlinesSelection() {
        return overlinesSpinner.getSelectedItemPosition();
    }

    private boolean isAiFirst() {
        return aiFirstSwitch.isChecked();
    }
}
