package com.github.vitalibo.heatmap.api.infrastructure;

import com.github.vitalibo.heatmap.api.core.facade.HeatmapFacade;
import com.github.vitalibo.heatmap.api.core.facade.HeatmapJsonFacade;
import com.github.vitalibo.heatmap.api.core.facade.PingFacade;
import com.github.vitalibo.heatmap.api.core.math.HeatmapRenderer;
import com.github.vitalibo.heatmap.api.infrastructure.hbase.HBaseRepository;
import com.github.vitalibo.heatmap.api.infrastructure.springframework.HttpRequestMappingHandlerAdapter;
import com.github.vitalibo.heatmap.api.infrastructure.springframework.RequestTracingFilter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Factory {

    @Bean
    public PingFacade createPingFacade() {
        return new PingFacade();
    }

    @Bean
    public HeatmapFacade createHeatmapFacade(@Value("${hbase.zookeeper.quorum}") String hbaseZookeeperQuorum,
                                             @Value("${hbase.heatmap.table-name}") String hbaseHeatmapTable) {
        return new HeatmapFacade(
            createHBaseRepository(
                hbaseZookeeperQuorum, hbaseHeatmapTable),
            new HeatmapRenderer());
    }

    @Bean
    public HeatmapJsonFacade createHeatmapJsonFacade(@Value("${hbase.zookeeper.quorum}") String hbaseZookeeperQuorum,
                                                     @Value("${hbase.heatmap.table-name}") String hbaseHeatmapTable,
                                                     @Value("${heatmap.sparse.threshold}") double sparseHeatmapThreshold) {
        return new HeatmapJsonFacade(
            createHBaseRepository(
                hbaseZookeeperQuorum, hbaseHeatmapTable),
            sparseHeatmapThreshold);
    }

    @Bean
    public WebMvcRegistrations createWebMvcRegistrations() {
        return new WebMvcRegistrations() {
            @Override
            public RequestMappingHandlerAdapter getRequestMappingHandlerAdapter() {
                return new HttpRequestMappingHandlerAdapter();
            }
        };
    }

    @Bean
    public FilterRegistrationBean<GenericFilterBean> registerRequestTracingFilter(@Value("${logging.tracing.header}")
                                                                                      String incomingHeader) {
        FilterRegistrationBean<GenericFilterBean> bean = new FilterRegistrationBean<>();
        bean.setFilter(new RequestTracingFilter(incomingHeader));
        bean.setOrder(1);
        return bean;
    }

    @SneakyThrows
    private static HBaseRepository createHBaseRepository(String zookeeperQuorum, String tableName) {
        final org.apache.hadoop.conf.Configuration configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.quorum", zookeeperQuorum);
        Connection connection = ConnectionFactory.createConnection(configuration);
        Table table = connection.getTable(TableName.valueOf(tableName));
        return new HBaseRepository(table);
    }

}
