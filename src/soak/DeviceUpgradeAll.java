package soak;

import static io.restassured.RestAssured.basic;
import static io.restassured.RestAssured.given;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import extra.Ssh;
import extra.StartupTestCase;
import extra.StartupTestCase2;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import methods.DevicesMethods;
import methods.DiscoveryMethods;
import methods.SystemMethods;

public class DeviceUpgradeAll extends StartupTestCase2 {
	// private String oldVersion, newVersion, txIP, rxIP, txMac, rxMac, ipCheck,
	// gateway, netmask;
	private DevicesMethods methods = new DevicesMethods();
	private DiscoveryMethods discoveryMethods = new DiscoveryMethods();
	private SystemMethods sysMethods = new SystemMethods();
	final static Logger log = Logger.getLogger(DeviceUpgradeAll.class);
	String con1 = "private";
	String con2 = "con2";
	String con3 = "con3";
	String con4 = "con4";

	// String rxIP1 = "10.211.128.156";
	// String rxIP2 = "10.211.129.193";
	// String rxIP3 = "10.211.128.155";
	// String rxIP4 = "10.211.128.181";

	ForceConnect connection1 = new ForceConnect();
	ForceConnect connection2 = new ForceConnect();
	ForceConnect connection3 = new ForceConnect();
	ForceConnect connection4 = new ForceConnect();

	class ForceConnect {
		public String action = "";
		public String user = "";
		public String connection = "";
	}
	// public DeviceUpgradeAll() {
	// // Accessing test data from excel file
	// txIP = prop.getProperty("txIP");
	// rxIP = prop.getProperty("rxIP");
	// if(!StartupTestCase.isEmerald) {
	// newVersion = prop.getProperty("newVersion");
	// oldVersion = prop.getProperty("oldVersion");
	// }else {
	// newVersion = prop.getProperty("emeraldNewVersion");
	// oldVersion = prop.getProperty("emeraldOldVersion");
	// }
	// txMac = prop.getProperty("txMac");
	// rxMac = prop.getProperty("rxMac");
	// ipCheck = prop.getProperty("ipCheck");
	// gateway = prop.getProperty("gateway");
	// netmask = prop.getProperty("netmask");
	// }

	// @BeforeClass(alwaysRun=true)
	public void beforeClass() throws InterruptedException {
		deviceManageTestPrep();
		RestAssured.authentication = basic(restuser, restPassword);
		RestAssured.useRelaxedHTTPSValidation();

		cleanUpLogin();
		try {
			// manageDevice();
			// uploadAndActivateVersion();
		} catch (Exception | AssertionError e) {
			e.printStackTrace();
			cleanUpLogout();
		}
		cleanUpLogout();
	}
	//
	//
	// @AfterClass(alwaysRun=true)
	// public void afterClass() {
	// try {
	// cleanUpLogin();
	// methods.unManageDevice(driver, rxIP);
	// cleanUpLogout();
	// }catch(Exception | AssertionError e) {
	// cleanUpLogout();
	// }
	// }

	// public void uploadAndActivateVersion() throws InterruptedException { //
	// Upload Two version to switch between
	// // Upload two version Old and New, activate new version and upgrade device
	// log.info("Test Preparation - Upload version on managed rx device");
	// methods.navigateToUpgrade(driver); // Navigate to Upgrade page
	// methods.uploadVersion(driver, "rx", newVersion); // Upload V4.2
	// methods.uploadVersion(driver, "rx", oldVersion); // Upload V4.1
	// log.info("Versions Uploaded - Test Preparation completed");
	// }
	//
	//
	// public void manageDevice() throws InterruptedException {
	//
	// //add license
	// ///sysMethods.addUnlimitedLicense(driver);
	// log.info("Test Preparation - Unamage - Manage Device");
	// discoveryMethods.discoverDevices(driver);
	// discoveryMethods.stateAndIPcheck(driver, rxMac, ipCheck, rxIP, gateway,
	// netmask);
	// discoveryMethods.manageApplianceAutomatic(driver, "Test_RX", rxMac, ipCheck);
	// log.info("Appliance Managed Successfully - Test Preparation Completed");
	// }

