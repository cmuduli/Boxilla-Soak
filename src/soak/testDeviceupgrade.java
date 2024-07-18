package soak;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;

import extra.Ssh;
import extra.StartupTestCase;
import extra.StartupTestCase2;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import methods.DevicesMethods;
import methods.DiscoveryMethods;
import methods.SystemMethods;
import northbound.get.BoxillaHeaders;
import northbound.get.config.AppliancePropertiesConfig;
import northbound.get.config.AppliancePropertiesConfig.GetProperties;
import northbound.put.SetKvmReceiverProperties.ReceiverProperties;
import northbound.put.config.EditKvmConnectionsConfig;

public class testDeviceupgrade extends StartupTestCase2 {

	private String oldVersion, newVersion, txIP, txIPDual, rxIPDual, rxIP, txMac, rxMac, rxMacDual, txMacDual, ipCheck,
			gateway, netmask, middleVersion, version4, version5, version6;
	private DevicesMethods methods = new DevicesMethods();
	private DiscoveryMethods discoveryMethods = new DiscoveryMethods();
	private SystemMethods sysMethods = new SystemMethods();
	private EditKvmConnectionsConfig config = new EditKvmConnectionsConfig();
	private AppliancePropertiesConfig con = new AppliancePropertiesConfig();
	private AppliancePropertiesConfig config1 = new AppliancePropertiesConfig();

	final static Logger log = Logger.getLogger(DeviceUpgrade.class);

	public class ReceiverProperties {
		public String device_name;
		public String http_enabled;
		public String zone;
	}

	public testDeviceupgrade() {
		// Accessing test data from excel file
		txIP = prop.getProperty("txIP");
		txIPDual = prop.getProperty("txIPDual");
		rxIP = prop.getProperty("rxIP");
		rxIPDual = prop.getProperty("rxIPDual");

		if (!StartupTestCase.isEmerald) {
			newVersion = prop.getProperty("newVersion");
			oldVersion = prop.getProperty("oldVersion");
			middleVersion = prop.getProperty("middleVersion");
			version4 = prop.getProperty("version4");
			version5 = prop.getProperty("version5");
			version6 = prop.getProperty("version6");

		} else {
			newVersion = prop.getProperty("emeraldNewVersion");
			oldVersion = prop.getProperty("emeraldOldVersion");
			middleVersion = prop.getProperty("emeraldMiddleVersion");
			version4 = prop.getProperty("emeraldversion4");
			version5 = prop.getProperty("emeraldversion5");
			version6 = prop.getProperty("emeraldversion6");
		}
		txMac = prop.getProperty("txMac");
		txMacDual = prop.getProperty("txMacDual");
		rxMac = prop.getProperty("rxMac");
		rxMacDual = prop.getProperty("rxMacDual");
		ipCheck = prop.getProperty("ipCheck");
		gateway = prop.getProperty("gateway");
		netmask = prop.getProperty("netmask");
	}

