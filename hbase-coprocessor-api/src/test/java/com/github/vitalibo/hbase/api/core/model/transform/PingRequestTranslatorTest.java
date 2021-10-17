package com.github.vitalibo.hbase.api.core.model.transform;

import com.github.vitalibo.hbase.api.core.model.HttpRequest;
import com.github.vitalibo.hbase.api.core.model.PingRequest;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PingRequestTranslatorTest {

    @Test
    public void testFrom() {
        PingRequest actual = PingRequestTranslator.from(new HttpRequest());

        Assert.assertNotNull(actual);
    }

}
