package northbound.put.config;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.http.ContentType;
import northbound.get.BoxillaHeaders;

public class EditZoneDetailsConfig {
	
	public String getUri(String boxillaIp)  {
		 return "https://" + boxillaIp  + "/bxa-api/zones";
	}
	
	public class EditZone {
		public String name;
		public String new_name;
		public String description;
	}
	
	
	public void editZoneDetails(String name, String new_name, String description, String boxillaIp, String restUser, 
			String restPassword) {
		
		EditZone edit = new EditZone();
		edit.name = name;
		edit.new_name = new_name;
		edit.description = description;
		
		given().auth().preemptive().basic(restUser, restPassword).headers(BoxillaHeaders.getBoxillaHeaders())
		.when().contentType(ContentType.JSON)
		.body(edit)
		.put(getUri(boxillaIp))
		.then().assertThat().statusCode(200)
		.body("message", equalTo("Successfully updated zone " + name + "."));
		
	}

}
