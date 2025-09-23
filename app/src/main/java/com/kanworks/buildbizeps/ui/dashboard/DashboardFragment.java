package com.kanworks.buildbizeps.ui.dashboard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.kanworks.buildbizeps.data.entity.WorkoutSession;
import com.kanworks.buildbizeps.data.model.DailySummary;
import com.kanworks.buildbizeps.data.model.ExerciseHistoryDetail;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
        setupHistoryButtons();
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
                
                // Calculate summary data properly
                int totalReps = 0;  // This will be sets * reps for all exercises
                int totalSets = 0;
                Set<Integer> uniqueExercises = new HashSet<>();
                
                // Calculate total reps as sets * reps for each record
                for (ExerciseRecord record : records) {
                    totalReps += (record.getSets() * record.getReps());  // Correct formula: sets * reps
                    totalSets += record.getSets();
                    uniqueExercises.add(record.getExerciseId());
                }
                
                // Get workout sessions count for the day
                List<WorkoutSession> sessions = database.workoutSessionDao().getWorkoutSessionsByDate(date);
                
                // Create summary object with correct data
                DailySummary summary = new DailySummary(
                    dateFormat.format(date),
                    totalReps,           // Total reps (sets * reps)
                    totalSets,           // Total sets
                    uniqueExercises.size(), // Number of different exercises
                    sessions.size(),     // Number of workout sessions
                    0 // Duration calculation can be added later
                );
                
                // Load detailed exercise history
                List<ExerciseHistoryDetail> exerciseHistory = loadDetailedExerciseHistory(records);
                
                // Update UI on main thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        updateCombinedSummaryDisplay(summary, exerciseHistory);
                    });
                }
                
            } catch (Exception e) {
                Log.e("DashboardFragment", "Error loading detailed exercise history", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> 
                        Toast.makeText(getContext(), "Error loading data: " + e.getMessage(), 
                                     Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }
    
    /**
     * Update the combined summary display with both stats and exercise details
     */
    private void updateCombinedSummaryDisplay(DailySummary summary, List<ExerciseHistoryDetail> exerciseHistory) {
        StringBuilder combinedText = new StringBuilder();
        
        // Add summary stats
        // combinedText.append(String.format(Locale.getDefault(),
        //     "📅 %s\n\n" +
        //     "🎯 Total Reps: %d\n" +
        //     "🏋️ Total Sets: %d\n" +
        //     "💪 Exercises: %d\n" +
        //     "� Sessions: %d\n",
        //     summary.getDate(),
        //     summary.getTotalReps(),
        //     summary.getTotalSets(),
        //     summary.getTotalExercises(),
        //     summary.getWorkoutCount()
        // ));
        
        // Add exercise details if available
        if (!exerciseHistory.isEmpty()) {
            // combinedText.append("\n");
            // combinedText.append("═══════════════════\n");
            // combinedText.append("EXERCISE SUMMARY\n");
            // combinedText.append("═══════════════════\n\n");
            
            for (int i = 0; i < exerciseHistory.size(); i++) {
                ExerciseHistoryDetail detail = exerciseHistory.get(i);
                combinedText.append(detail.getFormattedHistory());
                
                if (i < exerciseHistory.size() - 1) {
                    combinedText.append("\n");
                }
            }
        } else {
            combinedText.append("\n\nNo exercise details recorded.");
        }
        
        binding.textCombinedSummary.setText(combinedText.toString());
    }
    
    /**
     * Load detailed exercise history from records
     */
    private List<ExerciseHistoryDetail> loadDetailedExerciseHistory(List<ExerciseRecord> records) {
        Map<Integer, List<ExerciseRecord>> exerciseGroups = new HashMap<>();
        
        // Group records by exercise ID
        for (ExerciseRecord record : records) {
            exerciseGroups.computeIfAbsent(record.getExerciseId(), k -> new ArrayList<>()).add(record);
        }
        
        List<ExerciseHistoryDetail> historyDetails = new ArrayList<>();
        
        // Process each exercise group
        for (Map.Entry<Integer, List<ExerciseRecord>> entry : exerciseGroups.entrySet()) {
            int exerciseId = entry.getKey();
            List<ExerciseRecord> exerciseRecords = entry.getValue();
            
            // Get exercise name
            Exercise exercise = database.exerciseDao().getExerciseById(exerciseId);
            String exerciseName = exercise != null ? exercise.getName() : "Unknown Exercise";
            
            // Calculate total sets and create set details
            int totalSets = 0;
            List<ExerciseHistoryDetail.SetDetail> setDetails = new ArrayList<>();
            
            for (ExerciseRecord record : exerciseRecords) {
                // For each record, add individual sets based on the sets count
                for (int i = 0; i < record.getSets(); i++) {
                    setDetails.add(new ExerciseHistoryDetail.SetDetail(record.getReps(), record.getWeight()));
                    totalSets++;
                }
            }
            
            historyDetails.add(new ExerciseHistoryDetail(exerciseName, totalSets, setDetails));
        }
        
        return historyDetails;
    }
    
    private void setupHistoryButtons() {
        // Clear Selected Day button
        binding.btnClearDay.setOnClickListener(v -> {
            Log.d("DashboardFragment", "=== CLEAR DAY BUTTON PRESSED ===");
            Log.d("DashboardFragment", "Selected date: " + (selectedDate != null ? dateFormat.format(selectedDate) : "null"));
            
            if (selectedDate == null) {
                Log.d("DashboardFragment", "No date selected - showing error message");
                Toast.makeText(getContext(), "Please select a date first", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String dateStr = dateFormat.format(selectedDate);
            Log.d("DashboardFragment", "Showing confirmation dialog for date: " + dateStr);
            
            new AlertDialog.Builder(getContext())
                .setTitle("Clear Day History")
                .setMessage("Are you sure you want to clear all workout data for " + dateStr + "?")
                .setPositiveButton("Clear", (dialog, which) -> {
                    Log.d("DashboardFragment", "User confirmed deletion for date: " + dateStr);
                    clearDayHistory(selectedDate);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Log.d("DashboardFragment", "User cancelled deletion for date: " + dateStr);
                })
                .show();
        });
        
        // Clear All History button
        binding.btnClearAll.setOnClickListener(v -> {
            Log.d("DashboardFragment", "=== CLEAR ALL BUTTON PRESSED ===");
            
            new AlertDialog.Builder(getContext())
                .setTitle("Clear All History")
                .setMessage("⚠️ WARNING: This will permanently delete ALL workout history!\\n\\nThis cannot be undone. Are you sure?")
                .setPositiveButton("Clear All", (dialog, which) -> {
                    Log.d("DashboardFragment", "User confirmed clearing all data");
                    clearAllHistory();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Log.d("DashboardFragment", "User cancelled clearing all data");
                })
                .show();
        });
    }
    
    private void clearDayHistory(Date date) {
        Log.d("DashboardFragment", "=== STARTING CLEAR DAY HISTORY ===");
        Log.d("DashboardFragment", "Date to clear: " + dateFormat.format(date));
        
        executor.execute(() -> {
            try {
                Log.d("DashboardFragment", "Starting database deletion operations...");
                
                // Delete records and sessions for the selected date
                Log.d("DashboardFragment", "Deleting exercise records for date...");
                database.exerciseRecordDao().deleteRecordsByDate(date);
                Log.d("DashboardFragment", "Exercise records deleted");
                
                Log.d("DashboardFragment", "Deleting workout sessions for date...");
                database.workoutSessionDao().deleteSessionsByDate(date);
                Log.d("DashboardFragment", "Workout sessions deleted");
                
                // Update UI on main thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.d("DashboardFragment", "Updating UI after successful day deletion");
                        Toast.makeText(getContext(), "Day history cleared successfully", Toast.LENGTH_SHORT).show();
                        // Refresh the current day's summary
                        Log.d("DashboardFragment", "Refreshing daily summary after deletion");
                        loadDailySummary(selectedDate);
                    });
                }
                
            } catch (Exception e) {
                Log.e("DashboardFragment", "Error clearing day history", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> 
                        Toast.makeText(getContext(), "Error clearing day history: " + e.getMessage(), 
                                     Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }
    
    private void clearAllHistory() {
        Log.d("DashboardFragment", "=== STARTING CLEAR ALL HISTORY ===");
        
        executor.execute(() -> {
            try {
                Log.d("DashboardFragment", "Starting complete database wipe...");
                
                // Delete all records and sessions
                Log.d("DashboardFragment", "Deleting all exercise records...");
                database.exerciseRecordDao().deleteAllRecords();
                Log.d("DashboardFragment", "All exercise records deleted");
                
                Log.d("DashboardFragment", "Deleting all workout sessions...");
                database.workoutSessionDao().deleteAllSessions();
                Log.d("DashboardFragment", "All workout sessions deleted");
                
                // Update UI on main thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.d("DashboardFragment", "Updating UI after successful complete deletion");
                        Toast.makeText(getContext(), "All history cleared successfully", Toast.LENGTH_SHORT).show();
                        // Refresh the current day's summary
                        Log.d("DashboardFragment", "Refreshing daily summary after complete deletion");
                        loadDailySummary(selectedDate);
                    });
                }
                
            } catch (Exception e) {
                Log.e("DashboardFragment", "Error clearing all history", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> 
                        Toast.makeText(getContext(), "Error clearing all history: " + e.getMessage(), 
                                     Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
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