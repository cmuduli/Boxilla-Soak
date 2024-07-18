package soak;

import static io.restassured.RestAssured.basic;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import extra.StartupTestCase;
import extra.StartupTestCase2;
import io.restassured.RestAssured;
import methods.ConnectionsMethods;
import methods.DevicesMethods;
import methods.DiscoveryMethods;
import objects.Loginpage;
import testNG.Utilities;

public class DeviceParameters extends StartupTestCase {

	final static Logger log = Logger.getLogger(DeviceParameters.class);
	private ConnectionsMethods connections = new ConnectionsMethods();
	private DevicesMethods device = new DevicesMethods();
	private DiscoveryMethods discoveryMethods = new DiscoveryMethods();

	//test properties
	private int timeout = 100000;
	
	
	@Test
	public void setVideoQuality() throws InterruptedException {
		device.setUniquePropertyTx(driver, prop.getProperty("txIP"), "Video Quality", "Best Quality");
		log.info("Sleeping while device reboots..");
		String url = getHttp() + "://" + prop.getProperty("txIP") + getPort() + "/control/configuration/tx_settings";
		System.out.println(url);
		Thread.sleep(timeout);
		given().header(getHead())
		.when()
		.get(getHttp() + "://" + prop.getProperty("txIP") + getPort() + "/control/configuration/tx_settings")
		.then()
		.body("video_quality", equalTo(0));
	}
	
	@Test
	public void setVideoSourceOptimisation() throws InterruptedException {
		device.setUniquePropertyTx(driver, prop.getProperty("txIP"), "Video Source", "Off");
		log.info("Sleeping while device reboots..");
		Thread.sleep(timeout);
		String url = getHttp() + "://" + prop.getProperty("txIP") + getPort() + "/control/configuration/tx_settings";
		System.out.println(url);
		given().header(getHead())
		.when()
		.get(getHttp() + "://" + prop.getProperty("txIP") + getPort() + "/control/configuration/tx_settings")
		.then()
		.body("video_source_optimisation", equalTo(0));
	}
	
	@Test
	public void setHid() throws InterruptedException {
		device.setUniquePropertyTx(driver,  prop.getProperty("txIP"), "HID", "Default");
		log.info("Sleeping while device reboots..");
		Thread.sleep(timeout);
		given().header(getHead())
		.when()
		.get(getHttp() + "://" +  prop.getProperty("txIP") + getPort() + "/control/configuration/tx_settings")
		.then()
		.body("hid", equalTo(0));
	}
	
	@Test
	public void setEdid1() throws InterruptedException {
		device.setUniquePropertyTx(driver, prop.getProperty("txIP"), "EDID1", "1920x1080");
		log.info("Sleeping while device reboots..");
		Thread.sleep(timeout);
		given().header(getHead())
		.when()
		.get(getHttp() + "://" + prop.getProperty("txIP") + getPort() + "/control/configuration/tx_settings")
		.then()
		.body("head_1_edid", equalTo(0));
	}
	
	@Test
	public void setPowerMode() throws InterruptedException {
		device.setUniquePropertyRx(driver, prop.getProperty("rxIP"), "Power Mode", true);
		log.info("Sleeping while device reboots..");
		Thread.sleep(timeout);
		
		given().header(getHead())
		.when()
		.get(getHttp() + "://" + prop.getProperty("rxIP") + getPort() + "/control/configuration/rx_settings")
		.then()
		.body("power_mode", equalTo(0));
	}
	
	@Test
	public void setHttpEnabled() throws InterruptedException {
		device.setUniquePropertyRx(driver, prop.getProperty("rxIP"), "HTTP Enabled", true);
		log.info("Sleeping while device reboots..");
		Thread.sleep(timeout);
		
		given().header(getHead())
		.when()
		.get(getHttp() + "://" + prop.getProperty("rxIP") + getPort() + "/control/configuration/rx_settings")
		.then()
		.body("http_enable", equalTo(1));
	}
	
	@Test
	public void setMouseKeyboard() throws InterruptedException {
		device.setUniquePropertyTx(driver, prop.getProperty("txIP") + getPort(), "Mouse Timeout", "0");
		log.info("Sleeping while device reboots..");
		Thread.sleep(timeout);
		given().header(getHead())
		.when()
		.get(getHttp() + "://" + prop.getProperty("txIP") + getPort() + getPort() + "/control/configuration/tx_settings")
		.then()
		.body("mouse_keyboard_timeout", equalTo(0));
	}
	
	
	@AfterClass(alwaysRun=true)
	public void afterClass() {
		try {
			cleanUpLogin();
			device.unManageDevice(driver, prop.getProperty("rxIP"));
			device.unManageDevice(driver, prop.getProperty("txIP"));
		}catch(Exception | AssertionError e) {
			cleanUpLogout();
		}
		cleanUpLogout();
	}

