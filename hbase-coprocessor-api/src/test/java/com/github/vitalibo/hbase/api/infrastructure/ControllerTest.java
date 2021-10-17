package com.github.vitalibo.hbase.api.infrastructure;

import com.github.vitalibo.hbase.api.core.facade.PingFacade;
import com.github.vitalibo.hbase.api.core.model.HttpRequest;
import com.github.vitalibo.hbase.api.core.model.HttpResponse;
import com.github.vitalibo.hbase.api.core.model.PingResponse;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.web.client.RestTemplate;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;

@ActiveProfiles(value = {"test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = ControllerTest.MockFactory.class)
public class ControllerTest extends AbstractTestNGSpringContextTests {

    @LocalServerPort
    private int port;

    @Autowired
    private PingFacade mockPingFacade;
    @Captor
    private ArgumentCaptor<HttpRequest> captorHttpRequest;

    private RestTemplate restClient;
    private String resourceUrl;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        restClient = new RestTemplate();
        resourceUrl = String.format("http://localhost:%s/v1", port);
    }

    @Test
    public void testPing() {
        Mockito.when(mockPingFacade.process(Mockito.any(HttpRequest.class)))
            .thenReturn(new HttpResponse<>(201, Collections.singletonMap("foo", "bar"), new PingResponse("pong")));

        ResponseEntity<String> actual = restClient.getForEntity(resourceUrl + "/ping?k=v", String.class);

        Assert.assertNotNull(actual);
        Assert.assertEquals(actual.getStatusCode(), HttpStatus.CREATED);
        Assert.assertEquals(actual.getBody(), "{\"message\":\"pong\"}");
        Assert.assertEquals(actual.getHeaders().get("foo"), Collections.singletonList("bar"));
        Mockito.verify(mockPingFacade).process(captorHttpRequest.capture());
        HttpRequest httpRequest = captorHttpRequest.getValue();
        Assert.assertEquals(httpRequest.getPath(), "/v1/ping");
        Assert.assertEquals(httpRequest.getHttpMethod(), "GET");
        Assert.assertEquals(httpRequest.getQueryStringParameters(), Collections.singletonMap("k", "v"));
    }

    @AfterMethod
    public void cleanUp() {
        Mockito.reset(mockPingFacade);
    }

    @TestConfiguration
    public static class MockFactory {

        @Bean
        public PingFacade createPingFacade() {
            return Mockito.mock(PingFacade.class);
        }

    }

}
