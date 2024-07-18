package connection;



import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jetty.util.log.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import extra.SeleniumActions;
import extra.StartupTestCase;
import extra.StartupTestCase2;
import methods.ConnectionsMethods;
import methods.DevicesMethods;
import objects.Connections;
import objects.Landingpage;
import testNG.Utilities;
/**
 * Class that contains tests for Forced Connection in Boxilla
 * @author Brendan O Regan
 *
 */

public class ForcedConnection extends StartupTestCase {
	
	final static Logger log = Logger.getLogger(ForcedConnection.class);
	private ConnectionsMethods connections = new ConnectionsMethods();
	private DevicesMethods device = new DevicesMethods();
		
	//test properties
	private String privateConnectionName = "privateTest";
	private String sharedConnectionName = "sharedTest";
	private String presetName = "testPreset1";
	private String snapshotName = "Test_Snapshot";
	

	
	/**
	 * Uses boxilla to create a private connection and asserts if the connection has been created
	 * @throws InterruptedException
	 */
	@Test(groups = {"boxillaFunctional", "integration", "smoke", "emerald", "chrome", "quick"})
	public void test01_makePrivateConnection() throws InterruptedException {
		log.info("***** test01_makePrivateConnection *****");
		String[] connectionSources = {privateConnectionName};
		log.info("Attempting to add soruces");
		connections.addSources(driver, connectionSources);
		log.info("Sources added. Trying to add private destination");
		connections.addPrivateDestination(driver, privateConnectionName, singleRxName);
		log.info("Private destination added. Sleeping and refreshing page");
		Thread.sleep(65000);				//wait for boxilla to poll again and make sure the connection is actually up and running
		driver.navigate().refresh();
		Thread.sleep(5000);
		log.info("Checking if connection is active by asserting on the connections > viewer UI");
		boolean isConnection = Connections.singleSourceDestinationCheck(driver, privateConnectionName, singleRxName).isDisplayed();
		//Assert connection has been created
		log.info("Asserting if connections has been created between " + privateConnectionName + " and " + singleRxName);
		Assert.assertTrue(isConnection, "Connection between " + privateConnectionName + " and " + singleRxName + " is not displayed");	
		log.info("Assertion passed. Test finished");
	}
	
	/**
	 * Breaks a private connection in Boxilla
	 * @throws InterruptedException
	 */
	@Test(groups = {"boxillaFunctional", "integration", "emerald", "chrome"})
	public void test02_breakConnection() throws InterruptedException {
		log.info("***** test02_breakConnection *****");
		log.info("Making connection first");
		String[] connectionSources = {privateConnectionName};
		log.info("Attempting to add soruces");
		connections.addSources(driver, connectionSources);
		log.info("Sources added. Trying to add private destination");
		connections.addPrivateDestination(driver, privateConnectionName, singleRxName);
		log.info("Private destination added. Sleeping and refreshing page");
		Thread.sleep(65000);				//wait for boxilla to poll again and make sure the connection is actually up and running
		driver.navigate().refresh();
		Thread.sleep(5000);
		log.info("Checking if connection is active by asserting on the connections > viewer UI");
		boolean isConnection = Connections.singleSourceDestinationCheck(driver, privateConnectionName, singleRxName).isDisplayed();
		//Assert connection has been created
		log.info("Asserting if connections has been created between " + privateConnectionName + " and " + singleRxName);
		Assert.assertTrue(isConnection, "Connection between " + privateConnectionName + " and " + singleRxName + " is not displayed");	
		
		log.info("Attempting to break connection in connection > viewer");
		connections.breakConnection(driver, privateConnectionName);
		log.info("Connection should be broken. Sleeping and refreshing page");
		Thread.sleep(65000);				//wait for boxilla to poll again and make sure the connection is actually up and running
		driver.navigate().refresh();
		Thread.sleep(3000);
		log.info("Checking that connection is not displayed in connections > viewer");
		boolean isNonActiveSource = Connections.nonActiveSource(driver).isDisplayed();
		boolean isNonActiveDestination = Connections.nonActiveDestination(driver).isDisplayed();
		log.info("Asserting if connection has been broken");
		Assert.assertTrue(isNonActiveSource && isNonActiveDestination, "Connection has not been broken");
		log.info("rebooting devices incase connection was not broken");

	}
	
