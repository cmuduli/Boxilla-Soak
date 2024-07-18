package activeStandby;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import device.DeviceDiscovery;
import extra.Database;
import extra.ScpTo;
import extra.Ssh;
import extra.StartupTestCase;
import extra.StartupTestCase2;
import methods.ClusterMethods;
import methods.ConnectionsMethods;
import methods.SwitchMethods;
import methods.SystemMethods;
import methods.UsersMethods;
import objects.Users;
import objects.Cluster.CLUSTER_INFO_TABLE_COLUMNS;
import objects.Cluster.CLUSTER_NODE_COLUMNS;
import testNG.Utilities;

public class ActiveStandby extends StartupTestCase2 {
	
	final static Logger log = Logger.getLogger(ActiveStandby.class);
	private ConnectionsMethods connectmethods = new ConnectionsMethods();
	private ClusterMethods cluster = new ClusterMethods();
	private String clusterId = "cluster1";
	private String masterNodeId = "1";
	private String masterNodeName = "node1";
	private String standbyNodeName = "node2";
	private String standbyNodeId = "2";
	private String boxilla  = boxillaManager;
	private String dbUsername = dbUser;
	private String dataPassword = dbPassword;
	private UsersMethods users = new UsersMethods();
	private ConnectionsMethods connection = new ConnectionsMethods();
	private String oldConnection = "tx_testconnection1";
	private String oldUser = "test1";
	private SystemMethods system = new SystemMethods();
	
	@Test(priority=1)
	public void test01_createMaster() throws InterruptedException {
		cluster.prepareMasterBoxilla(driver, clusterId, virtualIp, masterNodeId, masterNodeName);
		log.info("Getting node ID from UI and asserting if it matches");
		String nodeIdFromUI = cluster.getNodeInfoTableColumn(driver, boxillaManager, CLUSTER_NODE_COLUMNS.NODE_ID);
		Assert.assertTrue(masterNodeId.equals(nodeIdFromUI), "Node ID from UI did not match Node ID used to create master, actual " + 
		nodeIdFromUI);
		log.info("Node id matched. Checking node name");
		String nodeNameFromUi = cluster.getNodeInfoTableColumn(driver, boxillaManager, CLUSTER_NODE_COLUMNS.NODE_NAME);
		Assert.assertTrue(masterNodeName.equals(nodeNameFromUi), "Node Name from UI did not match Node name "
				+ "used to create master, actual " + nodeNameFromUi);
		log.info("Node name matched. Checking Virtual IP");
		String ipFromUI = cluster.getClusterInfoTableColumn(driver, clusterId, CLUSTER_INFO_TABLE_COLUMNS.VIRTUAL_IP);
		Assert.assertTrue(virtualIp.equals(ipFromUI), "IP from UI did not match the IP used to create the master, actual"
				+ " " + virtualIp);
		log.info("Virtual IP matched, checking cluster ID");
		String clusterIdFromUI = cluster.getClusterInfoTableColumn(driver, clusterId, CLUSTER_INFO_TABLE_COLUMNS.CLUSTER_ID);
		Assert.assertTrue(clusterIdFromUI.equals(clusterId), "Cluster ID from UI did not match the cluster ID from "
				+ " creation, actual " + clusterIdFromUI);
		log.info("Cluster ID checked. Test passed");	
		//set boxilla for next test	
		boxillaManager = boxillaManager2;		//set boxillaManager to 2nd boxilla
		log.info("TEST FINISHED");
	}
	
