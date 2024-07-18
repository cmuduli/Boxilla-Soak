package methods;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.junit.rules.ExpectedException;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.SkipException;

import extra.SeleniumActions;
import extra.Ssh;
import extra.StartupTestCase;
import invisaPC.Rest.VideoFacts;
import objects.ActiveConnectionElements;
import objects.Connections;
import objects.Connections.VIA;
import objects.Discovery;
import objects.Landingpage;
import objects.Switch;
import objects.Users;
import testNG.Utilities;
/**
 * Class contains apis to interact with connection related tasks in boxilla and invisaPC
 * @author Boxilla
 *
 */
public class ConnectionsMethods {

	UsersMethods usermethods = new UsersMethods();
	final static Logger log = Logger.getLogger(ConnectionsMethods.class);

	public String autoRefreshPoll(String originalValue, String xpath, WebDriver driver) {
		
		
		String newValue = "";
		int counter = 0;
		boolean timer = true;
		while(timer) {
			try {
				Thread.sleep(10000);
			}catch(Exception e) {
				
			}
			counter++;
			newValue = SeleniumActions.seleniumGetText(driver, xpath);
			log.info("Value:" + newValue);
			if(!newValue.equals(originalValue) || counter==20 ) {
				timer = false;
				return newValue;
			}
		}
		return newValue;
		
	}
	
	public void setDisplayOrientation(WebDriver driver, Connections.Orientation orientation, boolean isTemplate) {
		switch(orientation) {
			case H12 :
				log.info("Setting Orientation to H12");
				if(!isTemplate) {
					SeleniumActions.dragAndDrop(driver, Connections.getOriginalDisplay2Orientation(), Connections.getOrientationRightColumn());
				}else {
					SeleniumActions.dragAndDrop(driver, Connections.getOriginalTemplateDisplay2Orientation(), Connections.getTemplateOrientationRightColumn());
				}
				break;
			case H21 :
				log.info("Setting Orientation to H21");
				if(!isTemplate) {
					SeleniumActions.dragAndDrop(driver, Connections.getOriginalDisplay1Orientation(), Connections.getOrientationRightColumn());
				}else {
					SeleniumActions.dragAndDrop(driver, Connections.getOriginalTemplateDisplay1Orientation(), Connections.getTemplateOrientationRightColumn());
				}
				break;
			case V21:
				log.info("Setting Orientation to V21");
				if(!isTemplate) {
					SeleniumActions.dragAndDrop(driver, Connections.getOriginalDisplay1Orientation(), Connections.getOriginalDisplay2Orientation());
				}else {
					SeleniumActions.dragAndDrop(driver, Connections.getOriginalTemplateDisplay1Orientation(), Connections.getOriginalTemplateDisplay2Orientation());
				}
				break;
			case V12:
				log.info("V12 is default orientation. Doing nothing");
				break;
		}
	}
	public void editPairedConnectionOrientation(WebDriver driver, String connectionToEdit, Connections.Orientation ore) throws InterruptedException {
		navigateToConnectionsManage(driver);
		//find the connection to edit
		SeleniumActions.seleniumSendKeys(driver, Connections.searchTextbox, connectionToEdit);
		//Connections.searchTextbox(driver).sendKeys(connectionName);
		timer(driver);
		String connectionTableText = SeleniumActions.seleniumGetText(driver, Connections.connectionTable);
		//String connectionTableText = Connections.connectionTable(driver).getText();
		Assert.assertTrue(connectionTableText.contains(connectionToEdit),
				"Connection table text did not contain: " + connectionToEdit + ", actual text: " + connectionTableText); // Asserting connection name present in connection table
		
		//click dropdown
		SeleniumActions.seleniumClick(driver, Connections.getConnectionDropdown());
		SeleniumActions.seleniumClick(driver, Connections.getConnectionDropdownEdit());
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(By.xpath(Connections.getEditModalNextBtn())));
		
