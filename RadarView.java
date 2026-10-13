package com.shadowradar.pro;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RadarView extends View {

    private RadarThread radarThread;
    private SettingsManager settings;
    private SoundManager soundManager;
    private Paint radarPaint, gridPaint, targetPaint, crosshairPaint, textPaint;
    private float radarCenterX, radarCenterY, radarRadius;
    private float sweepAngle = 0;
    private List<RadarTarget> targets = new ArrayList<>();
    private Random random = new Random();
    private long frameCount = 0;
    private long lastFpsTime = 0;
    private int fps = 0;
    private boolean controlPanelVisible = true;
    private boolean radarRunning = true;
    private boolean showCrosshair = true;
    private boolean soundEnabled = true;
    private int targetCount = 8;

    // Control panel buttons (simulated)
    private RectF btnRadarToggle, btnCrosshairToggle, btnRefresh, btnTargetCount,
                   btnSoundToggle, btnReset, btnHidePanel;

    public RadarView(Context context, SettingsManager settingsManager, SoundManager soundMgr) {
        super(context);
        this.settings = settingsManager;
        this.soundManager = soundMgr;

        loadSettings();
        initializePaints();
        generateTargets();

        radarThread = new RadarThread(this);
        radarThread.start();
    }

    private void loadSettings() {
        radarRunning = settings.getBooleanSetting("radarRunning", true);
        showCrosshair = settings.getBooleanSetting("showCrosshair", true);
        soundEnabled = settings.getBooleanSetting("soundEnabled", true);
        targetCount = settings.getIntSetting("targetCount", 8);
        controlPanelVisible = settings.getBooleanSetting("controlPanelVisible", true);
    }

    private void initializePaints() {
        radarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        radarPaint.setColor(0xFF00FF00); // Neon green
        radarPaint.setStyle(Paint.Style.STROKE);
        radarPaint.setStrokeWidth(2f);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(0x4400FF00);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1f);

        targetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        targetPaint.setColor(0xFFFF0000); // Neon red
        targetPaint.setStyle(Paint.Style.FILL);

        crosshairPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        crosshairPaint.setColor(0xFFFF00FF); // Magenta
        crosshairPaint.setStyle(Paint.Style.STROKE);
        crosshairPaint.setStrokeWidth(2f);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFF00FF00);
        textPaint.setTextSize(16f);
        textPaint.setTypeface(android.graphics.Typeface.MONOSPACE);
    }

    private void generateTargets() {
        targets.clear();
        for (int i = 0; i < targetCount; i++) {
            float angle = random.nextFloat() * 360f;
            float distance = random.nextFloat() * (radarRadius - 20f);
            float x = radarCenterX + (float) (distance * Math.cos(Math.toRadians(angle)));
            float y = radarCenterY + (float) (distance * Math.sin(Math.toRadians(angle)));
            float vx = (random.nextFloat() - 0.5f) * 4f;
            float vy = (random.nextFloat() - 0.5f) * 4f;
            targets.add(new RadarTarget(x, y, vx, vy, 6f));
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        radarCenterX = w / 2f;
        radarCenterY = h * 0.4f; // Radar positioned in upper portion
        radarRadius = Math.min(w, h) * 0.25f;

        // Define control panel button areas
        float panelY = h * 0.7f;
        float btnWidth = w * 0.18f;
        float btnHeight = 50f;
        float spacing = 10f;
        float startX = 10f;

        btnRadarToggle = new RectF(startX, panelY, startX + btnWidth, panelY + btnHeight);
        btnCrosshairToggle = new RectF(startX + btnWidth + spacing, panelY,
                startX + 2 * btnWidth + spacing, panelY + btnHeight);
        btnRefresh = new RectF(startX + 2 * (btnWidth + spacing), panelY,
                startX + 3 * btnWidth + 2 * spacing, panelY + btnHeight);
        btnTargetCount = new RectF(startX + 3 * (btnWidth + spacing), panelY,
                startX + 4 * btnWidth + 3 * spacing, panelY + btnHeight);
        btnSoundToggle = new RectF(startX + 4 * (btnWidth + spacing), panelY,
                startX + 5 * btnWidth + 4 * spacing, panelY + btnHeight);
        btnReset = new RectF(startX, panelY + btnHeight + spacing, startX + btnWidth,
                panelY + 2 * btnHeight + spacing);
        btnHidePanel = new RectF(startX + btnWidth + spacing, panelY + btnHeight + spacing,
                startX + 2 * btnWidth + spacing, panelY + 2 * btnHeight + spacing);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw radar background
        canvas.drawCircle(radarCenterX, radarCenterY, radarRadius, gridPaint);

        // Draw concentric rings
        for (int i = 1; i <= 4; i++) {
            canvas.drawCircle(radarCenterX, radarCenterY, radarRadius * i / 4, gridPaint);
        }

        // Draw sweep if radar is running
        if (radarRunning) {
            sweepAngle = (sweepAngle + 3f) % 360f;
            float sweepX1 = radarCenterX;
            float sweepY1 = radarCenterY;
            float sweepX2 = radarCenterX + (float) (radarRadius * Math.cos(Math.toRadians(sweepAngle)));
            float sweepY2 = radarCenterY + (float) (radarRadius * Math.sin(Math.toRadians(sweepAngle)));
            canvas.drawLine(sweepX1, sweepY1, sweepX2, sweepY2, radarPaint);
        }

        // Draw targets
        for (RadarTarget target : targets) {
            canvas.drawCircle(target.x, target.y, target.radius, targetPaint);
        }

        // Draw center crosshair
        if (showCrosshair && radarRunning) {
            float crossSize = 15f;
            canvas.drawLine(radarCenterX - crossSize, radarCenterY,
                    radarCenterX + crossSize, radarCenterY, crosshairPaint);
            canvas.drawLine(radarCenterX, radarCenterY - crossSize,
                    radarCenterX, radarCenterY + crossSize, crosshairPaint);
        }

        // Draw FPS counter
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFpsTime >= 1000) {
            fps = (int) frameCount;
            frameCount = 0;
            lastFpsTime = currentTime;
        }
        frameCount++;
        canvas.drawText("FPS: " + fps, 20, 40, textPaint);

        // Draw control panel if visible
        if (controlPanelVisible) {
            drawControlPanel(canvas);
        } else {
            // Draw small button to show panel
            canvas.drawText("[+]", 20, getHeight() - 20, textPaint);
        }
    }

    private void drawControlPanel(Canvas canvas) {
        Paint btnPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        btnPaint.setColor(0xFF1A1A2E);
        btnPaint.setStyle(Paint.Style.FILL);

        Paint btnBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
        btnBorder.setColor(0xFF00FF00);
        btnBorder.setStyle(Paint.Style.STROKE);
        btnBorder.setStrokeWidth(2f);

        Paint btnText = new Paint(Paint.ANTI_ALIAS_FLAG);
        btnText.setColor(0xFF00FF00);
        btnText.setTextSize(12f);
        btnText.setTypeface(android.graphics.Typeface.MONOSPACE);

        // Draw buttons
        canvas.drawRect(btnRadarToggle, btnPaint);
        canvas.drawRect(btnRadarToggle, btnBorder);
        canvas.drawText(radarRunning ? "RADAR: ON" : "RADAR: OFF", btnRadarToggle.left + 5,
                btnRadarToggle.top + 30, btnText);

        canvas.drawRect(btnCrosshairToggle, btnPaint);
        canvas.drawRect(btnCrosshairToggle, btnBorder);
        canvas.drawText(showCrosshair ? "CROSS: ON" : "CROSS: OFF", btnCrosshairToggle.left + 5,
                btnCrosshairToggle.top + 30, btnText);

        canvas.drawRect(btnRefresh, btnPaint);
        canvas.drawRect(btnRefresh, btnBorder);
        canvas.drawText("REFRESH", btnRefresh.left + 5, btnRefresh.top + 30, btnText);

        canvas.drawRect(btnTargetCount, btnPaint);
        canvas.drawRect(btnTargetCount, btnBorder);
        canvas.drawText("TARGETS: " + targetCount, btnTargetCount.left + 5,
                btnTargetCount.top + 30, btnText);

        canvas.drawRect(btnSoundToggle, btnPaint);
        canvas.drawRect(btnSoundToggle, btnBorder);
        canvas.drawText(soundEnabled ? "SOUND: ON" : "SOUND: OFF", btnSoundToggle.left + 5,
                btnSoundToggle.top + 30, btnText);

        canvas.drawRect(btnReset, btnPaint);
        canvas.drawRect(btnReset, btnBorder);
        canvas.drawText("RESET", btnReset.left + 5, btnReset.top + 30, btnText);

        canvas.drawRect(btnHidePanel, btnPaint);
        canvas.drawRect(btnHidePanel, btnBorder);
        canvas.drawText("HIDE", btnHidePanel.left + 5, btnHidePanel.top + 30, btnText);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) {
            return true;
        }

        float x = event.getX();
        float y = event.getY();

        if (!controlPanelVisible) {
            if (x < 50 && y > getHeight() - 50) {
                controlPanelVisible = true;
                invalidate();
                return true;
            }
        }

        if (btnRadarToggle != null && btnRadarToggle.contains(x, y)) {
            radarRunning = !radarRunning;
            settings.setBooleanSetting("radarRunning", radarRunning);
            if (soundEnabled) soundManager.playClick();
            invalidate();
            return true;
        }

        if (btnCrosshairToggle != null && btnCrosshairToggle.contains(x, y)) {
            showCrosshair = !showCrosshair;
            settings.setBooleanSetting("showCrosshair", showCrosshair);
            if (soundEnabled) soundManager.playClick();
            invalidate();
            return true;
        }

        if (btnRefresh != null && btnRefresh.contains(x, y)) {
            generateTargets();
            if (soundEnabled) soundManager.playClick();
            invalidate();
            return true;
        }

        if (btnTargetCount != null && btnTargetCount.contains(x, y)) {
            if (targetCount == 4) targetCount = 8;
            else if (targetCount == 8) targetCount = 12;
            else targetCount = 4;
            settings.setIntSetting("targetCount", targetCount);
            generateTargets();
            if (soundEnabled) soundManager.playClick();
            invalidate();
            return true;
        }

        if (btnSoundToggle != null && btnSoundToggle.contains(x, y)) {
            soundEnabled = !soundEnabled;
            settings.setBooleanSetting("soundEnabled", soundEnabled);
            if (soundEnabled) soundManager.playClick();
            invalidate();
            return true;
        }

        if (btnReset != null && btnReset.contains(x, y)) {
            radarRunning = true;
            showCrosshair = true;
            soundEnabled = true;
            targetCount = 8;
            controlPanelVisible = true;
            settings.reset();
            generateTargets();
            if (soundEnabled) soundManager.playClick();
            invalidate();
            return true;
        }

        if (btnHidePanel != null && btnHidePanel.contains(x, y)) {
            controlPanelVisible = false;
            if (soundEnabled) soundManager.playClick();
            invalidate();
            return true;
        }

        return true;
    }

    public void updateTargets() {
        for (RadarTarget target : targets) {
            target.x += target.vx;
            target.y += target.vy;

            // Bounce off radar boundaries
            if (target.x - target.radius < radarCenterX - radarRadius ||
                target.x + target.radius > radarCenterX + radarRadius) {
                target.vx *= -1;
            }
            if (target.y - target.radius < radarCenterY - radarRadius ||
                target.y + target.radius > radarCenterY + radarRadius) {
                target.vy *= -1;
            }

            // Clamp to radar area
            target.x = Math.max(radarCenterX - radarRadius + target.radius,
                    Math.min(radarCenterX + radarRadius - target.radius, target.x));
            target.y = Math.max(radarCenterY - radarRadius + target.radius,
                    Math.min(radarCenterY + radarRadius - target.radius, target.y));
        }
    }

    public void pause() {
        if (radarThread != null) {
            radarThread.pause();
        }
    }

    public void resume() {
        if (radarThread != null) {
            radarThread.resume();
        }
    }

    public void stop() {
        if (radarThread != null) {
            radarThread.stopThread();
            try {
                radarThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class RadarTarget {
        float x, y, vx, vy, radius;

        RadarTarget(float x, float y, float vx, float vy, float radius) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.radius = radius;
        }
    }
}