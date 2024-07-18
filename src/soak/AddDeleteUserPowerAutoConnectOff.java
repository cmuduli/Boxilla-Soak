package soak;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import extra.StartupTestCase;
import extra.StartupTestCase2;
import methods.UsersMethods;
import objects.Users;

public class AddDeleteUserPowerAutoConnectOff extends StartupTestCase{
	
	private UsersMethods methods = new UsersMethods();
	final static Logger log = Logger.getLogger(AddDeleteUserPowerAutoConnectOff.class);
	
	@Test
	public void addDeleteUser() throws InterruptedException { // Add User - Power User Privilege (no template - auto
		// connect OFF)
		log.info(
				"Started - Adding user with Power User Privilege (no template - auto connect OFF)");
		
		String user = "test4";
		
		methods.addUser(driver, user);

		Thread.sleep(2000);
		Users.userPrivilegePower(driver).click();
		log.info("User Privilege - Power User selected");

		methods.addUserNoTemplateAutoConnectOFF(driver, user);
		log.info("Power User Added (no template - auto connect OFF) - Completed");
		
		methods.deleteUser(user, driver);
	}

}
