package com.shadowradar.pro;

public class RadarThread extends Thread {

    private RadarView radarView;
    private boolean running = true;
    private boolean paused = false;
    private final Object pauseLock = new Object();

    public RadarThread(RadarView radarView) {
        this.radarView = radarView;
    }

    @Override
    public void run() {
        while (running) {
            synchronized (pauseLock) {
                while (paused) {
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }

            radarView.updateTargets();
            radarView.postInvalidate();

            try {
                Thread.sleep(16); // ~60 FPS
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    public void pause() {
        synchronized (pauseLock) {
            paused = true;
        }
    }

    public void resume() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll();
        }
    }

    public void stopThread() {
        running = false;
        resume(); // Ensure we can exit if paused
    }
}