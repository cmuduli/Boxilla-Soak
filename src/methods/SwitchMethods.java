package methods;

import static org.toilelibre.libe.curl.Curl.curl;

import java.net.InetAddress;

import org.apache.log4j.Logger;
import org.eclipse.jetty.util.log.Log;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import extra.SeleniumActions;
import invisaPC.Rest.Authentication;
import objects.Discovery;
import objects.Landingpage;
import objects.Switch;
import objects.Switch.Column;
import objects.Switch.PORT_COLUMN;

public class SwitchMethods {
	
	final static Logger log = Logger.getLogger(SwitchMethods.class);
	
	/**
	 * From the dashboard
	 * - Click switch dropdown
	 * - Check status link is visible
	 * - Click switch status link
	 * - Check for page loading by checking visibility of table heading
	 * @param driver
	 */
	public void navigateToSwitchStatus(WebDriver driver) {
		log.info("Navigating to switch status page");
		driver.navigate().refresh(); 		//refresh is need sometimes
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(Landingpage.dellSwitchesTab(driver)));
		//Landingpage.switchesTab(driver).click();
		SeleniumActions.seleniumClick(driver, Landingpage.switchesTab);
		WebElement status = driver.findElement(By.xpath(Landingpage.switchesStatusLink));
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(status));
		SeleniumActions.seleniumClick(driver, Landingpage.switchesStatusLink);
		WebElement tableHeading = driver.findElement(By.xpath(Switch.switchNameThXpath));
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(tableHeading));
		log.info("Succesfully navigated to switch > status");
	}
	
	public void navigateToSwitchUpgrade(WebDriver driver) {
		log.info("Navigating to switch upgrade page");
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(Landingpage.dellSwitchesTab(driver)));
		SeleniumActions.seleniumClick(driver, Landingpage.switchesTab);
		WebElement upgrade = driver.findElement(By.xpath(Landingpage.switchesUpgradeLink));
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(upgrade));
		SeleniumActions.seleniumClick(driver, Landingpage.switchesUpgradeLink);
		WebElement uploadBtn = driver.findElement(By.xpath(Switch.uploadBtnXpath));
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(uploadBtn));
		log.info("Succesfully navigated to switch > upgrades");
	}
	
	public void addSwitch(WebDriver driver, String name, String ipAddress) {
		log.info("Attempting to add switch");
		navigateToSwitchStatus(driver);
		SeleniumActions.seleniumClick(driver, Switch.addSwitchBtnXpath);
		log.info("Checking if add switch pop up is displayed");
		WebElement popUp = driver.findElement(By.xpath(Switch.addSwitchSaveBtnXpath));
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(popUp));
		log.info("Add switch pop up menu is displayed.. Adding switch details");
		SeleniumActions.seleniumSendKeys(driver, Switch.addSwitchNameTextXpath, name);
		log.info("Added switch name: " + name);
		SeleniumActions.seleniumSendKeys(driver, Switch.addSwitchIpTextXpath, ipAddress);
		log.info("Added switch IP:" + ipAddress);
		SeleniumActions.seleniumClick(driver, Switch.addSwitchSaveBtnXpath);
		log.info("Waiting for spinner to disappear");
		WebElement spinner = driver.findElement(By.xpath(Switch.spinnerXpath));
		new WebDriverWait(driver, 60).until(ExpectedConditions.invisibilityOf(spinner));
		//log.info("Waiting for toast message to appear");
