package soak;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.basic;
import static io.restassured.RestAssured.given;

import org.apache.log4j.Logger;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import connection.ForcedConnection;
import extra.Ssh;
import extra.StartupTestCase;
import extra.StartupTestCase2;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.path.json.JsonPath;
import methods.ConnectionsMethods;
import methods.DevicesMethods;
import methods.DiscoveryMethods;
import objects.Connections;
import objects.Landingpage;
import objects.Loginpage;
import testNG.Utilities;

public class ForceConnection extends StartupTestCase2 {

	final static Logger log = Logger.getLogger(ForcedConnection.class);
	private ConnectionsMethods connections = new ConnectionsMethods();
	private DevicesMethods device = new DevicesMethods();
	private DiscoveryMethods discoveryMethods = new DiscoveryMethods();
	private ArrayList<Integer> memoryAmountTx = new ArrayList<Integer>();
	private ArrayList<Integer> memoryAmountRx = new ArrayList<Integer>();
	private int testCounter = 1;
	private Ssh ssh;

	//test properties
	private int timeout = 100000;
	private String privateConnectionName = "privateSoakTest";

	class ForceConnect {
		public String action = "";
		public String user = "";
		public String connection = "";
	}
	
	@Override
	@BeforeMethod(alwaysRun = true)
	//@Parameters({ "browser" })
	public void login( Method method) {
		
	}
	@Override
	@AfterMethod(alwaysRun = true)
	public void logout(ITestResult result) throws InterruptedException {
		log.info("********* @ After Method Started ************");
		// Taking screen shot on failure
		//String url = "https://" + boxillaManager + "/";
//		try {
//			Thread.sleep(2000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		String scriptRunning = ssh.sendCommand("ps -ax");
		if(scriptRunning.contains("freerdp")) {
			log.info("Connection still running after test. NOT REBOOTING");
			ssh.sendCommand("/sbin/reboot");
			Thread.sleep(60000);
			ssh.loginToServer();
		}
		//print result
		if(ITestResult.FAILURE == result.getStatus())
			log.info(result.getName() + " :FAIL");
		
		if(ITestResult.SKIP == result.getStatus())
			log.info(result.getName() + " :SKIP");
		
		if(ITestResult.SUCCESS == result.getStatus())
			log.info(result.getName() + " :PASS");
		
		if (ITestResult.FAILURE == result.getStatus() || ITestResult.SKIP == result.getStatus()) {
			//String screenShotName = result.getName() + Utilities.getDateTimeStamp();
			//Utilities.captureScreenShot(driver, screenShotName, result.getName());
		}
	}
	
//	public static void main(String args[]) {
//		RestAssured.authentication = basic("rest_user", "18_22_33_AA");			//REST authentication
//		RestAssured.useRelaxedHTTPSValidation();
//		String url = "http://" + args[0] + ":7778/control/connections";
//		System.out.println("URL:" + url);
//		class ForceConnect {
//			public String action = "";
//			public String user = "";
//			public String transmitter = "";
//		}
//		ForceConnect con = new ForceConnect();
//		con.action = "make_connection";
//		con.user = "admin";
//		con.transmitter = "10.211.128.155";
//		Header head = new Header("api-version", "1.0.4");
//		int status = given().header(head).body(con)
//				.when()
//				.contentType(ContentType.JSON)
//				.put(url).andReturn().statusCode();
//		System.out.println("Status code:" + status);
//	}
//	
	
