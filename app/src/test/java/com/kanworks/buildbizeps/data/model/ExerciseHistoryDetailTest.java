package com.kanworks.buildbizeps.data.model;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for ExerciseHistoryDetail class
 * Tests the formatting logic for exercise history display
 */
public class ExerciseHistoryDetailTest {

    @Test
    public void testFormattedHistoryWithWeights() {
        // Test case: bench press: 3 sets; 30.0kg×6, 40.0kg×6, 50.0kg×7
        List<ExerciseHistoryDetail.SetDetail> setDetails = new ArrayList<>();
        setDetails.add(new ExerciseHistoryDetail.SetDetail(6, 30.0f));
        setDetails.add(new ExerciseHistoryDetail.SetDetail(6, 40.0f));
        setDetails.add(new ExerciseHistoryDetail.SetDetail(7, 50.0f));
        
        ExerciseHistoryDetail detail = new ExerciseHistoryDetail("Bench Press", 3, setDetails);
        String expected = "bench press: 3 sets; 30.0kg×6, 40.0kg×6, 50.0kg×7";
        
        assertEquals(expected, detail.getFormattedHistory());
    }

    @Test
    public void testFormattedHistoryWithoutWeights() {
        // Test case: push ups: 2 sets; 15 reps, 12 reps
        List<ExerciseHistoryDetail.SetDetail> setDetails = new ArrayList<>();
        setDetails.add(new ExerciseHistoryDetail.SetDetail(15, 0.0f));
        setDetails.add(new ExerciseHistoryDetail.SetDetail(12, 0.0f));
        
        ExerciseHistoryDetail detail = new ExerciseHistoryDetail("Push Ups", 2, setDetails);
        String expected = "push ups: 2 sets; 15 reps, 12 reps";
        
        assertEquals(expected, detail.getFormattedHistory());
    }

    @Test
    public void testFormattedHistoryMixedWeights() {
        // Test case: squats: 3 sets; 20 reps, 60.0kg×8, 70.0kg×6
        List<ExerciseHistoryDetail.SetDetail> setDetails = new ArrayList<>();
        setDetails.add(new ExerciseHistoryDetail.SetDetail(20, 0.0f));  // bodyweight
        setDetails.add(new ExerciseHistoryDetail.SetDetail(8, 60.0f));   // with weight
        setDetails.add(new ExerciseHistoryDetail.SetDetail(6, 70.0f));   // with weight
        
        ExerciseHistoryDetail detail = new ExerciseHistoryDetail("Squats", 3, setDetails);
        String expected = "squats: 3 sets; 20 reps, 60.0kg×8, 70.0kg×6";
        
        assertEquals(expected, detail.getFormattedHistory());
    }

    @Test
    public void testFormattedHistorySingleSet() {
        // Test case: deadlift: 1 sets; 100.0kg×5
        List<ExerciseHistoryDetail.SetDetail> setDetails = new ArrayList<>();
        setDetails.add(new ExerciseHistoryDetail.SetDetail(5, 100.0f));
        
        ExerciseHistoryDetail detail = new ExerciseHistoryDetail("Deadlift", 1, setDetails);
        String expected = "deadlift: 1 sets; 100.0kg×5";
        
        assertEquals(expected, detail.getFormattedHistory());
    }

    @Test
    public void testGettersAndSetters() {
        List<ExerciseHistoryDetail.SetDetail> setDetails = new ArrayList<>();
        setDetails.add(new ExerciseHistoryDetail.SetDetail(10, 50.0f));
        
        ExerciseHistoryDetail detail = new ExerciseHistoryDetail("Test Exercise", 1, setDetails);
        
        assertEquals("Test Exercise", detail.getExerciseName());
        assertEquals(1, detail.getTotalSets());
        assertEquals(1, detail.getSetDetails().size());
        assertEquals(10, detail.getSetDetails().get(0).getReps());
        assertEquals(50.0f, detail.getSetDetails().get(0).getWeight(), 0.01f);
    }
}