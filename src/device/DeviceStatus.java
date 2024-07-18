package device;


import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import extra.StartupTestCase;
import extra.StartupTestCase2;
import methods.DevicesMethods;
import methods.DiscoveryMethods;
/**
 * Class that contains tests for device status
 * @author Brendan O Regan
 *
 */
public class DeviceStatus extends StartupTestCase {
	private DevicesMethods methods = new DevicesMethods();
	private DiscoveryMethods discoverymethods = new DiscoveryMethods();
	final static Logger log = Logger.getLogger(DeviceStatus.class);

	

	/**
	 * Retrieves managed device details through Boxilla
	 * @throws InterruptedException
	 */
	@Test(groups= {"boxillaFunctional", "smoke" ,"emerald", "chrome", "quick"},retryAnalyzer = testNG.RetryAnalyzer.class)
	public void test01_getManagedDeviceDetails() throws InterruptedException { // Retrieve Managed Device Details
		getDevices();
		log.info("Test Case-66 Started - Retrieve Managed Device Details");
		methods.retrieveDetails(driver, txIp, txSingle.getMac(), txSingle.getDeviceName());
		log.info("Device Details Retrieved - Test Case-66 is Completed");
	}

	/**
	 * Ping managed device through Boxilla
	 * @throws InterruptedException
	 */
	
	@Test(groups= {"boxillaFunctional", "smoke" ,"emerald"},retryAnalyzer = testNG.RetryAnalyzer.class)
	public void test02_pingManagedDevice() throws InterruptedException { // Ping managed device
		getDevices();
		log.info("Test Case-67 Started - Ping Managed Device");
		methods.pingDevice(driver, txIp);
		log.info("Managed device ping - Test Case-67 is Completed");
	}

	/**
	 * Reboots device through Boxilla
	 * @throws InterruptedException
	 */
	@Test(groups= {"boxillaFunctional", "smoke" ,"emerald"},retryAnalyzer = testNG.RetryAnalyzer.class)
	public void test03_rebootDevice() throws InterruptedException { // Reboot device
		getDevices();
		log.info("Test Case-68 Started - Reboot device");
		methods.retrieveDetails(driver, txIp, txSingle.getMac(), txSingle.getDeviceName());
		float oldUptime = methods.uptime(driver);
		methods.rebootDevice(driver, txIp);
		methods.retrieveDetails(driver, txIp, txSingle.getMac(), txSingle.getDeviceName());
		float newUptime = methods.uptime(driver);
		Assert.assertTrue(newUptime < oldUptime, "***** New Uptime is higher than Old Uptime *****");
		log.info("Reboot Device - Test Case-68 is Completed");
	}

	/**
	 * Edits device thorugh Boxilla
	 * @throws InterruptedException
	 */
	@Test(groups= {"boxillaFunctional", "smoke" ,"emerald"},retryAnalyzer = testNG.RetryAnalyzer.class)
	public void test04_editDeviceIp() throws InterruptedException { // Edit Device
		getDevices();
		log.info("Test Case-69 Started - Edit Device");
		String oldIp = txIp;
		// Change IP address to New IP and revert it back to original IP address
		methods.editDevice(driver, txIp, prop.getProperty("extraIP1"), true);
	//	methods.editDevice(driver, txIp, oldIp);
		log.info("Edit device - Test Case-69 is Completed");
	}

	/**
	 * Changes device name through Boxilla
	 * @throws InterruptedException
	 */
	@Test(groups= {"boxillaFunctional", "smoke" ,"emerald"},retryAnalyzer = testNG.RetryAnalyzer.class)
	public void test05_changeDeviceName() throws InterruptedException { // Change Device name
		getDevices();
		log.info("Test Case-70 Started - Change Device name");
		// Using IP address to search for device

		methods.changeDeviceName(driver, txIp, "editedhostname");
		log.info("device name changed. CHanging back to original");
		String deviceOriginalName = "";
		//just awful testing
		if(StartupTestCase.isEmerald) {
			deviceOriginalName = txEmerald.getDeviceName();
		}else {
			deviceOriginalName = txSingle.getDeviceName();
		}
		methods.changeDeviceName(driver, txIp,deviceOriginalName);

		log.info("Edit device - Test Case-70 is Completed");
	}

	
//	@Test
//	public void test00() throws InterruptedException {
//		methods.setMulticastPortandIp(driver, "10.211.129.86", "");
//	}

	
//	@Test(retryAnalyzer = com.testNG.RetryAnalyzer.class)
//	public void testcase72() throws InterruptedException { // Unmanage device
//		log.info("Test Case-72 Started - Unmanage Device");
//		
//		//manage device again
//		discoverymethods.discoverDevices(driver);
//		discoverymethods.stateAndIPcheck(driver, txMac, ipCheck, txIP, gateway, netmask);
//		discoverymethods.manageApplianceAutomatic(driver, "Test_TX", txMac, ipCheck);
//		methods.unManageDevice(driver, "DTX1000-T"); // Using model name for search
//		log.info("Device Unmanaged - Test Case-72 is Completed");
//	}

	  

//	
////	@Test
////	public void test00() throws InterruptedException {
////		methods.setMulticastPortandIp(driver, "10.211.129.86", "");
////	}
//
//	
////	@Test(retryAnalyzer = testNG.RetryAnalyzer.class)
////	public void testcase72() throws InterruptedException { // Unmanage device
////		log.info("Test Case-72 Started - Unmanage Device");
////		
////		//manage device again
////		discoverymethods.discoverDevices(driver);
////		discoverymethods.stateAndIPcheck(driver, txMac, ipCheck, txIP, gateway, netmask);
////		discoverymethods.manageApplianceAutomatic(driver, "Test_TX", txMac, ipCheck);
////		methods.unManageDevice(driver, "DTX1000-T"); // Using model name for search
////		log.info("Device Unmanaged - Test Case-72 is Completed");
////	}
//
//	  


}