	@Test
	public void makeBreakForcedConnectionRest() throws InterruptedException {
		log.info("***** makeBreakForcedConnection *****");
		log.info("Test count: " + testCounter);
		testCounter++;
		ForceConnect con = new ForceConnect();
		con.action = "force_connection";
		con.user = "Boxilla";
		con.connection = "70_DTXSA_TX";
		String command = "/usr/bin/bbfreerdp -C 'tx_53' -u 'Boxilla' -p 'cloud' -g 1920x1024 --dfb:no-banner --dfb:mode=1920x1024 -a 32 -x l --rfx --ignore-certificate --no-tls --no-nla --composition --no-osb --no-bmp-cache "
				+ "-o --inactive 0 10.211.129.53&";
		//log.info("Command:" + command);
		//ssh.sendCommand(command);
		for(int j=0; j < 50; j++) {
		int status = given().header(getHead()).body(con)
				.when()
				.contentType(ContentType.JSON)
				.put("https://10.211.128.156:8888/control/connections").andReturn().statusCode();
		System.out.println("Status code:" + status);
		log.info("Sleeping for 10 seconds then terminating the connection");
		Thread.sleep(10000);
		//String isCloudRunning = ssh.sendCommand("ps -ax");
		//System.out.println("OUTPUT:" + isCloudRunning); 
		//Assert.assertTrue(isCloudRunning.contains("freerdp"), "Connection was not running when it should have been ");
		//log.info("Sleeping for 15 seconds");
		String json = "{\"action\": \"terminate_connection\"}";
		//System.out.println(prop.getProperty("rxIP"));
		int status2 = given().header(getHead()).body(json)
				.when()
				.contentType(ContentType.JSON)
				.put("https://10.211.128.156:8888/control/connections").andReturn().statusCode();
		//log.info("Status code from terminate_connection:" + status2);
		//ssh.sendCommand("killall -SIGUSR1 connection_control");
		Thread.sleep(90);
		}
		//System.out.println(prop.getProperty("rxIP"));
		// isCloudRunning = ssh.sendCommand("ps -ax");
		//System.out.println("OUTPUT:" + isCloudRunning); 
		//Assert.assertTrue(!isCloudRunning.contains("freerdp"), "Connection was running when it should have been down");
		////getMemory();
					//	printMemoryDetails();
		
	}
	private void getMemory() {
//		log.info("Getting system facts for RX");
//		JsonPath path = 
//				given().header(getHead())
//				.when()
//				.get(getHttp() + "://" + prop.getProperty("rxIP") + getPort() + "/statistics/facts/system_facts").jsonPath();
//				int freeMemory = 0;
//				try {
//				freeMemory = path.get("kvm_system_facts.'free memory'[0]");
//				}catch(Exception e) {
//					log.info("Could not get memory for RX. Reporting as 0");
//				}
//				memoryAmountRx.add(freeMemory);
//				System.out.println("MEMORY RX:" + freeMemory);
				log.info("Getting system facts for TX");
				JsonPath pathTx = 
						given().header(getHead())
						.when()
						.get(getHttp() + "://" + prop.getProperty("txIP") + getPort() + "/statistics/facts/system_facts").jsonPath();
						int freeMemoryTx = 0;
						try {
							freeMemoryTx = pathTx.get("kvm_system_facts.'free memory'[0]");	
						}catch(Exception e) {
							log.info("Could not get memory for TX. Reporting as 0");
						}
						memoryAmountTx.add(freeMemoryTx);
						System.out.println("MEMORY TX:" + freeMemoryTx);
						log.info("Printing memory details");
	}
	
	//@Test
	public void makeBreakForcedConnection() throws InterruptedException {
		log.info("***** makeBreakForcedConnection *****");
		String[] connectionSources = {privateConnectionName};
		try {
			connections.addSources(driver, connectionSources);
		}catch(Exception e) {
			log.info("There was a problem with adding the source. May be left over from previous test..Continuing ");
		}
		connections.timer(driver);
		connections.timer(driver);
		connections.addPrivateDestination(driver, privateConnectionName, prop.getProperty("rxIP"));
		log.info("Waiting 65 secs");
		Thread.sleep(65000);
		log.info("refreshing page");
		driver.navigate().refresh();
		connections.timer(driver);
		log.info("page refreshed");
		log.info("checking conditions..");
		connections.timer(driver);
		connections.timer(driver);
		boolean isConnection = Connections.singleSourceDestinationCheck(driver, privateConnectionName,  prop.getProperty("rxIP")).isDisplayed();
		log.info("conditions passed");
		//Assert connection has been created
		log.info("Asserting if connections has been created between " + privateConnectionName + " and " + prop.getProperty("rxIP"));
		Assert.assertTrue(isConnection, "Connection between " + privateConnectionName + " and " + prop.getProperty("rxIP") + " is not displayed");
		log.info("Connection created sucessfully");
	//	Thread.sleep(15000);
		
//		JsonPath videoFacts = given().header(getHead())
//				.when()
//				.get(getHttp() + "://" + prop.getProperty("rxIP") + getPort() + "/statistics/facts/video_facts").jsonPath();
//		String returnValue = given().header(getHead())
//				.when()
//				.get(getHttp() + "://" + prop.getProperty("rxIP") + getPort() + "/statistics/facts/video_facts").asString();
//		System.out.println(returnValue);
		//int fps = videoFacts.get("kvm_video_facts.'frames_per_second'[0]");
		//System.out.println("FPS:" + fps);
//		if(fps == 0) {
//			System.out.println("NO VIDEO GOING THROUGH");
//		}
//		
		log.info("Attemping to break connection for next test");
		
		connections.breakConnection(driver, privateConnectionName);
		log.info("sleeping");
		Thread.sleep(65000);
		log.info("refreshing page");
		driver.navigate().refresh();
		log.info("page refreshed");
		log.info("checking conditions ");
		connections.timer(driver);
		connections.timer(driver);
		boolean isNonActiveSource = Connections.nonActiveSource(driver).isDisplayed();
		log.info("Nonactive source:" + isNonActiveSource);
		boolean isNonActiveDestination = Connections.nonActiveDestination(driver).isDisplayed();
		log.info("Non activeDestinations:" + isNonActiveDestination);
		log.info("conditions checked");
		log.info("Asserting if connection has been broken");
		Assert.assertTrue(isNonActiveSource && isNonActiveDestination, "Connection has not been broken");
		log.info("Connection succesfully broken");
		//Thread.sleep(3000);
//		JsonPath path = 
//				given().header(getHead())
//				.when()
//				.get(getHttp() + "://" + prop.getProperty("rxIP") + getPort() + "/statistics/facts/system_facts").jsonPath();
//				int freeMemory = path.get("kvm_system_facts.'free memory'[0]");	
//				memoryAmountRx.add(freeMemory);
//				System.out.println("MEMORY RX:" + freeMemory);
//				JsonPath pathTx = 
//						given().header(getHead())
//						.when()
//						.get(getHttp() + "://" + prop.getProperty("txIP") + getPort() + "/statistics/facts/system_facts").jsonPath();
//						int freeMemoryTx = pathTx.get("kvm_system_facts.'free memory'[0]");	
//						memoryAmountTx.add(freeMemoryTx);
//						System.out.println("MEMORY TX:" + freeMemoryTx);
//						
//						printMemoryDetails();

	}
	
