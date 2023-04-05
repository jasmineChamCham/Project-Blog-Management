package com.example.blogapp.view;


import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.blogapp.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class FollowStatisticsFragment extends Fragment {
    private final DatabaseReference followRef;

    BarChart bcFollow;
    BarDataSet bdsFollower, bdsFollowed;
    Button butDaily, butMonthly, butAnnually;

    Button butPostChosen, butFollowChosen;

    private static String username;

    public FollowStatisticsFragment() {
        followRef = FirebaseDatabase.getInstance().getReference().child("follows");
        username = "Ngoc Tram";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_follow_statistics, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        butFollowChosen = view.findViewById(R.id.but_follow_chosen);
        butPostChosen = view.findViewById(R.id.but_post_chosen);

        butFollowChosen.setBackgroundColor(getResources().getColor(R.color.main_color));
        butPostChosen.setBackgroundColor(getResources().getColor(R.color.white));
        butFollowChosen.setTextColor(getResources().getColor(R.color.white));
        butPostChosen.setTextColor(getResources().getColor(R.color.main_color));

        butFollowChosen.setOnClickListener(v -> {
            bcFollow.setVisibility(View.GONE);
            drawInitialBarChart();
        });

        butPostChosen.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.postStatisticsFragment, null));

        butDaily = view.findViewById(R.id.but_daily);
        butMonthly = view.findViewById(R.id.but_monthly);
        butAnnually = view.findViewById(R.id.but_annually);
        bcFollow = view.findViewById(R.id.bc_follow);

        drawInitialBarChart();

        butDaily.setOnClickListener(v -> drawBarChart("Daily"));

        butMonthly.setOnClickListener(v -> drawBarChart("Monthly"));

        butAnnually.setOnClickListener(v -> drawBarChart("Annually"));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void drawInitialBarChart(){
        drawBarChart("Daily");
    }

    private String[] getRecentDays(){
        String[] days = new String[7];
        DateFormat dateFormat = new SimpleDateFormat("MM-dd");

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -7);

        for (int i=1; i<=7; i++) {
            cal.add(Calendar.DATE, 1);
            Date dayD = cal.getTime();
            String day = dateFormat.format(dayD);
            days[i-1] = day;
        }
        return days;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String[] getRecentMonths(){
        String[] months = new String[12];
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
        String todate = dateFormat.format(date);
        YearMonth stop = YearMonth.parse(todate);
        YearMonth start = stop.minusYears( 1 );
        List<YearMonth> yearMonths = new ArrayList<>( 12 );
        YearMonth yearMonth = start ;
        while ( yearMonth.isBefore( stop ) ) {
            yearMonth = yearMonth.plusMonths( 1 );
            yearMonths.add( yearMonth ) ;  // Add each incremented YearMonth to our collection.
        }

        for (int i=0; i<12; i++) {
            months[i] = yearMonths.get(i).getMonth().toString().substring(0,3);
        }
        return months;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String[] getRecentYears(){
        String[] years = new String[7];
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
        String todate = dateFormat.format(date);
        YearMonth stop = YearMonth.parse(todate);
        YearMonth start = stop.minusYears( 7 );
        List<YearMonth> yearMonths = new ArrayList<>();
        YearMonth yearMonth = start ;
        while ( yearMonth.isBefore( stop ) ) {
            yearMonth = yearMonth.plusYears( 1 );
            yearMonths.add( yearMonth ) ;  // Add each incremented YearMonth to our collection.
        }

        for (int i=0; i<7; i++) {
            years[i] = "" + yearMonths.get(i).getYear();
            Log.d("month[i]", years[i]);
        }
        return  years;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String[] getTimeRange(String type){
        if (type.equals("Daily")){
            return getRecentDays();
        } else if (type.equals("Monthly")){
            return getRecentMonths();
        } else if (type.equals("Annually")){
            return getRecentYears();
        } else return null;
    }

    private Date getFromDate(String type){
        Date fromdate = null;
        if (type.equals("Daily")){
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -7);
            fromdate = cal.getTime();
        }
        return fromdate;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("RestrictedApi")
    private void drawBarChart(String type) {
        ArrayList<BarEntry> barEntriesFollowers = new ArrayList<>();
        ArrayList<BarEntry> barEntriesFolloweds = new ArrayList<>();

        Map<Long, Integer> groupFollowers = new TreeMap<>();
        Map<Long, Integer> groupFolloweds = new TreeMap<>();

        followRef.orderByChild("time").endBefore(new Date().getTime())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Long fromdate = getFromDate(type).getTime();
                        Set<Long> listTime = new TreeSet<>();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Long time = snapshot.child("time").getValue(Long.class);
                            String follower = snapshot.child("follower").getValue(String.class);
                            String followed = snapshot.child("followed").getValue(String.class);

                            if (username.equals(follower)){
                                if (groupFollowers.containsKey(time)){
                                    groupFollowers.put(time, groupFollowers.get(time) + 1);
                                } else {
                                    groupFollowers.put(time, 1);
                                }
                                if (time >= fromdate) {
                                    listTime.add(time);
                                }
                            }

                            if (username.equals(followed)){
                                if (groupFolloweds.containsKey(time)){
                                    groupFolloweds.put(time, groupFolloweds.get(time) + 1);
                                } else {
                                    groupFolloweds.put(time, 1);
                                }
                                if (time >= fromdate) {
                                    listTime.add(time);
                                }
                            }
                        }

                        ArrayList<Long> timeArrLong = new ArrayList<>(listTime);
                        ArrayList<String> timeArr = new ArrayList<>();
                        for (Long time : timeArrLong) {
                            timeArr.add(getDayStrFromMillis(time));
                        }

                        int count = 0;
                        for (Long group : groupFollowers.keySet()) {
                            count += groupFollowers.get(group);
                            if (group >= fromdate) {
                                Log.d("DEBUG","Group Followers " + getDayStrFromMillis(group) + " has " + count + " items.");
                                Log.d("DEBUG", "index of group er " + (timeArr.indexOf(getDayStrFromMillis(group)) + 1));
                                barEntriesFollowers.add (new BarEntry(timeArr.indexOf(getDayStrFromMillis(group)) , count));
                            }
                        }

                        count = 0;
                        for (Long group : groupFolloweds.keySet()) {
                            count += groupFolloweds.get(group);

                            if (group >= fromdate){
                                Log.d("DEBUG","Group Followeds " + getDayStrFromMillis(group) + " has " + count + " items.");
                                Log.d("DEBUG", "index of group ed " + (timeArr.indexOf(getDayStrFromMillis(group)) + 1));
                                barEntriesFolloweds.add (new BarEntry(timeArr.indexOf(getDayStrFromMillis(group)) + 1, count));
                            }
                        }

                        bdsFollower = new BarDataSet(barEntriesFollowers, "Follower");
                        bdsFollowed = new BarDataSet(barEntriesFolloweds, "Followed");

                        if (type.trim().equals("Daily")){
                            bdsFollower.setColor(Color.parseColor("#F18A85"));
                            bdsFollowed.setColor(Color.parseColor("#24788F"));
                        } else if (type.trim().equals("Monthly")){
                            bdsFollower.setColor(Color.parseColor("#D3885E"));
                            bdsFollowed.setColor(Color.parseColor("#6988A3"));
                        } else if (type.trim().equals("Annually")){
                            bdsFollower.setColor(Color.parseColor("#FF8849"));
                            bdsFollowed.setColor(Color.parseColor("#69BE28"));
                        }


                        BarData data = new BarData(bdsFollower, bdsFollowed);
                        bcFollow.setData(data);

                        for (String s : timeArr.toArray(new String[timeArr.size()])){
                            Log.d("day", s);
                        }

                        bcFollow.getDescription().setEnabled(false);
                        XAxis xAxis = bcFollow.getXAxis();
                        xAxis.setValueFormatter(new IndexAxisValueFormatter(timeArr.toArray(new String[timeArr.size()])));
                        xAxis.setCenterAxisLabels(true);
                        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                        xAxis.setGranularityEnabled(true);
                        xAxis.setGranularity(1);
                        bcFollow.setDragEnabled(true);
                        bcFollow.setVisibleXRangeMaximum(12);

                        float barSpace = 0.1f;
                        float groupSpace = 0.5f;
                        data.setBarWidth(0.15f);
                        bcFollow.getXAxis().setAxisMinimum(0);
                        bcFollow.animate();
                        bcFollow.groupBars(0, groupSpace, barSpace);
                        bcFollow.invalidate();
                        
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        
    }

    private String getDayStrFromMillis(Long millis){
        DateFormat dateFormat = new SimpleDateFormat("dd/MM");
        return dateFormat.format(new Date(millis));
    }

}