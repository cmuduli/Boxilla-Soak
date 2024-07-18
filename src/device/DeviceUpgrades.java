package device;

import org.apache.log4j.Logger;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import extra.Ssh;
import extra.StartupTestCase;
import extra.StartupTestCase2;
import methods.DevicesMethods;
/**
 * Class that contains tests for upgrading and downgrading of devices
 * @author Boxilla
 *
 */
public class DeviceUpgrades extends StartupTestCase2 {
	
	DevicesMethods methods = new DevicesMethods();
	final static Logger log = Logger.getLogger(DeviceUpgrades.class);
	private String deviceNewVersion = prop.getProperty("newVersion");
	private String deviceMiddleVersion = prop.getProperty("middleVersion");
	private String deviceOldVersion = prop.getProperty("oldVersion");
	

	
	private void setVersion(String ipAddress) {
		methods.getDeviceTypeForUpgrade(driver, ipAddress);
//		if(StartupTestCase.isEmerald) {
//			System.out.println("********************************* IS EMERALD TEST ******************************************");
//			deviceNewVersion = prop.getProperty("emeraldNewVersion");
//			deviceMiddleVersion = prop.getProperty("emeraldMiddleVersion");
//			deviceOldVersion = prop.getProperty("emeraldOldVersion");
//		}
//		if(StartupTestCase.isEmeraldSe) {
//			System.out.println("********************************* IS EMERALD SE TEST ******************************************");
//			deviceNewVersion = prop.getProperty("emeraldSENewVersion");
//			deviceMiddleVersion = prop.getProperty("emeraldSEMiddleVersion");
//			deviceOldVersion = prop.getProperty("emeraldSEOldVersion");
//		}
//		if(StartupTestCase.isZeroU) {
//			System.out.println("********************************* IS EMERALD SE TEST ******************************************");
//			deviceNewVersion = prop.getProperty("zeronewVersion");
//			deviceMiddleVersion = prop.getProperty("zeromiddleVersion");
//			deviceOldVersion = prop.getProperty("zerooldVersion");
//		}
//		log.info("NEW VERSION:" + deviceNewVersion);
//		log.info("MIDDLE VERSION:" + deviceMiddleVersion);
//		log.info("OLD VERSION:" + deviceOldVersion);
		
	}

	/**
	 * Downgrade TX twice. Uses test.properties new, middle and old version properties
	 * @throws InterruptedException
	 */
	@Test(groups= {"boxillaFunctional", "smoke", "emerald", "chrome" },retryAnalyzer = testNG.RetryAnalyzer.class)
	public void test01_downgradeDeviceTX() throws InterruptedException { 
		log.info("****test01_downgradeDeviceTX*****");
//		String name = "testTem";
//		for(int j=0; j < 50; j++) {
//			methods.addTemplateTransmitter(driver, name + j, "Best Quality",
//					"Off", "Default", "Analog", "5", "1920x1200", "1680x1050");
//		}
//		setVersion("10.211.128.173");
//		methods.navigateToUpgrade(driver);
//		methods.upgradeDevice(driver, "tx", txIp, deviceNewVersion, deviceMiddleVersion);
//		methods.upgradeDevice(driver, "tx", txIp, deviceMiddleVersion, deviceOldVersion);
//		Ssh ssh = new Ssh("root", "barrow1admin_12", txIp);
//		ssh.loginToServer();
//		String version = ssh.sendCommand("cat /VERSION");
//		log.info("Output from cat /VERSION:" + version);
//		ssh.disconnect();
//		Assert.assertTrue(version.contains(deviceOldVersion), "Version on device differes from boxilla, expected:" + 
//				deviceOldVersion + " actual:" + version);

	}
	
//	/**
//	 * Upgrade TX twice. Uses test.properties new, middle and old version properties
//	 * @throws InterruptedException
//	 */
//	@Test(groups= {"boxillaFunctional", "smoke", "emerald"},retryAnalyzer = testNG.RetryAnalyzer.class)
//	public void test02_upgradeDeviceTX() throws InterruptedException { 
//		log.info("****test02_upgradeDeviceTX****");
//		setVersion();
//		methods.navigateToUpgrade(driver);
//
//		methods.upgradeDevice(driver, "tx", txIp, deviceOldVersion, deviceMiddleVersion);
//		methods.upgradeDevice(driver, "tx", txIp, deviceMiddleVersion, deviceNewVersion);
//		Ssh ssh = new Ssh(deviceUserName, devicePassword, txIp);
//		ssh.loginToServer();
//		String version = ssh.sendCommand("cat /VERSION");
//		log.info("Output from cat /VERSION:" + version);
//		ssh.disconnect();
//		Assert.assertTrue(version.contains(deviceNewVersion), "Version on device differes from boxilla, expected:" + 
//				deviceNewVersion + " actual:" + version);
//	}
//	
//	@Test(groups= {"boxillaFunctional", "smoke", "emerald"},retryAnalyzer = testNG.RetryAnalyzer.class)
//	public void test03_downgradeDeviceRX() throws InterruptedException {
//		log.info("****test03_downgradeDeviceRX*****");
//		setVersion();
//		methods.navigateToUpgrade(driver);
//		methods.upgradeDevice(driver, "rx", rxIp, deviceNewVersion, deviceMiddleVersion);
//		methods.upgradeDevice(driver, "rx", rxIp, deviceMiddleVersion, deviceOldVersion);
//		log.info("deviceNewVersion:" + deviceNewVersion);
//		log.info("deviceMiddleVersion:" + deviceMiddleVersion);
//		log.info("deviceOldVersion:" + deviceOldVersion);
//
//		Ssh ssh = new Ssh("root", "barrow1admin_12", rxIp);
//		ssh.loginToServer();
//		String version = ssh.sendCommand("cat /VERSION");
//		log.info("Output from cat /VERSION:" + version);
//		ssh.disconnect();
//		Assert.assertTrue(version.contains(deviceOldVersion), "Version on device differes from boxilla, expected:" + 
//				deviceMiddleVersion + " actual:" + version);
//		
//
//	}
//	
//	@Test(groups= {"boxillaFunctional", "smoke", "emerald" },retryAnalyzer = testNG.RetryAnalyzer.class)
//	public void test04_upgradeDeviceRX() throws InterruptedException {
//		log.info("****test04_upgradeDeviceRX****");
//		setVersion();
//		methods.navigateToUpgrade(driver);
//
//		methods.upgradeDevice(driver, "rx", rxIp, deviceOldVersion, deviceMiddleVersion);
//		methods.upgradeDevice(driver, "rx", rxIp, deviceMiddleVersion, deviceNewVersion);
//
//		Ssh ssh = new Ssh(deviceUserName, devicePassword, rxIp);
//		ssh.loginToServer();
//		String version = ssh.sendCommand("cat /VERSION");
//		log.info("Output from cat /VERSION:" + version);
//		ssh.disconnect();
//		Assert.assertTrue(version.contains(deviceNewVersion), "Version on device differes from boxilla, expected:" + 
//				deviceNewVersion + " actual:" + version);
//	}
	
//	@Test(groups= {"boxillaFunctional", "smoke" },retryAnalyzer = com.testNG.RetryAnalyzer.class)
//	public void test05_upgradeDevicesCheckTimes() throws InterruptedException {
//		log.info("****test04_upgradeDeviceRX****");
//		setVersion();
//		methods.upgradeAll(driver, deviceMiddleVersion);
//	}
	
}