	@Test(priority=2)
	public void test02_createStandby() throws InterruptedException {
		cluster.prepareStandByBoxilla(driver, boxilla, standbyNodeId, standbyNodeName);
		log.info("Killing webdriver and starting a new session with virtual IP");
		driver.quit();
		boxillaManager = virtualIp;			//set main boxilla to vip
		cleanUpLogin(virtualIp);
		log.info("Getting node ID from UI and asserting if it matches");
		String nodeIdFromUI = cluster.getNodeInfoTableColumn(driver, boxillaManager2, CLUSTER_NODE_COLUMNS.NODE_ID);
		Assert.assertTrue(standbyNodeId.equals(nodeIdFromUI), "node id from UI did not match node id used "
				+ "to create standby,expected," + nodeIdFromUI + " actual " + nodeIdFromUI);
		log.info("Node ids match. Checking node name");
		String nodeNameFromUi = cluster.getNodeInfoTableColumn(driver, boxillaManager2, CLUSTER_NODE_COLUMNS.NODE_NAME);
		Assert.assertTrue(standbyNodeName.equals(nodeNameFromUi), "Node Name from UI did not match Node name "
				+ "used to create master, actual " + nodeNameFromUi);
		log.info("Checking node state for standby");
		String nodeStateFromUi = cluster.getNodeInfoTableColumn(driver, boxillaManager2, CLUSTER_NODE_COLUMNS.STATE);
		Assert.assertTrue(nodeStateFromUi.equals("standby"), "Node state was not standby, actual :" + nodeStateFromUi);
		
		log.info("Node name checked. Test complete");
	}
	
	@Test(priority=3)
	public void test03_checkOldConnection() {
		log.info("Checking connection that existed in master is now on master and slave");
		Database masterDb = new Database();
		masterDb.connectToDatabase(boxilla, dbUsername, dataPassword, "foreman");
	invisaPC.Connection masterCon = masterDb.findConnectionByName(oldConnection);
		masterDb.closeDatabase();
		
	Database slaveDb = new Database();
	slaveDb.connectToDatabase(boxillaManager2,dbUsername, dataPassword, "foreman");
		invisaPC.Connection slaveCon = slaveDb.findConnectionByName(oldConnection);
		slaveDb.closeDatabase();
		
		Assert.assertTrue(masterCon.equals(slaveCon), "Old connection was not added to slave DB");
	}
	
	@Test(priority=4)
	public void test04_checkOldUser() {
		log.info("Checking user that existed in master is now on master and slave");
		log.info("Username "+dbUsername+" and the password is "+dataPassword);
		Database masterDatabase = new Database();
		masterDatabase.connectToDatabase(boxilla, dbUsername, dataPassword, "foreman");
		invisaPC.User masterUser = masterDatabase.findUserByName(oldUser);
		masterDatabase.closeDatabase();
		Database slaveDatabase = new Database();
		slaveDatabase.connectToDatabase(boxillaManager2,dbUsername, dataPassword, "foreman");
		invisaPC.User slaveUser = slaveDatabase.findUserByName(oldUser);
		slaveDatabase.closeDatabase();
		log.info("Master User:" + masterUser.toString());
		log.info("Slave User:" + slaveUser.toString());
		Assert.assertTrue(masterUser.equals(slaveUser), "Master did not equal slave, actual master user " + masterUser.toString() + 
				" slave user " + slaveUser.toString());
		
	}
	
	@Test(priority=5)
	public void test05_checkOldDevice() {
		log.info("Checking device that existed in master is now on master and slave");
		Database masterDatabase = new Database();
		masterDatabase.connectToDatabase(boxilla, dbUsername, dataPassword, "foreman");
		invisaPC.Device device = masterDatabase.findDeviceByIp(txIp);
		masterDatabase.closeDatabase();
		log.info("Master device:" + device.toString());
		
		Database slaveDatabase = new Database();
		slaveDatabase.connectToDatabase(boxillaManager2,dbUsername, dataPassword, "foreman");
		invisaPC.Device slaveDevice = slaveDatabase.findDeviceByIp(txIp);
		slaveDatabase.closeDatabase();
		log.info("Slave Device:" + slaveDevice.toString());
		
		Assert.assertTrue(device.equals(slaveDevice), "Device on master did not match device on slave");
	}
	
