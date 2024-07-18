package methods;

import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import extra.SeleniumActions;
import invisaPC.Rest.Alerts;
import objects.Cluster;
import objects.Cluster.CLUSTER_INFO_TABLE_COLUMNS;
import objects.Cluster.CLUSTER_NODE_COLUMNS;
import objects.Landingpage;
import objects.Switch;

public class ClusterMethods {
	
	final static Logger log = Logger.getLogger(ClusterMethods.class);
	
	public void navigateToCluster(WebDriver driver) {
		log.info("Attempting to navigate to cluster");
		SeleniumActions.seleniumClick(driver, Landingpage.clusterTabXpath);
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Cluster.getNodeInfoTabXpath())));
		log.info("Successfully navigated to cluster");
	}
	
	public void prepareStandByBoxilla(WebDriver driver, String masterBoxillaIp, String nodeId, String nodeName) throws InterruptedException {
		log.info("Attempting to prepare active standby");
		navigateToCluster(driver);
		Thread.sleep(2000);
		SeleniumActions.seleniumClick(driver, Cluster.getPrepareStandByBtnXpath());
		log.info("Waiting for prepare active standby pop up to appear");
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(Cluster.getStandByPrepareStandbyBtn(driver)));
		log.info("Pop up appeared. Entering details");
		//clear textboxes
		SeleniumActions.seleniumSendKeysClear(driver, Cluster.getStandyMasterBoxillaIpTextBoxXpath());
		SeleniumActions.seleniumSendKeysClear(driver, Cluster.getStandByNodeIdTextBoxXpath());
		SeleniumActions.seleniumSendKeysClear(driver, Cluster.getStandByNodeNameTextBoxXpath());
		
		SeleniumActions.seleniumSendKeys(driver, Cluster.getStandyMasterBoxillaIpTextBoxXpath(), masterBoxillaIp);
		SeleniumActions.seleniumSendKeys(driver, Cluster.getStandByNodeIdTextBoxXpath(), nodeId);
		SeleniumActions.seleniumSendKeys(driver, Cluster.getStandByNodeNameTextBoxXpath(), nodeName);
		SeleniumActions.seleniumClick(driver, Cluster.getStandByPrepareStandbyBtnXpath());
		log.info("Waiting for spinner to appear");
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getSpinner(driver)));
		log.info("Spinner appeared. Waiting for it to disappear");
		new WebDriverWait(driver,180).until(ExpectedConditions.invisibilityOf(Switch.getSpinner(driver)));
	}
	
	public void prepareMasterBoxilla(WebDriver driver, String clusterId, String virtualIp, String nodeId, String nodeName) {
		log.info("Attempting to prepare master boxilla");
		navigateToCluster(driver);
		SeleniumActions.seleniumClick(driver, Cluster.getPrepareMasterBtnXpath());
		log.info("Waiting for pop up to appear");
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(Cluster.getMasterPrepPrepareMasterBtn(driver)));
		log.info("Pop up appeared. Entering master details");
		SeleniumActions.seleniumSendKeys(driver, Cluster.getMasterPrepClusterIdTextBoxXpath(), clusterId);
		SeleniumActions.seleniumSendKeys(driver, Cluster.getMasterPrepVirtualIpTextBoxXpath(), virtualIp);
		SeleniumActions.seleniumSendKeys(driver, Cluster.getMasterPrepNodeIdTextBoxXpath(), nodeId);
		SeleniumActions.seleniumSendKeys(driver, Cluster.getMasterPrepNodeNameTextBoxXpath(), nodeName);
		SeleniumActions.seleniumClick(driver, Cluster.getMasterPrepPrepareMasterBtnXpath());
		log.info("Waiting for spinner to appear");
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getSpinner(driver)));
		log.info("Spinner appeared. Waiting for it to disappear");
		new WebDriverWait(driver,180).until(ExpectedConditions.invisibilityOf(Switch.getSpinner(driver)));
		log.info("Checking toast");
