package com.github.vitalibo.hbase.api.core;

import com.github.vitalibo.hbase.api.core.util.Specification;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.function.Consumer;

public class ValidationRulesTest {

    @Mock
    private Consumer<String> mockConsumer;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    public void testRequiredNotNull() {
        Specification<String> specification = ValidationRules.requiredNotNull();

        specification.accept("1", mockConsumer);

        Mockito.verify(mockConsumer, Mockito.never()).accept(Mockito.any());
    }

    @Test
    public void testRequiredNotNullWhenNull() {
        Specification<String> specification = ValidationRules.requiredNotNull();

        specification.accept(null, mockConsumer);

        Mockito.verify(mockConsumer).accept("The required field can't be null");
    }

    @Test
    public void testRequiredNotEmpty() {
        Specification<String> specification = ValidationRules.requiredNotEmpty();

        specification.accept("1", mockConsumer);

        Mockito.verify(mockConsumer, Mockito.never()).accept(Mockito.any());
    }

    @Test
    public void testRequiredNotEmptyWhenEmpty() {
        Specification<String> specification = ValidationRules.requiredNotEmpty();

        specification.accept("", mockConsumer);

        Mockito.verify(mockConsumer).accept("The required field can't be empty");
    }

    @Test
    public void testRequiredNotEmptyWhenSpace() {
        Specification<String> specification = ValidationRules.requiredNotEmpty();

        specification.accept("    ", mockConsumer);

        Mockito.verify(mockConsumer).accept("The required field can't be empty");
    }

    @Test
    public void testRequiredEqualsTo() {
        Specification<String> specification = ValidationRules.requiredEqualsTo("foo");

        specification.accept("foo", mockConsumer);

        Mockito.verify(mockConsumer, Mockito.never()).accept(Mockito.any());
    }

    @Test
    public void testRequiredEqualsToWhenNotEqual() {
        Specification<String> specification = ValidationRules.requiredEqualsTo("foo");

        specification.accept("bar", mockConsumer);

        Mockito.verify(mockConsumer).accept("The value is not equal to 'foo'");
    }

    @Test
    public void testRequiredLength() {
        Specification<String> specification = ValidationRules.requiredLength(3, 10);

        specification.accept("foo", mockConsumer);

        Mockito.verify(mockConsumer, Mockito.never()).accept(Mockito.any());
    }

    @Test
    public void testRequiredLengthWhenLess() {
        Specification<String> specification = ValidationRules.requiredLength(3, 10);

        specification.accept("fo", mockConsumer);

        Mockito.verify(mockConsumer).accept(
            "The field has length constraints: Minimum length of 3. Maximum length of 10");
    }

    @Test
    public void testRequiredLengthWhenMore() {
        Specification<String> specification = ValidationRules.requiredLength(3, 10);

        specification.accept("123456789101", mockConsumer);

        Mockito.verify(mockConsumer).accept(
            "The field has length constraints: Minimum length of 3. Maximum length of 10");
    }

    @Test
    public void testRequiredMatchRegex() {
        Specification<String> specification = ValidationRules.requiredMatchRegex("(foo|bar)[0-9]+");

        specification.accept("foo123", mockConsumer);

        Mockito.verify(mockConsumer, Mockito.never()).accept(Mockito.any());
    }

    @Test
    public void testRequiredMatchRegexWhenNotMatch() {
        Specification<String> specification = ValidationRules.requiredMatchRegex("(foo|bar)[0-9]+");

        specification.accept("foo", mockConsumer);

        Mockito.verify(mockConsumer).accept("The field must match to regex '(foo|bar)[0-9]+'");
    }

    @Test
    public void testRequiredAnyMatch() {
        Specification<String> specification = ValidationRules.requiredAnyMatch("foo", "bar");

        specification.accept("bar", mockConsumer);

        Mockito.verify(mockConsumer, Mockito.never()).accept(Mockito.any());
    }

    @Test
    public void testRequiredAnyMatchWhenNotMatch() {
        Specification<String> specification = ValidationRules.requiredAnyMatch("foo", "bar");

        specification.accept("baz", mockConsumer);

        Mockito.verify(mockConsumer).accept(
            "The value is not allowed for field. List allowed values: [foo, bar]");
    }

    @Test
    public void testRequiredBetween() {
        Specification<Integer> specification = ValidationRules.requiredBetween(10, 20);

        specification.accept(10, mockConsumer);

        Mockito.verify(mockConsumer, Mockito.never()).accept(Mockito.any());
    }

    @Test
    public void testRequiredBetweenWhenLess() {
        Specification<Integer> specification = ValidationRules.requiredBetween(10, 20);

        specification.accept(9, mockConsumer);

        Mockito.verify(mockConsumer).accept("The value should be between [10, 20]");
    }

    @Test
    public void testRequiredBetweenWhenMore() {
        Specification<Integer> specification = ValidationRules.requiredBetween(10, 20);

        specification.accept(21, mockConsumer);

        Mockito.verify(mockConsumer).accept("The value should be between [10, 20]");
    }

}
