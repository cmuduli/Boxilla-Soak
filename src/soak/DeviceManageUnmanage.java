package soak;

import org.apache.log4j.Logger;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import extra.SeleniumActions;
import extra.StartupTestCase;
import extra.StartupTestCase2;
import methods.DevicesMethods;
import methods.DiscoveryMethods;
import objects.Devices;

public class DeviceManageUnmanage extends StartupTestCase2 {
	private String txMac, rxMac, ipCheck, gateway, netmask, txIP, rxIP, newVersion, oldVersion;
	DiscoveryMethods methods = new DiscoveryMethods();
	DevicesMethods deviceMethods = new DevicesMethods();
	final static Logger log = Logger.getLogger(DeviceManageUnmanage.class);

	// Accessing data from excel file
	public DeviceManageUnmanage() {
		txIP = prop.getProperty("txIP");
		rxIP = prop.getProperty("rxIP");
		txMac = prop.getProperty("txMac");
		rxMac = prop.getProperty("rxMac");
		ipCheck = prop.getProperty("ipCheck");
		gateway = prop.getProperty("gateway");
		netmask = prop.getProperty("netmask");
		newVersion = prop.getProperty("newVersion");
		oldVersion = prop.getProperty("oldVersion");
	}

	
//	@BeforeClass(alwaysRun=true)
//	public void beforeClass() throws InterruptedException {
//		cleanUpLogin();
//		try {
//		deviceManageTestPrep();
//		//upgradeToRecentVersion();
//		}catch(Exception | AssertionError e) {
//			e.printStackTrace();
//			cleanUpLogout();
//		}
//		cleanUpLogout();
//	}
//	
//	@AfterClass(alwaysRun=true)
//	public void afterClass() {
//		try {
//			cleanUpLogin();
//			deviceMethods.unManageDevice(driver, txIP);
//			cleanUpLogout();
//		}catch(Exception | AssertionError e) {
//			cleanUpLogout();
//		}
//	}

	@Test
	public void manageUnmanageDevice() throws InterruptedException {
		log.info("Soak Test Case-03 Started - Unamage - Manage Device");
		Thread.sleep(60000);
		methods.discoverDevices(driver);
		methods.stateAndIPcheck(driver, rxMac, ipCheck, rxIP, gateway, netmask);
		methods.manageApplianceAutomatic(driver, "Test_RX", rxMac, ipCheck);
		log.info("Device Managed...Unmanaging");
		deviceMethods.unManageDevice(driver, rxIP);
		log.info("Appliance Managed Successfully - Soak Test Case-03 Completed");
	}
	
	/**
	 * Setup method to discover a device and manage it
	 * @throws InterruptedException
	 */
	//@Test(retryAnalyzer = testNG.RetryAnalyzer.class)
	public void deviceManageTestPrep() throws InterruptedException {
		log.info("Test Preparation - Unamage - Manage Device");
		methods.discoverDevices(driver);
		methods.stateAndIPcheck(driver, txMac, ipCheck, txIP, gateway, netmask);
		methods.manageApplianceAutomatic(driver, "Test_TX", txMac, ipCheck);
		log.info("Appliance Managed Successfully - Test Preparation Completed");
	}

	/**
	 * helper method that runs when a device has been discovered.. It uprades the device
	 * to the latest 
	 * @throws InterruptedException
	 */
	//@Test//(dependsOnMethods = { "deviceManageTestPrep" }, retryAnalyzer = testNG.RetryAnalyzer.class)
	public void upgradeToRecentVersion() throws InterruptedException {
		log.info("Test Preparation - Upgrade to Recent version");
		deviceMethods.navigateToUpgrade(driver);
		deviceMethods.timer(driver);
		SeleniumActions.seleniumSendKeys(driver, Devices.selectDevicesSearchBox, rxIP);
		//Devices.selectDevicesSearchBox(driver).sendKeys(rxIP);
		if (SeleniumActions.seleniumGetText(driver, Devices.upgradeTable).contains(newVersion)) {
		//if (Devices.upgradeTable(driver).getText().contains(newVersion)) {
			log.info("Current firmware version is : " + newVersion);
		} else {
			SeleniumActions.seleniumSendKeysClear(driver, Devices.selectDevicesSearchBox);
			//Devices.selectDevicesSearchBox(driver).clear();
			deviceMethods.upgradeDevice(driver, "rx", rxIP, newVersion, oldVersion);
		}
		log.info("Upgraded to Recent Version - Test Preparation Complete");
	}
	
	
}
