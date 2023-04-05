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
import android.widget.TextView;

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
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class FollowStatisticsFragment extends Fragment {
    private final DatabaseReference followRef;
    private final String monthYearPattern = "yyyy-MM";

    BarChart bcFollow;
    BarDataSet bdsFollower, bdsFollowed;
    Button butDaily, butMonthly, butAnnually;

    Button butPostChosen, butFollowChosen;

    TextView tvNumFollower, tvNumFollowed;

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
        tvNumFollowed = view.findViewById(R.id.tv_num_followed);
        tvNumFollower = view.findViewById(R.id.tv_num_follower);

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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Date getFromDate(String type){
        Date fromdate = null;
        switch (type) {
            case "Daily":
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, -7);
                fromdate = cal.getTime();
                break;
            case "Monthly": {
                DateFormat dateFormat = new SimpleDateFormat(monthYearPattern);
                String now = dateFormat.format(new Date());
                YearMonth stop = YearMonth.parse(now);
                YearMonth start = stop.minusYears(1);
                Log.d("start month", "" + start.getMonth().getValue());
                fromdate = new GregorianCalendar(start.getYear(), start.getMonthValue(), 1).getTime();
                break;
            }
            case "Annually": {
                DateFormat dateFormat = new SimpleDateFormat(monthYearPattern);
                String now = dateFormat.format(new Date());
                YearMonth stop = YearMonth.parse(now);
                YearMonth start = stop.minusYears(7);
                Log.d("start year", "" + start.getYear());
                fromdate = new GregorianCalendar(start.getYear(), 1, 1).getTime();
                break;
            }
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

                                if (username.equals(follower)) {
                                    if (groupFollowers.containsKey(time)) {
                                        groupFollowers.put(time, groupFollowers.get(time) + 1);
                                    } else {
                                        groupFollowers.put(time, 1);
                                    }
                                }

                                if (username.equals(followed)) {
                                    if (groupFolloweds.containsKey(time)) {
                                        groupFolloweds.put(time, groupFolloweds.get(time) + 1);
                                    } else {
                                        groupFolloweds.put(time, 1);
                                    }
                                }

                                if (time >= fromdate) {
                                    listTime.add(time);
                                }
                            }

                            ArrayList<String> timeArr = new ArrayList<>();
                            int countFollower = 0;
                            int countFollowed = 0;

                            switch (type) {
                                case "Daily": {
                                    ArrayList<Long> timeArrLong = new ArrayList<>(listTime);
                                    for (Long time : timeArrLong) {
                                        timeArr.add(getDayStrFromMillis(time));
                                    }
                                    for (Long time : timeArrLong) {
                                        if (groupFolloweds.containsKey(time)){
                                            countFollowed += groupFolloweds.get(time);
                                        }
                                        Log.d("DEBUG", "Group Followeds " + getDayStrFromMillis(time) + " has " + countFollowed + " people.");
                                        barEntriesFolloweds.add(new BarEntry(timeArr.indexOf(getDayStrFromMillis(time)), countFollowed));

                                        if (groupFollowers.containsKey(time)){
                                            countFollower += groupFollowers.get(time);
                                        }
                                        Log.d("DEBUG", "Group Followers " + getDayStrFromMillis(time) + " has " + countFollower + " people.");
                                        barEntriesFollowers.add(new BarEntry(timeArr.indexOf(getDayStrFromMillis(time)), countFollower));
                                    }
                                    break;
                                }

                                case "Monthly": {
                                    ArrayList<Integer> listMonth = new ArrayList<>();
                                    ArrayList<Long> timeArrLong = new ArrayList<>(listTime);
                                    for (Long time : timeArrLong) {
                                        listMonth.add(getYearMonthFromMillis(time).getMonth().getValue());
                                    }
                                    listMonth = new ArrayList<>( listMonth.stream().distinct().collect(Collectors.toList()) );
                                    for (int month : listMonth) {
                                        timeArr.add(Month.of(month).toString().substring(0,3));
                                        Log.d("DEBUG", "listMonth - " + month);
                                    }

                                    Log.d("DEBUG", "key group followed size = " + groupFolloweds.size());
                                    Log.d("DEBUG", "key group follower size = " + groupFollowers.size());

                                    countFollowed = 0;
                                    countFollower = 0;

                                    for (int month : listMonth){
                                        for (Long time : groupFolloweds.keySet()){
                                            if (getYearMonthFromMillis(time).getMonthValue() == month){
                                                countFollowed += groupFolloweds.get(time);
                                            }
                                        }
                                        barEntriesFolloweds.add(new BarEntry(listMonth.indexOf(month), countFollowed));
                                        Log.d("DEBUG", "TOTAL followed month " + month + " : " + countFollowed);

                                        for (Long time : groupFollowers.keySet()){
                                            if (getYearMonthFromMillis(time).getMonthValue() == month){
                                                countFollower += groupFollowers.get(time);
                                            }
                                        }
                                        barEntriesFollowers.add(new BarEntry(listMonth.indexOf(month), countFollower));
                                        Log.d("DEBUG", "TOTAL follower month " + month + " : " + countFollower);
                                    }
                                    break;
                                }
                                case "Annually": {
                                    ArrayList<Integer> listYear = new ArrayList<>();
                                    ArrayList<Long> timeArrLong = new ArrayList<>(listTime);
                                    for (Long time : timeArrLong) {
                                        listYear.add(getYearMonthFromMillis(time).getYear());
                                    }
                                    listYear = new ArrayList<>( listYear.stream().distinct().collect(Collectors.toList()) );
                                    for (int year : listYear) {
                                        timeArr.add("" + year);
                                        Log.d("DEBUG", "listYear - " + year);
                                    }

                                    Log.d("DEBUG", "key group followed size = " + groupFolloweds.size());
                                    Log.d("DEBUG", "key group follower size = " + groupFollowers.size());

                                    countFollowed = 0;
                                    countFollower = 0;

                                    for (int year : listYear){
                                        for (Long time : groupFolloweds.keySet()){
                                            if (getYearMonthFromMillis(time).getYear() == year){
                                                countFollowed += groupFolloweds.get(time);
                                            }
                                        }
                                        barEntriesFolloweds.add(new BarEntry(listYear.indexOf(year), countFollowed));
                                        Log.d("DEBUG", "TOTAL followed year " + year + " : " + countFollowed);

                                        for (Long time : groupFollowers.keySet()){
                                            if (getYearMonthFromMillis(time).getYear() == year){
                                                countFollower += groupFollowers.get(time);
                                            }
                                        }
                                        barEntriesFollowers.add(new BarEntry(listYear.indexOf(year), countFollower));
                                        Log.d("DEBUG", "TOTAL follower year " + year + " : " + countFollower);
                                    }
                                    break;
                                }
                            }

                            tvNumFollower.setText(":    " + countFollower + " people");
                            tvNumFollowed.setText(":    " + countFollowed + " people");

                            bdsFollower = new BarDataSet(barEntriesFollowers, "Follower");
                            bdsFollowed = new BarDataSet(barEntriesFolloweds, "Followed");

                            if (type.trim().equals("Daily")) {
                                bdsFollower.setColor(Color.parseColor("#F18A85"));
                                bdsFollowed.setColor(Color.parseColor("#24788F"));
                            } else if (type.trim().equals("Monthly")) {
                                bdsFollower.setColor(Color.parseColor("#D3885E"));
                                bdsFollowed.setColor(Color.parseColor("#6988A3"));
                            } else if (type.trim().equals("Annually")) {
                                bdsFollower.setColor(Color.parseColor("#FF8849"));
                                bdsFollowed.setColor(Color.parseColor("#69BE28"));
                            }


                            BarData data = new BarData(bdsFollower, bdsFollowed);
                            bcFollow.setData(data);

                            bcFollow.getDescription().setEnabled(false);
                            XAxis xAxis = bcFollow.getXAxis();
                            xAxis.setValueFormatter(new IndexAxisValueFormatter(timeArr.toArray(new String[timeArr.size()])));
                            xAxis.setCenterAxisLabels(true);
                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                            xAxis.setGranularityEnabled(true);
                            xAxis.setGranularity(1);
                            bcFollow.setDragEnabled(true);
                            bcFollow.setVisibleXRangeMaximum(14);
                            bcFollow.setVisibleXRangeMinimum(timeArr.size() + 1);

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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private YearMonth getYearMonthFromMillis(Long millis){
        DateFormat dateFormat = new SimpleDateFormat(monthYearPattern);
        String timeStr = dateFormat.format(new Date(millis));
        return YearMonth.parse(timeStr);
    }
}