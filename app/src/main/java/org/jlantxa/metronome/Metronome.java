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
    private double[] accentBeatSilenceSamples;
    private double[] plainBeatSilenceSamples;

    Metronome(int sampleRate, byte[] accentBeatSamples, byte[] plainBeatSamples) {
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
        int accentSilenceLength = (int) (((60.0 / bpm) * sampleRate) - accentBeatSamples.length);
        int plainSilenceLength = (int) (((60.0 / bpm) * sampleRate) - plainBeatSamples.length);

        accentBeatSilenceSamples = new double[accentSilenceLength];
        for (int i = 0; i < accentSilenceLength; i++) {
            accentBeatSilenceSamples[i] = 0;
        }

        plainBeatSilenceSamples = new double[plainSilenceLength];
        for (int i = 0; i < plainSilenceLength; i++) {
            plainBeatSilenceSamples[i] = 0;
        }
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

    private void writeToTrack(double[] samples) {
        byte[] pcmSamples = new byte[2*samples.length];
        int index = 0;
        for (double sample : samples) {
            short maxSample = (short) ((sample * Short.MAX_VALUE));
            pcmSamples[index++] = (byte) (maxSample & 0x00ff);
            pcmSamples[index++] = (byte) ((maxSample & 0xff00) >>> 8);
        }

        audioTrack.write(pcmSamples, 0, pcmSamples.length);
    }
}
