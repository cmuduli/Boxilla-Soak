package connection;

import static io.restassured.RestAssured.basic;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.eclipse.jetty.util.log.Log;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import device.DeviceUpgrades;
import extra.Database;
import extra.MakeConnect;
import extra.Ssh;
import extra.StartupTestCase;
import extra.StartupTestCase2;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import methods.ConnectionsMethods;
import methods.DevicesMethods;
import methods.DiscoveryMethods;
import methods.SwitchMethods;
import methods.UsersMethods;
import northbound.get.BoxillaHeaders;
import objects.Connections;
import objects.Devices;
import testNG.Utilities;

public class ConnectionInteroperability extends StartupTestCase2 {
	
	final static Logger log = Logger.getLogger(ConnectionInteroperability.class);

	private String dataFile = "C:\\Test_Workstation\\SeleniumAutomation\\src\\connection\\interOp.txt";
	private UsersMethods users = new UsersMethods();
	private DevicesMethods device = new DevicesMethods();
	private ConnectionsMethods connections = new ConnectionsMethods();
	private SwitchMethods switches = new SwitchMethods();
	private DiscoveryMethods discoveryMethods = new DiscoveryMethods();
	private String uername = "InterOp";
	private String userPass = "test01";
	private String privateConnection = "InterOpPrivate";
	private String sharedConnection = "InterOpShared";

	private int timeout = 120000;
	//ip addresses
	private String seSingleRxIP = "10.211.129.182";
	private String seDualRxIP = "10.211.129.183";
	private String seSingleTxIP = "10.211.128.68";
	private String seDualTxIP = "10.211.129.184";
	private String peSingleRxIP = "10.211.128.71";
	private String peDualRxIP = "10.211.130.115";
	private String peSingleTxIP = "10.211.130.88";
	private String peDualTxIP = "10.211.130.114";
	private String zeroUIP = "10.211.130.147";
	
	//connections
	private String seSingleConPrivate = "seSingleConPrivate";
	private String seSingleConShared = "seSingleConShared";
	private String seDualConShared = "seDualConShared";
	private String seDualConPrivate = "seDualConPrivate";
	private String peSingleConPrivate = "peSingleConPrivate";
	private String peSingleConShared = "peSingleConShared";
	private String peDualConShared = "peDualConShared";
	private String peDualConPrivate = "peDualConPrivate";
	private String zeroUConPrivate = "zeroUConPrivate";
	private String zeroUConShared = "zeroUConShared";
	
	class ForceConnect {
		public String action = "";
		public String user = "";
		public String connection = "";
	}
	
	@DataProvider(name = "DP1")
	public Object[][] createData() throws IOException {
		return readData(dataFile);
	}
	
