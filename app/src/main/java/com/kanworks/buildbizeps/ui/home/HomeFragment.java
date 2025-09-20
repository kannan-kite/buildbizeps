package com.kanworks.buildbizeps.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.kanworks.buildbizeps.databinding.FragmentHomeBinding;
import com.kanworks.buildbizeps.data.database.FitnessDatabase;
import com.kanworks.buildbizeps.data.entity.Exercise;
import com.kanworks.buildbizeps.data.entity.ExerciseRecord;
import com.kanworks.buildbizeps.data.entity.WorkoutSession;

import java.util.Date;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private FitnessDatabase database;
    
    // Workout tracking variables
    private int bicepsCount = 0;
    private int pullupsCount = 0;
    private WorkoutSession currentSession;
    private boolean workoutInProgress = false;
    
    // Exercise IDs (will be set after inserting exercises)
    private int bicepsExerciseId = -1;
    private int pullupsExerciseId = -1;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        // Initialize database
        database = FitnessDatabase.getDatabase(getContext());
        
        // Initialize exercises in database if not exists
        initializeExercises();
        
        // Set up UI components
        setupCounterButtons();
        setupWorkoutButtons();
        
        return root;
    }
    
    private void initializeExercises() {
        // Check if exercises already exist
        if (database.exerciseDao().getAllExercises().isEmpty()) {
            // Insert default exercises
            Exercise bicepsExercise = new Exercise("Biceps Curls", "strength", "Arm curl exercise", "biceps");
            Exercise pullupsExercise = new Exercise("Pull-ups", "strength", "Upper body pulling exercise", "back");
            
            bicepsExerciseId = (int) database.exerciseDao().insertExercise(bicepsExercise);
            pullupsExerciseId = (int) database.exerciseDao().insertExercise(pullupsExercise);
        } else {
            // Get existing exercise IDs
            for (Exercise exercise : database.exerciseDao().getAllExercises()) {
                if ("Biceps Curls".equals(exercise.getName())) {
                    bicepsExerciseId = exercise.getId();
                } else if ("Pull-ups".equals(exercise.getName())) {
                    pullupsExerciseId = exercise.getId();
                }
            }
        }
    }
    
    private void setupCounterButtons() {
        // Biceps counter buttons
        binding.btnBicepsPlus.setOnClickListener(v -> {
            bicepsCount++;
            binding.textBicepsCount.setText(String.valueOf(bicepsCount));
        });
        
        binding.btnBicepsMinus.setOnClickListener(v -> {
            if (bicepsCount > 0) {
                bicepsCount--;
                binding.textBicepsCount.setText(String.valueOf(bicepsCount));
            }
        });
        
        // Pull-ups counter buttons
        binding.btnPullupsPlus.setOnClickListener(v -> {
            pullupsCount++;
            binding.textPullupsCount.setText(String.valueOf(pullupsCount));
        });
        
        binding.btnPullupsMinus.setOnClickListener(v -> {
            if (pullupsCount > 0) {
                pullupsCount--;
                binding.textPullupsCount.setText(String.valueOf(pullupsCount));
            }
        });
    }
    
    private void setupWorkoutButtons() {
        binding.btnStartWorkout.setOnClickListener(v -> startWorkout());
        binding.btnSaveWorkout.setOnClickListener(v -> saveWorkout());
        
        // Initially disable save button
        binding.btnSaveWorkout.setEnabled(false);
    }
    
    private void startWorkout() {
        if (!workoutInProgress) {
            // Start new workout session
            currentSession = new WorkoutSession(new Date());
            workoutInProgress = true;
            
            // Reset counters
            bicepsCount = 0;
            pullupsCount = 0;
            binding.textBicepsCount.setText("0");
            binding.textPullupsCount.setText("0");
            
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
            // End the session
            currentSession.setEndTime(new Date());
            long duration = (currentSession.getEndTime().getTime() - currentSession.getStartTime().getTime()) / (1000 * 60); // minutes
            currentSession.setDurationMinutes(duration);
            
            // Save workout session to database
            int sessionId = (int) database.workoutSessionDao().insertWorkoutSession(currentSession);
            
            // Save exercise records if any reps were performed
            if (bicepsCount > 0) {
                ExerciseRecord bicepsRecord = new ExerciseRecord(bicepsExerciseId, sessionId, 1, bicepsCount);
                database.exerciseRecordDao().insertExerciseRecord(bicepsRecord);
            }
            
            if (pullupsCount > 0) {
                ExerciseRecord pullupsRecord = new ExerciseRecord(pullupsExerciseId, sessionId, 1, pullupsCount);
                database.exerciseRecordDao().insertExerciseRecord(pullupsRecord);
            }
            
            // Reset workout state
            workoutInProgress = false;
            currentSession = null;
            
            // Update UI
            binding.btnStartWorkout.setText("Start Workout");
            binding.btnStartWorkout.setEnabled(true);
            binding.btnSaveWorkout.setEnabled(false);
            
            // Show success message
            String message = String.format("Workout saved! Biceps: %d reps, Pull-ups: %d reps", bicepsCount, pullupsCount);
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            Log.d("HomeFragment", message);
            
            // Reset counters
            bicepsCount = 0;
            pullupsCount = 0;
            binding.textBicepsCount.setText("0");
            binding.textPullupsCount.setText("0");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}