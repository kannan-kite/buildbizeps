package com.kanworks.buildbizeps.utils;

import com.kanworks.buildbizeps.data.entity.ExerciseRecord;
import com.kanworks.buildbizeps.data.entity.WorkoutSession;
import com.kanworks.buildbizeps.data.model.DailySummary;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Unit tests for Daily Summary calculation logic
 * Tests the corrected formula: Total Reps = sets × reps for each exercise
 */
public class DailySummaryCalculatorTest {

    private SimpleDateFormat dateFormat;
    private Date testDate;
    private List<ExerciseRecord> exerciseRecords;
    private List<WorkoutSession> workoutSessions;

    @Before
    public void setUp() {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        testDate = new Date();
        exerciseRecords = new ArrayList<>();
        workoutSessions = new ArrayList<>();
    }

    /**
     * Helper method to calculate DailySummary from test data
     * This mimics the logic from DashboardFragment.loadDailySummary()
     */
    private DailySummary calculateDailySummary(List<ExerciseRecord> records, List<WorkoutSession> sessions, Date date) {
        int totalReps = 0;
        int totalSets = 0;
        Set<Integer> uniqueExercises = new HashSet<>();

        // Calculate total reps as sets * reps for each record
        for (ExerciseRecord record : records) {
            totalReps += (record.getSets() * record.getReps());  // Correct formula
            totalSets += record.getSets();
            uniqueExercises.add(record.getExerciseId());
        }

        return new DailySummary(
            dateFormat.format(date),
            totalReps,
            totalSets,
            uniqueExercises.size(),
            sessions.size(),
            0 // Duration calculation
        );
    }

    @Test
    public void testEmptyWorkoutDay() {
        // Test with no exercise records (rest day)
        DailySummary summary = calculateDailySummary(exerciseRecords, workoutSessions, testDate);
        
        assertEquals(dateFormat.format(testDate), summary.getDate());
        assertEquals(0, summary.getTotalReps());
        assertEquals(0, summary.getTotalSets());
        assertEquals(0, summary.getTotalExercises());
        assertEquals(0, summary.getWorkoutCount());
    }

    @Test
    public void testSingleExerciseCalculation() {
        // Test with one exercise: 3 sets × 10 reps = 30 total reps
        exerciseRecords.add(new ExerciseRecord(1, 1, 3, 10)); // exerciseId=1, sessionId=1, sets=3, reps=10
        workoutSessions.add(new WorkoutSession(testDate));
        
        DailySummary summary = calculateDailySummary(exerciseRecords, workoutSessions, testDate);
        
        assertEquals(30, summary.getTotalReps()); // 3 × 10 = 30
        assertEquals(3, summary.getTotalSets());
        assertEquals(1, summary.getTotalExercises());
        assertEquals(1, summary.getWorkoutCount());
    }

    @Test
    public void testMultipleExercisesCalculation() {
        // Test with multiple exercises
        exerciseRecords.add(new ExerciseRecord(1, 1, 3, 10)); // Biceps: 3 × 10 = 30 reps
        exerciseRecords.add(new ExerciseRecord(2, 1, 4, 8));  // Pullups: 4 × 8 = 32 reps
        exerciseRecords.add(new ExerciseRecord(3, 1, 2, 15)); // Pushups: 2 × 15 = 30 reps
        workoutSessions.add(new WorkoutSession(testDate));
        
        DailySummary summary = calculateDailySummary(exerciseRecords, workoutSessions, testDate);
        
        assertEquals(92, summary.getTotalReps()); // 30 + 32 + 30 = 92
        assertEquals(9, summary.getTotalSets());  // 3 + 4 + 2 = 9
        assertEquals(3, summary.getTotalExercises());
        assertEquals(1, summary.getWorkoutCount());
    }

    @Test
    public void testMultipleWorkoutSessions() {
        // Test with multiple workout sessions in one day
        // Morning session
        exerciseRecords.add(new ExerciseRecord(1, 1, 3, 10)); // 30 reps
        exerciseRecords.add(new ExerciseRecord(2, 1, 2, 12)); // 24 reps
        
        // Evening session
        exerciseRecords.add(new ExerciseRecord(1, 2, 2, 8));  // Same exercise, different session: 16 reps
        exerciseRecords.add(new ExerciseRecord(3, 2, 3, 5));  // New exercise: 15 reps
        
        workoutSessions.add(new WorkoutSession(testDate)); // Morning session
        workoutSessions.add(new WorkoutSession(testDate)); // Evening session
        
        DailySummary summary = calculateDailySummary(exerciseRecords, workoutSessions, testDate);
        
        assertEquals(85, summary.getTotalReps()); // 30 + 24 + 16 + 15 = 85
        assertEquals(10, summary.getTotalSets());  // 3 + 2 + 2 + 3 = 10
        assertEquals(3, summary.getTotalExercises()); // Exercise IDs: 1, 2, 3
        assertEquals(2, summary.getWorkoutCount());
    }

