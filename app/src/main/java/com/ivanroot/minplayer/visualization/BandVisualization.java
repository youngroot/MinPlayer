package com.ivanroot.minplayer.visualization;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Ivan Root on 22.10.2017.
 */

public class BandVisualization extends BaseVisualization {

    protected boolean useCenterDoubleBands = false;
    protected boolean useDb = false;
    protected boolean useRoundedCorners = false;
    protected int bandsAmount = 0;
    protected int backgroundColor = 0;
    protected int startColor = 0;
    protected int endColor = 0;


    @Override
    public BandVisualization setFftData(byte[] fftData) {
        super.setFftData(fftData);
        return this;
    }

    @Override
    public BandVisualization setSamplingRate(int samplingRate){
        super.setSamplingRate(samplingRate);
        return this;
    }

    @Override
    public BandVisualization setWidth(int width) {
        super.setWidth(width);
        return this;
    }

    @Override
    public BandVisualization setHeight(int height) {
        super.setHeight(height);
        return this;
    }


    protected float getNewPeak(float oldPeak, float maxPeak,  float height){
        if(maxPeak <= 0) maxPeak = 1;
        return (useDb ? (float)(20 * Math.log10(oldPeak)) * height / 85f : oldPeak * (height / maxPeak) / 3f);
    }

    @Override
    public synchronized Canvas visualize(Canvas canvas) throws MoreBandsThanDataException {

        List<Float> spectrumData = getSpectrumData();

        if(bandsAmount > spectrumData.size())
            throw new MoreBandsThanDataException(bandsAmount, spectrumData.size());

        float bandWidth = width / bandsAmount;
        float bandHeight;
        float bandSpan = bandWidth / 4; bandWidth -= bandSpan;
        float startX = bandSpan / 2;
        float maxPeak = Collections.max(spectrumData);
        float r = (useRoundedCorners ? bandWidth / 2 : 0);

        Paint paint = new Paint();
        canvas.drawColor(backgroundColor);

        for (int i = 0; i < spectrumData.size(); i++) {

                int height;
                float peak;
                LinearGradient lg;

                if(useCenterDoubleBands){

                    height = this.height / 2;
                    peak = getNewPeak(spectrumData.get(i),maxPeak,height);
                    bandHeight = height + peak;
                    lg = new LinearGradient(0,height,0,this.height,startColor,endColor, Shader.TileMode.CLAMP);
                    paint.setShader(lg);
                    canvas.drawRoundRect(startX, bandHeight, startX + bandWidth, height, r, r, paint);
                }
                else height = this.height;

                peak = getNewPeak(spectrumData.get(i),maxPeak,height);
                bandHeight = height - peak;
                lg = new LinearGradient(0,height,0,0,startColor,endColor, Shader.TileMode.CLAMP);
                paint.setShader(lg);
                canvas.drawRoundRect(startX, bandHeight, startX + bandWidth, height, r, r, paint);
                startX += bandWidth + bandSpan;

        }

        return canvas;
    }

    protected List<Float> getSpectrumData() {

        /*
        * 20 - 60 hz
        * 60 - 250 hz
        * 250 - 500 hz
        * 500 - 2000 hz
        * 2000 - 4000 hz
        * 6000 - 20000 hz
        * */

        List<Float> spectrumData = new ArrayList<>();
        List<Float> mgPoints = getFftMagnitudes();

        int size = mgPoints.size();
        int sampleCount;
        int lastCount = 0;
        float peak;

        for (int i = 0; i < bandsAmount; i++) {

            peak = 0;
            sampleCount = (int) Math.pow(2, i * 9.0 / (bandsAmount - 1));
            Log.i("Freq range", String.valueOf(sampleCount*samplingRate/fftData.length));

            if(sampleCount <= lastCount && lastCount + 1 < size) sampleCount = lastCount + 1;
            if (sampleCount > size - 1) sampleCount = size - 1;

            for (int j = lastCount; j < sampleCount; j++) {

                if(peak < mgPoints.get(j)) peak = mgPoints.get(j);
            }

            spectrumData.add(peak);
            lastCount = sampleCount;
        }

        return spectrumData;
    }

    public BandVisualization setBandsAmount(int bandsAmount){
        this.bandsAmount = bandsAmount;
        return this;
    }

    public BandVisualization setColors(int backgroundColor, int startColor, int endColor){
        this.backgroundColor = backgroundColor;
        this.startColor = startColor;
        this.endColor = endColor;
        return this;
    }

    public BandVisualization useCenterDoubleBands(boolean useCenterDoubleBands){
        this.useCenterDoubleBands = useCenterDoubleBands;
        return this;
    }

    public BandVisualization useDecibels(boolean useDb){
        this.useDb = useDb;
        return this;
    }

    public BandVisualization useRoundedCorners(boolean useRoundedCorners){
        this.useRoundedCorners = useRoundedCorners;
        return this;
    }

    public static class MoreBandsThanDataException extends Exception {

        private String what = "MoreBandsThanDataException";

        @Override
        public String getMessage() {
            return what;
        }

        public MoreBandsThanDataException(){}

        public MoreBandsThanDataException(int errorValue, int maxValue){

            what = "MoreBandsThanDataException: with "
                    + maxValue +
                    " only "
                    + maxValue +
                    " columns available, but the number of columns was "
                    + errorValue;
        }
    }
}
