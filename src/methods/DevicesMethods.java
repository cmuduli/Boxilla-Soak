package methods;

import java.util.Date;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.eclipse.jetty.util.log.Log;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.SkipException;

import device.DeviceDiscovery;
import extra.ScpTo;
import extra.SeleniumActions;
import extra.Ssh;
import extra.StartupTestCase;
import extra.StartupTestCase2;
import io.restassured.http.ContentType;
import northbound.get.BoxillaHeaders;
import northbound.get.config.AppliancePropertiesConfig;
import northbound.get.config.AppliancePropertiesConfig.GetProperties;
import northbound.post.config.CreateKvmConnectionsConfig;
import northbound.put.config.EditKvmConnectionsConfig;
import objects.ActiveConnectionElements;
import objects.Connections;
import objects.Devices;
import objects.Landingpage;
import objects.Users;
import testNG.Utilities;

/**
 * Class contains methods for interacting with boxilla devices
 * 
 * @author Boxilla
 *
 */
public class DevicesMethods extends StartupTestCase2 {

	private EditKvmConnectionsConfig config = new EditKvmConnectionsConfig();
	private AppliancePropertiesConfig config1 = new AppliancePropertiesConfig();

	public enum RESOLUTION {
		R1920X1080, R1920X1200, R800X600, R640X480, R1024X768, R1280X1024
	}

	public enum HOTKEY {
		PRINTSCRN, ALT_ALT, CTRL_CTRL, SHIFT_SHIFT, MOUSE
	}

	final static Logger log = Logger.getLogger(DevicesMethods.class);

	public String getTranmitterResolution(String transmitterIp) {
		int counter = 0;
		String res = "";
		Ssh txDevice = new Ssh("root", "barrow1admin_12", transmitterIp);
		while (counter < 5) {
			try {
				txDevice.loginToServer();
				res = txDevice.sendCommand("capture_layer_cli -d1");
				counter = 5;
			} catch (Exception e) {
				log.info("Could not log into server. Trying again");
				counter++;
			}
		}

		log.info(res);
		String[] resSplit = res.split("Detected resolution is ");

		String[] resSplit2 = resSplit[1].split("\\s+");
		res = resSplit2[0];
		txDevice.disconnect();
		return res;
	}

	public void setSourceResolution(String sourceIp, String user, String pass, String resolution) {
		int counter = 0;
		Ssh sourcePc = new Ssh(user, pass, sourceIp);

		while (counter < 5) {
			try {
				sourcePc.loginToServer();
				sourcePc.sendCommand("echo " + resolution + " >> autoLogfile.log");
				sourcePc.sendCommand("xrandr -d :0 -s " + resolution);

				counter = 5;
			} catch (Exception e) {
				log.info("Error logging in. Retry");
				counter++;
			}
		}
		sourcePc.disconnect();

	}

	// convert TX settings to deivce rest api values. Incomplete
	public int[] convertTXSingle(String videoQ, String videoOpt, String hid, String mouseT, String edid1) {
		int[] values = new int[5];
		switch (videoQ) {
		case "Default":
			values[0] = 2;
			break;
		case "Best Quality":
			values[0] = 0;
			break;
		case "Best Compression":
			values[0] = 4;
			break;
		case "2":
			values[0] = 1;
			break;
		case "4":
			values[0] = 3;
			break;
		}

		switch (videoOpt) {
		case "Off":
			values[1] = 0;
			break;
		case "DVI Optimised":
			values[1] = 1;
			break;
		case "VGA - High Performance":
			values[1] = 2;
			break;
		case "VGA - Optimised":
			values[1] = 3;
			break;
		case "VGA - Low Bandwidth":
			values[1] = 4;
			break;
		}

		switch (hid) {
		case "Default":
			values[2] = 0;
			break;
		case "Basic":
			values[2] = 1;
			break;
		case "MAC":
			values[2] = 2;
			break;
		case "Absolute":
			values[2] = 3;
			break;
		}

		switch (mouseT) {
		case "0":
			values[3] = 0;
			break;
		case "1":
			values[3] = 1;
			break;
		case "2":
			values[3] = 2;
			break;
		case "3":
			values[3] = 3;
			break;
		case "4":
			values[3] = 4;
			break;
		}
		return values;
	}

	/**
	 * Will ssh into boxilla and run 3 curl commands that replicate the logging in
	 * of an AD user on the device matching the mac passed in
	 * 
	 * @param username
	 * @param password
	 * @param mac
	 * @param boxillaIP
	 * @return
	 * @throws InterruptedException
	 */
	public String logInADUser(String username, String password, String mac, String boxillaIP, String deviceIp)
			throws InterruptedException {
		log.info("Using curl to log AD user into device and return the datatbase");
		Ssh ssh = new Ssh(StartupTestCase.boxillaUsername, StartupTestCase.boxillaPassword, boxillaIP);
		ssh.loginToServer();

		// curl commands to run on boxilla
		String loginCurl = "curl -i -v -X POST -H \"Content-Type: application/json\" -H \"Accept: application/json\" --data '{\"username\":\""
				+ username + "\", \"password\":\"" + password + "\", \"mac\":\"" + mac + "\"}' -u admin:admin https://"
				+ boxillaIP + "/api/ad/v1/user/login";
		String statusCurl = "curl -i -v -X GET -H \"Content-Type: application/json\" -H \"Accept: application/json\" --data '{\"username\":\""
				+ username + "\", \"mac\":\"" + mac + "\"}' -u admin:admin https://" + boxillaIP
				+ "/api/ad/v1/user/auth_status";
		String completeCurl = "curl -i -v -X POST -H \"Content-Type: application/json\" -H \"Accept: application/json\" --data '{\"username\":\""
				+ username + "\", \"mac\":\"" + mac + "\"}' -u admin:admin https://" + boxillaIP
				+ "/api/ad/v1/user/auth_complete";

		log.info("loginCurl:" + loginCurl);
		log.info("statusCurl:" + statusCurl);
		log.info("completeCurl:" + completeCurl);

		ssh.sendCommand(loginCurl);
		Thread.sleep(2000);
		ssh.sendCommand(statusCurl);
		Thread.sleep(2000);
		ssh.sendCommand(completeCurl);
		ssh.disconnect();

		log.info("Login complete. Getting CloudData");
		Ssh sshDevice = new Ssh(StartupTestCase.deviceUserName, StartupTestCase.devicePassword, deviceIp);
		sshDevice.loginToServer();
		String cloudData = sshDevice.sendCommand("cat /usr/local/gui_files/CloudDataA.xml");
		return cloudData;
	}

	public void setMulticastPortandIp(WebDriver driver, String deviceIp, String multicastIp)
			throws InterruptedException {
		navigateToOptions(driver, deviceIp);
		SeleniumActions.seleniumClick(driver, Devices.edit);
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(By.xpath(Devices.applyBtn)));