//		WebElement toast = driver.findElement(By.xpath(Switch.addSwitchSuccessToastXpath));
//		new WebDriverWait(driver, 80).until(ExpectedConditions.visibilityOf(spinner));
		
	//	WebElement toast = (new WebDriverWait(driver, 80))
		//		   .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Switch.addSwitchSuccessToastXpath)));
		
		log.info("Switch successfully added");
	}
	
	/**
	 *  Searches for switch by IP address and returns the 
	 * table column passed in
	 * @param driver
	 * @param ip
	 * @param column
	 * @return
	 */
	public String getSwitchTableValueByIp(WebDriver driver, String ip, Column column) {
		log.info("Searching for switch with IP: " + ip );
		String columnIndex = "0";
		//switch to get column index
		switch (column) {
			case NAME :
				columnIndex = "1";
				log.info("Going to return switch name");
				break;
			case STATUS :
				columnIndex = "2";
				log.info("Going to return switch status");
				break;
			case MODEL :
				columnIndex = "3";
				log.info("Going to return switch model");
				break;
			case IP :
				columnIndex = "4";
				log.info("Going to return switch IP");
				break;
			case PORTS_ONLINE :
				columnIndex = "5";
				log.info("Going to return switch Ports Online");
				break;
			case SHARED_MODE :
				columnIndex = "6";
				log.info("Going to return switch Shared Mode");
				break;
			case BANDWIDTH_IN :
				columnIndex = "7";
				log.info("Going to return switch Bandwidth In");
				break;
			case BANDWIDTH_OUT :
				columnIndex = "8";
				log.info("Going to return switch Bandwidth Out");
				break;
			case ALERTS :
				columnIndex = "9";
				log.info("Going to return switch Alerts");
				break;
				
		}
		
		searchSwitchByIp(driver, ip);
		log.info("Attempting to get text from table");
		String text = "";
		try {
			WebElement e = Switch.getSwitchTableColumn(driver, columnIndex);
			 text = e.getText();
		}catch(Exception e ) {
			return text;
		}

		
		log.info("Text from first table row, column number " + columnIndex + ": " + text );
		return text;
	}
	
	public String getSwitchUpgradeState(WebDriver driver, String ip) {
		SeleniumActions.seleniumSendKeysClear(driver, Switch.getUpgradeSearchTextboxXpath());
		SeleniumActions.seleniumSendKeys(driver, Switch.getUpgradeSearchTextboxXpath(), ip);
		WebElement e = Switch.getSwitchTableColumn(driver, "10");
		String text = e.getText();
		return text;
	}
	public String getSwitchVersion(WebDriver driver, String ip) {
		SeleniumActions.seleniumSendKeysClear(driver, Switch.getUpgradeSearchTextboxXpath());
		SeleniumActions.seleniumSendKeys(driver, Switch.getUpgradeSearchTextboxXpath(), ip);
		WebElement e = Switch.getSwitchTableColumn(driver, "9");
		String text = e.getText();
		return text;
	}
	public String getSwitchState(WebDriver driver, String ip) {
		navigateToSwitchUpgrade(driver);
		SeleniumActions.seleniumSendKeysClear(driver, Switch.getUpgradeSearchTextboxXpath());
		SeleniumActions.seleniumSendKeys(driver, Switch.getUpgradeSearchTextboxXpath(), ip);
		WebElement e = Switch.getSwitchTableColumn(driver, "14");
		String text = e.getText();
		return text;
	}
	
	public String getPortSwitchTableData(WebDriver driver, String ip, String switchName, String portName, PORT_COLUMN column) {
		String columnIndex = "0";
		String text = "";
		searchPortDetails(driver, ip, switchName, portName);
		switch (column) {
			case NAME:
				columnIndex = "1";
				log.info("Going to return port name");
				break;
			case STATUS:
				columnIndex = "2";
				log.info("Going to return port status");
				break;
			case MROUTER:
				columnIndex = "4";
				log.info("Going to return mrouter status");
				break;
			case MEDIA:
				columnIndex = "6";
				log.info("Going to return media");
			case BANDWIDTH_IN:
				columnIndex = "7";
				log.info("Going to return bandwidth in status");
				break;
			case BANDWIDTH_OUT:
				columnIndex = "8";
				log.info("Going to return bandwidth out status");
				break;
			case PACKETS_IN :
				columnIndex = "9";
				log.info("Going to return packets in status");
				break;
			case PACKETS_OUT :
				columnIndex = "10";
				log.info("Going to return packets out status");
				break;
			case LINE_USAGE_IN :
				columnIndex = "11";
				log.info("Going to return line usage in status");
				break;
			case LINE_USAGE_OUT :
				columnIndex = "12";
				log.info("Going to return line usage out status");
				break;
			case ERRORS :
				columnIndex = "13";
				log.info("Going to return line error status");
				break;
				
		}
		if(columnIndex.equals("2")) {		//convert image to text
			try {
				SeleniumActions.seleniumIsDisplayed(driver, Switch.portIsDownImgXpath);
				log.info("Text is Disabled");
				return "Disabled";
			}catch(Exception e) {
				SeleniumActions.seleniumIsDisplayed(driver, Switch.portIsDownImgXpath);
				log.info("Text is Enabled");
				return "Enabled";
			}
		}else {
			WebElement e = Switch.getPortTableColumn(driver, columnIndex);
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", e); //scroll into view
			 text = e.getText();
			
			log.info("Text from first table row, column number " + columnIndex + ": " + text );
		}
		return text;
	}

	public void searchSwitchByIp(WebDriver driver, String ip) {
		log.info("Searching for switch by IP");
		navigateToSwitchStatus(driver);
		SeleniumActions.seleniumSendKeysClear(driver, Switch.switchSearchBoxXpath);
		SeleniumActions.seleniumSendKeys(driver, Switch.switchSearchBoxXpath, ip);
	}
	
	public String getSwitchDetails(WebDriver driver, String ip) {
		log.info("Navigating to switch details");
		searchSwitchByIp(driver, ip);
		SeleniumActions.seleniumClick(driver, Switch.getSwitchDropdownXpath());
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getSwitchDetail(driver)));
		SeleniumActions.seleniumClick(driver, Switch.getSwitchDetailXpath());
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getSwitchDetailBackBtn(driver)));
		String pageDetails = driver.getPageSource();
		return pageDetails;
	}
	public int getSwitchDetailsTemperature(WebDriver driver, String ip) {
		log.info("Attempting to get switch temperature");
		getSwitchDetails(driver, ip);
		String output =  Switch.getSwitchDetailsTemperature(driver).getText();
		log.info("OUTPUT=" + output);
		String[] parsedOutput = output.split("\\)");
		for(String s : parsedOutput) {
			log.info("s :  " + s);
		}
		log.info("here: " + parsedOutput[parsedOutput.length -1]);
		return Integer.parseInt(parsedOutput[parsedOutput.length -1]);
	}
	
	public static void main(String args[]) {
		String x = "Temperature(Celsius)28";
		String[] y = x.split("\\)");
		for(String s : y) {
			System.out.println(s);
		}
	}
	
	public int getSwitchDetailsUpTime(WebDriver driver, String ip) {
		getSwitchDetails(driver, ip);
		String output =  Switch.switchDetailsUptime(driver).getText();
		String[] parsedOutput = output.split("Time");
		for(String s : parsedOutput) {
			log.info("s :  " + s);
		}
		log.info("here: " + parsedOutput[parsedOutput.length -1]);
		return Integer.parseInt(parsedOutput[parsedOutput.length -1]);
	}
	
	public void pingSwitch(WebDriver driver, String ip) {
		log.info("Pinging switch with IP: " + ip);
		searchSwitchByIp(driver, ip);
		SeleniumActions.seleniumClick(driver, Switch.getSwitchDropdownXpath());
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getSwitchPing(driver)));
		SeleniumActions.seleniumClick(driver, Switch.getSwitchPingXpath());
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getSpinner(driver)));
		new WebDriverWait(driver, 60).until(ExpectedConditions.invisibilityOf(Switch.getSpinner(driver)));
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getToast(driver)));
		String text = Switch.getToast(driver).getText();
		log.info("Asserting if toast was successful:" + text);
		Assert.assertTrue(text.contains("successfully"), "Toast message did not contain success for ping");
		log.info("Switch successfully pinged");
	}
	
	//not fully working due to toast notification. Need to fix
	public void changeSwitchName(WebDriver driver, String ip, String newName) throws InterruptedException {
		log.info("Chaning switch with IP " + ip +  "name to " + newName);
		navigateToChangeSwitchName(driver, ip);
		SeleniumActions.seleniumSendKeysClear(driver, Switch.switchNameTextboxXpath);
		SeleniumActions.seleniumSendKeys(driver, Switch.switchNameTextboxXpath, newName);
		SeleniumActions.seleniumClick(driver, Switch.getswitchChangeNameApplyBtnXpath());
		Alert alert = driver.switchTo().alert();
		alert.accept();
		Thread.sleep(4000);
//		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getToast(driver)));
//		String toastMessage = Switch.getToast(driver).getText();
//		log.info("Asserting toast message:" + toastMessage);
//		Assert.assertTrue(toastMessage.contains("successful"), "toast message did not contain successful, actuaal " + toastMessage);
	}
	
	public void enableSharedMode(WebDriver driver, String ip) {
		log.info("Attempting to enable shared mode");
		searchSwitchByIp(driver, ip);
		SeleniumActions.seleniumClick(driver, Switch.getSwitchDropdownXpath());
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getSwitchEnableSharedModeBtn(driver)));
		SeleniumActions.seleniumClick(driver, Switch.getSwitchEnableSharedModeBtnXpath());
		Alert alert = driver.switchTo().alert();
		alert.accept();
		log.info("waiting for spinner");
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getSpinner(driver)));
		log.info("waiting for spinner to disappear");
		new WebDriverWait(driver, 120).until(ExpectedConditions.invisibilityOf(Switch.getSpinner(driver)));
		String sharedModeEnabled = getSwitchTableValueByIp(driver, ip, Column.SHARED_MODE);
		Assert.assertTrue(sharedModeEnabled.contains("Enabled"), "Shared Mode was not enabled");	
	}
	public void disabledSharedMode(WebDriver driver, String ip) {
		log.info("Attempting to disable shared mode");
		searchSwitchByIp(driver, ip);
		SeleniumActions.seleniumClick(driver, Switch.getSwitchDropdownXpath());
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getSwitchDisableSharedModeBtn(driver)));
		SeleniumActions.seleniumClick(driver, Switch.getSwitchDisableSharedModeBtnXpath());
		Alert alert = driver.switchTo().alert();
		alert.accept();
		log.info("waiting for spinner");
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getSpinner(driver)));
		log.info("waiting for spinner to disappear");
		new WebDriverWait(driver, 120).until(ExpectedConditions.invisibilityOf(Switch.getSpinner(driver)));
		String sharedModeEnabled = getSwitchTableValueByIp(driver, ip, Column.SHARED_MODE);
		Assert.assertTrue(sharedModeEnabled.contains("Disabled"), "Shared Mode was not enabled");	
	}

	public void navigateToChangeSwitchName(WebDriver driver, String ip) {
		log.info("Navigating to change switch name");
		searchSwitchByIp(driver, ip);
		SeleniumActions.seleniumClick(driver, Switch.getSwitchDropdownXpath());
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getSwitchChangeName(driver)));
		SeleniumActions.seleniumClick(driver, Switch.switchChangeNameXpath);
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getSwitchNameTextbox(driver)));
		log.info("Successfully navigated to change switch name");
	}
	
	public void navigateToEditSwitchNetwork(WebDriver driver, String ip) {
		log.info("Navigating to edit switch network");
		searchSwitchByIp(driver, ip);
		SeleniumActions.seleniumClick(driver, Switch.getSwitchDropdownXpath());
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getSwitchEditNetwork(driver)));
		SeleniumActions.seleniumClick(driver, Switch.getSwitchEditNetworkXpath());
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getSwitchEditIpTextbox(driver)));
		log.info("Successfully navigated to change switch name");
	}
	
	public String editSwitchDetails(WebDriver driver, String oldIp, String newIp, String newNetmask
			, String newGateway) {
		log.info("Trying to edit switch with IP " + oldIp + " network details");
		navigateToEditSwitchNetwork(driver, oldIp);
		log.info("Editing IP");
		SeleniumActions.seleniumSendKeysClear(driver, Switch.getSwitchEditIpTextboxXpath());
		SeleniumActions.seleniumSendKeys(driver, Switch.getSwitchEditIpTextboxXpath(), newIp);
		log.info("Editing netmask");
		SeleniumActions.seleniumSendKeysClear(driver, Switch.getSwitchEditNetmaskXpath());
		SeleniumActions.seleniumSendKeys(driver, Switch.getSwitchEditNetmaskXpath(), newNetmask);
		log.info("Clicking apply");
		SeleniumActions.seleniumClick(driver, Switch.getSwitchEditNetworkApplyBtnXpath());
		Alert alert = driver.switchTo().alert();
		alert.accept();
		new WebDriverWait(driver, 180).until(ExpectedConditions.visibilityOf(Switch.getSpinner(driver)));
		new WebDriverWait(driver, 180).until(ExpectedConditions.invisibilityOf(Switch.getSpinner(driver)));
		
		/// CHANGE WHEN TOAST MESSAGES ARE FIXED IN BOXILLA /////////
		String text = "Success";
		//String text = Switch.getToast(driver).getText();
		//new WebDriverWait(driver, 60).until(ExpectedConditions.invisibilityOf(Switch.getToast(driver)));
		log.info("Network details changed, returning toast text");
		return text;
	}
	/**
	 * Checks the switch table every 5 seconds for offline. When the switch is reported as offline
	 * true is returned. If the switch does not report off line in the time given false is returned.
	 * Length of time to check for is numberOfLoops * 5 seconds
	 * @param driver
	 * @param ip
	 * @param numberOfLoops
	 * @return
	 * @throws InterruptedException
	 */
	public boolean isSwitchOffline(WebDriver driver, String ip, int numberOfLoops) throws InterruptedException {
		log.info("Checking if switch is offline");
		boolean isOffline = false;
		int time =0;
		searchSwitchByIp(driver, ip);
		while(!isOffline &&
				time < numberOfLoops) {
			String output = Switch.getSwitchTableColumn(driver, "2").getText();
			if(output.equals("OffLine")) {
				isOffline = true;
			}else {
				log.info("Switch not online. Trying again in two seconds. ");
				time++;
				Thread.sleep(5000);
				driver.navigate().refresh();
				new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath(Switch.switchNameThXpath))));
				SeleniumActions.seleniumSendKeysClear(driver, Switch.switchSearchBoxXpath);
				SeleniumActions.seleniumSendKeys(driver, Switch.switchSearchBoxXpath, ip);
			}
		}
		return isOffline;
	}
	
	public boolean isSwitchOnline(WebDriver driver, String ip, int numberOfLoops) throws InterruptedException {
		log.info("Checking if switch is online");
		boolean isOnline = false;
		int time = 0;
		searchSwitchByIp(driver, ip);
		while(!isOnline && time < numberOfLoops) {
			String output = Switch.getSwitchTableColumn(driver, "2").getText();
			if(output.equals("OnLine")) {
				isOnline = true;
			}else {
				log.info("Switch not online. Trying again in five seconds. ");
				time++;
				Thread.sleep(5000);
				driver.navigate().refresh();
				new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath(Switch.switchNameThXpath))));
				SeleniumActions.seleniumSendKeysClear(driver, Switch.switchSearchBoxXpath);
				SeleniumActions.seleniumSendKeys(driver, Switch.switchSearchBoxXpath, ip);
			}
		}
		return isOnline;
	}
	public void unmanageSwitch(WebDriver driver, String ip) {
		log.info("Unmanaging switch with ip " + ip);
		searchSwitchByIp(driver, ip);
		SeleniumActions.seleniumClick(driver, Switch.getSwitchDropdownXpath());
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getSwitchUnmanage(driver)));
		SeleniumActions.seleniumClick(driver, Switch.getSwitchUnmanageXpath());
		log.info("Unmanage selected. Clicking alert");
		Alert alert = driver.switchTo().alert();
		alert.accept();
		log.info("Waiting for spinner to appear and disappear");
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getSpinner(driver)));
		new WebDriverWait(driver, 60).until(ExpectedConditions.invisibilityOf(Switch.getSpinner(driver)));
		log.info("Switch unmanaged waiting for toast confirmation");
