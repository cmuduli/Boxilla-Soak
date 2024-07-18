package soak;

import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

import extra.StartupTestCase;
import extra.StartupTestCase2;
import methods.UsersMethods;
import objects.Users;

public class AddDeleteUserAdminAutoConnectOff extends StartupTestCase2 {
	
	private UsersMethods methods = new UsersMethods();
	final static Logger log = Logger.getLogger(AddDeleteUserAdminAutoConnectOff.class);
	
	@Test
	public void addDeleteUser() throws InterruptedException { // Add User - Administrator Privilege (no template - auto
		// connect OFF)
		log.info(
				"Test Start - Adding user with Administrator Privilege (no template - auto connect OFF)");
		String user = "test2";
		methods.addUser(driver, user);

		Thread.sleep(2000);
		Users.userPrivilegeAdmin(driver).click();
		log.info("User Privilege - Administrator selected");

		methods.addUserNoTemplateAutoConnectOFF(driver, user);
		log.info("Admin User Added (no template - auto connect OFF)");

		methods.deleteUser(user, driver);
		
		
	}
	


}
