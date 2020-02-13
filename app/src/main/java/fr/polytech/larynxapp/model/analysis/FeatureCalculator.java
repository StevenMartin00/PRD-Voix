package fr.polytech.larynxapp.model.analysis;

import java.util.ArrayList;
import java.util.List;

import fr.polytech.larynxapp.model.audio.AudioData;

import static java.lang.Math.floor;

/**
 * Class containing the different methods used to calculate the voice features
 */
public class FeatureCalculator
{
    /**
     * The pitches found in recording
     */
    private List<Float> pitches;

    /**
     * The audio data
     */
    private List<Short> data;

    /**
     * The sample rate of the recording
     */
    private int sampleRate;

    /**
     * The list of different periods found
     */
    private List<Double> periods;

    /**
     * The list of pitch positions found
     */
    private List<Integer> pitchesPositions;

    /**
     * FeatureCalculator default builder
     * Used for testing
     */
    public FeatureCalculator()
    {
        this.data = new ArrayList<>();
        this.pitches = new ArrayList<>();
        this.sampleRate = 0;
        this.periods = new ArrayList<>();
        this.pitchesPositions = new ArrayList<>();
    }

    /**
     * FeatureCalculator true builder.
     *
     * @param audioData the audio data containing the data to analyse.
     * @param pitches the list of pitches found in the recording phase
     */
    public FeatureCalculator( AudioData audioData, List<Float> pitches )
    {
        this.data = audioData.getData_processed();
        this.pitches = pitches;
        this.sampleRate = 44100;
        this.periods = new ArrayList<>();
        this.pitchesPositions = new ArrayList<>();
        setPeriods();
        setPitchesPositions();
    }

    /**
     * Calculates the periods
     */
    public void setPeriods()
    {
        double T;
        for(float pitch : pitches)
        {
            T = floor(this.sampleRate / pitch);
            this.periods.add(T);
        }
    }

    /**
     * Calculates the position of each pitch in the audio data
     */
    public void setPitchesPositions()
    {
        int periodMaxPitch;
        int periodMaxPitchIndex = 0;
        int periodBeginning     = 0;

        //search each periods maxima
        for ( int period = 0; period < periods.size(); period++ ) //periods.size() - 1
        {
            periodMaxPitch = 0;

            //search a maximum
            for ( int i = periodBeginning; i < periodBeginning + periods.get( period ); i++ ) {
                if ( periodMaxPitch < data.get( i ) ) {
                    periodMaxPitch = data.get( i );
                    periodMaxPitchIndex = i;
                }
            }

            periodBeginning += periods.get( period );
            pitchesPositions.add( periodMaxPitchIndex );
        }
    }

    /**
     * Calculates the jitter
     * @return the jitter
     */
    public double getJitter()
    {
        double sumOfDifferenceOfPeriods = 0;
        double sumOfPeriods = 0;
        double numberOfPeriods = periods.size();   //set as double for double division

        // JITTER FORMULA (LOCAL)
        for ( int i = 0; i < periods.size() - 1; i++ ) //TODO: test with periods.size()
        {
            sumOfDifferenceOfPeriods += Math.abs( periods.get( i ) - periods.get( i + 1 ) );
            sumOfPeriods += periods.get( i );
        }

        // add the last period into sum
        if ( periods.size() > 0 ) {
            sumOfPeriods += periods.get( periods.size() - 1 );
        }

        double meanPeriod = sumOfPeriods / numberOfPeriods;

        // calculate jitter (relative)
        return ( sumOfDifferenceOfPeriods / ( numberOfPeriods - 1 ) ) / meanPeriod;
    }

    /**
     * Calculates the shimmer
     * @return the shimmer
     */
    public double getShimmer()
    {
        int minAmp = 0;
        int maxAmp;
        long A_diff_sum = 0; // sum of difference between every two peak-to-peak amplitudes
        long A_sum = 0; // sum of all the peak-to-peak amplitudes
        List<Integer> ampPk2Pk   = new ArrayList<>(); // this list contains all the peak-to-peak amplitudes

        for ( int i = 0; i < pitchesPositions.size() - 1; i++ ) //TODO: test with pitchesPositions.size()
        {
            // get each pitch
            maxAmp = data.get( pitchesPositions.get( i ) );
            for ( int j = pitchesPositions.get( i ); j < pitchesPositions.get( i + 1 ); j++ ) {
                if ( minAmp > data.get( j ) ) {
                    minAmp = data.get( j );
                }
            }
            // add peak-to-peak amplitude into the list
            ampPk2Pk.add( maxAmp - minAmp );
            // reset the min amplitude
            minAmp = 0;
        }

        // SHIMMER FORMULA (LOCAL)
        for ( int i = 1; i < ampPk2Pk.size() - 1; i++ ) {
            A_diff_sum += Math.abs( ampPk2Pk.get( i ) - ampPk2Pk.get( i + 1 ) );
            A_sum += ampPk2Pk.get( i );
        }
        // add the last peak-to-peak amplitude into sum
        if ( ampPk2Pk.size() > 0 ) {
            A_sum += ampPk2Pk.get( ampPk2Pk.size() - 1 );
        }
        // calculate shimmer (relative)
        return ( (double) A_diff_sum / (double) ( ampPk2Pk.size() - 1 ) ) / ( (double) A_sum / (double) ampPk2Pk.size() );
    }

    /**
     * Calculates the F0
     * @return the f0
     */
    public double getF0()
    {
        int count;
        float f0 = 0;
        for(count = 0; count < pitches.size(); count++)
        {
            f0 += pitches.get(count);
        }
        return f0 / count;
    }

    public List<Double> getPeriods()
    {
        return this.periods;
    }

    public List<Float> getPitches()
    {
        return this.pitches;
    }

    public List<Integer> getPitchesPositions()
    {
        return this.pitchesPositions;
    }

    public void setSampleRate(int sampleRate)
    {
        this.sampleRate = sampleRate;
    }

    public void setData(List<Short> data)
    {
        this.data = data;
    }

    public void setPitches(List<Float> pitches)
    {
        this.pitches = pitches;
    }

}