	private void manageNewDevices() throws InterruptedException {
		//SE RX First
		discoveryMethods.discoverDevices(driver);
		discoveryMethods.stateAndIPcheck(driver, "00:8C:10:1E:D5:1E", prop.getProperty("ipCheck"),
				seSingleRxIP, rxSingle.getGateway(),rxSingle.getNetmask());
		discoveryMethods.manageApplianceAutomatic(driver, "SE_RX_Single", "00:8C:10:1E:D5:1E",
				prop.getProperty("ipCheck"));
		
		discoveryMethods.discoverDevices(driver);
		discoveryMethods.stateAndIPcheck(driver, "1C:37:BF:00:12:48", prop.getProperty("ipCheck"),
				seDualRxIP, rxSingle.getGateway(),rxSingle.getNetmask());
		discoveryMethods.manageApplianceAutomatic(driver, "SE_RX_Dual", "1C:37:BF:00:12:48",
				prop.getProperty("ipCheck"));
		
		//SE TX
		discoveryMethods.discoverDevices(driver);
		discoveryMethods.stateAndIPcheck(driver, "00:8C:10:1E:D6:7D", prop.getProperty("ipCheck"),
				seSingleTxIP, rxSingle.getGateway(),rxSingle.getNetmask());
		discoveryMethods.manageApplianceAutomatic(driver, "SE_TX_Single", "00:8C:10:1E:D6:7D",
				prop.getProperty("ipCheck"));
		
		discoveryMethods.discoverDevices(driver);
		discoveryMethods.stateAndIPcheck(driver, "00:8C:10:20:E6:C3", prop.getProperty("ipCheck"),
				seDualTxIP, rxSingle.getGateway(),rxSingle.getNetmask());
		discoveryMethods.manageApplianceAutomatic(driver, "SE_TX_Dual", "00:8C:10:20:E6:C3",
				prop.getProperty("ipCheck"));
		
		
		//PE RX
		discoveryMethods.discoverDevices(driver);
		discoveryMethods.stateAndIPcheck(driver, "00:8C:10:20:FE:46", prop.getProperty("ipCheck"),
				peSingleRxIP, rxSingle.getGateway(),rxSingle.getNetmask());
		discoveryMethods.manageApplianceAutomatic(driver, "PE_RX_Single", "00:8C:10:20:FE:46",
				prop.getProperty("ipCheck"));
		
		discoveryMethods.discoverDevices(driver);
		discoveryMethods.stateAndIPcheck(driver, "1C:37:BF:00:13:0F", prop.getProperty("ipCheck"),
				peDualRxIP, rxSingle.getGateway(),rxSingle.getNetmask());
		discoveryMethods.manageApplianceAutomatic(driver, "PE_RX_Dual", "1C:37:BF:00:13:0F",
				prop.getProperty("ipCheck"));
		
		//PE TX
		discoveryMethods.discoverDevices(driver);
		discoveryMethods.stateAndIPcheck(driver, "1C:37:BF:00:13:1B", prop.getProperty("ipCheck"),
				peSingleTxIP, rxSingle.getGateway(),rxSingle.getNetmask());
		discoveryMethods.manageApplianceAutomatic(driver, "PE_TX_Single", "1C:37:BF:00:13:1B",
				prop.getProperty("ipCheck"));
		
		discoveryMethods.discoverDevices(driver);
		discoveryMethods.stateAndIPcheck(driver, "1C:37:BF:00:12:B9", prop.getProperty("ipCheck"),
				peDualTxIP, rxSingle.getGateway(),rxSingle.getNetmask());
		discoveryMethods.manageApplianceAutomatic(driver, "PE_TX_Dual", "1C:37:BF:00:12:B9",
				prop.getProperty("ipCheck"));
		
		//zeroU
		discoveryMethods.discoverDevices(driver);
		discoveryMethods.stateAndIPcheck(driver, "1C:37:BF:00:12:75", prop.getProperty("ipCheck"),
				zeroUIP, rxSingle.getGateway(),rxSingle.getNetmask());
		discoveryMethods.manageApplianceAutomatic(driver, "ZeroU", "1C:37:BF:00:12:75",
				prop.getProperty("ipCheck"));
		
	}
	
	
	@BeforeClass(alwaysRun = true)
	public void beforeClass() {
		getDevices();
		printSuitetDetails(false);
		RestAssured.authentication = basic(restuser, restPassword);			//REST authentication
		RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
		RestAssured.useRelaxedHTTPSValidation();
//		try {
//			cleanUpLogin();
//			connections.createMasterConnection(seSingleConPrivate, "tx", "private", "false", "false", "false", "false", "false", seSingleTxIP, driver);	
//			connections.createMasterConnection(seSingleConShared, "tx", "shared", "false", "false", "false", "false", "false", seSingleTxIP, driver);
//			connections.createMasterConnection(seDualConPrivate, "tx", "private", "false", "false", "false", "false", "false", seDualTxIP, driver);
//			connections.createMasterConnection(peSingleConPrivate, "tx", "private", "false", "false", "false", "false", "false", peSingleTxIP, driver);
//			connections.createMasterConnection(peSingleConShared, "tx", "shared", "false", "false", "false", "false", "false", peSingleTxIP, driver);
//			connections.createMasterConnection(peDualConShared, "tx", "shared", "false", "false", "false", "false", "false", peDualTxIP, driver);
//			connections.createMasterConnection(seDualConShared, "tx", "shared", "false", "false", "false", "false", "false", seDualTxIP, driver);
//			connections.createMasterConnection(peDualConPrivate, "tx", "private", "false", "false", "false", "false", "false", peDualTxIP, driver);
//			connections.createMasterConnection(zeroUConPrivate, "tx", "private", "false", "false", "false", "false", "false", zeroUIP, driver);
//			connections.createMasterConnection(zeroUConShared, "tx", "shared", "false", "false", "false", "false", "false", zeroUIP, driver);
//			
//			users.masterCreateUser(driver, uername, userPass, "false", "false", "false", "false", "", "false");
//			users.addAllConnectionsToUser(driver, uername);
//			
//			
//			manageNewDevices();
//
//			device.recreateCloudData(rxIp);
//			device.rebootDeviceSSH(rxIp, deviceUserName, devicePassword, 0);
//			
//			device.recreateCloudData(rxIpDual);
//			device.rebootDeviceSSH(rxIpDual, deviceUserName, devicePassword, 0);
//
//			//http enabled is needed for make connect
//			//			device.changedHttpToEnabled(driver, rxIp);
////			device.changedHttpToEnabled(driver, rxIpDual);
////			device.changedHttpToEnabled(driver, seSingleRxIP);
////			device.changedHttpToEnabled(driver, seDualRxIP);
////			device.changedHttpToEnabled(driver, peSingleRxIP);
////			device.changedHttpToEnabled(driver, peDualRxIP);
//			
//		}catch(Exception | AssertionError e) {
//			Utilities.captureScreenShot(driver, this.getClass().getName() + "_beforeClass", "Before Class");
//			e.printStackTrace();
//			cleanUpLogout();
//		}
//		cleanUpLogout();
	}
	
//	
//	@Test(dataProvider = "DP1")
//	public void test01_privateSEConnectionSingleHead(String via) throws InterruptedException {
//		
////		Database masterDb = new Database();
////		masterDb.connectToDatabase(boxillaManager, "postgres", "foreman", "foreman");
////		masterDb.sendStatement("delete from kvm_provisions where 1=1;");
////		masterDb.closeDatabase();		
//	    
//				String [] ip = new String[1];
//		ip[0] = rxEmerald.getIpAddress();
//		String[] receivers = new String[1];
//		receivers[0] =  "Test_RX_Emerald";
//		launchConnection(via, seSingleConPrivate, ip, seSingleTxIP, receivers, "SE_TX_Single", false, true);
//		device.rebootDeviceSSH(ip[0], "root", "barrow1admin_12", 0);
////		String[] edid = {"1920x1080", "1920x1200", "1680x1050", "1280x1024", "1024x768"};
////		String[] resolutions = {"1920x1080", "1600x1200", "1680x1050", "1400x1050", "1280x1024", "1440x900",
////				"1280x960", "1280x800", "1280x720", "1024x768", "800x600", "640x480"};
////		
////		
////		for(int k=0; k < edid.length; k++) {
////			device.setUniquePropertyTx(driver, "10.211.128.68", "EDID1", edid[k]);
////			Thread.sleep(90000);
////			
////			String [] ip = new String[1];
////			ip[0] = "10.211.128.156";
////			String[] receivers = new String[1];
////			receivers[0] =  "Test_RX_Emerald";
////			launchConnection("force", seSingleConPrivate, ip, seSingleTxIP, receivers, "SE_TX_Single", false, true);
////			
////			for(int j=0; j < resolutions.length; j++) {
////				device.setSourceResolution("10.211.130.150", "wintelpro", "wintelpro", resolutions[j]);
////				Thread.sleep(10000);
////				String res = device.getTranmitterResolution("10.211.128.68");
////				Assert.assertTrue(res.equals(resolutions[j]), "Resolution on transmitter did not match source res. Expected " + resolutions[j] +  ", actual:" + res + ".");
////			}
////		}
////		
////		device.setSourceResolution("10.211.130.150", "wintelpro", "wintelpro", "1280x960");
////		
////		Thread.sleep(10000);
////		
////		 res = device.getTranmitterResolution("10.211.128.68");
////		Assert.assertTrue(res.equals("1280x960"), "Resolution on transmitter did not match source res. Expected 1280x960, actual:" + res);
//	}
//	
//	@Test(dataProvider = "DP1")
//	public void test02_privateSEConnectionDualHead(String via) throws InterruptedException {
//		
////		Database masterDb = new Database();
////		masterDb.connectToDatabase(boxillaManager, "postgres", "foreman", "foreman");
////		masterDb.sendStatement("delete from kvm_provisions where 1=1;");
////		masterDb.closeDatabase();
//		
//		
//	    
//				String [] ip = new String[1];
//				ip[0] = rxEmerald.getIpAddress();
//		String[] receivers = new String[1];
//		receivers[0] =  "Test_RX_Emerald";
//		launchConnection(via, seDualConPrivate, ip, seDualTxIP, receivers, "SE_TX_Dual", false, true);
//		device.rebootDeviceSSH(ip[0], "root", "barrow1admin_12", 0);
//	}
//	
//	@Test(dataProvider = "DP1")
//	public void test04_privatePEConnectionSingleHead(String via) throws InterruptedException {
//		
////		Database masterDb = new Database();
////		masterDb.connectToDatabase(boxillaManager, "postgres", "foreman", "foreman");
////		masterDb.sendStatement("delete from kvm_provisions where 1=1;");
////		masterDb.closeDatabase();
//		
//		
//	    
//				String [] ip = new String[1];
//				ip[0] = rxEmerald.getIpAddress();
//		String[] receivers = new String[1];
//		receivers[0] =  "Test_RX_Emerald";
//		launchConnection(via, peSingleConPrivate, ip, peSingleTxIP, receivers, "PE_TX_Single", false, true);
//		device.rebootDeviceSSH(ip[0], "root", "barrow1admin_12", 0);
//	}
//	
//	@Test(dataProvider = "DP1")
//	public void test05_privatePEConnectionDualHead(String via) throws InterruptedException {
//		
////		Database masterDb = new Database();
////		masterDb.connectToDatabase(boxillaManager, "postgres", "foreman", "foreman");
////		masterDb.sendStatement("delete from kvm_provisions where 1=1;");
////		masterDb.closeDatabase();
//		
//		
//	    
//				String [] ip = new String[1];
//				ip[0] = rxEmerald.getIpAddress();
//		String[] receivers = new String[1];
//		receivers[0] =  "Test_RX_Emerald";
//		launchConnection(via, peDualConPrivate, ip, peDualTxIP, receivers, "PE_TX_Dual", false, true);
//		device.rebootDeviceSSH(ip[0], "root", "barrow1admin_12", 0);
//	}
	
//	@Test(dataProvider = "DP1")
	public void test06_sharedZeroUConnection(String via) throws InterruptedException {
		
//		Database masterDb = new Database();
//		masterDb.connectToDatabase(boxillaManager, "postgres", "foreman", "foreman");
//		masterDb.sendStatement("delete from kvm_provisions where 1=1;");
//		masterDb.closeDatabase();
		
		
	    
				String [] ip = new String[2];
		ip[0] = rxEmerald.getIpAddress();
		ip[1] = rxDual.getIpAddress();
		String[] receivers = new String[2];
		receivers[0] =  "Test_RX_Emerald";
		receivers[1] = "Test_RX_Dual";
		launchConnection(via, zeroUConShared, ip, zeroUIP, receivers, "ZeroU", true, true);
		device.rebootDeviceSSH(ip[0], "root", "barrow1admin_12", 0);
		device.rebootDeviceSSH(ip[1], "root", "barrow1admin_12", 0);
	}
	