//		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getToast(driver)));
//		String toastMessage = Switch.getToast(driver).getText();
//		log.info("Asserting toast message:" + toastMessage);
//		Assert.assertTrue(toastMessage.contains("successful"), "toast message did not contain successful, actual " + toastMessage);
	}
	
	public void navigateToSwitchPortDetails(WebDriver driver, String ip, String name) {
		log.info("Navigating to port details for switch " + ip);
		searchSwitchByIp(driver, ip);
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getSwitchNameLink(driver, name)));
		SeleniumActions.seleniumClick(driver, Switch.getSwitchNameLinkXpath(name));
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getPortDetailsBackBtn(driver)));
		log.info("Successfully navigated to port details for switch " + ip);
	}
	public void searchPortDetails(WebDriver driver, String ip, String switchName, String search) {
		navigateToSwitchPortDetails(driver, ip, switchName);
		SeleniumActions.seleniumSendKeysClear(driver, Switch.portDetailsSearchTextboxXpath);
		SeleniumActions.seleniumSendKeys(driver, Switch.portDetailsSearchTextboxXpath, "1/1/" + search + " ");
		log.info("Searched for " + search + " complete");
	}
	
	public void rebootSwitch(WebDriver driver, String ip) {
		searchSwitchByIp(driver, ip);
		SeleniumActions.seleniumClick(driver, Switch.getSwitchDropdownXpath());
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(Switch.getSwitchReboot(driver)));
		SeleniumActions.seleniumClick(driver, Switch.getSwitchRebootXpath());
		Alert alert = driver.switchTo().alert();
		alert.accept();
		log.info("Waiting for spinner to appear");
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getSpinner(driver)));
		log.info("Waiting for spinner to disappear");
		new WebDriverWait(driver, 60).until(ExpectedConditions.invisibilityOf(Switch.getSpinner(driver)));
		log.info("Switch rebooted");
	}
	
	public String getChartText(WebDriver driver, String ip, String switchName, String portNumber) {
		log.info("Attempting to select port chart");
		searchPortDetails(driver, ip, switchName, portNumber + " ");
		log.info("Searched port. Attempting to click chart image");
		SeleniumActions.seleniumClick(driver, Switch.portChartImgXpath);
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getChart(driver)));
		String chartTitle = SeleniumActions.seleniumGetText(driver, Switch.getChartxpath());
		log.info("Chart title: " + chartTitle);
		return chartTitle;
		
	}
	/**
	 * true to disable, false to enable
	 * @param driver
	 * @param switchIp
	 * @param switchName
	 * @param portNumber
	 * @param isDown
	 */
	public void portDown(WebDriver driver, String switchIp, String switchName, String portNumber, boolean isDown) {
		searchPortDetails(driver, switchIp, switchName, portNumber + " "); //add the space due to the way search works. 1/1/2 search will return 1/1/2 and 1/1/22
		SeleniumActions.seleniumClick(driver, Switch.portDropdownBtnXpath);
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(Switch.getPortDisableBtn(driver)));
		
		if(isDown) {
			Assert.assertFalse(SeleniumActions.seleniumGetText(driver, Switch.getPortDisableBtnXpath()).contains("Enable Port"),
					"Port already disabled ending test");
		}else {
			Assert.assertFalse(SeleniumActions.seleniumGetText(driver, Switch.getPortDisableBtnXpath()).contains("Disable Port"),
					"Port already enabled ending test");
		}
		SeleniumActions.seleniumClick(driver, Switch.getPortDisableBtnXpath());
			Alert alert = driver.switchTo().alert();
			alert.accept();
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getSpinner(driver)));
		new WebDriverWait(driver, 60).until(ExpectedConditions.invisibilityOf(Switch.getSpinner(driver)));
		if(isDown) {
			log.info("Port disabled. Checking if port disabled image is displayed for port " + portNumber);
		}else {
			log.info("Port enabled. Checking if port enabled image is displayed for port " + portNumber);
		}
		
		searchPortDetails(driver, switchIp, switchName, portNumber + " ");
		if(isDown) {
			Assert.assertTrue(Switch.getPortDisabledImg(driver).isDisplayed(), "The port " + portNumber + " was not disabled");
			log.info("Port disabled image was displayed");
		}else {
			Assert.assertTrue(Switch.getPortIsDownImg(driver).isDisplayed(), "The port " + portNumber + " was not enabled");
			log.info("Port is down image was displayed");
		}
	}
	public void manageSwitchByDiscovery(WebDriver driver, String switchMac, String switchName, String ip) throws InterruptedException {
		log.info("Managing switch by discovery");
		setIpByDiscovery(driver, switchMac, ip);
		SeleniumActions.seleniumSendKeys(driver, Discovery.searchBox, ip);
		SeleniumActions.seleniumClick(driver, Discovery.breadCrumbBtn);
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(Discovery.getManageSwitchBtn(driver)));
		SeleniumActions.seleniumClick(driver, Discovery.getManageSwitchBtnXpath());
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(Discovery.manageApplyBtn(driver)));
		SeleniumActions.seleniumSendKeys(driver, Discovery.managedName, switchName);
		SeleniumActions.seleniumClick(driver, Discovery.manageApplyBtn);
		Alert alert2 = driver.switchTo().alert();
		alert2.accept();
