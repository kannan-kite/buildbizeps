package com.kanworks.buildbizeps.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.kanworks.buildbizeps.data.dao.ExerciseDao;
import com.kanworks.buildbizeps.data.dao.ExerciseRecordDao;
import com.kanworks.buildbizeps.data.dao.WorkoutSessionDao;
import com.kanworks.buildbizeps.data.entity.Exercise;
import com.kanworks.buildbizeps.data.entity.ExerciseRecord;
import com.kanworks.buildbizeps.data.entity.WorkoutSession;

@Database(
    entities = {Exercise.class, WorkoutSession.class, ExerciseRecord.class},
    version = 1,
    exportSchema = false
)
@TypeConverters({DateConverter.class})
public abstract class FitnessDatabase extends RoomDatabase {
    
    private static volatile FitnessDatabase INSTANCE;
    
    public abstract ExerciseDao exerciseDao();
    public abstract WorkoutSessionDao workoutSessionDao();
    public abstract ExerciseRecordDao exerciseRecordDao();
    
    public static FitnessDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (FitnessDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            FitnessDatabase.class, "fitness_database")
                            .allowMainThreadQueries() // For simplicity - in production, use background threads
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}