	@Test(priority=6)
	public void test06_checkTableRowCount() {
		log.info("Checking both database tables for number of rows and comparing them");
		Database db = new Database();
		db.connectToDatabase(boxilla, "postgres", "Pitcher1@B_Box", "foreman");
		HashMap map = db.getAllTableCount(db.getAllTableNames());
		
		Database db2 = new Database();
		db2.connectToDatabase(boxillaManager2, "postgres", "Pitcher1@B_Box", "foreman");
		HashMap map2 = db2.getAllTableCount(db2.getAllTableNames());
		
		Assert.assertTrue(map.equals(map2), "Row counts for both databases did not match");	
		
	}
	
	//creates a user and 
	@Test(priority=7)
	public void test07_createUser() throws InterruptedException {
		log.info("Creating user and checking user is in both databases");
		String userName = "clusterUser";
		users.addUser(driver, userName, "admin", "admin");
		Thread.sleep(2000);
		Users.userPrivilegeAdmin(driver).click();
		log.info("User Privilege - Administrator selected");

		users.addUserNoTemplateAutoConnectOFF(driver, userName);
		//check user it both boxilla databases
		log.info("Checking if user exists in master and standby DB");
		Database masterDatabase = new Database();
		masterDatabase.connectToDatabase(boxilla, dbUsername, dataPassword, "foreman");
		invisaPC.User masterUser = masterDatabase.findUserByName(userName);
		masterDatabase.closeDatabase();
		Database slaveDatabase = new Database();
		slaveDatabase.connectToDatabase(boxillaManager2,dbUsername, dataPassword, "foreman");
		invisaPC.User slaveUser = slaveDatabase.findUserByName(userName);
		slaveDatabase.closeDatabase();
		
		Assert.assertTrue(masterUser.equals(slaveUser), "Master did not equal slave, actual master user " + masterUser.toString() + 
				" slave user " + slaveUser.toString());
		log.info("Slave and master database entries for users matched");
	}
	
	@Test(priority=8)
	public void test08_createConnection() throws InterruptedException {
		log.info("Creating connection and comparing databases");
		String connectionName = "clusterConnection";
		//create the connection
		connection.addConnection(driver, connectionName, "no"); // connection name, user template
		connection.connectionInfo(driver, "tx", "user","user", txIp); // connection via, name, host ip
		connection.chooseCoonectionType(driver, "private"); // connection type
		connection.enableExtendedDesktop(driver);
		connection.enableUSBRedirection(driver);
		connection.enableAudio(driver);
		connection.enablePersistenConnection(driver);
		connection.propertyInfoClickNext(driver);
		connection.saveConnection(driver, connectionName); // Connection name to assert
		
		Database masterDb = new Database();
		masterDb.connectToDatabase(boxilla, dbUsername, dataPassword, "foreman");
		invisaPC.Connection masterCon = masterDb.findConnectionByName(connectionName);
		masterDb.closeDatabase();
		
		Database slaveDb = new Database();
		slaveDb.connectToDatabase(boxillaManager2,dbUsername, dataPassword, "foreman");
		invisaPC.Connection slaveCon = slaveDb.findConnectionByName(connectionName);
		slaveDb.closeDatabase();
		
		log.info("Asserting if both connection objects match");
		Assert.assertTrue(masterCon.equals(slaveCon), "Master and salve connection objects did not match, actual master : " + 
		masterCon.toString() + " , slave : " + slaveCon.toString());
	}
	
	
	@Test(priority=9)
	public void test09_masterBoxillaDown() throws InterruptedException {
		log.info("Closing browser");
		driver.quit();
		//first copy the script and run it
		Thread t = new Thread() {
			public void run() {
				boxillaDown(boxilla);
			}
		};
		t.start();
		log.info("Master boxilla has been brought down for 5 minutes. Waiting 3 minutes then checking");
		Thread.sleep(300000);
		cleanUpLogin(virtualIp);
		cleanUpLogout();
	}
	
