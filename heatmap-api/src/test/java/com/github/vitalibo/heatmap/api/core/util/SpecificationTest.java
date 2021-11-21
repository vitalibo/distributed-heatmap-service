package com.github.vitalibo.heatmap.api.core.util;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.function.Consumer;

public class SpecificationTest {

    @Mock
    private Consumer<String> mockConsumer;
    @Mock
    private Specification<String> mockSpecification;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    public void testTrueAndMock() {
        Specification<String> specification = alwaysTrue().and(mockSpecification);

        specification.accept("foo", mockConsumer);

        Mockito.verify(mockConsumer, Mockito.never()).accept(Mockito.any());
        Mockito.verify(mockSpecification).accept("foo", mockConsumer);
    }

    @Test
    public void testFalseAndMock() {
        Specification<String> specification = alwaysFalse().and(mockSpecification);

        specification.accept("foo", mockConsumer);

        Mockito.verify(mockConsumer).accept("foo=bar");
        Mockito.verify(mockSpecification, Mockito.never()).accept(Mockito.any(), Mockito.any());
    }

    @Test
    public void testTrueAndTrue() {
        Specification<String> specification = alwaysTrue().and(alwaysTrue());

        specification.accept("foo", mockConsumer);

        Mockito.verify(mockConsumer, Mockito.never()).accept(Mockito.any());
    }

    @Test
    public void testTrueAndFalse() {
        Specification<String> specification = alwaysTrue().and(alwaysFalse());

        specification.accept("foo", mockConsumer);

        Mockito.verify(mockConsumer).accept("foo=bar");
    }

    @Test
    public void testTrueOrMock() {
        Specification<String> specification = alwaysTrue().or(mockSpecification);

        specification.accept("foo", mockConsumer);

        Mockito.verify(mockConsumer, Mockito.never()).accept(Mockito.any());
        Mockito.verify(mockSpecification, Mockito.never()).accept(Mockito.any(), Mockito.any());
    }

    @Test
    public void testFalseOrMock() {
        Specification<String> specification = alwaysFalse().or(mockSpecification);

        specification.accept("foo", mockConsumer);

        Mockito.verify(mockConsumer, Mockito.never()).accept(Mockito.any());
        Mockito.verify(mockSpecification).accept(Mockito.eq("foo"), Mockito.any());
    }

    @Test
    public void testTrueOrFalse() {
        Specification<String> specification = alwaysTrue().or(alwaysFalse());

        specification.accept("foo", mockConsumer);

        Mockito.verify(mockConsumer, Mockito.never()).accept(Mockito.any());
    }

    @Test
    public void testFalseOrTrue() {
        Specification<String> specification = alwaysFalse().or(alwaysTrue());

        specification.accept("foo", mockConsumer);

        Mockito.verify(mockConsumer, Mockito.never()).accept(Mockito.any());
    }

    @Test
    public void testFalseOrFalse() {
        Specification<String> specification = alwaysFalse().or(alwaysFalse());

        specification.accept("foo", mockConsumer);

        Mockito.verify(mockConsumer, Mockito.times(2)).accept("foo=bar");
    }

    @Test
    public void testNamed() {
        Rule<String> rule = alwaysFalse()
            .named("baz");

        ErrorState errorState = new ErrorState();
        rule.accept("foo", errorState);

        Assert.assertTrue(errorState.hasErrors());
        Assert.assertEquals(errorState.get("baz"), Collections.singletonList("foo=bar"));
    }

    public Specification<String> alwaysFalse() {
        return (s, consumer) -> consumer.accept(s + "=bar");
    }

    public Specification<String> alwaysTrue() {
        return (s, consumer) -> {
        };
    }

}
