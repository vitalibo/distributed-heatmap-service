package com.github.vitalibo.hbase.api.infrastructure;

import com.github.vitalibo.hbase.api.TestHelper;
import com.github.vitalibo.hbase.api.core.facade.HeatmapFacade;
import com.github.vitalibo.hbase.api.core.facade.PingFacade;
import com.github.vitalibo.hbase.api.core.model.HeatmapResponse;
import com.github.vitalibo.hbase.api.core.model.HttpRequest;
import com.github.vitalibo.hbase.api.core.model.HttpResponse;
import com.github.vitalibo.hbase.api.core.model.PingResponse;
import com.github.vitalibo.hbase.api.core.util.ErrorState;
import com.github.vitalibo.hbase.api.core.util.ValidationException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.imageio.ImageIO;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;

@ActiveProfiles(value = {"test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = ControllerTest.MockFactory.class)
public class ControllerTest extends AbstractTestNGSpringContextTests {

    @LocalServerPort
    private int port;

    @Autowired
    private PingFacade mockPingFacade;
    @Autowired
    private HeatmapFacade mockHeatmapFacade;
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
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Header-Trace-RequestId", "6c48a00c-041a-4dd2-90ed-07818ac49d58");

        ResponseEntity<String> actual = restClient.exchange(
            resourceUrl + "/ping?k=v", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        Assert.assertNotNull(actual);
        Assert.assertEquals(actual.getStatusCode(), HttpStatus.CREATED);
        Assert.assertEquals(actual.getBody(), "{\"message\":\"pong\"}");
        Assert.assertEquals(actual.getHeaders().get("foo"), Collections.singletonList("bar"));
        Assert.assertEquals(actual.getHeaders().get("Request-Id"), Collections.singletonList("6c48a00c-041a-4dd2-90ed-07818ac49d58"));
        Mockito.verify(mockPingFacade).process(captorHttpRequest.capture());
        HttpRequest httpRequest = captorHttpRequest.getValue();
        Assert.assertEquals(httpRequest.getPath(), "/v1/ping");
        Assert.assertEquals(httpRequest.getHttpMethod(), "GET");
        Assert.assertEquals(httpRequest.getQueryStringParameters(), Collections.singletonMap("k", "v"));
    }

    @Test(enabled = false)
    public void testHeatmap() throws IOException {
        Mockito.when(mockHeatmapFacade.process(Mockito.any(HttpRequest.class)))
            .thenReturn(new HttpResponse<>(200, new HeatmapResponse(
                ImageIO.read(TestHelper.resourceAsInputStream(TestHelper.resourcePath("heatmap.png"))))));
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Header-Trace-RequestId", "8a0cddd0-d93a-4170-9a7d-444c3b21f7de");

        ResponseEntity<byte[]> actual = restClient.exchange(
            resourceUrl + "/heatmap?radius=32", HttpMethod.GET, new HttpEntity<>(headers), byte[].class);

        Assert.assertNotNull(actual);
        Assert.assertEquals(actual.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(actual.getHeaders().get("Request-Id"),
            Collections.singletonList("8a0cddd0-d93a-4170-9a7d-444c3b21f7de"));
        Assert.assertNotNull(actual.getBody());
        Mockito.verify(mockHeatmapFacade).process(captorHttpRequest.capture());
        HttpRequest httpRequest = captorHttpRequest.getValue();
        Assert.assertEquals(httpRequest.getPath(), "/v1/heatmap");
        Assert.assertEquals(httpRequest.getHttpMethod(), "GET");
        Assert.assertEquals(httpRequest.getQueryStringParameters(), Collections.singletonMap("radius", "32"));
        try (BufferedInputStream fis1 = new BufferedInputStream(new ByteArrayInputStream(actual.getBody()));
             BufferedInputStream fis2 = new BufferedInputStream(
                 TestHelper.resourceAsInputStream(TestHelper.resourcePath("heatmap.png")))) {
            int ch;
            while ((ch = fis1.read()) != -1) {
                Assert.assertEquals(ch, fis2.read());
            }
            Assert.assertEquals(fis2.read(), -1);
        }
    }

    @Test
    public void testInternalServerError() {
        Mockito.when(mockPingFacade.process(Mockito.any(HttpRequest.class))).thenThrow(new RuntimeException("foo"));
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Header-Trace-RequestId", "70437e93-8f9a-4793-9603-394699475da3");

        HttpServerErrorException.InternalServerError actual =
            Assert.expectThrows(HttpServerErrorException.InternalServerError.class, () -> restClient.exchange(
                resourceUrl + "/ping", HttpMethod.GET, new HttpEntity<>(headers), String.class));

        Assert.assertNotNull(actual);
        Assert.assertEquals(actual.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
        Assert.assertEquals(actual.getResponseBodyAsString(),
            TestHelper.resourceAsJson(TestHelper.resourcePath("InternalServerError.json")));
        Assert.assertEquals(actual.getResponseHeaders().get("Request-Id"),
            Collections.singletonList("70437e93-8f9a-4793-9603-394699475da3"));
    }

    @Test
    public void testNotFound() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Header-Trace-RequestId", "e3ea7f39-65a0-4fde-9913-9294489ddc8c");

        HttpClientErrorException.NotFound actual =
            Assert.expectThrows(HttpClientErrorException.NotFound.class, () -> restClient.exchange(
                resourceUrl + "/foo", HttpMethod.GET, new HttpEntity<>(headers), String.class));

        Assert.assertNotNull(actual);
        Assert.assertEquals(actual.getStatusCode(), HttpStatus.NOT_FOUND);
        Assert.assertEquals(actual.getResponseBodyAsString(),
            TestHelper.resourceAsJson(TestHelper.resourcePath("NotFound.json")));
        Assert.assertEquals(actual.getResponseHeaders().get("Request-Id"),
            Collections.singletonList("e3ea7f39-65a0-4fde-9913-9294489ddc8c"));
    }

    @Test
    public void testMethodNotAllowed() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Header-Trace-RequestId", "e3ea7f39-65a0-4fde-9913-9294489ddc8c");

        HttpClientErrorException.NotFound actual =
            Assert.expectThrows(HttpClientErrorException.NotFound.class, () -> restClient.exchange(
                resourceUrl + "/ping", HttpMethod.POST, new HttpEntity<>(headers), String.class));

        Assert.assertNotNull(actual);
        Assert.assertEquals(actual.getStatusCode(), HttpStatus.NOT_FOUND);
        Assert.assertEquals(actual.getResponseBodyAsString(),
            TestHelper.resourceAsJson(TestHelper.resourcePath("NotFound.json")));
        Assert.assertEquals(actual.getResponseHeaders().get("Request-Id"),
            Collections.singletonList("e3ea7f39-65a0-4fde-9913-9294489ddc8c"));
    }

    @Test
    public void testBadRequest() {
        ErrorState errorState = new ErrorState();
        errorState.addError("startedAt", "Should be less or equals now.");
        errorState.addError("foo", "First error message.");
        errorState.addError("foo", "Second error message.");
        Mockito.when(mockPingFacade.process(Mockito.any(HttpRequest.class)))
            .thenThrow(new ValidationException(errorState));
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Header-Trace-RequestId", "4fc42281-b638-43a6-b395-3e3c389c23c5");

        HttpClientErrorException.BadRequest actual =
            Assert.expectThrows(HttpClientErrorException.BadRequest.class, () -> restClient.exchange(
                resourceUrl + "/ping", HttpMethod.GET, new HttpEntity<>(headers), String.class));

        Assert.assertNotNull(actual);
        Assert.assertEquals(actual.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(actual.getResponseBodyAsString(),
            TestHelper.resourceAsJson(TestHelper.resourcePath("BadRequest.json")));
        Assert.assertEquals(actual.getResponseHeaders().get("Request-Id"),
            Collections.singletonList("4fc42281-b638-43a6-b395-3e3c389c23c5"));
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

        @Bean
        public HeatmapFacade createHeatmapFacade() {
            return Mockito.mock(HeatmapFacade.class);
        }

    }

}