	@Test(priority=10)
	public void test10_detachStandBy() throws InterruptedException {
		cleanUpLogin(virtualIp);
		log.info("Attempting to detach standby boxilla");
		Thread.sleep(2000);
		cluster.detachStandBy(driver, boxilla);
		log.info("Checking node state for standby");
		String nodeStateFromUi = cluster.getNodeInfoTableColumn(driver, boxilla, CLUSTER_NODE_COLUMNS.STATE);
		Assert.assertTrue(nodeStateFromUi.equals("detached"), "Node state was not detached, actual :" + nodeStateFromUi);
		log.info("Boxilla has been detached. Checking if original IP is active");
		driver.quit();
	//	boxillaManager = "10.211.129.3";
		log.info("Waiting for original IP address to become active before trying to log in and out");
		Thread.sleep(120000);
		cleanUpLogin();
		cleanUpLogout();
		
	}
	
	@Test(priority=11)
	public void test11_reattachDetachedStandby() throws InterruptedException {
		cleanUpLogin(boxilla);
		cluster.prepareStandByBoxilla(driver, boxillaManager2, masterNodeId, masterNodeName);
		driver.quit();
		cleanUpLogin(virtualIp);
		log.info("Getting node ID from UI and asserting if it matches");
		String nodeIdFromUI = cluster.getNodeInfoTableColumn(driver, boxilla, CLUSTER_NODE_COLUMNS.NODE_ID);
		Assert.assertTrue(masterNodeId.equals(nodeIdFromUI), "node id from UI did not match node id used "
				+ "to create standby, actual " + nodeIdFromUI);
		log.info("Node ids match. Checking node name");
		String nodeNameFromUi = cluster.getNodeInfoTableColumn(driver, boxilla, CLUSTER_NODE_COLUMNS.NODE_NAME);
		Assert.assertTrue(masterNodeName.equals(nodeNameFromUi), "Node Name from UI did not match Node name "
				+ "used to create master, actual " + nodeNameFromUi);
		log.info("Checking node state for standby");
		String nodeStateFromUi = cluster.getNodeInfoTableColumn(driver, boxilla, CLUSTER_NODE_COLUMNS.STATE);
		Assert.assertTrue(nodeStateFromUi.equals("standby"), "Node state was not standby, actual :" + nodeStateFromUi);
	}
//	
//	@Test(priority=12)
//	public void test12_makeStandbyStandalone() throws InterruptedException {
//		cleanUpLogin(virtualIp);
//		cluster.makeStandbyStandAlone(driver, boxilla);
//		String nodeIdFromUI = cluster.getNodeInfoTableColumn(driver, boxillaManager2, CLUSTER_NODE_COLUMNS.NODE_ID);
//		Assert.assertFalse(masterNodeId.equals(nodeIdFromUI), "node id from UI contained standby details when it shouldnt have ");
//		driver.quit();
//		log.info("Waiting for original IP address to become active before trying to log in and out");
//		Thread.sleep(120000);
//		cleanUpLogin();
//		cleanUpLogout();
//	}
	
	
	
	private void boxillaDown(String boxillaIp) {
	//	ScpTo scp = new ScpTo();
	//	scp.scpTo("C:\\Test_Workstation\\SeleniumAutomation\\scripts\\upDown.sh", "root", boxillaIp, "barrow1admin_12", "/tmp/", "upDown.sh");
		Ssh shell = new Ssh("root", "barrow1admin_12", boxillaIp);
		shell.loginToServer();
		shell.sendCommandNoReturn("ifdown p3p1 && sleep 300 && ifup p3p1 &");
		//shell.sendCommand("chmod 777 /tmp/upDown.sh");
		//shell.sendCommandNoReturn("/tmp/upDown.sh&");
		//shell.sendCommand("/tmp/upDown.sh&");
		log.info("done");
		//shell.disconnect();
	}
	
