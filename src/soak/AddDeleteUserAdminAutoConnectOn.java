package soak;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import extra.StartupTestCase;
import methods.UsersMethods;
import objects.Users;

public class AddDeleteUserAdminAutoConnectOn extends StartupTestCase {
	
	private UsersMethods methods = new UsersMethods();
	final static Logger log = Logger.getLogger(AddDeleteUserAdminAutoConnectOn.class);
	
	@Test
	public void addDeleteUser() throws InterruptedException { // Add User - Administrator Privilege (no template - auto
		// connect ON)
		log.info(
				"Test Started - Adding user with Administrator Privilege (no template - auto connect ON)");
		
		String user = "test3";
		
		methods.addUser(driver, user);

		Thread.sleep(2000);
		Users.userPrivilegeAdmin(driver).click();
		log.info("User Privilege - Administrator selected");

		methods.addUserNoTemplateAutoConnectON(driver, user);
		log.info("Admin User Added (no template - auto connect ON) Completed");
		
		methods.deleteUser(user, driver);
		
	}

}
