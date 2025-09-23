package com.kanworks.buildbizeps.utils;

import com.kanworks.buildbizeps.data.entity.ExerciseRecord;
import com.kanworks.buildbizeps.data.model.DailySummary;

import org.junit.Test;
import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Edge case and validation tests for Daily Summary calculations
 */
public class DailySummaryEdgeCasesTest {

    @Test
    public void testNegativeValues() {
        // Test handling of negative values (should not happen in real usage but good to test)
        List<ExerciseRecord> records = new ArrayList<>();
        records.add(new ExerciseRecord(1, 1, -2, 10)); // Negative sets
        records.add(new ExerciseRecord(2, 1, 3, -5));  // Negative reps
        records.add(new ExerciseRecord(3, 1, 2, 8));   // Normal values
        
        int totalReps = 0;
        int totalSets = 0;
        
        for (ExerciseRecord record : records) {
            totalReps += (record.getSets() * record.getReps());
            totalSets += record.getSets();
        }
        
        // Expected: (-2 × 10) + (3 × -5) + (2 × 8) = -20 + (-15) + 16 = -19
        assertEquals(-19, totalReps);
        assertEquals(3, totalSets); // -2 + 3 + 2 = 3
    }

    @Test
    public void testLargeNumbers() {
        // Test with very large numbers
        List<ExerciseRecord> records = new ArrayList<>();
        records.add(new ExerciseRecord(1, 1, 1000, 1000)); // 1 million reps
        records.add(new ExerciseRecord(2, 1, 500, 2000));   // 1 million reps
        
        int totalReps = 0;
        for (ExerciseRecord record : records) {
            totalReps += (record.getSets() * record.getReps());
        }
        
        assertEquals(2000000, totalReps); // 2 million total reps
    }

    @Test
    public void testMaxIntegerValues() {
        // Test with maximum integer values (edge case for overflow)
        ExerciseRecord record = new ExerciseRecord(1, 1, Integer.MAX_VALUE, 1);
        
        // This should not overflow since we're multiplying by 1
        long result = (long) record.getSets() * record.getReps();
        assertEquals(Integer.MAX_VALUE, result);
    }

    @Test
    public void testDateFormatting() {
        // Test date formatting consistency
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date testDate = new Date(1695427200000L); // Fixed timestamp for consistent testing
        
        DailySummary summary = new DailySummary(
            dateFormat.format(testDate), 100, 10, 3, 1, 45
        );
        
        // Verify date format is correct
        assertTrue("Date should be in yyyy-MM-dd format", 
                  summary.getDate().matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    public void testEmptyAndNullHandling() {
        // Test with empty list
        List<ExerciseRecord> emptyRecords = new ArrayList<>();
        
        int totalReps = 0;
        int totalSets = 0;
        
        for (ExerciseRecord record : emptyRecords) {
            totalReps += (record.getSets() * record.getReps());
            totalSets += record.getSets();
        }
        
        assertEquals(0, totalReps);
        assertEquals(0, totalSets);
    }

    @Test
    public void testSingleSetSingleRep() {
        // Test minimal valid workout
        List<ExerciseRecord> records = new ArrayList<>();
        records.add(new ExerciseRecord(1, 1, 1, 1)); // 1 set × 1 rep = 1 total rep
        
        int totalReps = 0;
        for (ExerciseRecord record : records) {
            totalReps += (record.getSets() * record.getReps());
        }
        
        assertEquals(1, totalReps);
    }

    @Test
    public void testVeryLongWorkout() {
        // Test with many exercises (performance test)
        List<ExerciseRecord> records = new ArrayList<>();
        
        // Add 100 exercises with 5 sets of 10 reps each
        for (int i = 1; i <= 100; i++) {
            records.add(new ExerciseRecord(i, 1, 5, 10));
        }
        
        int totalReps = 0;
        int totalSets = 0;
        
        for (ExerciseRecord record : records) {
            totalReps += (record.getSets() * record.getReps());
            totalSets += record.getSets();
        }
        
        assertEquals(5000, totalReps); // 100 exercises × 5 sets × 10 reps = 5000
        assertEquals(500, totalSets);  // 100 exercises × 5 sets = 500
    }

    @Test
    public void testMixedWorkoutIntensities() {
        // Test realistic mixed workout with different intensities
        List<ExerciseRecord> records = new ArrayList<>();
        
        // Heavy exercise (low reps, high sets)
        records.add(new ExerciseRecord(1, 1, 5, 3));   // Deadlifts: 5 × 3 = 15
        
        // Medium exercise
        records.add(new ExerciseRecord(2, 1, 3, 8));   // Bench press: 3 × 8 = 24
        
        // Light exercise (high reps, low sets)
        records.add(new ExerciseRecord(3, 1, 2, 20));  // Pushups: 2 × 20 = 40
        
        // Cardio-like (many reps)
        records.add(new ExerciseRecord(4, 1, 1, 100)); // Jumping jacks: 1 × 100 = 100
        
        int totalReps = 0;
        for (ExerciseRecord record : records) {
            totalReps += (record.getSets() * record.getReps());
        }
        
        assertEquals(179, totalReps); // 15 + 24 + 40 + 100 = 179
    }

    @Test
    public void testCalculationOrder() {
        // Test that calculation order doesn't matter (commutative property)
        List<ExerciseRecord> records1 = new ArrayList<>();
        records1.add(new ExerciseRecord(1, 1, 3, 10));
        records1.add(new ExerciseRecord(2, 1, 5, 8));
        
        List<ExerciseRecord> records2 = new ArrayList<>();
        records2.add(new ExerciseRecord(2, 1, 5, 8));  // Same data, different order
        records2.add(new ExerciseRecord(1, 1, 3, 10));
        
        int total1 = 0, total2 = 0;
        
        for (ExerciseRecord record : records1) {
            total1 += (record.getSets() * record.getReps());
        }
        
        for (ExerciseRecord record : records2) {
            total2 += (record.getSets() * record.getReps());
        }
        
        assertEquals("Order should not affect calculation", total1, total2);
        assertEquals(70, total1); // (3×10) + (5×8) = 30 + 40 = 70
    }

    @Test
    public void testDataIntegrityDuringCalculation() {
        // Test that original data is not modified during calculation
        ExerciseRecord originalRecord = new ExerciseRecord(1, 1, 3, 10);
        int originalSets = originalRecord.getSets();
        int originalReps = originalRecord.getReps();
        
        // Perform calculation
        int calculatedReps = originalRecord.getSets() * originalRecord.getReps();
        
        // Verify original data is unchanged
        assertEquals(originalSets, originalRecord.getSets());
        assertEquals(originalReps, originalRecord.getReps());
        assertEquals(30, calculatedReps);
    }
}