package soak;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import extra.StartupTestCase;
import extra.StartupTestCase2;
import methods.ConnectionsMethods;
import methods.SystemMethods;
import methods.UsersMethods;
import objects.Users;

public class BoxillaUpgrade extends StartupTestCase2 {
	private SystemMethods methods = new SystemMethods();
	final static Logger log = Logger.getLogger(BoxillaUpgrade.class);
	private ConnectionsMethods conMethods = new ConnectionsMethods();
	private UsersMethods userMethods = new UsersMethods();
	private static int connectionNumber = 0;
	private String connectionName = "upgradeDowngradeCon";
	private String userName = "upgradeDowngradeUser";
	
	public BoxillaUpgrade() throws IOException {
		super();
	}

//	@Test(retryAnalyzer = testNG.RetryAnalyzer.class)
	@Parameters({ "release1", "release2" })
	public void dependencyVersionUpload(String release1, String release2) throws InterruptedException {
		log.info("Test Preparation - Upload Versions");
		methods.navigateToSystemAdmin(driver);
		methods.versionUpload(driver, release1);
		methods.versionUpload(driver, release2);
		log.info("Upload version - Test Preparation Completed");
	}

	@Test//(dependsOnMethods = { "dependencyVersionUpload" }) // Upgrade-downgrade boxilla
	@Parameters({ "release1","release2" })
	public void versionUpgrade(String release1, String release2) throws InterruptedException {
		cleanUpLogin();
		log.info("Soak Test Case-05 Started - Upgrade Version");
		methods.systemUpgradeSoak(driver, release1, release2, prop.getProperty("boxillaManager"));
		log.info("Upgrade Complete - Creating Connection");
		connectionNumber ++;
		createConnection();
		createUser();
		log.info("Upgrade complete. User and connection created");
	}
	
	private void createUser() throws InterruptedException {
		userMethods.addUser(driver, userName + connectionNumber);

		Thread.sleep(2000);
		Users.userPrivilegeAdmin(driver).click();
		log.info("User Privilege - Administrator selected");

		userMethods.addUserNoTemplateAutoConnectOFF(driver, userName + connectionNumber);
	}
	private void createConnection() throws InterruptedException {
		log.info("Test Case-13 Started - Add Connection Via TX, Private, All option enabled");
		conMethods.addConnection(driver, connectionName + connectionNumber, "no"); // connection name, user template
		conMethods.connectionInfo(driver, "tx", "user","user", txIp); // connection via, name, host ip
		conMethods.chooseCoonectionType(driver, "private"); // connection type
		conMethods.enableExtendedDesktop(driver);
		conMethods.enableUSBRedirection(driver);
		conMethods.enableAudio(driver);
		conMethods.enablePersistenConnection(driver);
		conMethods.propertyInfoClickNext(driver);
		conMethods.saveConnection(driver, connectionName + connectionNumber); // Connection name to assert
	}
	
	@AfterClass(alwaysRun = true)
	@Parameters("release1")
	public void afterClass(String release1, ITestContext context)  throws InterruptedException{
		
		try {
		cleanUpLogin();
		
		log.info("Restoring latest version to continue regression");
		methods.systemUpgradeSoak(driver, release1, release1, prop.getProperty("boxillaManager"));
		log.info("System restored to latest");
		}catch(Exception e) {
			e.printStackTrace();
			cleanUpLogout();
		}
		cleanUpLogout();
		
	}
}
