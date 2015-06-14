/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import fr.frogdevelopment.nihongo.R;

/**
 * Created by PxL on 15/01/2015.
 */
public class ParameterView extends LinearLayout {

    private TextView mTitle;
    private TextView mSelection;
    private String mCode;

    public ParameterView(Context context) {
        this(context, null);
    }

    public ParameterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ParameterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWillNotDraw(false);
        setOrientation(VERTICAL);
        setClickable(true);

        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.param, this);

        mTitle = (TextView) findViewById(R.id.param_title);
        mSelection = (TextView) findViewById(R.id.param_value);
    }

    public void setTitle(CharSequence title) {
        this.mTitle.setText(title);
    }

    public CharSequence getTitle() {
        return this.mTitle.getText();
    }

    public void setValue(CharSequence value) {
        this.mSelection.setText(value);
    }

    public CharSequence getValue() {
        return this.mSelection.getText();
    }

    public String getCode() {
        return mCode;
    }

    public void setCode(String mCode) {
        this.mCode = mCode;
    }
}