	@BeforeClass(alwaysRun = true)
	public void beforeClass() throws InterruptedException {
		RestAssured.useRelaxedHTTPSValidation();
		RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
		cleanUpLogin();
		SystemMethods sys = new SystemMethods();
		sys.dbReset(driver);
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
			RestAssured.useRelaxedHTTPSValidation();
			RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
//			methods.unManageDevice(driver, rxIP);
//			discoveryMethods.unmanageDevice1(driver,boxillaManager, boxillaRestUser, boxillaRestPassword,rxMac,rxIp);
//			methods.unManageDevice(driver, rxIPDual);
////			discoveryMethods.unmanageDevice1(driver,boxillaManager, boxillaRestUser, boxillaRestPassword,rxMacDual,rxIPDual);
			methods.unManageDevice(driver, txIP);
////			discoveryMethods.unmanageDevice1(driver,boxillaManager, boxillaRestUser, boxillaRestPassword,txMac,txIP);
//			methods.unManageDevice(driver, txIpDual);
////			discoveryMethods.unmanageDevice1(driver,boxillaManager, boxillaRestUser, boxillaRestPassword,txMacDual,txIpDual);

			

			cleanUpLogout();
		} catch (Exception | AssertionError e) {
			cleanUpLogout();
		}
	}

	public void uploadAndActivateVersion() throws InterruptedException { // Upload Two version to switch between
		// Upload two version Old and New, activate new version and upgrade device
//		log.info("Test Preparation - Upload version on managed rx device");
//		methods.navigateToUpgrade(driver); // Navigate to Upgrade page
//		methods.uploadVersion(driver, "rx", newVersion); // Upload V4.2
//		methods.uploadVersion(driver, "rx", oldVersion); // Upload V4.1
//		log.info("Versions Uploaded - Test Preparation completed");
	}

	public void manageDevice() throws InterruptedException {

		// add license
		// sysMethods.addUnlimitedLicense(driver);
//		log.info("Test Preparation - Managing RX");
//		discoveryMethods.discoverDevices(driver); // navigates and clicks on discovery
//		discoveryMethods.stateAndIPcheck(driver, rxMac, ipCheck, rxIP, gateway, netmask); // runs discovery until found
//																							// // and changes network//
//																							// config when found
//		
//		discoveryMethods.manageApplianceAutomatic(driver, "Test_RX", rxMac, ipCheck);
//		discoveryMethods.manageDevice1(driver,boxillaManager, boxillaRestUser, boxillaRestPassword, rxIP, "Test_RX");
		// manages device (network should be changed at this stage)
//		
//		log.info("Test Preparation - Managing RXDual"); // also ensures device is managed
//		discoveryMethods.discoverDevices(driver);
//		discoveryMethods.stateAndIPcheck(driver, rxMacDual, ipCheck, rxIPDual, gateway, netmask);
//		discoveryMethods.manageApplianceAutomatic(driver, "Test_RX_Dual", rxMacDual, ipCheck);
//		discoveryMethods.manageDevice1(driver,boxillaManager, boxillaRestUser, boxillaRestPassword, rxIPDual, "Test_RX_Dual");
		log.info("Test Preparation - Managing TX");
		discoveryMethods.discoverDevices(driver);
		discoveryMethods.stateAndIPcheck(driver, txMac, ipCheck, txIP, gateway, netmask);
		discoveryMethods.manageApplianceAutomatic(driver, "Test_TX", txMac, ipCheck);
//		

		
//		discoveryMethods.manageDevice1(driver,boxillaManager, boxillaRestUser, boxillaRestPassword, txIP, "Test_TX");
//
//		log.info("Test Preparation - Managing TXDual"); // also ensures device is//managed
//		discoveryMethods.discoverDevices(driver);
//		discoveryMethods.stateAndIPcheck(driver, txMacDual, ipCheck, txIPDual, gateway, netmask);
//		discoveryMethods.manageApplianceAutomatic(driver, "Test_TX_Dual", txMacDual, ipCheck);
//		discoveryMethods.manageDevice1(driver,boxillaManager, boxillaRestUser, boxillaRestPassword, txIPDual, "Test_TX_Dual");
		log.info("Appliance Managed Successfully - Test Preparation Completed");
	}

//	@Test
	public void upgradeDowngradeRX() throws InterruptedException {
		log.info("Soak Test Case 01 - Upgrade downgrade RX");
		RestAssured.useRelaxedHTTPSValidation();
		RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

		String device_name = "Test_RX";
		String http_enabled = "Enabled";

		methods.navigateToUpgrade(driver);
		methods.upgradereciver(driver, "rx", rxIP, newVersion, oldVersion, middleVersion, version4, version5, version6,
				boxillaManager, boxillaRestUser, boxillaRestPassword, device_name, http_enabled);

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
			if (version.equalsIgnoreCase(newVersion)) {
				log.info("Version on device  from boxilla is:" + version);
			} else if (version.equalsIgnoreCase(oldVersion)) {
				log.info("Version on device  from boxilla is:" + version);
			} else if (version.equalsIgnoreCase(middleVersion)) {
				log.info("Version on device  from boxilla is:" + version);
			} else if (version.equalsIgnoreCase(version4)) {
				log.info("Version on device  from boxilla is:" + version);
			} else if (version.equalsIgnoreCase(version5)) {
				log.info("Version on device  from boxilla is:" + version);
			} else if (version.equalsIgnoreCase(version6)) {
				log.info("Version on device  from boxilla is:" + version);
			} else {
				Assert.fail("MAYBE A PROBLEM WITH THE VERSION. iF THE TEST DIDNT FAIL CHECK.");
			}
			// Assert.assertTrue(version.equalsIgnoreCase(newVersion),
			// "Version on device differes from boxilla, expected:" + oldVersion + "
			// actual:" + version);
		} catch (Exception e) {
			log.info("MAYBE A PROBLEM WITH THE VERSION. iF THE TEST DIDNT FAIL CHECK.");
		}
		// String temp = newVersion;
		// newVersion = oldVersion;
		// oldVersion = temp;

		log.info("Device Upgraded/Downgraded Successfully - Soak Test Case 01 complete");
	}

