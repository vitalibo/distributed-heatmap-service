package com.github.vitalibo.heatmap.loader.infrastructure;

import com.typesafe.config.Config;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptor;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.CoprocessorDescriptor;
import org.apache.hadoop.hbase.client.TableDescriptorBuilder.ModifyableTableDescriptor;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

public class FactoryTest {

    @Mock
    private Connection mockConnection;
    @Mock
    private Admin mockAdmin;
    @Mock
    private Config mockConfig;
    @Captor
    private ArgumentCaptor<ModifyableTableDescriptor> captorModifyableTableDescriptor;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateTableWhenExist() throws Exception {
        Mockito.when(mockConnection.getAdmin()).thenReturn(mockAdmin);
        Mockito.when(mockAdmin.tableExists(Mockito.any())).thenReturn(true);
        Mockito.when(mockConfig.getString(Mockito.anyString())).thenReturn("foo");

        Factory.createTableIfNotExists(() -> mockConnection, mockConfig);

        Mockito.verify(mockConnection).getAdmin();
        Mockito.verify(mockAdmin).tableExists(TableName.valueOf("foo"));
        Mockito.verify(mockConfig).getString("hbase.heatmap.table-name");
        Mockito.verify(mockAdmin, Mockito.never()).createTable(Mockito.any());
    }

    @Test
    public void testCreateTable() throws Exception {
        Mockito.when(mockConnection.getAdmin()).thenReturn(mockAdmin);
        Mockito.when(mockAdmin.tableExists(Mockito.any())).thenReturn(false);
        Mockito.when(mockConfig.getString(Mockito.any())).thenReturn("foo", "bar.Class");
        Mockito.when(mockConfig.getStringList(Mockito.any())).thenReturn(Arrays.asList("c1", "c2"));

        Factory.createTableIfNotExists(() -> mockConnection, mockConfig);

        Mockito.verify(mockConnection).getAdmin();
        Mockito.verify(mockAdmin).tableExists(TableName.valueOf("foo"));
        Mockito.verify(mockConfig).getString("hbase.heatmap.table-name");
        Mockito.verify(mockAdmin).createTable(captorModifyableTableDescriptor.capture());
        ModifyableTableDescriptor tableDescriptor = captorModifyableTableDescriptor.getValue();
        List<CoprocessorDescriptor> coprocessors = tableDescriptor.getCoprocessorDescriptors();
        Assert.assertEquals(coprocessors.size(), 1);
        Assert.assertEquals(coprocessors.get(0).getClassName(), "bar.Class");
        Mockito.verify(mockConfig).getString("hbase.heatmap.coprocessor");
        ColumnFamilyDescriptor[] columnFamilies = tableDescriptor.getColumnFamilies();
        Assert.assertEquals(columnFamilies.length, 2);
        Assert.assertEquals(columnFamilies[0].getName(), "c1".getBytes());
        Assert.assertEquals(columnFamilies[1].getName(), "c2".getBytes());
    }

}