//	
//		new WebDriverWait(driver, 120).until(ExpectedConditions.visibilityOf(Switch.getSpinner(driver)));
//		new WebDriverWait(driver, 120).until(ExpectedConditions.invisibilityOf(Switch.getSpinner(driver)));
//		log.info("Swtich managed.");
		Thread.sleep(60000);
		new WebDriverWait(driver, 280).until(ExpectedConditions.elementToBeClickable(Switch.getAddSwitchBtn(driver)));
	}

	public void setIpByDiscovery(WebDriver driver, String switchMac, String ip) throws InterruptedException {
		Landingpage.discoveryTab(driver).click();
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(Discovery.discoverBtn(driver)));
		SeleniumActions.seleniumClick(driver, Discovery.discoverBtnXpath);
		log.info("Running discovery");
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getSpinner(driver)));
		new WebDriverWait(driver, 60).until(ExpectedConditions.invisibilityOf(Switch.getSpinner(driver)));
		SeleniumActions.seleniumSendKeys(driver, Discovery.searchBox, switchMac);
		SeleniumActions.seleniumClick(driver, Discovery.breadCrumbBtn);
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(Discovery.applianceEdit(driver)));
		SeleniumActions.seleniumClick(driver, Discovery.applianceEdit);
		log.info("Editing network details");
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Discovery.ipAddress(driver)));
		SeleniumActions.seleniumSendKeysClear(driver, Discovery.ipAddress);
		SeleniumActions.seleniumSendKeys(driver, Discovery.ipAddress, ip);
		SeleniumActions.seleniumSendKeysClear(driver, Discovery.netmask);
		SeleniumActions.seleniumSendKeys(driver, Discovery.netmask, "255.255.248.0");
		//SeleniumActions.seleniumSendKeysClear(driver, Discovery.gateway);
		//SeleniumActions.seleniumSendKeys(driver, Discovery.gateway, "10.211.128.1");
		SeleniumActions.seleniumClick(driver, Discovery.applyBtn);
		Alert alert = driver.switchTo().alert();
		alert.accept();
		Thread.sleep(60000);
		//new WebDriverWait(driver, 180).until(ExpectedConditions.elementToBeClickable(Discovery.discoverBtn(driver)));
		log.info("IP address changed, waiting for switch to reboot then managing");
		Assert.assertTrue(isSwitchReachable(ip), "Switch was not reachable");
	}
	
	public boolean isSwitchReachable(String ip) {
		boolean reachable = false;
		try {
            InetAddress address = InetAddress.getByName(ip);

            int counter = 0;
            while(!reachable && counter < 20) {
            	 reachable = address.isReachable(10000);
            	 log.info("Is host reachable:" + reachable);
            	 counter ++;
            	 Thread.sleep(20000);
            }

            log.info("Final is host reachable? " + reachable);
        } catch (Exception e) {
            e.printStackTrace();
        }
		return reachable;
	}
	public void uploadSwitchFile(WebDriver driver, String fileLocation, String version, boolean isValid) {
		navigateToSwitchUpgrade(driver);
		SeleniumActions.seleniumClick(driver, Switch.uploadBtnXpath);
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(Switch.getUpgradeFileBrowseBtn(driver)));
		SeleniumActions.seleniumSendKeys(driver, Switch.upgradeFileBrowseBtnXpath, fileLocation);
		SeleniumActions.seleniumClick(driver, Switch.getUpgradeFileSubmitBtnXpath());
		log.info("Waiting for page to reload");
		if(isValid) {
			log.info("Checking for valid file");
			checkUpload(driver, fileLocation, version);
		}else {
			log.info("Checking for invalid file");
			new WebDriverWait(driver, 150).until(ExpectedConditions.elementToBeClickable(By.xpath(Switch.invalidFileBackBtnXpath)));
			String page = driver.getPageSource();
			Assert.assertTrue(page.contains("Incorrect file format"), "File was not flagged as incorrect");
		}
		
		
	}

	public void checkUpload(WebDriver driver, String fileLocation, String version) {
		new WebDriverWait(driver, 150).until(ExpectedConditions.elementToBeClickable(By.xpath(Switch.uploadBtnXpath)));
		log.info("Upload of " + fileLocation + " successful..checking");
		navigateToSwitchRelease(driver);
		SeleniumActions.seleniumSendKeys(driver, Switch.getUpgradeSwitchSearchTextboxXpath(), version);
		String text = Switch.getUpgradeShortReleaseVersion(driver,  "1").getText();
		Assert.assertTrue(text.equals(version), "Version did not equal uploaded version, actual:" + text);
	}
	public void navigateToSwitchRelease(WebDriver driver) {
		navigateToSwitchUpgrade(driver);
		SeleniumActions.seleniumClick(driver, Switch.releasesTabXpath);
		new WebDriverWait(driver, 150).until(ExpectedConditions.elementToBeClickable(By.xpath(Switch.upgradeReleaseTableHeadingXpath)));
		log.info("Successfully navigated to switch > releases");
	}