		//navigate to private shared
		SeleniumActions.seleniumClick(driver, Connections.getEditModalNextBtn());
		setDisplayOrientation(driver, ore, false);
		SeleniumActions.seleniumClick(driver, Connections.getEditModalNextBtn());
		saveConnection(driver, connectionToEdit);
		
		
	}
	public void editPairedConnectionType(WebDriver driver, String connectionToEdit, boolean isShared) throws InterruptedException{
		navigateToConnectionsManage(driver);
		//find the connection to edit
		SeleniumActions.seleniumSendKeys(driver, Connections.searchTextbox, connectionToEdit);
		//Connections.searchTextbox(driver).sendKeys(connectionName);
		timer(driver);
		String connectionTableText = SeleniumActions.seleniumGetText(driver, Connections.connectionTable);
		//String connectionTableText = Connections.connectionTable(driver).getText();
		Assert.assertTrue(connectionTableText.contains(connectionToEdit),
				"Connection table text did not contain: " + connectionToEdit + ", actual text: " + connectionTableText); // Asserting connection name present in connection table
		
		//click dropdown
		SeleniumActions.seleniumClick(driver, Connections.getConnectionDropdown());
		SeleniumActions.seleniumClick(driver, Connections.getConnectionDropdownEdit());
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(By.xpath(Connections.getEditModalNextBtn())));
		
		//navigate to private shared
		SeleniumActions.seleniumClick(driver, Connections.getEditModalNextBtn());
		if(isShared) {
			chooseCoonectionType(driver, "shared");
		}else {
			chooseCoonectionType(driver, "private");
		}
		SeleniumActions.seleniumClick(driver, Connections.getEditModalNextBtn());
		String completeText = SeleniumActions.seleniumGetText(driver, Connections.getCompeteConnectionModal());
		String clean = completeText.replaceAll("\r", "").replaceAll("\n", "");
		log.info(clean);
		
		//check shared
		if(isShared) {
			Assert.assertTrue(clean.contains("Connection TypeShared"), "Connection type was not shared");
		}else {
			Assert.assertTrue(clean.contains("Connection TypePrivate"), "Connection type was not private");
		}
		saveConnection(driver, connectionToEdit);
	}
	public void editPairedConnectionIps(WebDriver driver, String connectionToEdit, String ip1, String ip2) throws InterruptedException {
		navigateToConnectionsManage(driver);
		//find the connection to edit
		SeleniumActions.seleniumSendKeys(driver, Connections.searchTextbox, connectionToEdit);
		//Connections.searchTextbox(driver).sendKeys(connectionName);
		timer(driver);
		String connectionTableText = SeleniumActions.seleniumGetText(driver, Connections.connectionTable);
		//String connectionTableText = Connections.connectionTable(driver).getText();
		Assert.assertTrue(connectionTableText.contains(connectionToEdit),
				"Connection table text did not contain: " + connectionToEdit + ", actual text: " + connectionTableText); // Asserting connection name present in connection table
		
		//click dropdown
		SeleniumActions.seleniumClick(driver, Connections.getConnectionDropdown());
		SeleniumActions.seleniumClick(driver, Connections.getConnectionDropdownEdit());
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(By.xpath(Connections.getEditModalNextBtn())));
		SeleniumActions.seleniumSendKeysClear(driver, Connections.getEditConnectionIp1());
		SeleniumActions.seleniumSendKeys(driver, Connections.getEditConnectionIp1(), ip1);
		SeleniumActions.seleniumSendKeysClear(driver, Connections.getEditConnectionIp2());
		SeleniumActions.seleniumSendKeys(driver, Connections.getEditConnectionIp2(), ip2);
		
		SeleniumActions.seleniumClick(driver, Connections.getEditModalNextBtn());
		SeleniumActions.seleniumClick(driver, Connections.getEditModalNextBtn());
		//reviewnew WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.getCompeteConnectionModal())));
		String completeText = SeleniumActions.seleniumGetText(driver, Connections.getCompeteConnectionModal());
		String clean = completeText.replaceAll("\r", "").replaceAll("\n", "");
		log.info(clean);
		Assert.assertTrue(clean.contains(ip1), "review modal did not contain IP1");
		Assert.assertTrue(clean.contains(ip2), "review modal did not contain IP2");
		saveConnection(driver, connectionToEdit);
		
		
	}
	
	//assumes cpage is connections manage and connection has been searched for
	public void checkConnectionDetailsConnectionManage(WebDriver driver, String name, String type, String via) {
		if(!name.equals("") && name != null) {
			log.info("Checking con name");
			String realName = SeleniumActions.seleniumGetText(driver, Connections.getConnectionNameFromConManageTable());
			Assert.assertTrue(realName.equals(name), "Connection name did not match. Expected:" + name + " , Actual:" + realName);
		}
		
		if(!type.equals("") && type != null) {
			log.info("Checking con type");
			String realType = SeleniumActions.seleniumGetText(driver, Connections.getConnectionTypeFromConManageTable());
			Assert.assertTrue(realType.equals(type), "Connection type did not match. Expected:" + type + ", Actual:" + realType);
		}
		
		if(!via.equals("") && via != null) {
			log.info("Checking con via");
			String realVia = SeleniumActions.seleniumGetText(driver, Connections.getConnectionViaFromConManageTable());
			Assert.assertTrue(realVia.equals(via), "Connection via did not match. Expected:" + via + " , Actual:" + realVia);
		}
		
	
	}
	
	//using Strings instead of booleans cause im too lazy to figure out how to use dataprovider with bools
	public void createTxPairConnection(WebDriver driver, String connectionName, String isTemplate, String templateName, String targets, Connections.Orientation orientation,  String ip1, String ip2
			, String shared, String audio, String audio2,  String persistent, String viewOnly) throws InterruptedException {
		navigateToConnectionsManage(driver);
		log.info("Creating TX pair connection");
		SeleniumActions.seleniumClick(driver, Connections.btnAddConnection);
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.getAddConnectionModal())));
		WebElement txPair = driver.findElement(By.xpath(Connections.getTxPairButton()));
		SeleniumActions.exectuteJavaScriptClick(driver, txPair);
		if(isTemplate.equals("true")) {
			
			WebElement useTemplate = driver.findElement(By.xpath(Connections.useTemplateYes()));
			SeleniumActions.exectuteJavaScriptClick(driver, useTemplate);
			SeleniumActions.exectuteJavaScriptClick(driver, txPair);
			SeleniumActions.seleniumSendKeys(driver, Connections.connectionName, connectionName);
			SeleniumActions.seleniumSendKeys(driver, Connections.getPairedIpAddress1(), ip1);
			SeleniumActions.seleniumSendKeys(driver, Connections.getPairedIpAddress2(), ip2);
			SeleniumActions.seleniumClick(driver, Connections.btnNext);
			SeleniumActions.seleniumDropdown(driver, Connections.getConnectionTemplatePair(), templateName);
			propertyInfoClickNext(driver);
			assertReviewPaired(driver, connectionName, targets, ip1, ip2, shared, audio, audio2, persistent, viewOnly);
			saveConnection(driver, connectionName);
			return;
		}
		if(targets.equals("1")) {
			WebElement target1 = driver.findElement(By.xpath(Connections.getPairedTarget1()));
			SeleniumActions.exectuteJavaScriptClick(driver, target1);
			//SeleniumActions.seleniumClick(driver, Connections.getPairedTarget1());
		}else {
			WebElement target2 = driver.findElement(By.xpath(Connections.getPairedTarget2()));
			SeleniumActions.exectuteJavaScriptClick(driver, target2);
			//SeleniumActions.(driver, Connections.getPairedTarget2());
		}
		SeleniumActions.seleniumSendKeys(driver, Connections.connectionName, connectionName);
		SeleniumActions.seleniumSendKeys(driver, Connections.getPairedIpAddress1(), ip1);
		SeleniumActions.seleniumSendKeys(driver, Connections.getPairedIpAddress2(), ip2);
		SeleniumActions.seleniumClick(driver, Connections.btnNext);
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.connectionTypePrivate)));
		
		//property info
		if(shared.equals("true")) 
			chooseCoonectionType(driver, "shared");
		
		if(audio.equals("true")) {
			enableAudio(driver); 
			if(audio2.equals("true")) {
				WebElement audioElement1 = driver.findElement(By.xpath(Connections.getAudioIp1()));
				SeleniumActions.exectuteJavaScriptClick(driver, audioElement1);
			}
		}
		
		if(persistent.equals("true"))
			enablePersistenConnection(driver);
		
		if(viewOnly.equals("true"))
			enableViewOnlyConnection(driver);
		
		if(targets.equals("2")) {
			setDisplayOrientation(driver, orientation, false);
		}
		
		propertyInfoClickNext(driver);
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.getCompeteConnectionModal())));
		String completeText = SeleniumActions.seleniumGetText(driver, Connections.getCompeteConnectionModal());
		String clean = completeText.replaceAll("\r", "").replaceAll("\n", "");
		log.info(clean);
		
		assertReviewPaired(driver, connectionName, targets, ip1, ip2, shared, audio, audio2, persistent, viewOnly);
		saveConnection(driver, connectionName);
		
	}

	private void assertReviewPaired(WebDriver driver, String connectionName, String targets, String ip1, String ip2, String shared,
			String audio, String audio2, String persistent, String viewOnly) {
		//assert the review modal
		
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.getCompeteConnectionModal())));
		String completeText = SeleniumActions.seleniumGetText(driver, Connections.getCompeteConnectionModal());
		String clean = completeText.replaceAll("\r", "").replaceAll("\n", "");
		log.info(clean);
		
		//shared private
		if(shared.equals("true")) {
			Assert.assertTrue(clean.contains("Connection TypeShared"), "Connection type was not shared");
		}else {
			Assert.assertTrue(clean.contains("Connection TypePrivate"), "Connection type was not private");
		}
		//name
		Assert.assertTrue(clean.contains(connectionName), "Connection name was not in review modal");
		//number of targets
		if(targets.equals("1")) {
			Assert.assertTrue(clean.contains("Targets1"), "Number of targets on review modal was not 1");
		}else {
			Assert.assertTrue(clean.contains("Targets2"), "Number of targets on review modal was not 2");
		}
		// ip addresses
		Assert.assertTrue(clean.contains(ip1), "review modal did not contain IP1");
		Assert.assertTrue(clean.contains(ip2), "review modal did not contain IP2");
		//AUDIO
		if(audio.equals("true")) {
			Assert.assertTrue(clean.contains("AudioYes"), "Review modal did not contain Audio Yes");
			if(audio2.equals("true")) {
				Assert.assertTrue(clean.contains("Audio: IP Address 2Yes"), "Review modal did not contain Audio IP 2 Yes");
			}else {
				Assert.assertTrue(clean.contains("Audio: IP Address 1Yes"), "Review modal did not contain Audio IP 1 Yes");
			}
		}else {
			Assert.assertTrue(clean.contains("AudioNo"), "Review modal did not contain Audio No");
		}
		//persistent
		if(persistent.equals("true")) {
			Assert.assertTrue(clean.contains("Persistent ConnectionYes"), "Review modal did not contain Persistent Connection Yes");
		}else {
			Assert.assertTrue(clean.contains("Persistent ConnectionNo"), "Review modal did not contain Persistent Connection No");
		}
		//viewonly
		if(viewOnly.equals("true")) {
			Assert.assertTrue(clean.contains("View OnlyYes"), "Review modal did not contain View Only Yes");
		}else {
			Assert.assertTrue(clean.contains("View OnlyNo"), "Review modal did not contain View Only No");
		}
		
	}
	
	public void checkConnectionIsActiveSsh(String username, String password, String ip, String connectionName) {
		Ssh ssh = new Ssh(username, password, ip);
		ssh.loginToServer();
		String output = ssh.sendCommand("ps -ax");
		Assert.assertTrue(output.contains(connectionName), "Connection " + connectionName + " was not running");
		log.info("Connection " + connectionName + " is running");
	}
	public String getTimeFromActiveConnectionConfigTable(WebDriver driver) throws InterruptedException {
		navigateToActiveConnection(driver);
		SeleniumActions.seleniumClick(driver, ActiveConnectionElements.getConfigurationTableTab());
		String bw = SeleniumActions.seleniumGetText(driver, ActiveConnectionElements.getConfigTableConnectionTime());
		log.info("Time from Active Connection config table:" + bw);
		return bw;
	}
	
	
	public String getNetworkBWFromActiveConnectionFrameTable(WebDriver driver) throws InterruptedException {
		navigateToActiveConnection(driver);
		SeleniumActions.seleniumClick(driver, ActiveConnectionElements.getFrameRateTableTab());
		String bw = SeleniumActions.seleniumGetText(driver, ActiveConnectionElements.getFrameTableConnectionNetworkBW());
		log.info("Total connection network B/W from Active Connection frame table:" + bw);
		return bw;
	}
	
	public String getNetworkBWFromActiveConnectionTable(WebDriver driver) throws InterruptedException {
		navigateToActiveConnection(driver);
		String bw = SeleniumActions.seleniumGetText(driver, ActiveConnectionElements.getConnectionTableConnectionNetworkBW());
		log.info("Total connection network B/W from Active Connection table:" + bw);
		return bw;
	}
	
	public String getConnectionTimeFromActiveConnectionTable(WebDriver driver) throws InterruptedException {
		navigateToActiveConnection(driver);
		String time = SeleniumActions.seleniumGetText(driver, ActiveConnectionElements.getConnectionTableConnectionTime());
		log.info("Connection time from Active Connection table:" + time);
		return time;
	}

	public String[] getActiveConnectionsDetails(WebDriver driver, int[][] rows) throws InterruptedException {
		String[] connections = new String[rows.length];
		navigateToActiveConnection(driver);
		for(int j=0; j < rows.length; j++) {
			for(int k=0; k < rows[k].length; k++) {
				connections[j] = SeleniumActions.seleniumGetText(driver, ActiveConnectionElements.getRowAndColumnFromActiveConnectionTable(rows[j][0], rows[j][1]));
				log.info("Row:" + rows[j][0]);
				log.info("Column:"  + rows[j][1]);
				log.info("Return:" + connections[j]);
			}
		}
		return connections;
		
	}
	public String searchActiveConnectionForTransmitter(WebDriver driver, String txName) throws InterruptedException {
		navigateToActiveConnection(driver);
		SeleniumActions.seleniumSendKeysClear(driver, ActiveConnectionElements.getActiveConnectionSearchBox());
		SeleniumActions.seleniumSendKeys(driver, ActiveConnectionElements.getActiveConnectionSearchBox(), txName);
		String tx  = "";
		tx  = SeleniumActions.seleniumGetText(driver, ActiveConnectionElements.getTransmitterFromSearch(txName));
		return tx;
	}

	public String getTransmitterFromActiveConnectionTable(WebDriver driver) throws InterruptedException {
		navigateToActiveConnection(driver);
		String tx = SeleniumActions.seleniumGetText(driver, ActiveConnectionElements.getConnectionTableTransmitter());
		log.info("TX from Active Connection table:" + tx);
		return tx;
	}
	
	public String getUserFromActiveConnectionTable(WebDriver driver) throws InterruptedException {
		navigateToActiveConnection(driver);
		String user = SeleniumActions.seleniumGetText(driver, ActiveConnectionElements.getConnectionTableUser());
		log.info("User from Active Connection table:" + user);
		return user;
	}
	
	public String getConnectionNameFromActiveConnectionTable(WebDriver driver) throws InterruptedException {
		navigateToActiveConnection(driver);
		String name = SeleniumActions.seleniumGetText(driver, ActiveConnectionElements.getConnectionTableConnectionName());
		log.info("Connection name from Active Connection table:" + name);
		return name;
	}
	
	public String getReceiverFromActiveConnectionTable(WebDriver driver) throws InterruptedException {
		navigateToActiveConnection(driver);
		String receiver = SeleniumActions.seleniumGetText(driver, ActiveConnectionElements.getConnectionTableReceiver());
		log.info("Receiver from Active Connection Table:" + receiver);
		return receiver;
	}

	public void editConnectionTemplateName(WebDriver driver, String oldTemplateName, String newTemplateName ) throws InterruptedException {
		editConnectionTemplate(driver, oldTemplateName);
		String templateNameXpath = "//input[@id='property-name']";
		SeleniumActions.seleniumSendKeysClear(driver, templateNameXpath);
		SeleniumActions.seleniumSendKeys(driver, templateNameXpath, newTemplateName);
		SeleniumActions.seleniumClick(driver, "//button[@id='btn-property-save']");
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.templateToastMessage)));
		String toastMsg = SeleniumActions.seleniumGetText(driver, Connections.templateToastMessage);
		Assert.assertTrue(toastMsg.contains("Successfully"), "Toasta message did not contain success, actual:" + toastMsg);
	}
	
	
	public void editConnectionTemplateVia(WebDriver driver, String oldTemplateName, Connections.VIA via ) throws InterruptedException {
		editConnectionTemplate(driver, oldTemplateName);
		
		switch(via) {
		case VM :
			SeleniumActions.seleniumClick(driver, "//div[3]//div[1]//div[1]//label[2]");
			break;
		case TX:
			SeleniumActions.seleniumClick(driver, "//div[@id='property-form']//div[3]//div[1]//div[1]//label[1]");
			break;
		case POOL:
			SeleniumActions.seleniumClick(driver, "//div[@id='property-form']//label[3]");
			break;
		case BROKER:
			SeleniumActions.seleniumClick(driver, "//div[@id='property-form']//label[4]");
			break;
		case HORIZON :
			SeleniumActions.seleniumClick(driver, "//div[@id='property-form']//label[5]");
			break;
		}
		
//		String templateNameXpath = "//input[@id='property-name']";
//		SeleniumActions.seleniumSendKeysClear(driver, templateNameXpath);
//		SeleniumActions.seleniumSendKeys(driver, templateNameXpath, newTemplateName);
		SeleniumActions.seleniumClick(driver, "//button[@id='btn-property-save']");
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.templateToastMessage)));
		String toastMsg = SeleniumActions.seleniumGetText(driver, Connections.templateToastMessage);
		log.info("Toast Message:" + toastMsg);
		Assert.assertTrue(toastMsg.contains("Successfully"), "Toasta message did not contain success, actual:" + toastMsg);
	}
	
	
	public boolean[] checkConnectionOptionsViaTx(WebDriver driver, String name, String via, String type, String extDesk, String audio, String usb,
			String persistent, String view) throws InterruptedException {
		log.info("Checking connection details in Boxilla UI");
		navigateToConnectionsManage(driver);
		SeleniumActions.seleniumSendKeys(driver, Connections.searchTextbox, name + " ");		//add a space to only get one in the table
		isViewOnlyEnabled(driver, view);
		checkConnectionDetailsConnectionManage(driver, name, type, via);
		boolean [] options = new boolean[4];
//		//check options
		if(extDesk.equals("Yes")) {
			new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.getIconExtDesktopEnabled())));
			//options[0] = SeleniumActions.seleniumIsDisplayed(driver, Connections.getIconExtDesktopEnabled());
		}else {
			new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.getIconExtDesktopDisabled())));
			//options[0] = SeleniumActions.seleniumIsDisplayed(driver, Connections.getIconExtDesktopDisabled());
		}
		if(audio.equals("Yes")) {
			new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.getIconAudioEnabled())));
			//options[1] = SeleniumActions.seleniumIsDisplayed(driver, Connections.getIconAudioEnabled());
		}else {
			new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.getIconAudioDisabled())));
			//options[1] = SeleniumActions.seleniumIsDisplayed(driver, Connections.getIconAudioDisabled());
		}
		if(usb.equals("Yes")) {
			new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.getIconUsbEnabled())));
			//options[2] = SeleniumActions.seleniumIsDisplayed(driver, Connections.getIconUsbEnabled());
		}else {
			new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.getIconUsbDisabled())));
			//options[2] = SeleniumActions.seleniumIsDisplayed(driver, Connections.getIconUsbDisabled());
		}
		if(persistent.equals("Yes")) {
			new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.xpath( Connections.getIconPersistentEnabled())));
			//options[3] = SeleniumActions.seleniumIsDisplayed(driver, Connections.getIconPersistentEnabled());
		}else {
			new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.xpath( Connections.getIconPersistentDisabled())));
			//options[3] = SeleniumActions.seleniumIsDisplayed(driver, Connections.getIconPersistentDisabled());
		}
		return options;
	}
	
	public void isViewOnlyEnabled(WebDriver driver, String view) {
		String att = SeleniumActions.getAttribute(driver, Connections.getIconViewOnlyDisabled(), "style");
		if(view.equals("Yes")) {
			Assert.assertTrue(att.contains("rgb(184, 233, 134)"), "View only was not enabled");
		}else {
			Assert.assertTrue(att.contains("rgb(206, 207, 200)"), "View only was not disabled");
		}
	}
	public boolean[] checkConnectionOptionsVMPool(WebDriver driver, String name, String via, String type, String extDesk, String audio, 
			String usb, String view) throws InterruptedException {
		navigateToConnectionsManage(driver);
		SeleniumActions.seleniumSendKeys(driver, Connections.searchTextbox, name + " ");		//add a space to only get one in the table
		isViewOnlyEnabled(driver,  view);
		checkConnectionDetailsConnectionManage(driver, name, type, via);
		boolean [] options = new boolean[4];
		//check options
		if(extDesk.equals("Yes")) {
			log.info("Checking ext desktop is enabled");
			new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.getIconExtDesktopEnabled())));
			//options[0] = SeleniumActions.seleniumIsDisplayed(driver, Connections.getIconExtDesktopEnabled());
		}else {
			log.info("Checking ext desktop is disabled");
			new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.getIconExtDesktopDisabled())));
			//options[0] = SeleniumActions.seleniumIsDisplayed(driver, Connections.getIconExtDesktopDisabled());
		}
		if(audio.equals("Yes")) {
			log.info("Checking audio is enabled");
			new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.getIconAudioEnabled())));
			//options[1] = SeleniumActions.seleniumIsDisplayed(driver, Connections.getIconAudioEnabled());
		}else {
			log.info("Checking audio is disbled");
			new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.getIconAudioDisabled())));
			//options[1] = SeleniumActions.seleniumIsDisplayed(driver, Connections.getIconAudioDisabled());
		}
		if(usb.equals("Yes")) {
			log.info("Checking usb is enabled");
			new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.getIconUsbEnabled())));
			//options[2] = SeleniumActions.seleniumIsDisplayed(driver, Connections.getIconUsbEnabled());
		}else {
			log.info("Checking usb is disbled");
			new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.getIconUsbDisabled())));
			//options[2] = SeleniumActions.seleniumIsDisplayed(driver, Connections.getIconUsbDisabled());
		}
		return options;
	}
	
	public boolean[] checkConnectionOptionsVM(WebDriver driver, String name, String via, String type, String extDesk, String audio, String usb,
			 String nla, String view) throws InterruptedException {
		navigateToConnectionsManage(driver);
		SeleniumActions.seleniumSendKeys(driver, Connections.searchTextbox, name + " ");		//add a space to only get one in the table
		isViewOnlyEnabled(driver,  view);
		checkConnectionDetailsConnectionManage(driver, name, type, via);
		boolean [] options = new boolean[4];
		//check options
		if(extDesk.equals("Yes")) {
			log.info("Checking ext desktop is enabled");
			new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.getIconExtDesktopEnabled())));
			//options[0] = SeleniumActions.seleniumIsDisplayed(driver, Connections.getIconExtDesktopEnabled());
		}else {
			log.info("Checking ext desktop is disabled");
			new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.getIconExtDesktopDisabled())));
			//options[0] = SeleniumActions.seleniumIsDisplayed(driver, Connections.getIconExtDesktopDisabled());
		}
		if(audio.equals("Yes")) {
			log.info("Checking audio is enabled");
			new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.getIconAudioEnabled())));
			//options[1] = SeleniumActions.seleniumIsDisplayed(driver, Connections.getIconAudioEnabled());
		}else {
			log.info("Checking audio is disbled");
			new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.getIconAudioDisabled())));
			//options[1] = SeleniumActions.seleniumIsDisplayed(driver, Connections.getIconAudioDisabled());
		}
		if(usb.equals("Yes")) {
			log.info("Checking usb is enabled");
			new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.getIconUsbEnabled())));
			//options[2] = SeleniumActions.seleniumIsDisplayed(driver, Connections.getIconUsbEnabled());
		}else {
			log.info("Checking usb is disbled");
			new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.getIconUsbDisabled())));
			//options[2] = SeleniumActions.seleniumIsDisplayed(driver, Connections.getIconUsbDisabled());
		}
		if(nla.equals("Yes")) {
			log.info("Checking nla is enabled");
			new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.getIconNlaEnabled())));
		}else {
			log.info("Checking nla is enabled");
			new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.getIconNlaDisabled())));
		}
		return options;
	}


	private void editConnectionTemplate(WebDriver driver, String oldTemplateName) throws InterruptedException {
		navigateToConnectionsManage(driver);
		//click edit connection template
		String editTemplateButton = "//span[contains(text(),'Edit Connection Template')]";
		SeleniumActions.seleniumClick(driver, "//span[contains(text(),'Edit Connection Template')]");
		//check the pop up opened
		String saveTemplateButton = "//button[@id='btn-property-save']";
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(By.xpath(saveTemplateButton)));
		
		String chooseTemplateDropDown = "//select[@id='select-existing-template']";
		SeleniumActions.seleniumDropdown(driver, chooseTemplateDropDown, oldTemplateName);
	}
	
	public void masterCreateTemplate(String templateName, String via, String type, String isExtendedDesktop, 
			String isUSBR, String isAudio, String isPersistent, String isViewOnly, WebDriver driver) throws InterruptedException {
		addConnectionTemplate(driver, via, templateName);
		addTemplateChooseConnectionType(driver, type);
		
		if(via.equals("broker")) {
			addTemplateDomainName(driver, "blackbox");
			addTemplateLoadBalanceInfo(driver, "loadbalance");
		}
		
		if(isExtendedDesktop.equals("true")) 
			addTemplateEnableExtendedDesktop(driver);
		
		if(isUSBR.equals("true"))
			addTemplateEnableUSBRedirection(driver);
		
		if(isAudio.equals("true"))
			addTemplateEnableAudio(driver);
		
		if(isPersistent.equals("true") && !via.equals("vm")) {
			addTemplateEnablePersistenConnection(driver);	
		}else if(isPersistent.equals("true") && via.equals("vm")) {
			addTemplateEnableNLA(driver);
		}
		
		if(isViewOnly.equals("true"))
			addTemplateViewOnlyConnection(driver);
			
		
		SeleniumActions.seleniumClick(driver, Connections.addTemplateSavebtn);
	}
	
	public void createPairedConnection(WebDriver driver, String connectionName, int numberOfSources, 
			String ip1, String ip2) throws InterruptedException {
		navigateToConnectionsManage(driver);
		log.info("Creating paired connection with following details");
		log.info("Connection Name:" + connectionName);
		log.info("Number of sources:" + numberOfSources);
		log.info("Ip address 1:" + ip1 + " Ip address 2:" + ip2);
		
		SeleniumActions.seleniumClick(driver, Connections.btnAddConnection);
		
		
		
	}
	
	
	@SuppressWarnings("null")
	public void createMasterConnection(String connectionName, String connectVia, String type, String isExtended, 
			String isUSBR, String isAudio, String isPersistent, String isViewOnly,String ip, WebDriver driver, String zone, boolean isLossless) throws InterruptedException {
		addConnection(driver, connectionName, "no"); // connection name, user template
		if(zone != null ) {
			if(!zone.equals(""))
				SeleniumActions.seleniumDropdown(driver, Connections.getZone(), zone);
		}
		if(isLossless) 
			SeleniumActions.seleniumClick(driver, Connections.getLosslessBtn());
		
		connectionInfo(driver, connectVia, "user","user", ip); // connection via, name, host ip
		if(connectVia.equals("tx")) {
			chooseCoonectionType(driver, type); // connection type
		}else if(connectVia.equals("broker")) {
			domainName(driver, "blackbox");
			loadBalanceInfo(driver, "loadbalance");
		}
		if(isExtended.equals("true"))
			enableExtendedDesktop(driver);
		if(isUSBR.equals("true"))
			enableUSBRedirection(driver);
		if(isAudio.equals("true"))
			enableAudio(driver);
		if(isPersistent.equals("true"))
			enablePersistenConnection(driver);
		if(isViewOnly.equals("true"))
			enableViewOnlyConnection(driver);
		
		propertyInfoClickNext(driver);
		saveConnection(driver, connectionName, zone, isLossless); // Connection name to assert
	}
	
	public void createMasterConnection(String connectionName, String connectVia, String type, String isExtended, 
			String isUSBR, String isAudio, String isPersistent, String isViewOnly,String ip, WebDriver driver, String zone) throws InterruptedException {
		addConnection(driver, connectionName, "no"); // connection name, user template
		SeleniumActions.seleniumDropdown(driver, Connections.getZone(), zone);
		connectionInfo(driver, connectVia, "user","user", ip); // connection via, name, host ip
		if(connectVia.equals("tx")) {
			chooseCoonectionType(driver, type); // connection type
		}else if(connectVia.equals("broker")) {
			domainName(driver, "blackbox");
			loadBalanceInfo(driver, "loadbalance");
		}
		if(isExtended.equals("true"))
			enableExtendedDesktop(driver);
		if(isUSBR.equals("true"))
			enableUSBRedirection(driver);
		if(isAudio.equals("true"))
			enableAudio(driver);
		if(isPersistent.equals("true"))
			enablePersistenConnection(driver);
		if(isViewOnly.equals("true"))
			enableViewOnlyConnection(driver);
		
		propertyInfoClickNext(driver);
		saveConnection(driver, connectionName, zone); // Connection name to assert
	}
	
	public void createMasterConnection(String connectionName, String connectVia, String type, String isExtended, 
			String isUSBR, String isAudio, String isPersistent, String isViewOnly,String ip, WebDriver driver) throws InterruptedException {
		addConnection(driver, connectionName, "no"); // connection name, user template
		connectionInfo(driver, connectVia, "user","user", ip); // connection via, name, host ip
		if(connectVia.equals("tx")) {
			chooseCoonectionType(driver, type); // connection type
		}else if(connectVia.equals("broker")) {
			domainName(driver, "blackbox");
			loadBalanceInfo(driver, "loadbalance");
		}
		if(isExtended.equals("true"))
			enableExtendedDesktop(driver);
		if(isUSBR.equals("true"))
			enableUSBRedirection(driver);
		if(isAudio.equals("true"))
			enableAudio(driver);
		if(isPersistent.equals("true"))
			enablePersistenConnection(driver);
		if(isViewOnly.equals("true"))
			enableViewOnlyConnection(driver);
		
		propertyInfoClickNext(driver);
		saveConnection(driver, connectionName); // Connection name to assert
		
	}
	
	/**
	 * Deletes a preset from connections > viewer page
	 * Navigates to connections > viewer and deletes the preset with the passed in name
	 * 
	 * @param driver webdriver to drive the browser interaction
	 * @param name name of preset to delete
	 * 
	 * @throws InterruptedException
	 */
	public void deletePreset(WebDriver driver, String name) throws InterruptedException {
		navigateToConnectionViewer(driver);
		Connections.managePresetsBtn(driver).click();
		timer(driver);
		Connections.searchAvailablePresets(driver).sendKeys(name);
		Connections.deletePresetBtn(driver, name).click();
		timer(driver);
	}
	/**
	 * Edits a preset name in connections > viewer
	 * 
	 * Navigates to connections > viewer and edits the name of a preset
	 * 
	 * @param driver webdriver to drive the browser interaction
	 * @param oldName original preset name
	 * @param newName new preset name 
	 * 
	 * @throws InterruptedException
	 */
	public void editPresetName(WebDriver driver, String oldName, String newName) throws InterruptedException {
		navigateToConnectionViewer(driver);
		Connections.managePresetsBtn(driver).click();
		timer(driver);
		Connections.searchAvailablePresets(driver).sendKeys(oldName);
		Connections.editPresetBtn(driver, oldName).click();
		timer(driver);
		Connections.editPresetEditSourcesNextBtn(driver).click();
		timer(driver);
		Connections.editPresetEditDestinationsNextBtn(driver).click();
		timer(driver);
		SeleniumActions.seleniumSendKeysClear(driver, Connections.getCreatePresetNameTextBoxXpath());
		SeleniumActions.seleniumSendKeys(driver, Connections.getCreatePresetNameTextBoxXpath(), newName);
		Connections.createPresetCompleteBtn(driver).click();
	}
	
	/**
	 * Creates a new preset in connections > viewer
	 * 
	 * Navigates to connections > viewer and creates a new preset with passed in 
	 * connection sources and destinations
	 * 
	 * @param driver webdriver to drive the browser interaction
	 * @param sourceList array containing the names of all the sources (connections) to add to preset
	 * @param destinationList array containing the names of all the destinations (receivers) to add to the preset
	 * 
	 * @param presetName name of the preset
	 * @param isPartial boolean to indicate if the preset is partial or full. true for partial
	 * 
	 * @throws InterruptedException
	 */
	public void createPreset(WebDriver driver, String[] sourceList, String[] destinationList, String presetName, boolean isPartial) throws InterruptedException {
		log.info("Attempting to create preset with name " + presetName);
		navigateToConnectionViewer(driver);
		timer(driver);
		Connections.managePresetsBtn(driver).click();
		new WebDriverWait(driver, 60).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h4[contains(.,'Manage Presets')]")));
		Connections.createCustomPresetBtn(driver).click();
		for(String s : sourceList) {
			log.info("Attempting to add source " + s);
			Connections.presetSourcesSearchBox(driver).sendKeys(s);
			SeleniumActions.seleniumClick(driver, Connections.firstItemInCreatePresetSourceList(driver, s));
			log.info("Successfully added source " + s);
			Connections.presetSourcesSearchBox(driver).clear();
		}
		//Thread.sleep(5000);
		Connections.presetSelectSourceNextBtn(driver).click();
	//	Thread.sleep(5000);
		for(String d : destinationList) {
			log.info("Attempting to add destingation " + d);
			Connections.presetSelectDestinationSearchBox(driver).sendKeys(d);
			SeleniumActions.seleniumClick(driver, Connections.firstItemInCreatePresetDestinationList(driver, d));
			Connections.presetSelectDestinationSearchBox(driver).clear();
			log.info("Successfully added destination " + d);
		}
		//Thread.sleep(5000);
		Connections.presetDestinationNextBtn(driver).click();
		SeleniumActions.seleniumSendKeys(driver, Connections.getCreatePresetNameTextBoxXpath(), presetName);
		if(isPartial) {
			Connections.createPresetTypeDowndown(driver, "Partial");
		}else {
			Connections.createPresetTypeDowndown(driver, "Full");
		}
		//Thread.sleep(5000);
		Connections.createPresetCompleteBtn(driver).click();
		log.info("Successfully created preset with name " + presetName);
		
	}
	/**
	 * Breaks a connection in connections > viewer
	 * 
	 * Navigates to connections > viewer and clicks the x on the source with 
	 * the passed in name which breaks the connections
	 * 
	 * @param driver webdriver to drive the browser interaction
	 * @param name name of the source (connection) to break
	 * 
	 * @throws InterruptedException
	 */
	public void breakConnection(WebDriver driver, String name) throws InterruptedException {
		log.info("Attempting to break connection with name " + name);
		navigateToConnectionViewer(driver);
		timer(driver);
		Connections.breakConnection(driver, name).click();
		timer(driver);
		try {
			boolean isBroken = Connections.singleSourceDestinationCheck(driver, name).isDisplayed();
			if(isBroken) {
				driver.navigate().refresh();
				timer(driver);
				Connections.breakConnection(driver, name).click();
				timer(driver);
			}
			
		}catch(Exception e) {
			log.info("connection broken");
		}
		log.info("Successfully broken connection " + name);
	}
	/**
	 * Saves the current connections in connections > viewer to a  preset
	 * 
	 * Navigates to connections > viewer and saves the current connections as a preset
	 * with passed in name. Boolean indicates if the preset is partial or full
	 * 
	 * @param driver webdriver to drive the browser interaction
	 * @param name name of the preset to create
	 * @param isPartial boolean to indicate if the preset is full or partial. True for partial
	 * 
	 * @throws InterruptedException
	 */
	public void saveSnapshot(WebDriver driver, String name, boolean isPartial) throws InterruptedException {
		log.info("Saving snapshot " + name);
		navigateToConnectionViewer(driver);
		SeleniumActions.seleniumClick(driver, Connections.getSnapshotBtn());
		timer(driver);
		Connections.snapshotNameInput(driver).sendKeys(name);
		if(isPartial) {
			Connections.snapshotTypeDropdown(driver, "Partial");
		}else {
			Connections.snapshotTypeDropdown(driver, "Full");
		}
		timer(driver);
		Connections.saveSnapshotBtn(driver).click();
		timer(driver);

	}
	/**
	 * Navigates from anywhere in boxilla to connections > viewer
	 * 
	 * @param driver webdriver to drive the browser interaction
	 * 
	 * @throws InterruptedException
	 */
	public void navigateToConnectionViewer(WebDriver driver) throws InterruptedException {
		log.info("Navigating to connections > viewer");
		int counter = 0;
		while(counter < 5) {
			try {
				SeleniumActions.seleniumClick(driver, Landingpage.connectionsTab);
				log.info("Connections tab has been clicked. Checking for connections viewer link to be available");
				new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(Landingpage.connectionsViewer(driver)));
				log.info("Connections viewer link is available");
				counter = 6;
			}catch(Exception e) {
				log.info("Problem with clicking the connections tab button. Maybe tab did not expand. Printing stacktrace and retrying");
				e.printStackTrace();
				counter++;
			}
		}

		Landingpage.connectionsViewer(driver).click();
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(Connections.makeConnectionBtn(driver)));
		log.info("Successfully navigated to connections viewer page");
	}
	
	/**
	 * Adds a private destination to a source in connections > viewer
	 * 
	 * @param driver webdriver to drive the browser interaction
	 * @param connectionName name of the connection to add destination to
	 * @param destination name of the destination to add
	 * 
	 * @throws InterruptedException
	 */
	public void addPrivateDestination(WebDriver driver, String connectionName, String destination ) throws InterruptedException {
		log.info("Adding private destination. Connection: " + connectionName + " Destination: " + destination);
		navigateToConnectionViewer(driver);
		//sometime the button is highlighted but not clicked. Refresh the page and try again
		int counter = 0;
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(By.xpath(Connections.destination(driver, connectionName))));
		while(counter < 5) {
			try {
			SeleniumActions.seleniumClick(driver, Connections.destination(driver, connectionName));
			counter = 6;
			}catch(Exception e) {
				log.info("Button sometimes gets stuck. Refreshing page and retrying");
				counter++;
				
			}
		}
		timer(driver);
		Connections.privateConnectionDropDown(driver, destination);
		Connections.privateConnectionsActivateBtn(driver).click();
	}
	/**
	 * Adds one or more shared destinations to a source in connections > viewer
	 * 
	 * @param driver webdriver to drive the browser interaction
	 * @param connectionName name of the connection to add destination to
	 * @param destination array of names of the destinations to add
	 * 
	 * @throws InterruptedException
	 */
	public void addSharedDestination(WebDriver driver, String connectionName, String[] destinations) throws InterruptedException {
		log.info("Adding shared destination. Connectoin: " + connectionName);
		navigateToConnectionViewer(driver);
		timer(driver);
		SeleniumActions.seleniumClick(driver, Connections.destination(driver, connectionName));
		timer(driver);
		for(String s : destinations) {
			log.info("Destination : " + s);
			Connections.sharedDestinationSearchBox(driver).sendKeys(s);
			timer(driver);
			Connections.firstItemInDestination(driver, s).click();
			timer(driver);
			Connections.sharedDestinationSearchBox(driver).clear();
			timer(driver);
		}
		SeleniumActions.seleniumClick(driver, Connections.getActivateSelectedDestinationXpath());	
		timer(driver);
	}
	
	
	/**
	 * Add one or more sources in connections > viewer
	 * 
	 * @param driver webdriver to drive the browser interaction
	 * @param connections array holding the names of the connections to add
	 * 
	 * @throws InterruptedException
	 */
	public void addSources(WebDriver driver, String[] connections) throws InterruptedException {
		log.info("Adding Sources.");
		navigateToConnectionViewer(driver);
		//for some reason sometimes these buttons get stuck and do not click in selenium to refresh page and retry
		int counter = 0;
		while(counter < 5) {
			try {
				Connections.makeConnectionBtn(driver).click();
				new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(Connections.activateSourcesSource(driver)));
				log.info("Make connections successfully clicked");
			}catch(Exception e) {
				log.info("make connection button did not click. Refreshing page and retrying");
				//e.printStackTrace();
				counter ++;
			}
		}
		for(String s : connections) {
			log.info("Source Name: " + s);
			Connections.searchSources(driver).sendKeys(s);
			new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(By.xpath(Connections.firstItemInSourceList(driver, s))));
			SeleniumActions.seleniumClick(driver, Connections.firstItemInSourceList(driver, s));
			Connections.searchSources(driver).clear();
		}
		log.info("Clicking activate Sources");
		Connections.activateSourcesSource (driver).click();
		
	}

	/**
	 * Navigates to the connections > manage page from anywhere in boxilla
	 * 
	 * @param driver webdriver to drive the browser interaction
	 * 
	 * @throws InterruptedException
	 */
	public void navigateToConnectionsManage(WebDriver driver) throws InterruptedException {
		SeleniumActions.seleniumClick(driver, Landingpage.connectionsTab);
		//Landingpage.connectionsTab(driver).click();
		log.info("Navigate to Connections > Manage : Connections Tab clicked");
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(By.xpath( Landingpage.connectionsManage)));
		//Landingpage.connectionsManage(driver).click();
		SeleniumActions.seleniumClick(driver, Landingpage.connectionsManage);
		log.info("Navigate to Connections > Manage : Connections > Manage Tab clicked");
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(driver.findElement(By.xpath(Connections.btnAddConnection))));
		log.info("Navigate to Connections > Manage : Title Assertion Executed - Success");
	}

	/**
	 * Creates a connection in connections > manage
	 * 
	 * @param driver webdriver to drive the browser interaction
	 * @param connectionName name of the connection to create
	 * @param useTemplate Indicates if connection should be created from a template. yes or no
	 * 
	 * @throws InterruptedException
	 */
	public void addConnection(WebDriver driver, String connectionName, String useTemplate) throws InterruptedException {
		navigateToConnectionsManage(driver);
		timer(driver);
		SeleniumActions.seleniumClick(driver, Connections.btnAddConnection);
		//Connections.btnAddConnection(driver).click();
		log.info("Adding Connection : Add Connection Button Clicked");
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(Connections.useTemplateNo(driver)));
		if (useTemplate.equalsIgnoreCase("no")) {
			SeleniumActions.exectuteJavaScriptClick(driver, Connections.useTemplateNo(driver));
			//SeleniumActions.seleniumClick(driver, Connections.useTemplateNo());
			//Connections.useTemplateNo(driver).click();
			log.info("Adding Connection : Use Template No Selected");
		} else if (useTemplate.equalsIgnoreCase("yes")) {
			SeleniumActions.exectuteJavaScriptClick(driver, Connections.useTemplateYes(driver));
			//SeleniumActions.seleniumClick(driver, Connections.useTemplateYes());
			//Connections.useTemplateYes(driver).click();
			log.info("Adding Connection : Use Template Yes Selected");
		}
		timer(driver);
		SeleniumActions.seleniumSendKeys(driver, Connections.connectionName, connectionName);
		if(StartupTestCase.is4kCon) {
			log.info("This is a connection for 4k device so setting compression mode to lossless");
			WebElement e = driver.findElement(By.xpath(Connections.getLosslessBtn()));
			SeleniumActions.exectuteJavaScriptClick(driver, e);
			
		}
		//Connections.connectionName(driver).sendKeys(connectionName);
		log.info("Adding Connection : Connection Name Entered");
	}

	
	public void connectionInfo(WebDriver driver, String via, String username, String password, String hostIP)
			throws InterruptedException { // use via string from : tx , vm , vmpool, broker
		timer(driver);
		if (via.equalsIgnoreCase("tx")) {
			((JavascriptExecutor) driver).executeScript("arguments[0].checked = true;",
					SeleniumActions.getElement(driver, Connections.connectionViaTX));
			log.info("Connection Via " + via + " selected");
			timer(driver);
			SeleniumActions.seleniumSendKeys(driver, Connections.hostName, hostIP);
			//Connections.hostName(driver).sendKeys(hostIP);
			log.info("Adding Connection : Host Name Entered");
		} else if (via.equalsIgnoreCase("vm")) {
			SeleniumActions.seleniumClick(driver, Connections.connectionViaVM);
			//Connections.connectionViaVM(driver).click();
			log.info("Connection Via " + via + " selected");
			timer(driver);
			SeleniumActions.seleniumSendKeys(driver, Connections.hostName, hostIP);
			//Connections.hostName(driver).sendKeys(hostIP);
			log.info("Adding Connection : Host Name Entered");
			timer(driver);
			SeleniumActions.seleniumSendKeys(driver, Connections.usernameTextbox, username);
			//Connections.usernameTextbox(driver).sendKeys(username);
			log.info("Adding Connection : Username Entered");
			timer(driver);
			SeleniumActions.seleniumSendKeys(driver, Connections.passwordTextbox, password);
			//Connections.passwordTextbox(driver).sendKeys(username);
			log.info("Adding Connection : Password Entered");
		} else if (via.equalsIgnoreCase("vmpool")) {
			SeleniumActions.seleniumClick(driver, Connections.connectionViaVMPool);
			//Connections.connectionViaVMPool(driver).click();
			log.info("Connection Via " + via + " selected");
		} else if (via.equalsIgnoreCase("broker")) {
			SeleniumActions.seleniumClick(driver, Connections.connectionViaBroker);
			//Connections.connectionViaBroker(driver).click();
			log.info("Connection Via " + via + " selected");
			timer(driver);
			SeleniumActions.seleniumSendKeys(driver, Connections.hostName, hostIP);
			//Connections.hostName(driver).sendKeys(hostIP);
			log.info("Adding Connection : Host Name Entered");
			timer(driver);
			SeleniumActions.seleniumSendKeys(driver, Connections.usernameTextbox, username);
			//Connections.usernameTextbox(driver).sendKeys(username);
			log.info("Adding Connection : Username Entered");
			timer(driver);
			SeleniumActions.seleniumSendKeys(driver, Connections.passwordTextbox, username);
			//Connections.passwordTextbox(driver).sendKeys(username);
			log.info("Adding Connection : Password Entered");
		}
		else if (via.equalsIgnoreCase("horizon")) {
			SeleniumActions.exectuteJavaScriptClick(driver, Connections.getVmHorizonViewBtn(driver));
			//SeleniumActions.seleniumClick(driver, Connections.getVmHorizonViewBtn());
			log.info("Connection Via " + via + " selected");
			timer(driver);
			SeleniumActions.seleniumSendKeys(driver, Connections.hostName, hostIP);
			//Connections.hostName(driver).sendKeys(hostIP);
			log.info("Adding Connection : Host Name Entered");
			timer(driver);
			SeleniumActions.seleniumSendKeys(driver, Connections.usernameTextbox, username);
			//Connections.usernameTextbox(driver).sendKeys(username);
			log.info("Adding Connection : Username Entered");
			timer(driver);
			SeleniumActions.seleniumSendKeys(driver, Connections.passwordTextbox, username);
			//Connections.passwordTextbox(driver).sendKeys(username);
			log.info("Adding Connection : Password Entered");
		} 
		else {
			log.info("Error in Selecting Connect Via");
			throw new SkipException("There is issue with the selecting connection");
		}
		timer(driver);
		SeleniumActions.seleniumClick(driver, Connections.btnNext);
		//Connections.btnNext(driver).click();
		log.info("Adding Connection - Stage 1 Complete");
	}
	
	

	// Choose connection Private or Shared - Only for Connection via TX
	/**
	 * Chooses a connection, private or shared for TX connections
	 * 
	 * @param driver webdriver to drive the browser interaction
	 * @param connectionType shared for shared connection, private for a private one
	 * 
	 * @throws InterruptedException
	 */
	public void chooseCoonectionType(WebDriver driver, String connectionType) throws InterruptedException {
		if (connectionType.equalsIgnoreCase("private")) {
			timer(driver);
			SeleniumActions.seleniumClick(driver, Connections.connectionTypePrivate);
			//Connections.connectionTypePrivate(driver).click();
			log.info("Adding Connection : Connection Type selected to Private");
		} else if (connectionType.equalsIgnoreCase("shared")) {
			timer(driver);
			SeleniumActions.seleniumClick(driver, Connections.connectionTypeShared);
			//Connections.connectionTypeShared(driver).click();
			log.info("Adding Connection: Connection Type selected to Shared");
		}
	}

	// Enable Extended Desktop
	/**
	 * When creating a connection in connections > manage, sets extended desktop to on
	 * 
	 * @param driver webdriver to drive the browser interaction
	 * 
	 * @throws InterruptedException
	 */
	public void enableExtendedDesktop(WebDriver driver) throws InterruptedException {
		timer(driver);
		//SeleniumActions.seleniumClick(driver, Connections.extendedDesktop());
		//SeleniumActions.seleniumClick(driver, Connections.extendedDesktop());
		SeleniumActions.exectuteJavaScriptClick(driver, Connections.extendedDesktop(driver));
//		WebElement e = driver.findElement(By.xpath(Connections.extendedDesktop()));
//		JavascriptExecutor js = (JavascriptExecutor) driver;
//		js.executeScript("arguments[0].click()", e);
//		//Connections.extendedDesktop(driver).click();
		log.info("Adding Connection - Extended Desktop ON");

	}

	// Enable USB Redirection
	/**
	 * 	When creating a connection in connections > manage, sets USB redirection to on
	 * 
	 * @param driver webdriver to drive the browser interaction
	 * 
	 * @throws InterruptedException
	 */
	public void enableUSBRedirection(WebDriver driver) throws InterruptedException {
		timer(driver);
		SeleniumActions.exectuteJavaScriptClick(driver, Connections.usbRedirection(driver));
		//Connections.usbRedirection(driver).click();
		log.info("Adding Connection - USB Redirection ON");
	}

	// Enable Audio
	/**
	 * When creating a connection in connections > manage, sets audio input to on
	 * 
	 * @param driver webdriver to drive the browser interaction
	 * 
	 * @throws InterruptedException
	 */
	public void enableAudio(WebDriver driver) throws InterruptedException {
		timer(driver);
		SeleniumActions.exectuteJavaScriptClick(driver, Connections.audio(driver));
		//Connections.audio(driver).click();
		log.info("Adding Connection - Audio Input ON");
	}

	// Enable Persistent Connection
	/**
	 * Wehn creating a connection in connections > manage, sets persistent connection to on
	 * 
	 * @param driver webdriver to drive the browser interaction
	 * 
	 * @throws InterruptedException
	 */
	public void enablePersistenConnection(WebDriver driver) throws InterruptedException {
		timer(driver);
		SeleniumActions.exectuteJavaScriptClick(driver, Connections.persistentConnection(driver));
		//Connections.persistentConnection(driver).click();
		log.info("Adding Connection - Persistent Connection ON");
	}

	public void enableViewOnlyConnection(WebDriver driver) throws InterruptedException {
		timer(driver);
		SeleniumActions.exectuteJavaScriptClick(driver, Connections.viewOnlyConnection(driver));
		//Connections.viewOnlyConnection(driver).click();
		log.info("Add Connection - View Only Connection");
	}
	// Enable NLA
 	/**
	 * When creating a connection via VM in connections > manage, sets NLA to on
	 * 
	 * @param driver webdriver to drive the browser interaction
	 * 
	 * @throws InterruptedException
	 */
	public void enableNLA(WebDriver driver) throws InterruptedException {
		timer(driver);
		SeleniumActions.exectuteJavaScriptClick(driver, Connections.NLA(driver));
		//Connections.NLA(driver).click();
		log.info("Adding Connection - NLA ON");
	}

	// Enter Domain name
	/**
	 * When creating a connection via VM in connections > manage, enters the domain name in the field
	 * 
	 * @param driver webdriver to drive the browser interaction
	 * @param domainName the name of the domain
	 * 
	 * @throws InterruptedException
	 */
	public void domainName(WebDriver driver, String domainName) throws InterruptedException {
		timer(driver);
		SeleniumActions.seleniumSendKeys(driver, Connections.domainTextbox, domainName);
		//Connections.domainTextbox(driver).sendKeys(domainName);
		log.info("Adding Connection - Domain name Entered");
	}

	// Enter Load Balance Info
	/**
	 * When creating a connection via Broker in connections > manage, enters load balance info in the field
	 * 
	 * @param driver webdriver to drive the browser interaction
	 * @param loadBalanceInfo the load balance info
	 * 
	 * @throws InterruptedException
	 */
	public void loadBalanceInfo(WebDriver driver, String loadBalanceInfo) throws InterruptedException {
		timer(driver);
		SeleniumActions.seleniumSendKeys(driver, Connections.loadBalanceInfo, loadBalanceInfo);
		//Connections.loadBalanceInfo(driver).sendKeys(loadBalanceInfo);
		log.info("Adding Connection - Load Balance Info Entered");
	}

	// Click Next on Property information section : Stage 2 - Add Connections
	/**
	 * Clicks next when creating the initial connection
	 * 
	 * @param driver
	 * 
	 * @throws InterruptedException
	 */
	public void propertyInfoClickNext(WebDriver driver) throws InterruptedException {
		timer(driver);
		SeleniumActions.seleniumClick(driver, Connections.btnNext);
		//Connections.btnNext(driver).click();
		log.info("Adding Connection - Stage 2 completed");
	}
	public void propertyInfoClickNextWithTemplate(WebDriver driver,String templateName, VIA via) throws InterruptedException {
		timer(driver);
		SeleniumActions.seleniumDropdown(driver, Connections.getPropertyTemplate(via), templateName);
		SeleniumActions.seleniumClick(driver, Connections.btnNext);
		//Connections.btnNext(driver).click();
		log.info("Adding Connection - Stage 2 completed");
	}

	/**
	 * Checks the connections > manage table for a specific connection.
	 * Throws an assert error if connection is not found
	 * 
	 * @param driver webdriver to drive the browser interaction
	 * @param connectionName the name of the connection to search for 
	 * 
	 * @throws InterruptedException
	 */
	public void checkConnectionExists(WebDriver driver, String connectionName) throws InterruptedException {
		SeleniumActions.seleniumClick(driver, Landingpage.connectionsTab);
		//Landingpage.connectionsTab(driver).click();
		timer(driver);
		//Landingpage.connectionsManage(driver).click();
		SeleniumActions.seleniumClick(driver, Landingpage.connectionsManage);
		SeleniumActions.seleniumSendKeys(driver, Connections.searchTextbox, connectionName);
		//Connections.searchTextbox(driver).sendKeys(connectionName);
		timer(driver);
		String connectionTableText = SeleniumActions.seleniumGetText(driver, Connections.connectionTable);
		//String connectionTableText = Connections.connectionTable(driver).getText();
		Assert.assertTrue(connectionTableText.contains(connectionName),
				"Connection table text did not contain: " + connectionName + ", actual text: " + connectionTableText); // Asserting connection name present in connection table
	}
	
	// Save (Add) Connection
	/**
	 * Saves a connection in connections > manage 
	 * 
	 * @param driver  webdriver to drive the browser interaction
	 * @param connectionName the name of the connection to save
	 * 
	 * @throws InterruptedException
	 */
	public String saveConnection(WebDriver driver, String connectionName) throws InterruptedException {
		timer(driver);
		SeleniumActions.seleniumClick(driver, Connections.btnSave);
		//Connections.btnSave(driver).click();
		log.info("Adding Connection - Save button Clicked.. Asserting connection");
		timer(driver);
		String toastMessage = Users.notificationMessage(driver).getText();
		SeleniumActions.takeScreenshot(driver);
		Assert.assertTrue( // Asserting toast message
				toastMessage.contains("Successfully created"),
				"Toast Message did not contain: Successfully created, actual text: " + toastMessage);
		SeleniumActions.refreshPage(driver);
		SeleniumActions.seleniumSendKeys(driver, Connections.searchTextbox, connectionName);
		//Connections.searchTextbox(driver).sendKeys(connectionName);
		timer(driver);
		String connectionTableText = SeleniumActions.seleniumGetText(driver, Connections.connectionTable);
		//String connectionTableText = Connections.connectionTable(driver).getText();
		Assert.assertTrue(connectionTableText.contains(connectionName),
				"Connection table text did not contain: " + connectionName + ", actual text: " + connectionTableText); // Asserting connection name present in connection table
		log.info("Adding Connection - Assertion Completed. Successfully Created Connection");
		return connectionTableText;
	}
	
	public String saveConnection(WebDriver driver, String connectionName, String zoneName) throws InterruptedException {
		timer(driver);
		SeleniumActions.seleniumClick(driver, Connections.btnSave);
		//Connections.btnSave(driver).click();
		log.info("Adding Connection - Save button Clicked.. Asserting connection");
		timer(driver);
		String toastMessage = Users.notificationMessage(driver).getText();
		SeleniumActions.takeScreenshot(driver);
		Assert.assertTrue( // Asserting toast message
				toastMessage.contains("Successfully created"),
				"Toast Message did not contain: Successfully created, actual text: " + toastMessage);
		SeleniumActions.refreshPage(driver);
		SeleniumActions.seleniumSendKeys(driver, Connections.searchTextbox, connectionName);
		//Connections.searchTextbox(driver).sendKeys(connectionName);
		timer(driver);
		String connectionTableText = SeleniumActions.seleniumGetText(driver, Connections.connectionTable);
		//String connectionTableText = Connections.connectionTable(driver).getText();
		Assert.assertTrue(connectionTableText.contains(connectionName),
				"Connection table text did not contain: " + connectionName + ", actual text: " + connectionTableText); // Asserting connection name present in connection table
		
		if(zoneName != null || !zoneName.equals(""))  {
			Assert.assertTrue(connectionTableText.contains(zoneName), "Zone name was not in table data");
		}
		log.info("Adding Connection - Assertion Completed. Successfully Created Connection");
		return connectionTableText;
	}
	public String saveConnection(WebDriver driver, String connectionName, String zoneName, boolean isLossless) throws InterruptedException {
		timer(driver);
		SeleniumActions.seleniumClick(driver, Connections.btnSave);
		//Connections.btnSave(driver).click();
		log.info("Adding Connection - Save button Clicked.. Asserting connection");
		timer(driver);
		String toastMessage = Users.notificationMessage(driver).getText();
		SeleniumActions.takeScreenshot(driver);
		Assert.assertTrue( // Asserting toast message
				toastMessage.contains("Successfully created"),
				"Toast Message did not contain: Successfully created, actual text: " + toastMessage);
		SeleniumActions.refreshPage(driver);
		SeleniumActions.seleniumSendKeys(driver, Connections.searchTextbox, connectionName);
		//Connections.searchTextbox(driver).sendKeys(connectionName);
		timer(driver);
		String connectionTableText = SeleniumActions.seleniumGetText(driver, Connections.connectionTable);
		//String connectionTableText = Connections.connectionTable(driver).getText();
		Assert.assertTrue(connectionTableText.contains(connectionName),
				"Connection table text did not contain: " + connectionName + ", actual text: " + connectionTableText); // Asserting connection name present in connection table
		
		if(zoneName != null)  {
			if( !zoneName.equals(""))
				Assert.assertTrue(connectionTableText.contains(zoneName), "Zone name was not in table data");
		}
		
		if(isLossless) {
			Assert.assertTrue(connectionTableText.contains("Lossless"), "Connection was not lossless");
		}else {
			Assert.assertTrue(connectionTableText.contains("Optimized"), "Connection was not optimized");
		}
		log.info("Adding Connection - Assertion Completed. Successfully Created Connection");
		return connectionTableText;
	}

	// Add Connection Template - Enable Extended Desktop
	/**
	 * When creating a connection template in connections > manage, sets extended desktop to on
	 * 
	 * @param driver webdriver to drive the browser interaction
	 * 
	 * @throws InterruptedException
	 */
	public void addTemplateEnableExtendedDesktop(WebDriver driver) throws InterruptedException {
		timer(driver);
		SeleniumActions.exectuteJavaScriptClick(driver, Connections.addTemplateExtendedDesktop(driver));
		//Connections.addTemplateExtendedDesktop(driver).click();
		log.info("Adding Connection - Extended Desktop ON");
	}

	// Add Connection Template - Enable USB Redirection
	/**
	 * When creating a connection template in connections > manage, sets USB redirection to on
	 * 
	 * @param driverwebdriver to drive the browser interaction
	 * 
	 * @throws InterruptedException
	 */
	public void addTemplateEnableUSBRedirection(WebDriver driver) throws InterruptedException {
		timer(driver);
		SeleniumActions.exectuteJavaScriptClick(driver, Connections.addTemplateUsbRedirection(driver));
		//Connections.addTemplateUsbRedirection(driver).click();
		log.info("Adding Connection - USB Redirection ON");
	}

	// Add Connection Template - Enable Audio
	/**
	 * When creating a connection template in connections > manage, sets Audio input to on
	 * 
	 * @param driver webdriver to drive the browser interaction
	 * 
	 * @throws InterruptedException
	 */
	public void addTemplateEnableAudio(WebDriver driver) throws InterruptedException {
		timer(driver);
		SeleniumActions.exectuteJavaScriptClick(driver, Connections.addTemplateAudio(driver));
		//Connections.addTemplateAudio(driver).click();
		log.info("Adding Connection - Audio Input ON");
	}

	// Add Connection Template - Enable Persistent Connection
	/**
	 * When creating a connection template in connections > manage, sets Persistent connection to on
	 * 
	 * @param driver webdriver to drive the browser interaction
	 * 
	 * @throws InterruptedException
	 */
	public void addTemplateEnablePersistenConnection(WebDriver driver) throws InterruptedException {
		timer(driver);
		SeleniumActions.exectuteJavaScriptClick(driver, Connections.addTemplatePersistentConnection(driver));
		//Connections.addTemplatePersistentConnection(driver).click();
		log.info("Adding Connection - Persistent Connection ON");
	}
	public void addTemplateViewOnlyConnection(WebDriver driver) throws InterruptedException {
		timer(driver);
		SeleniumActions.exectuteJavaScriptClick(driver, Connections.addTemplateViewOnly(driver));
		//Connections.addTemplateViewOnly(driver).click();
	}

	// Add Connection Template - Enable NLA
	/**
	 * When creating a connection template in connections > manage, sets NLA to on
	 * @param driver
	 * @throws InterruptedException
	 */
	public void addTemplateEnableNLA(WebDriver driver) throws InterruptedException {
		timer(driver);
		SeleniumActions.exectuteJavaScriptClick(driver, Connections.addTemplateNLA(driver));
		//Connections.addTemplateNLA(driver).click();
		log.info("Adding Connection - NLA ON");
	}
	
	//using strings for dataprovider
	public void addPairConnectionTemplate(WebDriver driver, String templateName, String shared, String targets,Connections.Orientation orientation,
			String audio, String audio2, String persistent, String viewOnly) throws InterruptedException {
		navigateToConnectionsManage(driver);
		log.info("Creating paired connection template");
		SeleniumActions.seleniumClick(driver, Connections.btnAddConnectionTemplate);
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(By.xpath(Connections.addTemplateSavebtn)));
		SeleniumActions.seleniumClick(driver, Connections.getTemplateTxPairButton());
		SeleniumActions.seleniumSendKeys(driver, Connections.templateName, templateName);
		if(targets.equals("1")) {
			log.info("Do nothing. 1 target already selected");
			//SeleniumActions.seleniumClick(driver, Connections.getTemplatePairedTarget1());
		}else {
			WebElement target2 = driver.findElement(By.xpath(Connections.getTemplatePairedTarget2()));
			SeleniumActions.exectuteJavaScriptClick(driver, target2);
			setDisplayOrientation(driver, orientation, true);
		}
		if(shared.equals("true"))
			SeleniumActions.seleniumClick(driver, Connections.addTemplateShared);
		
		if(audio.equals("true")) {
			addTemplateEnableAudio(driver);
			if(audio2.equals("true")) {
				WebElement ip1Audio = driver.findElement(By.xpath(Connections.getTemplateAudioIp1()));
				SeleniumActions.exectuteJavaScriptClick(driver, ip1Audio);
			}
		}
		if(persistent.equals("true"))
			addTemplateEnablePersistenConnection(driver);
		
		if(viewOnly.equals("true"))
			addTemplateViewOnlyConnection(driver);
		
		SeleniumActions.seleniumClick(driver, Connections.addTemplateSavebtn);
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.templateToastMessage)));
		String toast = SeleniumActions.seleniumGetText(driver, Connections.templateToastMessage);
		Assert.assertTrue(toast.contains("Template successfully created"), "Toast message did not contain 'Template successfully created' , actual:" + toast);
		
		
		
		
		
	}

	// Add Connection Template - use via string from : tx , vm , vmpool, broker
	/**
	 * Creates a connection template in connections > manage
	 *  
	 * @param driver webdriver to drive the browser interaction
	 * @param via connection type. tx, vm, vmpool or broker
	 * @param templateName name of the template
	 * 
	 * @throws InterruptedException
	 */
	public void addConnectionTemplate(WebDriver driver, String via, String templateName) throws InterruptedException {
		navigateToConnectionsManage(driver);
		timer(driver);
		SeleniumActions.seleniumClick(driver, Connections.btnAddConnectionTemplate);
		//Connections.btnAddConnectionTemplate(driver).click();
		log.info("Adding Connection Tempalte : Add Connection Template Button Clicked");
		timer(driver);
		if (via.equalsIgnoreCase("tx")) {
			new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(driver.findElement(By.xpath(Connections.addTemplateTX))));
			WebElement e = driver.findElement(By.xpath(Connections.addTemplateTX));
			SeleniumActions.exectuteJavaScriptClick(driver,  e);
			//SeleniumActions.seleniumClick(driver, Connections.addTemplateTX);
			//Connections.addTemplateTX(driver).click();
			log.info("Connect via " + via + " selected");
		} else if (via.equalsIgnoreCase("broker")) {
			SeleniumActions.exectuteJavaScriptClick(driver,  Connections.addTemplateBroker(driver));
			//SeleniumActions.seleniumClick(driver, Connections.addTemplateBroker);
			//Connections.addTemplateBroker(driver).click();
			log.info("Connect via " + via + " selected");
		} else if (via.equalsIgnoreCase("vm")) {
			SeleniumActions.exectuteJavaScriptClick(driver,  Connections.addTemplateVM(driver));
			//SeleniumActions.seleniumClick(driver, Connections.addTemplateVM);
			//Connections.addTemplateVM(driver).click();
			log.info("Connect via " + via + " selected");
		} else if (via.equalsIgnoreCase("vmpool")) {
			SeleniumActions.exectuteJavaScriptClick(driver,  Connections.addTemplateVMPool(driver));
			//SeleniumActions.seleniumClick(driver, Connections.addTemplateVMPool);
			//Connections.addTemplateVMPool(driver).click();
			log.info("Connect via " + via + " selected");
		}else if (via.equalsIgnoreCase("horizon")) {
			SeleniumActions.exectuteJavaScriptClick(driver,  Connections.getVmHorizonTemplate(driver));
			//SeleniumActions.seleniumClick(driver, Connections.getVmHorizonTemplate());
		}
		else {
			log.info("Adding Template: Error in Selecting Connect Via");
			throw new SkipException("** Adding Connection Template : There is issue with the selecting connect via **");
		}
		timer(driver);
		SeleniumActions.seleniumSendKeys(driver, Connections.templateName, templateName);
		//Connections.templateName(driver).sendKeys(templateName);
		log.info("Adding Connection Template : Template Name entered");
	}

	// Add Template - Choose connection Private or Shared - Only for Connect via TX
	/**
	 * When creating a connection template in connections > manage, sets the connection to private or shared. TX only
	 * 
	 * @param driver webdriver to drive the browser interaction
	 * @param connectionType sets the connection type. shared or private
	 * 
	 * @throws InterruptedException
	 */
	public void addTemplateChooseConnectionType(WebDriver driver, String connectionType) throws InterruptedException {
		if (connectionType.equalsIgnoreCase("private")) {
			timer(driver);
			WebElement e = driver.findElement(By.xpath(Connections.addTemplatePrivate));
			SeleniumActions.exectuteJavaScriptClick(driver, e);
			//SeleniumActions.seleniumClick(driver, Connections.addTemplatePrivate);
			//Connections.addTemplatePrivate(driver).click();
			log.info("Adding Connection : Connection Type selected to Private");
		} else if (connectionType.equalsIgnoreCase("shared")) {
			timer(driver);
			SeleniumActions.seleniumClick(driver, Connections.addTemplateShared);
			//Connections.addTemplateShared(driver).click();
			log.info("Adding Connection: Connection Type selected to Shared");
		}
	}

	// Save (Add) Connection Template
	/**
	 * Saves a connection template
	 * 
	 * @param driver webdriver to drive the browser interaction
	 * 
	 * @throws InterruptedException
	 */
	public void saveConnectionTemplate(WebDriver driver) throws InterruptedException {
		timer(driver);
		SeleniumActions.seleniumClick(driver, Connections.btnAddConnectionTemplate);
		//Connections.btnAddConnectionTemplate(driver).click();
		log.info("Adding Connection Template - Save Template button Clicked.. Asserting connection");
		timer(driver);
		String toastMessage = Users.notificationMessage(driver).getText();
		Assert.assertTrue( // Asserting toast message
				toastMessage.contains("Template successfully created."),
				"Toast message did not contain: Template successfully created, actual text: "+ toastMessage);
		log.info("Adding Connection - Assertion Completed. Successfully Created Connection Template");
	}

	// Add Template - Enter Domain name
	/**
	 * When creating a connection template, enters the domain name
	 * 
	 * @param driver webdriver to drive the browser interaction
	 * @param domainName the name of the domain 
	 * 
	 * @throws InterruptedException
	 */
	public void addTemplateDomainName(WebDriver driver, String domainName) throws InterruptedException {
		timer(driver);
		SeleniumActions.seleniumSendKeys(driver, Connections.addTemplateDomainTextbox, domainName);
		//Connections.addTemplateDomainTextbox(driver).sendKeys(domainName);
		log.info("Adding Connection Template - Domain name Entered");
	}

	// Add Template - Enter Load Balance Info
	/**
	 * When adding a template, enters the load balancer info
	 * 
	 * @param driver webdriver to drive the browser interaction
	 * @param loadBalanceInfo load balancer information
	 * 
	 * @throws InterruptedException
	 */
	public void addTemplateLoadBalanceInfo(WebDriver driver, String loadBalanceInfo) throws InterruptedException {
		timer(driver);
		SeleniumActions.seleniumSendKeys(driver, Connections.addTemplateLoadBalanceInfo, loadBalanceInfo);
		//Connections.addTemplateLoadBalanceInfo(driver).sendKeys(loadBalanceInfo);
		log.info("Adding Connection Template - Load Balance Info Entered");
	}

	// Delete Connection Template Initiate
	/**
	 * Navigates to connections > manage and clicks the delete button
	 * 
	 * @param driver
	 * 
	 * @throws InterruptedException
	 */
	public void deleteTemplate(WebDriver driver) throws InterruptedException { // Delete User Template - Initiate
		navigateToConnectionsManage(driver);
		timer(driver);
		SeleniumActions.seleniumClick(driver, Connections.btnDeleteConnectionTemplate);
		//Connections.btnDeleteConnectionTemplate(driver).click();
		log.info("Delete Connection Template : Delete Connection Template Button Clicked");
	}

	/**
	 * Deletes selected template
	 * @param driver
	 * @throws InterruptedException
	 */
	public void deleteSelectedTemplate(WebDriver driver) throws InterruptedException {
		timer(driver);
		SeleniumActions.seleniumClick(driver, Connections.btnDeleteTemplates);
		//Connections.btnDeleteTemplates(driver).click();
		//driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
		String toastMessage = Users.notificationMessage(driver).getText();
		Assert.assertTrue(toastMessage.contains("Successfully deleted"), 
				"Toast message did not contain: Successfully deleted, actual text: " + toastMessage);
		log.info("Template Deleted Successfully");
	}

	/**
	 * Provides a wait in execution
	 * @param driver
	 * @throws InterruptedException
	 */
	public void timer(WebDriver driver) throws InterruptedException { // Method for thread sleep
		Thread.sleep(2000);
		driver.manage().timeouts().implicitlyWait(StartupTestCase.getWaitTime(), TimeUnit.SECONDS);
	}

	/**
	 * Navigates to connections > groups
	 * @param driver
	 * @throws InterruptedException
	 */
	public void navigateToGroups(WebDriver driver) throws InterruptedException {
		timer(driver);
		SeleniumActions.seleniumClick(driver, Landingpage.connectionsTab);
		//Landingpage.connectionsTab(driver).click();
		log.info("Connections Group - Clicked on Connections Tab");
		timer(driver);
		Landingpage.groupsTab(driver).click();
		log.info("Connections Group - Clicked on Groups tab");
	}
	
	public void selectTemplateForConnection(WebDriver driver, String templateName, String type, String connectionName) throws InterruptedException {
		Select templateList = new Select(Connections.connectionTemplateDropdown(driver, type));
		templateList.selectByVisibleText(templateName);
		propertyInfoClickNext(driver);
		saveConnection(driver, connectionName);
	}

	/**
	 * Adds a connection group to a group name
	 * 
	 * @param driver
	 * @param groupName
	 * @throws InterruptedException
	 */
	public void addConnectionGroup(WebDriver driver, String groupName) throws InterruptedException {
		navigateToGroups(driver);
		timer(driver);
		SeleniumActions.seleniumClick(driver, Connections.newGroupBtn);
		//Connections.newGroupBtn(driver).click();
		log.info("Add Connections Group - Clicked on add Group button");
		timer(driver);
		SeleniumActions.seleniumSendKeys(driver, Connections.groupName, groupName);
		//Connections.groupName(driver).sendKeys(groupName);
		log.info("Add Connections Group- Group Name Entered");
		timer(driver);
		SeleniumActions.seleniumSendKeys(driver, Connections.groupDescription, groupName + " description");
		//Connections.groupDescription(driver).sendKeys(groupName + " description");
		log.info("Add Connections Group - Group Description added");
		timer(driver);
		SeleniumActions.seleniumClick(driver, Connections.groupAddBtn);
		//Connections.groupAddBtn(driver).click();
		log.info("Add Connections Group - Add button clicked");
		timer(driver);
		String notificationMessage = Users.notificationMessage(driver).getText();
		Assert.assertTrue(notificationMessage.contains("Successfully added group"),
				"Notification message did not contain: Successfully added group, actual text: " + notificationMessage);
		SeleniumActions.seleniumSendKeys(driver, Connections.searchTextbox, groupName);
		//Connections.searchTextbox(driver).sendKeys(groupName);
		String connectionGroupTableText =  SeleniumActions.seleniumGetText(driver, Connections.connectionGroupTable);
		//String connectionGroupTableText =  Connections.connectionGroupTable(driver).getText();
		Assert.assertTrue(connectionGroupTableText.contains(groupName),
				"Connection group table did not contain: " + groupName + ", actual text: " + connectionGroupTableText);
		SeleniumActions.seleniumSendKeysClear(driver, Connections.searchTextbox);
		//Connections.searchTextbox(driver).clear();
		timer(driver);
		log.info("Add Connections Group - " + groupName + " added successfully");
	}

	// Use connection name to filter connection to manage tx , vm, vmpool, broker
	/**
	 * Adds a connection to the passed in group 
	 * @param driver
	 * @param connectionName
	 * @param groupname
	 * @throws InterruptedException
	 */
	public void addConnectionToSelectedGroup(WebDriver driver, String connectionName, String groupname)
			throws InterruptedException {
		navigateToManageConnection(driver, groupname);
		//connectionName = connectionName + "_";
		SeleniumActions.seleniumSendKeys(driver, Connections.connectionListFilterBox, connectionName);
		//Connections.connectionListFilterBox(driver).sendKeys(connectionName);
		timer(driver);
		Select options = new Select(SeleniumActions.getElement(driver, Connections.nonSelectedActiveConnectionList));
		//Select options = new Select(Connections.nonSelectedActiveConnectionList(driver));
		int connectionListSize = options.getOptions().size();
		if (connectionListSize > 0) { // Add all Connection, if Connection available to add
			timer(driver);
			SeleniumActions.seleniumClick(driver, Connections.moveAllBtn);
			//Connections.moveAllBtn(driver).click();
			log.info("Groups > Manage Connections - All Connection moved to Belongs to Group Box");
			timer(driver);
			SeleniumActions.seleniumClick(driver, Connections.saveBtnGroupConnections);
			//Connections.saveBtnGroupConnections(driver).click();
			timer(driver);
			String notificationMessage = Users.notificationMessage(driver).getText();
			Assert.assertTrue(
					notificationMessage.contains("Successfully added connections to group"),
					"Notificaion messgae did not contain: Successfully added connections to group, actual text: " + notificationMessage);
			log.info("Groups > Manage Connections - Connection Managed successfully");
		} else { // if no connection available, exit test and mark test case -failure
			throw new SkipException("****** Sufficient connection not available to add in to group ********");
		}
	}

	// Edit name of first group present in the table
	/**
	 * Edit group name in connetions > group
	 * @param driver
	 * @param newName
	 * @throws InterruptedException
	 */
	public void editGroupName(WebDriver driver, String newName) throws InterruptedException {
		timer(driver);
		SeleniumActions.seleniumClick(driver, Connections.breadCrumb);
		//Connections.breadCrumb(driver).click();
		log.info("Groups > Edit - Breadcrumb clicked");
		timer(driver);
		SeleniumActions.seleniumClick(driver, Connections.editGroup);
		//Connections.editGroup(driver).click();
		log.info("Group > Edit - Edit tab clicked");
		timer(driver);
		SeleniumActions.seleniumSendKeysClear(driver, Connections.groupName);
		//Connections.groupName(driver).clear();
		log.info("Group > Edit - Cuurent name cleared");
		timer(driver);
		SeleniumActions.seleniumSendKeys(driver, Connections.groupName, newName);
		//Connections.groupName(driver).sendKeys(newName);
		timer(driver);
		SeleniumActions.seleniumClick(driver, Connections.groupUpdateBtn);
		//Connections.groupUpdateBtn(driver).click();
		timer(driver);
		String notificationMessage = Users.notificationMessage(driver).getText();
		// Assert check - Successful name edit
		Assert.assertTrue(notificationMessage.contains("Successfully edited group"),
				"Notification message did not contain: Successfully edited group, actual text: " + notificationMessage);
		timer(driver);
	}

	// Delete first group present in the group table
	/**
	 * Delete group in connections > group
	 * @param driver
	 * @throws InterruptedException
	 */
	public void deleteGroup(WebDriver driver) throws InterruptedException {
		timer(driver);
		SeleniumActions.seleniumClick(driver, Connections.breadCrumb);
		//Connections.breadCrumb(driver).click();
		log.info("Groups > Delete - Breadcrumb clicked");
		timer(driver);
		SeleniumActions.seleniumClick(driver, Connections.groupDelete);
		//Connections.groupDelete(driver).click();
		log.info("Groups > Delete - Delete Button clicked");
		Alert alert = driver.switchTo().alert();
		alert.accept();
		timer(driver);
		String notificationMessage = Users.notificationMessage(driver).getText();
		Assert.assertTrue(notificationMessage.contains("Successfully deleted group"),
				"Notification message did not contain: Successfully deleted group, actual text: " + notificationMessage);
	}

	// This navigates to Manage Connections option under group
	/**
	 * Navigates to manage connections option under passed in groupname
	 * @param driver
	 * @param groupname
	 * @throws InterruptedException
	 */
	public void navigateToManageConnection(WebDriver driver, String groupname) throws InterruptedException {
		timer(driver);
		SeleniumActions.seleniumSendKeys(driver, Connections.searchTextbox, groupname);
		//Connections.searchTextbox(driver).sendKeys(groupname);
		log.info("Groups > Manage Connections - Group name entered in filter box");
		SeleniumActions.seleniumClick(driver, Connections.breadCrumb);
		//Connections.breadCrumb(driver).click(); // Clicking breadcrumb of first groupd in the list
		log.info("Groups > Manage Connections - Breadcrumb clicked");
		timer(driver);
		SeleniumActions.seleniumClick(driver, Connections.groupManageConnection);
		//Connections.groupManageConnection(driver).click();
		log.info("Groups > Manage Connections - Manage Connections clicked");
		timer(driver);
	}

	// Remove connection from single connection - First in the list
	public void removeSingleConnection(WebDriver driver) throws InterruptedException {
		timer(driver);
		SeleniumActions.seleniumClick(driver, Connections.breadCrumb);
		//Connections.breadCrumb(driver).click(); // Click breadcrumb from first element
		log.info("Remove Single Connection - Breadcrumb clicked in first element");
		timer(driver);
		SeleniumActions.seleniumClick(driver, Connections.groupManageConnection);
		//Connections.groupManageConnection(driver).click();
		log.info("Remove Single Connection - Manage Connection tab clicked");
		timer(driver);
		Select options = new Select(SeleniumActions.getElement(driver, Connections.selectedActiveConnectionList));
		//Select options = new Select(Connections.selectedActiveConnectionList(driver));
		if (options.getOptions().size() > 0) {
			options.selectByIndex(0); // select first connection in the table
			log.info("Remove Single Connection - First connection from list is selected");
			timer(driver);
			SeleniumActions.seleniumClick(driver, Connections.removeSelectedBtn);
			//Connections.removeSelectedBtn(driver).click();
			log.info("Remove Single Connection - Remove single connection button clicked");
			timer(driver);
			SeleniumActions.seleniumClick(driver, Connections.saveBtnGroupConnections);
			//Connections.saveBtnGroupConnections(driver).click();
			log.info("Remove Single Connection - Save button clicked");
			String notificationMessage = Users.notificationMessage(driver).getText();
			Assert.assertTrue(
					notificationMessage.contains("Successfully added connections to group"),
					"Notification message did not contain: Successfully added connections to group, actual text: " + notificationMessage);
			log.info("Remove Single Connection - Assertion check complete");
		} else {
			log.info("Connection List is empty.. Skipping test case..");
			throw new SkipException("Skipping test case - There isn't any connection present in the list to add.");
		}
	}
	
	public void deleteConnection(WebDriver driver, String name) throws InterruptedException {
		navigateToConnectionsManage(driver);
		SeleniumActions.seleniumSendKeys(driver, Connections.searchTextbox, name);
		SeleniumActions.seleniumClick(driver, Connections.breadCrumb);
		SeleniumActions.seleniumClick(driver, Connections.getDeleteConnection());
		Alert alert = driver.switchTo().alert();
		alert.accept();
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.templateToastMessage)));
		String toast = SeleniumActions.seleniumGetText(driver, Connections.templateToastMessage);
		Assert.assertTrue(toast.equals("Successfully deleted connection."), "toast message did not contain successfully deleted connection. Actual:" + toast);
	}
	
	public void editConnectionZone(WebDriver driver, String connectionName, String zoneName) throws InterruptedException {
		navigateToConnectionsManage(driver);
		SeleniumActions.seleniumSendKeys(driver, Connections.searchTextbox, connectionName);
		SeleniumActions.seleniumClick(driver, Connections.breadCrumb);
		SeleniumActions.seleniumClick(driver, Connections.getConnectionDropdownEdit());
		SeleniumActions.seleniumDropdown(driver, Connections.getZone(), zoneName);
		propertyInfoClickNext(driver);
		propertyInfoClickNext(driver);
		saveConnection(driver, connectionName, zoneName);
	}

	public void addGroupToUser(WebDriver driver, String username, String groupname) throws InterruptedException {
		usermethods.navigateToUsersManage(driver); // navigate to Users > Manage
		timer(driver);
		Users.searchbox(driver).sendKeys(username);
		log.info("Dissolve Group - Username enteted in serach box");
		timer(driver);
		if (Users.usersTable(driver).getText().contains(username)) {
			Users.userBreadCrumb(driver, username).click();
			log.info("Dissolve Group - User breadcrumb clicked");
			timer(driver);
			Users.userManageGroupTab(driver, username).click();
			log.info("Dissolve Group - Manage Group tab clicked");
			timer(driver);
			Users.groupFilterBox(driver).sendKeys(groupname);
			log.info("Dissolve Group - Groupname entered in filterbox");
			if (Users.nonSelectedGroupList(driver).getText().contains(groupname)) {
				timer(driver);
				Users.moveAllConnection(driver).click();
				log.info("Dissolve Group - All searched group moved to Selected Group table");
				timer(driver);
				Users.btnSaveGroup(driver).click();
				log.info("Dissolve Group - Save button clicked");
				timer(driver);
				String notificationMesage = Users.notificationMessage(driver).getText();
				Assert.assertTrue(
						notificationMesage.contains("Successfully added user to group"),
						"Notification message did not contain: Successfully added user to group, actual text: " + notificationMesage);
				log.info("Dissolve Group - Notification asserted.. Group Added Successfully");
			} else {
				log.info("Searched group not found in non-selected list");
				throw new SkipException("***** Seached group not found in the list *****");
			}
		} else {
			log.info("Searched user not found");
			throw new SkipException("***** Searched user not found *****");
		}
	}

	// Dissolve group using Group name and Username to add group
	public void dissolveGroup(WebDriver driver, String username, String groupname) throws InterruptedException {
		// addGroupToUser(driver, username, groupname); // Assign group to user
		navigateToGroups(driver); // Navigate to Connections > Groups
		timer(driver);
		SeleniumActions.seleniumSendKeys(driver, Connections.searchTextbox, groupname);
		//Connections.searchTextbox(driver).sendKeys(groupname);
		log.info("Dissolve Group - Group name entered in searchbox");
		if (SeleniumActions.seleniumGetText(driver, Connections.connectionGroupTable).contains(groupname)) {
		//if (Connections.connectionGroupTable(driver).getText().contains(groupname)) {
			timer(driver);
			SeleniumActions.seleniumClick(driver, Connections.breadCrumb);
			//Connections.breadCrumb(driver).click();
			log.info("Dissolve Group - Breadcrumb clicked from searched group");
			timer(driver);
			SeleniumActions.seleniumClick(driver, Connections.dissolveBtn);
			//Connections.dissolveBtn(driver).click();
			log.info("Dissolve Group - Clicked on Dissolve button");
			Alert alert = driver.switchTo().alert();
			alert.accept();
			timer(driver);
			String notificationMessage = Users.notificationMessage(driver).getText();
			Assert.assertTrue(notificationMessage.contains("Successfully dissolved group"),
					"Notification message did not cotain: Successfully dissolved group, actual text: " + notificationMessage);
			log.info("Dissolve Group - Notification message Asserted");
			timer(driver);
			SeleniumActions.seleniumSendKeys(driver, Connections.searchTextbox, groupname);
			//Connections.searchTextbox(driver).sendKeys(groupname);
			log.info(
					"Dissolve Group - Group name entered in search box to check if group dissolved successfully");
			// Assert if group name is disappeared from connection group table
			String connectionGroupTable = SeleniumActions.seleniumGetText(driver, Connections.connectionGroupTable);
			//String connectionGroupTable = Connections.connectionGroupTable(driver).getText();
			Assert.assertFalse(connectionGroupTable.contains(groupname),
					"Group table contained: " + groupname + ", actual text: " + connectionGroupTable);
			log.info("Dissolve Group - Group not present in the Group table");
			usermethods.navigateToUsersManage(driver);
			timer(driver);
			Users.searchbox(driver).sendKeys(username);
			log.info("Dissolve Group - Username enteted in serach box");
			if (Users.usersTable(driver).getText().contains(username)) {
				log.info("Dissolve Group - Searched user found");
				timer(driver);
				Users.userBreadCrumb(driver, username).click();
				log.info("Dissolve Group - User breadcrumb clicked");
				timer(driver);
				Users.userManageConnectionTab(driver, username).click();
				log.info("Dissolve Group - Users > Manage Connection tab clicked");
				timer(driver);
				String selectedActiveConnectionList = Users.selectedActiveConnectionList(driver).getText();
				Assert.assertTrue(selectedActiveConnectionList.contains("tx"),
						"Selected Active Connection List did not contain: tx, actual text: "+ selectedActiveConnectionList );
				log.info("Dissolve Group - Connection assigned to user");
			} else {
				log.info("Dissolve Group - Searched user not found");
				throw new SkipException("***** Searched user not found *****");
			}
		} else {
			log.info("Dissolve Group - Searched Group not found");
			throw new SkipException("***** Searched Group not found *****");
		}
	}
	public void navigateToActiveConnection(WebDriver driver) throws InterruptedException {
		DashboardMethods dash = new DashboardMethods();
		dash.navigateToDashboard(driver);
		log.info("Navigating to Active Connections");
		SeleniumActions.seleniumClick(driver, Landingpage.connectionsTab);
		//Landingpage.connectionsTab(driver).click();
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(Landingpage.connectionsActive(driver)));
		Landingpage.connectionsActive(driver).click();
		new WebDriverWait(driver, 60).until(ExpectedConditions.presenceOfElementLocated(By.xpath(ActiveConnectionElements.numberOfDevicesOnlineXpath)));
		log.info("Successfully navigated to Active Connections");
		
	}
	
	public void createTxConnection(String connectionName, String connectionType, WebDriver driver, String txIp) throws InterruptedException {
		log.info("Creating connection : " + connectionName);
		addConnection(driver, connectionName, "no"); // connection name, user template
		connectionInfo(driver, "tx", "user", "",  txIp); // connection via, name, host ip
		chooseCoonectionType(driver, connectionType); // connection type
		enableExtendedDesktop(driver);
		enablePersistenConnection(driver);
		if(connectionType.equals("private")) {
			enableUSBRedirection(driver);
			enableAudio(driver);
		}
		propertyInfoClickNext(driver);
		saveConnection(driver, connectionName);
	}
	
	public void createVmRdpConnection(String connectionName, String vmUserName, String vmPassword, String vmIp, String domainName, WebDriver driver) throws InterruptedException {
		addConnection(driver, connectionName, "no");
		connectionInfo(driver, "vm", vmUserName,vmPassword, vmIp);
		domainName(driver, domainName);
		enableExtendedDesktop(driver);
		enableUSBRedirection(driver);
		enableAudio(driver);
		enableNLA(driver);
		propertyInfoClickNext(driver);
		saveConnection(driver, connectionName);
	}
	
	
	public void createRealConnection(String connectionName, String userName, String password, String ipAddress, String txIpaddress) {
		if(connectionName.equals(""))
			connectionName = "Test_TX_Registry";
		Ssh shell = new Ssh(userName, password, ipAddress);
		shell.loginToServer();
		String output = "";
		
		String e2 = shell.sendCommand("e2_read");
		log.info("E2_Read:" + e2);
		if(!e2.contains("Information")) {
			output = shell.sendCommand("/usr/bin/dfreerdp -C '" + connectionName + "' -U 'admin' -u demo -p 'cloud' -g 1920x1080 --dfb:no-banner --dfb:mode=1920x1080 -a 32 -x l --rfx --ignore-certificate --no-tls --no-nla --composition --no-osb --no-bmp-cache --plugin rdpsnd --data alsa:hw:0,0 feedback:0 -- --plugin drdynvc --data rdpeusb -- --inactive 0 --hotkey 0 " + txIpaddress + "&");
		}else {
			 output = shell.sendCommand("PATH=/bin:/sbin:/usr/bin:/usr/sbin ; " + "/usr/bin/bbfreerdp -C '" + 
						connectionName + "' -U 'admin' -u demo -p 'cloud' -g 1920x1080 --dfb:no-banner --dfb:mode=1920x1080 -a 32 -x l --rfx --ignore-certificate --no-tls --no-nla --composition --no-osb --no-bmp-cache --plugin rdpsnd --data alsa:hw:0,0 feedback:0 -- --plugin drdynvc --data rdpeusb -- --inactive 0 --hotkey 0 " 
									+ txIpaddress + " > /usr/local/bbrdp.log 2>&1 &");
		}
		
//		if(StartupTestCase.isEmerald == false) {
//			output = shell.sendCommand("/usr/bin/dfreerdp -C '" + connectionName + "' -U 'admin' -u demo -p 'cloud' -g 1920x1080 --dfb:no-banner --dfb:mode=1920x1080 -a 32 -x l --rfx --ignore-certificate --no-tls --no-nla --composition --no-osb --no-bmp-cache --plugin rdpsnd --data alsa:hw:0,0 feedback:0 -- --plugin drdynvc --data rdpeusb -- --inactive 0 --hotkey 0 " + txIpaddress + "&");
//		}else {
//			 output = shell.sendCommand("PATH=/bin:/sbin:/usr/bin:/usr/sbin ; " + "/usr/bin/bbfreerdp -C '" + 
//		connectionName + "' -U 'admin' -u demo -p 'cloud' -g 1920x1080 --dfb:no-banner --dfb:mode=1920x1080 -a 32 -x l --rfx --ignore-certificate --no-tls --no-nla --composition --no-osb --no-bmp-cache --plugin rdpsnd --data alsa:hw:0,0 feedback:0 -- --plugin drdynvc --data rdpeusb -- --inactive 0 --hotkey 0 " 
//					+ txIpaddress + " > /usr/local/bbrdp.log 2>&1 &");
//			//output = shell.sendCommand("/usr/bin/bbfreerdp -C '" + connectionName + "' -U 'admin' -u demo -p 'cloud' -g 1920x1080 --dfb:no-banner --dfb:mode=1920x1080 -a 32 -x l --rfx --ignore-certificate --no-tls --no-nla --composition --no-osb --no-bmp-cache --plugin rdpsnd --data alsa:hw:0,0 feedback:0 -- --plugin drdynvc --data rdpeusb -- --inactive 0 --hotkey 0 " + txIpaddress + " > /usr/local/bbrdp.log 2>&1");			
//		}
		try {
			Thread.sleep(10000);
			shell.disconnect();
			log.info("Output from freedrdp script: " + output);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
