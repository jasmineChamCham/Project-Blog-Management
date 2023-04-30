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

import com.example.blogapp.R;
import com.example.blogapp.databinding.FragmentPostStatisticsBinding;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PostStatisticsFragment extends Fragment {
    private FragmentPostStatisticsBinding binding;
    private final DatabaseReference blogRef;
    private final DatabaseReference likedBlogRef;
    private final DatabaseReference commentRef;

    BarDataSet bdsOverall;
    ArrayList barEntries;

    private String userId = "-NRvClt0Ahu_stjh3Z5G"; // author

    public PostStatisticsFragment(){
        blogRef = FirebaseDatabase.getInstance().getReference("blogs");
        likedBlogRef = FirebaseDatabase.getInstance().getReference("likedBlogs");
        commentRef = FirebaseDatabase.getInstance().getReference("comments");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPostStatisticsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.butPostChosenP.setBackgroundColor(getResources().getColor(R.color.main_color));
        binding.butFollowChosenP.setBackgroundColor(getResources().getColor(R.color.white));
        binding.butPostChosenP.setTextColor(getResources().getColor(R.color.white));
        binding.butFollowChosenP.setTextColor(getResources().getColor(R.color.main_color));

        binding.butFollowChosenP.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.followStatisticsFragment, null));
        binding.butPostChosenP.setOnClickListener(v -> drawOverallBarChart());
        binding.butOverall.setOnClickListener(v -> drawOverallBarChart());
        binding.butLikes.setOnClickListener(v -> drawLikeBarChart());
    }

    @Override
    public void onStart() {
        super.onStart();
        drawLikeBarChart();
    }

    private void drawLikeBarChart() {
        Log.d("DEBUG", "BEFORE BLOGREF");

        blogRef.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, String> mapBlogsOfUsers = new LinkedHashMap<>();
                ArrayList<BarEntry> barEntriesPosts = new ArrayList<>();
                Map<String, Integer> groupPosts = new LinkedHashMap<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String blogId = snapshot.child("blogId").getValue(String.class);
                    String author = snapshot.child("userId").getValue(String.class);
                    String title = snapshot.child("title").getValue(String.class);

                    Log.d("DEBUG","blogRef snapshot : " + blogId);
                    Log.d("DEBUG","blogRef snapshot author : " + author);
                    Log.d("DEBUG","blogRef snapshot title : " + title);

                    if (author.equals(userId)){
                        mapBlogsOfUsers.putIfAbsent(blogId, title);
                    }
                }

                for (Map.Entry<String, String> s : mapBlogsOfUsers.entrySet()){
                    Log.d("DEBUG", "list title blogs of user : " + s.getValue());
                }

                likedBlogRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshotLike) {
                        Log.d("DEBUG", "INTO LIKEREF");
                        for (DataSnapshot snapshot : dataSnapshotLike.getChildren()) {
                            String blogId = snapshot.getKey();
                            if (mapBlogsOfUsers.keySet().contains(blogId)){
                                groupPosts.put(blogId, (int) snapshot.getChildrenCount());
                            }
                        }

                        Log.d("DEBUG", "groupPosts size = " + groupPosts.size());
                        for (Map.Entry<String, Integer> e : groupPosts.entrySet()){
                            Log.d("groupPosts", "blogId = " + e.getKey() + ", numLikes = " + e.getValue());
                        }

                        // sort groupPosts according to value (number of likes)
                        List<Map.Entry<String, Integer>> list = new ArrayList<>(groupPosts.entrySet());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            list.sort(Map.Entry.comparingByValue());
                        }
                        Collections.reverse(list);
                        groupPosts.clear();
                        for (Map.Entry<String, Integer> e : list){
                            groupPosts.put(e.getKey(), e.getValue());
                        }

                        ArrayList<String> listLikedBlogIds = new ArrayList(groupPosts.keySet());
                        for (int i = 0; i < listLikedBlogIds.size(); i++) {
                            Log.d("DEBUG", "blog no." + i + " => id=" + listLikedBlogIds.get(i));
                            Log.d("DEBUG", "=> likes=" + groupPosts.get(listLikedBlogIds.get(i)));
                            barEntriesPosts.add(new BarEntry(i, groupPosts.get(listLikedBlogIds.get(i))));
                        }

                        String titles = shortenTitle(mapBlogsOfUsers.get(listLikedBlogIds.get(0)));
                        int likeNums = groupPosts.get(listLikedBlogIds.get(0));
                        String[] listTitles = new String[listLikedBlogIds.size()];
                        listTitles[0] = mapBlogsOfUsers.get(listLikedBlogIds.get(0));

                        for (int i=1; i<listLikedBlogIds.size(); i++){
                            listTitles[i] = shortenTitle(mapBlogsOfUsers.get(listLikedBlogIds.get(i)));
                            if (groupPosts.get(listLikedBlogIds.get(i)) == likeNums) {
                                titles += (", " + shortenTitle(mapBlogsOfUsers.get(listLikedBlogIds.get(i))));
                            } else {
                                break;
                            }
                        }

                        for (String t : listTitles){
                            Log.d("DEBUG", "listTitle : " + t);
                        }

                        binding.tvMostLikesPost.setText(titles);

                        drawBarChart(listTitles, barEntriesPosts, "Likes", "#edcbd2");

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d("DEBUG", "likeRef cancelled");
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("DEBUG", "CANCELLED BLOGREF");

            }
        });
    }

    @SuppressLint("RestrictedApi")
    private void drawBarChart(String[] xAxisValues, ArrayList<BarEntry> barEntries, String label, String colorHex) {
        bdsOverall = new BarDataSet(barEntries, label);
        bdsOverall.setColor(Color.parseColor(colorHex));

        BarData data = new BarData(bdsOverall);
        binding.bcOverall.setData(data);
        binding.bcOverall.getDescription().setEnabled(false);

        binding.bcOverall.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        binding.bcOverall.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xAxisValues));
        binding.bcOverall.getXAxis().setLabelRotationAngle(-45f);
        binding.bcOverall.getAxisLeft().setAxisMinimum(0f);
        binding.bcOverall.getAxisLeft().setDrawGridLines(false);
        binding.bcOverall.getAxisRight().setEnabled(false);
        binding.bcOverall.getDescription().setEnabled(false);
        binding.bcOverall.setFitBars(true);
        binding.bcOverall.animateY(1000);

        Legend legend = binding.bcOverall.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setForm(Legend.LegendForm.SQUARE);
    }


    @SuppressLint("RestrictedApi")
    private void drawOverallBarChart() {
        String[] timeArr = new String[]{"", "Mondayhihi", "Tuesdayhihi", "Wednesdayhihi", "Thursday", "Fridaysfd"};
        int[] colors = new int[]{Color.parseColor("#edcbd2"), Color.parseColor("#80c4b7"), Color.parseColor("#e3856b")};
        bdsOverall = new BarDataSet(getBarEntriesFollower(), "TOP POSTS");
        bdsOverall.setColors(colors);
        bdsOverall.setStackLabels(new String[]{"Likes", "Views", "Comments"});

        BarData data = new BarData(bdsOverall);
        binding.bcOverall.setData(data);
        binding.bcOverall.getDescription().setEnabled(false);

        binding.bcOverall.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        binding.bcOverall.getXAxis().setValueFormatter(new IndexAxisValueFormatter(timeArr));
        binding.bcOverall.getXAxis().setLabelRotationAngle(-45f);
        binding.bcOverall.getAxisLeft().setAxisMinimum(0f);
        binding.bcOverall.getAxisLeft().setDrawGridLines(false);
        binding.bcOverall.getAxisRight().setEnabled(false);
        binding.bcOverall.getDescription().setEnabled(false);
        binding.bcOverall.setFitBars(true);
        binding.bcOverall.animateY(1000);

        Legend legend = binding.bcOverall.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setForm(Legend.LegendForm.SQUARE);
    }

    private ArrayList<BarEntry> getBarEntriesFollower() {
        barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(1f, new float[]{2, 4.5f, 4}));
        barEntries.add(new BarEntry(2f, new float[]{5, 7.3f, 3}));
        barEntries.add(new BarEntry(3f, new float[]{3, 2.3f, 7}));
        barEntries.add(new BarEntry(4f, new float[]{7, 10.2f, 2}));
        barEntries.add(new BarEntry(5f, new float[]{16, 8.3f, 2}));

        return barEntries;
    }

    private String shortenTitle(String title){
        if (title.length() > 25) {
            return title.substring(0,25) + "...";
        } else {
            return title;
        }
    }

}