	/**
	 * Creates a shared connection in boxilla
	 * @throws InterruptedException
	 */
	//@Test(groups = {"boxillaFunctional", "integration", "notEmerald"})
	public void test03_makeSharedConnection() throws InterruptedException {
 		log.info("***** test03_makeSharedConnection *****");
		String[] connectionSources = {sharedConnectionName};
		String[] destinations = {singleRxName, dualRxName};
		log.info("Adding sources");
		connections.addSources(driver, connectionSources);
		log.info("Sources added. Trying to add destinations");
		connections.addSharedDestination(driver, sharedConnectionName, destinations);
		log.info("Destinations added. Sleeping for 65 seconds and refreshing..");
		Thread.sleep(65000);				//wait for boxilla to poll again and make sure the connection is actually up and running
		driver.navigate().refresh();
		Thread.sleep(5000);
		log.info("Checking if shared connection is up in connection > viewer");
		
		
		boolean source = Connections.matrixItem(driver, sharedConnectionName).isDisplayed();
		boolean destination1 = Connections.matrixItem(driver, singleRxName).isDisplayed();
		boolean destination2 = Connections.matrixItem(driver, dualRxName).isDisplayed();
		boolean check = source && destination1 && destination2;
		log.info("Asserting if shared connection has been established");
		Assert.assertTrue(check, "Source and all destinations were not displayed");
		
		
		
//		boolean source = Connections.matrixItem(driver, sharedConnectionName).isDisplayed();
//		boolean destination1 = Connections.matrixItem(driver, singleRxName).isDisplayed();
//		boolean destination2 = Connections.matrixItem(driver, dualRxName).isDisplayed();
//		boolean check = source && destination1 && destination2;
//		log.info("Checking source");
//		Assert.assertTrue(source, "Source was not displayed in UI");
//		log.info("Source was displayed. SAsserting if destination 1 has is displayed in UI");
//		Assert.assertTrue(destination1, "destination 1 was not displayed");
//		log.info("Destination 1 was displayed. Checking for destination 2");
//		Assert.assertTrue(destination2, "destination 2 was not displayed");
		log.info("All connection elements displayed. Test passed");
		//break connection for next test
		log.info("Removing connection for next test");

	}
	
	
	//NEED TO WRITE A TEST SO THAT SHARED CONNECTION DOES NOT WORK ON EMERALD
	
	/**
	 * Creates a connection preset
	 * @throws InterruptedException
	 */
	@Test(groups = {"boxillaFunctional", "integration", "emerald"})
	public void test04_createPreset() throws InterruptedException {
		log.info("***** test04_createPreset *****");
		String[] sourceList = {privateConnectionName};
		String[] destinationList = {singleRxName};
		connections.createPreset(driver, sourceList, destinationList, presetName, false);
		log.info("Asserting if preset has been created by checking if the preset button for " + presetName + " is displayed");
		Assert.assertTrue(Connections.getPresetBtn(driver, presetName).isDisplayed(), "Button with preset name is not displayed, button name : " + presetName);	
		log.info("Assert passed. Test passed");
			
	}
	
