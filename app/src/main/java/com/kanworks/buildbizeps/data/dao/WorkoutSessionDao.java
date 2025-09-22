package com.kanworks.buildbizeps.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.kanworks.buildbizeps.data.entity.WorkoutSession;

import java.util.Date;
import java.util.List;

@Dao
public interface WorkoutSessionDao {
    
    @Query("SELECT * FROM workout_sessions ORDER BY startTime DESC")
    List<WorkoutSession> getAllWorkoutSessions();
    
    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    WorkoutSession getWorkoutSessionById(int id);
    
    @Query("SELECT * FROM workout_sessions WHERE DATE(startTime/1000, 'unixepoch') = DATE(:date/1000, 'unixepoch')")
    List<WorkoutSession> getWorkoutSessionsByDate(Date date);
    
    @Query("DELETE FROM workout_sessions WHERE DATE(startTime/1000, 'unixepoch') = DATE(:date/1000, 'unixepoch')")
    void deleteSessionsByDate(Date date);
    
    @Query("DELETE FROM workout_sessions")
    void deleteAllSessions();
    
    @Insert
    long insertWorkoutSession(WorkoutSession workoutSession);
    
    @Update
    void updateWorkoutSession(WorkoutSession workoutSession);
    
    @Delete
    void deleteWorkoutSession(WorkoutSession workoutSession);
}