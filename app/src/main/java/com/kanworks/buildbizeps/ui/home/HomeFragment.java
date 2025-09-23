package com.kanworks.buildbizeps.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
    
    // Dynamic exercise tracking - weight and reps per set
    private Map<Integer, Float> exerciseWeights = new HashMap<>();
    private Map<Integer, Integer> exerciseReps = new HashMap<>();
    private Map<Integer, EditText> exerciseWeightViews = new HashMap<>();
    private Map<Integer, TextView> exerciseRepsViews = new HashMap<>();
    private List<Exercise> favoriteExercises;
    
    // Store individual sets for each exercise (for calculating total sets)
    private Map<Integer, Integer> exerciseSetCounts = new HashMap<>();
    
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
        exerciseWeights.clear();
        exerciseReps.clear();
        exerciseWeightViews.clear();
        exerciseRepsViews.clear();
        exerciseSetCounts.clear();
        
        for (Exercise exercise : favoriteExercises) {
            exerciseWeights.put(exercise.getId(), 0.0f);
            exerciseReps.put(exercise.getId(), 0);
            exerciseSetCounts.put(exercise.getId(), 0);
            createExerciseSection(exercise, container);
        }
    }
    
    private void createExerciseSection(Exercise exercise, LinearLayout container) {
        // Create main section layout
        LinearLayout section = new LinearLayout(getContext());
        section.setOrientation(LinearLayout.VERTICAL);
        section.setPadding(20, 20, 20, 20);
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
        titleView.setTextSize(22);
        titleView.setTextColor(getResources().getColor(android.R.color.black));
        titleView.setGravity(android.view.Gravity.CENTER);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        section.addView(titleView);
        
        // Sets completed indicator
        TextView setsCompletedView = new TextView(getContext());
        setsCompletedView.setText("Sets completed: 0");
        setsCompletedView.setTextSize(14);
        setsCompletedView.setTextColor(getResources().getColor(android.R.color.darker_gray));
        setsCompletedView.setGravity(android.view.Gravity.CENTER);
        setsCompletedView.setPadding(0, 8, 0, 16);
        section.addView(setsCompletedView);
        
        // Weight input section
        LinearLayout weightSection = createWeightInputSection(exercise.getId());
        section.addView(weightSection);
        
        // Reps section  
        LinearLayout repsSection = createRepsCounterSection(exercise.getId());
        section.addView(repsSection);
        
        // Save Set button
        Button saveSetBtn = new Button(getContext());
        saveSetBtn.setText("💾 Save Set");
        saveSetBtn.setTextSize(16);
        saveSetBtn.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_bright));
        saveSetBtn.setTextColor(getResources().getColor(android.R.color.white));
        saveSetBtn.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        btnParams.setMargins(0, 16, 0, 0);
        saveSetBtn.setLayoutParams(btnParams);
        saveSetBtn.setOnClickListener(v -> saveSet(exercise.getId(), setsCompletedView));
        section.addView(saveSetBtn);
        
        container.addView(section);
    }
    
    private LinearLayout createWeightInputSection(int exerciseId) {
        // Main container for weight input
        LinearLayout weightSection = new LinearLayout(getContext());
        weightSection.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams sectionParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        sectionParams.setMargins(0, 16, 0, 8);
        weightSection.setLayoutParams(sectionParams);
        
        // Label
        TextView labelView = new TextView(getContext());
        labelView.setText("Weight (kg)");
        labelView.setTextSize(16);
        labelView.setTextColor(getResources().getColor(android.R.color.darker_gray));
        labelView.setGravity(android.view.Gravity.CENTER);
        labelView.setTypeface(null, android.graphics.Typeface.BOLD);
        weightSection.addView(labelView);
        
        // Weight input field
        EditText weightInput = new EditText(getContext());
        weightInput.setHint("0.0");
        weightInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        weightInput.setTextSize(18);
        weightInput.setGravity(android.view.Gravity.CENTER);
        weightInput.setBackgroundColor(getResources().getColor(android.R.color.white));
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        inputParams.setMargins(20, 8, 20, 0);
        weightInput.setLayoutParams(inputParams);
        weightInput.setPadding(16, 16, 16, 16);
        
        exerciseWeightViews.put(exerciseId, weightInput);
        weightSection.addView(weightInput);
        
        return weightSection;
    }
    
    private LinearLayout createRepsCounterSection(int exerciseId) {
        // Main container for reps counter
        LinearLayout repsSection = new LinearLayout(getContext());
        repsSection.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams sectionParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        sectionParams.setMargins(0, 16, 0, 8);
        repsSection.setLayoutParams(sectionParams);
        
        // Label
        TextView labelView = new TextView(getContext());
        labelView.setText("Reps");
        labelView.setTextSize(16);
        labelView.setTextColor(getResources().getColor(android.R.color.darker_gray));
        labelView.setGravity(android.view.Gravity.CENTER);
        labelView.setTypeface(null, android.graphics.Typeface.BOLD);
        repsSection.addView(labelView);
        
        // Counter row with buttons
        LinearLayout counterRow = new LinearLayout(getContext());
        counterRow.setOrientation(LinearLayout.HORIZONTAL);
        counterRow.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, 8, 0, 0);
        counterRow.setLayoutParams(rowParams);
        
        // Minus button
        Button minusBtn = new Button(getContext());
        minusBtn.setText("-");
        minusBtn.setTextSize(20);
        minusBtn.setWidth(140);
        minusBtn.setHeight(140);
        minusBtn.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        minusBtn.setTextColor(getResources().getColor(android.R.color.white));
        minusBtn.setTypeface(null, android.graphics.Typeface.BOLD);
        minusBtn.setOnClickListener(v -> decrementReps(exerciseId));
        
        // Count display
        TextView countView = new TextView(getContext());
        countView.setText("0");
        countView.setTextSize(28);
        countView.setTextColor(getResources().getColor(android.R.color.black));
        countView.setGravity(android.view.Gravity.CENTER);
        countView.setWidth(200);
        countView.setTypeface(null, android.graphics.Typeface.BOLD);
        exerciseRepsViews.put(exerciseId, countView);
        
        // Plus button
        Button plusBtn = new Button(getContext());
        plusBtn.setText("+");
        plusBtn.setTextSize(20);
        plusBtn.setWidth(140);
        plusBtn.setHeight(140);
        plusBtn.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        plusBtn.setTextColor(getResources().getColor(android.R.color.white));
        plusBtn.setTypeface(null, android.graphics.Typeface.BOLD);
        plusBtn.setOnClickListener(v -> incrementReps(exerciseId));
        
        counterRow.addView(minusBtn);
        counterRow.addView(countView);
        counterRow.addView(plusBtn);
        
        repsSection.addView(counterRow);
        return repsSection;
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
    
    // Reps increment/decrement methods
    private void incrementReps(int exerciseId) {
        Log.d("HomeFragment", "=== INCREMENT REPS BUTTON PRESSED ===");
        Log.d("HomeFragment", "Exercise ID: " + exerciseId);
        
        int currentReps = exerciseReps.get(exerciseId);
        Log.d("HomeFragment", "Current reps before increment: " + currentReps);
        
        exerciseReps.put(exerciseId, currentReps + 1);
        Log.d("HomeFragment", "New reps after increment: " + (currentReps + 1));
        
        updateRepsDisplay(exerciseId);
        Log.d("HomeFragment", "Reps display updated for exercise " + exerciseId);
    }
    
    private void decrementReps(int exerciseId) {
        Log.d("HomeFragment", "=== DECREMENT REPS BUTTON PRESSED ===");
        Log.d("HomeFragment", "Exercise ID: " + exerciseId);
        
        int currentReps = exerciseReps.get(exerciseId);
        Log.d("HomeFragment", "Current reps before decrement: " + currentReps);
        
        if (currentReps > 0) {
            exerciseReps.put(exerciseId, currentReps - 1);
            Log.d("HomeFragment", "Decremented reps to: " + (currentReps - 1));
            updateRepsDisplay(exerciseId);
            Log.d("HomeFragment", "Reps display updated for exercise " + exerciseId);
        } else {
            Log.d("HomeFragment", "Cannot decrement reps - count is already 0");
        }
    }
    
    private void updateRepsDisplay(int exerciseId) {
        TextView repsView = exerciseRepsViews.get(exerciseId);
        if (repsView != null) {
            repsView.setText(String.valueOf(exerciseReps.get(exerciseId)));
        }
    }
    
    /**
     * Save a single set for an exercise (weight + reps)
     */
    private void saveSet(int exerciseId, TextView setsCompletedView) {
        Log.d("HomeFragment", "=== SAVE SET BUTTON PRESSED ===");
        Log.d("HomeFragment", "Exercise ID: " + exerciseId);
        
        if (!workoutInProgress) {
            Toast.makeText(getContext(), "Please start a workout first!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Get weight from input
        EditText weightInput = exerciseWeightViews.get(exerciseId);
        String weightText = weightInput.getText().toString().trim();
        float weight = 0.0f;
        if (!weightText.isEmpty()) {
            try {
                weight = Float.parseFloat(weightText);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Please enter a valid weight", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        
        // Get reps
        int reps = exerciseReps.get(exerciseId);
        if (reps <= 0) {
            Toast.makeText(getContext(), "Please set the number of reps", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Save the set to database immediately
        final float finalWeight = weight;
        final int finalReps = reps;
        
        executor.execute(() -> {
            try {
                if (currentSession != null) {
                    // Create and save exercise record for this individual set
                    ExerciseRecord record = new ExerciseRecord(
                        exerciseId, 
                        currentSession.getId(),  // Use session ID
                        1,  // Always 1 set per record
                        finalReps
                    );
                    record.setWeight(finalWeight);
                    database.exerciseRecordDao().insertExerciseRecord(record);
                    
                    // Update set count
                    int currentSetCount = exerciseSetCounts.get(exerciseId);
                    exerciseSetCounts.put(exerciseId, currentSetCount + 1);
                    
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            // Reset reps counter for next set
                            exerciseReps.put(exerciseId, 0);
                            updateRepsDisplay(exerciseId);
                            
                            // Update sets completed display
                            int newSetCount = exerciseSetCounts.get(exerciseId);
                            setsCompletedView.setText("Sets completed: " + newSetCount);
                            
                            String message = String.format("Set saved: %.1fkg × %d reps", finalWeight, finalReps);
                            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                            
                            Log.d("HomeFragment", "Set saved successfully - Exercise: " + exerciseId + 
                                  ", Weight: " + finalWeight + "kg, Reps: " + finalReps);
                        });
                    }
                }
            } catch (Exception e) {
                Log.e("HomeFragment", "Error saving set", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> 
                        Toast.makeText(getContext(), "Error saving set", Toast.LENGTH_SHORT).show()
                    );
                }
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
        Log.d("HomeFragment", "=== START WORKOUT BUTTON PRESSED ===");
        Log.d("HomeFragment", "Workout in progress status: " + workoutInProgress);
        
        if (!workoutInProgress) {
            Log.d("HomeFragment", "Starting new workout session...");
            
            // Save workout session to database first to get ID
            executor.execute(() -> {
                try {
                    currentSession = new WorkoutSession(new Date());
                    long sessionId = database.workoutSessionDao().insertWorkoutSession(currentSession);
                    currentSession.setId((int) sessionId);
                    
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            workoutInProgress = true;
                            Log.d("HomeFragment", "Workout session created with ID: " + sessionId);
                            
                            // Reset all exercise data and UI
                            resetWorkoutUI();
                            
                            // Update UI
                            Log.d("HomeFragment", "Updating UI buttons...");
                            binding.btnStartWorkout.setText("Workout In Progress...");
                            binding.btnStartWorkout.setEnabled(false);
                            binding.btnSaveWorkout.setEnabled(true);
                            Log.d("HomeFragment", "UI updated - Start button disabled, Save button enabled");
                            
                            Toast.makeText(getContext(), "Workout started! Track your sets by entering weight and reps, then save each set.", Toast.LENGTH_LONG).show();
                            Log.d("HomeFragment", "Workout session started successfully");
                        });
                    }
                } catch (Exception e) {
                    Log.e("HomeFragment", "Error starting workout session", e);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> 
                            Toast.makeText(getContext(), "Error starting workout", Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            });
        } else {
            Log.d("HomeFragment", "Workout already in progress - ignoring button press");
        }
    }
    
    private void saveWorkout() {
        Log.d("HomeFragment", "=== SAVE WORKOUT BUTTON PRESSED ===");
        Log.d("HomeFragment", "Workout in progress: " + workoutInProgress);
        Log.d("HomeFragment", "Current session exists: " + (currentSession != null));
        
        if (workoutInProgress && currentSession != null) {
            Log.d("HomeFragment", "Finishing workout session...");
            
            executor.execute(() -> {
                try {
                    // Update session end time
                    currentSession.setEndTime(new Date());
                    database.workoutSessionDao().updateWorkoutSession(currentSession);
                    
                    // Log summary of saved sets
                    Log.d("HomeFragment", "Workout summary:");
                    int totalSets = 0;
                    for (Integer exerciseId : exerciseSetCounts.keySet()) {
                        int sets = exerciseSetCounts.get(exerciseId);
                        totalSets += sets;
                        Log.d("HomeFragment", "  Exercise " + exerciseId + ": " + sets + " sets saved");
                    }
                    Log.d("HomeFragment", "Total sets saved: " + totalSets);
                    
                    final int finalTotalSets = totalSets;
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Log.d("HomeFragment", "Updating UI after successful workout completion...");
                            workoutInProgress = false;
                            binding.btnStartWorkout.setText("Start Workout");
                            binding.btnStartWorkout.setEnabled(true);
                            binding.btnSaveWorkout.setEnabled(false);
                            Log.d("HomeFragment", "UI reset - Start button enabled, Save button disabled");
                            
                            String message = String.format("Workout completed! %d total sets saved.", finalTotalSets);
                            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                            Log.d("HomeFragment", "Workout session completed successfully");
                        });
                    }
                    
                } catch (Exception e) {
                    Log.e("HomeFragment", "Error completing workout", e);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> 
                            Toast.makeText(getContext(), "Error completing workout", Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            });
        } else {
            Log.d("HomeFragment", "Cannot save - workout not in progress or session is null");
            Toast.makeText(getContext(), "No active workout to save", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void resetWorkoutUI() {
        Log.d("HomeFragment", "Resetting workout UI...");
        
        // Clear all exercise data
        exerciseReps.clear();
        exerciseWeights.clear();
        exerciseSetCounts.clear();
        
        // Reset all UI elements for each exercise
        if (favoriteExercises != null) {
            for (Exercise exercise : favoriteExercises) {
                int exerciseId = exercise.getId();
                
                // Reset weight input
                EditText weightInput = exerciseWeightViews.get(exerciseId);
                if (weightInput != null) {
                    weightInput.setText("");
                }
                
                // Reset reps counter
                TextView repsCounter = exerciseRepsViews.get(exerciseId);
                if (repsCounter != null) {
                    repsCounter.setText("0");
                }
                
                // Initialize values
                exerciseReps.put(exerciseId, 0);
                exerciseWeights.put(exerciseId, 0.0f);
                exerciseSetCounts.put(exerciseId, 0);
            }
        }
        
        Log.d("HomeFragment", "Workout UI reset completed");
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