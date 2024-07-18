package northbound.delete.config;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.http.ContentType;
import northbound.get.BoxillaHeaders;

public class DeleteZoneConfig {

	
	public String getUri(String boxillaIp) {
		return "https://" + boxillaIp  + "/bxa-api/zones";
	}
	
	public class DeleteZoneObject {
		public String[] zone_names;
	}
	
	public void deleteZone(String[] zone_names, String boxillaManager, String boxillaRestUser, String boxillaRestPassword) {
		DeleteZoneObject delete = new DeleteZoneObject();
		delete.zone_names = zone_names;
		
		given().auth().preemptive().basic(boxillaRestUser, boxillaRestPassword).headers(BoxillaHeaders.getBoxillaHeaders())
		.when().contentType(ContentType.JSON)
		.body(delete)
		.delete(getUri(boxillaManager))
		.then().assertThat().statusCode(200);
		//.body("message", equalTo("Successfully created zone " + name + "."));
	}
	
}
