package northbound.put;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.apache.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import extra.StartupTestCase;
import extra.StartupTestCase2;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import methods.SystemMethods;
import northbound.get.BoxillaHeaders;
import northbound.get.config.UsersConfig;
import northbound.put.config.EditRestPasswordConfig;
import northbound.put.config.EditRestUsernameConfig;

public class EditRestPassword extends StartupTestCase{

	EditRestUsernameConfig config = new EditRestUsernameConfig();
	final static Logger log = Logger.getLogger(EditRestPassword.class);
	private UsersConfig userConfig = new UsersConfig();
	private EditRestPasswordConfig passConfig = new EditRestPasswordConfig();
	
	
	@BeforeClass(alwaysRun = true)
	public void beforeClass() {
		printSuitetDetails(false);
		getDevices();
		
		RestAssured.useRelaxedHTTPSValidation();
		RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
	}
	
	@Test
	public void test01_editPassword() {
		String newPassword = "automationPass";
		EditRestPasswordConfig.ChangePassword pass = passConfig.new ChangePassword();
		pass.username = boxillaRestUser;
		pass.new_password = newPassword;
		
		log.info("Editing password to " + pass.new_password);
		given().auth().preemptive().basic(boxillaRestUser, boxillaRestPassword).headers(BoxillaHeaders.getBoxillaHeaders())
		.when().contentType(ContentType.JSON)
		.body(pass)
		.put(passConfig.getUri(boxillaManager))
		.then().assertThat().statusCode(200)
		.body("message", equalTo("Successfully updated REST password."));
		
		log.info("Running get all users with updated password");
		 given().auth().preemptive().basic(boxillaRestUser, pass.new_password).headers(BoxillaHeaders.getBoxillaHeaders())
		 .when().contentType(ContentType.JSON)
		.get(userConfig.getUri(boxillaManager))
		.then().assertThat().statusCode(200);
		 
		 log.info("Changing password back to original");
		 pass.new_password = boxillaRestPassword;
		 given().auth().preemptive().basic(boxillaRestUser, newPassword).headers(BoxillaHeaders.getBoxillaHeaders())
			.when().contentType(ContentType.JSON)
			.body(pass)
			.put(passConfig.getUri(boxillaManager))
			.then().assertThat().statusCode(200)
			.body("message", equalTo("Successfully updated REST password."));
		 
		 log.info("Running get all users with original password");
		 given().auth().preemptive().basic(boxillaRestUser, boxillaRestPassword).headers(BoxillaHeaders.getBoxillaHeaders())
		 .when().contentType(ContentType.JSON)
		.get(userConfig.getUri(boxillaManager))
		.then().assertThat().statusCode(200);
	}
	
	@Test
	public void test02_editPasswordMaxLength() {
		String newPassword = "poeiruryrydhsysysheyeheyehsksjsj";
		EditRestPasswordConfig.ChangePassword pass = passConfig.new ChangePassword();
		pass.username = boxillaRestUser;
		pass.new_password = newPassword;
		
		log.info("Editing password to " + pass.new_password);
		given().auth().preemptive().basic(boxillaRestUser, boxillaRestPassword).headers(BoxillaHeaders.getBoxillaHeaders())
		.when().contentType(ContentType.JSON)
		.body(pass)
		.put(passConfig.getUri(boxillaManager))
		.then().assertThat().statusCode(200)
		.body("message", equalTo("Successfully updated REST password."));
		
		log.info("Changing password back to original");
		pass.new_password = boxillaRestPassword;
		
		given().auth().preemptive().basic(boxillaRestUser, newPassword).headers(BoxillaHeaders.getBoxillaHeaders())
		.when().contentType(ContentType.JSON)
		.body(pass)
		.put(passConfig.getUri(boxillaManager))
		.then().assertThat().statusCode(200)
		.body("message", equalTo("Successfully updated REST password."));
	}
	
	@Test
	public void test03_editPasswordMaxLengthExcedded() {
		String newPassword = "poeiruryrydhsysysheyeheyehsksjsj1";
		EditRestPasswordConfig.ChangePassword pass = passConfig.new ChangePassword();
		pass.username = boxillaRestUser;
		pass.new_password = newPassword;
		
		log.info("Editing password to " + pass.new_password);
		given().auth().preemptive().basic(boxillaRestUser, boxillaRestPassword).headers(BoxillaHeaders.getBoxillaHeaders())
		.when().contentType(ContentType.JSON)
		.body(pass)
		.put(passConfig.getUri(boxillaManager))
		.then().assertThat().statusCode(400)
		.body("message", equalTo("Invalid parameter: {\"new_password\"=>\"" + newPassword + "\"}."));
	}
	
	@Test
	public void test04_editPasswordEmpty() {
		String newPassword = "";
		EditRestPasswordConfig.ChangePassword pass = passConfig.new ChangePassword();
		pass.username = boxillaRestUser;
		pass.new_password = newPassword;
		
		log.info("Editing password to " + pass.new_password);
		given().auth().preemptive().basic(boxillaRestUser, boxillaRestPassword).headers(BoxillaHeaders.getBoxillaHeaders())
		.when().contentType(ContentType.JSON)
		.body(pass)
		.put(passConfig.getUri(boxillaManager))
		.then().assertThat().statusCode(400)
		.body("message", equalTo("Invalid parameter: {\"new_password\"=>\"" + newPassword + "\"}."));
	}
	
	@Test
	public void test05_editPasswordNull() {
		String newPassword = null;
		EditRestPasswordConfig.ChangePassword pass = passConfig.new ChangePassword();
		pass.username = boxillaRestUser;
		pass.new_password = newPassword;
		
		log.info("Editing password to " + pass.new_password);
		given().auth().preemptive().basic(boxillaRestUser, boxillaRestPassword).headers(BoxillaHeaders.getBoxillaHeaders())
		.when().contentType(ContentType.JSON)
		.body(pass)
		.put(passConfig.getUri(boxillaManager))
		.then().assertThat().statusCode(400)
		.body("message", equalTo("Invalid parameter: {\"new_password\"=>nil}."));
	}
	

}
