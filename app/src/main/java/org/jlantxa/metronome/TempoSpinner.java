package org.jlantxa.metronome;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Spinner;

import java.util.Objects;


class TempoSpinner extends Spinner
{
    public TempoSpinner(Context context) {
        super(context);
    }

    public TempoSpinner(Context context, int mode) {
        super(context, mode);
    }

    public TempoSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TempoSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TempoSpinner(Context context, AttributeSet attrs, int defStyleAttr, int mode) {
        super(context, attrs, defStyleAttr, mode);
    }

    public TempoSpinner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int mode) {
        super(context, attrs, defStyleAttr, defStyleRes, mode);
    }

    @Override
    public void setSelection(int position) {
        boolean sameSelected = position == getSelectedItemPosition();
        super.setSelection(position);
        if (sameSelected) {
            Objects.requireNonNull(getOnItemSelectedListener()).onItemSelected(this, getSelectedView(), position, getSelectedItemId());
        }
    }

    @Override
    public void setSelection(int position, boolean animate) {
        boolean sameSelected = position == getSelectedItemPosition();
        super.setSelection(position, animate);
        if (sameSelected) {
            Objects.requireNonNull(getOnItemSelectedListener()).onItemSelected(this, getSelectedView(), position, getSelectedItemId());
        }
    }
}