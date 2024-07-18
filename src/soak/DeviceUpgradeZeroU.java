package soak;

import org.apache.log4j.Logger;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import extra.StartupTestCase;
import extra.StartupTestCase2;
import methods.DevicesMethods;
import methods.DiscoveryMethods;
import methods.SystemMethods;

public class DeviceUpgradeZeroU extends StartupTestCase {
	private String oldVersion, newVersion, txIP, rxIP, txMac, rxMac, ipCheck, gateway, netmask;
	private DevicesMethods methods = new DevicesMethods();
	private DiscoveryMethods discoveryMethods = new DiscoveryMethods();
	private SystemMethods sysMethods = new SystemMethods();
	final static Logger log = Logger.getLogger(DeviceUpgradeZeroU.class);
	

	public DeviceUpgradeZeroU() {
		// Accessing test data from excel file
		txIP = prop.getProperty("txIP");
		rxIP = prop.getProperty("rxIP");
		if(!StartupTestCase.isEmerald) {
			newVersion = prop.getProperty("newVersion");
			oldVersion = prop.getProperty("oldVersion");
		}else {
			newVersion = prop.getProperty("emeraldNewVersion");
			oldVersion = prop.getProperty("emeraldOldVersion");
		}
		txMac = prop.getProperty("txMac");
		rxMac =  prop.getProperty("rxMac");
		ipCheck = prop.getProperty("ipCheck");
		gateway = prop.getProperty("gateway");
		netmask = prop.getProperty("netmask");
	}

	@BeforeClass(alwaysRun=true)
	public void beforeClass() throws InterruptedException {
		Thread.sleep(60000);
		cleanUpLogin();
		try {
		manageDevice();
		//uploadAndActivateVersion();
		}catch(Exception | AssertionError e) {
			e.printStackTrace();
			cleanUpLogout();
		}
		cleanUpLogout();
	}
	

	@AfterClass(alwaysRun=true)
	public void afterClass() {
		try {
			cleanUpLogin();
			methods.unManageDevice(driver, txIP);
			cleanUpLogout();
		}catch(Exception | AssertionError e) {
			cleanUpLogout();
		}
	}
	
	public void uploadAndActivateVersion() throws InterruptedException { // Upload Two version to switch between
		// Upload two version Old and New, activate new version and upgrade device
		log.info("Test Preparation - Upload version on managed rx device");
		methods.navigateToUpgrade(driver); // Navigate to Upgrade page
		methods.uploadVersion(driver, "rx", newVersion); // Upload V4.2
		methods.uploadVersion(driver, "rx", oldVersion); // Upload V4.1
		log.info("Versions Uploaded - Test Preparation completed");
	}


	public void manageDevice() throws InterruptedException {
		
		//add license
		///sysMethods.addUnlimitedLicense(driver);
		log.info("Test Preparation - Unamage - Manage Device");
		discoveryMethods.discoverDevices(driver);
		discoveryMethods.stateAndIPcheck(driver, txMac, ipCheck, txIP, gateway, netmask);
		discoveryMethods.manageApplianceAutomatic(driver, "Test_TX", txMac, ipCheck);
		log.info("Appliance Managed Successfully - Test Preparation Completed");
	}

	@Test
	public void upgradeDevice() throws InterruptedException {
		log.info("Soak Test Case 01 - Upgrade device");
		methods.navigateToUpgrade(driver);
		methods.upgradeDevice(driver, "tx", txIP, newVersion, oldVersion);
		log.info("Device Upgraded Successfully - Soak Test Case 01 complete");
	}
}