//		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Cluster.getToastNotification(driver)));
//		log.info("Toast message appeared. Getting text");
//		String toast = SeleniumActions.seleniumGetText(driver, Cluster.getToastNotificationXpath());
//		log.info("Toast text:" + toast);
//		Assert.assertTrue(toast.contains("Master Boxilla successfully prepared"), "Toast did not contain Master Boxilla successfully prepared");
		log.info("Cluster dissolved successfully");
	}
	
	public void dissolveCluster(WebDriver driver) {
		log.info("Attempting to dissolve cluster");
		navigateToCluster(driver);
		SeleniumActions.seleniumClick(driver, Cluster.getDissolveCluserBtnXpath());
		Alert alert = driver.switchTo().alert();
		alert.accept();
		log.info("Waiting for spinner to appear");
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getSpinner(driver)));
		log.info("Spinner appeared. Waiting for it to disappear");
		new WebDriverWait(driver,180).until(ExpectedConditions.invisibilityOf(Switch.getSpinner(driver)));
		log.info("Spinner has disappeared. Getting toast notification");
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Cluster.getToastNotification(driver)));
		log.info("Toast message appeared. Getting text and asserting ");
		String toast = SeleniumActions.seleniumGetText(driver, Cluster.getToastNotificationXpath());
		log.info("Toast message: " + toast);
		Assert.assertTrue(toast.contains("Boxilla cluster has been successfully dissolve"), "Toast did not contain success");	
	}
	
	public void searchNodeInfo(WebDriver driver, String search) {
		log.info("Attempting to search node info");
		navigateToCluster(driver);
		SeleniumActions.seleniumSendKeysClear(driver, Cluster.getNodeInfoSearchBoxXpath());
		SeleniumActions.seleniumSendKeys(driver, Cluster.getNodeInfoSearchBoxXpath(), search);
	}
	public void searchClusterInfoTable(WebDriver driver, String search) {
		log.info("Attempting to search cluster info table");
		navigateToCluster(driver);
		SeleniumActions.seleniumClick(driver, Cluster.getClusterInfoTabXpath());
		SeleniumActions.seleniumSendKeysClear(driver, Cluster.getClusterInfoSearchBoxXpath());
		SeleniumActions.seleniumSendKeys(driver, Cluster.getClusterInfoSearchBoxXpath(), search);
	}
	
	public String getClusterInfoTableColumn(WebDriver driver, String search, CLUSTER_INFO_TABLE_COLUMNS columns) throws InterruptedException {
		searchClusterInfoTable(driver, search);
		int columnIndex = 1;

		switch(columns) {
		case CLUSTER_ID:
			log.info("Getting cluster ID from table");
			columnIndex = 1;
			break;
		case VIRTUAL_IP :
			log.info("Getting virtual IP from table");
			columnIndex = 2;
			break;
		}
		Thread.sleep(3000);
		String textFromTable = Cluster.getClusterInfoTableColumn(driver, columnIndex).getText();
		Thread.sleep(3000);
		log.info("value from table : " + textFromTable);
		return textFromTable;	
	}
	
	public String getNodeInfoTableColumn(WebDriver driver,String search, CLUSTER_NODE_COLUMNS column) throws InterruptedException {
		searchNodeInfo(driver, search);
		int columnIndex = 1;

		switch(column) {
		case IP_ADDRESS :
			log.info("Getting IP Address from table");
			columnIndex = 1;
			break;
		case MAC :
			log.info("Getting MAC from table");
			columnIndex = 2;
			break;
		case HOST :
			log.info("Getting Host from table");
			columnIndex = 5;
			break;
		case SOFTWARE_VERSION :
			log.info("Getting software version from table");
			columnIndex = 6;
			break;
		case LIMIT:
			log.info("Getting limit from table");
			columnIndex = 7;
			break;
		case STATE:
			log.info("Getting state from table");
			columnIndex = 9;
			break;

			//when boxilla is a master
		case NODE_ID :
			log.info("Getting Node ID from table");
			columnIndex = 3;
			break;
		case NODE_NAME :
			log.info("Getting Node Name from table");
			columnIndex = 4;
			break;
		}
		Thread.sleep(3000);
		String textFromTable = Cluster.getNodeInfoTableColumn(driver, columnIndex).getText();
		log.info("value from table : " + textFromTable);
		return textFromTable;	
	}
	public void makeMasterStandAlone(WebDriver driver) {
		
	}
	
	public void makeStandbyStandAlone(WebDriver driver, String boxillaIp) throws InterruptedException {
		log.info("Attemping to make standby boxilla with IP " + boxillaIp + " standalone");
		searchNodeInfo(driver, "standby");
		Thread.sleep(2000);
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(Cluster.getNodeDropdownBtn(driver)));
		SeleniumActions.seleniumClick(driver, Cluster.getNodeDropdownBtnXpath());
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(Cluster.getMakeStandbyStandaloneDropdownBtnXpath(driver)));
		SeleniumActions.seleniumClick(driver, Cluster.getMakeStandbyStandaloneDropdownBtnXpath());
		Alert alert = driver.switchTo().alert();
		alert.accept();
		log.info("Waiting for spinner to appear");
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getSpinner(driver)));
		log.info("Spinner appeared. Waiting for it to disappear");
		new WebDriverWait(driver,180).until(ExpectedConditions.invisibilityOf(Switch.getSpinner(driver)));
		log.info("Toast message appeared. Getting text and asserting ");
		String toast = SeleniumActions.seleniumGetText(driver, Cluster.getToastNotificationXpath());
		log.info("Toast message: " + toast);
		Assert.assertTrue(toast.contains("Standalone Boxilla successfully prepared"), "Toast did not contain 'Standalone Boxilla successfully prepared', actual " + toast);
		log.info("Stanadby successfully ");	
	}
	public void prepareStandbyFailedBoxilla(WebDriver driver, String boxillaIp) {
		log.info("Attemping to make a failed boxilla with IP " + boxillaIp + " standby");
		searchNodeInfo(driver, boxillaIp);
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(Cluster.getNodeDropdownBtn(driver)));
		SeleniumActions.seleniumClick(driver, Cluster.getNodeDropdownBtnXpath()); 
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(Cluster.getPrepareStandbyFailedBtn(driver)));
		SeleniumActions.seleniumClick(driver, Cluster.getPrepareStandbyFailedBtnXpath());
		Alert alert = driver.switchTo().alert();
		alert.accept();
		log.info("Waiting for spinner to appear");
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getSpinner(driver)));
		log.info("Spinner appeared. Waiting for it to disappear");
		new WebDriverWait(driver,180).until(ExpectedConditions.invisibilityOf(Switch.getSpinner(driver)));
		log.info("Toast message appeared. Getting text and asserting ");
		String toast = SeleniumActions.seleniumGetText(driver, Cluster.getToastNotificationXpath());
		log.info("Toast message: " + toast);
		Assert.assertTrue(toast.contains("Standby Boxilla successfully prepared"), "Toast message did not contain 'Standby Boxilla successfully prepared', actual, " + toast);
		log.info("Failed boxilla is now standby again");
	}
	
	public void detachStandBy(WebDriver driver, String boxillaIp) throws InterruptedException {
		log.info("Attemping to detach standby boxilla with IP " + boxillaIp);
		Thread.sleep(2000);
		searchNodeInfo(driver, boxillaIp);
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(Cluster.getNodeDropdownBtn(driver)));
		SeleniumActions.seleniumClick(driver, Cluster.getNodeDropdownBtnXpath());
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(Cluster.getDetachStandbyDropdownBtn(driver)));
		SeleniumActions.seleniumClick(driver, Cluster.getDetachStandbyDropdownBtnXpath());
		Alert alert = driver.switchTo().alert();
		alert.accept();
		log.info("Waiting for spinner to appear");
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getSpinner(driver)));
		log.info("Spinner appeared. Waiting for it to disappear");
		new WebDriverWait(driver,180).until(ExpectedConditions.invisibilityOf(Switch.getSpinner(driver)));
		log.info("Spinner has disappeared.");