	@Override
	@BeforeMethod(alwaysRun = true)
	@Parameters({ "browser" })
	public void login(String browser, Method method) throws InterruptedException {
		// Select driver based on the Browser parameter selected
		//String url = "https://" + boxillaManager + "/";
		//if (browser.equalsIgnoreCase("firefox")) {
			/* *************************** Firefox Driver ********************************* */
			System.setProperty("webdriver.gecko.driver", "C:\\Selenium\\Webdrivers\\geckodriver.exe");
			DesiredCapabilities caps = new DesiredCapabilities();
			caps.setCapability("acceptInsecureCerts", true); // Accepting insecure content
			driver = new FirefoxDriver(caps);
			driver.manage().window().maximize();
			driver.get(url);
//		} else if (browser.equalsIgnoreCase("chrome")) {
//			/* **************************** Chrome Driver ********************************* */
//			System.setProperty("webdriver.chrome.driver", "C:\\Selenium\\Webdrivers\\chromedriver.exe");
//			driver = new ChromeDriver();
//			driver.manage().window().maximize();
//			driver.get(url);
//		} else if (browser.equalsIgnoreCase("ie")) {
//			// ******************************* IE Driver
//			// ************************************
//			System.setProperty("webdriver.ie.driver", "C:\\Selenium\\Webdrivers\\IEDriverServer.exe");
//			driver = new InternetExplorerDriver();
//			driver.manage().window().maximize();
//			driver.get(url);
//			driver.navigate().to("javascript:document.getElementById('overridelink').click()");
//		}

		try {
			Thread.sleep(2000);
			printTestDetails("STARTING ", method.getName(), "");
			Loginpage.username(driver).sendKeys(userName);

			Thread.sleep(2000);
			Loginpage.password(driver).sendKeys(password);

			Thread.sleep(2000);
			Loginpage.loginbtn(driver).click();
			
			//set timeout for webdriver
			int timeout = Integer.parseInt(prop.getProperty("waitTime"));
			driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
			log.info("setting devices to System properties");
			device.setTxToSystemProperties(driver, prop.getProperty("txIP"));
			device.setRxToSystemProperty(driver, prop.getProperty("rxIP"));
			log.info("sleeping while properties are applied");
			Thread.sleep(100000);
			log.info("System properties set");
		} catch (Exception e) {
			log.info("Error in Login method");
			driver.quit();
		}

	}

	@BeforeClass(alwaysRun = true)
	public void beforeClass() throws InterruptedException {
		printSuitetDetails(false);
		getDevices();
		RestAssured.authentication = basic(restuser, restPassword);			//REST authentication
		RestAssured.useRelaxedHTTPSValidation();
		try {	
			cleanUpLogin();
			deviceManageTestPrep();
			device.setSystemProperties(driver, "2", "DVI Optimised", "Basic",
					"USB", "3", "1920x1200", "1920x1200", false, false);
			Thread.sleep(timeout);
		}catch(Exception | AssertionError e) {
			Utilities.captureScreenShot(driver, this.getClass().getName() + "_beforeClass", "Before Class");
			e.printStackTrace();
			cleanUpLogout();
		}
		cleanUpLogout();
	}
	
	
	
	
	public void deviceManageTestPrep() throws InterruptedException {
		log.info("Test Preparation Manage Device");

		//RX
		discoveryMethods.discoverDevices(driver);
		discoveryMethods.stateAndIPcheck(driver, prop.getProperty("rxMac"), prop.getProperty("ipCheck"), prop.getProperty("rxIP"),
				prop.getProperty("gateway"), prop.getProperty("netmask"));
		discoveryMethods.manageApplianceAutomatic(driver, prop.getProperty("rxIP"), prop.getProperty("rxMac"),
				prop.getProperty("ipCheck"));



		//TX
		discoveryMethods.discoverDevices(driver);
		discoveryMethods.stateAndIPcheck(driver, prop.getProperty("txMac"), prop.getProperty("ipCheck"),
				prop.getProperty("txIP"), prop.getProperty("gateway"), prop.getProperty("netmask"));

		discoveryMethods.manageApplianceAutomatic(driver, prop.getProperty("txIP"), prop.getProperty("txMac"), 
				prop.getProperty("ipCheck"));


		log.info("Appliances Managed Successfully");
	}
	
	
}
