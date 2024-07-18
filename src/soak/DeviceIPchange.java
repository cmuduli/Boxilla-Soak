package soak;

import java.io.IOException;

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

public class DeviceIPchange extends StartupTestCase2 {

	public String txIP, rxIP, extraIP, txMac, rxMac, ipCheck, gateway, netmask, newVersion, oldVersion;
	DevicesMethods methods = new DevicesMethods();
	DiscoveryMethods discoverymethods = new DiscoveryMethods();
	final static Logger log = Logger.getLogger(DeviceIPchange.class);

	public DeviceIPchange() throws IOException {
		txIP = prop.getProperty("txIP");
		rxIP = prop.getProperty("rxIP");
		extraIP = prop.getProperty("extraIP1");
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
	
	public void loadProp() {
		txIP = prop.getProperty("txIP");
		rxIP = prop.getProperty("rxIP");
		extraIP = prop.getProperty("extraIP1");
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
	
	@BeforeClass(alwaysRun=true)
	public void beforeClass() throws InterruptedException {
		loadProp();
		try {
			cleanUpLogin();
		manageDevice();
		//upgradeToRecentVersion();
		}catch(Exception | AssertionError e) {
			e.printStackTrace();
			cleanUpLogout();
		}
		cleanUpLogout();
	}

	@AfterClass(alwaysRun=true)
	public void afterClass() throws InterruptedException {
		try {
			cleanUpLogin();
			methods.unManageDevice(driver, rxIP);
			cleanUpLogout();
		}catch(Exception | AssertionError e) {
			methods.unManageDevice(driver, extraIP);
			cleanUpLogout();
		}
	}

	@Test//(dependsOnMethods = { "upgradeToRecentVersion" })
	public void ipChange() throws InterruptedException { // IP change
		log.info("Soak Test Case-02 Started - Change Device IP");
		// Change IP address to New IP and revert it back to original IP address
		methods.deviceIPchangeSoak(driver, "Test_RX", rxIP, extraIP);
		log.info("Change device IP- Soak Test Case-02 Completed");
	}
	
	/**
	 * Setup method to discover a device and manage it
	 * @throws InterruptedException
	 */
	//@Test(retryAnalyzer = testNG.RetryAnalyzer.class)
	public void manageDevice() throws InterruptedException {
		log.info("Test Preparation - Unamage - Manage Device");
		discoverymethods.discoverDevices(driver);
		discoverymethods.stateAndIPcheck(driver, rxMac, ipCheck, rxIP, gateway, netmask);
		discoverymethods.manageApplianceAutomatic(driver, "Test_RX", rxMac, ipCheck);
		log.info("Appliance Managed Successfully - Test Preparation Completed");
	}

	/**
	 * helper method that runs when a device has been discovered.. It uprades the device
	 * to the latest 
	 * @throws InterruptedException
	 */
	//@Test(dependsOnMethods = { "manageDevice" }, retryAnalyzer = testNG.RetryAnalyzer.class)
	public void upgradeToRecentVersion() throws InterruptedException {
		log.info("Test Preparation - Upgrade to Recent version");
		methods.navigateToUpgrade(driver);
		methods.timer(driver);
		SeleniumActions.seleniumSendKeys(driver, Devices.selectDevicesSearchBox, rxIP);
		//Devices.selectDevicesSearchBox(driver).sendKeys(rxIP);
		if (SeleniumActions.seleniumGetText(driver, Devices.upgradeTable).contains(newVersion)) {
		//if (Devices.upgradeTable(driver).getText().contains(newVersion)) {
			log.info("Current firmware version is : " + newVersion);
		} else {
			SeleniumActions.seleniumSendKeysClear(driver, Devices.selectDevicesSearchBox);
			//Devices.selectDevicesSearchBox(driver).clear();
			methods.upgradeDevice(driver, "rx", rxIP, newVersion, oldVersion);
		}
		log.info("Upgraded to Recent Version - Test Preparation Complete");
	}
}
