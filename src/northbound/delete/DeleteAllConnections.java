package northbound.delete;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import org.apache.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import extra.StartupTestCase;
import extra.StartupTestCase2;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import methods.ConnectionsMethods;
import methods.DevicesMethods;
import methods.UsersMethods;
import northbound.get.BoxillaHeaders;
import northbound.get.KvmActiveConnections;
import northbound.get.config.ConnectionStatusConfig;
import northbound.get.config.ConnectionStatusConfig.GetConnectionStatus;
import northbound.post.config.CreateKvmConnectionsConfig;

public class DeleteAllConnections extends StartupTestCase {

	final static Logger log = Logger.getLogger(DeleteAllConnections.class);
	private CreateKvmConnectionsConfig conConfig = new CreateKvmConnectionsConfig();
	private ConnectionStatusConfig conStatusConfig = new ConnectionStatusConfig();
	private ConnectionsMethods methods = new ConnectionsMethods();
	private DevicesMethods deviceMethods = new DevicesMethods();
	private UsersMethods userMethods = new UsersMethods();
	String txCon = "deleteAllTx";
	String pairedCon = "deleteAlPaired";
	String vmCon = "deleteAllVM";
	String vmPoolCon = "deleteAllPool";
	String vmHorizonCon = "deleteAllHorizon";
	
	@BeforeClass(alwaysRun = true)
	public void beforeClass() {
		printSuitetDetails(false);
		getDevices();

		RestAssured.useRelaxedHTTPSValidation();
		RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
		createConnections();
		
		
		
	}
	
	private void createConnections() {
		conConfig.createViaTxConnection(txCon, txIp, "Private", "No", "ConnectViaTx", "No", "No",
				"No", "No", "10", "", boxillaManager, boxillaRestUser, boxillaRestPassword);
		conStatusConfig.checkViaTxConnection(txCon, txIp, "Private", "No", "ConnectViaTx", "No", "No",
				"No", "No", "Optimized", "", boxillaManager, boxillaRestUser, boxillaRestPassword);
		
		//paired
		conConfig.createPairConnection(pairedCon, "Private", txIp, txIpDual, "Yes", "1", "H12",
				"2", "No", "No", "", boxillaManager, boxillaRestUser, boxillaRestPassword);
		conStatusConfig.checkPairedConnection(pairedCon, "Private", txIp, txIpDual, "Yes", 1, "H12",
				"2", "No", "No", "", boxillaManager, boxillaRestUser, boxillaRestPassword);
		
		//vm
		conConfig.createVmConnection(vmCon, "No", "No", "No", "No", "444", "black", txIp, "test", "test", "Private", "No",
				"", boxillaManager, boxillaRestUser, boxillaRestPassword);
		conStatusConfig.checkVMConnection(vmCon, "No", "No", "No", "No", "444", "black", txIp,
				"test", "test", "Private", "No", "", boxillaManager, boxillaRestUser, boxillaRestPassword);
		
//		//vmPool
//		conConfig.createPoolConnection(vmPoolCon, "No", "No", "No", null, "Private", "No", boxillaManager,
//				boxillaRestUser, boxillaRestPassword);
//		conStatusConfig.checkPoolConnections(vmPoolCon, "No", "No", "No", "", "Private", "No", "",
//				boxillaManager, boxillaRestUser, boxillaRestPassword);
		
		//horizon
		conConfig.createVMHorizonConnection(vmHorizonCon, "Private", txIp, "No", "test", "test", "PCoIP",
				"",boxillaManager, boxillaRestUser, boxillaRestPassword);
		conStatusConfig.checkHorizonConnections(vmHorizonCon, "Private", txIp, "No", "test", "test", "PCoIP", "",
				boxillaManager, boxillaRestUser, boxillaRestPassword);		
	}
	@Test
	public void test01_deleteAllConnectionActive() throws InterruptedException {
		//add connection to user and update xml file
		userMethods.addAllConnectionsToUser(driver, "admin");
		deviceMethods.recreateCloudData(rxIp);
		deviceMethods.rebootDeviceSSH(rxIp, deviceUserName, devicePassword, 0);
		
		String body = getBody("admin", txCon, rxEmerald.getDeviceName(), "admin");
		given().auth().preemptive().basic(boxillaRestUser, boxillaRestPassword).headers(BoxillaHeaders.getBoxillaHeaders())
		.when().contentType(ContentType.JSON)
		.body(body)
		.post("https://" + boxillaManager  + "/bxa-api/connections/kvm/active")
		.then().assertThat().statusCode(201)
		.body("message", containsString("Active connection " + txCon + " is successfully launched."));
		waitForBoxillaPoll();	//sleep and wait for the next poll before checking connection
		
		//try to delete
		given().auth().preemptive().basic(boxillaRestUser, boxillaRestPassword).headers(BoxillaHeaders.getBoxillaHeaders())
		.when().contentType(ContentType.JSON)
		.delete("https://" + boxillaManager  + "/bxa-api/connections/kvm/all")
		.then().assertThat().statusCode(409)
		.body("message", equalTo("Fail to delete connection [\"" + txCon + "\"] because one or more connections are active."));
		//reboot to kill connection
		deviceMethods.rebootDeviceSSH(rxIp, deviceUserName, devicePassword, 0);
	}
	 
	@Test
	public void test02_deleteAllConnection() {
		
		given().auth().preemptive().basic(boxillaRestUser, boxillaRestPassword).headers(BoxillaHeaders.getBoxillaHeaders())
		.when().contentType(ContentType.JSON)
		.delete("https://" + boxillaManager  + "/bxa-api/connections/kvm/all")
		.then().assertThat().statusCode(200)
		.body("message", equalTo("Successfully deleted all connections."));
		
		ConnectionStatusConfig.GetConnectionStatus con = conStatusConfig.new GetConnectionStatus();
		con.connection_names = null;
		given().auth().preemptive().basic(boxillaRestUser, boxillaRestPassword).headers(BoxillaHeaders.getBoxillaHeaders())
		.body(con)
		.when().contentType(ContentType.JSON)
		.get(conStatusConfig.getUri(boxillaManager))
		.then().assertThat().statusCode(200)
		.body("message.connections", hasSize(0));
	}
	
	private String getBody(String username, String connectionName, String receiverName, String password) {
		String body = "{\"username\": \""+ username + "\",\"password\": \"" + password  + "\",\"connection_name\": \"" + connectionName + "\", \"receiver_name\":\"" + receiverName + "\"}";
		return body;
	}
}
