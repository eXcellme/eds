package com.coderjerry.eds.client.publisher;

import com.coderjerry.eds.client.AbstractEventPublisher;
import com.coderjerry.eds.core.Event;
import com.coderjerry.eds.core.EventConverter;
import com.coderjerry.eds.core.EventPublisher;
import com.coderjerry.eds.core.LifeCycle;
import com.coderjerry.eds.core.exception.EdsException;
import com.coderjerry.eds.core.logger.Logger;
import com.coderjerry.eds.core.logger.LoggerFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageProducer;
import javax.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;

/**
 * activemq publisher
 *
 * @author baofan.li
 */
public class ActiveMQEventPublisher extends AbstractEventPublisher implements EventPublisher, LifeCycle{

  private static final Logger LOG = LoggerFactory.getLogger(ActiveMQEventPublisher.class);


  private final Config config = new Config();


  class Config extends BaseConfig{
    // activemq option
    String userName,
        password,
        brokerUrl;
    int maximumActiveSessionPerConnection = 10,
        maxConnections = 10,
        idleTimeout = 300000;
    boolean useAnonymousProducers = true,
        reconnectOnException = true;
    int connectResponseTimeout = 30000 ; // 30s
  }

  private Map<String, Destination> destinationCache = new ConcurrentHashMap<>();

  private PooledConnectionFactory connectionFactory;

  protected void doInitialize() {
      LOG.debug(Thread.currentThread() + " initialize activemq publisher...");
      initConfig();
      initActiveMQ();
  }

  private void initConfig() {
    setProperties(config, id());
  }

  private void initActiveMQ() {
    LOG.debug(
        "initialize ConnectionFactory with [" + config.userName + "," + config.password + "," + config.brokerUrl + "]");
    ActiveMQConnectionFactory activemqConnectionFactory = new ActiveMQConnectionFactory(config.userName,
        config.password, config.brokerUrl);
    if (connectionFactory == null) {
      connectionFactory = new PooledConnectionFactory(activemqConnectionFactory);
    }
    activemqConnectionFactory
        .setConnectResponseTimeout(config.connectResponseTimeout); // 超时时间，针对activemq必须添加
    connectionFactory.setMaximumActiveSessionPerConnection(config.maximumActiveSessionPerConnection); // 10
    connectionFactory.setMaxConnections(config.maxConnections); // 10
    connectionFactory.setIdleTimeout(config.idleTimeout); // 管理连接池中的连接，5分钟超时   300000
    connectionFactory.setUseAnonymousProducers(config.useAnonymousProducers);
    connectionFactory.setReconnectOnException(config.reconnectOnException);
    connectionFactory.start();
  }

  protected void doPublish(Event event) throws EdsException {
    Connection conn = null;
    Session session = null;
    MessageProducer messageProducer = null;
    try {
      LOG.debug("eds pub 3 mq in -[event:" + event + "]");
      conn = connectionFactory.createConnection();
      // 设置非事务，客户端确认方式
      session = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE);

      MapMessage mapMessage = session.createMapMessage();
      mapMessage = EventConverter.convertToMessage(mapMessage, event);
      Destination dest = getDestination(event.getName(), session);
      messageProducer = session.createProducer(dest);
      messageProducer.send(mapMessage);
      // commit session if necessary
      if (session.getTransacted()) {
        session.commit();
      }
      LOG.debug("eds pub 4 mq ok -[conn:" + conn + ",session:" + session + ",event:" + event + "]");
    }catch(JMSException e){
      throw new EdsException("eds client activemq doPublish exception ", e);
    }finally {
      releaseSession(session);
      releaseMessageProducer(messageProducer);
      releaseConnection(conn, false);
    }
  }

  @Override
  protected void doDestroy() {
    if (connectionFactory != null) {
      LOG.debug("close connectionFactory ... current nums of connections : " + connectionFactory
          .getNumConnections());
      connectionFactory.stop();
      LOG.debug("current nums of connections : " + connectionFactory.getNumConnections());
    }
  }

  /**
   * get cached destination
   */
  private Destination getDestination(String name, Session session) {
    Destination dest = destinationCache.get(name);
    if (dest != null) {
      return dest;
    }
    try {
      dest = session.createQueue(DEST_PREFIX + name);
    } catch (JMSException e) {
      LOG.error("exception occurs when session.createQueue", e);
    }
    destinationCache.put(name, dest);
    return dest;

  }

  private static void releaseConnection(Connection con, boolean started) {
    if (con == null) {
      return;
    }
    if (started) {
      try {
        con.stop();
      } catch (Throwable ex) {
        LOG.warn("Could not stop JMS Connection before closing it", ex);
      }
    }
    try {
      con.close();
    } catch (Throwable ex) {
      LOG.warn("Could not close JMS Connection", ex);
    }
  }

  private static void releaseSession(Session session) {
    if (session == null) {
      return;
    }
    try {
      session.close();
    } catch (Throwable ex) {
      LOG.warn("Could not close JMS Session", ex);
    }
  }

  private static void releaseMessageProducer(MessageProducer producer) {
    if (producer == null) {
      return;
    }
    try {
      producer.close();
    } catch (Throwable ex) {
      LOG.warn("Could not close JMS MessageProducer", ex);
    }
  }

  @Override
  public String id() {
    return "activemq";
  }

}