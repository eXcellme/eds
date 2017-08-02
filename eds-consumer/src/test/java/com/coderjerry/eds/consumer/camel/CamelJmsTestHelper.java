package com.coderjerry.eds.consumer.camel;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.apache.camel.util.FileUtil;

final class CamelJmsTestHelper {

    private final static AtomicInteger counter = new AtomicInteger(0);

    private CamelJmsTestHelper() {
    }

    public static PooledConnectionFactory createPooledConnectionFactory() {
        ConnectionFactory cf = createConnectionFactory(null, null);
        PooledConnectionFactory pooled = new PooledConnectionFactory();
        pooled.setConnectionFactory(cf);
        pooled.setMaxConnections(8);
        return pooled;
    }

    public static ConnectionFactory createConnectionFactory() {
        return createConnectionFactory(null, null);
    }

    private static ConnectionFactory createConnectionFactory(String options, Integer maximumRedeliveries) {
        // using a unique broker name improves testing when running the entire test suite in the same JVM
        int id = counter.incrementAndGet();
        String url = "tcp://192.168.3.103:61618";
//        if (options != null) {
//            url = url + "&" + options;
//        }
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        // optimize AMQ to be as fast as possible so unit testing is quicker
        connectionFactory.setCopyMessageOnSend(false);
        connectionFactory.setOptimizeAcknowledge(true);
        connectionFactory.setOptimizedMessageDispatch(true);
        // When using asyncSend, producers will not be guaranteed to send in the order we
        // have in the tests (which may be confusing for queues) so we need this set to false.
        // Another way of guaranteeing order is to use persistent messages or transactions.
        connectionFactory.setUseAsyncSend(false);
        connectionFactory.setAlwaysSessionAsync(false);
        if (maximumRedeliveries != null) {
            connectionFactory.getRedeliveryPolicy().setMaximumRedeliveries(maximumRedeliveries);
        }
//        connectionFactory.setTrustAllPackages(true);
        return connectionFactory;
    }

    public static ConnectionFactory createPersistentConnectionFactory() {
        return createPersistentConnectionFactory(null);
    }

    private static ConnectionFactory createPersistentConnectionFactory(String options) {
        // using a unique broker name improves testing when running the entire test suite in the same JVM
        int id = counter.incrementAndGet();

        // use an unique data directory in target
        String dir = "target/activemq-data-" + id;

        // remove dir so its empty on startup
        FileUtil.removeDir(new File(dir));

        String url = "vm://test-broker-" + id + "?broker.persistent=true&broker.useJmx=false&broker.dataDirectory=" + dir;
        if (options != null) {
            url = url + "&" + options;
        }
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        // optimize AMQ to be as fast as possible so unit testing is quicker
        connectionFactory.setCopyMessageOnSend(false);
        connectionFactory.setOptimizeAcknowledge(true);
        connectionFactory.setOptimizedMessageDispatch(true);
        connectionFactory.setAlwaysSessionAsync(false);
//        connectionFactory.setTrustAllPackages(true);
        return connectionFactory;
    }
}