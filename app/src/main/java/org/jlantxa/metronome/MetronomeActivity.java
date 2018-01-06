package org.jlantxa.metronome;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

enum Tempo {
    Largo,
    Lento,
    Larghetto,
    Adagio,
    Adagietto,
    Andante,
    Moderato,
    Allegro,
    Vivace,
}

public class MetronomeActivity extends Activity
{
    private static final int BEAT_DEFAULT = 2;
    private static final int[] beats = {2, 3, 4, 5, 7};
    private static int currentBeatIndex;

    private static final int BPM_MAX = 240;
    private static final int BPM_MIN = 30;

    private Metronome metronome;
    private Thread metronomeThread;

    private TextView bpmView;
    private Button beatButton;
    private SeekBar bpmSeekBar;
    private ToggleButton muteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metronome);

        // Init GUI controls
        bpmView = findViewById(R.id.bpmView);
        muteButton = findViewById(R.id.muteButton);
        bpmSeekBar = findViewById(R.id.bpmSeekBar);
        Switch accentSwitch = findViewById(R.id.accentSwitch);
        beatButton = findViewById(R.id.beatButton);
        TempoSpinner tempoSpinner = findViewById(R.id.tempoSpinner);
        tempoSpinner.setAdapter(getTempoItems());
        tempoSpinner.setSelection(Tempo.Andante.ordinal());

        // Create metronome
        InputStream tickFileIS = getResources().openRawResource(R.raw.tick);
        InputStream tockFileIS = getResources().openRawResource(R.raw.tock);
        byte[] accentSignal = new byte[0];
        byte[] beatSignal = new byte[0];
        try {
            accentSignal = getPCMBytesFromInputStream(tickFileIS);
            beatSignal = getPCMBytesFromInputStream(tockFileIS);
        } catch (IOException e) {
            e.printStackTrace();
        }
        metronome = new Metronome(48000, accentSignal, beatSignal);
        metronome.setBPM(100);
        metronome.setAccent(false);

        // Set default beat
        currentBeatIndex = BEAT_DEFAULT;
        int beat = beats[currentBeatIndex];
        metronome.setBeat(beat);
        beatButton.setText(String.valueOf(beat));

        // Metronome thread
        metronomeThread = new Thread(metronome);

        // Widgets listeners
        tempoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                setTempoByName(pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        accentSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean state) {
                metronome.setAccent(state);
            }
        });

        muteButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean on) {
                metronome.setRunning(on);

                if (on) {
                    metronomeThread.start();
                }
            }
        });

        bpmSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setShowBPM(progress + BPM_MIN);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int bpm = getBPMSeekBar();
                updateBPM(bpm);
            }
        });

        // Update controls
        bpmSeekBar.setMax(BPM_MAX - BPM_MIN);
        muteButton.setActivated(metronome.isRunning());
        int defaultBPM = metronome.getBPM();
        updateBPM(defaultBPM);

        accentSwitch.setChecked(metronome.isAccented());
    }

    @Override
    protected void onPause() {
        super.onPause();
        metronome.setRunning(false);
        muteButton.setChecked(false);
    }

    public void onBPMChangeButtonClick(View view) {
        int bpm = metronome.getBPM();

        switch (view.getId()) {
            case R.id.plusButton:
                bpm++;
                break;

            case R.id.minusButton:
                bpm--;
                break;

            default:
                return;
        }

        updateBPM(bpm);
    }

    public void toggleBeat(@SuppressWarnings("unused") View view) {
        currentBeatIndex++;
        if (currentBeatIndex >= beats.length) {
            currentBeatIndex = 0;
        }

        int beat = beats[currentBeatIndex];
        metronome.setBeat(beat);
        beatButton.setText(String.valueOf(beat));
    }

    private void updateBPM(int bpm) {
        int setBPM = bpm;
        if (setBPM <= 0) setBPM = 1;

        metronome.setBPM(setBPM);
        setShowBPM(setBPM);
        setBPMSeekBar(setBPM);
    }

    private void setShowBPM(int bpm) {
        bpmView.setText(String.format("%s %s", String.valueOf(bpm), getString(R.string.bpm)));
    }

    private int getBPMSeekBar() {
        int progress = bpmSeekBar.getProgress();
        return progress + BPM_MIN;
    }

    private void setBPMSeekBar(int bpm) {
        bpmSeekBar.setProgress(bpm - BPM_MIN);
    }

    private byte[] getPCMBytesFromInputStream(InputStream inStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = inStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        return buffer.toByteArray();
    }

    private ArrayAdapter<String> getTempoItems() {
        Tempo[] tempos = Tempo.values();
        String[] tempoStrings = new String[tempos.length];

        for (int t = 0; t < tempos.length; t++) {
            tempoStrings[t] = tempos[t].toString();
        }

        return new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, tempoStrings);
    }

    private void setTempoByName(int itemPos) {
        Tempo selectedTempo = Tempo.values()[itemPos];

        switch (selectedTempo) {
            case Largo:
                updateBPM(50);
                break;

            case Lento:
                updateBPM(53);
                break;

            case Larghetto:
                updateBPM(63);
                break;

            case Adagio:
                updateBPM(71);
                break;

            case Adagietto:
                updateBPM(74);
                break;

            case Andante:
                updateBPM(92);
                break;

            case Moderato:
                updateBPM(114);
                break;

            case Allegro:
                updateBPM(138);
                break;

            case Vivace:
                updateBPM(166);
                break;

            default:
                break;
        }
    }
}
