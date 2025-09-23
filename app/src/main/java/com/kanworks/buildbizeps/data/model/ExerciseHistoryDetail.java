package com.kanworks.buildbizeps.data.model;

import java.util.List;

/**
 * Model class for detailed exercise history display
 * Shows exercise name and all sets/reps/weight for a specific workout
 */
public class ExerciseHistoryDetail {
    private String exerciseName;
    private int totalSets;
    private List<SetDetail> setDetails;
    
    public ExerciseHistoryDetail(String exerciseName, int totalSets, List<SetDetail> setDetails) {
        this.exerciseName = exerciseName;
        this.totalSets = totalSets;
        this.setDetails = setDetails;
    }
    
    // Getters
    public String getExerciseName() { return exerciseName; }
    public int getTotalSets() { return totalSets; }
    public List<SetDetail> getSetDetails() { return setDetails; }
    
    /**
     * Format exercise history as: "squats: 20kg×3  30kg×5  40kg×3"
     */
    public String getFormattedHistory() {
        StringBuilder sb = new StringBuilder();
        sb.append(exerciseName.toLowerCase()).append(": ");
        
        for (int i = 0; i < setDetails.size(); i++) {
            SetDetail set = setDetails.get(i);
            if (set.getWeight() > 0) {
                sb.append(String.format("%.0fkg×%d", set.getWeight(), set.getReps()));
            } else {
                sb.append(String.format("%d reps", set.getReps()));
            }
            
            if (i < setDetails.size() - 1) {
                sb.append("  ");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Format as table row with proper padding for alignment
     */
    public String getTableFormattedHistory() {
        StringBuilder sb = new StringBuilder();
        
        // Exercise name with proper padding (limit to 15 chars, pad with spaces)
        String paddedName = String.format("%-15s", 
            exerciseName.length() > 15 ? exerciseName.substring(0, 15) : exerciseName);
        sb.append(paddedName).append(" ");
        
        for (int i = 0; i < setDetails.size(); i++) {
            SetDetail set = setDetails.get(i);
            if (set.getWeight() > 0) {
                sb.append(String.format("%.0fkg×%d", set.getWeight(), set.getReps()));
            } else {
                sb.append(String.format("%d reps", set.getReps()));
            }
            
            if (i < setDetails.size() - 1) {
                sb.append("  ");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Inner class to represent individual set details
     */
    public static class SetDetail {
        private int reps;
        private float weight;
        
        public SetDetail(int reps, float weight) {
            this.reps = reps;
            this.weight = weight;
        }
        
        public int getReps() { return reps; }
        public float getWeight() { return weight; }
    }
}