	//@Test(dataProvider = "DP1")
	public void test07_privateZeroUConnection(String via) throws InterruptedException {
		
//		Database masterDb = new Database();
//		masterDb.connectToDatabase(boxillaManager, "postgres", "foreman", "foreman");
//		masterDb.sendStatement("delete from kvm_provisions where 1=1;");
//		masterDb.closeDatabase();
		
		
	    
				String [] ip = new String[1];
		ip[0] = rxEmerald.getIpAddress();
		String[] receivers = new String[1];
		receivers[0] =  "Test_RX_Emerald";
		launchConnection(via, zeroUConPrivate, ip, zeroUIP, receivers, "ZeroU", false, true);
		device.rebootDeviceSSH(ip[0], "root", "barrow1admin_12", 0);
	}
	
	
	
//	@Test(dataProvider = "DP1")
	public void test08_sharedSECnnection(String via) throws InterruptedException {
		
//		Database masterDb = new Database();
//		masterDb.connectToDatabase(boxillaManager, "postgres", "foreman", "foreman");
//		masterDb.sendStatement("delete from kvm_provisions where 1=1;");
//		masterDb.closeDatabase();
		
		
	    
				String [] ip = new String[2];
		ip[0] = rxEmerald.getIpAddress();
		ip[1] = rxDual.getIpAddress();
		String[] receivers = new String[2];
		receivers[0] =  "Test_RX_Emerald";
		receivers[1] = "Test_RX_Dual";
		launchConnection(via, seSingleConShared, ip, seSingleTxIP, receivers, "SE_TX_Single", true, true);
		device.rebootDeviceSSH(ip[0], "root", "barrow1admin_12", 0);
		device.rebootDeviceSSH(ip[1], "root", "barrow1admin_12", 0);
	}
	
//	@Test(dataProvider = "DP1")
	public void test09_sharedPEConnectionDual(String via) throws InterruptedException {
		
//		Database masterDb = new Database();
//		masterDb.connectToDatabase(boxillaManager, "postgres", "foreman", "foreman");
//		masterDb.sendStatement("delete from kvm_provisions where 1=1;");
//		masterDb.closeDatabase();
		
		
	    
				String [] ip = new String[2];
		ip[0] = rxEmerald.getIpAddress();
		ip[1] = rxDual.getIpAddress();
		String[] receivers = new String[2];
		receivers[0] =  "Test_RX_Emerald";
		receivers[1] = "Test_RX_Dual";
		launchConnection(via, peDualConShared, ip, peDualTxIP, receivers, "PE_TX_Dual", true, true);
		device.rebootDeviceSSH(ip[0], "root", "barrow1admin_12", 0);
		device.rebootDeviceSSH(ip[1], "root", "barrow1admin_12", 0);
	}
	
//	@Test(dataProvider = "DP1")
	public void test10_sharedSEConnectionDual(String via) throws InterruptedException {
		
//		Database masterDb = new Database();
//		masterDb.connectToDatabase(boxillaManager, "postgres", "foreman", "foreman");
//		masterDb.sendStatement("delete from kvm_provisions where 1=1;");
//		masterDb.closeDatabase();
		
		
	    
				String [] ip = new String[2];
		ip[0] = rxEmerald.getIpAddress();
		ip[1] = rxDual.getIpAddress();
		String[] receivers = new String[2];
		receivers[0] =  "Test_RX_Emerald";
		receivers[1] = "Test_RX_Dual";
		launchConnection(via, seDualConShared, ip, seDualTxIP, receivers, "SE_TX_Dual", true, true);
		device.rebootDeviceSSH(ip[0], "root", "barrow1admin_12", 0);
		device.rebootDeviceSSH(ip[1], "root", "barrow1admin_12", 0);
	}
	
//	@Test(dataProvider = "DP1")
	public void test11_sharedPEConnection(String via) throws InterruptedException {
		
//		Database masterDb = new Database();
//		masterDb.connectToDatabase(boxillaManager, "postgres", "foreman", "foreman");
//		masterDb.sendStatement("delete from kvm_provisions where 1=1;");
//		masterDb.closeDatabase();
		
		
	    
				String [] ip = new String[2];
				ip[0] = rxEmerald.getIpAddress();
				ip[1] = rxDual.getIpAddress();
		String[] receivers = new String[2];
		receivers[0] =  "Test_RX_Emerald";
		receivers[1] = "Test_RX_Dual";
		launchConnection(via, peSingleConShared, ip, peSingleTxIP, receivers, "PE_TX_Single", true, true);
		device.rebootDeviceSSH(ip[0], "root", "barrow1admin_12", 0);
		device.rebootDeviceSSH(ip[1], "root", "barrow1admin_12", 0);
	}
	
