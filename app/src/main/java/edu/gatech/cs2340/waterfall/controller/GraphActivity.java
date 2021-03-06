package edu.gatech.cs2340.waterfall.controller;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.gatech.cs2340.waterfall.R;

@SuppressWarnings("unchecked")
public class GraphActivity extends AppCompatActivity {
    private Spinner locSpinner;
    private Spinner ySpinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        locSpinner = (Spinner) findViewById(R.id.locSpinner);
        ySpinner = (Spinner) findViewById(R.id.yearSpinner);
        final Set<String> locations = new HashSet<>();
        final DatabaseReference ref = WelcomeActivity.getPurityReportDatabase();
        Log.d("BEFORE", "SUCCESS");
        readData(ref, new OnGetDataListener() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                for (DataSnapshot report: dataSnapshot.getChildren()) {
                    double lang = report.child("location").child("longitude").getValue(Double.class);
                    lang = (Math.round(lang * 100.0)) / 100.0;
                    double lat = report.child("location").child("latitude").getValue(Double.class);
                    lat = (Math.round(lat * 100.0)) / 100.0;
                    double[] loc = {lang, lat};
                    locations.add(Arrays.toString(loc));
                }
                List<String> locList = new ArrayList<>(locations);
                locSpinner.setAdapter(new ArrayAdapter<>(GraphActivity.this, android.R.layout.simple_spinner_item, locList));
                List<Integer> yList = new ArrayList<>();
                yList.add(2016);
                yList.add(2017);
                ySpinner.setAdapter(new ArrayAdapter<>(GraphActivity.this, android.R.layout.simple_spinner_item, yList));
                ySpinner.setSelection(1);
                locSpinner.setSelection(0);
                GraphView graph = (GraphView) findViewById(R.id.graph);
                populateGraphView(ref, graph, 0);
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onFailure() {

            }
        });
    }

    /**
     * read the data from the database
     * @param ref to the database
     * @param listener the data listener
     */
    private void readData(DatabaseReference ref, final OnGetDataListener listener) {
        listener.onStart();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listener.onSuccess(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onFailure();
            }
        });

    }

    private void populateGraphView(DatabaseReference ref, final GraphView graph, final int selected) {
        graph.removeAllSeries();
        final LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
        //Spinner locSpinner = (Spinner) findViewById(R.id.locSpinner);
        final String wantedLoc = (String) locSpinner.getSelectedItem();
        Spinner ySpinner = (Spinner) findViewById(R.id.yearSpinner);
        final int wantedYear = (int) ySpinner.getSelectedItem();
        readData(ref, new OnGetDataListener() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {

                List<Integer>[] data = new ArrayList[12];
                for (int i = 0; i < 12; i++) {
                    data[i] = new ArrayList<>();
                }
                for (DataSnapshot report: dataSnapshot.getChildren()) {
                    double lang = report.child("location").child("longitude").getValue(Double.class);
                    lang = (Math.round(lang * 100.0)) / 100.0;
                    double lat = report.child("location").child("latitude").getValue(Double.class);
                    lat = (Math.round(lat * 100.0)) / 100.0;
                    double[] loc = {lang, lat};
                    boolean k = wantedLoc == null;
                    String m = k? "true": "false";
                    Log.d("WANTED", m);
                    if (Arrays.toString(loc).equals(wantedLoc)) {
                        int year = report.child("dateAndTime").child("year").getValue(Integer.class);
                        if (year == (wantedYear - 1900)) {
                            int month = report.child("dateAndTime").child("month").getValue(Integer.class);
                            int virus = report.child("virus").getValue(Integer.class);
                            int cont = report.child("containment").getValue(Integer.class);
                            if (selected == 0) {
                                data[month].add(virus);
                            } else {
                                data[month].add(cont);
                            }
                        }
                    }
                }
                for (int i = 0; i < 12; i++) {
                    int sum = 0;
                    if (data[i] != null && data[i].size() != 0) {
                        for (int j : data[i]) {
                            sum += j;
                        }
                        double avg = ((double) sum) / ((double) data[i].size());
                        DataPoint dp = new DataPoint(i, avg);
                        series.appendData(dp, true, 12);
                    } else {
                        DataPoint dp = new DataPoint(i, 0);
                        series.appendData(dp, true, 12);
                    }
                }

                StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
                staticLabelsFormatter.setHorizontalLabels(new String[] {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"});
                graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
                graph.addSeries(series);
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onFailure() {

            }
        });
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        final DatabaseReference ref = WelcomeActivity.getPurityReportDatabase();
        GraphView graph = (GraphView) findViewById(R.id.graph);
        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.virus_btn:
                if (checked)
                    populateGraphView(ref, graph, 0);
                    break;
            case R.id.cunt_btn:
                if (checked)
                    populateGraphView(ref, graph, 1);
                    // Ninjas rule
                    break;
        }
    }
}
