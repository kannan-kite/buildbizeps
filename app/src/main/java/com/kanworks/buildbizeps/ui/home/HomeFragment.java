package com.kanworks.buildbizeps.ui.home;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.kanworks.buildbizeps.R;
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
    private Map<Integer, MaterialButton> exerciseSaveButtons = new HashMap<>();
    private List<Exercise> favoriteExercises;
    
    // Store individual sets for each exercise (for calculating total sets)
    private Map<Integer, Integer> exerciseSetCounts = new HashMap<>();
    
    // Workout tracking variables
    private WorkoutSession currentSession;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        // Initialize database and executor
        database = FitnessDatabase.getDatabase(getContext());
        executor = Executors.newSingleThreadExecutor();
        
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
        Log.d("HomeFragment", "No favorite exercises found - showing empty state");
        
        // Show the empty state card
        View noFavoritesCard = binding.getRoot().findViewById(R.id.card_no_favorites);
        if (noFavoritesCard != null) {
            noFavoritesCard.setVisibility(View.VISIBLE);
        }
        
        // Hide the exercises container
        if (binding.exercisesContainer != null) {
            binding.exercisesContainer.setVisibility(View.GONE);
        }
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
        Log.d("HomeFragment", "Creating section for exercise: " + exercise.getName());
        
        // Create main card using MaterialCardView
        com.google.android.material.card.MaterialCardView cardView = new com.google.android.material.card.MaterialCardView(getContext());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.bottomMargin = (int) (16 * getResources().getDisplayMetrics().density);
        cardView.setLayoutParams(cardParams);
        cardView.setCardElevation(4 * getResources().getDisplayMetrics().density);
        cardView.setRadius(16 * getResources().getDisplayMetrics().density);
        cardView.setUseCompatPadding(true);
        
        // Main container
        LinearLayout mainLayout = new LinearLayout(getContext());
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(
            (int) (20 * getResources().getDisplayMetrics().density),
            (int) (20 * getResources().getDisplayMetrics().density),
            (int) (20 * getResources().getDisplayMetrics().density),
            (int) (20 * getResources().getDisplayMetrics().density)
        );
        
        // Exercise title with icon
        LinearLayout headerLayout = new LinearLayout(getContext());
        headerLayout.setOrientation(LinearLayout.HORIZONTAL);
        headerLayout.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        headerParams.bottomMargin = (int) (16 * getResources().getDisplayMetrics().density);
        headerLayout.setLayoutParams(headerParams);
        
        // Exercise name
        TextView exerciseTitle = new TextView(getContext());
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f
        );
        exerciseTitle.setLayoutParams(titleParams);
        exerciseTitle.setText(getExerciseEmoji(exercise) + " " + exercise.getName());
        exerciseTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        exerciseTitle.setTypeface(exerciseTitle.getTypeface(), Typeface.BOLD);
        
        // Sets completed indicator
        TextView setsCompletedView = new TextView(getContext());
        setsCompletedView.setText("Sets completed: 0");
        setsCompletedView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        setsCompletedView.setTag("sets_counter_" + exercise.getId());
        
        headerLayout.addView(exerciseTitle);
        headerLayout.addView(setsCompletedView);
        
        // Weight input section
        LinearLayout weightSection = createWeightInputSection(exercise.getId());
        
        // Reps counter section  
        LinearLayout repsSection = createRepsCounterSection(exercise.getId());
        
        // Save set button
        com.google.android.material.button.MaterialButton saveSetButton = new com.google.android.material.button.MaterialButton(getContext());
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            (int) (48 * getResources().getDisplayMetrics().density)
        );
        buttonParams.topMargin = (int) (16 * getResources().getDisplayMetrics().density);
        saveSetButton.setLayoutParams(buttonParams);
        saveSetButton.setText("💾 Save Set");
        saveSetButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        saveSetButton.setCornerRadius((int) (24 * getResources().getDisplayMetrics().density));
        saveSetButton.setEnabled(false);
        saveSetButton.setTag("save_set_button_" + exercise.getId());
        
        // Store button reference for enable/disable functionality
        exerciseSaveButtons.put(exercise.getId(), saveSetButton);
        
        saveSetButton.setOnClickListener(v -> saveSet(exercise.getId(), setsCompletedView));
        
        // Add all sections to main layout
        mainLayout.addView(headerLayout);
        mainLayout.addView(weightSection);
        mainLayout.addView(repsSection);
        mainLayout.addView(saveSetButton);
        
        cardView.addView(mainLayout);
        container.addView(cardView);
        
        // Initialize tracking data
        exerciseReps.put(exercise.getId(), 0);
        exerciseWeights.put(exercise.getId(), 0.0f);
        exerciseSetCounts.put(exercise.getId(), 0);
        
        Log.d("HomeFragment", "Exercise section created for: " + exercise.getName());
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
        
        // Add text change listener to enable save button when both weight and reps are set
        weightInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSaveButtonState(exerciseId);
            }
            
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        
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
        updateSaveButtonState(exerciseId);
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
            updateSaveButtonState(exerciseId);
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
                // Verify exercise exists first
                Exercise exercise = database.exerciseDao().getExerciseById(exerciseId);
                if (exercise == null) {
                    Log.e("HomeFragment", "ERROR: Exercise with ID " + exerciseId + " does not exist!");
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> 
                            Toast.makeText(getContext(), "Exercise not found. Please refresh and try again.", Toast.LENGTH_SHORT).show()
                        );
                    }
                    return;
                }
                
                // Check if current session exists and is valid
                if (currentSession != null) {
                    WorkoutSession dbSession = database.workoutSessionDao().getWorkoutSessionById(currentSession.getId());
                    if (dbSession == null) {
                        Log.w("HomeFragment", "Current session ID " + currentSession.getId() + " no longer exists (likely deleted). Creating new session.");
                        currentSession = null; // Reset to force creation of new session
                    }
                }
                
                // Create workout session if it doesn't exist or was invalidated
                if (currentSession == null) {
                    currentSession = new WorkoutSession(new Date());
                    long sessionId = database.workoutSessionDao().insertWorkoutSession(currentSession);
                    currentSession.setId((int) sessionId);
                    Log.d("HomeFragment", "Created new workout session with ID: " + sessionId);
                }
                
                int sessionIdToUse = currentSession.getId();
                Log.d("HomeFragment", "Using session ID: " + sessionIdToUse + " for exercise: " + exerciseId + 
                      " (Exercise: " + exercise.getName() + ")");
                
                // Create and save exercise record for this individual set
                ExerciseRecord record = new ExerciseRecord(
                    exerciseId, 
                    sessionIdToUse,  // Use session ID
                    1,  // Always 1 set per record
                    finalReps
                );
                record.setWeight(finalWeight);
                
                Log.d("HomeFragment", "Inserting exercise record - Exercise: " + exerciseId + 
                      " (" + exercise.getName() + "), Session: " + sessionIdToUse + 
                      ", Weight: " + finalWeight + ", Reps: " + finalReps);
                      
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
    

    

    

    

    
    private void updateSaveButtonState(int exerciseId) {
        // Get current weight and reps values
        EditText weightInput = exerciseWeightViews.get(exerciseId);
        TextView repsCounter = exerciseRepsViews.get(exerciseId);
        MaterialButton saveButton = exerciseSaveButtons.get(exerciseId);
        
        if (weightInput != null && repsCounter != null && saveButton != null) {
            String weightText = weightInput.getText().toString().trim();
            String repsText = repsCounter.getText().toString().trim();
            
            // Enable save button if both weight and reps are valid
            boolean hasWeight = !weightText.isEmpty() && !weightText.equals("0.0") && !weightText.equals("0");
            boolean hasReps = !repsText.equals("0");
            boolean shouldEnable = hasWeight && hasReps;
            
            saveButton.setEnabled(shouldEnable);
            
            Log.d("HomeFragment", "Exercise " + exerciseId + " - Weight: " + hasWeight + ", Reps: " + hasReps + ", Button enabled: " + shouldEnable);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Reload exercises in case favorites changed in settings
        loadFavoriteExercises();
        
        // Validate current session in case it was deleted while on another tab
        validateCurrentSession();
    }
    
    private void validateCurrentSession() {
        if (currentSession != null) {
            executor.execute(() -> {
                try {
                    WorkoutSession dbSession = database.workoutSessionDao().getWorkoutSessionById(currentSession.getId());
                    if (dbSession == null) {
                        Log.w("HomeFragment", "Session validation failed: Session ID " + currentSession.getId() + " no longer exists. Resetting session.");
                        currentSession = null;
                    }
                } catch (Exception e) {
                    Log.e("HomeFragment", "Error validating session", e);
                    currentSession = null; // Reset on any error
                }
            });
        }
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