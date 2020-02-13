package fr.polytech.larynxapp.model.audio;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tianxue WANG and Wenli YAN
 * @version 2018.0115
 * @date 08/12/2017
 */

/**
 * Class stocking the information of the audio data.
 */
public class AudioData {
	
	/**
	 * The highest amplitude in this audio data.
	 */
	private short       maxAmplitude;
	
	/**
	 * The lowest amplitude in this audio data.
	 */
	private short       minAmplitude;
	
	/**
	 * The row audio data list.
	 */
	private List<Short> data;
	
	/**
	 * The data processed list for analysing.
	 */
	private List<Short> data_processed;
	
	/**
	 * AudioData sole and default builder.
	 */
	public AudioData() {
		data = new ArrayList<>();
		
		maxAmplitude = Short.MIN_VALUE;
		minAmplitude = Short.MAX_VALUE;
	}

	/**
	 * Gets for the highest amplitude.
	 *
	 * @return the highest amplitude of the audio data
	 */
	public short getMaxAmplitude() {
		return maxAmplitude;
	}
	
	/**
	 * Gets for the lowest amplitude.
	 *
	 * @return the lowest amplitude of the audio data
	 */
	public short getMinAmplitude() {
		return minAmplitude;
	}
	
	/**
	 * Gets for the data element of the given index.
	 *
	 * @param index the index of the element to return
	 * @return the element of the given index
	 */
	public Short getDataElement(int index ) {
		return data.get( index );
	}
	
	/**
	 * Gets for the row audio data size.
	 *
	 * @return the size of the row data
	 */
	public int getDataSize() {
		return data.size();
	}
	
	/**
	 * Add a data to the audio data.
	 *
	 * @param newData the new data to add
	 */
	public void addData( short newData ) {
		data.add( newData );
	}
	
	/**
	 * Gets for the audio data list.
	 *
	 * @return the list of the row audio data
	 */
	public List<Short> getData() {
		return data;
	}
	
	/**
	 * Gets for the audio data processed list.
	 *
	 * @return the list of the audio data processed
	 */
	public List<Short> getData_processed() {
		return data_processed;
	}
	
	/**
	 * Processes the row audio data.
	 *
	 * Cut the beginning and the end of the row audio data.
	 */
	public void processData() {
		try {
			data_processed = new ArrayList<>( data.size() );

			for ( int i = 0; i < data.size(); i++ ) {
				short newData = data.get( i );
				
				data_processed.add( newData );
				
				if (minAmplitude > newData)
					minAmplitude = newData;
				
				if (maxAmplitude < newData)
					maxAmplitude = newData;
			}
		}
		catch ( Exception e ) {
			Log.e( "---processData---", Log.getStackTraceString( e ), e );
		}
	}
}