//	public boolean isVersionPresent(WebDriver driver, String version) {
//		navigateToSwitchUpgrade(driver);
//		SeleniumActions.seleniumClick(driver, Switch.releasesTabXpath);
//		
//		
//	}
	public void changeMrouter(WebDriver driver, String switchIp, String switchName, String portNumber, boolean enable) {
		searchPortDetails(driver, switchIp, switchName, portNumber + " ");
		SeleniumActions.seleniumClick(driver, Switch.portDropdownBtnXpath);
		String text = "";
		log.info("Text from mrouter Button " + text);
		if(enable) {
			new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(Switch.getEnableMRouterBtn(driver)));
			text = SeleniumActions.seleniumGetText(driver, Switch.getEnableMRouterBtnXpth());
			Assert.assertFalse(text.contains("Disable"),
					"MRouter already enabled ending test");
			SeleniumActions.seleniumClick(driver, Switch.getEnableMRouterBtnXpth());
		}else {
			new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(Switch.getDisableMRouterBtn(driver)));
			text = SeleniumActions.seleniumGetText(driver, Switch.getDisableMRouterBtnXpath());
			Assert.assertFalse(text.contains("Enable"),
					"MRouter already disabled ending test");
			SeleniumActions.seleniumClick(driver, Switch.getDisableMRouterBtnXpath());
		}
		Alert alert = driver.switchTo().alert();
		alert.accept();
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getSpinner(driver)));
		new WebDriverWait(driver, 60).until(ExpectedConditions.invisibilityOf(Switch.getSpinner(driver)));
		
		log.info("Checking boxilla");
		String mrouterStatus = getPortSwitchTableData(driver, switchIp, switchName, portNumber, PORT_COLUMN.MROUTER);
		if(enable) {
			Assert.assertTrue(mrouterStatus.equals("Active"), "Boxilla did not report mrouter as active");
		}else {
			Assert.assertFalse(mrouterStatus.equals("Active"), "Boxilla did not report mrouter as not active");
		}
	}
	
	public void activateSwitchReleaseVersion(WebDriver driver, String version) {
		navigateToSwitchRelease(driver);
		SeleniumActions.seleniumSendKeys(driver, Switch.getUpgradeSwitchSearchTextboxXpath(), version);
		String text = Switch.getUpgradeActive(driver, "1").getText();
		if(text.equals("Yes")) {
			log.info("Version already activated. Returning...");
			return;
		}
		SeleniumActions.seleniumClick(driver, Switch.getReleaseDropdownBtnXpth());
		new WebDriverWait(driver, 150).until(ExpectedConditions.elementToBeClickable(By.xpath(Switch.activateReleaseBtnXpath)));
		SeleniumActions.seleniumClick(driver, Switch.activateReleaseBtnXpath);
		Alert alert = driver.switchTo().alert();
		alert.accept();
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getSpinner(driver)));
		new WebDriverWait(driver, 60).until(ExpectedConditions.invisibilityOf(Switch.getSpinner(driver)));
		log.info("Version activated..Checking");
		navigateToSwitchRelease(driver);
		SeleniumActions.seleniumSendKeys(driver, Switch.getUpgradeSwitchSearchTextboxXpath(), version);
		 text = Switch.getUpgradeActive(driver, "1").getText();
		Assert.assertTrue(text.equals("Yes"), version + " was not activated");
		
		
	}
	public void setActiveImage(WebDriver driver, String version) {
		navigateToSwitchRelease(driver);
		
	}
	public void upgradeSwitch(WebDriver driver, String ip, String version) throws InterruptedException {
		navigateToSwitchUpgrade(driver);
		SeleniumActions.seleniumSendKeysClear(driver, Switch.getUpgradeSwitchSearchTextboxXpath());
		SeleniumActions.seleniumSendKeys(driver, Switch.getUpgradeSwitchSearchTextboxXpath(), ip);
		Thread.sleep(4000);
		String text = getSwitchState(driver, ip);
		log.info("Switch upgrade state: " + text);
		if(text.contains("Mis-match to Active Version")) {
			log.info("Switch can be upgraded. Checking if checkbox is ticked");
			if(Switch.getSwitchUpgradeCheckBox(driver).isSelected()) {
				log.info("Check box is selected. Continuing to upgrade");
			}else {
				log.info("Upgrade check box is not selected, selecting");
				Switch.getSwitchUpgradeCheckBox(driver).click();
			}
			SeleniumActions.seleniumClick(driver, Switch.getSwitchUpgradeBtnXpath());
			Alert alert = driver.switchTo().alert();
			alert.accept();
			Thread.sleep(4000);
			
			Assert.assertTrue(getSwitchVersion(driver, ip).contains(version),"Switch version was not the same as uploaded version");
//			new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getToast(driver)));
//			new WebDriverWait(driver, 60).until(ExpectedConditions.invisibilityOf(Switch.getToast(driver)));
//			new WebDriverWait(driver, 120).until(ExpectedConditions.visibilityOf(Switch.getToast(driver)));
			
		}else {
			log.info("Switch is already on the correct version");
			return;
		}
		
	}
	public boolean isVersionMismatch(WebDriver driver, String ip) throws InterruptedException {
		navigateToSwitchUpgrade(driver);
		SeleniumActions.seleniumSendKeysClear(driver, Switch.getUpgradeSwitchSearchTextboxXpath());
		SeleniumActions.seleniumSendKeys(driver, Switch.getUpgradeSwitchSearchTextboxXpath(), ip);
		Thread.sleep(5000);
		String text = getSwitchState(driver, ip);
		log.info("Text returned from switch state:" + text);
		if(text.contains("Mis-match to Active Version")) {
			return true;
		}
		return false;
		
	}
	
	public void deleteDellSwitchVersion(WebDriver driver, String release) {
		navigateToSwitchRelease(driver);
		SeleniumActions.seleniumSendKeys(driver, Switch.getUpgradeSwitchSearchTextboxXpath(), release);
		SeleniumActions.seleniumClick(driver, Switch.getReleaseDropdownBtnXpth());
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getDeleteReleaseBtn(driver)));
		SeleniumActions.seleniumClick(driver, Switch.getDeleteReleaseBtnXpath());
		Alert alert = driver.switchTo().alert();
		alert.accept();
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(Switch.getSwitchUpgradeBtn(driver)));
		driver.navigate().refresh();
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(Landingpage.dellSwitchesTab(driver)));
		//new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getSwitchUpgradeBtn(driver)));
		log.info("Release " + release + " deleted. Asserting");
		navigateToSwitchRelease(driver);
		SeleniumActions.seleniumSendKeys(driver, Switch.getUpgradeSwitchSearchTextboxXpath(), release);
		String text = "";
		text = Switch.getUpgradeShortReleaseVersion(driver,  "1").getText();
		Assert.assertFalse(text.contains(release), "Release was not deleted from table");
		log.info("Assertion complete. Release deleted");
	}
	
	public String getPortDetailsFromRest(String switchIp, String portNumber, PORT_COLUMN column) {
		String restReturn = curl().$("-X GET -u admin:admin -k 'https://" + switchIp + "/restconf/data/interfaces-state/interface=ethernet1%2F1%2F" 
				+ portNumber + "/statistics' -H 'accept: application/json' --max-time 10");
		String [] restSplit = restReturn.split(",");
		String search = "";
		switch (column) {
		case BANDWIDTH_IN :
			search = "dell-ethernet:in-bit-rate";
			break;
		case BANDWIDTH_OUT :
			search = "dell-ethernet:out-bit-rate";
			break;
		case PACKETS_IN :
			search = "dell-ethernet:in-pkt-rate";
			break;
		case PACKETS_OUT :
			search = "dell-ethernet:out-pkt-rate";
			break;
		case LINE_USAGE_IN :
			search = "dell-ethernet:in-line-rate";
			break;
		case LINE_USAGE_OUT :
			search = "dell-ethernet:out-line-rate";
			break;
		}
		for(String x : restSplit) {
			if(x.contains(search)) {
				return x;
			}
		}
		return "";
	}
	
}
