package com.kanworks.buildbizeps.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.kanworks.buildbizeps.data.entity.Exercise;

import java.util.List;

@Dao
public interface ExerciseDao {
    
    @Query("SELECT * FROM exercises")
    List<Exercise> getAllExercises();
    
    @Query("SELECT * FROM exercises WHERE id = :id")
    Exercise getExerciseById(int id);
    
    @Query("SELECT * FROM exercises WHERE muscleGroup = :muscleGroup")
    List<Exercise> getExercisesByMuscleGroup(String muscleGroup);
    
    @Insert
    long insertExercise(Exercise exercise);
    
    @Update
    void updateExercise(Exercise exercise);
    
    @Delete
    void deleteExercise(Exercise exercise);
}