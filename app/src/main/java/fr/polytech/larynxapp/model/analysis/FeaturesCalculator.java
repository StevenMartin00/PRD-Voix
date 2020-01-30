package fr.polytech.larynxapp.model.analysis;

import android.util.Log;

import fr.polytech.larynxapp.model.audio.AudioData;
import fr.polytech.larynxapp.ui.home.HomeFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tianxue WANG and Wenli YAN
 * @version 2018.0115
 * @date 10/01/2017
 */

/**
 * The class calculating f0 (fundamental frequency), Jitter and Shimmer of the voice
 *
 * WARNING : The Shimmer's calculation has not been verified and could be improved
 */
public class FeaturesCalculator {
	
	/**
	 * The sampling constant of wave file analysed.
	 */
	private final int sampling                   = 44100;
//	private final int sampling                   = 22050;
	
	/**
	 * Initially used to represent the default maximum size of the periods
	 */
	private       int baseFragment               = 200;
	
	/**
	 * Initially used to represent the default minimum size of the periods
	 */
	private       int offset                     = 100;
	
	/**
	 * The beginning of the area where the next period is to be searched.
	 */
	private int           nextPeriodSearchingAreaBeginning;
	
	/**
	 * The ending of the area where the next period is to be searched.
	 */
	private       int nextPeriodSearchingAreaEnd = HzToPeriod( 40 );
	
	/**
	 * The list of the position of the maximum of the periods.
	 */
	private List<Integer> pitchPositions;
	
	/**
	 * The list of the periods' length.
	 */
	private List<Integer> periodsLength;
	
	/**
	 * The class audioData containing the data to analyse.
	 */
	private AudioData audioData;
	
	/**
	 * The data to analyse.
	 */
	private List<Short> data;
	
	/**
	 * The data filtered (with the high and low pass filters).
	 */
	private List<Float> dataFiltered;
	
	/**
	 * The fundamental frequency of the data.
	 */
	private Integer f0;

	/**
	 * The buffer that stores the calculated values. It is exactly half the size
	 * of the input buffer.
	 */
	private List<Short> yinBuffer;

	/**
	 * The actual YIN threshold.
	 */
	private double threshold;
	
	
	
	/**
	 * FeaturesCalculator sole builder.
	 *
	 * @param audioData the audio data containing the data to analyse.
	 */
	public FeaturesCalculator( AudioData audioData ) {
		this.audioData = audioData;
		this.data = audioData.getData_processed();

		periodsLength = new ArrayList<>();
		pitchPositions = new ArrayList<>();
		f0 = null;


//		calculatePositions();
		//TODO: ici ça merde
		initPeriodsSearch();

//		calculatePeriods();
//		zeroCrossingCalculatePeriods();
		autoCorrelationCalculatePeriods();
	}
	
	
	
	
	
	/**
	 * Save the data and the pitchPositions list into a file TXT.
	 *
	 * The data will be saved into "data.txt".
	 * The pitchPositions list into "position.txt".
	 *
	 * Both files will be saved in the record save folder.
	 *
	 * @param numberPoints the maximum position of the points to save
	 */
	public void saveDataFile( int numberPoints ) {
		
		String sdCardDir = HomeFragment.FILE_PATH;
		
		File saveFile         = new File( sdCardDir, "data.txt" );
		File saveFilePosition = new File( sdCardDir, "position.txt" );
		try {
			FileOutputStream outputStream         = new FileOutputStream( saveFile );
			FileOutputStream outputStreamPosition = new FileOutputStream( saveFilePosition );
			for ( int i = 0; i < numberPoints; i++ ) {
				int    input = data.get( i );
				String s     = Integer.toString( input );
				s = s + " ";
				outputStream.write( s.getBytes() );
				
			}
			for ( int position : pitchPositions ) {
				if ( position < numberPoints ) {
					int    y  = data.get( position );
					String ss = String.valueOf( position );
					ss = ss + "," + y  + " ";
					outputStreamPosition.write( ss.getBytes() );
				}
			}
			outputStream.close();
			outputStreamPosition.close();
			
		}
		catch ( IOException e ) {
			e.printStackTrace();
		}
	}
	
