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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.blogapp.R;
import com.example.blogapp.databinding.FragmentPostStatisticsBinding;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PostStatisticsFragment extends Fragment {
    private FragmentPostStatisticsBinding binding;
    private final DatabaseReference blogRef;
    private final DatabaseReference likedBlogRef;
    private final DatabaseReference cmtRef;

    BarDataSet bdsOverall;
    ArrayList<BarEntry> barEntries;

    private String userId; // author

    public PostStatisticsFragment(){
        blogRef = FirebaseDatabase.getInstance().getReference("blogs");
        likedBlogRef = FirebaseDatabase.getInstance().getReference("likedBlogs");
        cmtRef = FirebaseDatabase.getInstance().getReference("comments");

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPostStatisticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        if (getActivity().getIntent() != null ){
            Bundle bundle = getActivity().getIntent().getBundleExtra("userBundle");
            User userLogin = (User) bundle.getSerializable("userLogin");
            userId = userLogin.getUserId();
        } else {
            userId = "-NRlYm-P-HVbQtt_G2Zm";
        }

        binding.butPostChosenP.setBackgroundColor(getResources().getColor(R.color.main_color));
        binding.butFollowChosenP.setBackgroundColor(getResources().getColor(R.color.white));
        binding.butPostChosenP.setTextColor(getResources().getColor(R.color.white));
        binding.butFollowChosenP.setTextColor(getResources().getColor(R.color.main_color));

        binding.butFollowChosenP.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.followStatisticsFragment, null));
        binding.butPostChosenP.setOnClickListener(v -> drawOverallBarChart());
        binding.butOverall.setOnClickListener(v -> {
            drawOverallBarChart();
            binding.butOverall.setBackgroundColor(getResources().getColor(R.color.light_blue));
            binding.butLikes.setBackgroundColor(Color.parseColor("#52B8E6"));
            binding.butComments.setBackgroundColor(Color.parseColor("#52B8E6"));
        });
        binding.butLikes.setOnClickListener(v -> {
            drawLikeBarChart();
            binding.butLikes.setBackgroundColor(getResources().getColor(R.color.light_blue));
            binding.butOverall.setBackgroundColor(Color.parseColor("#52B8E6"));
            binding.butComments.setBackgroundColor(Color.parseColor("#52B8E6"));
        } );
        binding.butComments.setOnClickListener(v -> {
            drawCmtBarChart();
            binding.butComments.setBackgroundColor(getResources().getColor(R.color.light_blue));
            binding.butOverall.setBackgroundColor(Color.parseColor("#52B8E6"));
            binding.butLikes.setBackgroundColor(Color.parseColor("#52B8E6"));
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        drawOverallBarChart();
    }

    private void drawLikeBarChart() {
        getBlogsMostLikesAndComments();

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

                    assert author != null;
                    if (author.equals(userId)){
                        mapBlogsOfUsers.putIfAbsent(blogId, title);
                    }
                }

                likedBlogRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshotLike) {
                        for (DataSnapshot snapshot : dataSnapshotLike.getChildren()) {
                            String blogId = snapshot.getKey();
                            if (mapBlogsOfUsers.containsKey(blogId)){
                                groupPosts.put(blogId, (int) snapshot.getChildrenCount());
                            }
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
                            barEntriesPosts.add(new BarEntry(i+1, groupPosts.get(listLikedBlogIds.get(i))));
                        }

                        String[] listTitles = new String[listLikedBlogIds.size() + 1];
                        listTitles[0] = "";
                        listTitles[1] = shortenTitle( mapBlogsOfUsers.get(listLikedBlogIds.get(0)) , false);

                        for (int i=1; i<listLikedBlogIds.size(); i++){
                            listTitles[i+1] = shortenTitle(mapBlogsOfUsers.get(listLikedBlogIds.get(i)) , false);
                        }

                        drawBarChart(listTitles, barEntriesPosts, new String[]{"Likes"}, Color.parseColor("#80c4b7"));
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

    private void drawCmtBarChart(){
        getBlogsMostLikesAndComments();

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

                    if (author.equals(userId)){
                        mapBlogsOfUsers.putIfAbsent(blogId, title);
                    }
                }

                cmtRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshotLike) {
                        for (DataSnapshot snapshot : dataSnapshotLike.getChildren()) {
                            String blogId = snapshot.getKey();
                            if (mapBlogsOfUsers.keySet().contains(blogId)){
                                groupPosts.put(blogId, (int) snapshot.getChildrenCount());
                            }
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

                        ArrayList<String> listCommentedBlogIds = new ArrayList(groupPosts.keySet());
                        for (int i = 0; i < listCommentedBlogIds.size(); i++) {
                            Log.d("DEBUG", "blog no." + i + " => id=" + listCommentedBlogIds.get(i));
                            Log.d("DEBUG", "=> comments=" + groupPosts.get(listCommentedBlogIds.get(i)));
                            barEntriesPosts.add(new BarEntry(i+1, groupPosts.get(listCommentedBlogIds.get(i))));
                        }

                        String[] listTitles = new String[listCommentedBlogIds.size() + 1];
                        listTitles[0] = "";
                        listTitles[1] = shortenTitle( mapBlogsOfUsers.get(listCommentedBlogIds.get(0)) , false );

                        for (int i=1; i<listCommentedBlogIds.size(); i++){
                            listTitles[i+1] = shortenTitle(mapBlogsOfUsers.get(listCommentedBlogIds.get(i)), false);
                        }

                        drawBarChart(listTitles, barEntriesPosts, new String[]{"Comments"}, Color.parseColor("#e3856b"));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d("DEBUG", "cmtRef cancelled");
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
    private void drawOverallBarChart() {
        getBlogsMostLikesAndComments();

        int[] colors = new int[]{Color.parseColor("#80c4b7"), Color.parseColor("#e3856b")};
        String[] labels = new String[]{"Likes", "Comments"};

        blogRef.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, String> mapBlogs = new LinkedHashMap<>();
                ArrayList<BarEntry> barEntriesPosts = new ArrayList<>();
                Map<String, Map<String, Integer>> groupPosts = new LinkedHashMap<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String blogId = snapshot.child("blogId").getValue(String.class);
                    String author = snapshot.child("userId").getValue(String.class);
                    String title = snapshot.child("title").getValue(String.class);

                    if (author.equals(userId)){
                        mapBlogs.putIfAbsent(blogId, title);
                    }
                }

                likedBlogRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshotLike) {

                        for (DataSnapshot snapshot : dataSnapshotLike.getChildren()) {
                            Map<String, Integer> mapLikesComments = new HashMap<>();
                            String blogId = snapshot.getKey();
                            if (mapBlogs.keySet().contains(blogId)){
                                int numLikes = (int) snapshot.getChildrenCount();
                                Log.d("DEBUG", "blog " + mapBlogs.get(blogId) + " has " +
                                        + numLikes + " likes");
                                mapLikesComments.put("likes" , numLikes);

                                Log.d("DEBUG in likeRef", "mapLikesComment.like = " + mapLikesComments.get("likes"));
                                Log.d("DEBUG in likeRef", "mapLikesComment.comments = " + mapLikesComments.get("comments"));

                                groupPosts.put(blogId, mapLikesComments);
                            }
                        }

                        cmtRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshotComment) {

                                for (DataSnapshot snapshot : dataSnapshotComment.getChildren()) {
                                    String blogId = snapshot.getKey();
                                    if (mapBlogs.keySet().contains(blogId)){
                                        int numComments = (int) snapshot.getChildrenCount();
                                        Log.d("DEBUG", "blog " + mapBlogs.get(blogId) + " has " +
                                                + numComments + " comments");

                                        Map<String, Integer> mapLikesComments = groupPosts.get(blogId);
                                        if (mapLikesComments == null) {
                                            mapLikesComments = new HashMap<>();
                                        }
                                        mapLikesComments.put("comments", numComments);
                                        Log.d("DEBUG in cmtRef", "mapLikesComment.like = " + mapLikesComments.get("likes"));
                                        Log.d("DEBUG in cmtRef", "mapLikesComment.comments = " + mapLikesComments.get("comments"));

                                        groupPosts.put(blogId, mapLikesComments);
                                    }
                                }

                                ArrayList<String> listCommentedBlogIds = new ArrayList(groupPosts.keySet());
                                for (int i = 0; i < listCommentedBlogIds.size(); i++) {
                                    String blogId = listCommentedBlogIds.get(i);
                                    int numLikes = 0;
                                    if (groupPosts.get(blogId).containsKey("likes")){
                                        numLikes = groupPosts.get(blogId).get("likes");
                                    }
                                    int numComments = 0;
                                    if (groupPosts.get(blogId).containsKey("comments")){
                                        numComments = groupPosts.get(blogId).get("comments");
                                    }
                                    barEntriesPosts.add(new BarEntry(i+1, new float[] {numLikes, numComments}));
                                }

                                String[] listTitles = new String[listCommentedBlogIds.size() + 1];
                                listTitles[0] = "";
                                listTitles[1] = shortenTitle( mapBlogs.get(listCommentedBlogIds.get(0)) , false );

                                for (int i=1; i<listCommentedBlogIds.size(); i++){
                                    listTitles[i+1] = shortenTitle(mapBlogs.get(listCommentedBlogIds.get(i)), false);
                                }

                                drawBarChart(listTitles, barEntriesPosts, labels, colors);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d("DEBUG", "cmtRef cancelled");
                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d("DEBUG","likedBlogRef in overall barchart cancelled");
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
    private void drawBarChart(String[] xAxisValues, ArrayList<BarEntry> barEntries, String[] label, int ...colorHex) {
        if (label.length == 1){
            bdsOverall = new BarDataSet(barEntries, label[0]);
        } else {
            bdsOverall = new BarDataSet(barEntries, "ALL POSTS");
            bdsOverall.setStackLabels(label);
        }
        if (colorHex.length == 1)
            bdsOverall.setColor(colorHex[0]);
        else {
            bdsOverall.setColors(colorHex);
        }
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

    private void getBlogsMostLikesAndComments(){
        blogRef.get().addOnCompleteListener(t -> {
           DataSnapshot rsBlogs = t.getResult();
           Map<String, String> mapBlogs = new HashMap<>();

           for (DataSnapshot ds : rsBlogs.getChildren()){
               String blogId = ds.getKey();
               String author = ds.child("userId").getValue(String.class);
               if (author.equals(userId)){
                   String title = ds.child("title").getValue(String.class);
                   mapBlogs.put(blogId, title);
               }
           }
           likedBlogRef.get().addOnCompleteListener(tLikes -> {
              DataSnapshot rsLikes = tLikes.getResult();
              long max = Long.MIN_VALUE;
              String title = "";
              for (DataSnapshot ds : rsLikes.getChildren()){
                  String blogId = ds.getKey();
                  if (mapBlogs.containsKey(blogId)){
                      long numLikes = ds.child(blogId).getChildrenCount();
                      if (numLikes > max){
                          max = numLikes;
                          title = mapBlogs.get(blogId);
                      }
                  }
              }
              String titleOfMostLikesBlog = shortenTitle(title, true);
              binding.tvMostLikesPost.setText(titleOfMostLikesBlog);
           });
           cmtRef.get().addOnCompleteListener(tComments -> {
               DataSnapshot rsLikes = tComments.getResult();
               long max = Long.MIN_VALUE;
               String title = "";
               for (DataSnapshot ds : rsLikes.getChildren()){
                   String blogId = ds.getKey();
                   if (mapBlogs.containsKey(blogId)) {
                       long numLikes = ds.child(blogId).getChildrenCount();
                       if (numLikes > max){
                           max = numLikes;
                           title = mapBlogs.get(blogId);
                       }
                   }
               }
               String titleOfMostCommentsBlog = shortenTitle(title, true);
               binding.tvMostCommentsPost.setText(titleOfMostCommentsBlog);
            });

        });

    }

    private String shortenTitle(String title, boolean isForTitle){
        if (isForTitle){
            if (title.length() > 20) {
                return title.substring(0,20) + "...";
            } else {
                return title;
            }
        } else if (title.length() > 8) {
            return title.substring(0,8) + "...";
        } else {
            return title;
        }
    }
}