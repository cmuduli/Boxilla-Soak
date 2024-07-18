package soak;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import extra.StartupTestCase;
import extra.StartupTestCase2;
import methods.DkmMethods;
import objects.DkmElements;
import objects.Landingpage;
import objects.Loginpage;
import testNG.Utilities;

public class DkmPresets4 extends StartupTestCase {

	DkmMethods methods = new DkmMethods();
	final static Logger log = Logger.getLogger(DkmPresets4.class);

	public DkmPresets4() {
		super();
	}
	//@Test(retryAnalyzer = testNG.RetryAnalyzer.class)
	@BeforeClass(alwaysRun=true)
	public void beforeClass() throws InterruptedException {
		cleanUpLogin();
		log.info("Test Preparation  - DKM - Add Switch");
		
		try {
		methods.navigateToDkmSwitches(driver);
		methods.addDkmSwitch(driver, "10.211.128.123", "Test_Switch");
		}catch(Exception | AssertionError e) {
			cleanUpLogout();
		}
		log.info("DKM - Switch Added - Test Preparation Completed");
		cleanUpLogout();
	
	}

	@Test//(dependsOnMethods = { "dependencySwitchAdd" })
	public void dkmPreset4() throws InterruptedException { // Add Preset and Enable it
		/* 1. Detach All connection and Delete All Preset
		 * 2. Create Manual Connection VTX1 - VRX1 and Create Preset (Type- Full) with VTX2-VRX2, VTX3-VRX3
		 * 3. Assert Preset Created Using Preset Name and Enable Preset
		 * 4. Assert Connections - There should be two active connections VTX2-VRX2, VTX3-VRX3 */
		log.info("Full preset testcase - Soak Test dkmPreset4 Started");
		// Removing connections and presets
		methods.resetAllDkm(driver);
		// Create Manual connection VTX1 - VRX1
		methods.addSource(driver, "VTX1");
		methods.addDestination(driver, "VTX1", "VRX1");
		methods.timer(driver);
		DkmElements.managePresetsBtn(driver).click();
		log.info("Managed Presets button clicked");
		methods.timer(driver);
		DkmElements.createCustomPreset(driver).click();
		// Add two source using checkbox index - VTX2 and VTX3
		//first
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", DkmElements.presetSourceListItem(driver, "VTX2"));
		DkmElements.presetSourceListItem(driver, "VTX2").click();
		//second
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", DkmElements.presetSourceListItem(driver, "VTX3"));
		DkmElements.presetSourceListItem(driver, "VTX3").click();
		
		
		methods.timer(driver);
		DkmElements.nextBtn(driver).click();
		log.info("Create Presets - Stage 1 completed.. Next button clicked");
		methods.timer(driver);
		log.info(
				"Selected Preset Source list size is : " + DkmElements.selectedPresetSourceList(driver).size());
		// Add destination in selected sources - Add VRX1 to VTX1, VRX2 to VTX2 and VRX3
		// to VTX3
		DkmElements.selectedPresetSourceList(driver).get(0).click();
		DkmElements.selectPresetDestination(driver, "VRX2");
		Assert.assertTrue(DkmElements.presetSelectedDestinationContainer(driver).get(0).getText().contains("RX"),
				"***** Preset destination container does not contains RX *****");
		
		//second
		DkmElements.selectedPresetSourceList(driver).get(1).click();
		log.info("Source " + 1 + " selected from the list");
		methods.timer(driver);
		DkmElements.selectPresetDestination(driver, "VRX3");
		log.info("VR" + 2 + " is selected");
		Assert.assertTrue(DkmElements.presetSelectedDestinationContainer(driver).get(1).getText().contains("RX"),
				"***** Preset destination container does not contains RX *****");
//		for (int i = 1; i <= DkmElements.selectedPresetSourceList(driver).size(); i++) {
//			methods.timer(driver);
//			DkmElements.selectedPresetSourceList(driver).get(i - 1).click();
//			log.info("Source " + i + " selected from the list");
//			methods.timer(driver);
//			DkmElements.presetDestinationCheckbox(driver, (i + 1), i).click(); // Selecting second element from list
//			log.info("VR" + (i + 1) + " is selected");
//			methods.timer(driver);
//			Assert.assertTrue(
//					DkmElements.presetSelectedDestinationContainer(driver).get(i - 1).getText().contains("RX"),
//					"***** Preset destination container does not contains RX *****");
//		}

		methods.timer(driver);
		DkmElements.nextBtn(driver).click();
		log.info("Create Presets - Stage 2 completed.. Next button clicked");
		methods.timer(driver);
		DkmElements.presetName(driver).sendKeys("preset1");
		log.info("Preset name entered");
		methods.timer(driver);
		Select selectType = new Select(DkmElements.createPresetSelectType(driver));
		selectType.selectByValue("full");
		log.info("Preset type selected - Full");
		methods.timer(driver);
		DkmElements.completeBtn(driver).click();// Finish flow here
		methods.assertPreset(driver, "preset1");
		DkmElements.closeBtnManagePresetsModal(driver).click();
		methods.timer(driver);
		DkmElements.presetButton(driver).click();
		methods.timer(driver);
		Landingpage.dkmTab(driver).click();
		methods.timer(driver);
		Landingpage.dkmConnectionsTab(driver).click();
		methods.timer(driver);
		// Assert Connection - 2 Active Connection VTX2- VRX2 and VTX3 - VRX3
		Assert.assertTrue(DkmElements.activeConnectionCoutner(driver).getText().equalsIgnoreCase("2"),
				"***** Active Connection Counts is not equal to 2 *****");
		Assert.assertTrue(DkmElements.dkmConnectionRow(driver, "VTX2").getText().contains("VRX2"),
				"VTX2 is not connected to VRX2");
		Assert.assertTrue(DkmElements.dkmConnectionRow(driver, "VTX3").getText().contains("VRX3"),
				"VTX3 is not connected to VRX3");
		methods.timer(driver);
		log.info("Full preset testcase - Soak Test dkmPreset4 Completed");
		
	}
	
	/**
	 * Restores system to initial state. Will log into the application again and remove any connections created during tests
	 * and remove any switches added. 
	 * @throws InterruptedException
	 */
	
	@AfterClass(alwaysRun = true)
	public void afterClass(ITestContext context) throws InterruptedException{
		String url = "https://" + boxillaManager + "/";
		System.setProperty("webdriver.gecko.driver", "C:\\Selenium\\Webdrivers\\geckodriver.exe");
		DesiredCapabilities caps = new DesiredCapabilities();
		caps.setCapability("acceptInsecureCerts", true); // Accepting insecure content
		driver = new FirefoxDriver(caps);
		driver.manage().window().maximize();
		driver.get(url);
		
		try {
			Thread.sleep(2000);
			Loginpage.username(driver).sendKeys(userName);

			Thread.sleep(2000);
			Loginpage.password(driver).sendKeys(password);

			Thread.sleep(2000);
			Loginpage.loginbtn(driver).click();
			
			//set timeout for webdriver
			int timeout = Integer.parseInt(prop.getProperty("waitTime"));
			driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
		} catch (Exception e) {
			log.info("Error in Login method");
			driver.quit();
		}
		log.info("Cleaning up after DKM test");
		methods.resetAllDkm(driver);
		methods.navigateToDkmSwitches(driver);
		methods.deleteSwitch(driver, "10.211.128.123");
		log.info("Clean up complete");
		cleanUpLogout();
		
		
	}
}

