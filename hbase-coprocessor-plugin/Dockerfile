FROM vitalibo/hbase:2.4.4
MAINTAINER Vitaliy Boyarsky <boyarsky.vitaliy@live.com>

ADD target/hbase-coprocessor-plugin-1.0-SNAPSHOT.jar /opt/hbase/lib/

RUN ln -s /opt/hbase/conf /etc/hbase \
    && ln -s /opt/hbase /usr/local/hbase
