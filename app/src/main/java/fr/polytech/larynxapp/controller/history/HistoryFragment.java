package fr.polytech.larynxapp.controller.history;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import fr.polytech.larynxapp.R;
import fr.polytech.larynxapp.model.Record;
import fr.polytech.larynxapp.model.database.DBManager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HistoryFragment extends Fragment {

    /**
     * The line chart where the data will be shown
     */
    private LineChart mpLineChart;

    /**
     * The UI list of the data
     */
    private ListView listview;

    /**
     * The list of record datas
     */
    private List<Record> records;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);      //Sets the view for the fragment
        initMap();

        //********************************Creation of the line chart*******************************/
        mpLineChart = root.findViewById(R.id.line_chart);
        final ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        if(!records.isEmpty() ){
            LineDataSet lineDataSet = new LineDataSet(dataValues(records.get(0)), records.get(0).getName());
            setLineData(lineDataSet);
            dataSets.add((lineDataSet));
            final LineData data = new LineData(dataSets);
            mpLineChart.setData(data);
        }

        //***********************************Creation of the list**********************************/
        listview = root.findViewById(R.id.listViewRecords);
        final ArrayAdapter<Record> adapter = new ArrayAdapter<>(getActivity().getApplicationContext(),
                android.R.layout.simple_list_item_1, records);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {                     //Sets the action on a line click
            @SuppressLint("ResourceAsColor")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dataSets.clear();
                LineDataSet tmpLineDataSet = new LineDataSet(dataValues(records.get(position)), records.get(position).getName());
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
     * @param recordIn the record that contain shimmer and jitter data
     * @return the array list that will be shown
     */
    private ArrayList<Entry> dataValues(Record recordIn){
        ArrayList<Entry> dataVals = new ArrayList<>();
        dataVals.add(new Entry((float)recordIn.getJitter(), (float)recordIn.getShimmer()));
        return dataVals;
    }

    /**
     *  Initialisation of the data's map
     */
    private void initMap(){
        records = new DBManager(getContext()).query();
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
        yAxis.setAxisMaximum(10f);
        yAxis.setTextSize(12f);

        //Set the x axis property
        xAxis.setAxisLineWidth(2f);
        xAxis.setAxisLineColor(Color.BLACK);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(3f);
        xAxis.setTextSize(12f);

        chart.getLegend().setEnabled(false);
        chart.getDescription().setEnabled(false);
    }
}
