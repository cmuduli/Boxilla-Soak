package systemAdmin;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import extra.StartupTestCase;
import extra.StartupTestCase2;
import methods.SystemMethods;
import system.SystemSettings;
import testNG.Utilities;

public class SystemAdministration extends StartupTestCase {
	
	public String timeStamp, customBackup, boxillaManager;
	private SystemMethods methods = new SystemMethods();
	final static Logger log = Logger.getLogger(SystemAdministration.class);

	public SystemAdministration() {
		super();
		customBackup = prop.getProperty("customBackup");
		timeStamp = prop.getProperty("timeStamp");
	}

		// get yesterday's date using Calendar
		private Date yesterday() {
			final Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -1);
			return cal.getTime();
		}

//		@Test(retryAnalyzer = com.testNG.RetryAnalyzer.class)
//		@Parameters({ "release" })
//		public void testcase01(String release) throws InterruptedException { // Upgarde version of boxilla
//			log.info("Test Case-01 Started - Uprade Boxilla version");
//			log.info("Upgrading to version : " + release);
//			methods.systemUpgrade(driver, release, boxillaManager);
//			log.info("Assertion check completed Version Successfully Upgraded - Test Case-01 Completed");
//		}

		@Test(retryAnalyzer = testNG.RetryAnalyzer.class)
		public void test01_databaseRestoreReset() throws InterruptedException { // Database restore and reset
			log.info("Test Case-02 - Database Restore");
			// create date format and yesterday date using format
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd 02");
			String yesterday = dateFormat.format(yesterday());
			// calling method to to test restore db feature
			// checks user and connection with name "backupcheck"
			methods.dbReset(driver);
			//methods.dbRestore(driver, yesterday, customBackup, timeStamp);
			log.info("Database reset successfully - Test Case-02 Completed");
		}
	}