	private void printMemoryDetails() {
		System.out.println("TX Memory");
		System.out.println("Before Test:" + memoryAmountTx.get(0));
		for(int j=1; j < memoryAmountTx.size(); j++) {
			System.out.println("Iteration " + j + ": " + memoryAmountTx.get(j));
		}
		
		System.out.println();
		System.out.println();
		System.out.println();
		
//		System.out.println("RX Memory");
//		System.out.println("Before Test:" + memoryAmountRx.get(0));
//		for(int j=1; j < memoryAmountRx.size(); j++) {
//			System.out.println("Iteration " + j + ": " + memoryAmountRx.get(j));
//		}
		
	}

//	@AfterClass(alwaysRun=true)
//	public void afterClass() {
//		try {
//			Thread.sleep(60000);
//			log.info("Getting final stats");
//			//printMemoryDetails();
//			cleanUpLogin();
//			device.unManageDevice(driver, prop.getProperty("rxIP"));
//			device.unManageDevice(driver, prop.getProperty("txIP"));
//			cleanUpLogout();
//		}catch(Exception | AssertionError e) {
//			cleanUpLogout();
//		}
//	}

	@Override
	@BeforeClass(alwaysRun = true)
	public void beforeClass() throws InterruptedException {
		printSuitetDetails(false);
		getDevices();
		try {	
			RestAssured.authentication = basic(restuser, restPassword);		
			RestAssured.useRelaxedHTTPSValidation();
			ssh = new Ssh("root", "barrow1admin_12", "10.211.129.244");
			ssh.loginToServer();
//			cleanUpLogin();
//			makeConnection(privateConnectionName, "private");
//			deviceManageTestPrepTest();
			//Thread.sleep(30000);
//			
			
//			JsonPath path = 
//					given().header(getHead())
//					.when()
//					.get(getHttp() + "://" + prop.getProperty("rxIP") + getPort() + "/statistics/facts/system_facts").jsonPath();
//					int freeMemory = path.get("kvm_system_facts.'free memory'[0]");	
//					memoryAmountRx.add(freeMemory);
//					System.out.println("MEMORY RX:" + freeMemory);
//					
//					JsonPath pathTx = 
//							given().header(getHead())
//							.when()
//							.get(getHttp() + "://" + prop.getProperty("txIP") + getPort() + "/statistics/facts/system_facts").jsonPath();
//							int freeMemoryTx = pathTx.get("kvm_system_facts.'free memory'[0]");	
//							memoryAmountTx.add(freeMemoryTx);
//							System.out.println("MEMORY TX:" + freeMemoryTx);
					
			
			//Thread.sleep(timeout);
		}catch(Exception | AssertionError e) {
			Utilities.captureScreenShot(driver, this.getClass().getName() + "_beforeClass", "Before Class");
			e.printStackTrace();
			//cleanUpLogout();
		}
		//cleanUpLogout();
	}
	

	
//	@Override
//	@AfterMethod(alwaysRun = true)
//	public void logout(ITestResult result) throws InterruptedException {
//		log.info("********* @ After Method Started ************");
//		// Taking screen shot on failure
//		//String url = "https://" + boxillaManager + "/";
//		try {
//			Thread.sleep(2000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		//print result
//		if(ITestResult.FAILURE == result.getStatus()) {
//			log.info(result.getName() + " :FAIL");
//			//Thread.sleep(30000);
////			try {
////			Ssh sshRx = new Ssh("root", "barrow1admin_12", "10.211.129.244");
////			sshRx.loginToServer();
////			sshRx.sendCommand("cp /usr/local/syslog.log /usr/local/syslog.log_failOn" + (testCounter-1));
////			sshRx.disconnect();
////			}catch(Exception e) {
////				log.info("Error logging into RX server");
////			}
////			
////			//tx
////			try {
////			Ssh sshTx = new Ssh("root", "barrow1admin_12", "10.211.129.243");
////			sshTx.loginToServer();
////			sshTx.sendCommand("cp /usr/local/syslog.log /usr/local/syslog.log_failOn" + (testCounter-1));
////			sshTx.disconnect();
////			}catch(Exception e1) {
////				log.info("Error logging into TX server");
////			}
//		}
//		
//		if(ITestResult.SKIP == result.getStatus()) {
//			log.info(result.getName() + " :SKIP");
////			Thread.sleep(30000);
////			try {
////			Ssh sshRx = new Ssh("root", "barrow1admin_12", "10.211.129.244");
////			sshRx.loginToServer();
////			sshRx.sendCommand("cp /usr/local/syslog.log /usr/local/syslog.log_failOn" + (testCounter-1));
////			sshRx.disconnect();
////			}catch(Exception e2) {
////				log.info("Error logging into RX server");
////			}
////			
////			//tx
////			try {
////			Ssh sshTx = new Ssh("root", "barrow1admin_12", "10.211.129.243");
////			sshTx.loginToServer();
////			sshTx.sendCommand("cp /usr/local/syslog.log /usr/local/syslog.log_failOn" + (testCounter-1));
////			sshTx.disconnect();
////			}catch(Exception e3) {
////				log.info("Error logging into TX server");
////			}
//
//		}
//		
//		if(ITestResult.SUCCESS == result.getStatus())
//			log.info(result.getName() + " :PASS");
//		
//		if (ITestResult.FAILURE == result.getStatus() || ITestResult.SKIP == result.getStatus()) {
//			//String screenShotName = result.getName() + Utilities.getDateTimeStamp();
//			//Utilities.captureScreenShot(driver, screenShotName, result.getName());
//		}
//	}