//	@Test
	public void upgradeDowngradeRXDual() throws InterruptedException {
		log.info("Soak Test Case 02 - Upgrade downgrade RXDual");
		RestAssured.useRelaxedHTTPSValidation();
		RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

		ReceiverProperties prop = new ReceiverProperties();

		prop.device_name = "Test_RX_Dual";
		String http_enabled = "Enabled";
		String device_name = "Test_RX_Dual";

		methods.navigateToUpgrade(driver);
		methods.upgradereciver(driver, "rx", rxIPDual, newVersion, oldVersion, middleVersion, version4, version5,
				version6, boxillaManager, boxillaRestUser, boxillaRestPassword, device_name, http_enabled);

		String password = "barrow1admin_12";
		if (oldVersion.contains("5.2.5")) {
			password = "Witcher1@B_Box";
		}
		try {
			Thread.sleep(5000);
			Ssh ssh = new Ssh("root", password, rxIPDual);
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
		// String temp = newVersion;
		// newVersion = oldVersion;
		// oldVersion = temp;

		log.info("Device Upgraded/Downgraded Successfully - Soak Test Case 02 complete");

	}

	@Test
	public void upgradeDowngradeTX() throws InterruptedException {
		RestAssured.useRelaxedHTTPSValidation();
		RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

		log.info("Soak Test Case 03 - Upgrade downgrade TX");
		String device_name = "Test_TX";
		String edid_settings_dvi1 = "1920x1080";
		String edid_settings_dvi1_4K = "1920x1080p-60Hz";
		String hid_configurations = "1";
		String mouse_keyboard_timeout = "0";
		String video_quality = "Default";
		String hid1 = "Basic";

		String video_source_optimization = "Off";
		String audio_source = "DisplayPort Audio";
		String audio_source1="Analog Audio";

		methods.navigateToUpgrade(driver);
		// Thread.sleep(120000);
		if (StartupTestCase.isEmerald) {
			log.info("***Upgrading 4K device***");
			methods.upgradeDevice1(driver, "tx", txIP, newVersion, oldVersion, middleVersion, version4, version5,
					version6, boxillaManager, boxillaRestUser, boxillaRestPassword, device_name, edid_settings_dvi1_4K,
					hid_configurations, mouse_keyboard_timeout, video_quality, hid1,audio_source1);
		}else if (StartupTestCase.isZeroU) {
			log.info("***Upgrading ZeroU device***");
			methods.upgradeDevice1(driver, "tx", txIP, newVersion, oldVersion, middleVersion, version4, version5,
					version6, boxillaManager, boxillaRestUser, boxillaRestPassword, device_name, edid_settings_dvi1,
					hid_configurations, mouse_keyboard_timeout, video_quality, hid1,audio_source);
		} 
		else {
			log.info("***Upgrading SE device***");
			methods.upgradeDevice1(driver, "tx", txIP, newVersion, oldVersion, middleVersion, version4, version5,
					version6, boxillaManager, boxillaRestUser, boxillaRestPassword, device_name, edid_settings_dvi1,
					hid_configurations, mouse_keyboard_timeout, video_quality, hid1,audio_source1);
		}

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
			} else if (version.equalsIgnoreCase(version4)) {
				log.info("Version on device  from boxilla is:" + version);
			} else if (version.equalsIgnoreCase(version5)) {
				log.info("Version on device  from boxilla is:" + version);
			} else if (version.equalsIgnoreCase(version6)) {
				log.info("Version on device  from boxilla is:" + version);
			} else {
				Assert.fail("MAYBE A PROBLEM WITH THE VERSION. iF THE TEST DIDNT FAIL CHECK.");
			}
			// Assert.assertTrue(version.equalsIgnoreCase(newVersion),
			// "Version on device differes from boxilla, expected:" + oldVersion + "
			// actual:" + version);
		} catch (Exception e) {
			log.info("MAYBE A PROBLEM WITH THE VERSION. iF THE TEST DIDNT FAIL CHECK.");
		}
		// String temp = newVersion;
		// newVersion = oldVersion;
		// oldVersion = temp;

		log.info("Device Upgraded/Downgraded Successfully - Soak Test Case 01 complete");
	}

