package fr.polytech.larynxapp.model.audio;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import fr.polytech.larynxapp.R;

/**
 * @author Tianxue WANG and Wenli YAN
 * @version 2018.0115
 * @date 07/12/2017
 */

/**
 * The class drawing the diagrams on the screen
 */
public class AudioGraphDrawer extends View {
	
	/**
	 * Paint use to draw the forms.
	 */
	Paint paint;
	
	/**
	 * AudioData containing the data to draw
	 */
	AudioData audioData;
	
	/**
	 * Bitmap containing the graph once draw
	 */
	Bitmap graph;
	
	
	
	
	
	/**
	 * AudioGraphDrawer builder with 1 parameter
	 *
	 * The audioData will be a new one.
	 *
	 * @param context the context where to draw
	 */
	public AudioGraphDrawer( Context context ) {
		this( context, new AudioData() );
	}
	
	/**
	 * AudioGraphDrawer builder with 2 parameters
	 *
	 * @param context the context where to draw
	 * @param audioData the AudioData containing the data to draw
	 */
	public AudioGraphDrawer(Context context, AudioData audioData ) {
		super( context );
		
		this.audioData = audioData;
		
		paint = new Paint();
	}
	
	
	
	
	
	
	/**
	 * The method override which draws
	 *
	 * @param canvas the canvas
	 */
	@Override
	protected void onDraw( Canvas canvas ) {
		super.onDraw( canvas );
		
		if (graph == null)
			drawGraph( canvas );
		
		canvas.drawBitmap( graph, 0, 0, null );
	}
	
	/**
	 * Draw the graph.
	 *
	 * @param canvasSource the canvas where the drawing will be
	 */
	private void drawGraph(Canvas canvasSource) {
		final int canvasWidth  = canvasSource.getWidth() - 10;
		final int canvasHeight = 600;
		
		graph = Bitmap.createBitmap( canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888 );
		Canvas canvas = new Canvas( graph );
		
		paint.setStrokeWidth( 2 );
		paint.setTextSize( 20 );
		paint.setColor( Color.LTGRAY );
		
		
		/* Draw Data */
		final int offsetX = 50;//
		final int offsetY = canvasHeight / 2;
		
		// final long dataLength = AudioData.length;
		final long dataLength = audioData.getDataSize();
		int        height     = audioData.getMaxAmplitudeAbs() * 2;
		
		final double scaleY = (double) canvasHeight / 1.5 / height;
		final double scaleX = (double) ( canvasWidth - 30 ) / dataLength;
		
		// Background
		canvas.drawRect( 50, 50, canvasWidth + 60, canvasHeight - 50, paint );
		
		// Scale
		paint.setColor( getResources().getColor( R.color.colorAccent ) );
		final double s = ( canvasHeight - 100 ) / 4;
		
		for ( int i = offsetY; i <= canvasHeight - 50; i += s ) {
			
			canvas.drawLine( offsetX, i, offsetX + 10, i, paint );
			
		}
		
		for ( int i = offsetY; i >= 50; i -= s ) {
			
			canvas.drawLine( offsetX, i, offsetX + 10, i, paint );
		}
		
		double indice = -1;
		for ( int i = canvasHeight - 50; i > 0; i -= s ) {
			if ( indice >= 0 )
				canvas.drawText( Double.toString( indice ), 13, i, paint );
			else
				canvas.drawText( Double.toString( indice ), 5, i, paint );
				
			indice = indice + 0.5;
		}
		
		// X
		canvas.drawLine( offsetX, offsetY, canvasWidth + 60, offsetY, paint );
		
		// Y
		canvas.drawLine( offsetX, 50, offsetX, canvasHeight - 50, paint );
		
		paint.setColor( Color.BLUE );
		for ( int i = 10; i < dataLength; i += 10 ) {
			canvas.drawLine( (float) ( offsetX + 20 + i * scaleX ), (float) ( offsetY - audioData.getDataElement( i ) * scaleY ),
							 (float) ( offsetX + 20 + i * scaleX ), offsetY, paint );
		}
	}
	
}
