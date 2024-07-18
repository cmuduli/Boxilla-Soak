package soak;

import java.io.IOException;

import org.testng.Assert;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import extra.StartupTestCase;
import extra.StartupTestCase2;
import methods.SystemMethods;
import objects.SystemAll;
import testNG.Utilities;

public class RestoreResetDB extends StartupTestCase2 {

	private String timeStamp, customBackup;
	private SystemMethods methods = new SystemMethods();
	final static Logger log = Logger.getLogger(RestoreResetDB.class);

	public RestoreResetDB() {
		customBackup = prop.getProperty("customBackup");
		timeStamp = prop.getProperty("timeStamp");
	}

	@Test
	public void depenancyDatabaseUpload() throws InterruptedException {
		log.info("Test Preparation - Upload Custom Database Backup");
		methods.navigateToSystemAdmin(driver);
		methods.timer(driver);
		SystemAll.backupRestoreTab(driver).click();
		log.info("Backup and Restore Tab Clicked");
		methods.uploadCustomBackup(driver, customBackup, timeStamp);
		log.info("Database Backup Upload - Test Preparation Completed");
	}

	@Test//(dependsOnMethods = { "depenancyDatabaseUpload" }) // Database restore and reset
	public void restoreReset() throws InterruptedException {
		log.info("Soak Test Case-06 Started - Database Restore and Reset");
//		log.info("Checking license number before test");
//		int licenseNumber =  getLicenseNumber();
//		log.info("Current number of licenses: " + licenseNumber);
		methods.navigateToSystemAdmin(driver);
		methods.timer(driver);
		SystemAll.backupRestoreTab(driver).click();
		log.info("Backup and Restore Tab Clicked");
		methods.activateBackup(driver, timeStamp);
//		Assert.assertTrue(licenseNumber == getLicenseNumber(), "License numbers did not match after restore. Failing test");
		methods.dbReset(driver);
		log.info("Database Restore and Reset - Soak Test Case-06 Completed");
	}
	
	public int getLicenseNumber() throws InterruptedException {
		methods.navigateToLicense(driver);
		methods.timer(driver);
		return Integer.parseInt(SystemAll.currentLicenseLimt(driver).getText());
	}
}

