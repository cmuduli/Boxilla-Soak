package soak;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import extra.StartupTestCase;
import extra.StartupTestCase2;
import methods.UsersMethods;
import objects.Users;

public class AddDeleteUserGeneralAutoConnectOff extends StartupTestCase2 {
	
	private UsersMethods methods = new UsersMethods();
	final static Logger log = Logger.getLogger(AddDeleteUserGeneralAutoConnectOff.class);
	
	@Test
	public void addDeleteUser() throws InterruptedException { // Add User - General User Privilege (no template - auto
		// connect OFF)
		log.info(
				"Test Started - Adding user with General User Privilege (no template - auto connect OFF)");

		String user = "test1";
		methods.addUser(driver, user);

		Thread.sleep(2000);
		Users.userPrivilegeGeneral(driver).click();
		log.info("User Privilege - General User selected");

		methods.addUserNoTemplateAutoConnectOFF(driver, user);
		log.info("General User Added (no template - auto connect OFF) Completed");
		
		methods.deleteUser(user, driver);
	}

}