	//@Test(dataProvider = "DP1")
	public void test12_allReceiversSESingleTX(String via) throws InterruptedException {
		
		String [] ip = new String[6];
		ip[0] = rxEmerald.getIpAddress();
		ip[1] = rxDual.getIpAddress();
		ip[2] = seSingleRxIP;		//se single
		ip[3] = seDualRxIP;			//se dual
		ip[4] = peSingleRxIP;		//pe single
		ip[5] = peDualRxIP;  		//pe dual
		
		String[] receivers = new String[6];
		receivers[0] =  "Test_RX_Emerald";
		receivers[1] = "Test_RX_Dual";
		receivers[2] = "SE_RX_Single";
		receivers[3] = "SE_RX_Dual";
		receivers[4] = "PE_RX_Single";
		receivers[5] = "PE_RX_Dual";
		
		launchConnection(via, seSingleConShared, ip, seSingleTxIP, receivers, "SE_TX_Single", true, false);
		for(int j=0; j < ip.length; j++) {
			device.rebootDeviceSSH(ip[j], "root", "barrow1admin_12", 0);
		}
		
	}
	
//	@Test(dataProvider = "DP1")
	public void test13_allReceiversPESingleTX(String via) throws InterruptedException {
		
		String [] ip = new String[6];
		ip[0] = rxEmerald.getIpAddress();
		ip[1] = rxDual.getIpAddress();
		ip[2] = seSingleRxIP;		//se single
		ip[3] = seDualRxIP;			//se dual
		ip[4] = peSingleRxIP;		//pe single
		ip[5] = peDualRxIP;  		//pe dual
		
		String[] receivers = new String[6];
		receivers[0] =  "Test_RX_Emerald";
		receivers[1] = "Test_RX_Dual";
		receivers[2] = "SE_RX_Single";
		receivers[3] = "SE_RX_Dual";
		receivers[4] = "PE_RX_Single";
		receivers[5] = "PE_RX_Dual";
		
		launchConnection(via, peSingleConShared, ip, peSingleTxIP, receivers, "PE_TX_Single", true, false);
		for(int j=0; j < ip.length; j++) {
			device.rebootDeviceSSH(ip[j], "root", "barrow1admin_12", 0);
		}
		
	}
	
	
	//@Test(dataProvider = "DP1")
	public void test14_allReceiversZeroU(String via) throws InterruptedException {
		
		String [] ip = new String[6];
		ip[0] = rxEmerald.getIpAddress();
		ip[1] = rxDual.getIpAddress();
		ip[2] = seSingleRxIP;		//se single
		ip[3] = seDualRxIP;			//se dual
		ip[4] = peSingleRxIP;		//pe single
		ip[5] = peDualRxIP;  		//pe dual
		
		String[] receivers = new String[6];
		receivers[0] =  "Test_RX_Emerald";
		receivers[1] = "Test_RX_Dual";
		receivers[2] = "SE_RX_Single";
		receivers[3] = "SE_RX_Dual";
		receivers[4] = "PE_RX_Single";
		receivers[5] = "PE_RX_Dual";
		
		launchConnection(via, zeroUConShared, ip, zeroUIP, receivers, "ZeroU", true, false);
		for(int j=0; j < ip.length; j++) {
			device.rebootDeviceSSH(ip[j], "root", "barrow1admin_12", 0);
		}
		
	}
	
