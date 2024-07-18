package soak;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import extra.StartupTestCase;
import extra.StartupTestCase2;
import methods.SystemMethods;
import testNG.Utilities;

public class BoxillaReboot extends StartupTestCase2 {
	public String timeStamp, customBackup;
	SystemMethods methods = new SystemMethods();
	final static Logger log = Logger.getLogger(BoxillaReboot.class);

	public BoxillaReboot() {
		super();
	}
	@Test
	public void boxillaReboot() throws InterruptedException { // Boxilla restart / Reboot
		log.info("Soak Test Case-07 : Reboot Boxilla Unit");
		methods.boxillaReboot(driver);
		log.info("Reboot Boxilla Unit : Test Case-07 Completed");
	}
}

