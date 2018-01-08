package org.jlantxa.metronome;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

class Metronome implements Runnable
{
    private final int sampleRate;
    private AudioTrack audioTrack;

    private int bpm;
    private boolean isRunning;

    private boolean isAccented;

    private int beat;
    private int currentBeat;

    private final byte[] accentBeatSamples;
    private final byte[] plainBeatSamples;
    private byte[] accentBeatSilenceSamples;
    private byte[] plainBeatSilenceSamples;

    public Metronome(int sampleRate, byte[] accentBeatSamples, byte[] plainBeatSamples) {
        this.sampleRate = sampleRate;
        this.accentBeatSamples = accentBeatSamples;
        this.plainBeatSamples = plainBeatSamples;

        initTrack();

        setRunning(false);
        setBPM(100);
        setBeat(4);
        setAccent(false);
        resetBeat();
    }

    private void resetBeat() {
        currentBeat = 1;
    }

    public void setBPM(int bpm) {
        this.bpm = bpm;
        makeSilence();
    }

    public int getBPM() {
        return bpm;
    }

    public void setBeat(int b) {
        beat = b;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setAccent(boolean accentOn) {
        isAccented = accentOn;
    }

    public boolean isAccented() {
        return isAccented;
    }

    public void setRunning(boolean on) {
        this.isRunning = on;
    }

    private void makeSilence() {
        // The tick arrays have two bytes per audio sample
        int accentSilenceLength = 2 *(int) (((60.0 / bpm) * sampleRate) - accentBeatSamples.length/2);
        int plainSilenceLength = 2 * (int) (((60.0 / bpm) * sampleRate) - plainBeatSamples.length/2);

        // New array is initialised to zeros
        accentBeatSilenceSamples = new byte[accentSilenceLength];
        //Arrays.fill(accentBeatSilenceSamples, (byte) 0);

        // New array is initialised to zeros
        plainBeatSilenceSamples = new byte[plainSilenceLength];
        //Arrays.fill(plainBeatSilenceSamples, (byte) 0);
    }

    public void run() {
        initTrack();

        while (isRunning) {
            if(currentBeat == 1 && isAccented) {
                writeToTrack(accentBeatSamples);
                writeToTrack(accentBeatSilenceSamples);
            } else {
                writeToTrack(plainBeatSamples);
                writeToTrack(plainBeatSilenceSamples);
            }

            currentBeat++;
            if(currentBeat > beat) {
                resetBeat();
            }
        }

        releaseTrack();
    }

    private void initTrack(){
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, sampleRate,
                AudioTrack.MODE_STREAM);
        audioTrack.play();
    }

    private void releaseTrack() {
        audioTrack.stop();
        audioTrack.release();
    }

    private void writeToTrack(byte[] pcm16Samples) {
        audioTrack.write(pcm16Samples, 0, pcm16Samples.length);
    }
}