	@Test
	public void switchTest() throws InterruptedException {
		String [] ip = new String[1];
		ip[0] = rxEmerald.getIpAddress();   //Single 4k
		
		String[] receivers = new String[1];
		receivers[0] =  rxEmerald.getIpAddress();
		
		
			log.info("Lauinching SE con");
			launchConnection("force", seSingleConPrivate, ip, seSingleTxIP, receivers, "SE_TX_Single", false, false);
			log.info("SE con running. Launching 4k con");
			launchConnection("force", "bren_4k", ip, txEmerald.getIpAddress(), receivers, txEmerald.getDeviceName(), false, false);
			log.info("4k con running. Launching pe con");
			launchConnection("force", peSingleConPrivate, ip, peSingleTxIP, receivers, "PE_TX_Single", false, false);
			log.info("PE con running. Launching 4k con");
			launchConnection("force", "bren_4k", ip, txEmerald.getIpAddress(), receivers, txEmerald.getDeviceName(), false, false);
			log.info("4k con running. Launching ZeroU con");
			launchConnection("force", zeroUConPrivate, ip, zeroUIP, receivers, "ZeroU", false, false);
			log.info("ZeroU connection running. Launching 4k con");
			launchConnection("force", "bren_4k", ip, txEmerald.getIpAddress(), receivers, txEmerald.getDeviceName(), false, false);
			log.info("4k con running. Test complete");
				
	}
//	
	private void launchConnection(String via, String connectionName, String[] ip, String transmitterIp, String[] receivers, String transmitter, boolean isShared, boolean isCheckBoxilla) throws InterruptedException {
		switch(via) {
		case "boxilla" :
			launchViaBoxilla(connectionName, ip, transmitterIp, receivers, transmitter, isShared, isCheckBoxilla);
			break;
		case "launchNb" :
			launchViaNorthbound(connectionName, receivers,ip, transmitterIp, transmitter, isShared, isCheckBoxilla);
			break;
		case "force" :
			launchViaForce(connectionName, ip, receivers, transmitterIp, transmitter, isShared, isCheckBoxilla);
			break;
		case "make" :
			launchViaMake(transmitterIp, ip);
			break;
			
			
		}
	}
	
	
	private void launchViaMake(String ip, String[] rxIp) throws InterruptedException {
		
		MakeConnect mCon = new MakeConnect();
		mCon.action = "make_connection";
		mCon.user = uername;
		mCon.transmitter = ip;
		
		int status = given().auth().preemptive().basic(restuser, restPassword).header(getHead()).body(mCon)
				.when()
				.contentType(ContentType.JSON)
				.put("http://" + rxIp[0] + ":7778/control/connections").andReturn().statusCode();
		log.info("Status code from make_connection:" + status);
		Assert.assertTrue(status == 200, "Return code did not equal 200, actual:" + status);

		
		Thread.sleep(70000);
		given().auth().preemptive().basic(restuser, restPassword).header(getHead())
		.when()
		.get(getHttp() + "://" + rxIp[0] + getPort() + "/statistics/active_connections")
		.then().assertThat().statusCode(200)
		.body("kvm_active_connections.tx_hostname[0]", equalTo(ip));
		
	}
	
