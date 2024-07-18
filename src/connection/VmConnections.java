package connection;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static io.restassured.RestAssured.basic;

import extra.StartupTestCase;
import extra.StartupTestCase2;
import io.restassured.RestAssured;
import methods.ConnectionsMethods;
import methods.DevicesMethods;
import testNG.Utilities;
import static io.restassured.RestAssured.given;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class VmConnections extends StartupTestCase {
	
	final static Logger log = Logger.getLogger(VmConnections.class);
	private ConnectionsMethods connections = new ConnectionsMethods();
	private DevicesMethods device = new DevicesMethods();
	private String connectionName = "vmRDPConnection";
	private String vmIp;
	
	
	/**
	 * Overriding superclass method to make connections for this suite of tests
	 */
	@BeforeClass(alwaysRun = true)
	public void beforeClass() throws InterruptedException {
		printSuitetDetails(false);
		getDevices();
		
		try {	
			vmIp = getLocalIp();
			cleanUpLogin();
			RestAssured.authentication = basic(restuser, restPassword);			//REST authentication
			RestAssured.useRelaxedHTTPSValidation();
			connections.createVmRdpConnection(connectionName, "boxilla", "cloud$1", vmIp, "invisaPc.com", driver);
			device.recreateCloudData(rxIp, txIp);
			log.info("Sleep while devices reboot...");

			device.rebootDeviceSSH(rxIp, deviceUserName, devicePassword, 70000);
			log.info("Sleep while devices reboot...");
		}catch(Exception | AssertionError e) {
			Utilities.captureScreenShot(driver, this.getClass().getName() + "_beforeClass", "Before Class");
			e.printStackTrace();
			cleanUpLogout();
		}
		cleanUpLogout();
	}
	
	@Test
	public void test01_connectToVmRDP() throws InterruptedException {
		String[] connectionSources = {connectionName};
		connections.addSources(driver, connectionSources);
		connections.addPrivateDestination(driver, connectionName, singleRxName);
		
		Thread.sleep(30000);
		System.out.println(getHttp() + "://" + rxIp + getPort() + "/statistics/active_connections");
		String jsonReturnedActive = given().header(getHead())
				.when()
				.get(getHttp() + "://" + rxIp + getPort() + "/statistics/active_connections").asString();
		log.info("RETURNED ACTIVE: " + jsonReturnedActive);
		log.info("RX MAC:" + rxSingle.getMac().toLowerCase() );
		Assert.assertTrue(jsonReturnedActive.contains(rxSingle.getMac().toLowerCase()), "Active connection is not up");
		
	}
	
	public String getLocalIp() throws UnknownHostException {
		String ip = InetAddress.getLocalHost().toString();
		String[] ipSplit = ip.split("/");
		System.out.print(ipSplit[1]);
		return ipSplit[1];
		
		
	}
	

	

}
