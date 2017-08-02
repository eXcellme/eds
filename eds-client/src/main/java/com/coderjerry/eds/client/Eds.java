package com.coderjerry.eds.client;

import com.coderjerry.eds.core.logger.Logger;
import com.coderjerry.eds.core.logger.LoggerFactory;
import java.util.Properties;

/**
 * eds client
 */
public class Eds {

	private static final Logger LOG = LoggerFactory.getLogger(Eds.class);
	
	private Eds(){
		// add shutdown hook in case of not calling stop() explicitly
		Runtime.getRuntime().addShutdownHook(new Thread(){
			@Override
			public void run() {
				LOG.debug("eds client shutdown hook calling stop()");
				Eds.stop();
			}
		});
	}
	
	private static final Eds instance = new Eds();

	private static volatile boolean initialized = false; // wether eds client already done initializing

	private static volatile boolean stopCalled = false; // wether any thread called stop()

	private static final PublishManager publishManager = new PublishManager();
	
	public static void init(){
		checkAndinit();
	}

	public static void initWithConfiguration(Properties props){
		ClientConfig.fromProperties(props);
		checkAndinit();
	}

	public static void stop(){
		checkAndStop();
	}
	private static void checkAndStop() {
		if(!initialized || stopCalled){
			return ;
		}

		synchronized (instance){
			if(!stopCalled){
				stopCalled = true;
				try{
					LOG.debug("eds client call destroy ");
					publishManager.destroy();
					LOG.debug("eds client destroyed successfully ");
				}catch(Exception e){
					LOG.error("error hapends when stop eds client ",e);
				}
			}
		}
	}
	
	private static void checkAndinit(){
		if(initialized){
			return ;
		}
		synchronized (instance){
			if(!initialized){
				try{
					LOG.debug("eds client call initialize()");
					publishManager.initialize();
					LOG.debug("eds client initialzed successfully");
					initialized = true;
				}catch(Exception e){
					initialized = false;
					LOG.error("eds error hapends when init eds client",e);
				}
			}
		}
	}
	
	public static Eds getInstance(){
	    return instance;
	}
	
	/**  
	* publish event using default publisher
	* @param name queue name
	* @param data data to publish
	*/
	public static void publish(String name, Object data){
		checkAndinit(); 
		publishManager.pub(name,data);
	}

	/**
	 * publish event using specific publisher
	 * @param name queue name
	 * @param data data to publish
	 * @param publisherId id of publisher
	 */
	public static void publish(String name, Object data, String publisherId){
		checkAndinit();
		publishManager.pub(name, data, publisherId);
	}
}