	/**
	 * The method calculating the base fragment
	 */
	public void calculatePositionsV1() {
		
		//check if data is valid
		if ( data.size() < 5000 )
			return;
		
		// get the first 5000 points for calculating the periods
		List<Short> dataTmp = new ArrayList<>();
		int         maxTmp  = 0;
		for ( int i = 0; i < 5000; i++ ) {
			dataTmp.add( data.get( i ) );
			if ( maxTmp < data.get( i ) ) {
				maxTmp = data.get( i );
			}
		}
		
		// get the pitch position temporary and the difference between two pitch position
		// set the first condition of select the pitch position : value > 0.9 * maxTmp
		List<Integer> listPositions     = new ArrayList<>();
		List<Short> listPositionValue = new ArrayList<>();
		List<Integer> diffPositions     = new ArrayList<>();
		double        thresholdTop      = maxTmp * 1.1;
		double        thresholdUnder    = maxTmp * 0.90;
		for ( int i = 0; i < 5000; i++ ) {
			if ( dataTmp.get( i ) > thresholdUnder ) {
				listPositions.add( i );
				listPositionValue.add( dataTmp.get( i ) );
			}
		}
		
		//set the second condition of select the pitch position :
		// the difference between two pitch point must in (40,400)
		// because the frequency of the man normal is 100Hz to 1000Hz
		// the frequency of the sampling is 44100Hz
		int diffThresholdUnder = 40;
		int diffThresholdTop   = 400;
		for ( int i = 0; i < listPositions.size() - 1; i++ ) {
			int diff = listPositions.get( i + 1 ) - listPositions.get( i );
			
			if ( diff > diffThresholdUnder && diff < diffThresholdTop ) {
				diffPositions.add( diff );
			}
		}
		
		if ( diffPositions.isEmpty() ) {
			Log.d( "calculatePositions", "The record seams to be just noise" );
			return;
		}
		
		int mean = 0;
		for ( int i = 0; i < diffPositions.size(); i++ ) {
			mean += diffPositions.get( i );
		}
		
		// get the length of the period temporary
		mean = mean / diffPositions.size();
		
		// the length of base_fragment must be greater than a period and less than two period
		this.baseFragment = (int) ( mean * 1.5 );
		this.offset = this.baseFragment / 2;
	}
	
	private void calculatePositions() {
		int nbDataObserved = sampling / 16;
		
		// brutforce Fourier Transform of the signal  !!
		int[] dataReal      = new int[ nbDataObserved / 2 ];
		int[] dataImaginary = new int[ nbDataObserved / 2 ];
		
		
		for ( int frequence = 0; frequence < ( nbDataObserved / 2 ); frequence++ ) {
			
			for ( int i = 0; i < nbDataObserved; i++ ) {
				//System.out.println(i);
				double w = ( frequence * 2 * Math.PI * i ) / nbDataObserved;
				dataReal[ frequence ] += data.get( i ) * Math.cos( w );
				dataImaginary[ frequence ] -= data.get( i ) * Math.sin( w );
				
			}
			
			dataReal[ frequence ] /= ( nbDataObserved / 2 );
			dataImaginary[ frequence ] /= ( nbDataObserved / 2 );
		}
		
		// Fourier Transform of the filter H(t) = alpha*Math.exp(-alpha*t) !!!
		// ==>
		// Re(H(w)) = alpha^2/(alpha^2+w^2)
		// Im(H(w)) = -alpha*w/(alpha^2+w^2)
		double   alpha           = 4.;
		double[] filterReal      = new double[ nbDataObserved / 2 ];
		double[] filterImaginary = new double[ nbDataObserved / 2 ];
		
		for ( int f = 0; f < ( nbDataObserved / 2 ); f++ ) {
			double w = 2 * Math.PI * f;
			filterReal[ f ] = alpha * alpha / ( alpha * alpha + w * w );
			filterImaginary[ f ] = -w * alpha / ( alpha * alpha + w * w );
		}
		
		// dataFiltered=filter*signal (warning: multiplication of two complex) !!!
		int[] dataFilteredReal      = new int[ nbDataObserved / 2 ];
		int[] dataFilteredImaginary = new int[ nbDataObserved / 2 ];
		for ( int frequency = 0; frequency < nbDataObserved / 2; frequency++ ) {
			dataFilteredReal[ frequency ] = (int) ( dataReal[ frequency ] * filterReal[ frequency ] - dataImaginary[ frequency ] * filterImaginary[ frequency ] );
			dataFilteredImaginary[ frequency ] = (int) ( dataReal[ frequency ] * filterImaginary[ frequency ] + dataImaginary[ frequency ] * filterReal[ frequency ] );
		}
		
		// brut-force inverse-Fourier Transform of the dataFiltered (real part only) !!!
		ArrayList<Short> dataFiltered = new ArrayList<>( nbDataObserved );
		
		for ( int frequency = 0; frequency < ( nbDataObserved / 2 ); frequency++ ) {
			
			for ( int i = 0; i < nbDataObserved; i++ ) {
				if ( dataFiltered.size() == i )
					dataFiltered.add( (short) 0 );
				
				
				double phase = ( 2 * Math.PI * i ) / nbDataObserved;
				dataFiltered.set( i, (short) ( dataFiltered.get( i )
											   + dataFilteredReal[ frequency ] * Math.cos( frequency * phase )
											   - dataFilteredImaginary[ frequency ] * Math.sin( frequency * phase ) ) );
			}
		}
		
		
		/* Second Part */
		
		long autoCorrelationMax = 0;
		int  periodRef          = 0;
		long autoCorrelation;
		nextPeriodSearchingAreaEnd = nbDataObserved / 2 - HzToPeriod( 40 );
		
		//check if data is long enough
		if ( data.size() < 2 * HzToPeriod( 40 ) )
			return;
		
		//get the first period of the signal
		for ( int i = HzToPeriod( 400 ); i < HzToPeriod( 40 ); i++ ) {
			autoCorrelation = autoCorrelation( i, 0, dataFiltered );
			System.out.println( "autoCorrelation = " + autoCorrelation );
			
			if ( autoCorrelation > autoCorrelationMax * 1.03 ) {
				autoCorrelationMax = autoCorrelation;
				periodRef = i;
			}
		}
		
		System.out.println( "periodRef = " + periodToHz( periodRef ) );
		
		//set the first search area
		nextPeriodSearchingAreaEnd = (int) ( periodRef * 1.3 );
		nextPeriodSearchingAreaBeginning = (int) ( periodRef * 0.7 );
	}
	
