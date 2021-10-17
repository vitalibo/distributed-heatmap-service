package com.github.vitalibo.hbase.api.core.facade;

import com.github.vitalibo.hbase.api.core.model.HttpRequest;
import com.github.vitalibo.hbase.api.core.model.HttpResponse;
import com.github.vitalibo.hbase.api.core.model.PingRequest;
import com.github.vitalibo.hbase.api.core.model.PingResponse;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.function.Function;

public class PingFacadeTest {

    @Mock
    private Function<HttpRequest, PingRequest> mockPingRequestTranslator;
    @Mock
    private PingResponse mockPingResponse;

    private PingFacade spyFacade;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        spyFacade = Mockito.spy(new PingFacade(mockPingRequestTranslator));
    }

    @Test
    public void testHttpProcess() {
        Mockito.doReturn(mockPingResponse).when(spyFacade).process(Mockito.any(PingRequest.class));
        Mockito.when(mockPingRequestTranslator.apply(Mockito.any())).thenReturn(new PingRequest());

        HttpResponse<PingResponse> actual = spyFacade.process(new HttpRequest());

        Assert.assertNotNull(actual);
        Assert.assertEquals(actual.getStatusCode(), 200);
        Assert.assertEquals(actual.getBody(), mockPingResponse);
        Mockito.verify(spyFacade).process(Mockito.any(PingRequest.class));
        Mockito.verify(mockPingRequestTranslator).apply(Mockito.any());
    }

    @Test
    public void testProcess() {
        PingResponse actual = spyFacade.process(new PingRequest());

        Assert.assertNotNull(actual);
        Assert.assertEquals(actual.getMessage(), "pong");
    }

}
