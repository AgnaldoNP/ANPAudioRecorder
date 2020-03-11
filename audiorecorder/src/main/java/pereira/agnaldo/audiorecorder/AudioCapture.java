package pereira.agnaldo.audiorecorder;

import android.media.audiofx.Visualizer;
import android.util.Log;

public class AudioCapture {

    private Visualizer mVisualizer;
    private PeakListener mPeakListener;
    private SoundStartListener mSoundStartListener;
    private SoundStopListener mSoundStopListener;

    private float[] mPeaks = null;
    private int mOutPeakCount = 1;
    private static final int SAMPLE = 40;

    private final static int TIME_STOP_SOUND_THRESHOLD = 1200;
    private long mEndAudioMillisCandidate = -1;
    private float mAvgPeaks = -1;
    private boolean mIsSoundPlaying = true;

    public AudioCapture() {
        this(0);
    }

    public AudioCapture(final int audioSessionId) {
        try {
            final int[] range = Visualizer.getCaptureSizeRange();
            final int size = range[0];

            mVisualizer = new Visualizer(audioSessionId);
            if (mVisualizer.getEnabled()) {
                mVisualizer.setEnabled(false);
            }

            mVisualizer.setCaptureSize(size);
            mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
                @Override
                public void onWaveFormDataCapture(
                        final Visualizer visualizer, final byte[] bytes, final int samplingRate) {
                }

                @Override
                public void onFftDataCapture(
                        final Visualizer visualizer, final byte[] bytes, final int samplingRate) {
                    processAudioData(bytes);
                    if(fftListener != null){
                        fftListener.onAudioCaptured(bytes);
                    }
                }
            }, Visualizer.getMaxCaptureRate(), false, true);
        } catch (Exception e) {
            Log.e("AudioCapture", "Visualizer Exception", e);
        }
    }

    public void setPeakListener(PeakListener mPeakListener) {
        this.mPeakListener = mPeakListener;
    }

    public void setSoundStartListener(final SoundStartListener mSoundStartListener) {
        this.mSoundStartListener = mSoundStartListener;
    }

    public void setSoundStopListener(final SoundStopListener mSoundStopListener) {
        this.mSoundStopListener = mSoundStopListener;
    }

    public void processAudioData(final byte[] bytes) {
        if (mVisualizer == null || mPeakListener == null)
            return;

        if (mPeaks == null) {
            mPeaks = new float[mOutPeakCount];
        }

        final int sampleCount = SAMPLE / mPeaks.length;

        for (int i = 0; i < mPeaks.length; i++) {
            final int from = sampleCount * i;
            final int until = from + sampleCount;

            double rms = 0;
            for (int j = from; j < until; j++) {
                if (bytes.length > j) {
                    final byte aByte = bytes[j];
                    rms += Math.pow(aByte, 2);
                }
            }
            rms = Math.sqrt(rms / sampleCount);

            final float mAlpha = 0.45f;
            mPeaks[i] = (float) (mPeaks[i] * mAlpha + (1 - mAlpha) * rms);
        }

        if (mPeakListener != null) {
            mPeakListener.onPeakChange(mPeaks);
        }

        checkSoundState(mPeaks);
    }

    private void checkSoundState(float[] peaks) {
        final float avgPeak = getAverage(peaks);

        if (mAvgPeaks == -1) {
            mAvgPeaks = avgPeak;
            return;
        } else {
            mAvgPeaks = (mAvgPeaks + avgPeak) / 2;
        }

        if (mAvgPeaks < 0.01) {
            if (mEndAudioMillisCandidate == -1) {
                mEndAudioMillisCandidate = System.currentTimeMillis();
            }

            final long now = System.currentTimeMillis();
            final long diff = now - mEndAudioMillisCandidate;
            if (diff >= TIME_STOP_SOUND_THRESHOLD && mIsSoundPlaying) {
                mIsSoundPlaying = false;
                onSoundStop();
            }
        } else {
            mEndAudioMillisCandidate = -1;

            if (!mIsSoundPlaying) {
                mIsSoundPlaying = true;
                onSoundStart();
            }
        }
    }

    private float getAverage(final float[] peaks) {
        float avg = peaks[0];
        for (int i = 0; i < peaks.length; i++) {
            avg = (avg + peaks[i]) / 2;
        }
        return avg;
    }

    private void onSoundStart() {
        Log.d("ANP__", "com audio :)");
        if (mSoundStartListener != null) {
            mSoundStartListener.onSoundStart();
        }
    }

    private void onSoundStop() {
        Log.d("ANP__", "sem audio :)");
        if (mSoundStopListener != null) {
            mSoundStopListener.onSoundStop();
        }

    }

    public void start() {
        if (mVisualizer != null) {
            try {
                if (!mVisualizer.getEnabled()) {
                    mVisualizer.setEnabled(true);
                }
            } catch (Exception e) {
                Log.e("AudioCapture", "start() Exception", e);
            }
        }
    }

    public void stop() {
        if (mVisualizer != null) {
            try {
                if (mVisualizer.getEnabled()) {
                    mVisualizer.setEnabled(false);
                }
            } catch (Exception e) {
                Log.e("AudioCapture", "stop() Exception", e);
            }
        }
    }

    public void release() {
        if (mVisualizer != null) {
            mVisualizer.release();
            mVisualizer = null;
        }
    }

    private FFTListener fftListener;
    public void setFftListener(FFTListener fftListener) {
        this.fftListener = fftListener;
    }

    public interface PeakListener {
        void onPeakChange(final float[] peaks);
    }

    public interface SoundStartListener {
        void onSoundStart();
    }

    public interface SoundStopListener {
        void onSoundStop();
    }

    public interface FFTListener {
        void onAudioCaptured(byte[] bytes);
    }

}