	/**
	 * The method initializing the research of the periods.
	 *
	 * Finds the beginning and the ending of the area where the first period is to be searched.
	 * Filters the data into the dataFiltered list.
	 */
	private void initPeriodsSearch() {
//		long autoCorrelationMax = 0;
//		int  periodRef          = 0;
//		long autoCorrelation;
//
//		//check if data is long enough
//		if ( data.size() < HzToPeriod( 40 ) * 2 )
//			return;
//
//		//get the first period of the signal
//		for ( int i = HzToPeriod( 400 ); i < HzToPeriod( 40 ); i++ ) {
//			autoCorrelation = autoCorrelation( i );
//
//			if ( autoCorrelation > autoCorrelationMax * 1.05 ) {
//				autoCorrelationMax = autoCorrelation;
//				periodRef = i;
//			}
//		}
//
//		//set the first search area
//		nextPeriodSearchingAreaEnd = (int) ( periodRef * 1.4 );
//		nextPeriodSearchingAreaBeginning = (int) ( periodRef * 0.6 );
		
		//init f0
		calculateF0();
		
		//set the first search area
		final double confidenceLevel = 5 / 100.;
		
		nextPeriodSearchingAreaBeginning = (int) ( HzToPeriod(f0) * ( 1 - confidenceLevel ) );
		nextPeriodSearchingAreaEnd		 = (int) ( HzToPeriod(f0) * ( 1 + confidenceLevel ) );
		
		//filter data
		dataFiltered = new ArrayList<>();
		
		final float resonance                    = 1;
		final float lowFilterFrequenceThreshold  = (float) ( 1.8 * f0);
		final float highFilterFrequenceThreshold = (float) ( 0.05 * f0);
		
		Filter lowFilter  = new Filter( lowFilterFrequenceThreshold, sampling, Filter.PassType.LowPass, resonance );
		Filter highFilter = new Filter( highFilterFrequenceThreshold, sampling, Filter.PassType.HighPass, resonance );
		
		for ( Short data : data ) {
			lowFilter.Update( data );
			
			highFilter.Update( lowFilter.getValue() );
			
			dataFiltered.add( highFilter.getValue() );
		}
	}
	
