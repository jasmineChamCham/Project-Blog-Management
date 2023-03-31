package com.example.blogapp.view;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.blogapp.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class PostStatisticsFragment extends Fragment {

    LineChart lcFollower;
    LineData lineData;
    LineDataSet lineDataSet;
    ArrayList lineEntries;

    BarChart bcFollow;
    BarDataSet bdsFollower, bdsFollowed;
    ArrayList barEntries;
    Button butDaily, butMonthly, butAnnually;
    Button butPostChosen, butFollowChosen;


    public PostStatisticsFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_post_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        butFollowChosen = view.findViewById(R.id.but_follow_chosen_P);
        butPostChosen = view.findViewById(R.id.but_post_chosen_P);

        butPostChosen.setBackgroundColor(getResources().getColor(R.color.main_color));
        butFollowChosen.setBackgroundColor(getResources().getColor(R.color.white));
        butPostChosen.setTextColor(getResources().getColor(R.color.white));
        butFollowChosen.setTextColor(getResources().getColor(R.color.main_color));

        butFollowChosen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Navigation.findNavController(v).navigate(R.id.followStatisticsFragment, null);
            }
        });
        lcFollower = view.findViewById(R.id.lc_follower);
        drawInitialBarChart();
    }

    private void drawInitialBarChart(){
        String[] days = new String[7];
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM");
        String todate = dateFormat.format(date);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -7);
        Date fromdateD = cal.getTime();
        String fromdate = dateFormat.format(fromdateD);
        Log.d("From date", fromdate);
        Log.d("To date", todate);

        for (int i=1; i<=7; i++) {
            cal.add(Calendar.DATE, 1);
            Date dayD = cal.getTime();
            String day = dateFormat.format(dayD);
            days[i-1] = day;
        }

        lcFollower.getDescription().setEnabled(false);

        XAxis xAxis = lcFollower.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));
        xAxis.setCenterAxisLabels(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(1);
        lcFollower.setDragEnabled(true);
        lcFollower.setVisibleXRangeMaximum(days.length);

        lineEntries = getBarEntriesFollower();
        lineDataSet = new LineDataSet(lineEntries, "Followers");
        lineData = new LineData(lineDataSet);
        lcFollower.setData(lineData);
        lineDataSet.setColors(ColorTemplate.JOYFUL_COLORS);
        lineDataSet.setValueTextSize(18f);
    }
    @SuppressLint("RestrictedApi")
    private void drawBarChart(String[] timeRange) {
        bdsFollower = new BarDataSet(getBarEntriesFollower(), "Follower");
        bdsFollower.setColor(getApplicationContext().getResources().getColor(R.color.purple_200));
        bdsFollowed = new BarDataSet(getBarEntriesFollowed(), "Followed");
        bdsFollowed.setColor(Color.BLUE);

        BarData data = new BarData(bdsFollower, bdsFollowed);
        bcFollow.setData(data);

        bcFollow.getDescription().setEnabled(false);
        XAxis xAxis = bcFollow.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(timeRange));
        xAxis.setCenterAxisLabels(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(1);
        bcFollow.setDragEnabled(true);
        bcFollow.setVisibleXRangeMaximum(timeRange.length);

        float barSpace = 0.1f;
        float groupSpace = 0.5f;
        data.setBarWidth(0.15f);
        bcFollow.getXAxis().setAxisMinimum(0);
        bcFollow.animate();
        bcFollow.groupBars(0, groupSpace, barSpace);
        bcFollow.invalidate();
    }

    private ArrayList<BarEntry> getBarEntriesFollower() {
        barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(1f, 4));
        barEntries.add(new BarEntry(2f, 6));
        barEntries.add(new BarEntry(3f, 8));
        barEntries.add(new BarEntry(4f, 2));
        barEntries.add(new BarEntry(8f, 5));
        barEntries.add(new BarEntry(9f, 5));
        barEntries.add(new BarEntry(10f, 5));
        barEntries.add(new BarEntry(11f, 5));
        barEntries.add(new BarEntry(12f, 5));
        barEntries.add(new BarEntry(5f, 4));
        barEntries.add(new BarEntry(6f, 1));
        barEntries.add(new BarEntry(7f, 8));

        return barEntries;
    }

    private ArrayList<BarEntry> getBarEntriesFollowed() {
        barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(1f, 8));
        barEntries.add(new BarEntry(2f, 12));
        barEntries.add(new BarEntry(3f, 4));
        barEntries.add(new BarEntry(4f, 1));
        barEntries.add(new BarEntry(8f, 5));
        barEntries.add(new BarEntry(9f, 5));
        barEntries.add(new BarEntry(10f, 5));
        barEntries.add(new BarEntry(11f, 5));
        barEntries.add(new BarEntry(12f, 5));
        barEntries.add(new BarEntry(5f, 7));
        barEntries.add(new BarEntry(6f, 3));
        barEntries.add(new BarEntry(7f, 7));

        return barEntries;
    }

}