		SeleniumActions.seleniumSendKeysClear(driver, Devices.multiCastIpTextBox);
		SeleniumActions.seleniumSendKeys(driver, Devices.multiCastIpTextBox, multicastIp);

	}

	/**
	 * Retuns the value of the specified column. Options are Video Quality, Video
	 * Source, EDID1, EDID2, HID, Mouse Timeout.
	 * 
	 * @param driver
	 * @param ipAddress
	 * @param setting
	 * @return
	 * @throws InterruptedException
	 */
	public String getSettingDataFromTable(WebDriver driver, String ipAddress, String setting)
			throws InterruptedException {
		navigateToDeviceStatus(driver);
		String value = "";
		if (setting.equals("HID") || setting.equals("Mouse Timeout")) {
			SeleniumActions.seleniumClick(driver, Devices.getMiscSettingsXpath());
			timer(driver);
		}
		SeleniumActions.seleniumSendKeys(driver, Devices.deviceStatusSearchBox, ipAddress);
		switch (setting) {
		case "Video Quality":
			value = Devices.getDeviceTableSetting(driver, "7").getText();
			break;
		case "Video Source":
			value = Devices.getDeviceTableSetting(driver, "8").getText();
			break;
		case "EDID1":
			value = Devices.getDeviceTableSetting(driver, "9").getText();
			break;
		case "EDID2":
			value = Devices.getDeviceTableSetting(driver, "10").getText();
			break;
		case "HID":
			value = Devices.getDeviceMiscTableSetting(driver, "7").getText();
			break;
		case "Mouse Timeout":
			value = Devices.getDeviceMiscTableSetting(driver, "8").getText();
			break;
		}

		return value;
	}

	public void interfacesDownUp(String ip, String username, String password, String networkInterface, String sleepTime)
			throws InterruptedException {

		log.info(
				"Attempting to bring " + networkInterface + " down for " + sleepTime + " seconds and bring it back up");
		ScpTo scp = new ScpTo();
		scp.scpTo("interfaceDownUp.sh", username, ip, password, "/usr/local/", "interfaceDownUp.sh");
		Ssh shell = new Ssh(username, password, ip);
		shell.loginToServer();
		shell.sendCommand("chmod 777 /usr/local/interfaceDownUp.sh");
		shell.sendCommand("dos2unix /usr/local/interfaceDownUp.sh");
		shell.sendCommandNoReturn("nohup /usr/local/interfaceDownUp.sh " + networkInterface + " " + sleepTime + " &");
		shell.disconnect();
		log.info("script finished");
	}

	public void removeInterfaceFile(String ip, String password, String username) {
		log.info("attempting to remove /usr/local/interfaceDownUp.sh");
		Ssh ssh = new Ssh(username, password, ip);
		ssh.loginToServer();
		ssh.sendCommand("rm /usr/local/interfaceDownUp.sh");
		ssh.disconnect();
		log.info("/usr/local/interfaceDownUp.sh removed");
	}

	public void navigateToEmeraldUpgrade(WebDriver driver) throws InterruptedException {
		log.info("Navigating to upgrade emerald device");
		Landingpage.devicesTab(driver).click();
		timer(driver);
		Landingpage.devicesUpgrades(driver).click();
		SeleniumActions.seleniumClick(driver, Devices.releasesTab);
		// Devices.releasesTab(driver).click();
		Devices.emeraldTab(driver).click();
		log.info("Successfully navigated to upgrade emerald device");
	}

	/**
	 * Reboots device through SSH
	 * 
	 * SSH's into a device and runs the reboot command. Sleeps for 100 seconds while
	 * device comes back online
	 * 
	 * @param ip
	 *            The IP address of the device
	 * @param userName
	 *            Login username for the device
	 * @param password
	 *            Login password for the device
	 * 
	 * @throws InterruptedException
	 */
	public void rebootDeviceSSH(String ip, String userName, String password, long millSeconds)
			throws InterruptedException {
		Ssh shell = new Ssh(userName, password, ip);
		int retry = 0;
		while (retry < 5) {
			try {
				shell.loginToServer();
				retry = 6;
			} catch (Exception e) {
				e.printStackTrace();
				log.info("SSH login failed. Retrying");
				Thread.sleep(5000);
				retry++;
			}
		}
		String command = "reboot";
		if (StartupTestCase.isEmerald)
			command = "/sbin/reboot";
		shell.sendCommand(command);
		log.info("Waiting while device reboots");
		shell.disconnect();

		checkReboot(ip, userName, password);
	}

	public void checkReboot(String ip, String userName, String password) throws InterruptedException, AssertionError {
		Ssh shell = new Ssh(userName, password, ip);
		// first ping to make sure device has gone off line
		SystemMethods sysMethods = new SystemMethods();
		int deviceOffline = 0;
		while (sysMethods.pingIpAddress(ip) && deviceOffline < 50) {
			log.info("device has not gone offline yet.. rechceking:" + deviceOffline);
			deviceOffline++;
			Thread.sleep(2000);
		}
		if (!sysMethods.pingIpAddress(ip)) {
			log.info("device has gone offline");
		}

		int counter = 0;
		boolean isUp = false;
		while (!sysMethods.pingIpAddress(ip) && counter < 10) {
			log.info("device not up yet. Checking again");
			counter++;
		}
		if (counter >= 10) {
			log.info("DEVICE WAS NOT PINGABLE. MAY CAUSE TEST ERRORS PLEASE CHECK");
			// throw new AssertionError("Device did not come online");
		}
		counter = 0;
		Ssh shell2 = null;
		if (sysMethods.pingIpAddress(ip)) {
			log.info("Device is pingable. Checking for webservice");
			Thread.sleep(5000);
			boolean isLoggedIn = false;
			int loginCounter = 0;
			while (!isLoggedIn && loginCounter < 5) {
				shell2 = new Ssh(userName, password, ip);
				isLoggedIn = shell2.loginToServer();
				loginCounter++;
				Thread.sleep(2000);
			}
			boolean isWebService = false;
			while (!isWebService && counter < 30) {
				String output = shell2.sendCommand("ps -ax");
				if (output.contains("/usr/bin/webservice")) {
					log.info("WebSerivce is running. Device full rebooted");
					isWebService = true;
				} else {
					counter++;
					log.info("WebService is not running. checking again..");
					Thread.sleep(1000);
				}
			}
		}
		if (counter > 60) {
			log.info("ERROR... WEBSERVICE DID NOT START. MAY CAUSE TEST FAILS. PLEASE CHECK");
			// throw new AssertionError("Device came online but webservice did not start");
		}
		shell2.disconnect();
	}

	public static void main(String[] args) {
		int counter = 0;
		while (true && counter < 10) {
			log.info("counter:" + counter);
			counter++;
		}
	}

	/**
	 * Pushes the appliance database (CloudData.xml) from boxilla to appliances
	 * 
	 * @throws InterruptedException
	 */
	public void recreateCloudData(String rxIp, String txIp) throws InterruptedException {
		Ssh shell = new Ssh(StartupTestCase.deviceUserName, StartupTestCase.devicePassword, rxIp);
		shell.loginToServer();
		String command = "";
		String check = "cat /opt/blackbox/startgui.sh";
		check = shell.sendCommand(check);
		log.info("Check:" + check);
		if (!check.contains("source /opt/blackbox/script_output.sh")) {
			command = "/opt/cloudium/startgui.sh&";
		} else {
			command = "/opt/blackbox/startgui.sh&";
		}
		String output = shell.sendCommand(command);
		log.info("Output from startgui: " + output);
		Thread.sleep(10000);

		shell.disconnect();

		// Ssh shell2 = new Ssh("root", "barrow1admin_12", "10.211.129.246");
		// shell2.loginToServer();
		// String output2 = shell2.sendCommand("/opt/cloudium/startgui.sh&");
		// Thread.sleep(20000);
		// shell2.disconnect();

		// System.out.println(output);
		// String file = "C:\\temp\\CloudDataA.xml";
		// Utilities.writeStringToFile(output, file);
		//
		// ScpTo scp = new ScpTo();
		// scp.scpTo(file, "root", "10.211.128.155", "barrow1admin_12",
		// "/usr/local/gui_files/", "CloudDataA.xml");
		// scp.scpTo(file, "root", "10.211.128.155", "barrow1admin_12",
		// "/usr/local/gui_files/", "CloudDataB.xml");
		// scp.scpTo(file, "root", "10.211.128.156", "barrow1admin_12",
		// "/usr/local/gui_files/", "CloudDataA.xml");
		// scp.scpTo(file, "root", "10.211.128.156", "barrow1admin_12",
		// "/usr/local/gui_files/", "CloudDataB.xml");
		// scp.scpTo(file, "root", "10.211.129.245", "barrow1admin_12",
		// "/usr/local/gui_files/", "CloudDataA.xml");
		// scp.scpTo(file, "root", "10.211.129.245", "barrow1admin_12",
		// "/usr/local/gui_files/", "CloudDataB.xml");
		// scp.scpTo(file, "root", "10.211.129.246", "barrow1admin_12",
		// "/usr/local/gui_files/", "CloudDataA.xml");
		// scp.scpTo(file, "root", "10.211.129.246", "barrow1admin_12",
		// "/usr/local/gui_files/", "CloudDataB.xml");

	}

	public void recreateCloudData(String rxIp, String txIp, boolean force) throws InterruptedException {
		Ssh shell = new Ssh(StartupTestCase.deviceUserName, StartupTestCase.devicePassword, rxIp);
		shell.loginToServer();
		String command = "";
		String check = "cat /opt/blackbox/startgui.sh";

		command = "/opt/blackbox/startgui.sh&";

		String output = shell.sendCommand(command);
		log.info("Output from startgui: " + output);
		Thread.sleep(10000);

		shell.disconnect();
	}

	public String getDeviceTypeForUpgrade(WebDriver driver, String deviceIp) {
		navigateToDeviceStatus(driver);
		SeleniumActions.seleniumSendKeys(driver, Devices.deviceStatusSearchBox, deviceIp);
		String out = SeleniumActions.seleniumGetText(driver, Devices.applianceTable);
		if (out.contains("EMD2000SE-R") || out.contains("EMD2002SE-R")) {
			log.info("Device is a Single or Dual head SE Receiver");
			return "SE-R";
		} else if (out.contains("EMD200DV-T") || out.contains("EMD200DV-T")) {
			log.info("Device is a ZeroU Transmitter");
			return "ZEROU";
		} else if (out.contains("DTX1002-T") || out.contains("DTX1000-T")) { /// need to add other types of corrib TX
																				/// devices
			log.info("Device is a DTX Transmitter");
			return "DTX-T";
		} else if (out.contains("EMD2002PE-R") || out.contains("EMD2000PE-R")) {
			log.info("Device is a PE receiver");
			return "PE-R";
		} else if (out.contains("EMD2000SE-T") || out.contains("EMD2002SE-T")) {
			log.info("Device is a SE Transmitter");
			return "SE-T";
		} else if (out.contains("EMD2002PE-T") || out.contains("EMD2000PE-T")) {
			log.info("Deivce is PE transmitter");
			return "PE-T";
		} else if (out.contains("DTX1000-R") || out.contains("DTX1002-R")) {
			log.info("DEvice is a DTX Receiver");
			return "DTX-R";
		} else if (out.contains("EMD4000T")) {
			log.info("Device is a 4K Transmitter");
			return "4K-T";
		} else if (out.contains("EMD4000R")) {
			log.info("Device is 4K Receiver");
			return "4K-R";
		} else {
			log.info("Device type is not recoginsied");
			throw new AssertionError("Device type is not recoginsied:" + out);
		}
	}

	/**
	 * Pushes the appliance database (CloudData.xml) from boxilla to appliances
	 * 
	 * @throws InterruptedException
	 */
	public String recreateCloudData(String rxIp) throws InterruptedException {
		Ssh shell = new Ssh(StartupTestCase.deviceUserName, StartupTestCase.devicePassword, rxIp);
		shell.loginToServer();

		String check = shell.sendCommand("cat /opt/blackbox/startgui.sh");
		log.info("Check:" + check);

		Thread t1 = new Thread(new Runnable() {
			public void run() {
				String command = "";
				if (!check.contains("function")) {
					command = "/opt/cloudium/startgui.sh&";
				} else {
					command = "/opt/blackbox/startgui.sh&";
				}
				String output = shell.sendCommand(command);
			}
		});
		t1.start();

		Thread.sleep(10000);
		shell.disconnect();
		shell.loginToServer();
		String out = shell.sendCommand("cat /usr/local/gui_files/CloudDataA.xml");
		shell.disconnect();
		return out;

	}

	public void timer(WebDriver driver) throws InterruptedException { // Method for thread sleep
		Thread.sleep(5000);
		// driver.manage().timeouts().implicitlyWait(StartupTestCase.getWaitTime(),
		// TimeUnit.SECONDS);
	}

	/**
	 * Noavigates to the device bulk update modal. Waits until the save button is
	 * present
	 * 
	 * @param driver
	 * @throws InterruptedException
	 */
	public void navigateToBulkUpdate(WebDriver driver) throws InterruptedException {
		log.info("Navigating to Bulk Update Settings Modal..");
		navigateToDeviceStatus(driver);
		int counter = 0;
		while (counter < 6) {
			try {
				Devices.bulkUpdateBtn(driver).click();
				new WebDriverWait(driver, 10)
						.until(ExpectedConditions.elementToBeClickable(By.xpath(Devices.getBulkUpdateSaveBtnXpath())));
				log.info("Bulk update model appeared");
				counter = 6;
			} catch (Exception e) {
				log.info("*****************************");
				log.info("Bulk update modal did not appear. Retrying:" + counter++);
			}

		}

	}

	public void checkStatusConfigured(WebDriver driver, String deviceIp) throws InterruptedException {
		log.info("Attempting to check device status Configured for device " + deviceIp);
		navigateToDeviceStatus(driver);
		SeleniumActions.seleniumSendKeys(driver, Devices.deviceStatusSearchBox, deviceIp);
		String deviceDetails = SeleniumActions.seleniumGetText(driver, Devices.applianceTable);
		log.info("Table details:" + deviceDetails);
		if (!deviceDetails.contains("Configured") && !deviceDetails.contains("configured")) {
			log.info("Device was not configured. Waiting one minute and trying again");
			Thread.sleep(60000);
			driver.navigate().refresh();
			SeleniumActions.seleniumSendKeys(driver, Devices.deviceStatusSearchBox, deviceIp);
			deviceDetails = SeleniumActions.seleniumGetText(driver, Devices.applianceTable);
		}

		Assert.assertTrue(deviceDetails.contains("Configured") || deviceDetails.contains("configured"),
				"Device table did not contain Configured, actual: " + deviceDetails);
		log.info("Device is configured");
	}

	public boolean checkHttpEnabled(WebDriver driver, String deviceIp) throws InterruptedException {
		log.info("Checking if http is enabled for device  " + deviceIp);
		navigateToDeviceStatus(driver);
		SeleniumActions.seleniumClick(driver, Devices.getMiscSettingsXpath());
		SeleniumActions.seleniumSendKeys(driver, Devices.deviceStatusSearchBox, deviceIp);
		String status = SeleniumActions.seleniumGetText(driver, Devices.httpEnabledStatus);
		// String status = Devices.httpEnabledElement(driver).getText();
		log.info("status is " + status);
		if (status.contains("Enabled")) {
			return true;
		} else {
			return false;
		}
	}

	public void changedHttpToEnabled(WebDriver driver, String deviceIp) throws InterruptedException {
		try {
			log.info("Attempting to change http to enabled for device " + deviceIp);
			checkStatusConfigured(driver, deviceIp);
			boolean isEnabled = checkHttpEnabled(driver, deviceIp);
			if (!isEnabled) {
				log.info("HTTP is not enabled. Enabling");
				setUniquePropertyRx(driver, deviceIp, "HTTP Enabled", true);
				log.info("HTTP has been enabled. Pausing for device to configure");
				Thread.sleep(60000);
			} else {
				log.info("HTTP Enabled is already enabled.");
			}
		} catch (Exception e) {
			log.info("Error changing HTTP to enabled");
		}
	}

	/**
	 * Method to select devices for bulk update.
	 * 
	 * Will navigate to the bulk update modal. Select the passed in device type and
	 * template name. The name of each device to be updated should be added to an
	 * array and passed in.
	 * 
	 * @param driver
	 * @param deviceType
	 * @param templateName
	 * @param deviceList
	 * @throws InterruptedException
	 */
	public void bulkUpdate(WebDriver driver, String deviceType, String templateName, String[] deviceList)
			throws InterruptedException {
		log.info("Attempting bulk update");
		navigateToBulkUpdate(driver);
		Devices.bulkUpdateApplianceTypeDropdown(driver, deviceType);
		SeleniumActions.seleniumDropdown(driver, Devices.bulkUpdateTemplateNameDropdownXpath, templateName);
		// Devices.bulkUpdateTemplateNameDropdown(driver, templateName);
		for (String s : deviceList) {
			log.info("Selecting device: " + s);
			timer(driver);
			Devices.bulkUpdateSearchBox(driver).sendKeys(s);
			timer(driver);
			SeleniumActions.seleniumClick(driver, Devices.getBulkUpdateDeviceCheckboxXpath());
			Devices.bulkUpdateSearchBox(driver).clear();
		}
		Devices.bulkUpdateSearchBox(driver).clear();
		Devices.bulkUpdateSearchBox(driver).sendKeys(Keys.RETURN); /// ONLY NEEDED UNTIL BUG IS FIXED
		// save
		SeleniumActions.seleniumClick(driver, Devices.getBulkUpdateSaveBtnXpath());
		try {
			Alert alert = driver.switchTo().alert();
			alert.accept();
		} catch (Exception e) {
			log.info("No alert popup");
		}
		timer(driver);
		String message = Devices.getDeviceToastMessage(driver).getText();
		Assert.assertTrue(message.contains("Success"), "Toast did not contain success, actual " + message);
		// SeleniumActions.seleniumClick(driver, Devices.getBulkUpdateCancelBtnXpath());
		log.info("Bulk update initiated");

	}

	/**
	 * Sets a single unique property for a receiver device
	 * 
	 * @param driver
	 * @param ipAddress
	 * @param property
	 * @param value
	 * @throws InterruptedException
	 */
	public void setUniquePropertyRx(WebDriver driver, String ipAddress, String property, boolean value)
			throws InterruptedException {
		log.info("Attempting to set unique property " + property + " for RX device: " + ipAddress);
		navigateToEditSettingsRx(driver, ipAddress);
		Devices.editRxSettingsSettingDropdown(driver, "Unique");
		timer(driver);

		switch (property) {
		case "Power Mode":
			Devices.uniquePowerModeDropdown(driver, value);
			break;
		case "HTTP Enabled":
			Devices.uniqueHttpEnabledDropdown(driver, value);
			break;
		}
		saveEditRxSettings(driver); // done
		log.info("Unique property set");

	}

	/**
	 * Sets a single unique property for a transmitter device
	 * 
	 * @param driver
	 * @param ipAddress
	 * @param property
	 * @param value
	 * @throws InterruptedException
	 */
	public void setUniquePropertyTx(WebDriver driver, String ipAddress, String property, String value)
			throws InterruptedException {
		log.info("Attempting to set unique property " + property + " for TX device: " + ipAddress);
		navigateToEditSettingsTx(driver, ipAddress);
		Devices.editTxSettingsSettingDropdown(driver, "Unique");
		timer(driver);

		switch (property) {
		case "Video Quality":
			Devices.uniqueVideoQualityDropdown(driver, value);
			break;
		case "Video Source":
			Devices.uniqueVideoSourceDropdown(driver, value);
			break;
		case "HID":
			Devices.uniqueHidDropdown(driver, value);
			break;
		case "Audio":
			Devices.uniqueAudioDropdown(driver, value);
			break;
		case "Mouse Timeout":
			Devices.uniqueMouseTimeoutDropdown(driver, value);
			break;
		case "EDID1":
			Devices.uniqueEdid1Dropdown(driver, value);
			break;
		case "EDID2":
			Devices.uniqueEdid2Dropdown(driver, value);
			break;
		}
		saveEditTxSettings(driver); /// done
		log.info("Unique property set");
	}

	/**
	 * Bring you to the device status page
	 * 
	 * @param driver
	 * @throws InterruptedException
	 */
	public void navigateToDeviceStatus(WebDriver driver) {
		log.info("Attempting to navigate to device status");
		SeleniumActions.seleniumClick(driver, "//span[contains(.,'Devices')]");
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(Landingpage.devicesStatus(driver)));
		Landingpage.devicesStatus(driver).click();
		// check if page appeared
		new WebDriverWait(driver, 60)
				.until(ExpectedConditions.presenceOfElementLocated(By.xpath(Devices.getsystemPropertiesButtonXpath())));
		log.info("Successfully navigated to device status");
	}

	/**
	 * Edit the template for a receiver. Template name is passed along the two
	 * booleans for dropdowns with 2 options each
	 * 
	 * @param driver
	 * @param templateName
	 * @param isManual
	 * @param isEnabled
	 * @throws InterruptedException
	 */
	// done
	public void editTemplateReceiver(WebDriver driver, String templateName, boolean isManual, boolean isEnabled)
			throws InterruptedException {
		log.info("Attempting to edit receiver template " + templateName);
		navigateToEditTemplate(driver);
		Devices.templateNameDropdown(driver, templateName);
		Devices.editTemplatePowerModeDropdown(driver, false);
		Devices.editTemplateHttpEnabledDropdown(driver, true);
		timer(driver);
		Devices.editTemplateRxSaveBtn(driver).click();
		Alert alert = driver.switchTo().alert();
		alert.accept();
		// assert the template saved
		timer(driver);
		String message = Devices.getDeviceToastMessage(driver).getText();
		Assert.assertTrue(message.contains("Success"), "The toast message did not contain success, actual " + message);
		log.info("Successsfully edited RX template");
	}

	/**
	 * Navigates to the delete template modal
	 * 
	 * @param driver
	 * @throws InterruptedException
	 */
	public void navigateToDeleteTemplate(WebDriver driver) throws InterruptedException {
		log.info("Attempting to navigate to delete template");
		navigateToDeviceStatus(driver);
		Devices.deleteTemplate(driver).click();
		new WebDriverWait(driver, 60)
				.until(ExpectedConditions.elementToBeClickable(Devices.deleteTemplateDeleteBtn(driver)));
		log.info("Successfully navigated to delete template");

	}

	/**
	 * deletes the template with the passed in name
	 * 
	 * @param driver
	 * @param templateName
	 * @throws InterruptedException
	 */
	public void deleteTemplate(WebDriver driver, String templateName) throws InterruptedException {
		log.info("Attempting to delete template " + templateName);
		navigateToDeleteTemplate(driver);
		Devices.deleteTemplateTemplateNameDropdown(driver, templateName);
		timer(driver);
		Devices.deleteTemplateDeleteBtn(driver).click();
		Alert alert = driver.switchTo().alert();
		alert.accept();
		timer(driver);
		// assert toast message
		String message = Devices.getDeviceToastMessage(driver).getText();
		Assert.assertTrue(message.contains("Success"), "Toas message did not contain Success, actual " + message);
		timer(driver);
		log.info("Successfully deleted template " + templateName);
		// SeleniumActions.seleniumClick(driver, Devices.getdeleteTemplateCancelBtn());
	}

	/**
	 * Edit a template for a TX device. Template name is passed and any drop down
	 * values to be changed
	 * 
	 * @param driver
	 * @param templateName
	 * @param videoQuality
	 * @param videoSource
	 * @param hidConfig
	 * @param audio
	 * @param mouseTimeout
	 * @param edidDvi1
	 * @param edidDvi2
	 * @throws InterruptedException
	 */
	public void editTemplateTransmitter(WebDriver driver, String templateName, String videoQuality, String videoSource,
			String hidConfig, String audio, String mouseTimeout, String edidDvi1, String edidDvi2)
			throws InterruptedException {
		log.info("Attempting to edit TX template " + templateName);
		navigateToEditTemplate(driver);
		Devices.editTemplateTemplateNameDropdown(driver, templateName);
		Devices.editTemplateVideoQualityDropDown(driver, videoQuality);
		Devices.editTemplateVideoSourceDropdown(driver, videoSource);
		Devices.editTemplateHidConfigurationDropdown(driver, hidConfig);
		// Devices.editTemplateAudioDropdown(driver, audio);
		Devices.editTemplateMouseTimeoutDropdown(driver, mouseTimeout);
		Devices.editTemplateEdidSettingsDvi1Dropdown(driver, edidDvi1);
		Devices.editTemplateEdidSettingsDvi2Dropdown(driver, edidDvi2);
		Devices.editTemplateTxSaveBtn(driver).click();
		Alert alert = driver.switchTo().alert();
		alert.accept();
		timer(driver);
		// assert the toast message
		String message = Devices.getDeviceToastMessage(driver).getText();
		Assert.assertTrue(message.contains("Success"), "The toast message did not contain success, actual " + message);
		log.info("Successfully edited " + templateName);
		// SeleniumActions.seleniumClick(driver, Devices.getEditTxCancelXpath());

	}

	/**
	 * Navigates to the device system properties
	 * 
	 * @param driver
	 * @throws InterruptedException
	 */
	public void navigateToSystemProperties(WebDriver driver) throws InterruptedException {
		log.info("Attempting to navigate to System properties");
		navigateToDeviceStatus(driver);
		int counter = 0;
		while (counter < 5) {
			try {
				Devices.systemPropertiesBtn(driver).click();
				// check it modal appeared
				new WebDriverWait(driver, 10).until(ExpectedConditions.elementToBeClickable(Devices.spSaveBtn(driver)));
				log.info("Navigated to System properties");
				counter = 6;
			} catch (Exception e) {
				log.info("*******************************************************************************");
				log.info("System properties modal did not open retrying:" + counter++);

			}
		}

	}

	/**
	 * Navigate to the device template edit page
	 * 
	 * @param driver
	 * @throws InterruptedException
	 */
	public void navigateToEditTemplate(WebDriver driver) throws InterruptedException {
		log.info("Attempting to navigate to System properties");
		navigateToDeviceStatus(driver);

		Devices.editTemplateBtn(driver).click();
		// new WebDriverWait(driver,
		// 60).until(ExpectedConditions.presenceOfElementLocated(By.xpath(Devices.getTemplateNameDropdownXpath())));
		log.info("Successfully navigated to System properties");
	}

	/**
	 * Adds a template for a receiver. Pass in the template name and the options.
	 * Receiver only has 2 options with 2 optioons in each dropdown so a boolean is
	 * used
	 * 
	 * @param driver
	 * @param templateName
	 * @param powerMode
	 * @param isHttpEnabled
	 * @throws InterruptedException
	 */
	public void addTemplateReceiver(WebDriver driver, String templateName, boolean powerMode, boolean isHttpEnabled)
			throws InterruptedException {
		log.info("Attemprting to add receiver template: " + templateName);
		navigateToDevicePropertyCreateTemplate(driver);
		Devices.applianceTypeDropdown(driver, true);
		Devices.templateNameTextBox(driver).sendKeys(templateName);
		Devices.PowerModeDropdown(driver, powerMode);
		Devices.HttpEnabledDropdown(driver, isHttpEnabled);
		timer(driver);
		Devices.saveTemplateRxBtn(driver).click();
		timer(driver);

		// assert template is created
		String message = Devices.getDeviceToastMessage(driver).getText();
		Assert.assertTrue(message.contains("Success"), "Toast message did not contain success, actual " + message);
		// SeleniumActions.seleniumClick(driver, Devices.getEditRxCancelXpath());
		log.info("Successfully added template for receiver");

	}

	/**
	 * Adds a template for a TX device. Pass in the template name and values from
	 * the drop down. Clicks save and asserts the toast message displays success
	 * 
	 * @param driver
	 * @param templateName
	 * @param videoQuality
	 * @param videoSource
	 * @param hidConfig
	 * @param audio
	 * @param mouseTimeout
	 * @param edidDvi1
	 * @param edidDvi2
	 * @throws InterruptedException
	 */
	public void addTemplateTransmitter(WebDriver driver, String templateName, String videoQuality, String videoSource,
			String hidConfig, String audio, String mouseTimeout, String edidDvi1, String edidDvi2)
			throws InterruptedException {
		log.info("Attempting to add a template for transmitter: " + templateName);
		navigateToDevicePropertyCreateTemplate(driver);
		Devices.applianceTypeDropdown(driver, false);
		Devices.templateNameTextBox(driver).sendKeys(templateName);
		// Devices.videoQualityDropdown(driver, videoQuality);
		SeleniumActions.seleniumDropdown(driver, Devices.videoQualityDropdownXpath, videoQuality);
		Devices.videoSourceDropdown(driver, videoSource);
		Devices.HIDConfigurationDropdown(driver, hidConfig);
		// Devices.audioDropdown(driver, audio);
		Devices.mouseTimeoutDropdown(driver, mouseTimeout);
		Devices.EdidSettingsDvi1Dropdown(driver, edidDvi1);
		Devices.EdidSettingsDvi2Dropdown(driver, edidDvi2);
		timer(driver);
		Devices.saveTemplateTxBtn(driver).click();
		timer(driver);

		// assert template is created
		String message = Devices.getDeviceToastMessage(driver).getText();
		Assert.assertTrue(message.contains("Success"), "Toast message did not contain success, actual " + message);
		// SeleniumActions.seleniumClick(driver, Devices.getEditTxCancelXpath());
		timer(driver);
		log.info("Successfully added template for transmitter");
	}

	/**
	 * Navigate to device add template
	 * 
	 * @param driver
	 * @throws InterruptedException
	 */
	public void navigateToDevicePropertyCreateTemplate(WebDriver driver) throws InterruptedException {
		log.info("Attempting to navigate to device create template");
		navigateToDeviceStatus(driver);
		int counter = 0;
		while (counter < 5) {
			try {
				new WebDriverWait(driver, 60)
						.until(ExpectedConditions.elementToBeClickable(Devices.addTemplateBtn(driver)));
				Devices.addTemplateBtn(driver).click();
				new WebDriverWait(driver, 60)
						.until(ExpectedConditions.elementToBeClickable(Devices.saveTemplateTxBtn(driver)));
				counter = 6;
			} catch (Exception e) {
				log.info("Issue with templte modal opening. Retrying");
				counter++;
				driver.navigate().refresh();
			}
		}
		log.info("Successfully navigated to device create template");
	}

	/**
	 * Sets all the device system properties, Most take in the text that is in the
	 * dropdown menu, dropdowns with 2 options take a boolean
	 * 
	 * @param driver
	 * @param videoQuality
	 * @param videoSource
	 * @param hidConfig
	 * @param audio
	 * @param mouseTimeout
	 * @param edidDvi1
	 * @param edidDvi2
	 * @param isPowerModeManual
	 * @param isHttpEnabled
	 * @throws InterruptedException
	 */
	public void setSystemProperties(WebDriver driver, String videoQuality, String videoSource, String hidConfig,
			String audio, String mouseTimeout, String edidDvi1, String edidDvi2, boolean isPowerModeManual,
			boolean isHttpEnabled) throws InterruptedException {
		log.info("Attempting to set device system properties");
		navigateToSystemProperties(driver);
		log.info("Setting the following properties, Video Quality:" + videoQuality + " Video Source:" + videoSource
				+ " HID:" + hidConfig + " Audio:" + audio + " Mouse Timeout:" + mouseTimeout + " EDID DVI1: " + edidDvi1
				+ " EDID DVI1:" + edidDvi1 + " Power Mode Manual:" + isPowerModeManual + " Is HTTP Enabled:"
				+ isHttpEnabled);

		// set properties
		timer(driver);
		Devices.spVideoQualityDropdown(driver, videoQuality);
		Devices.spVideoSourceDropdown(driver, videoSource);
		Devices.spHidConfigurationDropdown(driver, hidConfig);
		// Devices.spAudioDropdown(driver, audio);
		Devices.spMouseTimeoutDropdown(driver, mouseTimeout);
		Devices.spEdidDvi1Dropdown(driver, edidDvi1);
		Devices.spEdidDvi2Dropdown(driver, edidDvi2);
		Devices.spPowerModeDropdown(driver, isPowerModeManual);
		Devices.spHttpEnabledDropdown(driver, isHttpEnabled);
		saveSystemProperty(driver);
		timer(driver);
		log.info("Successfully set device system properties");

	}

	/**
	 * hits the save button on the system property modal and asserts the toast
	 * message
	 * 
	 * @param driver
	 * @throws InterruptedException
	 */
	public void saveSystemProperty(WebDriver driver) throws InterruptedException {
		log.info("Attempting to save device system properties");
		timer(driver);
		Devices.spSaveBtn(driver).click();
		try {
			Alert alert = driver.switchTo().alert();
			alert.accept();
		} catch (Exception e) {
			log.info("No TX settings changed so no alert pop up");
		}
		timer(driver);
		// assert if successful
		String message = Devices.getDeviceToastMessage(driver).getText();
		log.info("Pop up message: " + message);
		timer(driver);
		if (message.equals("Error"))
			SeleniumActions.seleniumClick(driver, Devices.getSpCancelBtnXpath());
		// Devices.spCancelBtn(driver).click(); ///ONLY NEEDED WHILE ON TEST BUILD
		timer(driver);
		log.info("Successfully saved device system properties");
	}

	/**
	 * Saves a system property for a receiver device
	 * 
	 * @param driver
	 * @throws InterruptedException
	 */
	public void saveSystemPropertyRX(WebDriver driver) throws InterruptedException {
		log.info("Attempting to save device system properties");
		timer(driver);
		Devices.spSaveBtn(driver).click();
		Alert alert = driver.switchTo().alert();
		alert.accept();
		timer(driver);
		// assert if successful
		String message = Devices.getDeviceToastMessage(driver).getText();
		log.info("Pop up message: " + message);
		timer(driver);
		if (message.equals("Error"))
			SeleniumActions.seleniumClick(driver, Devices.getSpCancelBtnXpath());
		// Devices.spCancelBtn(driver).click(); ///ONLY NEEDED WHILE ON TEST BUILD
		timer(driver);
		log.info("Successfully saved device system properties");
	}

	/**
	 * Saves RX settings after edit
	 * 
	 * @param driver
	 * @throws InterruptedException
	 */
	public void saveEditRxSettings(WebDriver driver) throws InterruptedException {
		log.info("Attempting to save RX device settings");
		timer(driver);
		SeleniumActions.seleniumClick(driver, Devices.getEditRxSaveBtnXpath());
		// Devices.editRxSaveBtn(driver).click();
		// assert if successful
		// Alert alert = driver.switchTo().alert();
		// alert.accept();
		timer(driver);
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Devices.getDeviceToastMessage(driver)));
		String message = Devices.getDeviceToastMessage(driver).getText();
		log.info("Pop up message: " + message);
		timer(driver);
		if (message.equals("Error"))
			SeleniumActions.seleniumClick(driver, Devices.getEditRxCancelXpath());

		timer(driver);
		log.info("Successfully saved RX device settings");
	}

	/**
	 * Saves TX settings after edit
	 * 
	 * @param driver
	 * @throws InterruptedException
	 */
	public void saveEditTxSettings(WebDriver driver) throws InterruptedException {
		log.info("Attempting to save transmitter properties");
		timer(driver);
		// Devices.editTxSaveBtn(driver).click();
		SeleniumActions.seleniumClick(driver, Devices.getEditTxSaveBtnXpath());
		// assert if successful

		Alert alert = driver.switchTo().alert();
		alert.accept();
		timer(driver);
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Devices.getDeviceToastMessage(driver)));
		String message = Devices.getDeviceToastMessage(driver).getText();
		log.info("Pop up message: " + message);
		timer(driver);
		if (message.equals("Error"))
			SeleniumActions.seleniumClick(driver, Devices.getEditTxCancelXpath());

		timer(driver);
		log.info("Successfully saved transmitter properties");
	}

	/**
	 * Used to change a single receiver system property Values for property are -
	 * Power Mode, HTTP Enabled. Setting is a boolean. For Power Mode true == Manual
	 * Fro HTTP Enabled true == Enabled
	 * 
	 * @param driver
	 * @param property
	 * @param setting
	 * @throws InterruptedException
	 */
	public void setSingleSystemPropertyReceiver(WebDriver driver, String property, boolean setting)
			throws InterruptedException {
		log.info("Attempting to set single RX device system property : " + property);
		navigateToSystemProperties(driver);
		timer(driver);
		switch (property) {
		case "Power Mode":
			Devices.spPowerModeDropdown(driver, setting);
			saveSystemPropertyRX(driver);
			break;

		case "HTTP Enabled":
			Devices.spHttpEnabledDropdown(driver, setting);
			saveSystemPropertyRX(driver);
			break;
		}
		log.info("Successfully set sing RX device system property");
	}

	/**
	 * Used to change a single transmitter system property. Values for property are
	 * - Video Quality, Video Source, HID Config, Audio, Mouse Timeout, EDID DVI1,
	 * EDID DVI2 Setting is the value of the drop down
	 * 
	 * @param driver
	 * @param property
	 * @param setting
	 * @throws InterruptedException
	 */
	public void setSingleSystemPropertyTransmitter(WebDriver driver, String property, String setting)
			throws InterruptedException {
		log.info("Attempting to changing single TX system property: " + property + " to " + setting);
		navigateToSystemProperties(driver);
		timer(driver);
		switch (property) {
		case "Video Quality":
			Devices.spVideoQualityDropdown(driver, setting);
			saveSystemProperty(driver);
			break;

		case "Video Source":
			Devices.spVideoSourceDropdown(driver, setting);
			saveSystemProperty(driver);
			break;

		case "HID Config":
			Devices.spHidConfigurationDropdown(driver, setting);
			saveSystemProperty(driver);
			break;

		case "Audio":
			Devices.spAudioDropdown(driver, setting);
			saveSystemProperty(driver);
			break;

		case "Mouse Timeout":
			Devices.spMouseTimeoutDropdown(driver, setting);
			saveSystemProperty(driver);
			break;

		case "EDID DVI1":
			Devices.spEdidDvi1Dropdown(driver, setting);
			saveSystemProperty(driver);
			break;

		case "EDID DVI2":
			Devices.spEdidDvi2Dropdown(driver, setting);
			saveSystemProperty(driver);
			break;
		}
		log.info("Successfully set single TX device system property");
	}

	/**
	 * Navigate from anywhere in boxilla to the device upgrade page
	 * 
	 * @param driver
	 * @throws InterruptedException
	 */
	public void navigateToUpgrade(WebDriver driver) throws InterruptedException {
		timer(driver);
		Landingpage.devicesTab(driver).click();
		log.info("Upgrading device : Devices dropdown clicked");
		timer(driver);
		int x = 0;
		while (x < 5) {
			try {
				Landingpage.devicesUpgrades(driver).click();
				x = 5;
			} catch (Exception e) {
				log.info("try again");
				x++;
			}
		}
		log.info("Upgrading device : Upgrades tab clicked");
		timer(driver);
		timer(driver);
		timer(driver);
		timer(driver);
		timer(driver);
		String title = driver.getTitle();
		Assert.assertTrue(title.contentEquals("Boxilla - Devices | Upgrades"),
				"Title did not equal: Boxilla - Devices | Upgrades, actual text: " + title);
		log.info("Upgrading device : Page title asserted");
	}

	// Uploading version for the appliance selected either RX or TX
	/**
	 * Upload a version of software for devices
	 * 
	 * @param driver
	 * @param appliance
	 * @param versionNumber
	 * @throws InterruptedException
	 */
	public void uploadVersion(WebDriver driver, String appliance, String versionNumber) throws InterruptedException {
		// if appliance is tx
		if (appliance.equalsIgnoreCase("tx")) {
			log.info("Uploading Version : Started");
			timer(driver);
			SeleniumActions.seleniumClick(driver, Devices.releasesTab);
			// Devices.releasesTab(driver).click();
			log.info("Uploading Version : Releases tab clicked");
			timer(driver);
			SeleniumActions.seleniumClick(driver, Devices.txTab);
			// Devices.txTab(driver).click();
			log.info("Uploading Version : DTX-T clciked ");
			timer(driver);
			SeleniumActions.seleniumSendKeys(driver, Devices.searchboxTX, versionNumber);
			// Devices.searchboxTX(driver).sendKeys(versionNumber);
			log.info("Uploading Version : Version number entered in search box");
			log.info("Uploading Version : Checking if Version is Uploaded");
			timer(driver);
			// Upload check
			if (SeleniumActions.seleniumGetText(driver, Devices.txTableVersionNumber).equalsIgnoreCase(versionNumber)) {
				// if
				// (Devices.txTableVersionNumber(driver).getText().equalsIgnoreCase(versionNumber))
				// {
				log.info("Uploading Version : Version already uploaded.. ");
				timer(driver);
				SeleniumActions.seleniumSendKeysClear(driver, Devices.searchboxTX);
				// Devices.searchboxTX(driver).clear();
				log.info("Uploading Version : Search box cleared");
			} else {
				log.info("Uploading Version : Version not found.. Uploading..");
				timer(driver);
				log.info("Uploading Version : Started");
				SeleniumActions.seleniumClick(driver, Devices.uploadBtn);
				// Devices.uploadBtn(driver).click();
				log.info("Uploading Version : Upload button clicked");
				timer(driver);
				SeleniumActions.seleniumSendKeys(driver, Devices.uploadElement,
						"C:\\Selenium\\Corrib_Version\\" + "TX" + "_DTX_" + versionNumber + ".clu");
				// Devices.uploadElement(driver)
				// .sendKeys("C:\\Selenium\\Corrib_Version\\" + "TX" + "_DTX_" + versionNumber +
				// ".clu");
				log.info("Uploading Version : Version Selected to upload");
				timer(driver);
				SeleniumActions.seleniumClick(driver, Devices.applyBtn);
				// Devices.applyBtn(driver).click();
				log.info("Uploading Version : Apply button clicked");
				// driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
				Thread.sleep(20000);
				String title = driver.getTitle();
				Assert.assertTrue(title.contentEquals("Boxilla - Devices | Upgrades"),
						"Title did not contain: Boxilla - Devices | Upgrades, actual text: " + title);
				log.info("Uploading Version : Version uploaded successfully.. Researching version");
				timer(driver);
				SeleniumActions.seleniumClick(driver, Devices.releasesTab);
				// Devices.releasesTab(driver).click();
				log.info("Uploading Version : Releases tab clicked");
				timer(driver);
				SeleniumActions.seleniumClick(driver, Devices.txTab);
				// Devices.txTab(driver).click();
				log.info("Uploading Version : DTX-T clciked ");
				timer(driver);
				SeleniumActions.seleniumSendKeys(driver, Devices.searchboxTX, versionNumber);
				// Devices.searchboxTX(driver).sendKeys(versionNumber);
				log.info("Uploading Version : Version number entered in search box");
				timer(driver);
				String txTable = SeleniumActions.seleniumGetText(driver, Devices.txTable);
				// String txTable = Devices.txTable(driver).getText();
				Assert.assertTrue(txTable.contains(versionNumber),
						"TX table did not contain: " + versionNumber + ", actual text:" + txTable);
				timer(driver);
				SeleniumActions.seleniumSendKeysClear(driver, Devices.searchboxTX);
				// Devices.searchboxTX(driver).clear();
				// Calling devices upgrade method to redirect to the Device > Upgrades
				log.info("Uploading Version : Found searched version.. Checking State");
			}
		} else if (appliance.equalsIgnoreCase("rx")) {
			log.info("Uploading Version : Started");
			timer(driver);
			SeleniumActions.seleniumClick(driver, Devices.releasesTab);
			// Devices.releasesTab(driver).click();
			log.info("Uploading Version : Releases tab clicked");
			timer(driver);
			SeleniumActions.seleniumClick(driver, Devices.rxTab);
			// Devices.rxTab(driver).click();
			log.info("Uploading Version : DTX-R clciked ");
			timer(driver);
			SeleniumActions.seleniumSendKeys(driver, Devices.searchboxRX, versionNumber);
			// Devices.searchboxRX(driver).sendKeys(versionNumber);
			log.info("Uploading Version : Version number entered in search box");
			log.info("Uploading Version : Checking if Version is Uploaded");
			timer(driver);
			// Upload check
			if (SeleniumActions.seleniumGetText(driver, Devices.rxTableVersionNumber).equalsIgnoreCase(versionNumber)) {
				// if
				// (Devices.rxTableVersionNumber(driver).getText().equalsIgnoreCase(versionNumber))
				// {
				log.info("Uploading Version : Version already uploaded.. ");
				timer(driver);
				SeleniumActions.seleniumSendKeysClear(driver, Devices.searchboxRX);
				// Devices.searchboxRX(driver).clear();
				log.info("Uploading Version : Search box cleared");
			} else {
				log.info("Uploading Version : Version not found.. Uploading..");
				timer(driver);
				log.info("Uploading Version : Started");
				SeleniumActions.seleniumClick(driver, Devices.uploadBtn);
				// Devices.uploadBtn(driver).click();
				log.info("Uploading Version : Upload button clicked");
				timer(driver);
				SeleniumActions.seleniumSendKeys(driver, Devices.uploadElement,
						"C:\\Selenium\\Corrib_Version\\" + "RX" + "_DTX_" + versionNumber + ".clu");
				// Devices.uploadElement(driver)
				// .sendKeys("C:\\Selenium\\Corrib_Version\\" + "RX" + "_DTX_" + versionNumber +
				// ".clu");
				log.info("Uploading Version : Version Selected to upload");
				timer(driver);
				SeleniumActions.seleniumClick(driver, Devices.applyBtn);
				// Devices.applyBtn(driver).click();
				log.info("Uploading Version : Apply button clicked");
				driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
				String title = driver.getTitle();
				Assert.assertTrue(title.contentEquals("Boxilla - Devices | Upgrades"),
						"Title did not equal: Boxilla - Devices | Upgrades, actual text: " + title);
				log.info("Uploading Version : Version uploaded successfully.. Researching version");
				timer(driver);
				SeleniumActions.seleniumClick(driver, Devices.releasesTab);
				// Devices.releasesTab(driver).click();
				log.info("Uploading Version : Releases tab clicked");
				timer(driver);
				SeleniumActions.seleniumClick(driver, Devices.rxTab);
				// Devices.rxTab(driver).click();
				log.info("Uploading Version : DTX-R clciked ");
				timer(driver);
				SeleniumActions.seleniumSendKeys(driver, Devices.searchboxRX, versionNumber);
				// Devices.searchboxRX(driver).sendKeys(versionNumber);
				log.info("Uploading Version : Version number entered in search box");
				timer(driver);
				String rxTable = SeleniumActions.seleniumGetText(driver, Devices.rxTable);
				// String rxTable = Devices.rxTable(driver).getText();
				Assert.assertTrue(rxTable.contains(versionNumber),
						"Title did not contain: " + versionNumber + ", actual text: " + rxTable);
				timer(driver);
				SeleniumActions.seleniumSendKeysClear(driver, Devices.searchboxRX);
				// Devices.searchboxRX(driver).clear();
				// Calling devices upgrade method to redirect to the Device > Upgrades
				log.info("Uploading Version : Found searched version.. Checking State");
			}
		} else {
			new SkipException("***** Could not recognize appliance type");
		}

		log.info("Upgrading device : Version Upload Completed");
	}

	public void activateVersionEmerald(WebDriver driver, String appliance, String versionNumber)
			throws InterruptedException {
		if (appliance.equalsIgnoreCase("tx")) {
			timer(driver);
			SeleniumActions.seleniumClick(driver, Devices.releasesTab);
			// Devices.releasesTab(driver).click();
			log.info("Activating version : Clicked on Release tab");
			timer(driver);
			Devices.emeraldReleaseTabTransmitter(driver).click();
			log.info("Activating version : Clicked on Emerald TX tab");
			timer(driver);
			Devices.searchBoxEmeraldTx(driver).clear();
			Devices.searchBoxEmeraldTx(driver).sendKeys(versionNumber);
			log.info("Activating Version : Version number entered in the search box");

			if (SeleniumActions.seleniumGetText(driver, Devices.isActiveEmeraldTX).contains("No")) {
				// if(Devices.isActiveEmeraldTX(driver).getText().contains("No")) {
				log.info("Upgrading device : Version is not active.. Activating version");
				SeleniumActions.seleniumClick(driver, Devices.breadCrumbBtnEmeraldTX);
				// Devices.breadCrumbBtnEmeraldTX(driver).click();
				log.info("Upgrading device : Breadcrumb button clicked");
				timer(driver);
				SeleniumActions.seleniumClick(driver, Devices.activateVersionEmeraldTX);
				// Devices.activateVersionEmeraldTX(driver).click();
				log.info("Upgrading device : Acivate button clicked");
				Alert alert = driver.switchTo().alert();
				alert.accept();
				int i = 0;
				int counter = 0;
				while ((Landingpage.spinner(driver).isDisplayed()) && (counter < 10)) {
					log.info((i + 1) + ". Activation in Progress...");
					Thread.sleep(2000);
					i++;
				}
			} else {
				log.info("Upgrading device : Version is active");
			}
			String Activate_status=Users.notificationMessage(driver).getText();
			log.info("Status of the version is "+ Activate_status);
			Assert.assertTrue(Activate_status.contains("Successfully Activated Release File"),
					"***** Error in Activating Appliance Version *****"+"Actual Status is "+ Activate_status);
		} else if (appliance.equalsIgnoreCase("rx")) {
			timer(driver);
			SeleniumActions.seleniumClick(driver, Devices.releasesTab);
			// Devices.releasesTab(driver).click();
			log.info("Activating version : Clicked on Release tab");
			timer(driver);
			Devices.emeraldReleaseTabReceiver(driver).click();
			log.info("Activating version : Clicked on Emerald TX tab");
			timer(driver);
			SeleniumActions.seleniumSendKeysClear(driver, Devices.emeraldRxSearchBox);
			// Devices.searchBoxEmeraldRx(driver).clear();
			SeleniumActions.seleniumSendKeys(driver, Devices.emeraldRxSearchBox, versionNumber);
			// Devices.searchBoxEmeraldRx(driver).sendKeys(versionNumber);
			log.info("Activating Version : Version number entered in the search box");
			if (SeleniumActions.seleniumGetText(driver, Devices.isActiveEmeraldRX).contains("No")) {
				// if(Devices.isActiveEmeraldRX(driver).getText().contains("No")) {
				log.info("Upgrading device : Version is not active.. Activating version");
				SeleniumActions.seleniumClick(driver, Devices.breadCrumbBtnEmeraldRX);
				// Devices.breadCrumbBtnEmeraldRX(driver).click();
				log.info("Upgrading device : Breadcrumb button clicked");
				timer(driver);
				SeleniumActions.seleniumClick(driver, Devices.activateVersionEmeraldRX);
				// Devices.activateVersionEmeraldRX(driver).click();
				log.info("Upgrading device : Acivate button clicked");
				Alert alert = driver.switchTo().alert();
				alert.accept();
				int i = 0;
				int counter = 0;
				while ((Landingpage.spinner(driver).isDisplayed()) && (counter < 10)) {
					log.info((i + 1) + ". Activation in Progress...");
					Thread.sleep(5000);
					i++;
				}
			} else {
				log.info("Upgrading device : Version is active");
			}
			String Activate_status=Users.notificationMessage(driver).getText();
			log.info("Status of the version is "+ Activate_status);
			Assert.assertTrue(Activate_status.contains("Successfully Activated Release File"),
					"***** Error in Activating Appliance Version *****"+"Actual Status is "+ Activate_status);
		} else {
			new SkipException("***** Could not recognize appliance type *****");
		}

	}

	public void activateVersionZeroU(WebDriver driver, String appliance, String versionNumber)
			throws InterruptedException {
		if (appliance.equalsIgnoreCase("tx")) {
			timer(driver);
			SeleniumActions.seleniumClick(driver, Devices.releasesTab);
			// Devices.releasesTab(driver).click();
			log.info("Activating version : Clicked on Release tab");
			timer(driver);
			SeleniumActions.seleniumClick(driver, Devices.getZerouUpgradeTabXpath());
			// Devices.getEmeraldSeTransmitterTab(driver).click();
			log.info("Activating version : Clicked on ZeroU TX tab");
			timer(driver);
			SeleniumActions.seleniumSendKeysClear(driver, Devices.getZeroUSearchBox());
			SeleniumActions.seleniumSendKeys(driver, Devices.getZeroUSearchBox(), versionNumber);
			Thread.sleep(3000);
			// Devices.getEmeraldSeSearchBox(driver).clear();
			// Devices.getEmeraldSeSearchBox(driver).sendKeys(versionNumber);

			log.info("Activating Version : Version number entered in the search box");

			if (SeleniumActions.seleniumGetText(driver, Devices.isActiveZeroUTx).contains("No")) {
				// if(Devices.isActiveEmeraldTX(driver).getText().contains("No")) {
				log.info("Upgrading device : Version is not active.. Activating version");
				SeleniumActions.seleniumClick(driver, Devices.breadCrumbBtnZeroU);
				// Devices.breadCrumbBtnEmeraldTX(driver).click();
				log.info("Upgrading device : Breadcrumb button clicked");
				timer(driver);
				SeleniumActions.seleniumClick(driver, Devices.activateVersionZeroU);
				// Devices.activateVersionEmeraldTX(driver).click();
				log.info("Upgrading device : Acivate button clicked");
				Alert alert = driver.switchTo().alert();
				alert.accept();
				int i = 0;
				int counter = 0;
				while ((Landingpage.spinner(driver).isDisplayed()) && (counter < 10)) {
					log.info((i + 1) + ". Activation in Progress...");
					Thread.sleep(5000);
					i++;
				}
			} else {
				log.info("Upgrading device : Version is active");
			}
		} else if (appliance.equalsIgnoreCase("rx")) {
			timer(driver);
			SeleniumActions.seleniumClick(driver, Devices.releasesTab);
			// Devices.releasesTab(driver).click();
			log.info("Activating version : Clicked on Release tab");
			timer(driver);
			Devices.getEmeraldSeReceiverTab(driver).click();
			log.info("Activating version : Clicked on Emerald TX tab");
			timer(driver);
			Devices.getEmeraldSeSearchBoxRx(driver).clear();
			Devices.getEmeraldSeSearchBoxRx(driver).sendKeys(versionNumber);
			// Devices.searchBoxEmeraldRx(driver).clear();
			// Devices.searchBoxEmeraldRx(driver).sendKeys(versionNumber);
			log.info("Activating Version : Version number entered in the search box");
			if (SeleniumActions.seleniumGetText(driver, Devices.isActiveEmeraldSeRx).contains("No")) {
				// if(Devices.isActiveEmeraldRX(driver).getText().contains("No")) {
				log.info("Upgrading device : Version is not active.. Activating version");
				SeleniumActions.seleniumClick(driver, Devices.breadCrumbBtnEmeraldSeRx);
				// Devices.breadCrumbBtnEmeraldRX(driver).click();
				log.info("Upgrading device : Breadcrumb button clicked");
				timer(driver);
				SeleniumActions.seleniumClick(driver, Devices.activateVersionEmeraldSeRX);
				// Devices.activateVersionEmeraldRX(driver).click();
				log.info("Upgrading device : Acivate button clicked");
				Alert alert = driver.switchTo().alert();
				alert.accept();
				int i = 0;
				int counter = 0;
				while ((Landingpage.spinner(driver).isDisplayed()) && (counter < 10)) {
					log.info((i + 1) + ". Activation in Progress...");
					Thread.sleep(5000);
					i++;
				}
			} else {
				log.info("Upgrading device : Version is active");
			}
		} else {
			new SkipException("***** Could not recognize appliance type *****");
		}

	}

	public void activateVersionEmeraldSe(WebDriver driver, String appliance, String versionNumber)
			throws InterruptedException {
		if (appliance.equalsIgnoreCase("tx")) {
			timer(driver);
			SeleniumActions.seleniumClick(driver, Devices.releasesTab);
			// Devices.releasesTab(driver).click();
			log.info("Activating version : Clicked on Release tab");
			timer(driver);
			Devices.getEmeraldSeTransmitterTab(driver).click();
			log.info("Activating version : Clicked on Emerald SE TX tab");
			timer(driver);
			Devices.getEmeraldSeSearchBox(driver).clear();
			Devices.getEmeraldSeSearchBox(driver).sendKeys(versionNumber);

			log.info("Activating Version : Version number entered in the search box");

			if (SeleniumActions.seleniumGetText(driver, Devices.isActiveEmeraldSeTx).contains("No")) {
				// if(Devices.isActiveEmeraldTX(driver).getText().contains("No")) {
				log.info("Upgrading device : Version is not active.. Activating version");
				SeleniumActions.seleniumClick(driver, Devices.breadCrumbBtnEmeraldSeTx);
				// Devices.breadCrumbBtnEmeraldTX(driver).click();
				log.info("Upgrading device : Breadcrumb button clicked");
				timer(driver);
				SeleniumActions.seleniumClick(driver, Devices.activateVersionEmeraldSeTX);
				// Devices.activateVersionEmeraldTX(driver).click();
				log.info("Upgrading device : Acivate button clicked");
				Alert alert = driver.switchTo().alert();
				alert.accept();
				int i = 0;
				int counter = 0;
				while ((Landingpage.spinner(driver).isDisplayed()) && (counter < 10)) {
					log.info((i + 1) + ". Activation in Progress...");
					Thread.sleep(5000);
					i++;
				}
			} else {
				log.info("Upgrading device : Version is active");
			}
		} else if (appliance.equalsIgnoreCase("rx")) {
			timer(driver);
			SeleniumActions.seleniumClick(driver, Devices.releasesTab);
			// Devices.releasesTab(driver).click();
			log.info("Activating version : Clicked on Release tab");
			timer(driver);
			Devices.getEmeraldSeReceiverTab(driver).click();
			log.info("Activating version : Clicked on Emerald TX tab");
			timer(driver);
			Devices.getEmeraldSeSearchBoxRx(driver).clear();
			Devices.getEmeraldSeSearchBoxRx(driver).sendKeys(versionNumber);
			// Devices.searchBoxEmeraldRx(driver).clear();
			// Devices.searchBoxEmeraldRx(driver).sendKeys(versionNumber);
			log.info("Activating Version : Version number entered in the search box");
			if (SeleniumActions.seleniumGetText(driver, Devices.isActiveEmeraldSeRx).contains("No")) {
				// if(Devices.isActiveEmeraldRX(driver).getText().contains("No")) {
				log.info("Upgrading device : Version is not active.. Activating version");
				SeleniumActions.seleniumClick(driver, Devices.breadCrumbBtnEmeraldSeRx);
				// Devices.breadCrumbBtnEmeraldRX(driver).click();
				log.info("Upgrading device : Breadcrumb button clicked");
				timer(driver);
				SeleniumActions.seleniumClick(driver, Devices.activateVersionEmeraldSeRX);
				// Devices.activateVersionEmeraldRX(driver).click();
				log.info("Upgrading device : Acivate button clicked");
				Alert alert = driver.switchTo().alert();
				alert.accept();
				Utilities.captureScreenShot(driver, "applinaceactivate", "UpgradeDowngrade");
				int i = 0;
				int counter = 0;
				while ((Landingpage.spinner(driver).isDisplayed()) && (counter < 10)) {
					log.info((i + 1) + ". Activation in Progress...");
					Thread.sleep(5000);
					i++;
				}
			} else {
				log.info("Upgrading device : Version is active");
			}
		} else {
			new SkipException("***** Could not recognize appliance type *****");
		}

	}

	/**
	 * Activate a specific version of software for the devices
	 * 
	 * @param driver
	 * @param appliance
	 * @param versionNumber
	 * @throws InterruptedException
	 */
	public void activateVersion(WebDriver driver, String appliance, String versionNumber) throws InterruptedException {
		// Activate Version
		if (appliance.equalsIgnoreCase("tx")) {
			timer(driver);
			SeleniumActions.seleniumClick(driver, Devices.releasesTab);
			// Devices.releasesTab(driver).click();
			log.info("Activating version : Clicked on Release tab");
			timer(driver);
			SeleniumActions.seleniumClick(driver, Devices.txTab);
			// Devices.txTab(driver).click();
			log.info("Activating version : Clicked on TX tab");
			timer(driver);
			SeleniumActions.seleniumSendKeysClear(driver, Devices.searchboxTX);
			// Devices.searchboxTX(driver).clear();
			timer(driver);
			SeleniumActions.seleniumSendKeys(driver, Devices.searchboxTX, versionNumber);
			// Devices.searchboxTX(driver).sendKeys(versionNumber);
			log.info("Activating Version : Version number entered in the search box");

			// Activate Version if it is not activated
			if (SeleniumActions.seleniumGetText(driver, Devices.isActiveTX).contains("No")) {
				// if (Devices.isActiveTX(driver).getText().contains("No")) {
				timer(driver);
				log.info("Upgrading device : Version is not active.. Activating version");
				SeleniumActions.seleniumClick(driver, Devices.breadCrumbBtnTX);
				// Devices.breadCrumbBtnTX(driver).click();
				log.info("Upgrading device : Breadcrumb button clicked");
				timer(driver);
				SeleniumActions.seleniumClick(driver, Devices.activateVersionTX);
				// Devices.activateVersionTX(driver).click();
				log.info("Upgrading device : Acivate button clicked");
				Alert alert = driver.switchTo().alert();
				alert.accept();
				int i = 0;
				int counter = 0;
				while ((Landingpage.spinner(driver).isDisplayed()) && (counter < 10)) {
					log.info((i + 1) + ". Activation in Progress...");
					Thread.sleep(5000);
					i++;
				}
			} else {
				log.info("Upgrading device : Version is active");
			}
		} else if (appliance.equalsIgnoreCase("rx")) {
			timer(driver);
			SeleniumActions.seleniumClick(driver, Devices.releasesTab);
			// Devices.releasesTab(driver).click();
			log.info("Activating version : Clicked on Release tab");
			timer(driver);
			SeleniumActions.seleniumClick(driver, Devices.rxTab);
			timer(driver);
			SeleniumActions.seleniumSendKeysClear(driver, Devices.searchboxRX);
			// Devices.searchboxRX(driver).clear();
			timer(driver);
			SeleniumActions.seleniumSendKeys(driver, Devices.searchboxRX, versionNumber);
			// Devices.searchboxRX(driver).sendKeys(versionNumber);
			log.info("Activating Version : Version number entered in the search box");

			// Activate Version if it is not activated
			if (SeleniumActions.seleniumGetText(driver, Devices.isActiveRX).contains("No")) {
				// if (Devices.isActiveRX(driver).getText().contains("No")) {
				timer(driver);
				log.info("Upgrading device : Version is not active.. Activating version");
				SeleniumActions.seleniumClick(driver, Devices.breadCrumbBtnRX);
				// Devices.breadCrumbBtnRX(driver).click();
				log.info("Upgrading device : Breadcrumb button clicked");
				timer(driver);
				SeleniumActions.seleniumClick(driver, Devices.activateVersionRX);
				// Devices.activateVersionRX(driver).click();
				log.info("Upgrading device : Acivate button clicked");
				Alert alert = driver.switchTo().alert();
				alert.accept();
				int i = 0;
				int counter = 0;
				while ((Landingpage.spinner(driver).isDisplayed()) && (counter < 10)) {
					log.info((i + 1) + ". Activation in Progress...");
					Thread.sleep(5000);
					i++;
				}

			} else {
				log.info("Upgrading device : Version is active");
			}
		} else {
			new SkipException("***** Could not recognize appliance type *****");
		}
	}

	// old upgrade method - Replaced with other on 15th Sep
	/*
	 * public void upgradeDevice(WebDriver driver, String appliance, String
	 * applianceIP, String newVersion, String oldVersion) throws
	 * InterruptedException { timer(driver); Devices.selectDevices(driver).click();
	 * log.info("Upgrading device : Select Devices tab clicked."); timer(driver);
	 * log.info(
	 * "Upgrading device : Version activated.. Checking Firmware version of " +
	 * appliance + " device");
	 * Devices.selectDevicesSearchBox(driver).sendKeys(applianceIP);
	 * log.info("Upgrading device : Device searched using IP address");
	 * 
	 * // log.info(Devices.state(driver).getText().contains("No Upgrade //
	 * Required")); // log.info(Devices.state(driver).getText());
	 * 
	 * if (appliance.equalsIgnoreCase("tx")) { timer(driver); String firmware =
	 * Devices.firmwareVersionTX(driver).getText(); timer(driver); if
	 * (firmware.equalsIgnoreCase(newVersion)) {
	 * Assert.assertTrue(Devices.state(driver).getText().
	 * contains("No Upgrade Required") ||
	 * Devices.state(driver).getText().contains("Idle"),
	 * "***** State Missmatch *****");
	 * log.info("Uploading Version : Trying to activate older version");
	 * timer(driver); Devices.releasesTab(driver).click();
	 * log.info("Uploading Version : Releases tab clicked"); timer(driver);
	 * Devices.txTab(driver).click();
	 * log.info("Uploading Version : DTX-T clciked ");
	 * 
	 * activateVersion(driver, appliance, oldVersion); // Activating v4.1
	 * 
	 * // Re checking appliance version and state timer(driver);
	 * Devices.selectDevices(driver).click();
	 * log.info("Upgrading device : Select Devices tab clicked."); timer(driver);
	 * log.
	 * info("Upgrading device : Version activated.. Checking Firmware version of TX device"
	 * ); Devices.selectDevicesSearchBox(driver).sendKeys(applianceIP);
	 * log.info("Upgrading device : Device searched using IP address");
	 * timer(driver); firmware = Devices.firmwareVersionTX(driver).getText();
	 * timer(driver); completeUpgrade(driver, applianceIP); } else {
	 * completeUpgrade(driver, applianceIP); } } else if
	 * (appliance.equalsIgnoreCase("rx")) { timer(driver); String firmware =
	 * Devices.firmwareVersionRX(driver).getText(); timer(driver); if
	 * (firmware.equalsIgnoreCase(newVersion)) {
	 * Assert.assertTrue(Devices.state(driver).getText().
	 * contains("No Upgrade Required"), "***** State Missmatch *****");
	 * log.info("Uploading Version : Tryin to activate older version");
	 * timer(driver); Devices.releasesTab(driver).click();
	 * log.info("Uploading Version : Releases tab clicked"); timer(driver);
	 * Devices.rxTab(driver).click();
	 * log.info("Uploading Version : DTX-R clicked ");
	 * 
	 * activateVersion(driver, appliance, oldVersion); // Activating v4.1
	 * 
	 * // Re checking appliance version and state timer(driver);
	 * Devices.selectDevices(driver).click();
	 * log.info("Upgrading device : Select Devices tab clicked."); timer(driver);
	 * log.
	 * info("Upgrading device : Version activated.. Checking Firmware version of RX device"
	 * ); Devices.selectDevicesSearchBox(driver).sendKeys(applianceIP);
	 * log.info("Upgrading device : Device searched using IP address");
	 * timer(driver); firmware = Devices.firmwareVersionRX(driver).getText();
	 * timer(driver); completeUpgrade(driver, applianceIP); } else {
	 * completeUpgrade(driver, applianceIP); } } else { new
	 * SkipException("***** Could not recognize appliance type"); } }
	 */

	private void completeUpgradeAll(WebDriver driver, String applianceIP1, String applianceIP2)
			throws InterruptedException { // todo: pass array of ips and handle all?
		log.info("Upgarding device : initiating upgrade");
		log.info("Upgarding device : selecting all devices");
		
       
		SeleniumActions.seleniumClick(driver, Devices.allCheckbox);
		log.info("Upgarding device : selecting all devices done");

		timer(driver); // check implementation
		SeleniumActions.seleniumClick(driver, Devices.upgradeBtn);
		Thread.sleep(5000);
		log.info("Upgarding device : upgrade button clicked");
		driver.switchTo().alert().accept(); // confirm upgrade
		Assert.assertTrue(Users.notificationMessage(driver).getText().contains("Upgrades have started."),
				"Upgrade confirmation toast doesn't contain: \"Upgrades have started\"");
		// prev: slept fort 30 seconds for notification to disappear, why?
		boolean appliance1UpgradeComplete = false;
		boolean appliance2UpgradeComplete = false;
		int counter = 0;
		String appliance1upgradeState = "";
		String appliance2upgradeState = "";
		log.info("Upgarding device : waiting for devices to complete upgrade.");
		while (!appliance1UpgradeComplete && !appliance2UpgradeComplete && (counter < 10)) {
			log.info((counter + 1) + ". Device Upgrade in progress...");
			Thread.sleep(30000);
			driver.navigate().refresh();

			SeleniumActions.seleniumSendKeysClear(driver, Devices.selectDevicesSearchBox);
			SeleniumActions.seleniumSendKeys(driver, Devices.selectDevicesSearchBox, applianceIP1); // first
			appliance1upgradeState = SeleniumActions.seleniumGetText(driver, Devices.state);
			appliance1UpgradeComplete = appliance1upgradeState.contains("No Upgrade Required");
			log.info(String.format("Upgarding device : %s state = %s", applianceIP1, appliance1upgradeState));
			Thread.sleep(1000);
			SeleniumActions.seleniumSendKeysClear(driver, Devices.selectDevicesSearchBox);
			SeleniumActions.seleniumSendKeys(driver, Devices.selectDevicesSearchBox, applianceIP2); // second
			appliance2upgradeState = SeleniumActions.seleniumGetText(driver, Devices.state);
			appliance2UpgradeComplete = appliance2upgradeState.contains("No Upgrade Required");
			log.info(String.format("Upgarding device : %s state = %s", applianceIP2, appliance2upgradeState));
			counter++;
		}

		log.info("Upgrade Device : Upgrade completed. Rechecking state");
		Assert.assertTrue(appliance1upgradeState.contains("No Upgrade Required"),
				String.format("Device %s state did not contain: No Upgrade Required, actual text: %s", applianceIP1,
						appliance1upgradeState));

		Assert.assertTrue(appliance2upgradeState.contains("No Upgrade Required"),
				String.format("Device %s state did not contain: No Upgrade Required, actual text: %s", applianceIP2,
						appliance2upgradeState));

		log.info("Upgrade Device : Appliance upgrade state asserted.");
	}

	/**
	 * Upgrade a specific device
	 * 
	 * @param driver
	 * @param applianceIP
	 * @throws InterruptedException
	 */
	private void completeUpgrade(WebDriver driver, String applianceIP) throws InterruptedException {
		log.info("Upgarding device : Firmware Version doesn't match with active version - Upgrade required");
		timer(driver);
		// SeleniumActions.seleniumClick(driver, Devices.allCheckbox);
		// Devices.allCheckbox(driver).click();
		log.info("Upgrading device : All Devices checkbox unclicked");
		timer(driver);
		SeleniumActions.seleniumClick(driver, Devices.searchedDeviceCheckbox);
		// Devices.searchedDeviceCheckbox(driver).click();
		log.info("Upgrading device : Searched device checkbox clicked");
		timer(driver);
		SeleniumActions.seleniumClick(driver, Devices.upgradeBtn);
		// Devices.upgradeBtn(driver).click();
		log.info("Upgrading device : Upgrade Button Clicked");
		Alert alert2 = driver.switchTo().alert();
		log.info("Accept the alert message");
		Thread.sleep(3000);
		alert2.accept();
		log.info("Alert accepeted");
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Users.notificationMessage(driver)));
		Assert.assertTrue(Users.notificationMessage(driver).getText().contains("Upgrades have started."),
				"***** Error in starting Upgrade *****");
		Thread.sleep(30000); // wait for 30 seconds to clear notification message
		log.info("Upgrading device : Upgarde started waiting to complete");
		int counter = 0;
		while (!(SeleniumActions.seleniumGetText(driver, Devices.upgradeTable).contains("No Upgrade Required"))
				&& (counter < 10)) {
			// while (!(Devices.upgradeTable(driver).getText().contains("No Upgrade
			// Required")) && (counter < 10)) {
			log.info((counter + 1) + ". Device Upgrade in progress...");
			Thread.sleep(30000);
			driver.navigate().refresh();
			SeleniumActions.seleniumSendKeys(driver, Devices.selectDevicesSearchBox, applianceIP);
			counter++;
		}

		log.info("Upgrade Device : Upgrade Completed.. Asserting..");
		timer(driver);
		driver.navigate().refresh();
		SeleniumActions.seleniumSendKeys(driver, Devices.selectDevicesSearchBox, applianceIP);
		// Devices.selectDevicesSearchBox(driver).sendKeys(applianceIP);
		log.info("Upgrading Device : Appliance IP entered in search box");
		timer(driver);
		String deviceState = SeleniumActions.seleniumGetText(driver, Devices.state);
		// String deviceState = Devices.state(driver).getText();
		Assert.assertTrue(deviceState.contains("No Upgrade Required"),
				"Device state did not contain: No Upgrade Required, actual text: " + deviceState);
		log.info("Upgrading Device : Upgrade Asserted");

		// If Upgrade Have finished message appears on the screen
		/*
		 * if (Users.notificationMessage(driver).getText().
		 * contains("Upgrades have finished")) {
		 * log.info("Upgrade Device : Upgrade Completed.. Asserting..");
		 * Thread.sleep(15000); // Some times it takes time to update status after
		 * notification message driver.navigate().refresh();
		 * Devices.selectDevicesSearchBox(driver).sendKeys(applianceIP);
		 * log.info("Upgrading Device : Appliance IP entered in search box");
		 * timer(driver); Assert.assertTrue(Devices.state(driver).getText().
		 * contains("No Upgrade Required"),
		 * "***** Upgrading Device : Assertion failed on completing upgrade *****");
		 * log.info("Upgrading Device : Upgrade Asserted"); } else { // If other message
		 * appears on the screen
		 * org.testng.Assert.fail("***** Failure in Upgrading the Device *****"); }
		 */
	}

	private void completeUpgradeTimer(WebDriver driver, String applianceIP) throws InterruptedException {
		log.info("Upgarding device : Firmware Version doesn't match with active version - Upgrade required");
		timer(driver);
		SeleniumActions.seleniumClick(driver, Devices.allCheckbox);
		// Devices.allCheckbox(driver).click();
		log.info("Upgrading device : All Devices checkbox unclicked");
		timer(driver);
		SeleniumActions.seleniumClick(driver, Devices.searchedDeviceCheckbox);
		// Devices.searchedDeviceCheckbox(driver).click();
		log.info("Upgrading device : Searched device checkbox clicked");
		timer(driver);
		SeleniumActions.seleniumClick(driver, Devices.upgradeBtn);
		// Devices.upgradeBtn(driver).click();
		log.info("Upgrading device : Upgrade Button Clicked");
		Alert alert2 = driver.switchTo().alert();
		alert2.accept();
		Assert.assertTrue(Users.notificationMessage(driver).getText().contains("Upgrades have started."),
				"***** Error in starting Upgrade *****");
		Thread.sleep(30000); // wait for 30 seconds to clear notification message
		log.info("Upgrading device : Upgarde started waiting to complete");
		int counter = 0;
		while (!(SeleniumActions.seleniumGetText(driver, Devices.upgradeTable).contains("No Upgrade Required"))
				&& (counter < 10)) {
			// while (!(Devices.upgradeTable(driver).getText().contains("No Upgrade
			// Required")) && (counter < 10)) {
			log.info((counter + 1) + ". Device Upgrade in progress...");
			Thread.sleep(60000);
			counter++;
		}

		log.info("Upgrade Device : Upgrade Completed.. Asserting..");
		timer(driver);
		driver.navigate().refresh();
		SeleniumActions.seleniumSendKeys(driver, Devices.selectDevicesSearchBox, applianceIP);
		// Devices.selectDevicesSearchBox(driver).sendKeys(applianceIP);
		log.info("Upgrading Device : Appliance IP entered in search box");
		timer(driver);
		String deviceState = SeleniumActions.seleniumGetText(driver, Devices.state);
		// String deviceState = Devices.state(driver).getText();
		Assert.assertTrue(deviceState.contains("No Upgrade Required"),
				"Device state did not contain: No Upgrade Required, actual text: " + deviceState);
		log.info("Upgrading Device : Upgrade Asserted");

		// If Upgrade Have finished message appears on the screen
		/*
		 * if (Users.notificationMessage(driver).getText().
		 * contains("Upgrades have finished")) {
		 * log.info("Upgrade Device : Upgrade Completed.. Asserting..");
		 * Thread.sleep(15000); // Some times it takes time to update status after
		 * notification message driver.navigate().refresh();
		 * Devices.selectDevicesSearchBox(driver).sendKeys(applianceIP);
		 * log.info("Upgrading Device : Appliance IP entered in search box");
		 * timer(driver); Assert.assertTrue(Devices.state(driver).getText().
		 * contains("No Upgrade Required"),
		 * "***** Upgrading Device : Assertion failed on completing upgrade *****");
		 * log.info("Upgrading Device : Upgrade Asserted"); } else { // If other message
		 * appears on the screen
		 * org.testng.Assert.fail("***** Failure in Upgrading the Device *****"); }
		 */
	}

	/**
	 * Gets the device ID
	 * 
	 * @param driver
	 * @param ipaddress
	 * @throws InterruptedException
	 */
	public void getDeviceId(WebDriver driver, String ipaddress) throws InterruptedException {
		timer(driver);
		Landingpage.devicesTab(driver).click();
		log.info("Devices > Status > Options - Clicked on Devices tab");
		timer(driver);
		Landingpage.devicesStatus(driver).click();
		log.info("Devices > Status > Options - Clicked on Status tab");
		timer(driver);
		SeleniumActions.seleniumSendKeys(driver, Devices.deviceStatusSearchBox, Devices.deviceStatusSearchBox);
		// Devices.deviceStatusSearchBox(driver).sendKeys(ipaddress);
		log.info("Devices > Status > Options - Device Model entered in Search box");

		List<WebElement> list = driver
				.findElements(By.xpath("//table[@id='appliance_table']//following::tr[@class='success odd']"));
		for (WebElement e : list) {
			if (e.isDisplayed()) {
				System.out.println(e.getAttribute("data-id"));
			}
		}

	}

	/**
	 * Checks if a specific device is online
	 * 
	 * @param driver
	 * @param ipAddress
	 * @throws InterruptedException
	 */
	public void checkDeviceOnline(WebDriver driver, String ipAddress) throws InterruptedException {
		log.info("Attempting to check if device with IP address " + ipAddress + " is online");
		timer(driver);
		Landingpage.devicesTab(driver).click();
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(Landingpage.devicesStatus(driver)));
		Landingpage.devicesStatus(driver).click();
		log.info("Devices > Status > Options - Clicked on Status tab");
		timer(driver);
		SeleniumActions.seleniumSendKeys(driver, Devices.deviceStatusSearchBox, ipAddress);
		// Devices.deviceStatusSearchBox(driver).sendKeys(ipAddress);
		// check if device is online
		int timer = 0;
		int limit = 12; // 12 iterations of 5 seconds = 1 minute
		while (timer <= limit) {
			log.info("Checking if device is online");
			String isOnline = SeleniumActions.seleniumGetText(driver, Devices.applianceTable);
			log.info("Is Online:" + isOnline);
			if (SeleniumActions.seleniumGetText(driver, Devices.applianceTable).contains("OnLine")) {
				// if(Devices.applianceTable(driver).getText().contains("OnLine")) {
				log.info("Device is online");
				break;
			} else if (timer < limit) {
				timer++;
				log.info("Device is offline. Rechecking " + timer);
				driver.navigate().refresh();
				Thread.sleep(5000);
			} else if (timer == limit) {
				Assert.assertTrue(1 == 0, "Device is not online");
			}
		}
		log.info("Successfully checked if device is online");
	}

	/**
	 * Checks if a specific device is offline
	 * 
	 * @param driver
	 * @param ipAddress
	 * @throws InterruptedException
	 */
	public void checkDeviceOffline(WebDriver driver, String ipAddress) throws InterruptedException {
		log.info("Attempting to check if device with IP address " + ipAddress + " is offline");
		timer(driver);
		Landingpage.devicesTab(driver).click();
		timer(driver);
		Landingpage.devicesStatus(driver).click();
		timer(driver);
		SeleniumActions.seleniumSendKeys(driver, Devices.deviceStatusSearchBox, ipAddress);
		// Devices.deviceStatusSearchBox(driver).sendKeys(ipAddress);
		// check if device is online
		int timer = 0;
		int limit = 12; // 12 iterations of 5 seconds = 1 minute
		while (timer <= limit) {
			log.info("Checking if device is offline");
			if (SeleniumActions.seleniumGetText(driver, Devices.applianceTable).contains("OffLine")) {
				// if(Devices.applianceTable(driver).getText().contains("OffLine")) {
				log.info("Device is OffLine");
				break;
			} else if (timer < limit) {
				timer++;
				log.info("Device is online. Rechecking " + timer);
				driver.navigate().refresh();
				Thread.sleep(5000);
			} else if (timer == limit) {
				Assert.assertTrue(1 == 0, "Device is online");
			}
		}
		log.info("Successfully checked if device is offline");
	}

	/**
	 * If device is online clicks the device options dropdown
	 * 
	 * @param driver
	 * @param ipAddress
	 * @throws InterruptedException
	 */
	public void navigateToOptions(WebDriver driver, String ipAddress) throws InterruptedException {
		checkDeviceOnline(driver, ipAddress);

		// String currentIP = Devices.currentIP(driver).getText();
		timer(driver);
		if (SeleniumActions.seleniumGetText(driver, Devices.applianceTable).contains(ipAddress)) {
			// if (Devices.applianceTable(driver).getText().contains(ipAddress)) {
			SeleniumActions.seleniumClick(driver, Devices.breadCrumbBtn);
			// Devices.breadCrumbBtn(driver).click();
			log.info("Devices > Status > Options - Clicked on breadcrumb");
		} else {
			log.info("Devices > Status > Options - Searched device not found");
			throw new SkipException("***** Searched device - " + ipAddress + " not found *****");
		}

	}

	// Retrieve device details using device Name
	/**
	 * Retrive device details
	 * 
	 * @param driver
	 * @param ipAddress
	 * @throws InterruptedException
	 */
	public void retrieveDetails(WebDriver driver, String ipAddress, String mac, String deviceName)
			throws InterruptedException {
		navigateToOptions(driver, ipAddress);
		timer(driver);
		SeleniumActions.seleniumClick(driver, Devices.details);
		// Devices.details(driver).click();
		log.info("Retreieve Device Details - Details tab clicked");
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Landingpage.spinner(driver)));
		new WebDriverWait(driver, 60).until(ExpectedConditions.invisibilityOf(Landingpage.spinner(driver)));
		new WebDriverWait(driver, 60)
				.until(ExpectedConditions.elementToBeClickable(By.xpath(Devices.getDeviceDetailsBackBtn())));
		log.info("Getting page text and checking for ip, mac and device name");
		String pageText = driver.getPageSource();
		Assert.assertTrue(pageText.contains(ipAddress),
				"Page did not contain IP address:" + ipAddress + " actual:" + pageText);
		log.info("IP Address asserted");
		Assert.assertTrue(pageText.contains(mac), "Page did not contain mac address:" + mac + " actual:" + pageText);
		log.info("mac address asserted");
		Assert.assertTrue(pageText.contains(deviceName),
				"Page did not contain device name:" + deviceName + " actual:" + pageText);
		log.info("Device name asserted");

	}

	// Ping device - Using device IP Address to filter
	/**
	 * Ping a specific device through boxilla
	 * 
	 * @param driver
	 * @param ipAddress
	 * @throws InterruptedException
	 */
	public void pingDevice(WebDriver driver, String ipAddress) throws InterruptedException {
		navigateToOptions(driver, ipAddress);
		timer(driver);
		SeleniumActions.seleniumClick(driver, Devices.ping);
		// Devices.ping(driver).click();
		log.info("Ping device - Ping tab clicked");
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Landingpage.spinner(driver)));
		new WebDriverWait(driver, 60).until(ExpectedConditions.invisibilityOf(Landingpage.spinner(driver)));
		// int counter = 0;
		// while ((Landingpage.spinner(driver).isDisplayed()) & (counter < 10)) {
		// log.info((counter + 1) + ". Pinging device...");
		// Thread.sleep(2000);
		// counter++;
		// }
		// timer(driver);
		String notificationMessage = Users.notificationMessage(driver).getText();
		Assert.assertTrue(notificationMessage.contains("Successfully Pinged"),
				"Notification Message did not contain: Successfully Pinged, actual text: " + notificationMessage);
		log.info("Ping device - Notification message asserted successfully");
	}

	/**
	 * Finds the correct TX device, clicks the breadcrumb button and then selects
	 * edit settings which brings up an edit transmitter modal
	 * 
	 * @param driver
	 * @param ipAddress
	 * @throws InterruptedException
	 */
	public void navigateToEditSettingsTx(WebDriver driver, String ipAddress) throws InterruptedException {
		log.info("Attempting to navigate to edit TX device settings");
		navigateToOptions(driver, ipAddress);
		timer(driver);
		Devices.editDeviceSettingsTx(driver).click();
		timer(driver);
		new WebDriverWait(driver, 60).until(ExpectedConditions
				.presenceOfElementLocated(By.xpath(Devices.getEditTxSettingsSettingTypeDropdownXpath())));
		log.info("Successfully navigated to edit TX device settings");

	}

	/**
	 * Opens the edit settings modal for receiver device
	 * 
	 * @param driver
	 * @param ipAddress
	 * @throws InterruptedException
	 */
	public void navigateToEditSettingsRx(WebDriver driver, String ipAddress) throws InterruptedException {
		log.info("Attempting to navigate to edit RX device settings");
		navigateToOptions(driver, ipAddress);
		timer(driver);
		Devices.editDeviceSettingsRx(driver).click();
		timer(driver);
		new WebDriverWait(driver, 60).until(ExpectedConditions
				.presenceOfElementLocated(By.xpath(Devices.getEditRxSettingsSettingTypeDropdownXpath())));
		log.info("Successfully navigated to edit RX device settings");
	}

	/**
	 * Sets specific transmitter device to system properties
	 * 
	 * @param driver
	 * @param ipAddress
	 * @throws InterruptedException
	 */
	public void setTxToSystemProperties(WebDriver driver, String ipAddress) throws InterruptedException {
		navigateToEditSettingsTx(driver, ipAddress);
		Devices.editTxSettingsSettingDropdown(driver, "System");
		timer(driver);
		// Devices.editTxSaveBtn(driver).click();
		saveEditTxSettings(driver);
	}

	/**
	 * Sets specific transmitter device to template properties
	 * 
	 * @param driver
	 * @param ipAddress
	 *            IP address of the transmitter
	 * @param templateName
	 *            name of the template to set transmitter to
	 * @throws InterruptedException
	 */
	public void setTxToTemplateProperties(WebDriver driver, String ipAddress, String templateName)
			throws InterruptedException {
		navigateToEditSettingsTx(driver, ipAddress);
		Devices.editTxSettingsSettingDropdown(driver, "Template");
		timer(driver);
		Devices.EditTxTemplateNameDropdown(driver, templateName);
		timer(driver);
		saveEditTxSettings(driver);
	}

	/**
	 * Sets specific receiver device to system properties
	 * 
	 * @param driver
	 * @param ipAddress
	 *            IP address of the receiver
	 * @throws InterruptedException
	 */
	public void setRxToSystemProperty(WebDriver driver, String ipAddress) throws InterruptedException {
		navigateToEditSettingsRx(driver, ipAddress);
		Devices.editRxSettingsSettingDropdown(driver, "System");
		timer(driver);
		saveEditRxSettings(driver);
	}

	/**
	 * Sets specific receiver device to template properties
	 * 
	 * @param driver
	 * @param ipAddress
	 *            IP address of the receiver
	 * @param templateName
	 *            name of the template to set receiver
	 * @throws InterruptedException
	 */
	public void setRxToTemplateProperties(WebDriver driver, String ipAddress, String templateName)
			throws InterruptedException {
		navigateToEditSettingsRx(driver, ipAddress);
		Devices.editRxSettingsSettingDropdown(driver, "Template");
		timer(driver);
		Devices.editRxTemplateNameDropdown(driver, templateName);
		timer(driver);
		saveEditRxSettings(driver);
	}

	// Unmanage device using IP address
	/**
	 * Unmanage a device with the given IP address
	 * 
	 * @param driver
	 * @param ipAddress
	 *            IP address of the device to unmanage
	 * @throws InterruptedException
	 */
	public void unManageDevice(WebDriver driver, String ipAddress) throws InterruptedException {
		navigateToOptions(driver, ipAddress);
		timer(driver);
		SeleniumActions.seleniumClick(driver, Devices.unManageTab);
		// Devices.unManageTab(driver).click();
		log.info("UnManage Device -  Clicked on Unamange Tab");
		Alert alert = driver.switchTo().alert();
		alert.accept();
		int counter = 0;
		// log.info(Landingpage.spinner(driver).isDisplayed());
		while (Landingpage.spinner(driver).isDisplayed() && (counter < 20)) {
			log.info((counter + 1) + ". Unmanaging device...");
			// log.info(Landingpage.spinner(driver).isDisplayed());
			Thread.sleep(3000);
			counter++;
		}
		Thread.sleep(3000);
		SeleniumActions.seleniumSendKeys(driver, Devices.deviceStatusSearchBox, ipAddress);
		// Devices.deviceStatusSearchBox(driver).sendKeys(ipAddress);
		log.info("UnManage Device - Device name entered in search box");
		timer(driver);
		String deviceApplianceTable = SeleniumActions.seleniumGetText(driver, Devices.applianceTable);
		// String deviceApplianceTable = Devices.applianceTable(driver).getText();
		Assert.assertFalse(deviceApplianceTable.contains(ipAddress),
				"Device appliance table did not contain: " + ipAddress + ", actual text: " + ipAddress);
	}

	// edit device IP
	/**
	 * Change the devices IP to a new one
	 * 
	 * @param driver
	 * @param currentIP
	 *            Current IP address of the device
	 * @param newIP
	 *            IP address to change device to
	 * @throws InterruptedException
	 */
	public void editDevice(WebDriver driver, String currentIP, String newIP, boolean isChangeBack)
			throws InterruptedException {
		navigateToOptions(driver, currentIP);
		editIPAdd(driver, currentIP, newIP);
		log.info("Edit Device - IP address changed to " + newIP + ". Changing back to " + currentIP);
		int counter = 0;
		log.info("waiting for boxilla to pick up device online / offline");
		Thread.sleep(30000);
		driver.navigate().refresh();
		SeleniumActions.seleniumSendKeys(driver, Devices.deviceStatusSearchBox, newIP);
		// Devices.deviceStatusSearchBox(driver).sendKeys(newIP);
		while (SeleniumActions.seleniumGetText(driver, Devices.applianceTable).contains("OffLine") && counter < 5) {
			// while (Devices.applianceTable(driver).getText().contains("OffLine") &&
			// counter < 5) {
			driver.navigate().refresh();
			SeleniumActions.seleniumSendKeys(driver, Devices.deviceStatusSearchBox, newIP);
			// Devices.deviceStatusSearchBox(driver).sendKeys(newIP);
			log.info((counter + 1) + ". Device state is offline.. Refreshing page");
			Thread.sleep(4000);
			counter++;

		}
		if (isChangeBack) {
			timer(driver);
			// Devices.deviceStatusSearchBox(driver).sendKeys(newIP);
			log.info("Edit Device - new IP address entered in search box");
			timer(driver);
			SeleniumActions.seleniumClick(driver, Devices.breadCrumbBtn);
			// Devices.breadCrumbBtn(driver).click();
			log.info("Edit Device - Clicked on breadcrumb");
			editIPAdd(driver, newIP, currentIP); // New IP became currentIP and previous IP will be new IP
		}
	}

	/**
	 * Soak specific method to change the device IP address
	 * 
	 * @param driver
	 * @param deviceName
	 * @param currentIP
	 * @param newIP
	 * @throws InterruptedException
	 */
	public void deviceIPchangeSoak(WebDriver driver, String deviceName, String currentIP, String newIP)
			throws InterruptedException {
		/*
		 * device IP change method to use in soak test.. Flow: Search device using
		 * device name , extract existing IP address, compares it with IP passed in
		 * parameters and change IP address accordingly
		 */
		navigateToOptions(driver, deviceName); // Searching using device name instead of IP address
		timer(driver);
		log.info(SeleniumActions.seleniumGetText(driver, Devices.applianceTable));
		if (SeleniumActions.seleniumGetText(driver, Devices.applianceTable).contains(currentIP)) {
			// if (Devices.ipAddress(driver).getText().contains(currentIP)) {
			editIPAdd(driver, currentIP, newIP); // Changing IP address from Current IP to new IP
		} else if (SeleniumActions.seleniumGetText(driver, Devices.applianceTable).contains(newIP)) {
			// } else if (Devices.ipAddress(driver).getText().contains(newIP)) {
			editIPAdd(driver, newIP, currentIP);
		} else {
			log.info("IP address doesn't match with any of addresses passed with method.. Skipping test");
			throw new SkipException("***** IP address error.. Skipping test.. *****");
		}
	}

	// method to change IP address from current to new
	private void editIPAdd(WebDriver driver, String currentIP, String newIP) throws InterruptedException {
		log.info("Changing IP address form " + currentIP + " to " + newIP);
		timer(driver);
		SeleniumActions.seleniumClick(driver, Devices.edit);
		// Devices.edit(driver).click();
		log.info("Edit Device - Edit tab clicked");
		timer(driver);
		SeleniumActions.seleniumSendKeysClear(driver, Devices.editDeviceIPAddTextbox);
		// Devices.editDeviceIPAddTextbox(driver).clear();
		log.info("Edit Device - " + currentIP + " cleared from textbox");
		timer(driver);
		SeleniumActions.seleniumSendKeys(driver, Devices.editDeviceIPAddTextbox, newIP);
		// Devices.editDeviceIPAddTextbox(driver).sendKeys(newIP);
		log.info("Edit Device - " + newIP + " entered");
		timer(driver);
		SeleniumActions.seleniumClick(driver, Devices.applyBtn);
		// Devices.applyBtn(driver).click();
		log.info("Edit Device - Apply button clicked");
		timer(driver);

		Thread.sleep(60000);
		navigateToDeviceStatus(driver);
		SeleniumActions.seleniumSendKeys(driver, Devices.deviceStatusSearchBox, newIP);
		// Devices.deviceStatusSearchBox(driver).sendKeys(newIP);
		String deviceApplicaneTable = SeleniumActions.seleniumGetText(driver, Devices.applianceTable);
		// String deviceApplicaneTable = Devices.applianceTable(driver).getText();
		log.info("Edit Device - New IP address entered in to search box");
		Assert.assertTrue(deviceApplicaneTable.contains(newIP),
				"Device applicane table did not contain: " + newIP + ", actual text: " + deviceApplicaneTable);
		log.info("Edit Device - Device Edit asserted");
		timer(driver);
		SeleniumActions.seleniumSendKeysClear(driver, Devices.deviceStatusSearchBox);
		// Devices.deviceStatusSearchBox(driver).clear(); // Clear Search box
	}

	// Change Device Name using device IP address and New host Name
	/**
	 * Edit the device name
	 * 
	 * @param driver
	 * @param ipAddress
	 *            IP address of the device
	 * @param newName
	 *            New name to change the device to
	 * @throws InterruptedException
	 */
	public void changeDeviceName(WebDriver driver, String ipAddress, String newName) throws InterruptedException {
		navigateToOptions(driver, ipAddress);
		log.info("Changing name to:" + newName);
		timer(driver);
		SeleniumActions.seleniumClick(driver, Devices.chanageDeviceName);
		// Devices.chanageDeviceName(driver).click();
		log.info("Change Device Name - Change Device Name button clicked");
		timer(driver);
		SeleniumActions.seleniumSendKeys(driver, Devices.hostnameTextbox, newName);
		// Devices.hostnameTextbox(driver).sendKeys(newName);
		log.info("Change Device Name - New name entered in Hostname textbox");
		timer(driver);
		SeleniumActions.seleniumClick(driver, Devices.hostnameApplyBtn);
		// Devices.hostnameApplyBtn(driver).click();
		log.info("Change Device Name - Apply Button Clicked");
		Alert alert = driver.switchTo().alert();
		alert.accept();
		timer(driver);
		SeleniumActions.seleniumSendKeysClear(driver, Devices.deviceStatusSearchBox);
		SeleniumActions.seleniumSendKeys(driver, Devices.deviceStatusSearchBox, ipAddress);
		// Devices.deviceStatusSearchBox(driver).sendKeys(ipAddress);
		log.info("Change Device Name - IP address entered into search box");
		timer(driver);
		String deviceApplicaneTable = SeleniumActions.seleniumGetText(driver, Devices.applianceTable);
		// String deviceApplicaneTable = Devices.applianceTable(driver).getText();
		Assert.assertTrue(deviceApplicaneTable.contains(newName),
				"Device appliance table did not contain: " + newName + ", actual text: " + deviceApplicaneTable);
		log.info("Change Device Name - Assertion Completed");
	}

	/**
	 * Check through boxilla, how old a device has been running for
	 * 
	 * @param driver
	 * @return uptime
	 */
	public float uptime(WebDriver driver) {
		String value = SeleniumActions.seleniumGetText(driver, Devices.deviceUptime);
		// String value = Devices.deviceUptime(driver).getText();
		// Extract everyting after '</strong> ' text
		String extracted = value.substring(value.lastIndexOf(' ') + 1);
		float uptime = Float.valueOf(extracted);
		log.info("Device Uptime:" + uptime);
		return uptime;
	}

	/**
	 * Reboot a device through Boxilla
	 * 
	 * @param driver
	 * @param ipAddress
	 * @throws InterruptedException
	 */
	public void rebootDevice(WebDriver driver, String ipAddress) throws InterruptedException {
		timer(driver);
		navigateToOptions(driver, ipAddress);
		timer(driver);
		SeleniumActions.seleniumClick(driver, Devices.rebootDevice);
		// Devices.rebootDevice(driver).click();
		log.info("Reboot Device - Reboot tab clicked.. Waiting for 2 minutes before asserting uptime");
		Alert alert = driver.switchTo().alert();
		alert.accept();
		Thread.sleep(120000);
	}

	/**
	 * Restore a device through Boxilla
	 * 
	 * @param driver
	 * @param ipAddress
	 * @throws InterruptedException
	 */
	public void restoreDevice(WebDriver driver, String ipAddress) throws InterruptedException {
		navigateToOptions(driver, ipAddress);
		timer(driver);
		SeleniumActions.seleniumClick(driver, Devices.restoreDevice);
		// Devices.restoreDevice(driver).click();
		log.info("Restore Device - Restore tab clicked");
		Alert alert = driver.switchTo().alert();
		alert.accept();
		int counter = 0;
		while ((Landingpage.spinner(driver).isDisplayed()) && (counter < 10)) {
			log.info((counter + 1) + ". Restoring device...");
			Thread.sleep(2000);
			counter++;
		}
		log.info("Restore Device - Restore Completed.. Asserting.. ");
		timer(driver);
		SeleniumActions.seleniumSendKeys(driver, Devices.deviceStatusSearchBox, ipAddress);
		// Devices.deviceStatusSearchBox(driver).sendKeys(ipAddress);
		log.info("Restore Device - Device IP entered in to search box");
		timer(driver);
		String deviceApplianceTable = SeleniumActions.seleniumGetText(driver, Devices.applianceTable);
		// String deviceApplianceTable = Devices.applianceTable(driver).getText();
		Assert.assertFalse(deviceApplianceTable.contains(ipAddress),
				"Device appliance table contained: " + ipAddress + ", actual text: " + deviceApplianceTable);
		log.info("Restore Device - Device not found.. Restore complete");
	}

	/**
	 * Change connection inactivity time on a device
	 * 
	 * @param driver
	 * @throws InterruptedException
	 */
	public String changeOSDInactivityTimer(WebDriver driver) throws InterruptedException {
		Landingpage.devicesTab(driver).click();
		log.info("Devices Settings - Clicked on Devices tab");
		Thread.sleep(2000);
		Landingpage.devicesSettings(driver).click();
		log.info("Devices Settings - Clicked on Devices > Settings tab");
		Thread.sleep(2000);
		String button2Text = SeleniumActions.seleniumGetText(driver, Devices.osdInactivityTimerBtn);
		int min = 2;
		int max = 60;
		Random number = new Random();
		int generatedNumber = number.nextInt((max - min) + 1) + min;
		String numberString = Integer.toString(generatedNumber);
		log.info("Setting value to:" + generatedNumber);

		if (button2Text.contains("Disable")) {
			// if (Devices.OSDInactivityRange(driver).isDisplayed()) {
			log.info("OSD InActivity Timer is Enabled.. changing timeout value");
			// Scoll to the element as it was failing in Chrome without scrolling
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
					SeleniumActions.getElement(driver, Devices.osdInactivityTimerBtn));

			// ((JavascriptExecutor)
			// driver).executeScript("arguments[0].scrollIntoView(true);",
			// Devices.osdInactivityTimerBtn(driver));
			SeleniumActions.seleniumDropdown(driver, Devices.OSDInactivityRange, numberString);
			// Devices.osdInactivityTimerBtn(driver).click();

		} else {
			log.info("OSD InActivity Timer is Disabled.. Enabling..");
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
					SeleniumActions.getElement(driver, Devices.osdInactivityTimerBtn));

			// ((JavascriptExecutor)
			// driver).executeScript("arguments[0].scrollIntoView(true);",
			// Devices.osdInactivityTimerBtn(driver));
			SeleniumActions.seleniumClick(driver, Devices.osdInactivityTimerBtn);
			// Devices.osdInactivityTimerBtn(driver).click();
			SeleniumActions.seleniumDropdown(driver, Devices.OSDInactivityRange, numberString);
			log.info("Devices Settings - OSD Inactivity Timer Enabled");
		}

		Thread.sleep(2000);
		SeleniumActions.seleniumClick(driver, Devices.deviceSettingsApply);
		// Devices.applyBtn(driver).click();
		Alert alert = driver.switchTo().alert();
		alert.accept();
		log.info("Device Settings - Apply Button clicked and Alert accepted");
		return numberString;
	}

	public void disableFunctionalHotkey(WebDriver driver) throws InterruptedException {
		Landingpage.devicesTab(driver).click();
		Thread.sleep(2000);
		Landingpage.devicesSettings(driver).click();
		new WebDriverWait(driver, 60)
				.until(ExpectedConditions.elementToBeClickable(By.xpath(Devices.deviceSettingsApply)));
		String selectOption = SeleniumActions.seleniumDropdownGetText(driver, Devices.getFunctionalHotkeyDropdown());

		if (selectOption.equals("Disable")) {
			log.info("Functional Hotkey already disabled. Doing nothing");
		} else {
			log.info("Functional hotkey is not disabled. Disabling");
			SeleniumActions.seleniumDropdown(driver, Devices.getFunctionalHotkeyDropdown(), "Disable");
			SeleniumActions.seleniumClick(driver, Devices.deviceSettingsApply);
			// Devices.applyBtn(driver).click();
			Alert alert = driver.switchTo().alert();
			alert.accept();
			log.info("Device Settings - Apply Button clicked and Alert accepted");

		}
	}

	public void setHotkey(WebDriver driver, HOTKEY key) throws InterruptedException {
		Landingpage.devicesTab(driver).click();
		Thread.sleep(2000);
		Landingpage.devicesSettings(driver).click();
		new WebDriverWait(driver, 60)
				.until(ExpectedConditions.elementToBeClickable(By.xpath(Devices.deviceSettingsApply)));
		switch (key) {
		case PRINTSCRN:
			SeleniumActions.seleniumDropdown(driver, Devices.getHotkeyDropdown(), "PrintScrn");
			break;
		case ALT_ALT:
			SeleniumActions.seleniumDropdown(driver, Devices.getHotkeyDropdown(), "Alt-Alt");
			break;
		case CTRL_CTRL:
			SeleniumActions.seleniumDropdown(driver, Devices.getHotkeyDropdown(), "Ctrl-Crtl");
			break;
		case SHIFT_SHIFT:
			SeleniumActions.seleniumDropdown(driver, Devices.getHotkeyDropdown(), "Shift-Shift");
			break;
		case MOUSE:
			SeleniumActions.seleniumDropdown(driver, Devices.getHotkeyDropdown(), "Mouse-Left+Right");
			break;
		}
		SeleniumActions.seleniumClick(driver, Devices.deviceSettingsApply);
		Alert alert = driver.switchTo().alert();
		alert.accept();
		log.info("Device Settings - Apply Button clicked and Alert accepted");

	}

	public void setRDP_ConnectionResolution(WebDriver driver, RESOLUTION res) throws InterruptedException {
		Landingpage.devicesTab(driver).click();
		Thread.sleep(2000);
		Landingpage.devicesSettings(driver).click();
		new WebDriverWait(driver, 60)
				.until(ExpectedConditions.elementToBeClickable(By.xpath(Devices.deviceSettingsApply)));
		switch (res) {
		case R1920X1080:
			SeleniumActions.seleniumDropdown(driver, Devices.getRDP_ConnectionResolution(), "1920x1080");
			break;
		case R1024X768:
			SeleniumActions.seleniumDropdown(driver, Devices.getRDP_ConnectionResolution(), "1024x768");
			break;
		case R1920X1200:
			SeleniumActions.seleniumDropdown(driver, Devices.getRDP_ConnectionResolution(), "1920x1200");
			break;
		case R1280X1024:
			SeleniumActions.seleniumDropdown(driver, Devices.getRDP_ConnectionResolution(), "1280x1024");
			break;
		case R640X480:
			SeleniumActions.seleniumDropdown(driver, Devices.getRDP_ConnectionResolution(), "640x480");
			break;
		case R800X600:
			SeleniumActions.seleniumDropdown(driver, Devices.getRDP_ConnectionResolution(), "800x600");
			break;
		}

		SeleniumActions.seleniumClick(driver, Devices.deviceSettingsApply);
		Alert alert = driver.switchTo().alert();
		alert.accept();
		log.info("Device Settings - Apply Button clicked and Alert accepted");

	}

	public void enableFunctionalHotkey(WebDriver driver) throws InterruptedException {
		Landingpage.devicesTab(driver).click();
		Thread.sleep(2000);
		Landingpage.devicesSettings(driver).click();
		new WebDriverWait(driver, 60)
				.until(ExpectedConditions.elementToBeClickable(By.xpath(Devices.deviceSettingsApply)));
		String selectOption = SeleniumActions.seleniumDropdownGetText(driver, Devices.getFunctionalHotkeyDropdown());

		if (selectOption.equals("Enable")) {
			log.info("Functional Hotkey already enabled. Doing nothing");
		} else {
			log.info("Functional hotkey is not enabled. Enabling");
			SeleniumActions.seleniumDropdown(driver, Devices.getFunctionalHotkeyDropdown(), "Enable");
			SeleniumActions.seleniumClick(driver, Devices.deviceSettingsApply);
			// Devices.applyBtn(driver).click();
			Alert alert = driver.switchTo().alert();
			alert.accept();
			log.info("Device Settings - Apply Button clicked and Alert accepted");

		}
	}

	public void disableOSDTimer(WebDriver driver) throws InterruptedException {
		Landingpage.devicesTab(driver).click();
		log.info("Devices Settings - Clicked on Devices tab");
		Thread.sleep(2000);
		Landingpage.devicesSettings(driver).click();
		log.info("Devices Settings - Clicked on Devices > Settings tab");
		Thread.sleep(2000);
		// get text from button to determine if timer is enabled or not
		String buttonText = SeleniumActions.seleniumGetText(driver, Devices.osdInactivityTimerBtn);
		if (buttonText.contains("Disable")) {
			log.info("Timer enabled. Disabling");
			SeleniumActions.seleniumClick(driver, Devices.osdInactivityTimerBtn);
		} else {
			log.info("Timer already disabled. Doing nothing");
		}
		SeleniumActions.seleniumClick(driver, Devices.deviceSettingsApply);
		// Devices.applyBtn(driver).click();
		Alert alert = driver.switchTo().alert();
		alert.accept();
		log.info("Device Settings - Apply Button clicked and Alert accepted");

	}

	public void disableInactivityTimer(WebDriver driver) throws InterruptedException {
		Landingpage.devicesTab(driver).click();
		log.info("Devices Settings - Clicked on Devices tab");
		Thread.sleep(2000);
		Landingpage.devicesSettings(driver).click();
		log.info("Devices Settings - Clicked on Devices > Settings tab");
		Thread.sleep(2000);
		// get text from button to determine if timer is enabled or not
		String buttonText = SeleniumActions.seleniumGetText(driver, Devices.connectionInactivityTimerBtn);
		if (buttonText.contains("Disable")) {
			log.info("Timer enabled. Disabling");
			SeleniumActions.seleniumClick(driver, Devices.connectionInactivityTimerBtn);
		} else {
			log.info("Timer already disabled. Doing nothing");
		}
		SeleniumActions.seleniumClick(driver, Devices.deviceSettingsApply);
		// Devices.applyBtn(driver).click();
		Alert alert = driver.switchTo().alert();
		alert.accept();
		log.info("Device Settings - Apply Button clicked and Alert accepted");

	}

	public String changeInactivityTimer(WebDriver driver) throws InterruptedException {
		Landingpage.devicesTab(driver).click();
		log.info("Devices Settings - Clicked on Devices tab");
		Thread.sleep(2000);
		Landingpage.devicesSettings(driver).click();
		log.info("Devices Settings - Clicked on Devices > Settings tab");
		Thread.sleep(2000);
		// get text from button to determine if timer is enabled or not
		String buttonText = SeleniumActions.seleniumGetText(driver, Devices.connectionInactivityTimerBtn);
		// generate random number to change the timer to
		int min = 2;
		int max = 60;
		Random number = new Random();
		int generatedNumber = number.nextInt((max - min) + 1) + min;
		String numberString = Integer.toString(generatedNumber);
		log.info("Setting value to:" + generatedNumber);
		if (buttonText.contains("Disable")) {
			// if (Devices.connectionInactivityRange(driver).isDisplayed()) {
			log.info("Connection Inactivity Timer is Enabled.. changing timer value");
			SeleniumActions.seleniumDropdown(driver, Devices.connectionInactivityRange, numberString);
			// SeleniumActions.seleniumClick(driver, Devices.connectionInactivityTimerBtn);
			// //Devices.connectionInactivityTimerBtn(driver).click();
			// timer(driver);
			//
			// Assert.assertFalse(SeleniumActions.seleniumIsDisplayed(driver,
			// Devices.connectionInactivityRange),
			// "***** Connection Inactivity Range dropbox is visible *****");
			//// Assert.assertFalse(Devices.connectionInactivityRange(driver).isDisplayed(),
			//// "***** Connection Inactivity Range dropbox is visible *****");
			// log.info("Devices Settings - Connection Inactivity Timer Disabled");
		} else {
			log.info("Connection Inactivity Timer is Disabled.. Enabling..");
			SeleniumActions.seleniumClick(driver, Devices.connectionInactivityTimerBtn);
			SeleniumActions.seleniumDropdown(driver, Devices.connectionInactivityRange, numberString);
		}
		SeleniumActions.seleniumClick(driver, Devices.deviceSettingsApply);
		// Devices.applyBtn(driver).click();
		Alert alert = driver.switchTo().alert();
		alert.accept();
		log.info("Device Settings - Apply Button clicked and Alert accepted");
		return numberString;
	}

	public void newUpgrade(WebDriver driver, String upgradeVersion, String deviceType, String[] deviceIps)
			throws InterruptedException {

		log.info("All devices must be on the same version and of the same type");

		activePEReceiverRelease(driver, upgradeVersion);
		activePETransmitterRelease(driver, upgradeVersion);

		log.info("Starting upgrade");
		navigateToUpgrade(driver);
		SeleniumActions.seleniumClick(driver, Devices.getSelectAllDevicesUpgrade());
		String upgradeStyleBefore = getUpgradeButtonStyle(driver);
		SeleniumActions.seleniumClick(driver, Devices.upgradeBtn);
		Alert alert = driver.switchTo().alert();
		alert.accept();
		new WebDriverWait(driver, 60).until(
				ExpectedConditions.visibilityOfElementLocated(By.xpath(Devices.getSaveSystemProperyToastXpath())));
		// String toastStart = SeleniumActions.seleniumGetText(driver,
		// Devices.getSaveSystemProperyToastXpath());
		// Assert.assertTrue(toastStart.contains("Success"), "Toast message did not
		// contain upgrades have started, actual" + toastStart);
		// new WebDriverWait(driver,
		// 360).until(ExpectedConditions.elementToBeClickable(By.xpath(Devices.upgradeBtn)));
		int counter = 0;
		String upgradeStyleAfter = "";
		boolean isDisabled = true;
		while (isDisabled && counter < 40) {
			Thread.sleep(10000);
			upgradeStyleAfter = getUpgradeButtonStyle(driver);
			log.info("Upgrade enabled:" + upgradeStyleBefore);
			log.info("Upgrade disabled:" + upgradeStyleAfter);
			if (!upgradeStyleAfter.contains("disabled")) {
				log.info("MATCH");
				isDisabled = false;
			}
			counter++;
		}

		if (counter > 40) {
			log.info("Upgrade took longer than 6 minutes. Failing");
			throw new AssertionError("Upgrade took longer than 6 minutes. Failing");
		}

		String version1 = getDeviceVersionFromUi(driver, deviceIps[0]);
		String version2 = getDeviceVersionFromUi(driver, deviceIps[1]);
		String version3 = getDeviceVersionFromUi(driver, deviceIps[2]);
		String version4 = getDeviceVersionFromUi(driver, deviceIps[3]);

		Assert.assertTrue(version1.equals(upgradeVersion));
		Assert.assertTrue(version2.equals(upgradeVersion));
		Assert.assertTrue(version3.equals(upgradeVersion));
		Assert.assertTrue(version4.equals(upgradeVersion));

	}

	public String getUpgradeButtonStyle(WebDriver driver) {
		String att = SeleniumActions.getAttribute(driver, Devices.upgradeBtn, "class");
		log.info("class:" + att);
		return att;
	}

	public String getDeviceVersionFromUi(WebDriver driver, String deviceIp) throws InterruptedException {
		navigateToUpgrade(driver);
		SeleniumActions.seleniumSendKeysClear(driver, Devices.selectDevicesSearchBox);
		SeleniumActions.seleniumSendKeys(driver, Devices.selectDevicesSearchBox, deviceIp);
		String version = SeleniumActions.seleniumGetText(driver, Devices.getDeviceVersion());
		log.info("Device version from table:" + version);
		return version;
	}

	private void activePEReceiverRelease(WebDriver driver, String upgradeVersion)
			throws InterruptedException, AssertionError {
		navigateToUpgrade(driver);
		SeleniumActions.seleniumClick(driver, Devices.releasesTab);
		log.info("Activating PE Receiver software");
		SeleniumActions.seleniumClick(driver, Devices.getPeReceiverTab());
		new WebDriverWait(driver, 60).until(
				ExpectedConditions.visibilityOfElementLocated(By.xpath(Devices.getPeUpgradeSearchBoxReceiver())));
		SeleniumActions.seleniumSendKeys(driver, Devices.getPeUpgradeSearchBoxReceiver(), upgradeVersion);
		SeleniumActions.seleniumClick(driver, Devices.getPeReceiverActiveDropdown());
		new WebDriverWait(driver, 60)
				.until(ExpectedConditions.elementToBeClickable(By.xpath(Devices.getActivateReleaseLinkPeReceiver())));
		SeleniumActions.seleniumClick(driver, Devices.getActivateReleaseLinkPeReceiver());
		Alert alert = driver.switchTo().alert();
		alert.accept();
		new WebDriverWait(driver, 120).until(ExpectedConditions.visibilityOf(Landingpage.spinner(driver)));
		new WebDriverWait(driver, 120).until(ExpectedConditions.invisibilityOf(Landingpage.spinner(driver)));
		log.info("Version activated. checking ");
		SeleniumActions.seleniumClick(driver, Devices.releasesTab);
		log.info("Activating PE Receiver software");
		SeleniumActions.seleniumClick(driver, Devices.getPeReceiverTab());
		new WebDriverWait(driver, 60).until(
				ExpectedConditions.visibilityOfElementLocated(By.xpath(Devices.getPeUpgradeSearchBoxReceiver())));
		SeleniumActions.seleniumSendKeys(driver, Devices.getPeUpgradeSearchBoxReceiver(), upgradeVersion);
		String isActivated = SeleniumActions.seleniumGetText(driver, Devices.getPeReceiverActiveReleaseTable());
		log.info("Is Activeated:" + isActivated);
		if (isActivated.contains("Yes")) {
			log.info("Release is activated");
		} else {
			throw new AssertionError("The release was not marked as activated by boxilla");
		}
	}

	private void activePETransmitterRelease(WebDriver driver, String upgradeVersion)
			throws InterruptedException, AssertionError {
		navigateToUpgrade(driver);
		SeleniumActions.seleniumClick(driver, Devices.releasesTab);
		log.info("Activating PE Transmitter software");
		SeleniumActions.seleniumClick(driver, Devices.getPeTransmitterTab());
		new WebDriverWait(driver, 60).until(
				ExpectedConditions.visibilityOfElementLocated(By.xpath(Devices.getPeUpgradeSearchBoxTransmitter())));
		SeleniumActions.seleniumSendKeys(driver, Devices.getPeUpgradeSearchBoxTransmitter(), upgradeVersion);
		SeleniumActions.seleniumClick(driver, Devices.getPeTransmitterActiveDropdown());
		new WebDriverWait(driver, 60)
				.until(ExpectedConditions.elementToBeClickable(By.xpath(Devices.getActivateReleaseLinkPeReceiver())));
		SeleniumActions.seleniumClick(driver, Devices.getActivateReleaseLinkPeReceiver());
		Alert alert = driver.switchTo().alert();
		alert.accept();
		new WebDriverWait(driver, 120).until(ExpectedConditions.visibilityOf(Landingpage.spinner(driver)));
		new WebDriverWait(driver, 120).until(ExpectedConditions.invisibilityOf(Landingpage.spinner(driver)));
		log.info("Version activated. checking ");
		SeleniumActions.seleniumClick(driver, Devices.releasesTab);
		log.info("Activating PE Receiver software");
		SeleniumActions.seleniumClick(driver, Devices.getPeTransmitterTab());
		new WebDriverWait(driver, 60).until(
				ExpectedConditions.visibilityOfElementLocated(By.xpath(Devices.getPeUpgradeSearchBoxTransmitter())));
		SeleniumActions.seleniumSendKeys(driver, Devices.getPeUpgradeSearchBoxTransmitter(), upgradeVersion);
		String isActivated = SeleniumActions.seleniumGetText(driver, Devices.getPeTransmitterActiveReleaseTable());
		log.info("Is Activeated:" + isActivated);
		if (isActivated.contains("Yes")) {
			log.info("Release is activated");
		} else {
			throw new AssertionError("The release was not marked as activated by boxilla");
		}
	}

	/**
	 * Used to test timings. Assumptions: upgrade version already exists on boxilla.
	 * Emerald devices
	 * 
	 * @param upgradeVersion
	 * @throws InterruptedException
	 */
	public void upgradeAll(WebDriver driver, String upgradeVersion) throws InterruptedException {
		log.info("Upgrading all devices");
		navigateToUpgrade(driver);
		SeleniumActions.seleniumClick(driver, Devices.releasesTab);
		timer(driver);
		// log.info("Activating emerald TX release");
		// Devices.emeraldReleaseTabTransmitter(driver).click();
		// timer(driver);
		// Devices.searchBoxEmeraldTx(driver).clear();
		// Devices.searchBoxEmeraldTx(driver).sendKeys(upgradeVersion);
		// timer(driver);
		// log.info("Activating Version : Version number entered in the search box");
		// SeleniumActions.seleniumClick(driver, Devices.breadCrumbBtnEmeraldTX);
		// timer(driver);
		// //Devices.breadCrumbBtnEmeraldTX(driver).click();
		// log.info("Upgrading device : Breadcrumb button clicked");
		// timer(driver);
		// SeleniumActions.seleniumClick(driver, Devices.activateVersionEmeraldTX);
		// timer(driver);
		// Alert alert = driver.switchTo().alert();
		// alert.accept();
		// log.info("Emerald TX version activated.");
		// log.info("Activating emerald RX version");
		// timer(driver);
		// timer(driver);
		// timer(driver);
		// //RX
		// navigateToUpgrade(driver);
		// SeleniumActions.seleniumClick(driver, Devices.releasesTab);
		// timer(driver);
		// Devices.emeraldReleaseTabReceiver(driver).click();
		// timer(driver);
		// Devices.searchBoxEmeraldRx(driver).clear();
		// Devices.searchBoxEmeraldRx(driver).sendKeys(upgradeVersion);
		// timer(driver);
		// SeleniumActions.seleniumClick(driver, Devices.breadCrumbBtnEmeraldRX);
		// //Devices.breadCrumbBtnEmeraldTX(driver).click();
		// log.info("Upgrading device : Breadcrumb button clicked");
		// timer(driver);
		// SeleniumActions.seleniumClick(driver, Devices.activateVersionEmeraldRX);
		// timer(driver);
		// Alert alert2 = driver.switchTo().alert();
		// alert2.accept();
		// log.info("Emerald RX version activated");
		activateVersion(driver, "tx", upgradeVersion);

		log.info("TX ACTIVATED");

		activateVersion(driver, "rx", upgradeVersion);

		log.info("RX ACTIVATED");

		log.info("Starting upgrade");
		timer(driver);
		timer(driver);
		timer(driver);
		navigateToUpgrade(driver);
		timer(driver);
		SeleniumActions.seleniumClick(driver, Devices.upgradeBtn);
		Alert alert3 = driver.switchTo().alert();
		alert3.accept();
		// Thread t1 = new Thread() {
		//
		// public void run() {
		// try {
		// getTime(driver);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// };
		// t1.start();
	}

	private void getTime(WebDriver driver) throws InterruptedException {
		Date startTransferring = new Date();
		Thread.sleep(7000);
		log.info(SeleniumActions.seleniumGetText(driver, Devices.upgradeTable));
		while (SeleniumActions.seleniumGetText(driver, Devices.upgradeTable).contains("Transferring")) {
			log.info("transferring");
			Thread.sleep(300);
		}
		Date endTransferring = new Date();
		int numSeconds = (int) ((endTransferring.getTime() - startTransferring.getTime()) / 1000);
		log.info("Trasnsferring took " + numSeconds + " seconds");
		Date startExtracting = new Date();
		while (SeleniumActions.seleniumGetText(driver, Devices.upgradeTable).contains("Extracting")) {
			log.info("Extracting");
			Thread.sleep(300);
		}
		Date endExtracting = new Date();
		int numSeconds2 = (int) ((endExtracting.getTime() - startExtracting.getTime()) / 1000);
		log.info("Extracting took " + numSeconds2 + " seconds");
		Date startRebooting = new Date();
		while (SeleniumActions.seleniumGetText(driver, Devices.upgradeTable).contains("Rebooting")) {
			log.info("Rebooting");
			Thread.sleep(300);
		}
		Date endRebooting = new Date();
		int numSeconds3 = (int) ((endRebooting.getTime() - startRebooting.getTime()) / 1000);
		log.info("Extracting took " + numSeconds3 + " seconds");

		Date startNoUpgrade = new Date();
		while (!SeleniumActions.seleniumGetText(driver, Devices.upgradeTable).contains("No Upgrade")) {
			log.info("Rebooting");
			Thread.sleep(300);
		}
		Date endNoUpgrade = new Date();
		int numSeconds4 = (int) ((endNoUpgrade.getTime() - startNoUpgrade.getTime()) / 1000);
		int total = numSeconds + numSeconds2 + numSeconds3 + numSeconds4;
		log.info("Total Upgrade Time: " + total);
	}

	public String getAllDeviceTable(WebDriver driver) throws InterruptedException {
		timer(driver);
		driver.navigate().refresh();
		Thread.sleep(7000);
		SeleniumActions.seleniumClick(driver, Devices.selectDevices);
		// Devices.selectDevices(driver).click();
		log.info("Upgrading device : Select Devices tab clicked.");
		timer(driver);

		// check entire table for versions
		String upgradeTable = SeleniumActions.seleniumGetText(driver, Devices.getUpgradeTableEntire());
		log.info("upgrade table:" + upgradeTable);
		return upgradeTable;

	}

	private void SearchForDevice(WebDriver driver, String applianceName, String applianceIP) {
		SeleniumActions.seleniumClick(driver, Devices.selectDevices);
		SeleniumActions.seleniumSendKeysClear(driver, Devices.selectDevicesSearchBox);
		SeleniumActions.seleniumSendKeys(driver, Devices.selectDevicesSearchBox, applianceIP);
	}

	// appliance = type of appliance
	// applianceIP
	// newVersion
	// oldVersion
	private boolean CanUpgradeDevice(WebDriver driver, String applianceName, String applianceIP) {
		// boolean upgradStatus=false;
		SearchForDevice(driver, applianceName, applianceIP);
		return SeleniumActions.seleniumGetText(driver, Devices.state).contains("Mis-match to Active Version");
		// if(SeleniumActions.seleniumGetText(driver, Devices.state).contains("Mis-match
		// to Active Version") ) {//|| SeleniumActions.seleniumGetText(driver,
		// Devices.state).contains("Idle")) {
		// upgradStatus=true;
		// }
		// return upgradStatus;
	}

	// returns version specified by boxilla
	private String GetDeviceVersion(WebDriver driver, String applianceName, String applianceIP) {
		SearchForDevice(driver, applianceName, applianceIP);
		return Devices.firmwareVersion(driver, applianceName).getText();
	}

	private void ActivateApplianceFirmwareVersion(WebDriver driver, String appliance, String version)
			throws InterruptedException {
		SeleniumActions.seleniumClick(driver, Devices.releasesTab);
		log.info("Upgrading device : Releases tab clicked");
		timer(driver);
		Devices.deviceReleaseTab(driver, appliance).click();

		if (StartupTestCase.isEmerald) {
			System.out.println("****** ACTIVATING VERSION: " + version + " FOR 4K device ************");
			activateVersionEmerald(driver, appliance, version);
		} else if (StartupTestCase.isEmeraldSe) {
			System.out.println("****** ACTIVATING VERSION: " + version + " FOR SE device ************");
			activateVersionEmeraldSe(driver, appliance, version); // Activating v4.1
		} else if (StartupTestCase.isZeroU) {
			System.out.println("****** ACTIVATING VERSION: " + version + " FOR Zero U device ************");
			activateVersionZeroU(driver, appliance, version);
		} else {
			Assert.fail("Unable to determine appliance version, check config.");
		}
		System.out.println("Upgrading device : Version " + version + " Activated..");
	}

	private void AssertDeviceHasExpectedFirmware(String applianceIP, String expectedVersion) {
		String password = "barrow1admin_12";
		if (expectedVersion.contains("5.2.5")) {
			password = "Witcher1@B_Box";
		}
		try {
			Ssh ssh = new Ssh("root", password, applianceIP);
			ssh.loginToServer();
			String version = ssh.sendCommand("cat /VERSION");
			ssh.disconnect();
			log.info("VERSION FROM DEVICE:" + version);
			version = version.trim();
			expectedVersion = expectedVersion.trim();
			Assert.assertTrue(version.equalsIgnoreCase(expectedVersion),
					"Version on device differes from boxilla, expected:" + expectedVersion + " actual:" + version);
		} catch (Exception e) {
			log.info("MAYBE A PROBLEM WITH THE VERSION. iF THE TEST DIDNT FAIL CHECK.");
		}
	}

	public void upgradeDevices(WebDriver driver, String appliance1, String appliance2, String applianceIP1,
			String applianceIP2, String newVersion, String oldVersion) throws InterruptedException {

		// if idle or noUpgradeRequired activate certain version else complete upgrade
		// capture current version compare at the end

		timer(driver);
		driver.navigate().refresh();

		Thread.sleep(1000);

		boolean canUpgradeDevice1 = CanUpgradeDevice(driver, appliance1, applianceIP1);
		boolean canUpgradeDevice2 = CanUpgradeDevice(driver, appliance2, applianceIP2);

		String currentDevice1Version = GetDeviceVersion(driver, appliance1, applianceIP1);
		String currentDevice2Version = GetDeviceVersion(driver, appliance2, applianceIP2);
		System.out.println("current version of Device 1 is " + currentDevice1Version);
		System.out.println("current version of Device 2 is " + currentDevice2Version);
		// init for furhter assertions
		String expectedDevice1Version = currentDevice1Version;
		String expectedDevice2Version = currentDevice2Version;

		if (!canUpgradeDevice1) { // activate version for device 1
			// if(canUpgradeDevice1) {
			timer(driver);
			if (currentDevice1Version.equalsIgnoreCase(newVersion)) {
				ActivateApplianceFirmwareVersion(driver, appliance1, oldVersion);
				expectedDevice1Version = oldVersion;
			} else { 
				ActivateApplianceFirmwareVersion(driver, appliance1, newVersion);
				expectedDevice1Version = newVersion;
			}
		}

		if (!canUpgradeDevice2) { // activate version for device 2
			timer(driver);
			if (currentDevice2Version.equalsIgnoreCase(newVersion)) {
				ActivateApplianceFirmwareVersion(driver, appliance2, oldVersion);
				expectedDevice2Version = oldVersion;
			} else {
				ActivateApplianceFirmwareVersion(driver, appliance2, newVersion);
				expectedDevice2Version = newVersion;
			}
		}

		// recheck versions
		driver.navigate().refresh(); // the refresh is required for xpath to work for
		// some reason

		Thread.sleep(1000);

		canUpgradeDevice1 = CanUpgradeDevice(driver, appliance1, applianceIP1);
		canUpgradeDevice2 = CanUpgradeDevice(driver, appliance2, applianceIP2);
		driver.navigate().refresh(); // must clear the search box at this stage,
		// default method doesn't trigger table refresh for some reason
		Thread.sleep(5000);
		if (canUpgradeDevice1 && canUpgradeDevice2) {
			completeUpgradeAll(driver, applianceIP1, applianceIP2);

			// assert version from dev ssh equals to expected
			Thread.sleep(5000);
			log.info(String.format("Comparing actual firmware version from dev[%s] ssh, to expected version",
					applianceIP1));
			AssertDeviceHasExpectedFirmware(applianceIP1, expectedDevice1Version);
			log.info(String.format("Comparing actual firmware version from dev[%s] ssh, to expected version",
					applianceIP2));
			AssertDeviceHasExpectedFirmware(applianceIP2, expectedDevice2Version);

		} else {
			log.info("Version activated, rechecking appliances state.");
			if (!canUpgradeDevice1)
				Assert.fail("Device " + appliance1 + "status is not equal to mismatch to active version.");
			else if (!canUpgradeDevice2)
				Assert.fail("Device " + appliance2 + "status is not equal to mismatch to active version.");
			else {
				Assert.fail("Device " + appliance1 + " and " + appliance2
						+ " status is not equal to mismatch to active version.");
			}

		}
	}

	/**
	 * Upgrade a device
	 * 
	 * @param driver
	 * @param appliance
	 * @param applianceIP
	 * @param newVersion
	 * @param oldVersion
	 * @param middleVersion
	 * @throws InterruptedException
	 */
	public void upgradeDevice(WebDriver driver, String appliance, String applianceIP, String newVersion,
			String oldVersion) throws InterruptedException {

		/*
		 * Search Device using IP - Check current state - if ----> State is "Ideal" or
		 * "No upgrade required" Find out current firmware version - if version is 4.1
		 * activate version 4.2 and vice versa - assert state is equal to Mis-match to
		 * active version - upgrade if version is neither 4.1 or 4.2 activate 4.2 and
		 * upgrade else if ---> State is "Mis-Match to Active Version" - Execute Upgrade
		 */

		timer(driver);
		driver.navigate().refresh();

		Thread.sleep(7000);
		SeleniumActions.seleniumClick(driver, Devices.selectDevices);
		// Devices.selectDevices(driver).click();
		log.info("Upgrading device : Select Devices tab clicked.");
		timer(driver);
		log.info("Upgrading device : Checking Firmware version of " + appliance + " device");
		SeleniumActions.seleniumSendKeysClear(driver, Devices.selectDevicesSearchBox);
		Thread.sleep(3000);
		// Devices.selectDevicesSearchBox(driver).clear();
		SeleniumActions.seleniumSendKeys(driver, Devices.selectDevicesSearchBox, applianceIP);

		// Devices.selectDevicesSearchBox(driver).sendKeys(applianceIP);
		log.info("Upgrading device : Device searched using IP address");

		// log.info(Devices.state(driver).getText().contains("No Upgrade
		// Required"));
		// log.info(Devices.state(driver).getText());

		if (SeleniumActions.seleniumGetText(driver, Devices.state).contains("No Upgrade Required")
				|| SeleniumActions.seleniumGetText(driver, Devices.state).contains("Idle")) {
			// if (Devices.state(driver).getText().contains("No Upgrade Required")
			// || Devices.state(driver).getText().contains("Idle")) {

			timer(driver);
			String firmware = Devices.firmwareVersion(driver, appliance).getText();
			timer(driver);
			log.info("Firmware from boxilla:" + firmware);
			log.info("Firmware new:" + newVersion);
			if (firmware.equalsIgnoreCase(newVersion)) {
				/*
				 * Assert.assertTrue(Devices.state(driver).getText().
				 * contains("No Upgrade Required") ||
				 * Devices.state(driver).getText().contains("Idle"),
				 * "***** State Missmatch *****");
				 */
				log.info("Upgraing device : current version is " + newVersion + " Trying to activate " + oldVersion);
				timer(driver);
				SeleniumActions.seleniumClick(driver, Devices.releasesTab);
				// Devices.releasesTab(driver).click();
				log.info("Upgrading device : Releases tab clicked");
				timer(driver);

				Devices.deviceReleaseTab(driver, appliance).click();
				log.info("Upgrading device : Device releases tab clciked ");

				if (StartupTestCase.isEmerald) {
					System.out.println("****** USING EMERALD ************");
					activateVersionEmerald(driver, appliance, oldVersion);
				} else if (StartupTestCase.isEmeraldSe) {
					System.out.println("****** USING EMERALD SE ************");
					activateVersionEmeraldSe(driver, appliance, oldVersion); // Activating v4.1
				} else if (StartupTestCase.isZeroU) {
					log.info("***** USING ZERO U *******************");
					activateVersionZeroU(driver, appliance, oldVersion);
				} else {
					System.out.println("****** NOT USING EMERALD ************");
					activateVersion(driver, appliance, oldVersion); // Activating v4.1
				}
				System.out
						.println("Upgrading device : Version " + oldVersion + " Activated.. Re checking Device State");
				// Re checking appliance version and state
				timer(driver);
				SeleniumActions.seleniumClick(driver, Devices.selectDevices);
				// Devices.selectDevices(driver).click();
				log.info("Upgrading device : Select Devices tab clicked.");
				timer(driver);
				// driver.navigate().refresh(); //refresh the page or else the xpath for the
				// search box will have changed
				driver.get(driver.getCurrentUrl());
				SeleniumActions.seleniumSendKeysClear(driver, Devices.selectDevicesSearchBox);
				// Devices.selectDevicesSearchBox(driver).clear();
				SeleniumActions.seleniumSendKeys(driver, Devices.selectDevicesSearchBox, applianceIP);
				// Devices.selectDevicesSearchBox(driver).sendKeys(applianceIP);
				log.info("Upgrading device : Device searched using IP address");
				timer(driver);
				String deviceUpgradeTable = SeleniumActions.seleniumGetText(driver, Devices.upgradeTable);
				log.info("Text from upgrade table:" + deviceUpgradeTable);
				// String deviceUpgradeTable = Devices.upgradeTable(driver).getText();
				Assert.assertTrue(deviceUpgradeTable.contains("Mis-match to Active Version"),
						"Device upgrade table did not contain: Mis-match to Active Version, actual text: "
								+ deviceUpgradeTable);
				log.info("Upgrading device : Device state is Mis-match.. Starting upgrade");
				timer(driver);
				completeUpgrade(driver, applianceIP);
				// if(deviceUpgradeTable.contains("Mis-match to Active Version"))
				// {
				// log.info("Upgrading device : Device state is Mis-match.. Starting upgrade");
				// timer(driver);
				// completeUpgrade(driver, applianceIP);
				// }
				// else if (deviceUpgradeTable.contains("No Upgrade Required")||
				// deviceUpgradeTable.contains("idle")){
				// Assert.assertTrue(deviceUpgradeTable.contains("No Upgrade Required"),
				// "Device state did not contain: No Upgrade Required, actual text: " +
				// deviceUpgradeTable);
				// log.info("Upgrading Device : Upgrade Asserted");
				// }
			} else if (firmware.equalsIgnoreCase(oldVersion)) {

				log.info("Upgraing device : current version is " + oldVersion + " Trying to activate " + newVersion);
				timer(driver);
				SeleniumActions.seleniumClick(driver, Devices.releasesTab);
				// Devices.releasesTab(driver).click();
				log.info("Upgrading device : Releases tab clicked");
				timer(driver);
				Devices.deviceReleaseTab(driver, appliance).click();
				log.info("Upgrading device : Device releases tab clciked ");

				if (StartupTestCase.isEmerald) {

					System.out.println("****** USING EMERALD ************");
					// activateVersionEmerald(driver, appliance, middleVersion);
					activateVersionEmerald(driver, appliance, newVersion);
				} else if (StartupTestCase.isEmeraldSe) {
					activateVersionEmeraldSe(driver, appliance, newVersion);
				} else if (StartupTestCase.isZeroU) {
					activateVersionZeroU(driver, appliance, newVersion);
				} else {
					System.out.println("****** NOT USING EMERALD ************");
					activateVersion(driver, appliance, newVersion); // Activating v4.1
				}
				System.out
						.println("Upgrading device : Version " + newVersion + " Activated.. Re checking Device State");
				// Re checking appliance version and state
				timer(driver);
				SeleniumActions.seleniumClick(driver, Devices.selectDevices);
				// Devices.selectDevices(driver).click();
				log.info("Upgrading device : Select Devices tab clicked.");
				timer(driver);
				driver.navigate().refresh();
				SeleniumActions.seleniumSendKeysClear(driver, Devices.selectDevicesSearchBox);
				// Devices.selectDevicesSearchBox(driver).clear();
				SeleniumActions.seleniumSendKeys(driver, Devices.selectDevicesSearchBox, applianceIP);
				// Devices.selectDevicesSearchBox(driver).sendKeys(applianceIP);
				log.info("Upgrading device : Device searched using IP address");
				timer(driver);
				String deviceUpgradeTable = SeleniumActions.seleniumGetText(driver, Devices.upgradeTable);
				// String deviceUpgradeTable = Devices.upgradeTable(driver).getText();
				Assert.assertTrue(deviceUpgradeTable.contains("Mis-match to Active Version"),
						"Device upgrade table did not contain: Mis-match to Active Version, actual text: "
								+ deviceUpgradeTable);
				log.info("Upgrading device : Device state is Mis-match.. Starting upgrade");
				timer(driver);
				completeUpgrade(driver, applianceIP);
			} else {
				log.info("Version is neither " + newVersion + " nor " + oldVersion + ".. Activating " + newVersion);
				if (StartupTestCase.isEmerald) {

					System.out.println("****** USING EMERALD ************");
					activateVersionEmerald(driver, appliance, newVersion);
				} else if (StartupTestCase.isEmeraldSe) {
					activateVersionEmeraldSe(driver, appliance, newVersion);
				} else if (StartupTestCase.isZeroU) {
					activateVersionZeroU(driver, appliance, newVersion);
				} else {
					System.out.println("****** NOT USING EMERALD ************");
					activateVersion(driver, appliance, newVersion); // Activating v4.1
				}
				timer(driver);
				driver.navigate().refresh();
				Thread.sleep(7000);
				SeleniumActions.seleniumSendKeysClear(driver, Devices.selectDevicesSearchBox);
				// Devices.selectDevicesSearchBox(driver).clear();
				SeleniumActions.seleniumSendKeys(driver, Devices.selectDevicesSearchBox, applianceIP);
				// Devices.selectDevicesSearchBox(driver).sendKeys(applianceIP);
				log.info("Upgrading device : Device searched using IP address");
				log.info("Upgrading device : Device searched using IP address");
				String deviceUpgradeTable2 = SeleniumActions.seleniumGetText(driver, Devices.upgradeTable);
				// String deviceUpgradeTable2 = Devices.upgradeTable(driver).getText();
				Assert.assertTrue(deviceUpgradeTable2.contains("Mis-match to Active Version"),
						"Device upgrade table did not contain: Mis-match to Active Version, actual text: "
								+ deviceUpgradeTable2);
				completeUpgrade(driver, applianceIP);
			}
		} else if (SeleniumActions.seleniumGetText(driver, Devices.upgradeTable)
				.contains("Mis-match to Active Version")) {

			// } else if (Devices.upgradeTable(driver).getText().contains("Mis-match to
			// Active Version")) {
			driver.navigate().refresh();
			Thread.sleep(7000);
			SeleniumActions.seleniumSendKeysClear(driver, Devices.selectDevicesSearchBox);
			// Devices.selectDevicesSearchBox(driver).clear();
			SeleniumActions.seleniumSendKeys(driver, Devices.selectDevicesSearchBox, applianceIP);
			completeUpgrade(driver, applianceIP);
		}
	}

	public String getE2Read(String deviceIp, String userName, String password) {
		Ssh ssh = new Ssh(userName, password, deviceIp);
		ssh.loginToServer();
		String e2 = ssh.sendCommand("e2_read");
		log.info("E2_Read:" + e2);
		if (!e2.contains("Information")) {
			e2 = ssh.sendCommand("/opt/cloudium/hw_scripts/factory/e2_read.elf");
			log.info("E2_Read:" + e2);
		}
		ssh.disconnect();
		return e2;
	}

	public String getDeviceSwVersion(String deviceIp, String userName, String password) {
		Ssh ssh = new Ssh(userName, password, deviceIp);
		ssh.loginToServer();
		String version = ssh.sendCommand("cat /VERSION");
		version = version.replace("\n", ""); // replace line breals
		ssh.disconnect();
		return version;
	}

	public String getSerialNumber(String ipAddress, String username, String password) {
		String out = getE2Read(ipAddress, username, password);
		String[] split = out.split("SEBB");
		String[] split2 = split[1].split("\\s+");
		log.info("Serial Number:" + "SEBB" + split2[0]);
		return "SEBB" + split2[0];
	}

	public String getMpn(String ip, String username, String password) {
		String out = getE2Read(ip, username, password);
		String[] split = out.split("300-");
		String[] split2 = split[1].split("\\s+");
		log.info("MPN:300-" + split2[0]);
		return "300-" + split2[0];
	}

	public boolean checkAdUserConnection(String xml, String username, String conName) {
		Scanner scan = new Scanner(xml);
		while (scan.hasNextLine()) {
			String line = scan.nextLine();
			if (line.contains(username)) {
				while (scan.hasNextLine()) {
					line = scan.nextLine();
					if (line.contains(conName)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public void upgradeDevice1(WebDriver driver, String appliance, String applianceIP, String newVersion,
			String oldVersion, String middleVersion, String version4, String version5, String version6,
			String boxillaIp, String restUser, String restPassword, String device_name, String edid_settings_dvi1,
			String hid_configurations, String mouse_keyboard_timeout, String video_quality, String hid_configurations1,String audio_source) throws InterruptedException {

		/*
		 * Search Device using IP - Check current state - if ----> State is "Ideal" or
		 * "No upgrade required" Find out current firmware version - if version is 4.1
		 * activate version 4.2 and vice versa - assert state is equal to Mis-match to
		 * active version - upgrade if version is neither 4.1 or 4.2 activate 4.2 and
		 * upgrade else if ---> State is "Mis-Match to Active Version" - Execute Upgrade
		 */

		timer(driver);
		driver.navigate().refresh();

		SeleniumActions.seleniumClick(driver, Devices.selectDevices);
		// Devices.selectDevices(driver).click();
		log.info("Upgrading device : Select Devices tab clicked.");
		timer(driver);
		log.info("Upgrading device : Checking Firmware version of " + appliance + " device");
		SeleniumActions.seleniumSendKeysClear(driver, Devices.selectDevicesSearchBox);
		SeleniumActions.seleniumSendKeys(driver, Devices.selectDevicesSearchBox, applianceIP);
		Thread.sleep(3000);
		// it will check the devices state before upgrading and downgrading
		if (SeleniumActions.seleniumGetText(driver, Devices.state).contains("Mis-match to Active Version")) 
		{
			log.info("Device Updrading");
			completeUpgrade(driver, applianceIP);

		}

		else if (SeleniumActions.seleniumGetText(driver, Devices.state).contains("No Upgrade Required")|| SeleniumActions.seleniumGetText(driver, Devices.state).contains("Idle")) 
		{

			log.info("Upgrading Device : Upgrade Asserted");
		}
		timer(driver);
		String firmware = Devices.firmwareVersion(driver, appliance).getText();
		timer(driver);
		log.info("Firmware from boxilla:" + firmware);
		log.info("Firmware newversion:" + newVersion);
		log.info("firmware middleversion:" + middleVersion);
		log.info("firmware oldVersion:" + oldVersion);
		log.info("firmware version4:" + version4);
		log.info("firmware version5:" + version5);
		log.info("firmware version6:" + version6);

		String[] version = { oldVersion, middleVersion, version4, version5, version6, newVersion };

		if (!firmware.equalsIgnoreCase(oldVersion)) {
			log.info("Coverting device version into old version");

			/*
			 * Assert.assertTrue(Devices.state(driver).getText().
			 * contains("No Upgrade Required") ||
			 * Devices.state(driver).getText().contains("Idle"),
			 * "***** State Missmatch *****");
			 */
			log.info("Upgraing device : current version is " + firmware + " Trying to activate " + oldVersion);
			timer(driver);
			SeleniumActions.seleniumClick(driver, Devices.releasesTab);
			// Devices.releasesTab(driver).click();
			log.info("Upgrading device : Releases tab clicked");
			timer(driver);

			Devices.deviceReleaseTab(driver, appliance).click();
			log.info("Upgrading device : Device releases tab clciked ");

			if (StartupTestCase.isEmerald) {
				System.out.println("****** USING EMERALD ************");
				activateVersionEmerald(driver, appliance, oldVersion);
			} else if (StartupTestCase.isEmeraldSe) {
				System.out.println("****** USING EMERALD SE ************");
				activateVersionEmeraldSe(driver, appliance, oldVersion); // Activating v4.1
			} else if (StartupTestCase.isZeroU) {
				log.info("***** USING ZERO U *******************");
				activateVersionZeroU(driver, appliance, oldVersion);
			} else {
				System.out.println("****** NOT USING EMERALD ************");
				activateVersion(driver, appliance, oldVersion); // Activating v4.1
			}
			System.out.println("Upgrading device : Version " + oldVersion + " Activated.. Re checking Device State");
			// Re checking appliance version and state
			timer(driver);
			SeleniumActions.seleniumClick(driver, Devices.selectDevices);
			// Devices.selectDevices(driver).click();
			log.info("Upgrading device : Select Devices tab clicked.");
			timer(driver);
			// driver.navigate().refresh(); //refresh the page or else the xpath for the
			// search box will have changed
			driver.get(driver.getCurrentUrl());
			SeleniumActions.seleniumSendKeysClear(driver, Devices.selectDevicesSearchBox);
			// Devices.selectDevicesSearchBox(driver).clear();
			SeleniumActions.seleniumSendKeys(driver, Devices.selectDevicesSearchBox, applianceIP);

			// Devices.selectDevicesSearchBox(driver).sendKeys(applianceIP);
			log.info("Upgrading device : Device searched using IP address");
			timer(driver);
			String deviceUpgradeTable = SeleniumActions.seleniumGetText(driver, Devices.upgradeTable);
			log.info("Text from upgrade table:" + deviceUpgradeTable);
			// String deviceUpgradeTable = Devices.upgradeTable(driver).getText();
			Assert.assertTrue(deviceUpgradeTable.contains("Mis-match to Active Version"),
					"Device upgrade table did not contain: Mis-match to Active Version, actual text: "
							+ deviceUpgradeTable);
			log.info("Upgrading device : Device state is Mis-match.. Starting upgrade");
			timer(driver);
			completeUpgrade(driver, applianceIP);
			config1.checkDeviceState1(device_name, boxillaIp, restUser, restPassword);

		}

		for (int i = 0; i < version.length - 1; i++) {

			log.info("Updating the Appliance properties");
			Thread.sleep(20000);
			config.editappliancespropertiesSingle(boxillaIp, restUser, restPassword, device_name, edid_settings_dvi1,
					hid_configurations, mouse_keyboard_timeout, video_quality,audio_source);
			config1.checkDeviceState(device_name, boxillaIp, restUser, restPassword);

			log.info("Retrieving the properties of " + applianceIP +" through the API and comparing ");

			AppliancePropertiesConfig.GetProperties getProp = config1.new GetProperties();
			getProp.device_names = new String[1];
			getProp.device_names[0] = device_name;
			Integer mouseTimeout = Integer.parseInt(mouse_keyboard_timeout);
			given().auth().preemptive().basic(boxillaRestUser, boxillaRestPassword)
					.headers(BoxillaHeaders.getBoxillaHeaders()).body(getProp).when().contentType(ContentType.JSON)
					.get(config1.getUri(boxillaManager)).then().assertThat().statusCode(200)
					.body(config1.getDeviceName(0), equalTo(device_name))
					.body(config1.getDeviceHidConfigurations(0), equalTo(hid_configurations1))
					.body(config1.getDeviceEdidSettingsDvi1(0), equalTo(edid_settings_dvi1))
					.body(config1.getDeviceMouseKeyTimeout(0), equalTo(mouseTimeout));
			log.info("Succesfully retrieving the Appliance  properties of " +applianceIP+" through the API and comparing");
			/*
			 * Assert.assertTrue(Devices.state(driver).getText().
			 * contains("No Upgrade Required") ||
			 * Devices.state(driver).getText().contains("Idle"),
			 * "***** State Missmatch *****");
			 */

			Thread.sleep(15000);
			log.info("Upgraing device : current version is " + version[i] + " Trying to activate " + newVersion);

			SeleniumActions.seleniumClick(driver, Devices.releasesTab);
			// Devices.releasesTab(driver).click();
			log.info("Upgrading device : Releases tab clicked");
			timer(driver);

			Devices.deviceReleaseTab(driver, appliance).click();
			log.info("Upgrading device : Device releases tab clciked ");

			if (StartupTestCase.isEmerald) {
				System.out.println("****** USING EMERALD ************");
				activateVersionEmerald(driver, appliance, newVersion);
			} else if (StartupTestCase.isEmeraldSe) {
				System.out.println("****** USING EMERALD SE ************");
				activateVersionEmeraldSe(driver, appliance, newVersion); // Activating v4.1
			} else if (StartupTestCase.isZeroU) {
				log.info("***** USING ZERO U *******************");
				activateVersionZeroU(driver, appliance, newVersion);
			} else {
				System.out.println("****** NOT USING EMERALD ************");
				activateVersion(driver, appliance, newVersion); // Activating v4.1
			}
			System.out.println("Upgrading device : Version " + newVersion + " Activated.. Re checking Device State");
			// Re checking appliance version and state
			timer(driver);
			SeleniumActions.seleniumClick(driver, Devices.selectDevices);
			// Devices.selectDevices(driver).click();
			log.info("Upgrading device : Select Devices tab clicked.");
			timer(driver);
			// driver.navigate().refresh(); //refresh the page or else the xpath for the
			// search box will have changed
			driver.get(driver.getCurrentUrl());
			SeleniumActions.seleniumSendKeysClear(driver, Devices.selectDevicesSearchBox);
			// Devices.selectDevicesSearchBox(driver).clear();
			SeleniumActions.seleniumSendKeys(driver, Devices.selectDevicesSearchBox, applianceIP);
			// Devices.selectDevicesSearchBox(driver).sendKeys(applianceIP);
			log.info("Upgrading device : Device searched using IP address");
			timer(driver);
			String deviceUpgradeTable = SeleniumActions.seleniumGetText(driver, Devices.upgradeTable);
			log.info("Text from upgrade table:" + deviceUpgradeTable);
			// String deviceUpgradeTable = Devices.upgradeTable(driver).getText();
			Assert.assertTrue(deviceUpgradeTable.contains("Mis-match to Active Version"),
					"Device upgrade table did not contain: Mis-match to Active Version, actual text: "
							+ deviceUpgradeTable);
			log.info("Upgrading device : Device state is Mis-match.. Starting upgrade");
			timer(driver);
			completeUpgrade(driver, applianceIP);
			config1.checkDeviceState1(device_name, boxillaIp, restUser, restPassword);

			given().auth().preemptive().basic(boxillaRestUser, boxillaRestPassword)
					.headers(BoxillaHeaders.getBoxillaHeaders()).body(getProp).when().contentType(ContentType.JSON)
					.get(config1.getUri(boxillaManager)).then().assertThat().statusCode(200)
					.body(config1.getDeviceName(0), equalTo(device_name))
					.body(config1.getDeviceHidConfigurations(0), equalTo(hid_configurations1))
					.body(config1.getDeviceEdidSettingsDvi1(0), equalTo(edid_settings_dvi1))
					.body(config1.getDeviceMouseKeyTimeout(0), equalTo(mouseTimeout));

			log.info("Sucessfully Appliance properties asserted after upgrading to newVersion" +  newVersion);

			String firmware1 = Devices.firmwareVersion(driver, appliance).getText();
			if (i < version.length - 2) {

				Thread.sleep(10000);

				log.info("Upgraing device : current version is " + firmware1 + " Trying to activate " + version[i + 1]);
				timer(driver);
				SeleniumActions.seleniumClick(driver, Devices.releasesTab);
				// Devices.releasesTab(driver).click();
				log.info("Upgrading device : Releases tab clicked");
				timer(driver);

				Devices.deviceReleaseTab(driver, appliance).click();
				log.info("Upgrading device : Device releases tab clciked ");

				if (StartupTestCase.isEmerald) {
					System.out.println("****** USING EMERALD ************");
					activateVersionEmerald(driver, appliance, version[i + 1]);
				} else if (StartupTestCase.isEmeraldSe) {
					System.out.println("****** USING EMERALD SE ************");
					activateVersionEmeraldSe(driver, appliance, version[i + 1]); // Activating v4.1
				} else if (StartupTestCase.isZeroU) {
					log.info("***** USING ZERO U *******************");
					activateVersionZeroU(driver, appliance, version[i + 1]);
				} else {
					System.out.println("****** NOT USING EMERALD ************");
					activateVersion(driver, appliance, version[i + 1]); // Activating v4.1
				}
				System.out.println(
						"Upgrading device : Version " + version[i + 1] + " Activated.. Re checking Device State");
				// Re checking appliance version and state
				timer(driver);
				SeleniumActions.seleniumClick(driver, Devices.selectDevices);
				// Devices.selectDevices(driver).click();
				log.info("Upgrading device : Select Devices tab clicked.");
				timer(driver);
				// driver.navigate().refresh(); //refresh the page or else the xpath for the
				// search box will have changed
				driver.get(driver.getCurrentUrl());
				SeleniumActions.seleniumSendKeysClear(driver, Devices.selectDevicesSearchBox);
				// Devices.selectDevicesSearchBox(driver).clear();
				SeleniumActions.seleniumSendKeys(driver, Devices.selectDevicesSearchBox, applianceIP);
				// Devices.selectDevicesSearchBox(driver).sendKeys(applianceIP);
				log.info("Upgrading device : Device searched using IP address");
				timer(driver);
				String deviceUpgradeTable1 = SeleniumActions.seleniumGetText(driver, Devices.upgradeTable);
				log.info("Text from upgrade table:" + deviceUpgradeTable1);
				// String deviceUpgradeTable = Devices.upgradeTable(driver).getText();
				Assert.assertTrue(deviceUpgradeTable1.contains("Mis-match to Active Version"),
						"Device upgrade table did not contain: Mis-match to Active Version, actual text: "
								+ deviceUpgradeTable1);
				log.info("Upgrading device : Device state is Mis-match.. Starting upgrade");
				timer(driver);
				completeUpgrade(driver, applianceIP);
				config1.checkDeviceState1(device_name, boxillaIp, restUser, restPassword);
				
				
				
			}

		}

	}

	public void upgradeDevice_Dual(WebDriver driver, String appliance, String applianceIP, String newVersion,
			String oldVersion, String middleVersion, String version4, String version5, String version6,
			String boxillaIp, String restUser, String restPassword, String device_name, String edid_settings_dvi1,
			String hid_configurations, String mouse_keyboard_timeout, String video_quality, String hid_configurations1,
			String audio_source, String edid_settings_dvi2) throws InterruptedException {

		/*
		 * Search Device using IP - Check current state - if ----> State is "Ideal" or
		 * "No upgrade required" Find out current firmware version - if version is 4.1
		 * activate version 4.2 and vice versa - assert state is equal to Mis-match to
		 * active version - upgrade if version is neither 4.1 or 4.2 activate 4.2 and
		 * upgrade else if ---> State is "Mis-Match to Active Version" - Execute Upgrade
		 */
		timer(driver);
		driver.navigate().refresh();

		SeleniumActions.seleniumClick(driver, Devices.selectDevices);
		// Devices.selectDevices(driver).click();
		log.info("Upgrading device : Select Devices tab clicked.");
		timer(driver);
		log.info("Upgrading device : Checking Firmware version of " + appliance + " device");
		SeleniumActions.seleniumSendKeysClear(driver, Devices.selectDevicesSearchBox);
		SeleniumActions.seleniumSendKeys(driver, Devices.selectDevicesSearchBox, applianceIP);
		Thread.sleep(3000);
		// it will check the devices state before upgrading and downgrading
		if (SeleniumActions.seleniumGetText(driver, Devices.state).contains("Mis-match to Active Version")) 
		{
			log.info("Device Updrading");
			completeUpgrade(driver, applianceIP);

		}

		else if (SeleniumActions.seleniumGetText(driver, Devices.state).contains("No Upgrade Required")|| SeleniumActions.seleniumGetText(driver, Devices.state).contains("Idle")) 
		{

			log.info("Upgrading Device : Upgrade Asserted");
		}
			timer(driver);
			String firmware = Devices.firmwareVersion(driver, appliance).getText();
			timer(driver);
			log.info("Firmware from boxilla:" + firmware);
			log.info("Firmware new:" + newVersion);
			log.info("firmware middleversion:" + middleVersion);
			log.info("firmware oldVersion:" + oldVersion);
			log.info("firmware version4:" + version4);
			log.info("firmware version5:" + version5);
			log.info("firmware version6:" + version6);

			String[] version = { oldVersion, middleVersion, version4, version5, version6, newVersion };
			if (!firmware.equalsIgnoreCase(oldVersion)) {
				System.out.println("Whichever version is, at the end the version will be the old version");

				/*
				 * Assert.assertTrue(Devices.state(driver).getText().
				 * contains("No Upgrade Required") ||
				 * Devices.state(driver).getText().contains("Idle"),
				 * "***** State Missmatch *****");
				 */
				log.info("Upgraing device : current version is " + firmware + " Trying to activate " + oldVersion);
				timer(driver);
				SeleniumActions.seleniumClick(driver, Devices.releasesTab);
				// Devices.releasesTab(driver).click();
				log.info("Upgrading device : Releases tab clicked");
				timer(driver);

				Devices.deviceReleaseTab(driver, appliance).click();
				log.info("Upgrading device : Device releases tab clciked ");

				if (StartupTestCase.isEmerald) {
					System.out.println("****** USING EMERALD ************");
					activateVersionEmerald(driver, appliance, oldVersion);
				} else if (StartupTestCase.isEmeraldSe) {
					System.out.println("****** USING EMERALD SE ************");
					activateVersionEmeraldSe(driver, appliance, oldVersion); // Activating v4.1
				} else if (StartupTestCase.isZeroU) {
					log.info("***** USING ZERO U *******************");
					activateVersionZeroU(driver, appliance, oldVersion);
				} else {
					System.out.println("****** NOT USING EMERALD ************");
					activateVersion(driver, appliance, oldVersion); // Activating v4.1
				}
				System.out
						.println("Upgrading device : Version " + oldVersion + " Activated.. Re checking Device State");
				// Re checking appliance version and state
				timer(driver);
				SeleniumActions.seleniumClick(driver, Devices.selectDevices);
				// Devices.selectDevices(driver).click();
				log.info("Upgrading device : Select Devices tab clicked.");
				timer(driver);
				// driver.navigate().refresh(); //refresh the page or else the xpath for the
				// search box will have changed
				driver.get(driver.getCurrentUrl());
				SeleniumActions.seleniumSendKeysClear(driver, Devices.selectDevicesSearchBox);
				// Devices.selectDevicesSearchBox(driver).clear();
				SeleniumActions.seleniumSendKeys(driver, Devices.selectDevicesSearchBox, applianceIP);

				// Devices.selectDevicesSearchBox(driver).sendKeys(applianceIP);
				log.info("Upgrading device : Device searched using IP address");
				timer(driver);
				String deviceUpgradeTable = SeleniumActions.seleniumGetText(driver, Devices.upgradeTable);
				log.info("Text from upgrade table:" + deviceUpgradeTable);
				// String deviceUpgradeTable = Devices.upgradeTable(driver).getText();
				Assert.assertTrue(deviceUpgradeTable.contains("Mis-match to Active Version"),
						"Device upgrade table did not contain: Mis-match to Active Version, actual text: "
								+ deviceUpgradeTable);
				log.info("Upgrading device : Device state is Mis-match.. Starting upgrade");
				timer(driver);
				completeUpgrade(driver, applianceIP);
				// if(deviceUpgradeTable.contains("Mis-match to Active Version"))
				// {
				// log.info("Upgrading device : Device state is Mis-match.. Starting upgrade");
				// timer(driver);
				// completeUpgrade(driver, applianceIP);
				// }
				// else if (deviceUpgradeTable.contains("No Upgrade Required")||
				// deviceUpgradeTable.contains("idle")){
				// Assert.assertTrue(deviceUpgradeTable.contains("No Upgrade Required"),
				// "Device state did not contain: No Upgrade Required, actual text: " +
				// deviceUpgradeTable);
				// log.info("Upgrading Device : Upgrade Asserted");
				// }

			}

			for (int i = 0; i < version.length - 1; i++) {
				SeleniumActions.seleniumSendKeysClear(driver, Devices.selectDevicesSearchBox);
				SeleniumActions.seleniumSendKeys(driver, Devices.selectDevicesSearchBox, applianceIP);
				String current_version = Devices.firmwareVersion(driver, appliance).getText();
				System.out.println(current_version);
				driver.navigate().refresh();

				log.info("Capturing the current_version  of the appliance");

				System.out.println(firmware);
				if (current_version.equals(oldVersion)) {
					config.editappliancespropertiesDual(boxillaIp, restUser, restPassword, device_name,
							edid_settings_dvi1, hid_configurations, mouse_keyboard_timeout, video_quality,
							edid_settings_dvi2, audio_source);
					config1.checkDeviceState(device_name, boxillaIp, restUser, restPassword);
				} else {
					config.editappliancespropertiesSingle(boxillaIp, restUser, restPassword, device_name,
							edid_settings_dvi1, hid_configurations, mouse_keyboard_timeout, video_quality,audio_source);
					config1.checkDeviceState(device_name, boxillaIp, restUser, restPassword);
				}

				AppliancePropertiesConfig.GetProperties getProp = config1.new GetProperties();
				getProp.device_names = new String[1];
				getProp.device_names[0] = device_name;
				Integer mouseTimeout = Integer.parseInt(mouse_keyboard_timeout);
				given().auth().preemptive().basic(boxillaRestUser, boxillaRestPassword)
						.headers(BoxillaHeaders.getBoxillaHeaders()).body(getProp).when().contentType(ContentType.JSON)
						.get(config1.getUri(boxillaManager)).then().assertThat().statusCode(200)
						.body(config1.getDeviceName(0), equalTo(device_name))
						.body(config1.getDeviceHidConfigurations(0), equalTo(hid_configurations1))
						.body(config1.getDeviceEdidSettingsDvi1(0), equalTo(edid_settings_dvi1))
						.body(config1.getDeviceMouseKeyTimeout(0), equalTo(mouseTimeout));
				log.info("Sucessfully testdata asserted ");

				// if(deviceUpgradeTable.contains("Mis-match to Active Version"))
				// {
				// log.info("Upgrading device : Device state is Mis-match.. Starting upgrade");
				// timer(driver);
				// completeUpgrade(driver, applianceIP);
				// }
				// else if (deviceUpgradeTable.contains("No Upgrade Required")||
				// deviceUpgradeTable.contains("idle")){
				// Assert.assertTrue(deviceUpgradeTable.contains("No Upgrade Required"),
				// "Device state did not contain: No Upgrade Required, actual text: " +
				// deviceUpgradeTable);
				// log.info("Upgrading Device : Upgrade Asserted");
				// }

				/*
				 * Assert.assertTrue(Devices.state(driver).getText().
				 * contains("No Upgrade Required") ||
				 * Devices.state(driver).getText().contains("Idle"),
				 * "***** State Missmatch *****");
				 */
				driver.navigate().refresh();
				Thread.sleep(10000);
				log.info("Upgraing device : current version is " + version[i] + " Trying to activate " + newVersion);

				SeleniumActions.seleniumClick(driver, Devices.releasesTab);
				// Devices.releasesTab(driver).click();
				log.info("Upgrading device : Releases tab clicked");
				timer(driver);

				Devices.deviceReleaseTab(driver, appliance).click();
				log.info("Upgrading device : Device releases tab clciked ");

				if (StartupTestCase.isEmerald) {
					System.out.println("****** USING EMERALD ************");
					activateVersionEmerald(driver, appliance, newVersion);
				} else if (StartupTestCase.isEmeraldSe) {
					System.out.println("****** USING EMERALD SE ************");
					activateVersionEmeraldSe(driver, appliance, newVersion); // Activating v4.1
				} else if (StartupTestCase.isZeroU) {
					log.info("***** USING ZERO U *******************");
					activateVersionZeroU(driver, appliance, newVersion);
				} else {
					System.out.println("****** NOT USING EMERALD ************");
					activateVersion(driver, appliance, newVersion); // Activating v4.1
				}
				System.out
						.println("Upgrading device : Version " + newVersion + " Activated.. Re checking Device State");
				// Re checking appliance version and state
				timer(driver);
				SeleniumActions.seleniumClick(driver, Devices.selectDevices);
				// Devices.selectDevices(driver).click();
				log.info("Upgrading device : Select Devices tab clicked.");
				timer(driver);
				// driver.navigate().refresh(); //refresh the page or else the xpath for the
				// search box will have changed
				driver.get(driver.getCurrentUrl());
				SeleniumActions.seleniumSendKeysClear(driver, Devices.selectDevicesSearchBox);
				// Devices.selectDevicesSearchBox(driver).clear();
				SeleniumActions.seleniumSendKeys(driver, Devices.selectDevicesSearchBox, applianceIP);
				// Devices.selectDevicesSearchBox(driver).sendKeys(applianceIP);
				log.info("Upgrading device : Device searched using IP address");
				timer(driver);
				String deviceUpgradeTable = SeleniumActions.seleniumGetText(driver, Devices.upgradeTable);
				log.info("Text from upgrade table:" + deviceUpgradeTable);
				// String deviceUpgradeTable = Devices.upgradeTable(driver).getText();
				Assert.assertTrue(deviceUpgradeTable.contains("Mis-match to Active Version"),
						"Device upgrade table did not contain: Mis-match to Active Version, actual text: "
								+ deviceUpgradeTable);
				log.info("Upgrading device : Device state is Mis-match.. Starting upgrade");
				timer(driver);
				completeUpgrade(driver, applianceIP);

				given().auth().preemptive().basic(boxillaRestUser, boxillaRestPassword)
						.headers(BoxillaHeaders.getBoxillaHeaders()).body(getProp).when().contentType(ContentType.JSON)
						.get(config1.getUri(boxillaManager)).then().assertThat().statusCode(200)
						.body(config1.getDeviceName(0), equalTo(device_name))
						.body(config1.getDeviceHidConfigurations(0), equalTo(hid_configurations1))
						.body(config1.getDeviceEdidSettingsDvi1(0), equalTo(edid_settings_dvi1))
						.body(config1.getDeviceMouseKeyTimeout(0), equalTo(mouseTimeout));

				log.info("Sucessfully testdata asserted after upgrading to newVersion" + newVersion);

				String firmware1 = Devices.firmwareVersion(driver, appliance).getText();
				if (i < version.length - 2) {

					System.out.println("Whichever version is, at the end the version will be the old version");

					/*
					 * Assert.assertTrue(Devices.state(driver).getText().
					 * contains("No Upgrade Required") ||
					 * Devices.state(driver).getText().contains("Idle"),
					 * "***** State Missmatch *****");
					 */
					log.info("Upgraing device : current version is " + firmware1 + " Trying to activate "
							+ version[i + 1]);
					timer(driver);
					SeleniumActions.seleniumClick(driver, Devices.releasesTab);
					// Devices.releasesTab(driver).click();
					log.info("Upgrading device : Releases tab clicked");
					timer(driver);

					Devices.deviceReleaseTab(driver, appliance).click();
					log.info("Upgrading device : Device releases tab clciked ");

					if (StartupTestCase.isEmerald) {
						System.out.println("****** USING EMERALD ************");
						activateVersionEmerald(driver, appliance, version[i + 1]);
					} else if (StartupTestCase.isEmeraldSe) {
						System.out.println("****** USING EMERALD SE ************");
						activateVersionEmeraldSe(driver, appliance, version[i + 1]); // Activating v4.1
					} else if (StartupTestCase.isZeroU) {
						log.info("***** USING ZERO U *******************");
						activateVersionZeroU(driver, appliance, version[i + 1]);
					} else {
						System.out.println("****** NOT USING EMERALD ************");
						activateVersion(driver, appliance, version[i + 1]); // Activating v4.1
					}
					System.out.println(
							"Upgrading device : Version " + version[i + 1] + " Activated.. Re checking Device State");
					// Re checking appliance version and state
					timer(driver);
					SeleniumActions.seleniumClick(driver, Devices.selectDevices);
					// Devices.selectDevices(driver).click();
					log.info("Upgrading device : Select Devices tab clicked.");
					timer(driver);
					// driver.navigate().refresh(); //refresh the page or else the xpath for the
					// search box will have changed
					driver.get(driver.getCurrentUrl());
					SeleniumActions.seleniumSendKeysClear(driver, Devices.selectDevicesSearchBox);
					// Devices.selectDevicesSearchBox(driver).clear();
					SeleniumActions.seleniumSendKeys(driver, Devices.selectDevicesSearchBox, applianceIP);
					// Devices.selectDevicesSearchBox(driver).sendKeys(applianceIP);
					log.info("Upgrading device : Device searched using IP address");
					timer(driver);
					String deviceUpgradeTable1 = SeleniumActions.seleniumGetText(driver, Devices.upgradeTable);
					log.info("Text from upgrade table:" + deviceUpgradeTable1);
					// String deviceUpgradeTable = Devices.upgradeTable(driver).getText();
					Assert.assertTrue(deviceUpgradeTable1.contains("Mis-match to Active Version"),
							"Device upgrade table did not contain: Mis-match to Active Version, actual text: "
									+ deviceUpgradeTable1);
					log.info("Upgrading device : Device state is Mis-match.. Starting upgrade");
					timer(driver);
					completeUpgrade(driver, applianceIP);
				}
			}
		}
	

	public void upgradereciver(WebDriver driver, String appliance, String applianceIP, String newVersion,
			String oldVersion, String middleVersion, String version4, String version5, String version6,
			String boxillaIp, String restUser, String restPassword, String device_name, String http_enabled)
			throws InterruptedException {
		/*
		 * Search Device using IP - Check current state - if ----> State is "Ideal" or
		 * "No upgrade required" Find out current firmware version - if version is 4.1
		 * activate version 4.2 and vice versa - assert state is equal to Mis-match to
		 * active version - upgrade if version is neither 4.1 or 4.2 activate 4.2 and
		 * upgrade else if ---> State is "Mis-Match to Active Version" - Execute Upgrade
		 */

		timer(driver);
		driver.navigate().refresh();

		SeleniumActions.seleniumClick(driver, Devices.selectDevices);
		// Devices.selectDevices(driver).click();
		log.info("Upgrading device : Select Devices tab clicked.");
		timer(driver);
		log.info("Upgrading device : Checking Firmware version of " + appliance + " device");
		SeleniumActions.seleniumSendKeysClear(driver, Devices.selectDevicesSearchBox);
		SeleniumActions.seleniumSendKeys(driver, Devices.selectDevicesSearchBox, applianceIP);
		Thread.sleep(3000);
		// it will check the devices state before upgrading and downgrading
		if (SeleniumActions.seleniumGetText(driver, Devices.state).contains("Mis-match to Active Version")) 
		{
			log.info("Device Updrading");
			completeUpgrade(driver, applianceIP);

		}

		else if (SeleniumActions.seleniumGetText(driver, Devices.state).contains("No Upgrade Required")|| SeleniumActions.seleniumGetText(driver, Devices.state).contains("Idle")) 
		{

			log.info("Upgrading Device : Upgrade Asserted");
		}
			timer(driver);
			String firmware = Devices.firmwareVersion(driver, appliance).getText();
			timer(driver);
			log.info("Firmware from boxilla:" + firmware);
			 log.info("Firmware new:" + newVersion);
			 log.info("firmware middleversion:" + middleVersion);
			 log.info("firmware oldVersion:" + oldVersion);
			 log.info("firmware version4:" + version4);
			 log.info("firmware version5:" + version5);
			 log.info("firmware version6:" + version6);

			String[] version = { oldVersion, middleVersion, version4, version5, version6, newVersion };
			if (!firmware.equalsIgnoreCase(oldVersion)) {
				
				/*
				 * Assert.assertTrue(Devices.state(driver).getText().
				 * contains("No Upgrade Required") ||
				 * Devices.state(driver).getText().contains("Idle"),
				 * "***** State Missmatch *****");
				 */
				log.info("Upgraing device : current version is " + firmware + " Trying to activate " + oldVersion);
				timer(driver);
				SeleniumActions.seleniumClick(driver, Devices.releasesTab);
				// Devices.releasesTab(driver).click();
				log.info("Upgrading device : Releases tab clicked");
				timer(driver);

				Devices.deviceReleaseTab(driver, appliance).click();
				log.info("Upgrading device : Device releases tab clciked ");

				if (StartupTestCase.isEmerald) {
					System.out.println("****** USING EMERALD ************");
					activateVersionEmerald(driver, appliance, oldVersion);
				} else if (StartupTestCase.isEmeraldSe) {
					System.out.println("****** USING EMERALD SE ************");
					activateVersionEmeraldSe(driver, appliance, oldVersion); // Activating v4.1
				} else if (StartupTestCase.isZeroU) {
					log.info("***** USING ZERO U *******************");
					activateVersionZeroU(driver, appliance, oldVersion);
				} else {
					System.out.println("****** NOT USING EMERALD ************");
					activateVersion(driver, appliance, oldVersion); // Activating v4.1
				}
				System.out
						.println("Upgrading device : Version " + oldVersion + " Activated.. Re checking Device State");
				// Re checking appliance version and state
				timer(driver);
				SeleniumActions.seleniumClick(driver, Devices.selectDevices);
				// Devices.selectDevices(driver).click();
				log.info("Upgrading device : Select Devices tab clicked.");
				timer(driver);
				// driver.navigate().refresh(); //refresh the page or else the xpath for the
				// search box will have changed
				driver.get(driver.getCurrentUrl());
				SeleniumActions.seleniumSendKeysClear(driver, Devices.selectDevicesSearchBox);
				// Devices.selectDevicesSearchBox(driver).clear();
				SeleniumActions.seleniumSendKeys(driver, Devices.selectDevicesSearchBox, applianceIP);
				// Devices.selectDevicesSearchBox(driver).sendKeys(applianceIP);
				log.info("Upgrading device : Device searched using IP address");
				timer(driver);
				String deviceUpgradeTable = SeleniumActions.seleniumGetText(driver, Devices.upgradeTable);
				log.info("Text from upgrade table:" + deviceUpgradeTable);
				// String deviceUpgradeTable = Devices.upgradeTable(driver).getText();
				Assert.assertTrue(deviceUpgradeTable.contains("Mis-match to Active Version"),
						"Device upgrade table did not contain: Mis-match to Active Version, actual text: "
								+ deviceUpgradeTable);
				log.info("Upgrading device : Device state is Mis-match.. Starting upgrade");
				timer(driver);
				completeUpgrade(driver, applianceIP);
			}

			for (int i = 0; i < version.length - 1; i++) {

				config.editreciverapplianceproperties(boxillaIp, restUser, restPassword, device_name, http_enabled);
				config1.checkDeviceState(device_name, boxillaIp, restUser, restPassword);
				AppliancePropertiesConfig.GetProperties getProp = config1.new GetProperties();
				getProp.device_names = new String[1];
				getProp.device_names[0] = device_name;

				log.info("Getting properties through REST");
				given().auth().preemptive().basic(boxillaRestUser, boxillaRestPassword)
						.headers(BoxillaHeaders.getBoxillaHeaders()).body(getProp).when().contentType(ContentType.JSON)
						.get(config1.getUri(boxillaManager)).then().assertThat().statusCode(200)
						.body(config1.getDeviceName(0), equalTo(device_name))
						.body(config1.getDeviceHttpEnabled(0), equalTo(http_enabled));

				driver.navigate().refresh();
				Thread.sleep(10000);
				log.info("Upgraing device : current version is " + version[i] + " Trying to activate " + newVersion);

				SeleniumActions.seleniumClick(driver, Devices.releasesTab);
				// Devices.releasesTab(driver).click();
				log.info("Upgrading device : Releases tab clicked");
				timer(driver);

				Devices.deviceReleaseTab(driver, appliance).click();
				log.info("Upgrading device : Device releases tab clciked ");

				if (StartupTestCase.isEmerald) {
					System.out.println("****** USING EMERALD ************");
					activateVersionEmerald(driver, appliance, newVersion);
				} else if (StartupTestCase.isEmeraldSe) {
					System.out.println("****** USING EMERALD SE ************");
					activateVersionEmeraldSe(driver, appliance, newVersion); // Activating v4.1
				} else if (StartupTestCase.isZeroU) {
					log.info("***** USING ZERO U *******************");
					activateVersionZeroU(driver, appliance, newVersion);
				} else {
					System.out.println("****** NOT USING EMERALD ************");
					activateVersion(driver, appliance, newVersion); // Activating v4.1
				}
				System.out
						.println("Upgrading device : Version " + newVersion + " Activated.. Re checking Device State");
				// Re checking appliance version and state
				timer(driver);
				SeleniumActions.seleniumClick(driver, Devices.selectDevices);
				// Devices.selectDevices(driver).click();
				log.info("Upgrading device : Select Devices tab clicked.");
				timer(driver);
				// driver.navigate().refresh(); //refresh the page or else the xpath for the
				// search box will have changed
				driver.get(driver.getCurrentUrl());
				SeleniumActions.seleniumSendKeysClear(driver, Devices.selectDevicesSearchBox);
				// Devices.selectDevicesSearchBox(driver).clear();
				SeleniumActions.seleniumSendKeys(driver, Devices.selectDevicesSearchBox, applianceIP);
				// Devices.selectDevicesSearchBox(driver).sendKeys(applianceIP);
				log.info("Upgrading device : Device searched using IP address");
				timer(driver);
				String deviceUpgradeTable = SeleniumActions.seleniumGetText(driver, Devices.upgradeTable);
				log.info("Text from upgrade table:" + deviceUpgradeTable);
				// String deviceUpgradeTable = Devices.upgradeTable(driver).getText();
				Assert.assertTrue(deviceUpgradeTable.contains("Mis-match to Active Version"),
						"Device upgrade table did not contain: Mis-match to Active Version, actual text: "
								+ deviceUpgradeTable);
				log.info("Upgrading device : Device state is Mis-match.. Starting upgrade");
				timer(driver);
				completeUpgrade(driver, applianceIP);
				given().auth().preemptive().basic(boxillaRestUser, boxillaRestPassword)
						.headers(BoxillaHeaders.getBoxillaHeaders()).body(getProp).when().contentType(ContentType.JSON)
						.get(config1.getUri(boxillaManager)).then().assertThat().statusCode(200)
						.body(config1.getDeviceName(0), equalTo(device_name))
						.body(config1.getDeviceHttpEnabled(0), equalTo("Enabled"));
				log.info("Appliance properties matched sucessfully");

				String firmware1 = Devices.firmwareVersion(driver, appliance).getText();
				if (i < version.length - 2) {

					System.out.println("Whichever version is, at the end the version will be the old version");

					/*
					 * Assert.assertTrue(Devices.state(driver).getText().
					 * contains("No Upgrade Required") ||
					 * Devices.state(driver).getText().contains("Idle"),
					 * "***** State Missmatch *****");
					 */
					log.info("Upgraing device : current version is " + firmware1 + " Trying to activate "
							+ version[i + 1]);
					timer(driver);
					SeleniumActions.seleniumClick(driver, Devices.releasesTab);
					// Devices.releasesTab(driver).click();
					log.info("Upgrading device : Releases tab clicked");
					timer(driver);

					Devices.deviceReleaseTab(driver, appliance).click();
					log.info("Upgrading device : Device releases tab clciked ");

					if (StartupTestCase.isEmerald) {
						System.out.println("****** USING EMERALD ************");
						activateVersionEmerald(driver, appliance, version[i + 1]);
					} else if (StartupTestCase.isEmeraldSe) {
						System.out.println("****** USING EMERALD SE ************");
						activateVersionEmeraldSe(driver, appliance, version[i + 1]); // Activating v4.1
					} else if (StartupTestCase.isZeroU) {
						log.info("***** USING ZERO U *******************");
						activateVersionZeroU(driver, appliance, version[i + 1]);
					} else {
						System.out.println("****** NOT USING EMERALD ************");
						activateVersion(driver, appliance, version[i + 1]); // Activating v4.1
					}
					System.out.println(
							"Upgrading device : Version " + version[i + 1] + " Activated.. Re checking Device State");
					// Re checking appliance version and state
					timer(driver);
					SeleniumActions.seleniumClick(driver, Devices.selectDevices);
					// Devices.selectDevices(driver).click();
					log.info("Upgrading device : Select Devices tab clicked.");
					timer(driver);
					// driver.navigate().refresh(); //refresh the page or else the xpath for the
					// search box will have changed
					driver.get(driver.getCurrentUrl());
					SeleniumActions.seleniumSendKeysClear(driver, Devices.selectDevicesSearchBox);
					// Devices.selectDevicesSearchBox(driver).clear();
					SeleniumActions.seleniumSendKeys(driver, Devices.selectDevicesSearchBox, applianceIP);
					// Devices.selectDevicesSearchBox(driver).sendKeys(applianceIP);
					log.info("Upgrading device : Device searched using IP address");
					timer(driver);
					String deviceUpgradeTable1 = SeleniumActions.seleniumGetText(driver, Devices.upgradeTable);
					log.info("Text from upgrade table:" + deviceUpgradeTable1);
					// String deviceUpgradeTable = Devices.upgradeTable(driver).getText();
					Assert.assertTrue(deviceUpgradeTable1.contains("Mis-match to Active Version"),
							"Device upgrade table did not contain: Mis-match to Active Version, actual text: "
									+ deviceUpgradeTable1);
					log.info("Upgrading device : Device state is Mis-match.. Starting upgrade");
					timer(driver);
					completeUpgrade(driver, applianceIP);

				}
			}

		}

	

	public String getCloudGuiXml(String deviceIp) throws InterruptedException {
		Ssh shell = new Ssh(StartupTestCase.deviceUserName, StartupTestCase.devicePassword, deviceIp);
		int retry = 0;
		while (retry < 5) {
			try {
				shell.loginToServer();
				retry = 6;
			} catch (Exception e) {
				e.printStackTrace();
				log.info("SSH login failed. Retrying");
				Thread.sleep(5000);
				retry++;
			}
		}
		String xml = shell.sendCommand("cat /usr/local/gui_files/CloudDataA.xml");
		log.info("XML:" + xml);
		return xml;
	}

	public boolean checkConnectionFavouritesXml(String deviceIp, String username, String connectionName,
			String favNumber) throws InterruptedException {
		recreateCloudData(deviceIp);
		rebootDeviceSSH(deviceIp, StartupTestCase.deviceUserName, StartupTestCase.devicePassword, 0);

		// get xml
		Ssh shell = new Ssh(StartupTestCase.deviceUserName, StartupTestCase.devicePassword, deviceIp);
		int retry = 0;
		while (retry < 5) {
			try {
				shell.loginToServer();
				retry = 6;
			} catch (Exception e) {
				e.printStackTrace();
				log.info("SSH login failed. Retrying");
				Thread.sleep(5000);
				retry++;
			}
		}
		String xml = shell.sendCommand("cat /usr/local/gui_files/CloudDataA.xml");

		Scanner scan = new Scanner(xml);
		while (scan.hasNextLine()) {
			String line = scan.nextLine();
			if (line.contains(username)) {
				while (scan.hasNextLine()) {
					line = scan.nextLine();
					while (line.contains("Associated_connections")) {
						if (line.contains(connectionName) && line.contains("Favorite='" + favNumber + "'")) {
							System.out.println("MATCH!");
							return true;
						} else {
							line = scan.nextLine();
						}
					}
				}
			}
		}
		return false;
	}

}
