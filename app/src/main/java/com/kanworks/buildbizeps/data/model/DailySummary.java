package com.kanworks.buildbizeps.data.model;

public class DailySummary {
    private String date;
    private int totalReps;  // This will be sets * reps for all exercises
    private int totalSets;
    private int totalExercises;  // Number of different exercises performed
    private int workoutCount;
    private long totalDurationMinutes;
    
    public DailySummary() {}
    
    public DailySummary(String date, int totalReps, int totalSets, 
                       int totalExercises, int workoutCount, long totalDurationMinutes) {
        this.date = date;
        this.totalReps = totalReps;
        this.totalSets = totalSets;
        this.totalExercises = totalExercises;
        this.workoutCount = workoutCount;
        this.totalDurationMinutes = totalDurationMinutes;
    }
    
    // Getters and Setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public int getTotalReps() { return totalReps; }
    public void setTotalReps(int totalReps) { this.totalReps = totalReps; }
    
    public int getTotalSets() { return totalSets; }
    public void setTotalSets(int totalSets) { this.totalSets = totalSets; }
    
    public int getTotalExercises() { return totalExercises; }
    public void setTotalExercises(int totalExercises) { this.totalExercises = totalExercises; }
    
    public int getWorkoutCount() { return workoutCount; }
    public void setWorkoutCount(int workoutCount) { this.workoutCount = workoutCount; }
    
    public long getTotalDurationMinutes() { return totalDurationMinutes; }
    public void setTotalDurationMinutes(long totalDurationMinutes) { this.totalDurationMinutes = totalDurationMinutes; }
}