	/**
	 * Applies a preset and creates a connection 
	 * @throws InterruptedException
	 */
	@Test(groups = {"boxillaFunctional", "integration", "emerald"})
	public void test05_applyPreset() throws InterruptedException {
		log.info("***** test05_applyPreset *****");
		connections.navigateToConnectionViewer(driver);
		log.info("Clicking preset button: " + presetName);
		Connections.getPresetBtn(driver, presetName).click();
		log.info("sleeping and refreshing page");
		Thread.sleep(65000);
		driver.navigate().refresh();
		Thread.sleep(5000);
		log.info("Waking and chekcing if connection elements are displayed");
		boolean isConnection = Connections.singleSourceDestinationCheck(driver, privateConnectionName, singleRxName).isDisplayed();
		//Assert connection has been created
		log.info("Asserting if preset has been applied by looking for the source and destination in the viewer");
		Assert.assertTrue(isConnection, "Connection between " + privateConnectionName + " and " + singleRxName + " is not displayed");	
		log.info("Assert passed");
		//break connection for next test
		log.info("Breaking connection for next test");
	}
	
	/**
	 * Edit a presets name
	 * @throws InterruptedException
	 */
	@Test(groups = {"boxillaFunctional", "integration", "emerald"})
	public void test06_editPresetName() throws InterruptedException {
		log.info("***** test06_editPresetName *****");
		connections.editPresetName(driver, presetName, "new preset");
		connections.timer(driver);
		boolean originalPreset = Connections.getPresetBtn(driver, presetName).isDisplayed();
		boolean newPreset = Connections.getPresetBtn(driver, "new preset").isDisplayed();
		log.info("Asserting if new preset with name: new preset and old preset with name:" + presetName + " have been created");
		Assert.assertTrue(originalPreset && newPreset, "Both preset buttons were not displayed");
		log.info("Assert passed");
	}
	/**
	 * Deletes a preset
	 * @throws InterruptedException
	 */
	@Test(groups = {"boxillaFunctional", "integration", "emerald"})
	public void test07_deletePreset() throws InterruptedException {
		log.info("***** test07_deletePreset *****");
		connections.deletePreset(driver, presetName);
		connections.deletePreset(driver, "new preset");		//delete preset from previous test
		List<WebElement> elements = driver.findElements(By.xpath(Connections.getPresetButtonXpath(presetName)));
		log.info("Asserting if preset has been deleted by checking the button is not displayed");
		Assert.assertTrue(elements.isEmpty(), "Preset button still exists");	
		log.info("Assert passed");
	}
	
	/**
	 * Saves a connection as a snapshot
	 * @throws InterruptedException
	 */
	@Test(groups = {"boxillaFunctional", "integration", "emerald"})
	public void test08_saveSnapshot() throws InterruptedException {
		log.info("***** test08_saveSnapshot *****");
		//make the connection to save
		String[] connectionSources = {privateConnectionName};
		log.info("adding sources");
		connections.addSources(driver, connectionSources);
		log.info("Sources added, adding destination");
		connections.addPrivateDestination(driver, privateConnectionName, singleRxName);
		log.info("Destination added. Sleeping for 65 seconds and refreshing page");
		Thread.sleep(65000);
		driver.navigate().refresh();
		Thread.sleep(5000);
		//save connection as snapshot
		log.info("trying to save connection as snapshot");
		connections.saveSnapshot(driver, snapshotName, false);
		log.info("Connection saved. Checking if elements are displayed in IU");
		boolean snapShot = Connections.getPresetBtn(driver, snapshotName).isDisplayed();
		log.info("Asserting if snapshot has been created by checking the button is displayed");
		Assert.assertTrue(snapShot, "Snap shot was not saved as a preset");
		log.info("Assert passed. Breaking connection for next test");

	}
	