	private void launchViaForce(String connectionName, String[] ip,  String[] receivers, String transmitterIp, 
			String txName, boolean isShared, boolean isCheckBoxilla) throws InterruptedException {
		ForceConnect con = new ForceConnect() ;
			con.action = "force_connection";
			con.user = "Boxilla";
			con.connection = connectionName;
			for(int j=0; j < ip.length; j++) {
			int status = given().auth().preemptive().basic(restuser, restPassword).header(getHead()).body(con)
					.when()
					.contentType(ContentType.JSON)
					.put("https://" + ip[j] + ":8888/control/connections").andReturn().statusCode();		//make connection 
			Assert.assertTrue(status == 200, "status did not equal 200");
			}
			//Thread.sleep(120000);
			try {
				getFPS(transmitterIp);
			}catch(Exception e) {
				log.info("Unable to get fps");
			}
			
			checkConnection(ip, transmitterIp, connectionName, receivers, txName, isShared, isCheckBoxilla);
			
			Ssh ssh = new Ssh("root", "barrow1admin_12", ip[0]);
			ssh.loginToServer();
			String isCloudRunning2 = ssh.sendCommand("ps -ax");
			ssh.disconnect();
			Assert.assertTrue(isCloudRunning2.contains(connectionName), "Connection was not running ");
		
	}
	private void checkConnection(String[] receiverIp, String transmitterIp, String connectionName, 
			String[] receiverNames, String txName, boolean isShared, boolean isCheckBoxilla) throws InterruptedException {
		
		
		
		
		//cleanUpLogin();
		for(int j=0; j < receiverIp.length; j++) {
//			int dialogButton = JOptionPane.YES_NO_OPTION;
//			
//		    JOptionPane.showConfirmDialog (null, "Is the connection running on "  + receiverIp[j] ,"Warning",dialogButton);
//			
//		    if(dialogButton == JOptionPane.YES_OPTION) {
//		    	log.info("Connection Running");
//		    }else {
//		    	log.info("Connection not running. Failing");
//		    	throw new AssertionError("Connection was not running on " + receiverIp[j]);
//		    }
			//check connection is running in SSH
			Ssh ssh = new Ssh("root", "barrow1admin_12", receiverIp[j]);
			int retry = 0;
			String isCloudRunning2  ="";
			while(retry < 5) 
				try {
				ssh.loginToServer();
				 isCloudRunning2 = ssh.sendCommand("ps -ax");
				ssh.disconnect();
				retry = 5;
				}catch(Exception e) {
					log.info("Cannot log into device " + receiverIp[j]);
					retry++;
			}
			Assert.assertTrue(isCloudRunning2.contains(connectionName), "Connection was not running on  " + receiverIp[j]);
			
			
			
			log.info("Connection is running on process");
			String connectionType = "";
			if(!isShared) {
				connectionType = "Private";
			}else {
				connectionType = "Shared";
			}
			
			if(isCheckBoxilla) {
				//NB REST
				given().auth().preemptive().basic(boxillaRestUser, boxillaRestPassword).headers(BoxillaHeaders.getBoxillaHeaders())
				.when().contentType(ContentType.JSON)
				.get(getUri())
				.then().assertThat().statusCode(200)
				.body("message.active_connections[" + j + "].connection_name", equalTo(connectionName))
				.body("message.active_connections[" + j + "].receiver_name", anyOf(equalTo(rxEmerald.getDeviceName()), equalTo(rxDual.getDeviceName())))
				.body("message.active_connections[" + j + "].host.type", equalTo("ConnectViaTx"))
				.body("message.active_connections[" + j + "].host.value", equalTo(txName))			//bug logged change when fixed
				.body("message.active_connections[" + j + "].active_user",anyOf(equalTo(uername), equalTo("Boxilla"), equalTo("admin"), equalTo(null), equalTo("")))
				.body("message.active_connections[" + j + "].type", equalTo(connectionType))
				.body("message.active_connections[" + j + "]", hasKey("duration"))
				.body("message.active_connections[" + j + "]", hasKey("total_bandwidth"))
				.body("message.active_connections[" + j + "]", hasKey("video_bandwidth"))
				.body("message.active_connections[" + j + "]", hasKey("audio_bandwidth"))
				.body("message.active_connections[" + j + "]", hasKey("usb_bandwidth"))				//bug logged. Missing from return
				.body("message.active_connections[" + j + "]", hasKey("rtt"))
				.body("message.active_connections[" + j + "]", hasKey("fps"))
				.body("message.active_connections[" + j + "]", hasKey("dropped_fps"))
				.body("message.active_connections[" + j + "]", hasKey("user_latency"));
			}else {
				given().auth().preemptive().basic(boxillaRestUser, boxillaRestPassword).headers(BoxillaHeaders.getBoxillaHeaders())
				.when().contentType(ContentType.JSON)
				.get(getUri())
				.then().assertThat().statusCode(200)
				.body("message.active_connections[" + j + "].connection_name", equalTo(connectionName))
				.body("message.active_connections[" + j + "].host.type", equalTo("ConnectViaTx"))
				.body("message.active_connections[" + j + "].host.value", equalTo(txName))			//bug logged change when fixed
				.body("message.active_connections[" + j + "].active_user",anyOf(equalTo(uername), equalTo("Boxilla"), equalTo("admin"), equalTo(null), equalTo("")))
				.body("message.active_connections[" + j + "].type", equalTo(connectionType))
				.body("message.active_connections[" + j + "]", hasKey("duration"))
				.body("message.active_connections[" + j + "]", hasKey("total_bandwidth"))
				.body("message.active_connections[" + j + "]", hasKey("video_bandwidth"))
				.body("message.active_connections[" + j + "]", hasKey("audio_bandwidth"))
				.body("message.active_connections[" + j + "]", hasKey("usb_bandwidth"))				//bug logged. Missing from return
				.body("message.active_connections[" + j + "]", hasKey("rtt"))
				.body("message.active_connections[" + j + "]", hasKey("fps"))
				.body("message.active_connections[" + j + "]", hasKey("dropped_fps"))
				.body("message.active_connections[" + j + "]", hasKey("user_latency"));
			}
			log.info("Connection is running in NB REST");
			
			if(isCheckBoxilla) {
			//boxilla UI
			int counter = 0;
			while(counter < 5) {
				
			if(!isShared) {	
			try {
				connections.navigateToConnectionViewer(driver);
				boolean isConnection = Connections.singleSourceDestinationCheck(driver, connectionName,  receiverNames[j]).isDisplayed();
				//Assert connection has been created
				log.info("Asserting if connections has been created between " + connectionName + " and " +  receiverNames[j]);
				Assert.assertTrue(isConnection, "Connection between " + connectionName + " and " +  receiverNames[j] + " is not displayed");	
				counter = 5;
			}catch(Exception e) {
				log.info("Couldnt find connection. trying again:" + counter);
				counter++;
			}
			}else {
				try {
					boolean source = Connections.matrixItem(driver, connectionName).isDisplayed();
					boolean destination1 = Connections.matrixItem(driver, singleRxName).isDisplayed();
					boolean destination2 = Connections.matrixItem(driver, dualRxName).isDisplayed();
					boolean check = source && destination1 && destination2;
					log.info("Asserting if shared connection has been established");
					Assert.assertTrue(check, "Source and all destinations were not displayed");
					counter = 5;
				}catch(Exception e) {
					log.info("Couldnt find connection. trying again:" + counter);
					counter++;
				}
			}
			}
			}
			
	}
	}
	
