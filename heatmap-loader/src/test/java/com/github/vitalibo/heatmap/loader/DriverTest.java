package com.github.vitalibo.heatmap.loader;

import com.github.vitalibo.heatmap.loader.core.Job;
import com.github.vitalibo.heatmap.loader.core.Spark;
import com.github.vitalibo.heatmap.loader.infrastructure.Factory;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DriverTest {

    @Mock
    private Factory mockFactory;
    @Mock
    private Job mockJob;
    @Mock
    private Spark mockSpark;

    private Driver driver;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        driver = new Driver(mockFactory);
        Mockito.when(mockFactory.createGenerateJob(Mockito.any())).thenReturn(mockJob);
        Mockito.when(mockFactory.createWordCountJob(Mockito.any())).thenReturn(mockJob);
        Mockito.when(mockFactory.createSpark()).thenReturn(mockSpark);
    }

    @Test
    public void testRunWordCount() {
        driver.run(new String[]{"word_count"});

        Mockito.verify(mockFactory).createWordCountJob(new String[]{"word_count"});
        Mockito.verify(mockFactory, Mockito.never()).createGenerateJob(Mockito.any());
        Mockito.verify(mockFactory).createSpark();
        Mockito.verify(mockSpark).submit(mockJob);
    }

    @Test
    public void testRunGenerate() {
        driver.run(new String[]{"generate"});

        Mockito.verify(mockFactory).createGenerateJob(new String[]{"generate"});
        Mockito.verify(mockFactory, Mockito.never()).createWordCountJob(Mockito.any());
        Mockito.verify(mockFactory).createSpark();
        Mockito.verify(mockSpark).submit(mockJob);
    }

    @Test
    public void testUnknownJob() {
        IllegalArgumentException ex = Assert.expectThrows(
            IllegalArgumentException.class, () -> driver.run(new String[]{"foo"}));

        Assert.assertEquals(ex.getMessage(), "Unknown job name");
        Mockito.verify(mockFactory, Mockito.never()).createGenerateJob(Mockito.any());
        Mockito.verify(mockFactory, Mockito.never()).createWordCountJob(Mockito.any());
        Mockito.verify(mockFactory, Mockito.never()).createSpark();
        Mockito.verify(mockSpark, Mockito.never()).submit(mockJob);
    }

}
