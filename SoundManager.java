package com.shadowradar.pro;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;

public class SoundManager {

    private ToneGenerator toneGenerator;

    public SoundManager(Context context) {
        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
    }

    public void playClick() {
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 50);
    }

    public void release() {
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
    }
}