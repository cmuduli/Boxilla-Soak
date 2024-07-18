package soak;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.testng.Assert;
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

public class DeviceReboot extends StartupTestCase2 {
	private String txIP, rxIP, extraIP, txMac, rxMac, ipCheck, gateway, netmask, newVersion, oldVersion;

	private DevicesMethods methods = new DevicesMethods();
	private DiscoveryMethods discoverymethods = new DiscoveryMethods();
	final static Logger log = Logger.getLogger(DeviceReboot.class);

	public DeviceReboot() {
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
	public void beforeClass() throws InterruptedException {
		Thread.sleep(60000);
		cleanUpLogin();
		try {
		manageDevice();
		//upgradeToRecentVersion();
		}catch(Exception | AssertionError e) {
			e.printStackTrace();
			cleanUpLogout();
		}
		cleanUpLogout();
	}
	
//	@AfterClass(alwaysRun=true)
	public void afterClass() {
		try {
			cleanUpLogin();
			methods.unManageDevice(driver, rxIP);
			cleanUpLogout();
		}catch(Exception | AssertionError e) {
			cleanUpLogout();
		}
	}

	@Test(alwaysRun=true)//(dependsOnMethods = { "upgradeToRecentVersion" })
	public void deviceReboot() throws InterruptedException { // Reboot device
		log.info("Soak Test Case-04 Started - Reboot device");
		for(int j=0; j < 20; j++) {
			methods.retrieveDetails(driver, "10.211.130.159", "00:8C:10:1E:D2:9F", "Test_RX_Emerald");
			float oldUptime = methods.uptime(driver);
			methods.rebootDevice(driver, "10.211.130.159");
			methods.retrieveDetails(driver,  "10.211.130.159", "00:8C:10:1E:D2:9F", "Test_RX_Emerald");
			float newUptime = methods.uptime(driver);
			Assert.assertTrue(newUptime < oldUptime, "***** New Uptime is higher than Old Uptime *****");
			log.info("Waiting for 1 minutes before finishing the test..");
			Thread.sleep(60000);
			log.info("Reboot Device - Soak Test Case-04 Completed");
		}
	}
	
	//@Test(retryAnalyzer = testNG.RetryAnalyzer.class)
	public void manageDevice() throws InterruptedException {
		log.info("Test Preparation - Unamage - Manage Device");
		discoverymethods.discoverDevices(driver);
		discoverymethods.stateAndIPcheck(driver, rxMac, ipCheck, rxIP, gateway, netmask);
		discoverymethods.manageApplianceAutomatic(driver, "Test_RX", rxMac, ipCheck);
		log.info("Appliance Managed Successfully - Test Preparation Completed");
	}

	//@Test(dependsOnMethods = { "manageDevice" }, retryAnalyzer = testNG.RetryAnalyzer.class)
	public void upgradeToRecentVersion() throws InterruptedException {
		log.info("Test Preparation - Upgrade to Recent version");
		methods.navigateToUpgrade(driver);
		methods.timer(driver);
		SeleniumActions.seleniumSendKeys(driver, Devices.selectDevicesSearchBox, Devices.selectDevicesSearchBox);
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
	
//	public static void main(String[] args) throws IOException {
////		Files.walk(Paths.get("\\\\READYNAS-SHARE\\tftpboot\\work"))
////		.filter(Files::isRegularFile)
////		.forEach(System.out::println);
//		
//	    Path path = Paths.get("\\\\READYNAS-SHARE\\tftpboot\\work\\Jenkins_Nightly");
//	    Files.walk(Paths.get("\\\\READYNAS-SHARE\\tftpboot\\work\\Jenkins_Nightly"))
//	     .filter(f -> f.toString().endsWith(".clu"))
//	     .map(p -> p.getRoot())
//	     .distinct()
//	     .forEach(System.out::println);
//		
//	
//	    
//	}
}
