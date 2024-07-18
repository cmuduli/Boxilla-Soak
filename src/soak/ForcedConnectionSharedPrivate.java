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

/**
 * Creates the following test
 * Setup - Creates a shared connection between RX1 and TX 1
 * 		 - Creates a private connection between RX2 and TX2
 * 
 * Test
 * Switches RX2 to the shared connection and then switches back 
 * to private
 *  
 * @author Boxilla
 *
 */
public class ForcedConnectionSharedPrivate extends StartupTestCase2 {

	final static Logger log = Logger.getLogger(ForcedConnection.class);
	private ConnectionsMethods connections = new ConnectionsMethods();
	private DevicesMethods device = new DevicesMethods();
	private DiscoveryMethods discoveryMethods = new DiscoveryMethods();
	private ArrayList<Integer> memoryAmountTx = new ArrayList<Integer>();
	private ArrayList<Integer> memoryAmountRx = new ArrayList<Integer>();
	private int testCounter= 1;
	String rx1 = "10.211.128.156";
	Ssh ssh;

	ForceConnect privateConnection = new ForceConnect();
	ForceConnect sharedConnection = new ForceConnect();
	//test properties
	private int timeout = 100000;
	private String privateConnectionName = "seSingleConPrivate";
	private String sharedConnectionName = "4k_Con";
	
	Ssh shellPrivate;
	Ssh shellShared;