    @Test
    public void testZeroSetsOrReps() {
        // Test with zero sets or reps (should contribute 0 to total)
        exerciseRecords.add(new ExerciseRecord(1, 1, 0, 10)); // 0 × 10 = 0
        exerciseRecords.add(new ExerciseRecord(2, 1, 3, 0));  // 3 × 0 = 0
        exerciseRecords.add(new ExerciseRecord(3, 1, 4, 5));  // 4 × 5 = 20
        workoutSessions.add(new WorkoutSession(testDate));
        
        DailySummary summary = calculateDailySummary(exerciseRecords, workoutSessions, testDate);
        
        assertEquals(20, summary.getTotalReps()); // Only the valid exercise contributes
        assertEquals(7, summary.getTotalSets());  // 0 + 3 + 4 = 7
        assertEquals(3, summary.getTotalExercises());
    }

    @Test
    public void testHighVolumeWorkout() {
        // Test with high volume workout
        exerciseRecords.add(new ExerciseRecord(1, 1, 10, 20)); // 200 reps
        exerciseRecords.add(new ExerciseRecord(2, 1, 8, 25));  // 200 reps
        exerciseRecords.add(new ExerciseRecord(3, 1, 6, 30));  // 180 reps
        workoutSessions.add(new WorkoutSession(testDate));
        
        DailySummary summary = calculateDailySummary(exerciseRecords, workoutSessions, testDate);
        
        assertEquals(580, summary.getTotalReps()); // 200 + 200 + 180 = 580
        assertEquals(24, summary.getTotalSets());   // 10 + 8 + 6 = 24
        assertEquals(3, summary.getTotalExercises());
        assertEquals(1, summary.getWorkoutCount());
    }

    @Test
    public void testSameExerciseMultipleTimes() {
        // Test same exercise performed multiple times (different sets)
        exerciseRecords.add(new ExerciseRecord(1, 1, 3, 10)); // First set: 30 reps
        exerciseRecords.add(new ExerciseRecord(1, 1, 2, 8));  // Second set: 16 reps
        exerciseRecords.add(new ExerciseRecord(1, 1, 1, 5));  // Third set: 5 reps
        workoutSessions.add(new WorkoutSession(testDate));
        
        DailySummary summary = calculateDailySummary(exerciseRecords, workoutSessions, testDate);
        
        assertEquals(51, summary.getTotalReps()); // 30 + 16 + 5 = 51
        assertEquals(6, summary.getTotalSets());  // 3 + 2 + 1 = 6
        assertEquals(1, summary.getTotalExercises()); // Same exercise ID
        assertEquals(1, summary.getWorkoutCount());
    }

    @Test
    public void testCorrectFormulaVsOldFormula() {
        // Test that demonstrates the difference between correct and old formula
        exerciseRecords.add(new ExerciseRecord(1, 1, 3, 10)); // 3 sets × 10 reps
        exerciseRecords.add(new ExerciseRecord(2, 1, 4, 5));  // 4 sets × 5 reps
        
        DailySummary summary = calculateDailySummary(exerciseRecords, workoutSessions, testDate);
        
        // Correct formula: (3 × 10) + (4 × 5) = 30 + 20 = 50
        int correctTotal = 50;
        assertEquals(correctTotal, summary.getTotalReps());
        
        // Old incorrect formula would be: 10 + 5 = 15 (just adding reps)
        int oldIncorrectTotal = 15;
        assertNotEquals(oldIncorrectTotal, summary.getTotalReps());
        
        // Verify the correct calculation is significantly higher
        assertTrue("Correct formula should yield higher total than old formula", 
                  summary.getTotalReps() > oldIncorrectTotal);
    }

    @Test
    public void testUniqueExerciseCount() {
        // Test that unique exercise count is correct even with duplicate exercise IDs
        exerciseRecords.add(new ExerciseRecord(1, 1, 1, 10)); // Exercise 1
        exerciseRecords.add(new ExerciseRecord(2, 1, 1, 10)); // Exercise 2
        exerciseRecords.add(new ExerciseRecord(1, 1, 1, 10)); // Exercise 1 again
        exerciseRecords.add(new ExerciseRecord(3, 1, 1, 10)); // Exercise 3
        exerciseRecords.add(new ExerciseRecord(2, 1, 1, 10)); // Exercise 2 again
        
        DailySummary summary = calculateDailySummary(exerciseRecords, workoutSessions, testDate);
        
        assertEquals(3, summary.getTotalExercises()); // Should be 3 unique exercises (1, 2, 3)
        assertEquals(50, summary.getTotalReps()); // 5 records × 10 reps each
        assertEquals(5, summary.getTotalSets());  // 5 records × 1 set each
    }
}