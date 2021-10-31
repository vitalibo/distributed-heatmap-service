package com.github.vitalibo.hbase.api.core;

import com.github.vitalibo.hbase.api.core.model.HeatmapRequest;
import com.github.vitalibo.hbase.api.core.model.HttpRequest;
import com.github.vitalibo.hbase.api.core.util.ErrorState;
import com.github.vitalibo.hbase.api.core.util.Rule;
import com.github.vitalibo.hbase.api.core.util.Specification;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
    public void testPossibleNull() {
        Specification<String> specification = ValidationRules::possibleNull;

        specification.accept(null, mockConsumer);

        Mockito.verify(mockConsumer, Mockito.never()).accept(Mockito.any());
    }

    @Test
    public void testPossibleNullWhenNotNull() {
        Specification<String> specification = ValidationRules::possibleNull;

        specification.accept("", mockConsumer);

        Mockito.verify(mockConsumer).accept("The optional field can be null");
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

    @Test
    public void testRequiredStandardISO8601() {
        Specification<String> specification = ValidationRules.requiredStandardISO8601();

        specification.accept("2011-12-03T10:15:30Z", mockConsumer);

        Mockito.verify(mockConsumer, Mockito.never()).accept(Mockito.any());
    }

    @Test
    public void testRequiredStandardISO8601WhenIncorrectFormat() {
        Specification<String> specification = ValidationRules.requiredStandardISO8601();

        specification.accept("2011-12-03T10:15:3", mockConsumer);

        Mockito.verify(mockConsumer).accept("The timestamp field must meet to ISO-8601 standard, such as '2011-12-03T10:15:30Z'");
        Mockito.verify(mockConsumer, Mockito.times(1)).accept(Mockito.any());
    }

    @Test
    public void testRequiredStandardISO8601WhenHourEquals32() {
        Specification<String> specification = ValidationRules.requiredStandardISO8601();

        specification.accept("2011-12-03T32:15:30Z", mockConsumer);

        Mockito.verify(mockConsumer).accept("Invalid value for HourOfDay (valid values 0 - 23): 32");
    }

    @Test
    public void testRequiredStandardISO8601WhenFebruary30() {
        Specification<String> specification = ValidationRules.requiredStandardISO8601();

        specification.accept("2011-02-30T12:15:30Z", mockConsumer);

        Mockito.verify(mockConsumer).accept("Invalid date 'FEBRUARY 30'");
    }

    @DataProvider
    public Object[][] sampleQueryParameterId() {
        return new Object[][]{
            {"0", null},
            {"1", null},
            {"123", null},
            {null, errors("The required field can't be null")},
            {" ", errors("The required field can't be empty")},
            {"-1", errors("The field must match to regex '[0-9]+'")},
            {"12.3", errors("The field must match to regex '[0-9]+'")},
        };
    }

    @Test(dataProvider = "sampleQueryParameterId")
    public void testVerifyQueryParameterId(String id, List<String> expected) {
        HttpRequest request = new HttpRequest();
        request.setQueryStringParameters(Collections.singletonMap("id", id));
        Rule<HttpRequest> rule = ValidationRules.verifyQueryParameterId();
        ErrorState errorState = new ErrorState();

        rule.accept(request, errorState);

        Assert.assertEquals(errorState.hasErrors(), Objects.nonNull(expected));
        Assert.assertTrue(errorState.size() <= 1);
        Assert.assertEquals(errorState.get("id"), expected);
    }

    @DataProvider
    public Object[][] sampleQueryParameterFromUntil() {
        return new Object[][]{
            {"2011-02-28T12:15:30Z", null},
            {"2021-10-31T18:32:51Z", null},
            {null, errors("The required field can't be null")},
            {" ", errors("The required field can't be empty")},
            {"2011-02-28T12:15:30", errors("The timestamp field must meet to ISO-8601 standard, such as '2011-12-03T10:15:30Z'")},
            {"2011-02-28T12:15:30+03:00", errors("The timestamp field must meet to ISO-8601 standard, such as '2011-12-03T10:15:30Z'")},
            {"2011-02-30T12:15:30Z", errors("Invalid date 'FEBRUARY 30'")},
            {"2021-10-31T28:32:51Z", errors("Invalid value for HourOfDay (valid values 0 - 23): 28")},
            {"2021-10-31T18:32:91Z", errors("Invalid value for SecondOfMinute (valid values 0 - 59): 91")},
        };
    }

    @Test(dataProvider = "sampleQueryParameterFromUntil")
    public void testVerifyQueryParameterFrom(String from, List<String> expected) {
        HttpRequest request = new HttpRequest();
        request.setQueryStringParameters(Collections.singletonMap("from", from));
        Rule<HttpRequest> rule = ValidationRules.verifyQueryParameterFrom();
        ErrorState errorState = new ErrorState();

        rule.accept(request, errorState);

        Assert.assertEquals(errorState.hasErrors(), Objects.nonNull(expected));
        Assert.assertTrue(errorState.size() <= 1);
        Assert.assertEquals(errorState.get("from"), expected);
    }

    @Test(dataProvider = "sampleQueryParameterFromUntil")
    public void testVerifyQueryParameterUntil(String from, List<String> expected) {
        HttpRequest request = new HttpRequest();
        request.setQueryStringParameters(Collections.singletonMap("until", from));
        Rule<HttpRequest> rule = ValidationRules.verifyQueryParameterUntil();
        ErrorState errorState = new ErrorState();

        rule.accept(request, errorState);

        Assert.assertEquals(errorState.hasErrors(), Objects.nonNull(expected));
        Assert.assertTrue(errorState.size() <= 1);
        Assert.assertEquals(errorState.get("until"), expected);
    }

    @DataProvider
    public Object[][] sampleQueryParameterRadius() {
        return new Object[][]{
            {"1", null},
            {"123", null},
            {null, null},
            {" ", errors("The optional field can be null", "The required field can't be empty")},
            {"-123", errors("The field must match to regex '[0-9]+'", "The optional field can be null")},
            {"123 ", errors("The field must match to regex '[0-9]+'", "The optional field can be null")},
            {"12.3", errors("The field must match to regex '[0-9]+'", "The optional field can be null")}
        };
    }

    @Test(dataProvider = "sampleQueryParameterRadius")
    public void testVerifyQueryParameterRadius(String radius, List<String> expected) {
        HttpRequest request = new HttpRequest();
        request.setQueryStringParameters(Collections.singletonMap("radius", radius));
        Rule<HttpRequest> rule = ValidationRules.verifyQueryParameterRadius();
        ErrorState errorState = new ErrorState();

        rule.accept(request, errorState);

        Assert.assertEquals(errorState.hasErrors(), Objects.nonNull(expected));
        Assert.assertTrue(errorState.size() <= 1);
        Assert.assertEquals(errorState.get("radius"), expected);
    }

    @DataProvider
    public Object[][] sampleQueryParameterOpacity() {
        return new Object[][]{
            {"0.5", null},
            {"1.5", null},
            {"1.", null},
            {"0.", null},
            {"1.234", null},
            {null, null},
            {" ", errors("The optional field can be null", "The required field can't be empty")},
            {" 1.0", errors("The field must match to regex '[0-1]\\.[0-9]*'", "The optional field can be null")},
            {"-1.0", errors("The field must match to regex '[0-1]\\.[0-9]*'", "The optional field can be null")},
            {"2.4", errors("The field must match to regex '[0-1]\\.[0-9]*'", "The optional field can be null")}
        };
    }

    @Test(dataProvider = "sampleQueryParameterOpacity")
    public void testVerifyQueryParameterOpacity(String opacity, List<String> expected) {
        HttpRequest request = new HttpRequest();
        request.setQueryStringParameters(Collections.singletonMap("opacity", opacity));
        Rule<HttpRequest> rule = ValidationRules.verifyQueryParameterOpacity();
        ErrorState errorState = new ErrorState();

        rule.accept(request, errorState);

        Assert.assertEquals(errorState.hasErrors(), Objects.nonNull(expected));
        Assert.assertTrue(errorState.size() <= 1);
        Assert.assertEquals(errorState.get("opacity"), expected);
    }

    @DataProvider
    public Object[][] sampleHeatmapRequestSupportedQueryParameters() {
        return new Object[][]{
            {"id", null},
            {"from", null},
            {"until", null},
            {"radius", null},
            {"opacity", null},
            {"foo", errors("Unsupported parameter")},
            {"form", errors("Unsupported parameter")}
        };
    }

    @Test(dataProvider = "sampleHeatmapRequestSupportedQueryParameters")
    public void testVerifyHeatmapRequestSupportedQueryParameters(String column, List<String> expected) {
        HttpRequest request = new HttpRequest();
        request.setQueryStringParameters(Collections.singletonMap(column, "foo"));
        Rule<HttpRequest> rule = ValidationRules.verifyHeatmapRequestSupportedQueryParameters();
        ErrorState errorState = new ErrorState();

        rule.accept(request, errorState);

        Assert.assertEquals(errorState.hasErrors(), Objects.nonNull(expected));
        Assert.assertTrue(errorState.size() <= 1);
        Assert.assertEquals(errorState.get(column), expected);
    }

    @DataProvider
    public Object[][] sampleFromIsBeforeUntil() {
        return new Object[][]{
            {"2021-10-30T00:00:00Z", "2021-10-31T00:00:00Z", null},
            {"2021-10-31T00:00:00Z", "2021-10-31T00:00:01Z", null},
            {"2021-10-31T00:00:00Z", "2021-10-31T00:00:00Z", errors("The field value should be before 'until' timestamp.")},
            {"2021-10-31T00:00:00Z", "2021-10-30T00:00:00Z", errors("The field value should be before 'until' timestamp.")}
        };
    }

    @Test(dataProvider = "sampleFromIsBeforeUntil")
    public void testVerifyFromIsBeforeUntil(String from, String until, List<String> expected) {
        HeatmapRequest request = new HeatmapRequest();
        request.setFrom(LocalDateTime.parse(from, DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        request.setUnit(LocalDateTime.parse(until, DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        Rule<HeatmapRequest> rule = ValidationRules.verifyFromIsBeforeUntil();
        ErrorState errorState = new ErrorState();

        rule.accept(request, errorState);

        Assert.assertEquals(errorState.hasErrors(), Objects.nonNull(expected));
        Assert.assertTrue(errorState.size() <= 1);
        Assert.assertEquals(errorState.get("from"), expected);
    }

    @DataProvider
    public Object[][] sampleOpacity() {
        return new Object[][]{
            {0.0, null},
            {0.5, null},
            {1.0, null},
            {-0.5, errors("The value should be between [0.0, 1.0]")},
            {1.5, errors("The value should be between [0.0, 1.0]")}
        };
    }

    @Test(dataProvider = "sampleOpacity")
    public void testVerifyOpacity(Double opacity, List<String> expected) {
        HeatmapRequest request = new HeatmapRequest();
        request.setOpacity(opacity);
        Rule<HeatmapRequest> rule = ValidationRules.verifyOpacity();
        ErrorState errorState = new ErrorState();

        rule.accept(request, errorState);

        Assert.assertEquals(errorState.hasErrors(), Objects.nonNull(expected));
        Assert.assertTrue(errorState.size() <= 1);
        Assert.assertEquals(errorState.get("opacity"), expected);
    }

    @DataProvider
    public Object[][] sampleRadius() {
        return new Object[][]{
            {8, null},
            {64, null},
            {128, null},
            {7, errors("The value should be between [8, 128]")},
            {129, errors("The value should be between [8, 128]")}
        };
    }

    @Test(dataProvider = "sampleRadius")
    public void testVerifyRadius(Integer radius, List<String> expected) {
        HeatmapRequest request = new HeatmapRequest();
        request.setRadius(radius);
        Rule<HeatmapRequest> rule = ValidationRules.verifyRadius();
        ErrorState errorState = new ErrorState();

        rule.accept(request, errorState);

        Assert.assertEquals(errorState.hasErrors(), Objects.nonNull(expected));
        Assert.assertTrue(errorState.size() <= 1);
        Assert.assertEquals(errorState.get("radius"), expected);
    }

    private static List<String> errors(String... errors) {
        return Arrays.asList(errors);
    }

}
