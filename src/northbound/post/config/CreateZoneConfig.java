package northbound.post.config;

import static io.restassured.RestAssured.given;

import org.apache.log4j.Logger;

import io.restassured.http.ContentType;
import northbound.get.BoxillaHeaders;
import static org.hamcrest.Matchers.equalTo;

public class CreateZoneConfig {

	final static Logger log = Logger.getLogger(CreateZoneConfig.class);
	
	public String getUri(String boxillaIp) {
		return "https://" + boxillaIp  + "/bxa-api/zones";
	}
	
	
	public class CreateZone {
		public String name;
		public String description;
	}
	
	public void createZone(String name, String description, String boxillaIp,  String restUser, String restPassword) {
		
		CreateZone cZone = new CreateZone();
		cZone.name = name;
		cZone.description = description;	
		log.info("Attempting to create zone with name:" + name);
		
		given().auth().preemptive().basic(restUser, restPassword).headers(BoxillaHeaders.getBoxillaHeaders())
		.when().contentType(ContentType.JSON)
		.body(cZone)
		.post(getUri(boxillaIp))
		.then().assertThat().statusCode(201)
		.body("message", equalTo("Successfully created zone " + name + "."));
		
		log.info("Zone was successfully created");
	}
	
	
	
	
}
