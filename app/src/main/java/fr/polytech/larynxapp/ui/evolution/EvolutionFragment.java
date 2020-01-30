package fr.polytech.larynxapp.ui.evolution;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import fr.polytech.larynxapp.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

public class EvolutionFragment extends Fragment {

    /**
     * The line chart where the shimmer values will be shown
     */
    private LineChart shimmerMpLineChart;

    /**
     * The line chart where the jitter values will be shown
     */
    private LineChart jitterMpLineChart;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_evolution, container, false);    //Sets the view the the fragment

        //******************************Creation of the shimmer's chart*****************************/
        final TextView shimmerTextView = root.findViewById(R.id.shimmer_text_view);
        shimmerTextView.setText("Shimmer");
        shimmerTextView.setTextSize(20f);


        shimmerMpLineChart = root.findViewById(R.id.shimmer_line_chart);
        setChart(shimmerMpLineChart);

        LineDataSet shimmerLineSet = new LineDataSet(shimmerDataValues1(), "Shimmer");
        shimmerLineSet.setColor(Color.BLACK);
        shimmerLineSet.setLineWidth(2f);
        shimmerLineSet.setCircleColor(Color.BLACK);
        shimmerLineSet.setCircleRadius(5f);
        shimmerLineSet.setCircleHoleRadius(2.5f);
        shimmerLineSet.setValueTextSize(0f);
        ArrayList<ILineDataSet> shimmerDataSets = new ArrayList<>();
        shimmerDataSets.add((shimmerLineSet));


        LineData shimmerData = new LineData(shimmerDataSets);
        shimmerMpLineChart.setData(shimmerData);
        shimmerMpLineChart.invalidate();
        shimmerMpLineChart.setDrawGridBackground(false);



        //******************************Creation of the jitter's chart******************************/
        final TextView jitterTextView = root.findViewById(R.id.jitter_text_view);
        jitterTextView.setText("jitter");
        jitterTextView.setTextSize(20f);

        jitterMpLineChart = root.findViewById(R.id.jitter_line_chart);
        setChart(jitterMpLineChart);

        LineDataSet jitterLineSet = new LineDataSet(jitterDataValues(), "Jitter");
        jitterLineSet.setColor(Color.BLACK);
        jitterLineSet.setLineWidth(2f);
        jitterLineSet.setCircleColor(Color.BLACK);
        jitterLineSet.setCircleRadius(5f);
        jitterLineSet.setCircleHoleRadius(2.5f);
        jitterLineSet.setValueTextSize(0f);
        ArrayList<ILineDataSet> jitterDataSets = new ArrayList<>();
        jitterDataSets.add((jitterLineSet));

        LineData jitterData = new LineData(jitterDataSets);
        jitterMpLineChart.setData(jitterData);
        jitterMpLineChart.invalidate();
        jitterMpLineChart.setDrawGridBackground(false);


        return root;
    }

    /**
     * Initialisation of the shimmer data's arraylist, should be completed and link to the data base
     * @return the shimmer data's arraylist
     */
    private ArrayList<Entry> shimmerDataValues1(){
        ArrayList<Entry> dataVals = new ArrayList<>();
        dataVals.add(new Entry(1,0.5f));
        dataVals.add(new Entry(2,1f));
        dataVals.add(new Entry(3,0.75f));
        dataVals.add(new Entry(4,0.80f));

        return dataVals;
    }

    /**
     * Initialisation of the jitter data's arraylist, should be completed and link to the data base
     * @return the jiiter data's arraylist
     */
    private ArrayList<Entry> jitterDataValues(){
        ArrayList<Entry> dataVals = new ArrayList<>();
        dataVals.add(new Entry(1,0.9f));
        dataVals.add(new Entry(2,0.5f));
        dataVals.add(new Entry(3,0.6f));
        dataVals.add(new Entry(4,0.23f));
        return dataVals;
    }

    /**
     * Set the graphique feature of the line charts
     * @param chart the chart to be set
     */
    private void setChart(LineChart chart){

        YAxis yAxis = chart.getAxisLeft();                  //The line chart's y axis
        XAxis xAxis = chart.getXAxis();                     //The line chart's x axis

        chart.getAxisRight().setEnabled(false);             //Disable the right axis

        //Set the y axis property
        yAxis.setAxisLineWidth(2f);
        yAxis.setAxisLineColor(Color.BLACK);
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(3f);
        yAxis.setTextSize(12f);

        //Set the x axis property
        xAxis.setAxisLineWidth(2f);
        xAxis.setAxisLineColor(Color.BLACK);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(12f);

        chart.getLegend().setEnabled(false);
        chart.getDescription().setEnabled(false);
    }
}