	/**
	 * The method calculating the pitch periods
	 */
	private void calculatePeriods() {
		int size     = data.size();
		int maxAmp   = 0;
		int startPos = 0;
		
		// get the first pitch in the basic period
		for ( int i = 0; i < baseFragment; i++ ) {
			if ( maxAmp < data.get( i ) ) {
				maxAmp = data.get( i );
				// set this position as the start position
				startPos = i;
			}
		}
		
		// find every pitch in all the fragments
		int pos = startPos + offset; // set current position
		int posAmpMax;
		while ( startPos < size - baseFragment ) {
			if ( data.get( pos ) > 0 ) { // only read the positive data
				posAmpMax = 0;
				maxAmp = 0;
				// access to all the data in this fragment
				while ( pos < startPos + baseFragment ) {
					// find the pitch and mark this position
					if ( maxAmp < data.get( pos ) ) {
						maxAmp = data.get( pos );
						posAmpMax = pos;
					}
					pos++;
				}
				// add pitch position into the list
				pitchPositions.add( posAmpMax );
				// update the start position and the current position
				startPos = posAmpMax;
				pos = startPos + offset;
			}
			else {
				pos++;
			}
		}
		
		// calculate all periods and add them into list
		for ( int i = 0; i < pitchPositions.size() - 1; i++ ) {
			periodsLength.add( pitchPositions.get( i + 1 ) - pitchPositions.get( i ) );
		}
	}
	
	private void zeroCrossingCalculatePeriods() {
		if ( data.isEmpty() ) {
			Log.e( "calculatePeriods (v2)", "data is empty !" );
			return;
		}
		
		boolean isPositive     = ( data.get( 0 ) > 0 );
		int     voiceThreshold = (int) ( audioData.getMaxAmplitude() * 0.6 );    // amplitude above which sounds are not considered noise anymore
		int     position       = 0;
		
		Log.d( "calculatePeriods (v2)", "voiceThreshold = " + Integer.toString( voiceThreshold ) );
		
		if ( isPositive ) {
			//skip the period which is probably not whole
			while ( data.get( position ) > 0 ) position++;
			while ( data.get( position ) < 0 ) position++;
		}
		else {
			//skip the end of the began period
			while ( data.get( position ) < 0 ) position++;
		}
		
		int localMaxPos = position;
		int localMax    = data.get( position );
		
		Log.d( "calculatePeriods (v2)", "begin finding periods at " + Integer.toString( position ) );
		
		//iterate through the whole data
		while ( position < data.size() ) {
			
			//search for local's maximum
			if ( data.get( position ) > localMax ) {
				localMaxPos = position;
				localMax = data.get( position );
			}
			
			//if going negative after being positive
			if ( isPositive && ( data.get( position ) < 0 ) ) {
				
				isPositive = false;

//				String logMsg = "local max found = " + Integer.toString( localMax ) + " (at " + Integer.toString( localMaxPos ) + ")";
//				Log.v( "calculatePeriods (v2)", logMsg );
				
				//if the local's maximum found is higher than the limit considered for noise
				if ( localMax > voiceThreshold ) {
					
					//if it's the first value, we add it directly
					if ( pitchPositions.isEmpty() )
						pitchPositions.add( localMaxPos );
						
						//if not, we add it, only if the period between these 2 points give a
					else {
						int    lastPitchPosition      = pitchPositions.get( pitchPositions.size() - 1 );
						double thisPossiblePeriodToHz = (double) sampling / ( localMaxPos - lastPitchPosition );

//						String logMsg = "(Last max, new possible max) <=> (" + Integer.toString( lastPitchPosition ) + ", " + Integer.toString( localMaxPos ) + ")";
//						Log.d( "calculatePeriods (v2)", logMsg );
//						Log.d( "calculatePeriods (v2)", "This possible new period in Hz = " + Double.toString( thisPossiblePeriodToHz ) );
						
						
						if ( 40 < thisPossiblePeriodToHz && thisPossiblePeriodToHz < 400 ) {
							//add the local's maximum found to the pitch's positions
							pitchPositions.add( localMaxPos );
						}
					}
				}
			}
			
			//if going positive after being negative
			else if ( !isPositive && data.get( position ) > 0 ) {
				isPositive = true;
				
				//reset local's maximum value
				localMax = data.get( position );
			}
			
			++position;
		}
		
		// calculate all periods and add them into T list
		for ( int i = 0; i < pitchPositions.size() - 1; i++ ) {
			periodsLength.add( pitchPositions.get( i + 1 ) - pitchPositions.get( i ) );
			
			if ( periodsLength.get( periodsLength.size() - 1 ) < 293 || periodsLength.get( periodsLength.size() - 1 ) > 295 )
				Log.d( "calculatePeriods (v2)", "Period " + i + " = " + periodsLength.get( periodsLength.size() - 1 ) );
		}
		
		Log.d( "calculatePeriods (v2)", "Number of periods = " + Integer.toString( periodsLength.size() ) );
	}
	