	class ForceConnect {
		public String action = "";
		public String user = "";
		public String connection = "";
	}
	@Override
	@AfterMethod(alwaysRun = true)
	public void logout(ITestResult result) throws InterruptedException {
		// Taking screen shot on failure
		//String url = "https://" + boxillaManager + "/";
		Thread.sleep(2000);
		String results = "";
		//print result
		if(ITestResult.FAILURE == result.getStatus())
			results = "FAIL";
		
		if(ITestResult.SKIP == result.getStatus())
			results = "SKIP";
		
		if(ITestResult.SUCCESS == result.getStatus())
			results = "PASS";
		
		if (ITestResult.FAILURE == result.getStatus() || ITestResult.SKIP == result.getStatus()) {
			String screenShotName = result.getName() + Utilities.getDateTimeStamp();
			Utilities.captureScreenShot(driver, screenShotName, result.getName());
			System.exit(0);
			try {
			//Utilities.captureLog(boxillaManager, prop.getProperty("boxillaUserName"), prop.getProperty("boxillaPassword"),
			//		 "./test-output/Screenshots/LOG_" + result.getName() + Utilities.getDateTimeStamp() + ".txt");
			}catch(Exception e) {
				System.out.println("Error when trying to capture log file. Catching error and continuing");
				e.printStackTrace();
			}
		}
//		try {
//			Thread.sleep(1000);
//			driver.get(url);
//			Thread.sleep(2000);
//			Landingpage.logoutDropdown(driver).click();
//			Thread.sleep(2000);
//			Landingpage.logoutbtn(driver).click();
//			Thread.sleep(2000);
//			driver.quit();
//		
			
		
		printTestDetails("FINISHING", result.getName(), results);
//		System.out.println("Tests Completed:" + ++testCounter);
//		Ssh shell = new Ssh("root", "barrow1admin_12", prop.getProperty("rxIP"));
//		shell.loginToServer();
//		shell.sendCommand("reboot");
//		shell.disconnect();
//		Thread.sleep(60000);
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
	public void makeBreakSharedPrivate() throws InterruptedException {
		log.info("***** Starting test I want *************");
	String tx1 = "10.211.129.86";
	String rx2 = "10.211.129.216";
	String tx2 = "10.211.129.217";
	int time = 100;
	  String tx = "10.211.129.87";
	
	  privateConnection.action = "force_connection";
		privateConnection.user = "Boxilla";
		privateConnection.connection = privateConnectionName;
		
		sharedConnection.action = "force_connection";
		sharedConnection.user = "Boxilla";
		sharedConnection.connection = sharedConnectionName;
	
		
		System.out.println("joining 4k connection");
		int status = given().header(getHead()).body(sharedConnection)
				.when()
				.contentType(ContentType.JSON)
				.put("https://" + rx1 + ":8888/control/connections").andReturn().statusCode();		//make connection 
		Assert.assertTrue(status == 200, "status did not equal 200");
		System.out.println("RETURN code :" + status);
		System.out.println("Leaving connection up 3 minutes");
		Thread.sleep(180000);
		//Thread.sleep(5000);
				String isCloudRunning = ssh.sendCommand("ps -ax");
				//System.out.println("OUTPUT:" + isCloudRunning); 
				Assert.assertTrue(isCloudRunning.contains(sharedConnectionName), "Connection was not running ");
				//System.out.println("RX swapped to first shared con. Swapping to second shared con");
				log.info("starting private 2k/4k connection");
				int status2 = given().header(getHead()).body(privateConnection)
						.when()
						.contentType(ContentType.JSON)
						.put("https://" + rx1 + ":8888/control/connections").andReturn().statusCode();		//make connection 
					System.out.println("RETURN code :" + status2);
					System.out.println("Leaving connection up 3 minutes");
					Thread.sleep(180000);
					//Thread.sleep(5000);
						String isCloudRunning2 = ssh.sendCommand("ps -ax");
						Assert.assertTrue(isCloudRunning2.contains(privateConnectionName), "Connection was not running ");
						System.out.println("RX swapped to private. Test complete");
				
		System.out.println("Test Count:" + testCounter);
		testCounter++;
	
//		for(int j=0; j < 25; j++) {
		String json = "{\"action\": \"terminate_connection\"}";
//		System.out.println(prop.getProperty("rxIP"));
//		int status2 = given().header(getHead()).body(json)			//terminate
//				.when()
//				.contentType(ContentType.JSON)
//				.put("https://" + rx1 + ":8888/control/connections").andReturn().statusCode();
//	
//		Assert.assertTrue(status2 == 200, "terminate status did not equal 200");
//		log.info("Connection terminated. Checking");
//		Thread.sleep(5000);
//		String isCloudRunning2 = ssh.sendCommand("ps -ax");
//		//System.out.println("OUTPUT:" + isCloudRunning); 
//		Assert.assertFalse(isCloudRunning2.contains("brendanTest"), "Connection was running ");
	}
//		
//		Thread.sleep(time);					//wait time
//		int status = given().header(getHead()).body(privateConnection)
//		.when()
//		.contentType(ContentType.JSON)
//		.put("https://" + rx + ":8888/control/connections").andReturn().statusCode();		//make connection 
//		Thread.sleep(5000);
//		String isCloudRunning = ssh.sendCommand("ps -ax");
//		
//		if(isCloudRunning.contains("/usr/bin/dfreerdp")) {
//			System.out.println("Connection running");
//		}else {
//			System.out.println("Connection NOT running");
//		}
//		//System.out.println(isCloudRunning);
//		if(isCloudRunning.contains("/apps/bin/cloudgui")) {
//			System.out.println("FAIL on " + time);
//		}else {
//			System.out.println("Pass on " + time);
//		}
////		if(j >= 10 && j < 20) {
////			time = 3000;
////		}else if(j >= 20 && j <30) {
////			time = 2000;
////		}else if(j >= 30 && j < 40) {
////			time = 1000;
////		}else if (j >= 40) {
////			time = 300;
////		}
//		
//		}
////		log.info("Status code from terminate_connection on private connection:" + status2);
////		
////		Thread.sleep(10000);
//	//	log.info("adding to shared connection");
////		int status = given().header(getHead()).body(privateConnection)
////				.when()
////				.contentType(ContentType.JSON)
////				.put("https://" + rx + ":8888/control/connections").andReturn().statusCode();
////		System.out.println("Status code from shared Connection:" + status);
////		log.info("waiting one minute");
////		Thread.sleep(60000);
////		log.info("Checking if private receiver was added to shared connection");
////		String outputShared = shellPrivate.sendCommand("ps -ax");
////		Assert.assertTrue(outputShared.contains(sharedConnectionName), "Private receiver was not added to shared connection");
////		log.info("Private receiver was added to shared connection");
//////		log.info("Breaking from shared");
//////		String json1 = "{\"action\": \"terminate_connection\"}";
//////		System.out.println(prop.getProperty("rxIP"));
//////		int status5 = given().header(getHead()).body(json)
//////				.when()
//////				.contentType(ContentType.JSON)
//////				.put("https://" + prop.getProperty("rxIP") + ":8888/control/connections").andReturn().statusCode();
//////		log.info("Status code from terminate_connection on private connection:" + status5);
////		
////		
//////		Thread.sleep(10000);
////		log.info("connecting to private");
////		int status3 = given().header(getHead()).body(privateConnection)
////				.when()
////				.contentType(ContentType.JSON)
////				.put("https://" + "10.211.129.86" + ":8888/control/connections").andReturn().statusCode();
////		System.out.println("Status code from private Connection:" + status3);
////		log.info("waiting one minute");
////		Thread.sleep(60000);
////		log.info("Checking if private receiver was added back to private connection");
////		String privateOutput = shellPrivate.sendCommand("ps -ax");
////		Assert.assertTrue(privateOutput.contains(privateConnectionName), "private receiver was not added back to private connection");
////		log.info("Receiver was added to back to private connection");
////		log.info("**************** test finished **************");
//		
//	}
//	
//	
////	@Test
////	public void makeBreakForcedConnectionRest() throws InterruptedException {
////		log.info("***** makeBreakForcedConnection *****");
////		log.info("Test count: " + testCounter);
////		testCounter++;
////		ForceConnect con = new ForceConnect();
////		con.action = "force_connection";
////		con.user = "Boxilla";
////		con.connection = "TX_14";
////		int status = given().header(getHead()).body(con)
////				.when()
////				.contentType(ContentType.JSON)
////				.put("https://10.211.128.156:8888/control/connections").andReturn().statusCode();
////		System.out.println("Status code:" + status);
////		
////		Thread.sleep(15000);
////		log.info("Sleeping for 15 seconds");
////		String json = "{\"action\": \"terminate_connection\"}";
////		System.out.println(prop.getProperty("rxIP"));
////		int status2 = given().header(getHead()).body(json)
////				.when()
////				.contentType(ContentType.JSON)
////				.put("https://10.211.129.156:8888/control/connections").andReturn().statusCode();
////		log.info("Status code from terminate_connection:" + status2);
////		System.out.println(prop.getProperty("rxIP"));
////		Thread.sleep(5000);
////		log.info("waking...");
////		////getMemory();
////					//	printMemoryDetails();
//		
//	}
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

	@AfterClass(alwaysRun=true)
	public void afterClass() {
		try {
//			Thread.sleep(60000);
//			log.info("Getting final stats");
//			//printMemoryDetails();
//			cleanUpLogin();
//			device.unManageDevice(driver, prop.getProperty("rxIP"));
//			device.unManageDevice(driver, prop.getProperty("txIP"));
//			cleanUpLogout();
		}catch(Exception | AssertionError e) {
//			cleanUpLogout();
		}
	}
	
	public void loginToReceivers() {
		shellPrivate = new Ssh("root", "barrow1admin_12", "10.211.129.86");
		shellShared = new Ssh("root", "barrow1admin_12", "10.211.129.245");
		
		shellPrivate.loginToServer();
		shellShared.loginToServer();
		
	}
	

	@Override
	@BeforeClass(alwaysRun = true)
	public void beforeClass() throws InterruptedException {
		printSuitetDetails(false);
		getDevices();
//		try {	
			RestAssured.authentication = basic("rest_user", "18_22_33_AA");			//REST authentication
			RestAssured.useRelaxedHTTPSValidation();
			 ssh = new Ssh("root", "barrow1admin_12", rx1);
			  ssh.loginToServer();
//			cleanUpLogin();
//			makeConnection(privateConnectionName, "private", prop.getProperty("txIP"));
//			makeConnection(sharedConnectionName, "shared", prop.getProperty("txIP2"));
//			deviceManageTestPrepTest2();
			//Thread.sleep(30000);

//			loginToReceivers();
//			//set up initial private and shared connection
//			
//			privateConnection.action = "force_connection";
//			privateConnection.user = "Boxilla";
//			privateConnection.connection = privateConnectionName;
//			
//			sharedConnection.action = "force_connection";
//			sharedConnection.user = "Boxilla";
//			sharedConnection.connection = sharedConnectionName;
//			
//			
//			int status = given().header(getHead()).body(privateConnection)
//					.when()
//					.contentType(ContentType.JSON)
//					.put("https://" + "10.211.129.86" + ":8888/control/connections").andReturn().statusCode();
//			System.out.println("Status code Private Connection:" + status);
//			
//			
//			int statusShared = given().header(getHead()).body(sharedConnection)
//					.when()
//					.contentType(ContentType.JSON)
//					.put("https://" + "10.211.129.245" + ":8888/control/connections").andReturn().statusCode();
//			System.out.println("Status code shared Connection:" + statusShared);
//			
//			Thread.sleep(60000);
//			log.info("Checking if private connectino is up");
//			String privateOutput = shellPrivate.sendCommand("ps -ax");
//			Assert.assertTrue(privateOutput.contains(privateConnectionName), "Initial private connection was not made");
//			log.info("private connectino is up");
//			log.info("Checking if shared connectino is up");
//			String sharedOutput = shellShared.sendCommand("ps -ax");
//			Assert.assertTrue(sharedOutput.contains(sharedConnectionName), "Initial shared connection was not made");
//			log.info("Shared connectino is up");
//			
//			
////			JsonPath path = 
////					given().header(getHead())
////					.when()
////					.get(getHttp() + "://" + prop.getProperty("rxIP") + getPort() + "/statistics/facts/system_facts").jsonPath();
////					int freeMemory = path.get("kvm_system_facts.'free memory'[0]");	
////					memoryAmountRx.add(freeMemory);
////					System.out.println("MEMORY RX:" + freeMemory);
////					
////					JsonPath pathTx = 
////							given().header(getHead())
////							.when()
////							.get(getHttp() + "://" + prop.getProperty("txIP") + getPort() + "/statistics/facts/system_facts").jsonPath();
////							int freeMemoryTx = pathTx.get("kvm_system_facts.'free memory'[0]");	
////							memoryAmountTx.add(freeMemoryTx);
////							System.out.println("MEMORY TX:" + freeMemoryTx);
//					
//			
//		//	Thread.sleep(timeout);
//		}catch(Exception | AssertionError e) {
//			Utilities.captureScreenShot(driver, this.getClass().getName() + "_beforeClass", "Before Class");
//			e.printStackTrace();
//			//cleanUpLogout();
//		}
//	//	cleanUpLogout();
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

	public void makeConnection(String connectionName, String connectionType, String ip) throws InterruptedException {
		log.info("Creating connection : " + connectionName);
		connections.addConnection(driver, connectionName, "no"); // connection name, user template
		connections.connectionInfo(driver, "tx", "user", "user", ip); // connection via, name, host ip
		connections.chooseCoonectionType(driver, connectionType); // connection type
//		connections.enableExtendedDesktop(driver);
//		connections.enablePersistenConnection(driver);
		if(connectionType.equals("private")) {
		   // connections.enableUSBRedirection(driver);
			//connections.enableAudio(driver);
		}
		connections.propertyInfoClickNext(driver);
		connections.saveConnection(driver, connectionName);
	}


	public void deviceManageTestPrepTest2() throws InterruptedException {
		log.info("Test Preparation Manage Device");

		//RX
		discoveryMethods.discoverDevices(driver);
		discoveryMethods.stateAndIPcheck(driver, prop.getProperty("rxMac"), prop.getProperty("ipCheck"), prop.getProperty("rxIP"),
				prop.getProperty("gateway"), prop.getProperty("netmask"));
		discoveryMethods.manageApplianceAutomatic(driver, prop.getProperty("rxIP"), prop.getProperty("rxMac"),
				prop.getProperty("ipCheck"));


		//RX
		discoveryMethods.discoverDevices(driver);
		discoveryMethods.stateAndIPcheck(driver, prop.getProperty("rxMac2"), prop.getProperty("ipCheck"), prop.getProperty("rxIP2"),
				prop.getProperty("gateway"), prop.getProperty("netmask"));
		discoveryMethods.manageApplianceAutomatic(driver, prop.getProperty("rxIP2"), prop.getProperty("rxMac2"),
				prop.getProperty("ipCheck"));

		//TX
		discoveryMethods.discoverDevices(driver);
		discoveryMethods.stateAndIPcheck(driver, prop.getProperty("txMac"), prop.getProperty("ipCheck"),
				prop.getProperty("txIP"), prop.getProperty("gateway"), prop.getProperty("netmask"));

		discoveryMethods.manageApplianceAutomatic(driver, prop.getProperty("txIP"), prop.getProperty("txMac"), 
				prop.getProperty("ipCheck"));
		
		//TX
		discoveryMethods.discoverDevices(driver);
		discoveryMethods.stateAndIPcheck(driver, prop.getProperty("txMac2"), prop.getProperty("ipCheck"),
				prop.getProperty("txIP2"), prop.getProperty("gateway"), prop.getProperty("netmask"));

		discoveryMethods.manageApplianceAutomatic(driver, prop.getProperty("txIP2"), prop.getProperty("txMac2"), 
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
	@Override
	@BeforeMethod(alwaysRun = true)
	//@Parameters({ "browser" })
	public void login(Method method) {
		
	}
	

}
