package com.example.simplebluetooth;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Model {
    private int mNum;
    private double mDate;
    private double mSample;

    public Model(int mNum, double mDate, double mSample) {
        this.mNum = mNum;
        this.mDate = mDate;
        this.mSample = mSample;
    }

    public String getNum() {
        return String.valueOf(mNum);
    }

    public String getDate() {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date((long) mDate);
        return format.format(date);
    }

    public String getSample() {
        return String.valueOf(mSample);
    }
}
