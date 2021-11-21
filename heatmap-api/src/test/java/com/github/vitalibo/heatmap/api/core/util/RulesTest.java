package com.github.vitalibo.heatmap.api.core.util;

import com.github.vitalibo.heatmap.api.core.model.HttpRequest;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.function.Supplier;

public class RulesTest {

    @Mock
    private Rule<HttpRequest> mockHttpRule;
    @Mock
    private Rule<String> mockRule;

    private Rules<String> rules;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        rules = new Rules<>(Collections.singletonList(mockHttpRule), Collections.singletonList(mockRule));
    }

    @Test
    public void testVerifyHttpRequest() {
        HttpRequest httpRequest = new HttpRequest();
        httpRequest.setHttpMethod("GET");

        rules.verify(httpRequest);

        Mockito.verify(mockHttpRule).accept(Mockito.eq(httpRequest), Mockito.any(ErrorState.class));
        Mockito.verify(mockRule, Mockito.never()).accept(Mockito.any(), Mockito.any());
    }

    @Test
    public void testVerifyHttpRequestThrowException() {
        HttpRequest httpRequest = new HttpRequest();
        httpRequest.setHttpMethod("GET");
        Mockito.doAnswer(answer -> {
            ErrorState errorState = answer.getArgument(1, ErrorState.class);
            errorState.addError("foo", "bar");
            return null;
        }).when(mockHttpRule).accept(Mockito.any(), Mockito.any());

        ValidationException actual = Assert.expectThrows(ValidationException.class, () -> rules.verify(httpRequest));

        ErrorState errorState = actual.getErrorState();
        Assert.assertNotNull(errorState);
        Assert.assertTrue(errorState.hasErrors());
        Assert.assertEquals(errorState.get("foo"), Collections.singletonList("bar"));
        Mockito.verify(mockHttpRule).accept(Mockito.eq(httpRequest), Mockito.any(ErrorState.class));
        Mockito.verify(mockRule, Mockito.never()).accept(Mockito.any(), Mockito.any());
    }

    @Test
    public void testTestVerify() {
        rules.verify("foo");

        Mockito.verify(mockHttpRule, Mockito.never()).accept(Mockito.any(), Mockito.any());
        Mockito.verify(mockRule).accept(Mockito.eq("foo"), Mockito.any(ErrorState.class));
    }

    @Test
    public void testTestVerifyThrowException() {
        Mockito.doAnswer(answer -> {
            ErrorState errorState = answer.getArgument(1, ErrorState.class);
            errorState.addError("foo", "bar");
            return null;
        }).when(mockRule).accept(Mockito.any(), Mockito.any());

        ValidationException actual = Assert.expectThrows(ValidationException.class, () -> rules.verify("foo"));

        ErrorState errorState = actual.getErrorState();
        Assert.assertNotNull(errorState);
        Assert.assertTrue(errorState.hasErrors());
        Assert.assertEquals(errorState.get("foo"), Collections.singletonList("bar"));
        Mockito.verify(mockHttpRule, Mockito.never()).accept(Mockito.any(), Mockito.any());
        Mockito.verify(mockRule).accept(Mockito.eq("foo"), Mockito.any(ErrorState.class));
    }

    @Test
    public void testLazy() {
        Supplier<Rule<HttpRequest>> lazyHttpRule = () -> mockHttpRule;
        Supplier<Rule<String>> lazyRule = () -> mockRule;

        Rules<String> actual = Rules.lazy(
            Collections.singletonList(lazyHttpRule), Collections.singletonList(lazyRule));

        actual.verify("foo");

        Mockito.verify(mockHttpRule, Mockito.never()).accept(Mockito.any(), Mockito.any());
        Mockito.verify(mockRule).accept(Mockito.eq("foo"), Mockito.any(ErrorState.class));
    }

}