	@Test(priority=12)
	public void test12_upgradeCluster () throws InterruptedException {
		//boxilla = 214
		log.info("Attempting to upgrade cluster");
		log.info("Detaching standby first");
		cluster.detachStandBy(driver, boxilla);		//detach 147
		String nodeStateFromUi = cluster.getNodeInfoTableColumn(driver, boxilla, CLUSTER_NODE_COLUMNS.STATE);  
		Assert.assertTrue(nodeStateFromUi.equals("detached"), "Node state was not standby, actual :" + nodeStateFromUi);  //check 147 detatched
		log.info("Standby detached. Logging into standby boxilla and upgrading");
		driver.quit();			//quit browser
		Thread.sleep(100000);
		boxillaManager = boxilla;  //boxilla manager = 147
		cleanUpLogin(boxillaManager2);
		system.systemUpgrade(driver, "bxa_3.0.0.4025.bbx", boxillaManager);   	//upgrade 147
		cluster.switchoverBoxilla(driver);			//switchover
		driver.quit();
		boxillaManager = boxilla; 			//boxilla = 214
		cleanUpLogin();
		//check 147 is active
		String nodeStateFromUiActive = cluster.getNodeInfoTableColumn(driver, boxilla, CLUSTER_NODE_COLUMNS.STATE);
		Assert.assertTrue(nodeStateFromUiActive.equals("active"), "Node state was not active, actual :" + nodeStateFromUiActive);
		log.info("Standby has been upgraded and is now master boxilla. Detaching new standby to upgrade");
		
		
		//detach 129.3
		cluster.detachStandBy(driver, "10.211.129.3");
		
		//check 129.3 is detatched
		String nodeStateFromUi2 = cluster.getNodeInfoTableColumn(driver, "10.211.129.3", CLUSTER_NODE_COLUMNS.STATE);
		Assert.assertTrue(nodeStateFromUi2.equals("detached"), "Node state was not standby, actual :" + nodeStateFromUi2);
		log.info("Standby detached. Logging into standby boxilla and upgrading");
		driver.quit();			//quit fire fox
		boxillaManager = boxillaManager2;		//boxilla = 129.3
		Thread.sleep(100000);
		cleanUpLogin();
		system.systemUpgrade(driver, "bxa_3.0.0.4025.bbx", boxillaManager2);		//upgrade 129.3
		Thread.sleep(5000);
		cluster.prepareStandByBoxilla(driver, boxilla, "4", "bxa4");
		driver.quit();
		boxillaManager = virtualIp;  	//boxilla = 214r
		cleanUpLogin();
		String nodeStateFromUiActive3 = cluster.getNodeInfoTableColumn(driver, boxillaManager2, CLUSTER_NODE_COLUMNS.STATE);
		Assert.assertTrue(nodeStateFromUiActive3.equals("standby"), "Node state was not standby, actual :" + nodeStateFromUiActive3);
		
	}
//	
//	@Test(priority=13)
//	public void test13_dissolveCluster() throws InterruptedException {
//		log.info("Attempting to dissolve cluster. First make standby standalone");
//		cluster.makeStandbyStandAlone(driver, boxillaManager2);
//		Thread.sleep(30000);
//		boxillaManager = boxillaManager2;
//		driver.quit();
//		cleanUpLogin();
//		driver.quit();
//		boxillaManager = virtualIp;
//		cleanUpLogin();
//		cluster.dissolveCluster(driver);
//		driver.quit();
//		Thread.sleep(30000);
//		boxillaManager = boxilla;
//		cleanUpLogin(boxillaManager2);
//		log.info("checking if VIP is still active");
//		boolean vipActive = isIpReachable(virtualIp);	
//		Assert.assertFalse(vipActive, "Virtual IP was still active. Dissolve cluster failed");
//		
//	}
	
	
//	
//	
	