	@Test
	public void upgradeDevice() throws InterruptedException {
		String[] deviceIps = { rxIp, txIp, rxIpDual };

		methods.newUpgrade(driver, "V5.5.2_r7026", "PE", deviceIps);
		methods.newUpgrade(driver, "V5.6.0_r7363", "PE", deviceIps);

		// methods.newUpgrade(driver, "V5.5.2_r6932", "PE");
		// methods.newUpgrade(driver, "V5.5.2_r7006", "PE");
		// methods.newUpgrade(driver, "V5.5.2_r6932", "PE");
		// methods.newUpgrade(driver, "V5.5.2_r7006", "PE");
		// methods.newUpgrade(driver, "V5.5.2_r6932", "PE");
		// methods.newUpgrade(driver, "V5.5.2_r7006", "PE");
		// log.info("Soak Test Case 01 - Upgrade device");
		// methods.navigateToUpgrade(driver);
		// String output = methods.getAllDeviceTable(driver);
		// if(output.contains(newVersion) && output.contains(oldVersion)) {
		// log.info("ERROR - NOT ALL DEVICES ON SAME VERSION. WILL PICK NEW VERSION AND
		// UPGRADE TO FIX FOR NEXT TEST");
		// }
		//
		// String notUpgrade = "";
		// String upgradeVersion = "";
		// if(output.contains(newVersion)) {
		// upgradeVersion = oldVersion;
		// notUpgrade = newVersion;
		// }else {
		// upgradeVersion = newVersion;
		// notUpgrade = oldVersion;
		// }
		// log.info("VERSION SELECTED FOR UPGRADE:" + upgradeVersion);
		// methods.upgradeAll(driver, upgradeVersion);
		// log.info("sleeping wile devices upgrade");
		// Thread.sleep(260000);
		// Thread.sleep(1000);
		//
		// String output2 = methods.getAllDeviceTable(driver);
		// log.info("OUTPUT AFTER UPGRADE:" + output2);
		// Assert.assertTrue(output2.contains(upgradeVersion), "DEVICES DID NOT UPGRADE
		// TO :" + upgradeVersion);
		// log.info("Devices upgraded");
		// Assert.assertFalse(output2.contains(notUpgrade), "SOME DEVICES DID NOT
		// UPGRADE TO " + upgradeVersion);
		//
		//
		// //SSH
		//
		// String[] ipList = {"10.211.128.155", "10.211.128.156"};
		// for(String s : ipList) {
		// Ssh ssh = new Ssh("root", "barrow1admin_12", s);
		// ssh.loginToServer();
		// String versionOnDevice = ssh.sendCommand("cat /VERSION");
		// ssh.disconnect();
		// log.info("OUTPUT FROM DEVICE:" + versionOnDevice);
		// Assert.assertTrue(versionOnDevice.contains(upgradeVersion), "Version on
		// device does not match upgrade version");
		// }
		//
		// connection1.action = "force_connection";
		// connection1.user = "Boxilla";
		// connection1.connection = con1;
		//
		//// connection2.action = "force_connection";
		//// connection2.user = "Boxilla";
		//// connection2.connection = con2;
		////
		//// connection3.action = "force_connection";
		//// connection3.user = "Boxilla";
		//// connection3.connection = con3;
		////
		//// connection4.action = "force_connection";
		//// connection4.user = "Boxilla";
		//// connection4.connection = con4;
		//
		//
		// int status1 = given().header(getHead()).body(connection1)
		// .when()
		// .contentType(ContentType.JSON)
		// .put("https://" + rxIP1 +
		// ":8888/control/connections").andReturn().statusCode(); //make connection
		// System.out.println("RETURN code :" + status1);
		// Assert.assertTrue(status1 == 200, "status did not equal 200");
		//
		// System.out.println("Leaving connection up 10 seconds");
		// Thread.sleep(10000);
		// //Thread.sleep(5000);
		// Ssh ssh = new Ssh("root", "barrow1admin_12", rxIP1);
		// ssh.loginToServer();
		// String isCloudRunning = ssh.sendCommand("ps -ax");
		// ssh.disconnect();
		// //System.out.println("OUTPUT:" + isCloudRunning);
		// Assert.assertTrue(isCloudRunning.contains(con1), "Connection was not running
		// ");
		// log.info("FIRST CONNECTION OK");
		//
		//// //second
		//// int status2 = given().header(getHead()).body(connection2)
		//// .when()
		//// .contentType(ContentType.JSON)
		//// .put("https://" + rxIP2 +
		// ":8888/control/connections").andReturn().statusCode(); //make connection
		//// System.out.println("RETURN code :" + status2);
		//// Assert.assertTrue(status2 == 200, "status did not equal 200");
		////
		//// System.out.println("Leaving connection up 10 seconds");
		//// Thread.sleep(10000);
		//// //Thread.sleep(5000);
		//// Ssh ssh2 = new Ssh("root", "barrow1admin_12", rxIP2);
		//// ssh2.loginToServer();
		//// String isCloudRunning2 = ssh2.sendCommand("ps -ax");
		//// ssh2.disconnect();
		//// //System.out.println("OUTPUT:" + isCloudRunning);
		//// Assert.assertTrue(isCloudRunning2.contains(con2), "Connection was not
		// running ");
		//// log.info("SECOND CONNECTION OK");
		////
		//// //third
		//// int status3 = given().header(getHead()).body(connection3)
		//// .when()
		//// .contentType(ContentType.JSON)
		//// .put("https://" + rxIP3 +
		// ":8888/control/connections").andReturn().statusCode(); //make connection
		//// System.out.println("RETURN code :" + status3);
		//// Assert.assertTrue(status3 == 200, "status did not equal 200");
		////
		//// System.out.println("Leaving connection up 10 seconds");
		//// Thread.sleep(10000);
		//// //Thread.sleep(5000);
		//// Ssh ssh3 = new Ssh("root", "barrow1admin_12", rxIP3);
		//// ssh3.loginToServer();
		//// String isCloudRunning3 = ssh3.sendCommand("ps -ax");
		//// ssh3.disconnect();
		//// //System.out.println("OUTPUT:" + isCloudRunning);
		//// Assert.assertTrue(isCloudRunning3.contains(con3), "Connection was not
		// running ");
		//// log.info("THIRD CONNECTION OK");
		////
		//// //forth
		//// int status4 = given().header(getHead()).body(connection4)
		//// .when()
		//// .contentType(ContentType.JSON)
		//// .put("https://" + rxIP4 +
		// ":8888/control/connections").andReturn().statusCode(); //make connection
		//// System.out.println("RETURN code :" + status4);
		//// Assert.assertTrue(status4 == 200, "status did not equal 200");
		////
		//// System.out.println("Leaving connection up 10 seconds");
		//// Thread.sleep(10000);
		//// //Thread.sleep(5000);
		//// Ssh ssh4 = new Ssh("root", "barrow1admin_12", rxIP4);
		//// ssh4.loginToServer();
		//// String isCloudRunning4 = ssh4.sendCommand("ps -ax");
		//// ssh4.disconnect();
		//// //System.out.println("OUTPUT:" + isCloudRunning);
		//// Assert.assertTrue(isCloudRunning4.contains(con4), "Connection was not
		// running ");
		//
		//
		//
		//
		// //methods.upgradeAll(driver, "blah");
		//// methods.upgradeDevice(driver, "rx", rxIP, newVersion, oldVersion);
		////
		//// try {
		//// Ssh ssh = new Ssh("root", "barrow1admin_12", rxIP);
		//// ssh.loginToServer();
		//// String version = ssh.sendCommand("cat /VERSION");
		//// ssh.disconnect();
		//// Assert.assertTrue(version.contains(newVersion), "Version on device differes
		// from boxilla, expected:" +
		//// oldVersion + " actual:" + version);
		//// }catch(Exception e) {
		//// log.info("MAYBE A PROBLEM WITH THE VERSION. iF THE TEST DIDNT FAIL
		// CHECK.");
		//// }
		//// String temp = newVersion;
		//// newVersion = oldVersion;
		//// oldVersion = temp;
		////
		//// log.info("Device Upgraded Successfully - Soak Test Case 01 complete");
	}
}
