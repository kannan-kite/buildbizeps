package com.kanworks.buildbizeps.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.kanworks.buildbizeps.data.entity.ExerciseRecord;

import java.util.Date;
import java.util.List;

@Dao
public interface ExerciseRecordDao {
    
    @Query("SELECT * FROM exercise_records ORDER BY timestamp DESC")
    List<ExerciseRecord> getAllExerciseRecords();
    
    @Query("SELECT * FROM exercise_records WHERE workoutSessionId = :sessionId")
    List<ExerciseRecord> getRecordsByWorkoutSession(int sessionId);
    
    @Query("SELECT * FROM exercise_records WHERE exerciseId = :exerciseId ORDER BY timestamp DESC")
    List<ExerciseRecord> getRecordsByExercise(int exerciseId);
    
    @Query("SELECT * FROM exercise_records WHERE DATE(timestamp/1000, 'unixepoch') = DATE(:date/1000, 'unixepoch')")
    List<ExerciseRecord> getRecordsByDate(Date date);
    
    @Query("SELECT SUM(reps) FROM exercise_records WHERE exerciseId = :exerciseId AND DATE(timestamp/1000, 'unixepoch') = DATE(:date/1000, 'unixepoch')")
    int getTotalRepsByExerciseAndDate(int exerciseId, Date date);
    
    @Insert
    long insertExerciseRecord(ExerciseRecord exerciseRecord);
    
    @Update
    void updateExerciseRecord(ExerciseRecord exerciseRecord);
    
    @Delete
    void deleteExerciseRecord(ExerciseRecord exerciseRecord);
}