	/**
	 * Applies a snapshot and creates a connection
	 * @throws InterruptedException
	 */
	@Test(groups = {"boxillaFunctional", "integration", "emerald"})
	public void test09_applySnapshot() throws InterruptedException {
		log.info("***** test09_applySnapshot *****");
		connections.navigateToConnectionViewer(driver);
		log.info("Applying snapshot: " + snapshotName);
		Connections.getPresetBtn(driver, snapshotName).click();
		log.info("Snapshot applied. Sleeping and refreshing..");
		Thread.sleep(65000);
		driver.navigate().refresh();
		Thread.sleep(5000);
		log.info("Waking and checking if connection is displayed on UI");
		boolean isConnection = Connections.singleSourceDestinationCheck(driver, privateConnectionName, singleRxName).isDisplayed();
		//Assert connection has been created
		Assert.assertTrue(isConnection, "Connection between " + privateConnectionName + " and " + singleRxName + " is not displayed");	
		//break connection for next test
		log.info("Assert passed.. breaking connectgion for next test");
		device.rebootDeviceSSH(rxIp, deviceUserName, devicePassword, 0);
		device.rebootDeviceSSH(rxIpDual,deviceUserName, devicePassword, 100000);
		log.info("Connections broken.. deleting snapshot");
		connections.deletePreset(driver, snapshotName);
	
	}
	
	/**
	 * Applies a partial preset and checks if both connections are up
	 * @throws InterruptedException
	 */
	//@Test(groups = {"boxillaFunctional", "integration", "emerald", "notEmerald"})
	public void test10_applyPartialPreset() throws InterruptedException {
		log.info("***** test10_applyPartialPreset *****");
		//create preset
		String[] sourceList = {privateConnectionName};
		String[] destinationList = {singleRxName};
		connections.createPreset(driver, sourceList, destinationList, "partialPreset", true);
		//create shared connection
		String[] connectionSources = {sharedConnectionName};
		String[] destinations = {singleRxName, dualRxName};
		connections.addSources(driver, connectionSources);
		connections.addSharedDestination(driver, sharedConnectionName, destinations);
		Thread.sleep(5000);
		
		//apply preset
		connections.navigateToConnectionViewer(driver);
		log.info("Clicking preset.");
		Connections.getPresetBtn(driver, "partialPreset").click();
		log.info("Preset clicked. Waiting 65 seconds and refreshing page");
		Thread.sleep(65000);
		driver.navigate().refresh();
		Thread.sleep(5000);
		log.info("Waking and checking if both connections are in the UI");
		boolean original = Connections.singleSourceDestinationCheck(driver, sharedConnectionName, dualRxName).isDisplayed();
		boolean newConnection = Connections.singleSourceDestinationCheck(driver, privateConnectionName, singleRxName).isDisplayed();
		log.info("Asserting that both the original and new connections are displayed");
		Assert.assertTrue(original && newConnection, "Partial preset failed");
		
		device.rebootDeviceSSH(rxIp,deviceUserName, devicePassword, 0);
		device.rebootDeviceSSH(rxIpDual, deviceUserName, devicePassword, 100000);
//		connections.breakConnection(driver, privateConnectionName);
//		connections.breakConnection(driver, sharedConnectionName);
		connections.deletePreset(driver, "partialPreset");
	}
	
