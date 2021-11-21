package com.github.vitalibo.heatmap.api.core.util;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;

public class ErrorStateTest {

    private ErrorState errorState;

    @BeforeMethod
    public void setUp() {
        errorState = new ErrorState();
    }

    @Test
    public void testHasErrors() {
        errorState.put("k", Arrays.asList("v1", "v2"));

        boolean actual = errorState.hasErrors();

        Assert.assertTrue(actual);
    }

    @Test
    public void testNoHasErrors() {
        boolean actual = errorState.hasErrors();

        Assert.assertFalse(actual);
    }

    @Test
    public void testAddError() {
        errorState.addError("foo", "bar");
        errorState.addError("foo", "baz");

        Assert.assertTrue(errorState.containsKey("foo"));
        Assert.assertEquals(errorState.get("foo"), Arrays.asList("bar", "baz"));
    }

}
