package fr.polytech.larynxapp.model.analysis;

import java.util.ArrayList;
import java.util.List;

import fr.polytech.larynxapp.model.audio.AudioData;


/**
 * The class calculating fundamentalFreq (fundamental frequency), Jitter and Shimmer of the voice
 */
public class FeaturesCalculator {

	/**
	 * The ending of the area where the next period is to be searched.
	 */
	private int nextPeriodSearchingAreaEnd = (int) hzToPeriod( 40 );

	/**
	 * The list of the position of the maximum of the periods.
	 */
	private List<Integer> pitchPositions;

	/**
	 * The list of the periods' length.
	 */
	private List<Integer> periods;

	/**
	 * The data to analyse.
	 */
	private List<Short> data;

	/**
	 * The fundamental frequency of the data.
	 */
	private float fundamentalFreq;

	/**
	 * The list of pitches (voice frequencies)
	 */
	private List<Float> pitches;


	/**
	 * FeaturesCalculator sole builder.
	 *
	 * @param audioData the audio data containing the data to analyse.
	 */
	public FeaturesCalculator(AudioData audioData, List<Float> pitches ) {
		this.data = audioData.getData_processed();
		this.pitches = pitches;
		periods = new ArrayList<>();
		pitchPositions = new ArrayList<>();
		fundamentalFreq = 0f;

		initPeriodsSearch();

		searchPitchPositions();
	}

	/**
	 * The method initializing the research of the periods.
	 *
	 * Finds the beginning and the ending of the area where the first period is to be searched.
	 * Filters the data into the dataFiltered list.
	 */
	private void initPeriodsSearch() {

		//init fundamentalFreq
		calculatefundamentalFreq();

		//set the first search area
		final double confidenceLevel = 5 / 100.;

		nextPeriodSearchingAreaEnd = (int) Math.floor( hzToPeriod( fundamentalFreq ) * ( 1 + confidenceLevel ) );
	}

	/**
	 * The method calculating the periods in the data and searching the pitches positions.
	 *
	 * Fills the pitchPositions and the periods lists.
	 */
	private void searchPitchPositions() {

		for(float pitch : pitches)
			periods.add((int) hzToPeriod(pitch));

		int periodMaxPitch;
		int periodMaxPitchIndex = 0;
		int periodBeginning     = 0;

		//search each periods maxima
		for ( int period = 0; period < periods.size() - 1; period++ ) {
			periodMaxPitch = 0;

			//search a maximum
			for ( int i = periodBeginning; i < periodBeginning + periods.get( period ); i++ ) {
				if ( periodMaxPitch < data.get( i ) ) {
					periodMaxPitch = data.get( i );
					periodMaxPitchIndex = i;
				}
			}

			periodBeginning += periods.get( period );
			pitchPositions.add( periodMaxPitchIndex );
		}
	}

	/**
	 * Returns the period length of the given value in Hz considering the sample rate (44.1kHz).
	 *
	 * @param hz the value in Hz
	 * @return the equivalent period length
	 */
	private float hzToPeriod(float hz ) {
		int sampling = 44100;
		return sampling / hz;
	}

	// FEATURE NUMBER 1 : SHIMMER

	/**
	 * The method calculating the Shimmer (corresponds to dda in Praat)
	 *
	 * @return the Shimmer
	 */
	public double getShimmer() {
		int           minAmp     = 0;
		int           maxAmp;
		long          amplitudeDiffSum = 0; // sum of difference between every two peak-to-peak amplitudes
		long          amplitudeSum      = 0; // sum of all the peak-to-peak amplitudes
		List<Integer> ampPk2Pk   = new ArrayList<>(); // this list contains all the peak-to-peak amplitudes

		for ( int i = 0; i < pitchPositions.size() - 1; i++ ) {
			// get each pitch
			maxAmp = data.get( pitchPositions.get( i ) );
			for ( int j = pitchPositions.get( i ); j < pitchPositions.get( i + 1 ); j++ ) {
				if ( minAmp > data.get( j ) ) {
					minAmp = data.get( j );
				}
			}
			// add peak-to-peak amplitude into the list
			ampPk2Pk.add( maxAmp - minAmp );
			// reset the min amplitude
			minAmp = 0;
		}

		// SHIMMER FORMULA (RELATIVE)
		for ( int i = 0; i < ampPk2Pk.size() - 1; i++ ) {
			amplitudeDiffSum += Math.abs( ampPk2Pk.get( i ) - ampPk2Pk.get( i + 1 ) );
			amplitudeSum += ampPk2Pk.get( i );
		}
		// add the last peak-to-peak amplitude into sum
		if ( !ampPk2Pk.isEmpty() ) {
			amplitudeSum += ampPk2Pk.get( ampPk2Pk.size() - 1 );
		}
		// calculate shimmer (relative)
		return ( (double) amplitudeDiffSum / periods.size() - 1 ) / ( (double) amplitudeSum / periods.size() );
	}

	// FEATURE NUMBER 2 : JITTER

	/**
	 * The method calculating the Jitter (corresponds to ddp in Praat)
	 *
	 * @return the Jitter
	 */
	public double getJitter() {
		double sumOfDifferenceOfPeriods = 0.0;        // sum of difference between every two periods
		double sumOfPeriods             = 0.0;        // sum of all periods
		double numberOfPeriods          = periods.size();   //set as double for double division

		// JITTER FORMULA (RELATIVE)
		for ( int i = 0; i < periods.size() - 1; i++ ) {
			sumOfDifferenceOfPeriods += Math.abs( periods.get( i ) - periods.get( i + 1 ) );
			sumOfPeriods += periods.get( i );
		}

		// add the last period into sum
		if ( !periods.isEmpty() ) {
			sumOfPeriods += periods.get( periods.size() - 1 );
		}

		double meanPeriod = sumOfPeriods / numberOfPeriods;

		// calculate jitter (relative)
		return ( sumOfDifferenceOfPeriods / ( numberOfPeriods - 1 ) ) / meanPeriod;
	}

	// FEATURE NUMBER 3 : FUNDAMENTAL FREQUENCY

	/**
	 * Getter for the fundamental frequency
	 *
	 * @return the fundamental frequency
	 */
	public double getfundamentalFreq() {
		if ( fundamentalFreq == 0f )
			calculatefundamentalFreq();

		return fundamentalFreq;
	}

	/**
	 * The method finding the fundamental frequency of the data.
	 *
	 * To increase efficiency, this method only test the frequencies between 40Hz to 400Hz.
	 */
	private void calculatefundamentalFreq() {
		int count;
		float f0 = 0;
		for(count = 0; count < pitches.size(); count++)
		{
			f0 += pitches.get(count);
		}
		if(count != 0)
			fundamentalFreq =  f0 / count;
		else
			fundamentalFreq = 0;
	}
}
