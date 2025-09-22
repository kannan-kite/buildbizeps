package com.kanworks.buildbizeps.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.kanworks.buildbizeps.databinding.FragmentHomeBinding;
import com.kanworks.buildbizeps.data.database.FitnessDatabase;
import com.kanworks.buildbizeps.data.entity.Exercise;
import com.kanworks.buildbizeps.data.entity.ExerciseRecord;
import com.kanworks.buildbizeps.data.entity.WorkoutSession;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private FitnessDatabase database;
    private ExecutorService executor;
    
    // Dynamic exercise tracking
    private Map<Integer, Integer> exerciseCounts = new HashMap<>();
    private Map<Integer, TextView> exerciseCountViews = new HashMap<>();
    private List<Exercise> favoriteExercises;
    
    // Workout tracking variables
    private WorkoutSession currentSession;
    private boolean workoutInProgress = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        // Initialize database and executor
        database = FitnessDatabase.getDatabase(getContext());
        executor = Executors.newSingleThreadExecutor();
        
        setupWorkoutButtons();
        loadFavoriteExercises();
        
        return root;
    }
    
    private void loadFavoriteExercises() {
        executor.execute(() -> {
            try {
                favoriteExercises = database.exerciseDao().getFavoriteExercises();
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (favoriteExercises.isEmpty()) {
                            showNoFavoritesMessage();
                        } else {
                            createExerciseSections();
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("HomeFragment", "Error loading favorite exercises", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> 
                        Toast.makeText(getContext(), "Error loading exercises", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }
    
    private void showNoFavoritesMessage() {
        LinearLayout container = binding.exercisesContainer;
        container.removeAllViews();
        
        TextView message = new TextView(getContext());
        message.setText("📝 No favorite exercises selected!\n\nGo to Settings to add exercises and mark them as favorites.");
        message.setTextSize(16);
        message.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        message.setPadding(32, 64, 32, 64);
        
        container.addView(message);
    }
    
    private void createExerciseSections() {
        LinearLayout container = binding.exercisesContainer;
        container.removeAllViews();
        exerciseCounts.clear();
        exerciseCountViews.clear();
        
        for (Exercise exercise : favoriteExercises) {
            exerciseCounts.put(exercise.getId(), 0);
            createExerciseSection(exercise, container);
        }
    }
    
    private void createExerciseSection(Exercise exercise, LinearLayout container) {
        // Create main section layout
        LinearLayout section = new LinearLayout(getContext());
        section.setOrientation(LinearLayout.VERTICAL);
        section.setPadding(16, 16, 16, 16);
        section.setBackground(getResources().getDrawable(android.R.drawable.dialog_holo_light_frame));
        
        LinearLayout.LayoutParams sectionParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        sectionParams.setMargins(0, 0, 0, 24);
        section.setLayoutParams(sectionParams);
        
        // Exercise title
        TextView titleView = new TextView(getContext());
        titleView.setText(getExerciseEmoji(exercise) + " " + exercise.getName());
        titleView.setTextSize(20);
        titleView.setTextColor(getResources().getColor(android.R.color.black));
        titleView.setGravity(android.view.Gravity.CENTER);
        section.addView(titleView);
        
        // Counter layout
        LinearLayout counterLayout = new LinearLayout(getContext());
        counterLayout.setOrientation(LinearLayout.HORIZONTAL);
        counterLayout.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams counterParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        counterParams.setMargins(0, 16, 0, 0);
        counterLayout.setLayoutParams(counterParams);
        
        // Minus button
        Button minusBtn = new Button(getContext());
        minusBtn.setText("-");
        minusBtn.setTextSize(24);
        minusBtn.setWidth(180);
        minusBtn.setHeight(180);
        minusBtn.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        minusBtn.setOnClickListener(v -> decrementExercise(exercise.getId()));
        
        // Count display
        TextView countView = new TextView(getContext());
        countView.setText("0");
        countView.setTextSize(36);
        countView.setTextColor(getResources().getColor(android.R.color.black));
        countView.setGravity(android.view.Gravity.CENTER);
        countView.setWidth(300);
        exerciseCountViews.put(exercise.getId(), countView);
        
        // Plus button
        Button plusBtn = new Button(getContext());
        plusBtn.setText("+");
        plusBtn.setTextSize(24);
        plusBtn.setWidth(180);
        plusBtn.setHeight(180);
        plusBtn.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        plusBtn.setOnClickListener(v -> incrementExercise(exercise.getId()));
        
        counterLayout.addView(minusBtn);
        counterLayout.addView(countView);
        counterLayout.addView(plusBtn);
        
        section.addView(counterLayout);
        container.addView(section);
    }
    
    private String getExerciseEmoji(Exercise exercise) {
        String name = exercise.getName().toLowerCase();
        if (name.contains("bicep")) return "💪";
        if (name.contains("pullup") || name.contains("pull-up")) return "🏋️";
        if (name.contains("pushup") || name.contains("push-up")) return "🔥";
        if (name.contains("squat")) return "🦵";
        if (name.contains("plank")) return "🏃";
        return "🏋️"; // Default
    }
    
    private void incrementExercise(int exerciseId) {
        int currentCount = exerciseCounts.get(exerciseId);
        exerciseCounts.put(exerciseId, currentCount + 1);
        updateCountDisplay(exerciseId);
    }
    
    private void decrementExercise(int exerciseId) {
        int currentCount = exerciseCounts.get(exerciseId);
        if (currentCount > 0) {
            exerciseCounts.put(exerciseId, currentCount - 1);
            updateCountDisplay(exerciseId);
        }
    }
    
    private void updateCountDisplay(int exerciseId) {
        TextView countView = exerciseCountViews.get(exerciseId);
        if (countView != null) {
            countView.setText(String.valueOf(exerciseCounts.get(exerciseId)));
        }
    }
    
    private void setupWorkoutButtons() {
        binding.btnStartWorkout.setOnClickListener(v -> startWorkout());
        binding.btnSaveWorkout.setOnClickListener(v -> saveWorkout());
        
        // Initially disable save button
        binding.btnSaveWorkout.setEnabled(false);
    }
    
    private void startWorkout() {
        if (!workoutInProgress) {
            currentSession = new WorkoutSession(new Date());
            workoutInProgress = true;
            
            // Reset all counters
            for (Integer exerciseId : exerciseCounts.keySet()) {
                exerciseCounts.put(exerciseId, 0);
                updateCountDisplay(exerciseId);
            }
            
            // Update UI
            binding.btnStartWorkout.setText("Workout In Progress...");
            binding.btnStartWorkout.setEnabled(false);
            binding.btnSaveWorkout.setEnabled(true);
            
            Toast.makeText(getContext(), "Workout started! Start tracking your reps.", Toast.LENGTH_SHORT).show();
            Log.d("HomeFragment", "Workout session started");
        }
    }
    
    private void saveWorkout() {
        if (workoutInProgress && currentSession != null) {
            currentSession.setEndTime(new Date());
            
            executor.execute(() -> {
                try {
                    // Save workout session
                    long sessionId = database.workoutSessionDao().insertWorkoutSession(currentSession);
                    
                    // Save exercise records
                    for (Map.Entry<Integer, Integer> entry : exerciseCounts.entrySet()) {
                        int exerciseId = entry.getKey();
                        int reps = entry.getValue();
                        
                        if (reps > 0) { // Only save if there are reps
                            ExerciseRecord record = new ExerciseRecord(
                                exerciseId, 
                                (int) sessionId, 
                                1, // sets
                                reps
                            );
                            database.exerciseRecordDao().insertExerciseRecord(record);
                        }
                    }
                    
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            workoutInProgress = false;
                            binding.btnStartWorkout.setText("Start Workout");
                            binding.btnStartWorkout.setEnabled(true);
                            binding.btnSaveWorkout.setEnabled(false);
                            
                            Toast.makeText(getContext(), "Workout saved successfully!", Toast.LENGTH_SHORT).show();
                            Log.d("HomeFragment", "Workout session saved");
                        });
                    }
                    
                } catch (Exception e) {
                    Log.e("HomeFragment", "Error saving workout", e);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> 
                            Toast.makeText(getContext(), "Error saving workout", Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            });
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Reload exercises in case favorites changed in settings
        loadFavoriteExercises();
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