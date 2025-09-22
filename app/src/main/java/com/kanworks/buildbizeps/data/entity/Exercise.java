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
    private boolean isFavorite; // New field for favorites
    private boolean isCustom;   // Track if user-created or default
    
    // Constructors
    public Exercise() {}
    
    public Exercise(String name, String type, String description, String muscleGroup) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.muscleGroup = muscleGroup;
        this.isFavorite = false;
        this.isCustom = false;
    }
    
    public Exercise(String name, String type, String description, String muscleGroup, boolean isCustom) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.muscleGroup = muscleGroup;
        this.isFavorite = false;
        this.isCustom = isCustom;
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
    
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    
    public boolean isCustom() { return isCustom; }
    public void setCustom(boolean custom) { isCustom = custom; }
}