	private void autoCorrelationCalculatePeriodsV1() {
		int periodLength = 0;
		
		//loop on all data to search periods
		for ( int periodBeginning = 0; periodBeginning < data.size() - 2 * nextPeriodSearchingAreaEnd; periodBeginning += periodLength ) {
			
			long autoCorrelation;
			long autoCorrelationMax = 0;
			
			//search a period end from last period end
			for ( int lag = nextPeriodSearchingAreaBeginning; lag < nextPeriodSearchingAreaEnd; lag++ ) {
				autoCorrelation = autoCorrelation( lag, periodBeginning );
				if ( autoCorrelation > autoCorrelationMax ) {
					autoCorrelationMax = autoCorrelation;
					periodLength = lag;
				}
			}
			
			//add the period in the period list and set the next search area
			periodsLength.add( periodLength );
			nextPeriodSearchingAreaEnd = (int) ( periodLength * 1.4 );
			nextPeriodSearchingAreaBeginning = (int) ( periodLength * 0.6 );
		}
		
		int periodMaxPitch;
		int periodMaxPitchIndex = 0;
		int periodBeginning     = 0;
		
		//search each periods maxima
		for ( int period = 0; period < periodsLength.size() - 1; period++ ) {
			periodMaxPitch = 0;
			
			//search a maximum
			for ( int i = periodBeginning; i < periodBeginning + periodsLength.get( period ); i++ ) {
				if ( periodMaxPitch < data.get( i ) ) {
					periodMaxPitch = data.get( i );
					periodMaxPitchIndex = i;
				}
			}
			
			periodBeginning += periodsLength.get( period );
			pitchPositions.add( periodMaxPitchIndex );
		}
	}
	
	/**
	 * The method calculating the periods in the data.
	 *
	 * Also fills the pitchPositions and the T lists.
	 */
	private void autoCorrelationCalculatePeriods() {
		int periodLength;
		int basePeriodLength = nextPeriodSearchingAreaBeginning;
		
		//loop on all data to search periods
		for ( int periodBeginning = 0;
			  periodBeginning < data.size() - 2 * nextPeriodSearchingAreaEnd;
			  periodBeginning += periodLength ) {
			
			long autoCorrelation;
			long autoCorrelationMax = -1;
			periodLength = nextPeriodSearchingAreaBeginning;
			
			//search a period end from last period end
			for ( int lag = nextPeriodSearchingAreaBeginning; lag < nextPeriodSearchingAreaEnd; lag++ ) {
				
				autoCorrelation = autoCorrelationDataFiltered( lag, periodBeginning );
				
				if ( autoCorrelation > autoCorrelationMax ) {
					autoCorrelationMax = autoCorrelation;
					periodLength = lag;
				}
			}
			
			//add the period in the period list and set the next search area
			if ( periodLength != 0 )
				periodsLength.add( periodLength );
			else
				periodBeginning += basePeriodLength;
		}
		
		int periodMaxPitch;
		int periodMaxPitchIndex = 0;
		int periodBeginning     = 0;
		
		//search each periods maxima
		for ( int period = 0; period < periodsLength.size() - 1; period++ ) {
			periodMaxPitch = 0;
			
			//search a maximum
			for ( int i = periodBeginning; i < periodBeginning + periodsLength.get( period ); i++ ) {
				if ( periodMaxPitch < data.get( i ) ) {
					periodMaxPitch = data.get( i );
					periodMaxPitchIndex = i;
				}
			}
			
			periodBeginning += periodsLength.get( period );
			pitchPositions.add( periodMaxPitchIndex );
		}
	}
	
