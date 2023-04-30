package com.example.blogapp.view;


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

import com.example.blogapp.R;
import com.example.blogapp.databinding.FragmentFollowStatisticsBinding;
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
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class FollowStatisticsFragment extends Fragment{
    public FragmentFollowStatisticsBinding binding;
    private final DatabaseReference followRef;
    private final String monthYearPattern = "yyyy-MM";
    private String userId;

    BarDataSet bdsFollower, bdsFollowed;


    public FollowStatisticsFragment(){
        followRef = FirebaseDatabase.getInstance().getReference("followers");
        userId = "-NRlYm-P-HVbQtt_G2Zm";
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFollowStatisticsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.butFollowChosen.setBackgroundColor(getResources().getColor(R.color.main_color));
        binding.butPostChosen.setBackgroundColor(getResources().getColor(R.color.white));
        binding.butFollowChosen.setTextColor(getResources().getColor(R.color.white));
        binding.butPostChosen.setTextColor(getResources().getColor(R.color.main_color));

        binding.butFollowChosen.setOnClickListener(v -> drawBarChart("Daily"));
        binding.butPostChosen.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.postStatisticsFragment));

        binding.butDaily.setOnClickListener(v -> drawBarChart("Daily"));
        binding.butMonthly.setOnClickListener(v -> drawBarChart("Monthly"));
        binding.butAnnually.setOnClickListener(v -> drawBarChart("Annually"));
    }

    @Override
    public void onStart() {
        super.onStart();
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
    private ArrayList<Long> getTimeUnits(String type){
        ArrayList<Long> timeArrLong = new ArrayList<>();
        switch (type){
            case "Daily":{
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.MILLISECOND, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.HOUR, 0);
                for (int i=0; i<7; i++){
                    timeArrLong.add(cal.getTimeInMillis());
                    cal.add(Calendar.DATE, -1);
                }
                Collections.reverse(timeArrLong);
                for (Long time : timeArrLong){
                    Log.d("DEBUG" ,"daily day timeArr : " + time);
                }
                break;
            }

            case "Monthly": {
                DateFormat dateFormat = new SimpleDateFormat(monthYearPattern);
                Date datenow = new Date();
                String now = dateFormat.format(datenow);
                Log.d("now getTimeUnits", now);
                YearMonth stop = YearMonth.parse(now);
                YearMonth temp = stop;
                for (int i=0; i<12; i++){
                    Log.d("timeArr monthly", "" + temp.getMonthValue());
                    timeArrLong.add((long) temp.getMonthValue());
                    temp = temp.minusMonths(1);
                }
                break;
            }
            case "Annually": {
                DateFormat dateFormat = new SimpleDateFormat(monthYearPattern);
                Date datenow = new Date();
                String now = dateFormat.format(datenow);
                YearMonth stop = YearMonth.parse(now);
                YearMonth temp = stop;
                for (int i=0; i<7; i++){
                    Log.d("timeArr annually", "" + temp.getYear());
                    timeArrLong.add((long) temp.getYear());
                    temp = temp.minusYears(1);
                }
                break;
            }
        }
        return timeArrLong;
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getDayMonthYearFromMillis(Long millis){
        DateFormat dateFormat = new SimpleDateFormat("MM-dd");
        return getYearMonthFromMillis(millis).getYear() + "-" + dateFormat.format(new Date(millis));
    }

    private void drawBarChart(String type){
        Log.d("DEBUG", "FOLLOW REF : " + followRef.toString());
        ArrayList<BarEntry> barEntriesFollowers = new ArrayList<>();
        ArrayList<BarEntry> barEntriesFolloweds = new ArrayList<>();

        Map<String, Integer> groupFollowers = new TreeMap<>();
        Map<String, Integer> groupFolloweds = new TreeMap<>();

        followRef.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Long fromdate = getFromDate(type).getTime();
                Log.d("fromdate: ", ""+fromdate);
                Log.d("fromdate: ", getDayMonthYearFromMillis(fromdate));

                Set<Long> listTime = new TreeSet<>();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String followerId = snapshot.getKey().trim();

                    for (DataSnapshot i : snapshot.getChildren()){
                        String followedId = i.getKey().trim();
                        Long time = i.child("time").getValue(Long.class);
                        Log.d("time: ", getDayMonthYearFromMillis(time));

                        Calendar c = Calendar.getInstance();
                        c.add(Calendar.DATE, 1);
                        c.set(Calendar.HOUR, 0);
                        c.set(Calendar.MINUTE, 0);
                        c.set(Calendar.SECOND, 0);
                        c.set(Calendar.MILLISECOND, 0);

                        if (time < c.getTimeInMillis()){
                            Log.d("userId", userId);
                            Log.d("followerId: ", followerId);
                            Log.d("followedId: ", followedId);
                            Log.d("DEBUG", "userId equal followerId : " + userId.equals(followerId));

                            if (userId.equals(followerId)) {
                                Log.d("DEBUG", "INTO USERID EQUALS FOLLOWER");
                                if (groupFollowers.containsKey(getDayMonthYearFromMillis(time))) {
                                    groupFollowers.put(getDayMonthYearFromMillis(time),
                                            groupFollowers.get(getDayMonthYearFromMillis(time)) + 1);
                                } else {
                                    groupFollowers.put(getDayMonthYearFromMillis(time), 1);
                                }
                            }

                            if (userId.trim().equals(followedId)) {
                                Log.d("DEBUG", "INTO USERID EQUALS FOLLOWED");
                                if (groupFolloweds.containsKey(getDayMonthYearFromMillis(time))) {
                                    groupFolloweds.put(getDayMonthYearFromMillis(time), 
                                            groupFolloweds.get(getDayMonthYearFromMillis(time)) + 1);
                                } else {
                                    groupFolloweds.put(getDayMonthYearFromMillis(time), 1);
                                }
                            }

                            Log.d("DEBUG", "groupFollower size : " + groupFollowers.size());
                            Log.d("DEBUG", "groupFollowed size : " + groupFolloweds.size());

                            if (time >= fromdate) {
                                listTime.add(time);
                            }
                        }
                    }
                }

                int countFollower = 0;
                int countFollowed = 0;

                for (String timeStr : groupFolloweds.keySet()){
                    if (timeStr.compareTo(getDayMonthYearFromMillis(fromdate)) < 0 ){
                        countFollowed += groupFolloweds.get(timeStr);
                    }
                }

                for (String timeStr : groupFollowers.keySet()){
                    if (timeStr.compareTo(getDayMonthYearFromMillis(fromdate)) < 0 ){
                        countFollower += groupFollowers.get(timeStr);
                    }
                }

                Log.d("DEBUG", "countFollower : " + countFollower);
                Log.d("DEBUG", "countFollowed : " + countFollowed);

                ArrayList<Long> timeUnits = getTimeUnits(type);
                ArrayList<String> timeArr = new ArrayList<>();
                ArrayList<String> listTimeDayStr = new ArrayList<>();

                for (Long t : listTime){
                    Log.d("DEBUG", "time in listTime : " + t);
                    Log.d("DEBUG", "time in listTime : " + getDayMonthYearFromMillis(t));
                    listTimeDayStr.add(getDayStrFromMillis(t));
                }

                switch (type) {
                    case "Daily": {
                        for (Long time : timeUnits) {
                            timeArr.add(getDayStrFromMillis(time));
                            if (listTimeDayStr.contains(getDayStrFromMillis(time))){
                                Log.d("DEBUG", "listTime.contains(time) : " + getDayStrFromMillis(time));
                                if (groupFolloweds.containsKey(getDayMonthYearFromMillis(time))){
                                    countFollowed += groupFolloweds.get(getDayMonthYearFromMillis(time));
                                }

                                if (groupFollowers.containsKey(getDayMonthYearFromMillis(time))){
                                    countFollower += groupFollowers.get(getDayMonthYearFromMillis(time));
                                }
                            }

                            Log.d("DEBUG", "Group Followeds " + getDayStrFromMillis(time) + " has " + countFollowed + " people.");
                            barEntriesFolloweds.add(new BarEntry(timeUnits.indexOf(time), countFollowed));


                            Log.d("DEBUG", "Group Followers " + getDayStrFromMillis(time) + " has " + countFollower + " people.");
                            barEntriesFollowers.add(new BarEntry(timeUnits.indexOf(time), countFollower));
                        }

                        break;
                    }

                    case "Monthly": {
                        ArrayList<Integer> listMonth = new ArrayList<>();
                        for (Long month : timeUnits) {
                            listMonth.add(month.intValue());
                        }
                        listMonth = new ArrayList<>( listMonth.stream().distinct().collect(Collectors.toList()) );
                        Collections.reverse(listMonth);
                        for (int month : listMonth) {
                            timeArr.add(""+month);
                            Log.d("DEBUG", "listMonth - " + month);
                        }

                        Log.d("DEBUG", "key group followed size = " + groupFolloweds.size());
                        Log.d("DEBUG", "key group follower size = " + groupFollowers.size());

                        countFollowed = 0;
                        countFollower = 0;

                        for (int month : listMonth){
                            for (String timeStr : groupFolloweds.keySet()){
                                String monthTimeStr = timeStr.split("-")[1];
                                if (Integer.parseInt(monthTimeStr.trim()) == month){
                                    countFollowed += groupFolloweds.get(timeStr);
                                }
                            }
                            barEntriesFolloweds.add(new BarEntry(listMonth.indexOf(month), countFollowed));
                            Log.d("DEBUG", "TOTAL followed month " + month + " : " + countFollowed);

                            for (String timeStr : groupFollowers.keySet()){
                                Log.d("DEBUG", "timeStr : " + timeStr);
                                String monthTimeStr = timeStr.split("-")[1];
                                if (Integer.parseInt(monthTimeStr.trim()) == month){
                                    Log.d("timeStr month follower", timeStr);
                                    Log.d("debug", "groupFollowers.get(timeStr): "+groupFollowers.get(timeStr));
                                    countFollower += groupFollowers.get(timeStr);
                                }
                            }
                            barEntriesFollowers.add(new BarEntry(listMonth.indexOf(month), countFollower));
                            Log.d("DEBUG", "TOTAL follower month " + month + " : " + countFollower);
                        }
                        break;
                    }

                    case "Annually": {
                        ArrayList<Integer> listYear = new ArrayList<>();
                        for (Long year : timeUnits) {
                            listYear.add(year.intValue());
                        }
                        listYear = new ArrayList<>( listYear.stream().distinct().collect(Collectors.toList()) );
                        Collections.reverse(listYear);
                        for (int year : listYear) {
                            timeArr.add(""+year);
                            Log.d("DEBUG", "listYear - " + year);
                        }

                        Log.d("DEBUG", "key group followed size = " + groupFolloweds.size());
                        Log.d("DEBUG", "key group follower size = " + groupFollowers.size());

                        countFollowed = 0;
                        countFollower = 0;

                        for (int year : listYear){
                            for (String timeStr : groupFolloweds.keySet()){
                                String yearTimeStr = timeStr.split("-")[0];
                                if (Integer.parseInt(yearTimeStr.trim()) == year){
                                    countFollowed += groupFolloweds.get(timeStr);
                                }
                            }
                            barEntriesFolloweds.add(new BarEntry(listYear.indexOf(year), countFollowed));
                            Log.d("DEBUG", "TOTAL followed year " + year + " : " + countFollowed);

                            for (String timeStr : groupFollowers.keySet()){
                                Log.d("DEBUG", "timeStr : " + timeStr);
                                String yearTimeStr = timeStr.split("-")[0];
                                if (Integer.parseInt(yearTimeStr.trim()) == year){
                                    Log.d("timeStr year follower", timeStr);
                                    Log.d("debug", "groupFollowers.get(timeStr): "+groupFollowers.get(timeStr));
                                    countFollower += groupFollowers.get(timeStr);
                                }
                            }
                            barEntriesFollowers.add(new BarEntry(listYear.indexOf(year), countFollower));
                            Log.d("DEBUG", "TOTAL follower year " + year + " : " + countFollower);
                        }
                        break;
                    }
                }

                binding.tvNumFollower.setText(":    " + countFollower + " people");
                binding.tvNumFollowed.setText(":    " + countFollowed + " people");

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
                binding.bcFollow.setData(data);

                binding.bcFollow.getDescription().setEnabled(false);
                XAxis xAxis = binding.bcFollow.getXAxis();
                xAxis.setValueFormatter(new IndexAxisValueFormatter(timeArr.toArray(new String[timeArr.size()])));
                xAxis.setCenterAxisLabels(true);
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setGranularityEnabled(true);
                if (type.trim().equals("Monthly")) {
                    xAxis.setGranularity(0.5f);
                    Log.d("DEBUG", "Granularity 0.5f");
                    xAxis.setLabelCount(12);
                } else {
                    xAxis.setGranularity(1);

                }
                binding.bcFollow.setDragEnabled(true);
                binding.bcFollow.setVisibleXRangeMaximum(14);
                binding.bcFollow.setVisibleXRangeMinimum(timeArr.size() + 0.5f);

                float barSpace = 0.1f;
                float groupSpace = 0.5f;
                data.setBarWidth(0.15f);
                binding.bcFollow.getXAxis().setAxisMinimum(0);
                binding.bcFollow.animate();
                binding.bcFollow.groupBars(0, groupSpace, barSpace);
                binding.bcFollow.invalidate();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("DEBUG: ", "followRef cancelled");

            }
        });
    }
}