//		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Cluster.getToastNotification(driver)));
//		log.info("Toast message appeared. Getting text and asserting ");
//		String toast = SeleniumActions.seleniumGetText(driver, Cluster.getToastNotificationXpath());
//		log.info("Toast message: " + toast);
//		Assert.assertTrue(toast.contains("Boxilla successfully detached"), "Toast message did not contain Boxilla successfully detached, actual " + toast);
		log.info("Successfully detached standby");
	}
	public void detachFailedBoxilla(WebDriver driver, String boxillaIp) {
		log.info("Attemping to detach failed boxilla with IP " + boxillaIp);
		searchNodeInfo(driver, boxillaIp);
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(Cluster.getNodeDropdownBtn(driver)));
		SeleniumActions.seleniumClick(driver, Cluster.getNodeDropdownBtnXpath());
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(Cluster.getDetachFailedBtn(driver)));
		SeleniumActions.seleniumClick(driver, Cluster.detachFailedBtnXpath);
		Alert alert = driver.switchTo().alert();
		alert.accept();
		log.info("Waiting for spinner to appear");
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getSpinner(driver)));
		log.info("Spinner appeared. Waiting for it to disappear");
		new WebDriverWait(driver,180).until(ExpectedConditions.invisibilityOf(Switch.getSpinner(driver)));
		log.info("Spinner has disappeared. Getting toast notification");
	//	new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Cluster.getToastNotification(driver)));
