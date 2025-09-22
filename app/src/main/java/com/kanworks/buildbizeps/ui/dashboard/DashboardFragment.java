package com.kanworks.buildbizeps.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.kanworks.buildbizeps.databinding.FragmentDashboardBinding;
import com.kanworks.buildbizeps.data.database.FitnessDatabase;
import com.kanworks.buildbizeps.data.entity.Exercise;
import com.kanworks.buildbizeps.data.entity.ExerciseRecord;
import com.kanworks.buildbizeps.data.model.DailySummary;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private FitnessDatabase database;
    private ExecutorService executor;
    private Date selectedDate;
    private SimpleDateFormat dateFormat;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        // Initialize database and executor
        database = FitnessDatabase.getDatabase(getContext());
        executor = Executors.newSingleThreadExecutor();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        
        // Set default selected date to today
        selectedDate = new Date();
        
        setupCalendarView();
        loadDailySummary(selectedDate);
        
        return root;
    }
    
    private void setupCalendarView() {
        binding.calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            selectedDate = calendar.getTime();
            loadDailySummary(selectedDate);
        });
    }
    
    private void loadDailySummary(Date date) {
        executor.execute(() -> {
            try {
                // Get all exercise records for the selected date
                List<ExerciseRecord> records = database.exerciseRecordDao().getRecordsByDate(date);
                
                // Calculate summary data
                int totalBiceps = 0;
                int totalPullups = 0;
                int totalSets = 0;
                
                // Get exercise IDs (assuming we know them or can query)
                List<Exercise> exercises = database.exerciseDao().getAllExercises();
                int bicepsId = -1, pullupsId = -1;
                
                for (Exercise exercise : exercises) {
                    if (exercise.getName().toLowerCase().contains("biceps")) {
                        bicepsId = exercise.getId();
                    } else if (exercise.getName().toLowerCase().contains("pullup") || 
                              exercise.getName().toLowerCase().contains("pull-up")) {
                        pullupsId = exercise.getId();
                    }
                }
                
                // Count reps for each exercise
                for (ExerciseRecord record : records) {
                    if (record.getExerciseId() == bicepsId) {
                        totalBiceps += record.getReps();
                    } else if (record.getExerciseId() == pullupsId) {
                        totalPullups += record.getReps();
                    }
                    totalSets += record.getSets();
                }
                
                // Create summary object
                DailySummary summary = new DailySummary(
                    dateFormat.format(date),
                    totalBiceps,
                    totalPullups,
                    totalSets,
                    records.size(),
                    0 // Duration calculation can be added later
                );
                
                // Update UI on main thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> updateSummaryDisplay(summary));
                }
                
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> 
                        Toast.makeText(getContext(), "Error loading data: " + e.getMessage(), 
                                     Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }
    
    private void updateSummaryDisplay(DailySummary summary) {
        String summaryText = String.format(Locale.getDefault(),
            "📅 Date: %s\n\n" +
            "💪 Biceps Curls: %d reps\n" +
            "🔥 Pull-ups: %d reps\n" +
            "📊 Total Sets: %d\n" +
            "🏋️ Workout Sessions: %d\n\n" +
            "🎯 Total Reps: %d",
            summary.getDate(),
            summary.getTotalBicepsReps(),
            summary.getTotalPullupsReps(),
            summary.getTotalSets(),
            summary.getWorkoutCount(),
            summary.getTotalReps()
        );
        
        binding.textDailySummary.setText(summaryText);
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
        binding = null;
    }
}