package com.example.blogapp.view;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.blogapp.R;
import com.example.blogapp.databinding.FragmentBlogStatisticsBinding;
import com.example.blogapp.model.Blog;
import com.example.blogapp.model.User;
import com.github.mikephil.charting.components.Legend;
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

public class BlogStatisticsFragment extends Fragment {
    FragmentBlogStatisticsBinding binding;
    private final String monthYearPattern = "yyyy-MM";
    private User userLogin;
    private String userId, blogId;
    private Blog blogItem;
    BarDataSet bdsLikes, bdsComments;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userLogin = (User) getArguments().getSerializable("userLogin");
            blogItem = (Blog) getArguments().getSerializable("blogItem");
        }
        userId = "-NRlYm-P-HVbQtt_G2Zm";
        blogId = "-NRWzGau3B5daz75wUxS";
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.fragment_blog_statistics, container, false);
        View viewRoot = binding.getRoot();
        binding.btnDays.setOnClickListener(v -> {
            binding.btnDays.setBackgroundColor(getResources().getColor(R.color.main_color));
            binding.btnYears.setBackgroundColor(getResources().getColor(R.color.white));
            binding.btnMonths.setBackgroundColor(getResources().getColor(R.color.white));
            binding.btnDays.setTextColor(getResources().getColor(R.color.white));
            binding.btnYears.setTextColor(getResources().getColor(R.color.main_color));
            binding.btnMonths.setTextColor(getResources().getColor(R.color.main_color));
            drawBarChart("Days");
        });
        binding.btnMonths.setOnClickListener(v -> {
            binding.btnDays.setBackgroundColor(getResources().getColor(R.color.white));
            binding.btnYears.setBackgroundColor(getResources().getColor(R.color.white));
            binding.btnMonths.setBackgroundColor(getResources().getColor(R.color.main_color));
            binding.btnDays.setTextColor(getResources().getColor(R.color.main_color));
            binding.btnYears.setTextColor(getResources().getColor(R.color.main_color));
            binding.btnMonths.setTextColor(getResources().getColor(R.color.white));
            drawBarChart("Months");
        });
        binding.btnYears.setOnClickListener(v -> {
            binding.btnDays.setBackgroundColor(getResources().getColor(R.color.white));
            binding.btnYears.setBackgroundColor(getResources().getColor(R.color.main_color));
            binding.btnMonths.setBackgroundColor(getResources().getColor(R.color.white));
            binding.btnDays.setTextColor(getResources().getColor(R.color.main_color));
            binding.btnYears.setTextColor(getResources().getColor(R.color.white));
            binding.btnMonths.setTextColor(getResources().getColor(R.color.main_color));
            drawBarChart("Years");
        });

        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.logout_menu, menu);
                if (getActivity() != null) {
                    ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
                }
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == android.R.id.home){
                    getActivity().onBackPressed();
                    return true;
                }
                return false;
            }
        });
        return viewRoot;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onStart() {
        super.onStart();
        getBlogLikesComments();
        drawBarChart("Days");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Date getFromDate(String type){
        Date fromdate = null;
        switch (type) {
            case "Days":
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, -7);
                fromdate = cal.getTime();
                break;
            case "Months": {
                DateFormat dateFormat = new SimpleDateFormat(monthYearPattern);
                String now = dateFormat.format(new Date());
                YearMonth stop = YearMonth.parse(now);
                YearMonth start = stop.minusYears(1);
                Log.d("start month", "" + start.getMonth().getValue());
                fromdate = new GregorianCalendar(start.getYear(), start.getMonthValue(), 1).getTime();
                break;
            }
            case "Years": {
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
            case "Days":{
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
                break;
            }

            case "Months": {
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
            case "Years": {
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

    private void getBlogLikesComments(){
        DatabaseReference likeRef = FirebaseDatabase.getInstance().getReference("likedBlogs");
        DatabaseReference cmtRef = FirebaseDatabase.getInstance().getReference("comments");
        DatabaseReference blogRef = FirebaseDatabase.getInstance().getReference("blogs");

        blogRef.child(blogId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot s) {
                String title = s.child("title").getValue(String.class);
                binding.tvBlogName.setText(title);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        likeRef.child(blogId).get().addOnCompleteListener(t -> {
            DataSnapshot rs = t.getResult();
            long count = rs.getChildrenCount();
            binding.tvLikesCount.setText(""+count);
        });

        cmtRef.child(blogId).get().addOnCompleteListener(t -> {
            DataSnapshot rs = t.getResult();
            long count = rs.getChildrenCount();
            binding.tvCmtCount.setText(""+count);
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void drawBarChart(String type){
        DatabaseReference cmtRef = FirebaseDatabase.getInstance().getReference("comments");

        Date fromdate = getFromDate(type);
        Date todate = new Date();
        String timeRangeStr = "";

        switch(type){
            case "Days": {
                timeRangeStr = getDayStrFromMillis(fromdate.getTime()) + " - " + getDayStrFromMillis(todate.getTime());
                break;
            }
            case "Months": {
                int monthfrom = getYearMonthFromMillis(fromdate.getTime()).getMonthValue();
                int monthto = getYearMonthFromMillis(todate.getTime()).getMonthValue();
                int yearfrom = getYearMonthFromMillis(fromdate.getTime()).getYear();
                int yearto = getYearMonthFromMillis(todate.getTime()).getYear();
                timeRangeStr = monthfrom + "/"+ yearfrom + " - " + monthto + "/"+ yearto;
                break;
            }
            case "Years": {
                timeRangeStr = getYearMonthFromMillis(fromdate.getTime()).getYear() + " - "
                        + getYearMonthFromMillis(todate.getTime()).getYear();
                break;
            }
        }
//        binding.tvTime.setText(timeRangeStr);

        ArrayList<BarEntry> barEntriesComments = new ArrayList<>();
        Map<String, Integer> groupComments = new TreeMap<>();

        cmtRef.child(blogId).get().addOnCompleteListener(t -> {
            DataSnapshot rs = t.getResult();
            Long fromdateLong = fromdate.getTime();
            Set<Long> listTime = new TreeSet<>();

            for(DataSnapshot s : rs.getChildren()){
                String cmtId = s.getKey();

                Long time = s.child("createdTime").getValue(Long.class);
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DATE, 1);
                c.set(Calendar.HOUR, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                if (time < c.getTimeInMillis()){
                    String key = getDayMonthYearFromMillis(time);
                    if (groupComments.containsKey(key)){
                        groupComments.put(key, groupComments.get(key) +  1);
                    } else {
                        groupComments.put(key, 1);
                    }
                    if (time >= fromdateLong) {
                        listTime.add(time);
                    }
                }
            }


            int countComments = 0;

            for (String timeStr : groupComments.keySet()){
                if (timeStr.compareTo(getDayMonthYearFromMillis(fromdateLong)) < 0 ){
                    countComments += groupComments.get(timeStr);
                }
            }

            ArrayList<Long> timeUnits = getTimeUnits(type);
            ArrayList<String> timeArr = new ArrayList<>();
            ArrayList<String> listTimeDayStr = new ArrayList<>();

            for (Long temp : listTime){
                listTimeDayStr.add(getDayStrFromMillis(temp));
            }

            switch (type){
                case "Days": {
                    for(long time:timeUnits){
                        timeArr.add(getDayStrFromMillis(time));
                        if (listTimeDayStr.contains(getDayStrFromMillis(time))){
                            if (groupComments.containsKey(getDayMonthYearFromMillis(time))){
                                countComments += groupComments.get(getDayMonthYearFromMillis(time));
                            }
                        }
                        Log.d("DEBUG", "Group comments " + getDayStrFromMillis(time) + " has " + countComments + " people.");
                        barEntriesComments.add(new BarEntry(timeUnits.indexOf(time), countComments));
                    }
                    break;
                }
                case "Months": {
                    ArrayList<Integer> listMonth = new ArrayList<>();
                        for (Long month : timeUnits) {
                            listMonth.add(month.intValue());
                        }
                        listMonth = new ArrayList<>( listMonth.stream().distinct().collect(Collectors.toList()) );
                        Collections.reverse(listMonth);
                        for (int month : listMonth) {
                            timeArr.add(""+month);
                        }

                        countComments = 0;

                        for (int month : listMonth){
                            for (String timeStr : groupComments.keySet()){
                                String monthTimeStr = timeStr.split("-")[1];
                                if (Integer.parseInt(monthTimeStr.trim()) == month){
                                    countComments += groupComments.get(timeStr);
                                }
                            }
                            barEntriesComments.add(new BarEntry(listMonth.indexOf(month), countComments));
                        }
                        break;
                }
                case "Years": {
                    ArrayList<Integer> listYear = new ArrayList<>();
                    for (Long year : timeUnits) {
                        listYear.add(year.intValue());
                    }
                    listYear = new ArrayList<>( listYear.stream().distinct().collect(Collectors.toList()) );
                    Collections.reverse(listYear);
                    for (int year : listYear) {
                        timeArr.add(""+year);
                    }

                    countComments = 0;

                    for (int year : listYear){
                        for (String timeStr : groupComments.keySet()){
                            String yearTimeStr = timeStr.split("-")[0];
                            if (Integer.parseInt(yearTimeStr.trim()) == year){
                                countComments += groupComments.get(timeStr);
                            }
                        }
                        barEntriesComments.add(new BarEntry(listYear.indexOf(year), countComments));
                    }
                    break;
                }
            }

            bdsComments = new BarDataSet(barEntriesComments, "Comments");

            BarData data = new BarData(bdsComments);
            bdsComments.setColor(Color.parseColor("#e3856b"));
            binding.bcBlog.setData(data);
            binding.bcBlog.getDescription().setEnabled(false);
            binding.bcBlog.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            binding.bcBlog.getXAxis().setValueFormatter(new IndexAxisValueFormatter(timeArr.toArray(new String[timeArr.size()])));
            binding.bcBlog.getAxisLeft().setAxisMinimum(0f);
            binding.bcBlog.getAxisLeft().setDrawGridLines(false);
            binding.bcBlog.getAxisRight().setEnabled(false);
            if (type.trim().equals("Months")) {
                binding.bcBlog.getXAxis().setGranularity(0.5f);
                binding.bcBlog.getXAxis().setLabelCount(12);
            } else {
                binding.bcBlog.getXAxis().setGranularity(1);
            }
            binding.bcBlog.getDescription().setEnabled(false);
            binding.bcBlog.setFitBars(true);
            binding.bcBlog.animateY(1000);

            Legend legend = binding.bcBlog.getLegend();
            legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
            legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
            legend.setOrientation(Legend.LegendOrientation.VERTICAL);
            legend.setDrawInside(false);
            legend.setForm(Legend.LegendForm.SQUARE);
        });
    }
}