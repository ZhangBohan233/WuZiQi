package com.trashsoftware.wuziqi.graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.trashsoftware.wuziqi.programs.Game;

import static android.view.MotionEvent.INVALID_POINTER_ID;

public class ChessboardView extends View {

    private Paint backgroundPaint;
    private Paint boardBorderPaint;
    private Paint boardLinePaint;
    private Paint blackChessPaint;
    private Paint whiteChessPaint;

    private int screenWidth;
    private int screenHeight;

    private float upLeftX;
    private float upLeftY;

    private float initialScalar;
    private float scalar = 0;
    private boolean scaled;

    private static final long MAX_CLICK_DURATION = 200;
    private static final int MAX_CLICK_DISTANCE = 15;

    private long startClickTime;

    private static final int BLOCK_SIZE = 48;
    private static final int MIN_SHOWING = 5;

    private float lastTouchX, lastTouchY;
    private float downX, downY;
    private int activePointerId;

//    private boolean started;

    private ScaleGestureDetector scaleDetector;

    private Game game;

    public ChessboardView(Context context) {
        super(context);

        init();
    }

    public ChessboardView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        init();
    }

    public void setGame(Game game) {
        this.game = game;
    }

    private void setScalar() {
        initialScalar = (float) screenWidth / BLOCK_SIZE / 16;
        scalar = initialScalar;
    }

    private void init() {
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.rgb(230, 190, 130));

        boardBorderPaint = new Paint();
        boardBorderPaint.setStrokeWidth(6);
        boardBorderPaint.setColor(Color.BLACK);

        boardLinePaint = new Paint();
        boardLinePaint.setStrokeWidth(4);
        boardLinePaint.setColor(Color.BLACK);

        blackChessPaint = new Paint();
        blackChessPaint.setColor(Color.BLACK);

        whiteChessPaint = new Paint();
        whiteChessPaint.setColor(Color.WHITE);

        scaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        screenWidth = getWidth();
        screenHeight = getHeight();
        if (scalar == 0) {
            setScalar();
            restoreOutBounds();
        }
        drawChessboard(canvas);
        drawAllChess(canvas);
        drawLastChessHighlight(canvas);
    }

    private void drawChessboard(Canvas canvas) {
        final float blockSize = BLOCK_SIZE * scalar;
        final float firstLineX = upLeftX + blockSize;
        final float firstLineY = upLeftY + blockSize;
        final float right = firstLineX + blockSize * 14;
        final float down = firstLineY + blockSize * 14;

        canvas.drawRect(upLeftX, upLeftY, right + blockSize, down + blockSize, backgroundPaint);

        canvas.drawLine(firstLineX, firstLineY, firstLineX, down, boardBorderPaint);
        canvas.drawLine(firstLineX, firstLineY, right, firstLineY, boardBorderPaint);
        canvas.drawLine(right, firstLineY, right, down, boardBorderPaint);
        canvas.drawLine(firstLineX, down, right, down, boardBorderPaint);

        for (int x = 1; x < 14; x++) {
            final float vPos = firstLineY + blockSize * x;
            final float hPos = firstLineX + blockSize * x;
            canvas.drawLine(firstLineX, vPos, right, vPos, boardLinePaint);  // h line
            canvas.drawLine(hPos, firstLineY, hPos, down, boardLinePaint);  // v line
        }
    }

    private void drawAllChess(Canvas canvas) {
        for (int r = 0; r < 15; r++) {
            for (int c = 0; c < 15; c++) {
                if (game.getChessAt(r, c) == 1) {
                    drawChess(r, c, canvas, blackChessPaint);
                } else if (game.getChessAt(r, c) == 2) {
                    drawChess(r, c, canvas, whiteChessPaint);
                }
            }
        }
    }

    private void drawChess(int r, int c, Canvas canvas, Paint paint) {
        float[] yx = getPosByRowCol(r + 1, c + 1);
        float radius = BLOCK_SIZE * scalar * 0.4f;
        float left = yx[1] - radius;
        float right = yx[1] + radius;
        float top = yx[0] - radius;
        float bottom = yx[0] + radius;
        canvas.drawOval(left, top, right, bottom, paint);
    }

    private void drawLastChessHighlight(Canvas canvas) {
        if (game.getLastChessPlayer() == 1) {
            drawHighlight(game.getLastChessR(), game.getLastChessC(), canvas, whiteChessPaint);
        } else if (game.getLastChessPlayer() == 2) {
            drawHighlight(game.getLastChessR(), game.getLastChessC(), canvas, blackChessPaint);
        }
    }

    private void drawHighlight(int r, int c, Canvas canvas, Paint paint) {
        float[] yx = getPosByRowCol(r + 1, c + 1);
        float radius = BLOCK_SIZE * scalar * 0.15f;
        float left = yx[1] - radius;
        float right = yx[1] + radius;
        float top = yx[0] - radius;
        float bottom = yx[0] + radius;
        canvas.drawOval(left, top, right, bottom, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);

        int pointerIndex;
        float x;
        float y;

        if (scaled) {
            scaled = false;
            pointerIndex = event.getActionIndex();
            lastTouchX = event.getX(pointerIndex);
            lastTouchY = event.getY(pointerIndex);
            return true;
        }

        int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                startClickTime = System.currentTimeMillis();

                pointerIndex = event.getActionIndex();
                x = event.getX(pointerIndex);
                y = event.getY(pointerIndex);

                lastTouchX = x;
                lastTouchY = y;
                downX = x;
                downY = y;

                activePointerId = event.getPointerId(0);

//                performClick();
                break;
            case MotionEvent.ACTION_MOVE:
                pointerIndex = event.findPointerIndex(activePointerId);

                x = event.getX(pointerIndex);
                y = event.getY(pointerIndex);

                final float dx = x - lastTouchX;
                final float dy = y - lastTouchY;

                upLeftX += dx;
                upLeftY += dy;

                restoreOutBounds();

                invalidate();

                lastTouchX = x;
                lastTouchY = y;
                break;
            case MotionEvent.ACTION_UP:
//                game.getMatrix().releaseAll();
                pointerIndex = event.findPointerIndex(activePointerId);

                x = event.getX(pointerIndex);
                y = event.getY(pointerIndex);

                // TODO: Bug
                if (System.currentTimeMillis() - startClickTime < MAX_CLICK_DURATION &&
                        Math.abs(x - downX) < MAX_CLICK_DISTANCE &&
                        Math.abs(y - downY) < MAX_CLICK_DISTANCE) {
                    performClick();
                } else {
                    invalidate();
                }

                activePointerId = INVALID_POINTER_ID;
                break;
            case MotionEvent.ACTION_CANCEL:
//                game.getMatrix().releaseAll();
                activePointerId = INVALID_POINTER_ID;
//                System.out.println(123);
                break;
            case MotionEvent.ACTION_POINTER_UP:
//                System.out.println(456);
//                game.getMatrix().releaseAll();
                pointerIndex = event.getActionIndex();
                final int pointerId = event.getPointerId(pointerIndex);
                if (pointerId == activePointerId) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    lastTouchX = event.getX(newPointerIndex);
                    lastTouchY = event.getY(newPointerIndex);
                    activePointerId = event.getPointerId(newPointerIndex);
                }
                break;
            default:
