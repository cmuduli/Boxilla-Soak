package soak;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import extra.StartupTestCase;
import methods.SystemMethods;
import objects.Landingpage;
import objects.Users;

public class ServiceRestart extends StartupTestCase{
	
	SystemMethods methods = new SystemMethods();
	final static Logger log = Logger.getLogger(ServiceRestart.class);
	
	@Test
	public void ServiceMoyRestart() throws InterruptedException{
		log.info("Going to restart services...");
		methods.restartMoyService(prop.getProperty("boxillaUserName"), prop.getProperty("boxillaPassword"), 
				prop.getProperty("boxillaManager"));
		driver.navigate().refresh();
		methods.timer(driver);
		//check for element
		Assert.assertTrue(Landingpage.dashboard(driver).isDisplayed(),
				"***** Dashboard is not diaplyed *****");
		
	}
	
	

}
