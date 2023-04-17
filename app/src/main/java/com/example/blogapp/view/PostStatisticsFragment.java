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
import com.example.blogapp.model.Blog;
import com.example.blogapp.model.LikedBlog;
import com.example.blogapp.viewmodel.DBHelper;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.TreeMap;

public class PostStatisticsFragment extends Fragment {
    private DatabaseReference likeRef;
    private DatabaseReference viewRef;
    private DatabaseReference commentRef;
    private DatabaseReference blogRef;

    TextView tvMostLikesPost;
    TextView tvMostViewsPost;
    BarChart bcOverall;
    BarDataSet bdsOverall;
    ArrayList barEntries;
    Button butPostChosen, butFollowChosen;
    Button butOverall, butLike, butView;

    public void addLikedBlog(String userId, String blogId){
        String id = likeRef.push().getKey();

        likeRef.child(id).setValue(new LikedBlog(userId, blogId))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Log.d("DEBUG", "Add likedBlog successfully");
                        } else {
                            Log.d("DEBUG", "Add likedBlog failed");
                        }
                    }
                });
    }

    public void addBlog(String blogId, String title, String content, String createdTime, String userId, int likesNumber, int viewsNumber, String category, String status){
        String id = likeRef.push().getKey();

        blogRef.child(id).setValue(new Blog(blogId, title, content, createdTime, userId, likesNumber, viewsNumber,  category, status))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Log.d("DEBUG", "Add Blog successfully");
                        } else {
                            Log.d("DEBUG", "Add Blog failed");
                        }
                    }
                });
    }


    public PostStatisticsFragment(){
        likeRef = FirebaseDatabase.getInstance().getReference().child("likedBlog");
        viewRef = FirebaseDatabase.getInstance().getReference().child("views");
        commentRef = FirebaseDatabase.getInstance().getReference().child("comments");
        blogRef = FirebaseDatabase.getInstance().getReference().child("blogs");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvMostLikesPost = view.findViewById(R.id.tv_most_likes_post);
        tvMostViewsPost = view.findViewById(R.id.tv_most_views_post);

//        addLikedBlog("user1", "blog3");
//        addLikedBlog("user2", "blog6");
//        addLikedBlog("user3", "blog7");
//        addLikedBlog("user1", "blog5");
//        addLikedBlog("user2", "blog5");
//        addLikedBlog("user3", "blog5");
//        addLikedBlog("user4", "blog5");
//        addLikedBlog("user5", "blog5");
//        addLikedBlog("user2", "blog1");
//        addLikedBlog("user1", "blog1");
//        addLikedBlog("user3", "blog1");
//        addLikedBlog("user5", "blog1");
//        addLikedBlog("user2", "blog2");
//        addLikedBlog("user2", "blog3");
//        addLikedBlog("user3", "blog3");


//        addBlog("blog1", "title1", "content1", "createdTime1", "userId1", 10, 20, "category1", "status1");
//        addBlog("blog2", "title2", "content2", "createdTime2", "userId2", 9, 10, "category2", "status2");
//        addBlog("blog3", "title3", "content3", "createdTime3", "userId3", 13, 15, "category3", "status3");
//        addBlog("blog4", "title4", "content4", "createdTime4", "userId4", 16, 8, "category4", "status4");
//        addBlog("blog5", "title5", "content5", "createdTime5", "userId5", 32, 8, "category5", "status5");
//        addBlog("blog6", "title6", "content6", "createdTime6", "userId6", 24, 6, "category6", "status6");
//        addBlog("blog7", "title7", "content7", "createdTime7", "userId7", 18, 7, "category7", "status7");


        bcOverall = view.findViewById(R.id.bc_overall);
        bcOverall = view.findViewById(R.id.bc_overall);
        butOverall = view.findViewById(R.id.but_overall);
        butLike = view.findViewById(R.id.but_likes);
        butView = view.findViewById(R.id.but_views);

        butFollowChosen = view.findViewById(R.id.but_follow_chosen_P);
        butPostChosen = view.findViewById(R.id.but_post_chosen_P);

        butPostChosen.setBackgroundColor(getResources().getColor(R.color.main_color));
        butFollowChosen.setBackgroundColor(getResources().getColor(R.color.white));
        butPostChosen.setTextColor(getResources().getColor(R.color.white));
        butFollowChosen.setTextColor(getResources().getColor(R.color.main_color));

        drawInitialBarChart();

        butFollowChosen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.followStatisticsFragment, null);
            }
        });

        butPostChosen.setOnClickListener(v -> drawInitialBarChart());

        butOverall.setOnClickListener(v -> drawOverallBarChart());
        butLike.setOnClickListener(v -> drawLikeBarChart());


    }

    private void drawInitialBarChart(){
        drawOverallBarChart();
    }

    @SuppressLint("RestrictedApi")
    private void drawBarChart(String[] xAxisValues, ArrayList<BarEntry> barEntries, String label, String colorHex) {
        bdsOverall = new BarDataSet(barEntries, label);
        bdsOverall.setColor(Color.parseColor(colorHex));

        BarData data = new BarData(bdsOverall);
        bcOverall.setData(data);
        bcOverall.getDescription().setEnabled(false);

        bcOverall.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        bcOverall.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xAxisValues));
        bcOverall.getXAxis().setLabelRotationAngle(-45f);
        bcOverall.getAxisLeft().setAxisMinimum(0f);
        bcOverall.getAxisLeft().setDrawGridLines(false);
        bcOverall.getAxisRight().setEnabled(false);
        bcOverall.getDescription().setEnabled(false);
        bcOverall.setFitBars(true);
        bcOverall.animateY(1000);

        Legend legend = bcOverall.getLegend();
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
        bcOverall.setData(data);
        bcOverall.getDescription().setEnabled(false);

        bcOverall.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        bcOverall.getXAxis().setValueFormatter(new IndexAxisValueFormatter(timeArr));
        bcOverall.getXAxis().setLabelRotationAngle(-45f);
        bcOverall.getAxisLeft().setAxisMinimum(0f);
        bcOverall.getAxisLeft().setDrawGridLines(false);
        bcOverall.getAxisRight().setEnabled(false);
        bcOverall.getDescription().setEnabled(false);
        bcOverall.setFitBars(true);
        bcOverall.animateY(1000);

        Legend legend = bcOverall.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setForm(Legend.LegendForm.SQUARE);
    }

    @SuppressLint("RestrictedApi")
    private void drawLikeBarChart() {
        ArrayList<BarEntry> barEntriesPosts = new ArrayList<>();
        Map<String, Integer> groupPosts = new LinkedHashMap<>();

        likeRef.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String blogId = snapshot.child("blogId").getValue(String.class);

                    if (groupPosts.containsKey(blogId)) {
                        groupPosts.put(blogId, groupPosts.get(blogId) + 1);
                    } else {
                        groupPosts.put(blogId, 1);
                    }
                }

                List<Map.Entry<String, Integer>> list = new ArrayList<>(groupPosts.entrySet());
                list.sort(Map.Entry.comparingByValue());
                Collections.reverse(list);

                groupPosts.clear();
                for (Map.Entry<String, Integer> e : list){
                    groupPosts.put(e.getKey(), e.getValue());
                }

                ArrayList<String> listLikedBlogIds = new ArrayList(groupPosts.keySet());
                for (int i = 0; i < 5; i++) {
                    Log.d("DEBUG", "blog no." + i + " => id=" + listLikedBlogIds.get(i));
                    Log.d("DEBUG", "=> likes=" + groupPosts.get(listLikedBlogIds.get(i)));
                    barEntriesPosts.add(new BarEntry(i, groupPosts.get(listLikedBlogIds.get(i))));
                }


                blogRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String[] listTitles = new String[5];
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String blogId = snapshot.child("blogId").getValue(String.class);
                            String title = snapshot.child("title").getValue(String.class);

                            int index = listLikedBlogIds.indexOf(blogId);
                            if (index == 0) {
                                tvMostLikesPost.setText(":   " + title);
                            }
                            if (index >= 0 && index < 5) {
                                listTitles[index] = title;
                            }
                        }
                        String titles = ":   " + listTitles[0];
                        int likeNums = groupPosts.get(listLikedBlogIds.get(0));
                        for (int i=1; i<listTitles.length; i++){
                            if (groupPosts.get(listLikedBlogIds.get(i)) == likeNums) {
                                titles += (", " + listTitles[i]);
                            } else {
                                break;
                            }
                        }

                        drawBarChart(listTitles, barEntriesPosts, "Likes", "#edcbd2");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d("DEBUG", "blogRef cancelled");
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("DEBUG", "likedBlogRef cancelled");
            }
        });
    }

    @SuppressLint("RestrictedApi")
    private void drawViewBarChart() {
        ArrayList<BarEntry> barEntriesPosts = new ArrayList<>();
        Map<String, Integer> groupPosts = new LinkedHashMap<>();

        viewRef.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String blogId = snapshot.child("blogId").getValue(String.class);

                    if (groupPosts.containsKey(blogId)) {
                        groupPosts.put(blogId, groupPosts.get(blogId) + 1);
                    } else {
                        groupPosts.put(blogId, 1);
                    }
                }

                List<Map.Entry<String, Integer>> list = new ArrayList<>(groupPosts.entrySet());
                list.sort(Map.Entry.comparingByValue());
                Collections.reverse(list);

                groupPosts.clear();
                for (Map.Entry<String, Integer> e : list){
                    groupPosts.put(e.getKey(), e.getValue());
                }

                ArrayList<String> listLikedBlogIds = new ArrayList(groupPosts.keySet());
                for (int i = 0; i < 5; i++) {
                    Log.d("DEBUG", "blog no." + i + " => id=" + listLikedBlogIds.get(i));
                    Log.d("DEBUG", "=> likes=" + groupPosts.get(listLikedBlogIds.get(i)));
                    barEntriesPosts.add(new BarEntry(i, groupPosts.get(listLikedBlogIds.get(i))));
                }


                blogRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String[] listTitles = new String[5];
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String blogId = snapshot.child("blogId").getValue(String.class);
                            String title = snapshot.child("title").getValue(String.class);

                            int index = listLikedBlogIds.indexOf(blogId);
                            if (index == 0) {
                                tvMostLikesPost.setText(":   " + title);
                            }
                            if (index >= 0 && index < 5) {
                                listTitles[index] = title;
                            }
                        }
                        String titles = ":   " + listTitles[0];
                        int likeNums = groupPosts.get(listLikedBlogIds.get(0));
                        for (int i=1; i<listTitles.length; i++){
                            if (groupPosts.get(listLikedBlogIds.get(i)) == likeNums) {
                                titles += (", " + listTitles[i]);
                            } else {
                                break;
                            }
                        }

                        drawBarChart(listTitles, barEntriesPosts, "Likes", "#edcbd2");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d("DEBUG", "blogRef cancelled");
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("DEBUG", "likedBlogRef cancelled");
            }
        });
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
}