	/**
	 * Overriding beforeClass in superclass to create connections to be used in tests.
	 * @throws InterruptedException 
	 * 
	 */
	@BeforeClass(alwaysRun = true)
	public void beforeClass() throws InterruptedException {
		
			getDevices();
			String originalBoxilla = boxillaManager;
//			boxillaManager = boxillaManager2;		//swap boxilla to set the license to be the same as original boxilla
			
			try {
//				cleanUpLogin();
//				system.deletLicense(driver);
//				system.addLicense(driver, "25", "300", boxillaManager);
//				cleanUpLogout();
//				boxillaManager = originalBoxilla;
			}catch(Exception e) {
				Utilities.captureScreenShot(driver, this.getClass().getName() + "_beforeClass", "Before Class");
				e.printStackTrace();
				cleanUpLogout();
				boxillaManager = originalBoxilla;
			}
			try {
				log.info("current boxilla is "+boxillaManager);
				cleanUpLogin(boxillaManager);
//				system.deletLicense(driver);
				system.addLicense(driver, "25", "300", boxillaManager);
				log.info("Add Connection Via TX, Private, All option enabled");
				connectmethods.addConnection(driver, "tx_testconnection1", "no"); // connection name, user template
				connectmethods.connectionInfo(driver, "tx", "user","user", txIp); // connection via, name, host ip
				connectmethods.chooseCoonectionType(driver, "private"); // connection type
				connectmethods.enableExtendedDesktop(driver);
				connectmethods.enableUSBRedirection(driver);
				connectmethods.enableAudio(driver);
				connectmethods.enablePersistenConnection(driver);
				connectmethods.propertyInfoClickNext(driver);
				connectmethods.saveConnection(driver, "tx_testconnection1"); // Connection name to assert
				log.debug("Connection Via TX Added Completed");
			}catch(Exception e) {
				Utilities.captureScreenShot(driver, this.getClass().getName() + "_beforeClass", "Before Class");
				e.printStackTrace();
				cleanUpLogout();
				boxillaManager = originalBoxilla;
			}
			cleanUpLogout();
			boxillaManager = originalBoxilla;
	}
//	
//	@Test(priority=10)
//	public void test10_bringFailedBoxillaOnline() throws InterruptedException {
//		Thread.sleep(100000);
//		String userName = "clusterUser2";
//		log.info("Creating a user and then Bringing failed boxilla back online");
//		users.addUser(driver, userName, "admin", "admin");
//		Thread.sleep(2000);
//		Users.userPrivilegeAdmin(driver).click();
//		log.info("User Privilege - Administrator selected");
////
//		users.addUserNoTemplateAutoConnectOFF(driver, userName);
//		cluster.prepareStandbyFailedBoxilla(driver, boxilla);
//		String nodeStateFromUi = cluster.getNodeInfoTableColumn(driver, boxilla, CLUSTER_NODE_COLUMNS.STATE);
//		Assert.assertTrue(nodeStateFromUi.equals("standby"), "Node state was not standby, actual :" + nodeStateFromUi);
//		log.info("Checking database for user created while boxilla was down");
//		
//		Database masterDatabase = new Database();
//		masterDatabase.connectToDatabase(boxilla, "postgres", "foreman", "foreman");
//		invisaPC.User masterUser = masterDatabase.findUserByName(userName);
//		masterDatabase.closeDatabase();
//		Database slaveDatabase = new Database();
//		slaveDatabase.connectToDatabase(boxillaManager2,"postgres", "foreman", "foreman");
//		invisaPC.User slaveUser = slaveDatabase.findUserByName(userName);
//		slaveDatabase.closeDatabase();
//		
//		Assert.assertTrue(masterUser.equals(slaveUser), "Master did not equal slave, actual master user " + masterUser.toString() + 
//				" slave user " + slaveUser.toString());
//	}
}
