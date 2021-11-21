package com.github.vitalibo.heatmap.api.infrastructure;

import com.github.vitalibo.heatmap.api.core.facade.HeatmapFacade;
import com.github.vitalibo.heatmap.api.core.facade.HeatmapJsonFacade;
import com.github.vitalibo.heatmap.api.core.facade.PingFacade;
import com.github.vitalibo.heatmap.api.infrastructure.springframework.HttpRequestMappingHandlerAdapter;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.MiniHBaseCluster;
import org.apache.hadoop.hbase.net.Address;
import org.apache.hadoop.hbase.zookeeper.MiniZooKeeperCluster;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

public class FactoryTest {

    private MiniZooKeeperCluster zookeeperCluster;
    private MiniHBaseCluster hbaseCluster;
    private Factory factory;

    @BeforeClass
    public void startHBaseCluster() throws Exception {
        final HBaseTestingUtility utility = new HBaseTestingUtility();
        zookeeperCluster = utility.startMiniZKCluster();
        hbaseCluster = utility.startMiniHBaseCluster();
        hbaseCluster.waitForActiveAndReadyMaster(10_000);
    }

    @BeforeMethod
    public void setUp() {
        factory = new Factory();
    }

    @Test
    public void testCreatePingFacade() {
        PingFacade actual = factory.createPingFacade();

        Assert.assertNotNull(actual);
    }

    @Test
    public void testCreateHeatmapFacade() {
        Address address = zookeeperCluster.getAddress();
        HeatmapFacade actual = factory.createHeatmapFacade(
            address.getHostname() + ":" + address.getPort(), "foo");

        Assert.assertNotNull(actual);
    }

    @Test
    public void testCreateHeatmapJsonFacade() {
        Address address = zookeeperCluster.getAddress();
        HeatmapJsonFacade actual = factory.createHeatmapJsonFacade(
            address.getHostname() + ":" + address.getPort(), "foo", 0.5);

        Assert.assertNotNull(actual);
    }

    @Test
    public void testCreateWebMvcRegistrations() {
        WebMvcRegistrations actual = factory.createWebMvcRegistrations();

        Assert.assertNotNull(actual);
        Assert.assertTrue(actual.getRequestMappingHandlerAdapter() instanceof HttpRequestMappingHandlerAdapter);
    }

    @AfterClass
    public void shutdownHBaseCluster() throws IOException {
        hbaseCluster.shutdown();
        zookeeperCluster.shutdown();
    }

}
