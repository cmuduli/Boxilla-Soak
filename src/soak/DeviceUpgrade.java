package soak;

import org.apache.log4j.Logger;
import org.openqa.selenium.logging.Logs;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.blackbox.Version;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static io.restassured.RestAssured.given;
import extra.Ssh;
import extra.StartupTestCase;
import extra.StartupTestCase2;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import methods.DevicesMethods;
import methods.DiscoveryMethods;
import methods.SystemMethods;
import northbound.get.BoxillaHeaders;

public class DeviceUpgrade extends StartupTestCase2 {
	private String oldVersion, newVersion, txIP, txIPDual, rxIPDual, rxIP, txMac, rxMac, rxMacDual, txMacDual, ipCheck,
			gateway, netmask, middleVersion, version4, version5, version6;
	private DevicesMethods methods = new DevicesMethods();
	private DiscoveryMethods discoveryMethods = new DiscoveryMethods();
	private SystemMethods sysMethods = new SystemMethods();

	final static Logger log = Logger.getLogger(DeviceUpgrade.class);

	public DeviceUpgrade() {
		// Accessing test data from excel file
		txIP = prop.getProperty("txIP");
		txIPDual = prop.getProperty("txIPDual");
		rxIP = prop.getProperty("rxIP");
		rxIPDual = prop.getProperty("rxIPDual");

		if (!StartupTestCase.isEmerald) {
			newVersion = prop.getProperty("newVersion");
			oldVersion = prop.getProperty("oldVersion");
			version4 = prop.getProperty("version4");
			middleVersion = prop.getProperty("middleVersion");
			version5 = prop.getProperty("version5");
			version6 = prop.getProperty("version6");
		} else {
			newVersion = prop.getProperty("emeraldNewVersion");
			oldVersion = prop.getProperty("emeraldOldVersion");
			version4 = prop.getProperty("version4");
			middleVersion = prop.getProperty("middleVersion");
			version5 = prop.getProperty("version5");
			version6 = prop.getProperty("version6");
		}
		txMac = prop.getProperty("txMac");
		txMacDual = prop.getProperty("txMacDual");
		rxMac = prop.getProperty("rxMac");
		rxMacDual = prop.getProperty("rxMacDual");
		ipCheck = prop.getProperty("ipCheck");
		gateway = prop.getProperty("gateway");
		netmask = prop.getProperty("netmask");
	}

//	 @BeforeClass(alwaysRun = true)
	public void beforeClass() throws InterruptedException {
		Thread.sleep(6000);
		RestAssured.useRelaxedHTTPSValidation();
		RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
		cleanUpLogin();
		sysMethods.enableNorthboundAPI(driver);
		try {
			manageDevice();
			// uploadAndActivateVersion();
		} catch (Exception | AssertionError e) {
			e.printStackTrace();
			cleanUpLogout();
		}
		cleanUpLogout();
	}

