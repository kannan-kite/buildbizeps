package com.kanworks.buildbizeps.ui.notifications;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.kanworks.buildbizeps.databinding.FragmentNotificationsBinding;
import com.kanworks.buildbizeps.data.database.FitnessDatabase;
import com.kanworks.buildbizeps.data.entity.Exercise;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private FitnessDatabase database;
    private ExecutorService executor;
    private LinearLayout exercisesContainer;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        // Initialize database and executor
        database = FitnessDatabase.getDatabase(getContext());
        executor = Executors.newSingleThreadExecutor();
        
        setupUI();
        loadExercises();
        
        return root;
    }
    
    private void setupUI() {
        exercisesContainer = binding.exercisesContainer;
        
        // Set up add exercise button
        binding.btnAddExercise.setOnClickListener(v -> showAddExerciseDialog());
        
        // Add default exercises if database is empty
        initializeDefaultExercises();
    }
    
    private void initializeDefaultExercises() {
        executor.execute(() -> {
            List<Exercise> exercises = database.exerciseDao().getAllExercises();
            if (exercises.isEmpty()) {
                // Add default exercises
                Exercise biceps = new Exercise("Biceps Curls", "strength", "Arm exercise using dumbbells or barbells", "arms", false);
                Exercise pullups = new Exercise("Pull-ups", "strength", "Upper body exercise using body weight", "back", false);
                Exercise pushups = new Exercise("Push-ups", "strength", "Chest and arm exercise using body weight", "chest", true);
                
                // Mark first two as favorites by default
                biceps.setFavorite(true);
                pullups.setFavorite(true);
                
                database.exerciseDao().insertExercise(biceps);
                database.exerciseDao().insertExercise(pullups);
                database.exerciseDao().insertExercise(pushups);
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> loadExercises());
                }
            }
        });
    }
    
    private void loadExercises() {
        executor.execute(() -> {
            List<Exercise> exercises = database.exerciseDao().getAllExercises();
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    exercisesContainer.removeAllViews();
                    for (Exercise exercise : exercises) {
                        addExerciseToUI(exercise);
                    }
                });
            }
        });
    }
    
    private void addExerciseToUI(Exercise exercise) {
        // Create horizontal layout for exercise info and switch
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(16, 16, 16, 16);
        
        LinearLayout textLayout = new LinearLayout(getContext());
        textLayout.setOrientation(LinearLayout.VERTICAL);
        textLayout.setLayoutParams(new LinearLayout.LayoutParams(0, 
            LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        
        // Create title and subtitle views directly
        TextView titleView = new TextView(getContext());
        TextView subtitleView = new TextView(getContext());
        
        titleView.setText(exercise.getName() + (exercise.isCustom() ? " (Custom)" : ""));
        titleView.setTextSize(16);
        titleView.setTextColor(getResources().getColor(android.R.color.black));
        titleView.setPadding(8, 8, 8, 4);
        
        subtitleView.setText(exercise.getDescription());
        subtitleView.setTextSize(14);
        subtitleView.setTextColor(getResources().getColor(android.R.color.darker_gray));
        subtitleView.setPadding(8, 0, 8, 8);
        
        // Add favorite toggle
        Switch favoriteSwitch = new Switch(getContext());
        favoriteSwitch.setChecked(exercise.isFavorite());
        favoriteSwitch.setText("Favorite");
        favoriteSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            executor.execute(() -> {
                database.exerciseDao().updateFavoriteStatus(exercise.getId(), isChecked);
            });
        });
        
        textLayout.addView(titleView);
        textLayout.addView(subtitleView);
        
        layout.addView(textLayout);
        layout.addView(favoriteSwitch);
        
        // Add long-press listener for delete functionality
        layout.setOnLongClickListener(v -> {
            showDeleteExerciseDialog(exercise);
            return true;
        });
        
        exercisesContainer.addView(layout);
    }
    
    private void showDeleteExerciseDialog(Exercise exercise) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Delete Exercise")
               .setMessage("Are you sure you want to delete \"" + exercise.getName() + "\"?\n\nThis will also remove all workout history for this exercise.")
               .setPositiveButton("Delete", (dialog, which) -> {
                   executor.execute(() -> {
                       // First delete all exercise records for this exercise
                       database.exerciseRecordDao().deleteRecordsByExerciseId(exercise.getId());
                       // Then delete the exercise itself
                       database.exerciseDao().deleteExercise(exercise);
                       
                       if (getActivity() != null) {
                           getActivity().runOnUiThread(() -> {
                               loadExercises();
                               Toast.makeText(getContext(), "Exercise deleted", Toast.LENGTH_SHORT).show();
                           });
                       }
                   });
               })
               .setNegativeButton("Cancel", null)
               .show();
    }
    
    private void showAddExerciseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(
            android.R.layout.simple_expandable_list_item_2, null);
        
        // Create custom dialog layout
        LinearLayout dialogLayout = new LinearLayout(getContext());
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(32, 32, 32, 32);
        
        EditText nameInput = new EditText(getContext());
        nameInput.setHint("Exercise Name (e.g., Push-ups)");
        
        EditText descInput = new EditText(getContext());
        descInput.setHint("Description");
        
        EditText muscleInput = new EditText(getContext());
        muscleInput.setHint("Muscle Group (e.g., chest, arms)");
        
        dialogLayout.addView(nameInput);
        dialogLayout.addView(descInput);
        dialogLayout.addView(muscleInput);
        
        builder.setTitle("Add Custom Exercise")
               .setView(dialogLayout)
               .setPositiveButton("Add", (dialog, which) -> {
                   String name = nameInput.getText().toString().trim();
                   String desc = descInput.getText().toString().trim();
                   String muscle = muscleInput.getText().toString().trim();
                   
                   if (!name.isEmpty()) {
                       Exercise newExercise = new Exercise(name, "strength", desc, muscle, true);
                       executor.execute(() -> {
                           database.exerciseDao().insertExercise(newExercise);
                           if (getActivity() != null) {
                               getActivity().runOnUiThread(() -> {
                                   loadExercises();
                                   Toast.makeText(getContext(), "Exercise added!", Toast.LENGTH_SHORT).show();
                               });
                           }
                       });
                   }
               })
               .setNegativeButton("Cancel", null)
               .show();
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