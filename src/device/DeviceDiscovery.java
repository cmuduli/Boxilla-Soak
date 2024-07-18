package device;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;
import extra.StartupTestCase;
import extra.StartupTestCase2;
import junit.framework.Assert;
import methods.DevicesMethods;
import methods.DiscoveryMethods;

/**
 * Class that contains tests for device discovery
 * @author Brendan O Regan
 *
 */

public class DeviceDiscovery extends StartupTestCase{
	//private String txMac, rxMac, ipCheck, gateway, netmask, txIP, rxIP;
	private DiscoveryMethods methods = new DiscoveryMethods();
	DevicesMethods devMethods = new DevicesMethods();
	final static Logger log = Logger.getLogger(DeviceDiscovery.class);

	
	// Manage TX device automatically
	/**
	 * Manage TX device automatically
	 * @throws InterruptedException
	 */
	//@Test(groups= {"boxillaFunctional"}, retryAnalyzer = testNG.RetryAnalyzer.class)
	public void test01_addApplianceAutomaticDiscovery() throws InterruptedException {
		log.info("Unmanage device first");
		devMethods.unManageDevice(driver, rxIpDual);
		log.info("Test Case-61 Started - Adding Appliance using Automatic Discovery");
		methods.discoverDevices(driver);
		methods.stateAndIPcheck(driver, rxDual.getMac(), prop.getProperty("ipCheck"),
				rxIpDual, rxDual.getGateway(),rxDual.getNetmask());
		methods.manageApplianceAutomatic(driver, rxDual.getDeviceName(), rxDual.getMac(),
				prop.getProperty("ipCheck"));
		log.info("Appliance Added using Automatic Discovery - Test Case-61 is Completed");
	}

	/**
	 * Manage device manually
	 * @throws InterruptedException
	 */
	@Test(groups= {"boxillaFunctional"},retryAnalyzer = testNG.RetryAnalyzer.class)
	public void test02_manageApplianceManually() throws InterruptedException { // Manage Appliance manually
		log.info("Unmanage device first");
		devMethods.unManageDevice(driver, rxIpDual);
		log.info("Test Case-62 Started - Adding Appliance Manually");
		methods.discoverDevices(driver);
		methods.stateAndIPcheck(driver, rxDual.getMac(), prop.getProperty("ipCheck"),
				rxIpDual, rxDual.getGateway(),rxDual.getNetmask());
		methods.addDeviceManually(driver, rxDual.getMac(), rxDual.getDeviceName(), rxIpDual);
		log.info("Appliance Added manually - Test Case-62 is Completed");
	}

	
}