	private void launchViaNorthbound(String connectionName, String[] receiversNames,String[] receiversIp,
			String transmitter,String txName, boolean isShared, boolean isCheckBoxilla ) throws InterruptedException {
		
		for(int j=0; j < receiversNames.length; j++) {
		String body = "{\"username\": \""+ uername + "\",\"password\": \"" + userPass  + "\",\"connection_name\": \"" + connectionName + "\", \"receiver_name\":\"" + receiversNames[j] + "\"}";
		
		given().auth().preemptive().basic(boxillaRestUser, boxillaRestPassword).headers(BoxillaHeaders.getBoxillaHeaders())
		.when().contentType(ContentType.JSON)
		.body(body)
		.post(getUri())
		.then().assertThat().statusCode(201)
		.body("message", containsString("Active connection " + connectionName + " is successfully launched."));
		}
		
		try {
			getFPS(transmitter);
		}catch(Exception e) {
			log.info("Unable to get fps");
		}
		checkConnection(receiversIp, transmitter, connectionName, receiversNames, txName, isShared, isCheckBoxilla);
		
//		given().auth().preemptive().basic(boxillaRestUser, boxillaRestPassword).headers(BoxillaHeaders.getBoxillaHeaders())
//		.when().contentType(ContentType.JSON)
//		.get(getUri())
//		.then().assertThat().statusCode(200)
//		.body("message.active_connections[0].connection_name", equalTo(connectionName))
//		.body("message.active_connections[0].receiver_name", equalTo(receivers[0]))
//		.body("message.active_connections[0].host.type", equalTo("ConnectViaTx"))
//		.body("message.active_connections[0].host.value", equalTo(transmitter))			//bug logged change when fixed
//		.body("message.active_connections[0].active_user",anyOf(equalTo(uername), equalTo("Boxilla"), equalTo("admin"), equalTo(null), equalTo("")))
//		.body("message.active_connections[0].type", equalTo("Private"))
//		.body("message.active_connections[0]", hasKey("duration"))
//		.body("message.active_connections[0]", hasKey("total_bandwidth"))
//		.body("message.active_connections[0]", hasKey("video_bandwidth"))
//		.body("message.active_connections[0]", hasKey("audio_bandwidth"))
//		.body("message.active_connections[0]", hasKey("usb_bandwidth"))				//bug logged. Missing from return
//		.body("message.active_connections[0]", hasKey("rtt"))
//		.body("message.active_connections[0]", hasKey("fps"))
//		.body("message.active_connections[0]", hasKey("dropped_fps"))
//		.body("message.active_connections[0]", hasKey("user_latency"));
//		
		
	}
	
