package com.example;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class AppTest {
    @Test
    public void testAppExecution() {
        App.main(new String[]{});
        assertTrue(true);
    }
}
