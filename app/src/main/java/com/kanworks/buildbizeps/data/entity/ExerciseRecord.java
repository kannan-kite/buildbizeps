package com.kanworks.buildbizeps.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "exercise_records",
        foreignKeys = {
            @ForeignKey(entity = Exercise.class,
                       parentColumns = "id",
                       childColumns = "exerciseId",
                       onDelete = ForeignKey.CASCADE),
            @ForeignKey(entity = WorkoutSession.class,
                       parentColumns = "id", 
                       childColumns = "workoutSessionId",
                       onDelete = ForeignKey.CASCADE)
        })
public class ExerciseRecord {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private int exerciseId;
    private int workoutSessionId;
    private int sets;
    private int reps;
    private float weight; // weight used (if applicable)
    private Date timestamp;
    private String notes;
    
    // Constructors
    public ExerciseRecord() {}
    
    public ExerciseRecord(int exerciseId, int workoutSessionId, int sets, int reps) {
        this.exerciseId = exerciseId;
        this.workoutSessionId = workoutSessionId;
        this.sets = sets;
        this.reps = reps;
        this.timestamp = new Date();
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getExerciseId() { return exerciseId; }
    public void setExerciseId(int exerciseId) { this.exerciseId = exerciseId; }
    
    public int getWorkoutSessionId() { return workoutSessionId; }
    public void setWorkoutSessionId(int workoutSessionId) { this.workoutSessionId = workoutSessionId; }
    
    public int getSets() { return sets; }
    public void setSets(int sets) { this.sets = sets; }
    
    public int getReps() { return reps; }
    public void setReps(int reps) { this.reps = reps; }
    
    public float getWeight() { return weight; }
    public void setWeight(float weight) { this.weight = weight; }
    
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}