	private String getUri() {
		return getHttp() + "://" + boxillaManager  + "/bxa-api/connections/kvm/active";
	}
	
	private void launchViaBoxilla(String connectionName, String[] receiversIp, String transmitterIp, String[] receiverName,
			String txName, boolean isShared, boolean isCheckBoxilla) throws InterruptedException {
		
		if(!isShared) {
		String[] connectionSources = {connectionName};
		log.info("Attempting to add soruces");
		connections.addSources(driver, connectionSources);
		log.info("Sources added. Trying to add private destination");
		connections.addPrivateDestination(driver, connectionName, receiverName[0]);
		log.info("Private destination added. Sleeping and refreshing page");
		}else {
			//shared connnection
			String[] connectionSources = {connectionName};
			String[] destinations = receiverName;
			log.info("Adding sources");
			connections.addSources(driver, connectionSources);
			log.info("Sources added. Trying to add destinations");
			connections.addSharedDestination(driver, connectionName, destinations);
			log.info("Destinations added. Sleeping for 65 seconds and refreshing..");
			
			
			
		
			
			
		}
		
		try {
			getFPS(transmitterIp);
		}catch(Exception e) {
			log.info("Unable to get fps");
		}
		
		checkConnection(receiversIp, transmitterIp, connectionName, receiverName, txName, isShared, isCheckBoxilla);
//		driver.navigate().refresh();
//		Thread.sleep(5000);
//		log.info("Checking if connection is active by asserting on the connections > viewer UI");
//		boolean isConnection = Connections.singleSourceDestinationCheck(driver, connectionName,  receivers[0]).isDisplayed();
//		//Assert connection has been created
//		log.info("Asserting if connections has been created between " + connectionName + " and " +  receivers[0]);
//		Assert.assertTrue(isConnection, "Connection between " + connectionName + " and " +  receivers[0] + " is not displayed");	
	}

	private void getFPS(String ipAddress) throws InterruptedException {
		log.info("TX IP:" + ipAddress);
		Ssh fps = new Ssh("root", "barrow1admin_12", ipAddress);
		fps.loginToServer();
		fps.sendCommand("rm /usr/local/syslog.log*");
		Thread.sleep(timeout);
		String fpsLog = fps.sendCommand("tail -30 /usr/local/syslog.log");
		log.info("FPS:" + fpsLog);
		ArrayList<String> logs = new ArrayList<String>();
		Scanner scan = new Scanner(fpsLog);
		ArrayList<Integer> fpsInt = new ArrayList<Integer>();
		while(scan.hasNextLine()) {
			String line = scan.nextLine();
			
			if(line.contains("H1: ID=")) {
				logs.add(line);
			}
		}
		scan.close();
		for(String s : logs) {
		
			String[] splitLog = s.split("fps=");
			String[] fpsSplit = splitLog[1].split("\\s+");
			System.out.println("Log:" + fpsSplit[0]);
			fpsInt.add(Integer.parseInt(fpsSplit[0]));
		}
		
		
		//get average
		int average = 0;
		int sum =0;
		for(int x : fpsInt) {
			
			sum  = x + sum;
		}
		average = sum / fpsInt.size();
		log.info("AVERAGE:" + average);
		Assert.assertTrue(average > 0, "Average FPS is not 1 or greater. Actual:" + average);
		//log.info(fpsLog);
	}

		
	
	

}