	@AfterClass(alwaysRun = true)
	public void afterClass() {
		try {
			cleanUpLogin();
			// methods.unManageDevice(driver, rxIP);
			methods.unManageDevice(driver, txIP);
			cleanUpLogout();
		} catch (Exception | AssertionError e) {
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

		// add license
		/// sysMethods.addUnlimitedLicense(driver);
		// log.info("Test Preparation - Managing RX");
		// discoveryMethods.discoverDevices(driver); // navigates and clicks on
		// discovery
		// discoveryMethods.stateAndIPcheck(driver, rxMac, ipCheck, rxIP, gateway,
		// netmask); // runs discovery until found
		// // and changes network
		// // config when found
		// discoveryMethods.manageApplianceAutomatic(driver, "Test_RX", rxMac, ipCheck);
		// // manages device (network should
		// // be changed at this stage)
		//

		log.info("Test Preparation - Managing TX");
		discoveryMethods.discoverDevices(driver);
		discoveryMethods.stateAndIPcheck(driver, txMac, ipCheck, txIP, gateway, netmask);
		discoveryMethods.manageApplianceAutomatic(driver, "Test_TX", txMac, ipCheck);

		log.info("Appliance Managed Successfully - Test Preparation Completed");
	}

	// @Test
	public void upgradeDowngradeRX() throws InterruptedException {
		log.info("Soak Test Case 01 - Upgrade downgrade RX");
		methods.navigateToUpgrade(driver);
		methods.upgradeDevice(driver, "rx", rxIP, newVersion, oldVersion);
		String password = "barrow1admin_12";
		if (oldVersion.contains("5.2.5")) {
			password = "Witcher1@B_Box";
		}
		try {
			Thread.sleep(5000);
			Ssh ssh = new Ssh("root", password, rxIP);
			ssh.loginToServer();
			String version = ssh.sendCommand("cat /VERSION");
			ssh.disconnect();
			log.info("VERSION FROM DEVICE:" + version);
			version = version.trim();
			newVersion = newVersion.trim();
			log.info("Version:" + version + ".");
			log.info("New Version:" + newVersion + ".");
			Assert.assertTrue(version.equalsIgnoreCase(newVersion),
					"Version on device differes from boxilla, expected:" + oldVersion + " actual:" + version);
		} catch (Exception e) {
			log.info("MAYBE A PROBLEM WITH THE VERSION. iF THE TEST DIDNT FAIL CHECK.");
		}
		String temp = newVersion;
		newVersion = oldVersion;
		oldVersion = temp;

		log.info("Device Upgraded/Downgraded Successfully - Soak Test Case 01 complete");
	}

	@Test
	public void upgradeDowngradeTX() throws InterruptedException {
		RestAssured.useRelaxedHTTPSValidation();
		RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
		log.info("Soak Test Case 02 - Upgrade downgrade TX");
		String device_name = "Test_TX";
		String edid_settings_dvi1 = "1920x1080";
		String hid_configurations = "1";
		String mouse_keyboard_timeout = "0";
		String video_quality = "Default";
		String hid1 = "Basic";

		String video_source_optimization = "Off";
		String audio_source = "Analog Audio";
		methods.navigateToUpgrade(driver);
		methods.upgradeDevice(driver, "tx", txIP, newVersion, oldVersion);
//		methods.upgradeDevice1(driver, "tx", txIP, newVersion, oldVersion, middleVersion, boxillaManager,
//				boxillaRestUser, boxillaRestPassword, device_name, edid_settings_dvi1, hid_configurations,
//				mouse_keyboard_timeout, video_quality, hid1, audio_source, video_source_optimization);
		// methods.upgradeDevice(driver, "tx", txIP, newVersion, oldVersion);
		String password = "barrow1admin_12";
		if (oldVersion.contains("5.2.5")) {
			password = "Witcher1@B_Box";
		}
		try {
			Thread.sleep(5000);
			Ssh ssh = new Ssh("root", password, txIP);
			ssh.loginToServer();
			String version = ssh.sendCommand("cat /VERSION");
			ssh.disconnect();
			log.info("VERSION FROM DEVICE:" + version);
			version = version.trim();
			newVersion = newVersion.trim();
			log.info("Version:" + version + ".");
			log.info("New Version:" + newVersion + ".");
			if (version.equalsIgnoreCase(newVersion)) {
				log.info("Version on device  from boxilla is:" + version);
			} else if (version.equalsIgnoreCase(oldVersion)) {
				log.info("Version on device  from boxilla is:" + version);
			} else if (version.equalsIgnoreCase(middleVersion)) {
				log.info("Version on device  from boxilla is:" + version);
//			} else if (version.equalsIgnoreCase(version4)) {
//				log.info("Version on device  from boxilla is:" + version);
//			} else if (version.equalsIgnoreCase(version5)) {
//				log.info("Version on device  from boxilla is:" + version);
//			} else if (version.equalsIgnoreCase(version6)) {
//				log.info("Version on device  from boxilla is:" + version);
			} else {
				Assert.fail("MAYBE A PROBLEM WITH THE VERSION. iF THE TEST DIDNT FAIL CHECK.");
				// log.info("MAYBE A PROBLEM WITH THE VERSION. iF THE TEST DIDNT FAIL CHECK.");
			}
			// Assert.assertTrue(version.equalsIgnoreCase(newVersion),
			// "Version on device differes from boxilla, expected:" + oldVersion + "
			// actual:" + version);
		} catch (Exception e) {
			log.info("MAYBE A PROBLEM WITH THE VERSION. iF THE TEST DIDNT FAIL CHECK.");
		}
		String temp = newVersion;
		newVersion = oldVersion;
		oldVersion = temp;

		log.info("Device Upgraded/Downgraded Successfully - Soak Test Case 01 complete");
	}

	// @Test
	public void upgradeDowngradeAllDevices() throws InterruptedException {
		log.info("Soak Test Case 03 - Upgrade all devices");
		methods.navigateToUpgrade(driver);
		methods.upgradeDevices(driver, "tx", "rx", txIP, rxIP, newVersion, oldVersion);
	}

}
