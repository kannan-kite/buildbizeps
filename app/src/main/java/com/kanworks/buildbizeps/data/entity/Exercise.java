package com.kanworks.buildbizeps.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "exercises")
public class Exercise {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String name;
    private String type; // "strength", "cardio", etc.
    private String description;
    private String muscleGroup;
    
    // Constructors
    public Exercise() {}
    
    public Exercise(String name, String type, String description, String muscleGroup) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.muscleGroup = muscleGroup;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getMuscleGroup() { return muscleGroup; }
    public void setMuscleGroup(String muscleGroup) { this.muscleGroup = muscleGroup; }
}