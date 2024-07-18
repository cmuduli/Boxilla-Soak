package northbound.get;

import static io.restassured.RestAssured.basic;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.apache.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import extra.StartupTestCase;
import extra.StartupTestCase2;
import invisaPC.Rest.HttpConnections;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import methods.SystemMethods;
import northbound.get.config.VersioningConfig;
import testNG.Utilities;

public class Versioning extends StartupTestCase{
	
	private VersioningConfig config = new VersioningConfig();
	private SystemMethods system = new SystemMethods();
	final static Logger log = Logger.getLogger(Versioning.class);


	
	@BeforeClass(alwaysRun = true)
	public void beforeClass() {
		printSuitetDetails(false);
		getDevices();
		//RestAssured.authentication = basic(boxillaRestUser, boxillaRestPassword);
		RestAssured.useRelaxedHTTPSValidation();
		RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
	}
	
	@Test
	public void test01_getBoxillaVersion() throws InterruptedException {
		//getBoxilla version from UI
		String major = system.getBoxillaMajorVersion(driver);
		String minor = system.getBoxillaMinorVersion(driver);
		String version =  major + "." + minor;
		
		//call rest api and verify version given().auth().preemptive().basic("admin", "admin").headers(BoxillaHeaders.getBoxillaHeaders())
		given().auth().preemptive().basic(boxillaRestUser, boxillaRestPassword).headers(BoxillaHeaders.getBoxillaHeaders())
		.when().contentType(ContentType.JSON)
		.get(config.getUri(boxillaManager))
		.then().assertThat().statusCode(200)
		.body(config.getBoxillaSoftwareVersion(), equalTo(version));		
	}
	
	@Test
	public void test02_getBoxillaSerialNumber() throws InterruptedException {
		//get Serial number from UI
		String serial = system.getBoxillaSerialNumber(driver);
		given().auth().preemptive().basic(boxillaRestUser, boxillaRestPassword).headers(BoxillaHeaders.getBoxillaHeaders())
		.when()
		.get(config.getUri(boxillaManager))
		.then().assertThat().statusCode(200)
		.body(config.getBoxillaSerialNumber(), equalTo(serial));		
	}
	
	@Test
	public void test03_getModelNumber() {
		given().auth().preemptive().basic(boxillaRestUser, boxillaRestPassword).headers(BoxillaHeaders.getBoxillaHeaders())
		.when()
		.get(config.getUri(boxillaManager))
		.then().assertThat().statusCode(200)
		.body(config.getBoxillaModelNumber(), equalTo("BXAMGR"));
	}
	@Test
	public void test04_getApiVersion() {
		given().auth().preemptive().basic(boxillaRestUser, boxillaRestPassword).headers(BoxillaHeaders.getBoxillaHeaders())
		.when()
		.get(config.getUri(boxillaManager))
		.then().assertThat().statusCode(200)
		.body(config.getApiVersion(), equalTo("v1.1"));
	}
	@Test
	public void test05_unauthorizedAccessUsername() {
		
		given().auth().preemptive().basic("invalid", boxillaRestPassword).headers(BoxillaHeaders.getBoxillaHeaders())
		.when()
		.get(config.getUri(boxillaManager))
		.then().assertThat().statusCode(401)
		.body("message", equalTo("Operation is not authorized."));
	}
	@Test
	public void test06_unauthorizedAccessPassword() {
		
		given().auth().preemptive().basic(boxillaRestUser, "invalid").headers(BoxillaHeaders.getBoxillaHeaders())
		.when()
		.get(config.getUri(boxillaManager))
		.then().assertThat().statusCode(401)
		.body("message", equalTo("Operation is not authorized."));
	}
	
	@Test
	public void test07_simpleLoadTest() throws InterruptedException {
		//getBoxilla version from UI
		String major = system.getBoxillaMajorVersion(driver);
		String minor = system.getBoxillaMinorVersion(driver);
		String version =  major + "." + minor;
		
		//call rest api and verify version given().auth().preemptive().basic("admin", "admin").headers(BoxillaHeaders.getBoxillaHeaders())
		for(int j=0; j < 2000; j++) {
			given().auth().preemptive().basic(boxillaRestUser, boxillaRestPassword).headers(BoxillaHeaders.getBoxillaHeaders())
			.when().contentType(ContentType.JSON)
			.get(config.getUri(boxillaManager))
			.then().assertThat().statusCode(200)
			.body(config.getBoxillaSoftwareVersion(), equalTo(version));	
			log.info("TEST OK:" + j);
		}
	}
}
