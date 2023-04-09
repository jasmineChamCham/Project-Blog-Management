package com.example.blogapp.view;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.text.format.DateUtils;
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
import com.example.blogapp.model.Comment;
import com.example.blogapp.model.User;
import com.example.blogapp.viewmodel.DBHelper;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.DefaultAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlogStatisticsFragment extends Fragment {

    FragmentBlogStatisticsBinding binding;
    private DBHelper dbHelper;
    private User userLogin;
    private Blog blogItem;
    private List<Comment> comments;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userLogin = (User) getArguments().getSerializable("userLogin");
            blogItem = (Blog) getArguments().getSerializable("blogItem");
        }
    }
    public class DataPoint{
        int xValue, yValue;
        public DataPoint(){}
        public DataPoint(int xValue, int yValue){
            this.xValue = xValue;
            this.yValue = yValue;
        }

        public int getxValue() {
            return xValue;
        }

        public void setxValue(int xValue) {
            this.xValue = xValue;
        }

        public int getyValue() {
            return yValue;
        }

        public void setyValue(int yValue) {
            this.yValue = yValue;
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.fragment_blog_statistics, container, false);
        View viewRoot = binding.getRoot();
        Log.d("DEBUG",blogItem.getBlogId());


//        dbHelper = new DBHelper(viewRoot.getContext());

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference commentsRef = database.getReference().child("comments").child(blogItem.getBlogId());
        // Set the end date to today's date
        Calendar endDate = Calendar.getInstance();
        endDate.set(Calendar.HOUR_OF_DAY, 23);
        endDate.set(Calendar.MINUTE, 59);
        endDate.set(Calendar.SECOND, 59);
        endDate.set(Calendar.MILLISECOND, 999);

        // Set the start date to 10 days ago
        Calendar startDate = Calendar.getInstance();
        startDate.setTime(endDate.getTime());
        startDate.add(Calendar.DAY_OF_MONTH, -10);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        // Use the formatted start and end dates in the Firebase query
        Query query = commentsRef.orderByChild("createdTime").startAt(startDate.getTimeInMillis()).endAt(endDate.getTimeInMillis() + 1);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("DEBUG", Long.toString(dataSnapshot.getChildrenCount()));
                // Step 2: Organize comment data into map of dates and comment counts
                Map<String, Integer> commentCounts = new HashMap<>();
                for (DataSnapshot commentSnapshot : dataSnapshot.getChildren()) {
                    Comment comment = commentSnapshot.getValue(Comment.class);
                    Date date = new Date(comment.getCreatedTime());
                    String dateString = dateFormat.format(date);
                    if (commentCounts.containsKey(dateString)) {
                        commentCounts.put(dateString, commentCounts.get(dateString) + 1);
                    } else {
                        commentCounts.put(dateString, 1);
                    }
                }
                // Step 3: Fill in missing dates with 0 comments
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(startDate.getTime());
                while (calendar.getTimeInMillis() <= endDate.getTimeInMillis()) {
                    String dateString = dateFormat.format(calendar.getTime());
                    if (!commentCounts.containsKey(dateString)) {
                        commentCounts.put(dateString, 0);
                    }
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }
                // Step 4: Populate chart with data
                List<BarEntry> entries = new ArrayList<>();
                int index = 0;
                for (Map.Entry<String, Integer> entry : commentCounts.entrySet()) {
                    String date = entry.getKey();
                    int count = entry.getValue();
                    entries.add(new BarEntry(index, count));
                    index++;
                }
                BarDataSet dataSet = new BarDataSet(entries, "Comment Count");
                BarData barData = new BarData(dataSet);
                binding.bcBlog.setData(barData);
                binding.bcBlog.invalidate(); // refresh chart
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("DEBUG", "Error retrieving comments", databaseError.toException());
            }
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
}