	/**
	 * Applies a full preset and checks only one connection is up
	 * @throws InterruptedException
	 */
	//@Test(groups = {"boxillaFunctional", "integration", "emerald", "notEmerald"})
	public void test11_applyFullPreset() throws InterruptedException {
		//create preset
		String[] sourceList = {privateConnectionName};
		String[] destinationList = {singleRxName};
		connections.createPreset(driver, sourceList, destinationList, "fullPreset", false);
		//create shared connection
		String[] connectionSources = {sharedConnectionName};
		String[] destinations = {singleRxName, dualRxName};
		log.info("Adding sources");
		connections.addSources(driver, connectionSources);
		log.info("Sources added. Trying to add destination");
		connections.addSharedDestination(driver, sharedConnectionName, destinations);
		log.info("Destination added. Sleeping for 65 seconds and refreshing page");
		Thread.sleep(65000);
		driver.navigate().refresh();
		Thread.sleep(5000);
		log.info("Waking, attempting to apply full preset.");
		//apply preset
		connections.navigateToConnectionViewer(driver);
		Connections.getPresetBtn(driver, "fullPreset").click();
		log.info("Full preset applied. Sleeping for 65 seconds and refreshing page");
		Thread.sleep(65000);
		driver.navigate().refresh();
		Thread.sleep(5000);
		log.info("Waking and checking full preset is in UI");
		boolean newConnection = Connections.singleSourceDestinationCheck(driver, privateConnectionName, singleRxName).isDisplayed();
		Assert.assertTrue(newConnection, "Full preset failed");
		log.info("Assert passed.. cleaning up connection");
		device.rebootDeviceSSH(rxIp, deviceUserName, devicePassword, 0);
		device.rebootDeviceSSH(rxIpDual,deviceUserName, devicePassword, 100000);
		log.info("Connection broken. Deleting preset");
		connections.deletePreset(driver, "fullPreset");
	}
//	
//	//ISSUE WITH MENU BUG 5016
////	@Test
////	public void contextMenu() throws InterruptedException {
////		String[] connectionSources = {privateConnectionName};
////		connections.addSources(driver, connectionSources);
////		connections.addPrivateDestination(driver, privateConnectionName, singleRxName);
////		Thread.sleep(5000);
////		//connections.navigateToConnectionViewer(driver);
////		connections.timer(driver);
////		Connections.matrixItem(driver, privateConnectionName).click();
////		boolean audioCheck = Connections.sourceContextMenuAudio(driver, true).isDisplayed();
////		Assert.assertTrue(audioCheck, "blah");
////	}
////	
	/**
	 * Checks the number of receivers online
	 * @throws InterruptedException
	 */
	//@Test(groups = {"boxillaFunctional", "integration"})
	public void test12_checkReceiverStatus() throws InterruptedException {
		connections.navigateToConnectionViewer(driver);
		Assert.assertTrue(Connections.numberOfOnlineReceivers(driver, "2").isDisplayed(), "Number of online reeivers is wrong");	
	}
	/**
	 * Checks the number of active connections
	 * @throws InterruptedException
	 */
	//@Test(groups = {"boxillaFunctional", "integration", "emerald"})
	public void test13_checkActiveConnections() throws InterruptedException {
		String[] connectionSources = {privateConnectionName};
		log.info("adding sources");
		connections.addSources(driver, connectionSources);
		log.info("Sources added. Trying to add private destination");
		connections.addPrivateDestination(driver, privateConnectionName, singleRxName);
		log.info("Private destination added. Sleeping for 65 seconds and refreshing page");
		Thread.sleep(65000);
		driver.navigate().refresh();
		Thread.sleep(5000);
		log.info("Waking and checking if connection is displayed in UI");
		Assert.assertTrue(Connections.numberOfActiveConnections(driver, "1").isDisplayed(), "Incorrect number of active connections");
		log.info("Assert passed. Breaking connection");
	}
	/**
	 * Rearranges the presets in the list
	 * @throws InterruptedException
	 */
	
	//NEEDS TO BE LOOKED AT
	//@Test(groups = {"boxillaFunctional", "integration", "emerald"})
	public void test14_rearrangePresets() throws InterruptedException {
		
		//make 3 presets
		String[] sourceList = {privateConnectionName};
		String[] destinationList = {singleRxName};
		connections.createPreset(driver, sourceList, destinationList, "fullPreset1", false);
		connections.createPreset(driver, sourceList, destinationList, "fullPreset2", false);
		connections.createPreset(driver, sourceList, destinationList, "fullPreset3", false);
		
		connections.navigateToConnectionViewer(driver);
		Connections.managePresetsBtn(driver).click();
		Thread.sleep(2000);
		SeleniumActions.dragAndDrop(driver, Connections.managePresetsListItem(driver, "3"), Connections.managePresetsListItem(driver, "1"));
		Thread.sleep(2000);
		Connections.managePresetsBtn(driver).click();
		String check = Connections.managePresetsListItem(driver, "1").getText();
		connections.timer(driver);
		Connections.createPresetCloseModal(driver).click();
		Assert.assertTrue(check.equals("fullPreset3"), "fullPreset3 has not moved to the top of the list");
		driver.navigate().refresh();
		connections.deletePreset(driver, "fullPreset1");
		connections.deletePreset(driver, "fullPreset2");
		connections.deletePreset(driver, "fullPreset3");
	}
		