	/**
	 * Calculate the autoCorrelation for the given lag (correspond to a value in Hz in this use-case).
	 *
	 * Calculate the autoCorrelation from the start of the default data.
	 *
	 * @param lag the lag (to test)
	 * @return the value of the autoCorrelation integral
	 */
	private long autoCorrelation( int lag ) {
		return autoCorrelation( lag, 0 );
	}
	
	/**
	 * Calculate the autoCorrelation for the given lag from the given t0.
	 *
	 * Calculate the autoCorrelation with the default data.
	 *
	 * @param lag the lag (to test)
	 * @param t0 the position where to begin the autoCorrelation
	 * @return the value of the autoCorrelation integral
	 */
	private long autoCorrelation( int lag, int t0 ) {
		return autoCorrelation( lag, t0, data );
	}
	
	/**
	 * Calculate the autoCorrelation for the given lag from the given t0 on the given data
	 *
	 * @param lag the lag (to test)
	 * @param t0 the position where to begin the autoCorrelation
	 * @param data the data on which the autoCorrelation must be calculate
	 * @return the value of the autoCorrelation integral
	 */
	private long autoCorrelation( int lag, int t0, List<Short> data ) {
		int  minDataSize = t0 + nextPeriodSearchingAreaEnd + lag;
		long integralSum = 0;
		
		//check data long enough
		if ( data.size() < minDataSize ) {
			return 0;
		}
		
		//calculate sum / integral (for given lag and t0)
		for ( int t = t0; t < t0 + nextPeriodSearchingAreaEnd; t++ ) {
			integralSum += data.get( t ) * data.get( t + lag );
		}
		
		return integralSum;
	}
	
	/**
	 * Calculate the autoCorrelation for the given lag from the given t0 on the data filtered
	 *
	 * INFO : this method was created because the data filtered is a list of float and not short...
	 *
	 * @param lag the lag (to test)
	 * @param t0 the position where to begin the autoCorrelation
	 * @return the value of the autoCorrelation integral
	 */
	private long autoCorrelationDataFiltered( int lag, int t0 ) {
		int  minDataSize = t0 + nextPeriodSearchingAreaEnd + lag;
		long integralSum = 0;
		
		//check data long enough
		if ( dataFiltered.size() < minDataSize ) {
			return 0;
		}
		
		//calculate sum / integral (for given lag and t0)
		for ( int t = t0; t < t0 + nextPeriodSearchingAreaEnd; t++ ) {
			integralSum += dataFiltered.get( t ) * dataFiltered.get( t + lag );
		}
		
		return integralSum;
	}
	
	/**
	 * Returns the value in Hz for the given period length.
	 *
	 * @param periodLength the period length
	 * @return the equivalent in Hz
	 */
	private int periodToHz( int periodLength ) {
		return sampling / periodLength;
	}
	
	/**
	 * Returns the period length of the given value in Hz.
	 *
	 * @param Hz the value in Hz
	 * @return the equivalent period length
	 */
	private int HzToPeriod( int Hz ) {
		return sampling / Hz;
	}
	
	// FEATURE NUMBER 1 : SHIMMER
	
