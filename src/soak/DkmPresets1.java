package soak;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import extra.StartupTestCase;
import extra.StartupTestCase2;
import methods.DkmMethods;
import objects.DkmElements;
import objects.Landingpage;
import objects.Loginpage;
import testNG.Utilities;

public class DkmPresets1 extends StartupTestCase{

	DkmMethods methods = new DkmMethods();
	final static Logger log = Logger.getLogger(DkmPresets1.class);

	public DkmPresets1(){
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
	public void dkmPreset1() throws InterruptedException {
		log.info("Partial preset testcase - Soak Test dkmPreset1 started");
		methods.resetAllDkm(driver);
		log.info("Starting dkmPreset1..");
		methods.navigateToDkmViewer(driver);
		methods.addSource(driver, "VTX1");
		methods.addDestination(driver, "VTX1", "VRX1");
		methods.assertUI(driver, "VTX1");
		DkmElements.managePresetsBtn(driver).click();
		methods.timer(driver);
		DkmElements.createCustomPreset(driver).click();
			methods.timer(driver);
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", DkmElements.presetSourceListItem(driver, "VTX2"));
			methods.timer(driver);
			DkmElements.presetSourceListItem(driver, "VTX2").click();
			
			methods.timer(driver);
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", DkmElements.presetSourceListItem(driver, "VTX3"));
			DkmElements.presetSourceListItem(driver, "VTX3").click();

		DkmElements.nextBtn(driver).click();
		methods.timer(driver);

		// first TRX
		DkmElements.selectedPresetSourceList(driver).get(0).click();
		log.info("Source " + 1 + " selected from the list");
		methods.timer(driver);
		DkmElements.selectPresetDestination(driver, "VRX2");
		log.info("VR" + 2 + " is selected");
		Assert.assertTrue(DkmElements.presetSelectedDestinationContainer(driver).get(0).getText().contains("RX"),
				"***** Preset destination container does not contains RX *****");
		// second TRX
		DkmElements.selectedPresetSourceList(driver).get(1).click();
		log.info("Source " + 2 + " selected from the list");
		methods.timer(driver);
		DkmElements.selectPresetDestination(driver, "VRX1");
		log.info("VR" + 1 + " is selected");
		Assert.assertTrue(DkmElements.presetSelectedDestinationContainer(driver).get(1).getText().contains("RX"),
				"***** Preset destination container does not contains RX *****");
		methods.timer(driver);
		DkmElements.nextBtn(driver).click();
		log.info("Create Presets - Stage 2 completed.. Next button clicked");
		methods.timer(driver);
		DkmElements.presetName(driver).sendKeys("testPreset");
		log.info("Preset name entered");
		methods.timer(driver);
		Select selectType = new Select(DkmElements.createPresetSelectType(driver));
		selectType.selectByValue("partial");
		log.info("Preset type selected - Partial");
		methods.timer(driver);
		DkmElements.completeBtn(driver).click();// Finish flow here
		methods.assertPreset(driver, "testPreset");
		DkmElements.closeBtnManagePresetsModal(driver).click();
		methods.timer(driver);
		DkmElements.presetButton(driver).click();
		methods.timer(driver);
		Landingpage.dkmTab(driver).click();
		methods.timer(driver);
		Landingpage.dkmConnectionsTab(driver).click();
		methods.timer(driver);
		// Assert Connection
		Assert.assertTrue(DkmElements.activeConnectionCoutner(driver).getText().equalsIgnoreCase("2"),
				"Active connections is greater than 2");
		Assert.assertTrue(DkmElements.dkmConnectionRow(driver, "VTX3").getText().contains("VRX1"),
				"VTX3 is not connected to VRX1");
		Assert.assertTrue(DkmElements.dkmConnectionRow(driver, "VTX2").getText().contains("VRX2"),
				"VTX2 is not connected to VRX2");
		methods.timer(driver);
		log.info("Partial preset testcase - Soak Test dkmPreset1 Completed");
		
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
		try {
		log.info("Cleaning up after DKM test");
		methods.resetAllDkm(driver);
		methods.navigateToDkmSwitches(driver);
		methods.deleteSwitch(driver, "10.211.128.123");
		log.info("Clean up complete");
		cleanUpLogout();
		}catch(Exception | AssertionError e) {
			cleanUpLogout();
		}finally {
			cleanUpLogout();
		}
		
	}
}