//                game.getMatrix().releaseAll();
                break;
        }
        return true;
    }

    @Override
    public boolean performClick() {
        int[] rc = getRowColByClickPos(lastTouchX, lastTouchY);
        if (inBound(rc[0], rc[1])) {
            game.playerPlace(rc[0], rc[1]);
            invalidate();
        }
        return super.performClick();
    }

    private boolean inBound(int r, int c) {
        return r >= 0 && r < 15 && c >= 0 && c < 15;
    }

    private int[] getRowColByClickPos(float clickX, float clickY) {
        final float bs = scalar * BLOCK_SIZE;
        int row = (int) ((clickY - upLeftY - bs / 2) / bs);
        int col = (int) ((clickX - upLeftX - bs / 2) / bs);
//        System.out.println(row + " " + col);
        return new int[]{row, col};
    }

    private float[] getPosByRowCol(int r, int c) {
        float y = upLeftY + r * scalar * BLOCK_SIZE;
        float x = upLeftX + c * scalar * BLOCK_SIZE;
        return new float[]{y, x};
    }

    private float preferredUpLeftY() {
        final float calculatedUpLeftY = (float) screenHeight / 2 - BLOCK_SIZE * scalar * 8;
        if (calculatedUpLeftY < 0) return 0;
        else return calculatedUpLeftY;
    }

    private void restoreOutBounds() {
        final float prefULY = preferredUpLeftY();
        if (upLeftY > prefULY) upLeftY = prefULY;
        if (upLeftX > 0) upLeftX = 0;

        float desiredX = getDesiredUpLeftX();
        float desiredY = getDesiredUpLeftY(prefULY);

        if (upLeftX < desiredX) upLeftX = desiredX;
        if (upLeftY < desiredY) upLeftY = desiredY;
    }

    private float getDesiredUpLeftX() {
        return screenWidth - (16 * BLOCK_SIZE * scalar);
    }

    private float getDesiredUpLeftY(float prefUpLeftY) {
        float currentHeightPixel = 16 * BLOCK_SIZE * scalar;
        if (currentHeightPixel < screenHeight) return prefUpLeftY;
        else {
            return screenHeight - currentHeightPixel;
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            scaleFactor = (float) Math.max(0.1, Math.min(10, scaleFactor));
            scalar *= scaleFactor;
            scalar = Math.max(initialScalar,
                    Math.min(scalar, (float) screenWidth / BLOCK_SIZE / MIN_SHOWING));
            final float focusX = detector.getFocusX();
            final float focusY = detector.getFocusY();
            final float focusXOffset = (upLeftX - focusX) * scaleFactor;
            final float focusYOffset = (upLeftY - focusY) * scaleFactor;
            upLeftX = focusX + focusXOffset;
            upLeftY = focusY + focusYOffset;
            scaled = true;

            restoreOutBounds();

            invalidate();
            return true;
        }
    }
}
