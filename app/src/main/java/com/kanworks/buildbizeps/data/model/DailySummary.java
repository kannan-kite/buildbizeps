package com.kanworks.buildbizeps.data.model;

public class DailySummary {
    private String date;
    private int totalBicepsReps;
    private int totalPullupsReps;
    private int totalSets;
    private int workoutCount;
    private long totalDurationMinutes;
    
    public DailySummary() {}
    
    public DailySummary(String date, int totalBicepsReps, int totalPullupsReps, 
                       int totalSets, int workoutCount, long totalDurationMinutes) {
        this.date = date;
        this.totalBicepsReps = totalBicepsReps;
        this.totalPullupsReps = totalPullupsReps;
        this.totalSets = totalSets;
        this.workoutCount = workoutCount;
        this.totalDurationMinutes = totalDurationMinutes;
    }
    
    // Getters and Setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public int getTotalBicepsReps() { return totalBicepsReps; }
    public void setTotalBicepsReps(int totalBicepsReps) { this.totalBicepsReps = totalBicepsReps; }
    
    public int getTotalPullupsReps() { return totalPullupsReps; }
    public void setTotalPullupsReps(int totalPullupsReps) { this.totalPullupsReps = totalPullupsReps; }
    
    public int getTotalSets() { return totalSets; }
    public void setTotalSets(int totalSets) { this.totalSets = totalSets; }
    
    public int getWorkoutCount() { return workoutCount; }
    public void setWorkoutCount(int workoutCount) { this.workoutCount = workoutCount; }
    
    public long getTotalDurationMinutes() { return totalDurationMinutes; }
    public void setTotalDurationMinutes(long totalDurationMinutes) { this.totalDurationMinutes = totalDurationMinutes; }
    
    public int getTotalReps() {
        return totalBicepsReps + totalPullupsReps;
    }
}