package com.kanworks.buildbizeps.data.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class DailySummaryTest {

    private DailySummary dailySummary;

    @Before
    public void setUp() {
        dailySummary = new DailySummary("2025-09-23", 150, 10, 3, 2, 45);
    }

    @Test
    public void testConstructor() {
        assertEquals("2025-09-23", dailySummary.getDate());
        assertEquals(150, dailySummary.getTotalReps());
        assertEquals(10, dailySummary.getTotalSets());
        assertEquals(3, dailySummary.getTotalExercises());
        assertEquals(2, dailySummary.getWorkoutCount());
        assertEquals(45, dailySummary.getTotalDurationMinutes());
    }

    @Test
    public void testDefaultConstructor() {
        DailySummary emptySummary = new DailySummary();
        assertNull(emptySummary.getDate());
        assertEquals(0, emptySummary.getTotalReps());
        assertEquals(0, emptySummary.getTotalSets());
        assertEquals(0, emptySummary.getTotalExercises());
        assertEquals(0, emptySummary.getWorkoutCount());
        assertEquals(0, emptySummary.getTotalDurationMinutes());
    }

    @Test
    public void testSettersAndGetters() {
        DailySummary testSummary = new DailySummary();
        testSummary.setDate("2025-12-25");
        assertEquals("2025-12-25", testSummary.getDate());
        
        testSummary.setTotalReps(200);
        assertEquals(200, testSummary.getTotalReps());
        
        testSummary.setTotalSets(15);
        assertEquals(15, testSummary.getTotalSets());
    }
}