//		log.info("Toast message appeared. Getting text and asserting ");
//		String toast = SeleniumActions.seleniumGetText(driver, Cluster.getToastNotificationXpath());
//		log.info("Toast message: " + toast);
//		Assert.assertTrue(toast.contains("Boxilla successfully detached"), "Toast message did not contain 'Boxilla successfully detached', actual, " + toast);
		log.info("Boxilla with IP " + boxillaIp + " detached from cluster");
	}
	
	public void switchoverBoxilla(WebDriver driver) {
		log.info("Attempting to switchover boxilla");
		navigateToCluster(driver);
		SeleniumActions.seleniumClick(driver, Cluster.getNodeDropdownBtnXpath());
		log.info("node button clicked");
		SeleniumActions.seleniumClick(driver, Cluster.getSwitchoverBtnpath());
		driver.switchTo().alert().accept();
		log.info("switchover button clicked");
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(Cluster.getSwitchoverFormBtn(driver)));
		SeleniumActions.seleniumClick(driver, Cluster.getSwitchoverFormBtnXpath());
		
		log.info("Waiting for spinner to appear");
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getSpinner(driver)));
		log.info("Spinner appeared. Waiting for it to disappear");
		new WebDriverWait(driver,180).until(ExpectedConditions.invisibilityOf(Switch.getSpinner(driver)));
		log.info("Switchover complete");
		
		
		
	}
	
	public void makeFailedBoxillaStandalone(WebDriver driver, String boxillaIp) {
		log.info("Attemping to make failed boxilla with IP " + boxillaIp + " a standalone boxilla");
		searchNodeInfo(driver, boxillaIp);
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(Cluster.getNodeDropdownBtn(driver)));
		SeleniumActions.seleniumClick(driver, Cluster.getNodeDropdownBtnXpath());
		new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(Cluster.getMakeFailedStandAlone(driver)));
		SeleniumActions.seleniumClick(driver, Cluster.getMakeFailedStandAloneXpath());
		Alert alert = driver.switchTo().alert();
		alert.accept();
		log.info("Waiting for spinner to appear");
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Switch.getSpinner(driver)));
		log.info("Spinner appeared. Waiting for it to disappear");
		new WebDriverWait(driver,180).until(ExpectedConditions.invisibilityOf(Switch.getSpinner(driver)));
		log.info("Spinner has disappeared. Getting toast notification");
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(Cluster.getToastNotification(driver)));
		log.info("Toast message appeared. Getting text and asserting ");
		String toast = SeleniumActions.seleniumGetText(driver, Cluster.getToastNotificationXpath());
		log.info("Toast message: " + toast);
		Assert.assertTrue(toast.contains("Standalone Boxilla successfully prepared"),
				"toast did not contain 'Standalone Boxilla successfully prepared', actual ," + toast);
		log.info("Failed boxilla has been made standalone");
	}
}
