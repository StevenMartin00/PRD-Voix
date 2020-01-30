package fr.polytech.larynxapp.ui.history;

import android.app.Activity;
import android.app.LauncherActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import fr.polytech.larynxapp.MainActivity;
import fr.polytech.larynxapp.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.HashMap;

public class HistoryFragment extends Fragment {

    /**
     * The line chart where the data will be shown
     */
    private LineChart mpLineChart;

    /**
     * The list of the data
     */
    private ListView listview;

    /**
     * The list of the data's dates
     */
    private String[] dates = new String[]{
            "01/01/2020", "02/01/2020", "03/01/2020", "04/01/2020", "05/01/2020", "06/01/2020",
            "07/01/2020", "08/01/2020", "09/01/2020", "10/01/2020", "11/01/2020", "12/01/2020",
    };

    /**
     * The map where the data will be associated with the dates
     */
    private HashMap<String, float[]> listMap = new HashMap<>();


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_history, container, false);      //Sets the view the the fragment
        initMap();

        //********************************Creation of the line chart*******************************/
        mpLineChart = root.findViewById(R.id.line_chart);
        final ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        if(dates != null ){
            LineDataSet lineDataSet = new LineDataSet(dataValues(listMap.get(dates[0])),"Data Set 1");
            setLineData(lineDataSet);
            dataSets.add((lineDataSet));
            final LineData data = new LineData(dataSets);
            mpLineChart.setData(data);
        }



        //***********************************Création of the list**********************************/
        listview = root.findViewById(R.id.listView1);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity().getApplicationContext(),
                android.R.layout.simple_list_item_1, dates);
        listview.setAdapter(adapter);


        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {                     //Sets the action on a line click
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dataSets.clear();
                LineDataSet tmpLineDataSet = new LineDataSet(dataValues(listMap.get(dates[position])), dates[position]);
                setLineData(tmpLineDataSet);
                dataSets.add(tmpLineDataSet);
                final LineData data = new LineData(dataSets);
                mpLineChart.setData(data);
                mpLineChart.invalidate();
            }
        });

        setChart(mpLineChart);
        mpLineChart.setDrawGridBackground(false);
        mpLineChart.invalidate();

        return root;
    }

    /**
     * Sets the data set's graphical parameters
     * @param lineDataSet the data set to configure
     */
    private void setLineData(LineDataSet lineDataSet){
        lineDataSet.setCircleColor(Color.BLACK);
        lineDataSet.setCircleRadius(5f);
        lineDataSet.setCircleHoleRadius(2.5f);
        lineDataSet.setValueTextSize(0f);
    }

    /**
     * Sets the data that will be shown in the chart
     * @param vals the shimmer and the jitter of a data
     * @return the array list that will be shown
     */
    private ArrayList<Entry> dataValues(float[] vals){
        ArrayList<Entry> dataVals = new ArrayList<>();
        dataVals.add(new Entry(vals[0],vals[1]));

        return dataVals;
    }

    /**
     *  Initialisation of the data's map, should be completed and link to the data base
     */
    private void initMap(){
        float tmpfTab[][]= {{0.64f, 0.32f},{0.54f,0.87f},{0.32f,0.96f},{0.51f,0.63f},{1.13f,0.45f},{1.1f,0.5f},{1.13f,0.36f},{0.39f,0.68f},{0.8f,0.81f},{0.65f,0.74f},{0.63f,1.05f},{0.36f,0.66f}};
        for(int i =0; i<12; i++){
            listMap.put(dates[i],tmpfTab[i]);
        }
    }

    /**
     * Set the graphic feature of the line chart
     * @param chart the chart to be set
     */
    private void setChart(LineChart chart){

        YAxis yAxis = chart.getAxisLeft();                      //The line chart's y axis
        XAxis xAxis = chart.getXAxis();                         //The line chart's x axis

        chart.getAxisRight().setEnabled(false);                 //Disable the right axis

        //Set the y axis property
        yAxis.setAxisLineWidth(2.5f);
        yAxis.setAxisLineColor(Color.BLACK);
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(1.5f);
        yAxis.setTextSize(12f);

        //Set the x axis property
        xAxis.setAxisLineWidth(2f);
        xAxis.setAxisLineColor(Color.BLACK);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(1.5f);
        xAxis.setTextSize(12f);

        chart.getLegend().setEnabled(false);
        chart.getDescription().setEnabled(false);
    }
}