	/**
	 * The method calculating the Shimmer
	 *
	 * @return the Shimmer
	 */
	public double getShimmer() {
		int           minAmp     = 0;
		int           maxAmp;
		long          A_diff_sum = 0; // sum of difference between every two peak-to-peak amplitudes
		long          A_sum      = 0; // sum of all the peak-to-peak amplitudes
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
	
	// FEATURE NUMBER 2 : JITTER
	
	/**
	 * The method calculating the Jitter
	 *
	 * @return the Jitter
	 */
	public double getJitter() {
		double sumOfDifferenceOfPeriods = 0.0;        // sum of difference between every two periods
		double sumOfPeriods             = 0.0;        // sum of all periods
		double numberOfPeriods          = periodsLength.size();   //set as double for double division
		
		// JITTER FORMULA (RELATIVE)
		for ( int i = 0; i < periodsLength.size() - 1; i++ ) {
			sumOfDifferenceOfPeriods += Math.abs( periodsLength.get( i ) - periodsLength.get( i + 1 ) );
			sumOfPeriods += periodsLength.get( i );
		}
		
		// add the last period into sum
		if ( periodsLength.size() > 0 ) {
			sumOfPeriods += periodsLength.get( periodsLength.size() - 1 );
		}
		
		double meanPeriod = sumOfPeriods / numberOfPeriods;

		// calculate jitter (relative)
		return ( sumOfDifferenceOfPeriods / ( numberOfPeriods - 1 ) ) / meanPeriod; // Is it really useful to divide by the meanPeriod???
	}
	
	// FEATURE NUMBER 3 : FUNDAMENTAL FREQUENCY
	
	/**
	 * Getter for the fundamental frequency
	 *
	 * @return the fundamental frequency
	 */
	public double getF0() {
		if ( f0 == null )
			calculateF0();
		
		return f0;

//		double sum = 0.0; // sum of all the fundamental frequencies
//		for ( double t : T ) {
//			sum += t;
//		}
//
//		// average period in second
//		double averageT = sum / T.size() / (double) sampling;
//
//		// f = 1/T
//		return 1 / averageT;
	}
	
	/**
	 * The method finding the fundamental frequency of the data.
	 *
	 * To increase efficiency, this method only test the frequencies between 40Hz to 400Hz.
	 */
	private void calculateF0() {
		final double newMaxThreshold = 1.03;
		
		nextPeriodSearchingAreaEnd = data.size() - HzToPeriod( 40 );
		long autoCorrelationMax = 0;
		int  periodRef          = 0;
		long autoCorrelation;
		
		for ( int i = HzToPeriod( 400 ); i < HzToPeriod( 40 ); i++ ) {
			autoCorrelation = autoCorrelation( i );
			if ( autoCorrelation > autoCorrelationMax * newMaxThreshold ) {
				autoCorrelationMax = autoCorrelation;
				periodRef = i;
			}
		}
		
		f0 = periodToHz( periodRef );
	}

	/*private void difference_function(AudioData audioData)
	{
		int index, tau;
		float delta;
		for(tau = 0; tau < yinBuffer.size(); tau++)
			yinBuffer.add(tau, (short) 0);

		for(tau = 1; tau < yinBuffer.size(); tau++)
		{
			for (index = 0; index < yinBuffer.size(); index++)
			{
				delta = audioData.getDataElement(index) - audioData.getDataElement(index + tau);
				yinBuffer.add(tau, (short) (delta * delta));
			}
		}
	}

	private void cumulativeMeanNormalizedDifference()
	{
		int tau;
		yinBuffer.add(0, (short) 1);
		short runningSum = 0;
		for(tau = 1; tau < yinBuffer.size(); tau++)
		{
			runningSum += yinBuffer.get(tau);
			short buffer = (short) (yinBuffer.get(tau) * (tau / runningSum));
			yinBuffer.add(tau, buffer);
		}
	}

	private int absoluteThreshold()
	{
		int tau;

		for(tau = 2; tau < yinBuffer.size(); tau++)
		{
			if(yinBuffer.get(tau) < threshold)
			{
				while(tau + 1 < yinBuffer.size() && yinBuffer.get(tau + 1) < yinBuffer.get(tau))
				{
					tau++;
				}

				break;
			}
		}

		if(tau == yinBuffer.size() || yinBuffer.get(tau) >= threshold)
		{
			tau = -1;

		}
		else
		{

		}

		return tau;
	}

	private float parabolicInterpolation(final int tauEstimate) {
		final float betterTau;
		final int x0;
		final int x2;

		if (tauEstimate < 1) {
			x0 = tauEstimate;
		} else {
			x0 = tauEstimate - 1;
		}
		if (tauEstimate + 1 < yinBuffer.size()) {
			x2 = tauEstimate + 1;
		} else {
			x2 = tauEstimate;
		}
		if (x0 == tauEstimate) {
			if (yinBuffer.get(tauEstimate) <= yinBuffer.get(x2)) {
				betterTau = tauEstimate;
			} else {
				betterTau = x2;
			}
		} else if (x2 == tauEstimate) {
			if (yinBuffer.get(tauEstimate) <= yinBuffer.get(x0)) {
				betterTau = tauEstimate;
			} else {
				betterTau = x0;
			}
		} else {
			short s0, s1, s2;
			s0 = yinBuffer.get(x0);
			s1 = yinBuffer.get(tauEstimate);
			s2 = yinBuffer.get(x2);
			// fixed AUBIO implementation, thanks to Karl Helgason:
			// (2.0f * s1 - s2 - s0) was incorrectly multiplied with -1
			betterTau = tauEstimate + (s2 - s0) / (2 * (2 * s1 - s2 - s0));
		}
		return betterTau;
	}*/


}