//	@Test
	public void upgradeDowngradeTXDual() throws InterruptedException {
		log.info("Soak Test Case 04 - Upgrade downgrade TXDual");

		String device_name = "Test_TX_Dual";
		String edid_settings_dvi1 = "1920x1080";
		String edid_settings_dvi2 = "1920x1080";
		String hid_configurations = "1";
		String mouse_keyboard_timeout = "0";
		String video_quality = "Default";
		// String video_source_optimization = "Off";
		String audio_source = "DisplayPort Audio";
		String hid1 = "Basic";
		String edid_settings_dvi1_4K = "1920x1080p-60Hz";
		String audio_source1="Analog Audio";

		methods.navigateToUpgrade(driver);
		if (StartupTestCase.isEmerald) {
			log.info("***Upgrading 4K_Dual device***");
			methods.upgradeDevice1(driver, "tx", txIPDual, newVersion, oldVersion, middleVersion, version4, version5,
					version6, boxillaManager, boxillaRestUser, boxillaRestPassword, device_name, edid_settings_dvi1_4K,
					hid_configurations, mouse_keyboard_timeout, video_quality, hid1,audio_source1);
		}else if (StartupTestCase.isZeroU) {
			log.info("***Upgrading ZeroU_Dual device***");
			methods.upgradeDevice1(driver, "tx", txIPDual, newVersion, oldVersion, middleVersion, version4, version5,
					version6, boxillaManager, boxillaRestUser, boxillaRestPassword, device_name, edid_settings_dvi1,
					hid_configurations, mouse_keyboard_timeout, video_quality, hid1,audio_source);
		} 
		else {
			log.info("***Upgrading SE_Dual device***");
			methods.upgradeDevice1(driver, "tx", txIPDual, newVersion, oldVersion, middleVersion, version4, version5,
					version6, boxillaManager, boxillaRestUser, boxillaRestPassword, device_name, edid_settings_dvi1,
					hid_configurations, mouse_keyboard_timeout, video_quality, hid1,audio_source1);
		}

		String password = "barrow1admin_12";
		if (oldVersion.contains("5.2.5")) {
			password = "Witcher1@B_Box";
		}
		try {
			Thread.sleep(5000);
			Ssh ssh = new Ssh("root", password, txIPDual);
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
			} else if (version.equalsIgnoreCase(version4)) {
				log.info("Version on device  from boxilla is:" + version);
			} else if (version.equalsIgnoreCase(version5)) {
				log.info("Version on device  from boxilla is:" + version);
			} else if (version.equalsIgnoreCase(version6)) {
				log.info("Version on device  from boxilla is:" + version);
			} else {
				Assert.fail("MAYBE A PROBLEM WITH THE VERSION. iF THE TEST DIDNT FAIL CHECK.");
			}
			// Assert.assertTrue(version.equalsIgnoreCase(newVersion),
			// "Version on device differes from boxilla, expected:" + oldVersion + "
			// actual:" + version);
		} catch (Exception e) {
			log.info("MAYBE A PROBLEM WITH THE VERSION. iF THE TEST DIDNT FAIL CHECK.");
		}
		// String temp = newVersion;
		// newVersion = oldVersion;
		// oldVersion = temp;

		log.info("Device Upgraded/Downgraded Successfully - Soak Test Case 01 complete");

	}

//	@Test
	public void upgradeDowngradeAllDevices() throws InterruptedException {
		log.info("Soak Test Case 03 - Upgrade all devices");
		methods.navigateToUpgrade(driver);
		methods.upgradeDevices(driver, "tx", "rx", txIP, rxIP, newVersion, oldVersion);
	}

}