	/**
	 * Overriding superclass method to make connections for this suite of tests
	 */
	@BeforeClass(alwaysRun = true)
	public void beforeClass() throws InterruptedException {
		printSuitetDetails(false);
		getDevices();
		
		if(StartupTestCase.isEmerald) {
			singleRxName = "Test_RX_Emerald";
		}
		
		try {	
			cleanUpLogin();
			connections.createTxConnection(privateConnectionName, "private", driver, txIp);
			connections.createTxConnection(sharedConnectionName, "shared", driver, txIp);
			device.recreateCloudData(rxIp, rxIpDual);
			device.rebootDeviceSSH(rxIp,deviceUserName, devicePassword, 0);
			log.info("Sleep while devices reboot...");
			device.rebootDeviceSSH(rxIpDual, deviceUserName, devicePassword, 100000);
		}catch(Exception | AssertionError e) {
			Utilities.captureScreenShot(driver, this.getClass().getName() + "_beforeClass", "Before Class");
			e.printStackTrace();
			cleanUpLogout();
		}
		cleanUpLogout();
	}

	
	/**
	 * Runs after each test unless overridden. Gets the results of each test and logs it. Also captures a screen shot 
	 * if a test has failed. Logs out of boxilla and closes the webdriver
	 * @param result - injected into method by testng. Used to get the test result.
	 * @throws InterruptedException
	 */
	@Override
	@AfterMethod(alwaysRun = true)
	public void logout(ITestResult result) throws InterruptedException {
		// Taking screen shot on failure
		//String url = "https://" + boxillaManager + "/";
		log.info("Rebooting devices to clean up");
		device.rebootDeviceSSH(rxIp, deviceUserName, devicePassword, 0);
		device.rebootDeviceSSH(rxIpDual, deviceUserName, devicePassword, 100000);
		Thread.sleep(2000);
		String results = "";
		//print result
		if(ITestResult.FAILURE == result.getStatus()) {
			results = "FAIL";
			
		}
		
		if(ITestResult.SKIP == result.getStatus())
			results = "SKIP";
		
		if(ITestResult.SUCCESS == result.getStatus())
			results = "PASS";
		
		if (ITestResult.FAILURE == result.getStatus() || ITestResult.SKIP == result.getStatus()) {
			String screenShotName = result.getName() + Utilities.getDateTimeStamp();
			Utilities.captureScreenShot(driver, screenShotName, result.getName());
			
			Throwable failReason = result.getThrowable();
			log.info("FAIL REASON:" + failReason.toString());
			
			try {
			//Utilities.captureLog(boxillaManager, boxillaUsername, boxillaPassword,
			//		 "./test-output/Screenshots/LOG_" + result.getName() + Utilities.getDateTimeStamp() + ".txt");
			}catch(Exception e) {
				System.out.println("Error when trying to capture log file. Catching error and continuing");
				e.printStackTrace();
			}
		}
		try {
			Thread.sleep(1000);
			driver.get(url);
			Thread.sleep(2000);
			Landingpage.logoutDropdown(driver).click();
			Thread.sleep(2000);
			Landingpage.logoutbtn(driver).click();
			Thread.sleep(2000);
			driver.quit();
			long endTime = System.currentTimeMillis();
			//long duration = endTime - startTime;
			//System.out.println("Regression running for : " + getTimeFromMilliSeconds(duration));
		//	long singleTestTime = endTime - splitTime;
			//System.out.println(result.getName() + " took : " + getTimeFromMilliSeconds(singleTestTime));
			
		} catch (Exception e) {
			// TODO: handle exception
			driver.quit();
		}
		printTestDetails("FINISHING", result.getName(), results);
		//System.out.println("Tests Completed:" + ++testCounter);
	}
}