	public void makeConnection(String connectionName, String connectionType) throws InterruptedException {
		log.info("Creating connection : " + connectionName);
		connections.addConnection(driver, connectionName, "no"); // connection name, user template
		connections.connectionInfo(driver, "tx", "user", "user", prop.getProperty("txIP")); // connection via, name, host ip
		connections.chooseCoonectionType(driver, connectionType); // connection type
		connections.enableExtendedDesktop(driver);
		connections.enablePersistenConnection(driver);
		if(connectionType.equals("private")) {
		   // connections.enableUSBRedirection(driver);
			//connections.enableAudio(driver);
		}
		connections.propertyInfoClickNext(driver);
		connections.saveConnection(driver, connectionName);
	}


	public void deviceManageTestPrepTest() throws InterruptedException {
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
//	@Override
//	@BeforeMethod(alwaysRun = true)
	//@Parameters({ "browser" })
//	public void login(Method method) throws InterruptedException {
//		splitTime = System.currentTimeMillis();
//		// Select driver based on the Browser parameter selected
//		//String url = "https://" + boxillaManager + "/";
//		//if (browser.equalsIgnoreCase("firefox")) {
//			/* *************************** Firefox Driver ********************************* */
//			System.setProperty("webdriver.gecko.driver", "C:\\Selenium\\Webdrivers\\geckodriver.exe");
//			DesiredCapabilities caps = new DesiredCapabilities();
//			caps.setCapability("acceptInsecureCerts", true); // Accepting insecure content
//			driver = new FirefoxDriver(caps);
//			driver.manage().window().maximize();
//			driver.get(url);
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

//		try {
//			Thread.sleep(2000);
//			printTestDetails("STARTING ", method.getName(), "");
//			Loginpage.username(driver).sendKeys(userName);
//
//			Thread.sleep(2000);
//			Loginpage.password(driver).sendKeys(password);
//
//			Thread.sleep(2000);
//			Loginpage.loginbtn(driver).click();
//			
//			//set timeout for webdriver
//			int timeout = Integer.parseInt(prop.getProperty("waitTime"));
//			driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
//		} catch (Exception e) {
//			log.info("Error in Login method");
//			driver.quit();
//		}

//	}

}
