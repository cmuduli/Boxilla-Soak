package methods;

import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import extra.Device;
import extra.SeleniumActions;
import extra.StartupTestCase2;
import northbound.delete.TerminateActiveConnection;
import objects.Connections;
import objects.Discovery;
import objects.Switch;
import objects.Users;
import objects.Zones;

public class ZoneMethods {

	
	final static Logger log = Logger.getLogger(ZoneMethods.class);
	
	public void navigateToZones(WebDriver driver) {
		log.info("Navigating to zones");
		SeleniumActions.seleniumClick(driver, Zones.getZoneDashboardLink());
		new WebDriverWait(driver, 120).until(ExpectedConditions.elementToBeClickable(By.xpath(Zones.getAddZoneButton())));
		log.info("Successfully navigated to Zones");
	}
	
	public void addZone(WebDriver driver, String name, String description) {
		log.info("Adding new zone with name:" + name);
		navigateToZones(driver);
		SeleniumActions.seleniumClick(driver, Zones.getAddZoneButton());
		new WebDriverWait(driver, 120).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Zones.getAddZoneModal())));
		SeleniumActions.seleniumSendKeysClear(driver, Zones.getAddZoneZoneNameTB());
		SeleniumActions.seleniumSendKeys(driver, Zones.getAddZoneZoneNameTB(), name);
		SeleniumActions.seleniumSendKeysClear(driver, Zones.getAddZoneZoneDescriptionTB());
		SeleniumActions.seleniumSendKeys(driver, Zones.getAddZoneZoneDescriptionTB(), description);
		SeleniumActions.seleniumClick(driver, Zones.getAddZoneSaveButton());
		
		//for toast message if one becomes available
//		new WebDriverWait(driver, 120).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.templateToastMessage)));
//		String toastText1 = SeleniumActions.seleniumGetText(driver, Connections.templateToastMessage);
//		String toastText2 = SeleniumActions.seleniumGetInnerText(driver, Connections.templateToastMessage);
//		log.info("TEXT1:" + toastText1);
//		log.info("TEXT2:" + toastText2);
		
		boolean available = isZoneAvailable(driver, name);
		Assert.assertTrue(available, "Zone with name:" + name + "was not created");
	}
	public String getZoneId(WebDriver driver, String zoneName) {
		String  id = SeleniumActions.seleniumGetText(driver, Zones.getAvailableZone(zoneName));
		//need to parse the return
		char first = id.charAt(0);
		id = Character.toString(first);
		log.info("Zone ID for zone:" + zoneName + " is ID:" + id);
		return id;
	}
	public boolean isZoneAvailable(WebDriver driver, String zoneName) {
		navigateToZones(driver);
		boolean isAvailable = SeleniumActions.seleniumIsDisplayed(driver, Zones.getAvailableZone(zoneName));
		log.info("Zone is available:" + isAvailable);
		return isAvailable;
	}
	public void editZone(WebDriver driver, String name, String newName, String newDescription) {
		navigateToZones(driver);
		if(isZoneAvailable(driver, name)) {
			SeleniumActions.seleniumClick(driver, Zones.getZoneEditButton(name));
			SeleniumActions.seleniumSendKeysClear(driver, Zones.getEditNameTextBox());
			SeleniumActions.seleniumSendKeys(driver, Zones.getEditNameTextBox(), newName);
			SeleniumActions.seleniumSendKeysClear(driver, Zones.getEditDescriptionTextBox());
			SeleniumActions.seleniumSendKeys(driver, Zones.getEditDescriptionTextBox(), newDescription);
			SeleniumActions.seleniumClick(driver, Zones.getEditZoneApplyButton());	
		}else {
			throw new AssertionError("Zone " + name + " is not available");
		}

	}
	
	public void editZoneFromDescription(WebDriver driver, String name, String newName, String newDescription) {
		navigateToZones(driver);
		if(isZoneAvailable(driver, name)) {
			SeleniumActions.seleniumClick(driver, Zones.getAvailableZone(name));
			SeleniumActions.seleniumClick(driver, Zones.getEditZoneFromDescriptionButton());
			SeleniumActions.seleniumSendKeysClear(driver, Zones.getEditNameTextBox());
			SeleniumActions.seleniumSendKeys(driver, Zones.getEditNameTextBox(), newName);
			SeleniumActions.seleniumSendKeysClear(driver, Zones.getEditDescriptionTextBox());
			SeleniumActions.seleniumSendKeys(driver, Zones.getEditDescriptionTextBox(), newDescription);
			SeleniumActions.seleniumClick(driver, Zones.getEditZoneApplyButton());	
		}else {
			throw new AssertionError("Zone " + name + " is not available");
		}
	}
	public void clickZoneSelectionTowerConnection(WebDriver driver, String zoneName) {
		String id = getZoneId(driver, zoneName);
		SeleniumActions.seleniumClick(driver, Zones.getConnectionTowerZone(id));
	}
	public void clickZoneSelectionTowerDevice(WebDriver driver, String zoneName) {
		String id = getZoneId(driver, zoneName);
		SeleniumActions.seleniumClick(driver, Zones.getDeviceTowerZone(id));
	}
	
	public void deleteZone(WebDriver driver, String name) {
		navigateToZones(driver);
		if(isZoneAvailable(driver, name)) {
			SeleniumActions.seleniumClick(driver, Zones.getZoneDeleteButton(name));
		}else {
			throw new AssertionError("Zone " + name + " is not available");
		}
		
		Alert alert = driver.switchTo().alert();
		alert.accept();
		new WebDriverWait(driver, 120).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Switch.spinnerXpath)));
		new WebDriverWait(driver, 120).until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(Switch.spinnerXpath)));
		boolean isDeleted = isZoneAvailable(driver, name);
		Assert.assertFalse(isDeleted, "Zone was not deleted");
		log.info("Zone " + name + " is deleted");
	}
	
	public void deleteZoneUnable(WebDriver driver, String name) {
		navigateToZones(driver);
		if(isZoneAvailable(driver, name)) {
			SeleniumActions.seleniumClick(driver, Zones.getZoneDeleteButton(name));
		}else {
			throw new AssertionError("Zone " + name + " is not available");
		}
		new WebDriverWait(driver, 120).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.templateToastMessage)));
		String toast = SeleniumActions.seleniumGetText(driver, Connections.templateToastMessage);
		Assert.assertTrue(toast.equals("Zone has devices or connections assigned. Removal forbidden."));
		
	}
	
	public String addZoneLimitReached(WebDriver driver) {
		navigateToZones(driver);
		SeleniumActions.seleniumClick(driver, Zones.getAddZoneButton());
		new WebDriverWait(driver, 120).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.templateToastMessage)));
		String toast = SeleniumActions.seleniumGetText(driver, Connections.templateToastMessage);
		return toast;
	}

	public String[] getZoneDetails(WebDriver driver, String name) {
		boolean isAvailable = isZoneAvailable(driver, name);
		if(isAvailable) {
			String[] details = new String[5];
			SeleniumActions.seleniumClick(driver, Zones.getAvailableZone(name));
			details[0] = SeleniumActions.seleniumGetText(driver, Zones.getDetailsZoneName());
			details[1] = SeleniumActions.seleniumGetText(driver, Zones.getDetailsZoneDescription());
			//users does not come out clean so we need to parse
			String users = SeleniumActions.seleniumGetText(driver, Zones.getDetailsZoneUsers());
			details[2] = users.substring(users.length() - 1);
			//connections sdoes not come out clean so we need to parse
			String connections = SeleniumActions.seleniumGetText(driver, Zones.getDetailsZoneConnections());
			details[3] = connections.substring(connections.length() -1);
			//devices does not come out clean so need to parse
			String devices = SeleniumActions.seleniumGetText(driver, Zones.getDetailsZoneDevices());
			details[4] = devices.substring(devices.length() - 1);
			log.info("zone name:" + details[0]);
			log.info("zone description:" + details[1]);
			log.info("Zone users:" + details[2]);
			log.info("Zone connections:" + details[3]);
			log.info("Zone devices:" + details[4]);
			return details;
		}else {
			throw new AssertionError("Zone " + name + " is not available");
		}
	}
	
//	public void assertZoneDetails(String[] details, String... expected) {
//		Assert.assertTrue(details[0].equals(details[0]), "Zone name from details did not equal " + zoneName + ". Actual:" + details[0]);
//		Assert.assertTrue(details[1].equals(details[1]), "Zone description from details did not equal " + zoneDescription + ". Actual:" + details[1]);
//		Assert.assertTrue(details[2].equals(details[2]), "Zone users from details did not equal " + zoneDescription + ". Actual:" + details[2]);
//		Assert.assertTrue(details[2].equals(details[3]), "Zone connections from details did not equal " + zoneDescription + ". Actual:" + details[2]);
//	}

	public void addDevicesToZone(WebDriver driver, String zoneName, String... devices) {
		String [] details = getZoneDetails(driver, zoneName);
		Assert.assertTrue(details[0].equals(zoneName), "Zone details did not match");
		
		for(String s : devices) {
			SeleniumActions.seleniumClick(driver, Zones.getAvailableDevices(s));
			Assert.assertTrue(isDeviceInActive(driver, s), "Device is not active");
		}
		log.info("Device has been added to active list.. Saving");
		SeleniumActions.seleniumClick(driver, Zones.getDevicesApplyButton());
		new WebDriverWait(driver, 120).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.templateToastMessage)));
		String toastText = SeleniumActions.seleniumGetText(driver, Connections.templateToastMessage);
		Assert.assertTrue(toastText.equals("Devices reassigned."), "Toast message was not Devices reassigned.  Actual:" + toastText);
		for(String s : devices) {
			Assert.assertTrue(isDeviceInActive(driver, s), "Device is not active");
		}
	}
	
	public void removeDevicesFromZone(WebDriver driver, String zoneName, String... devices) {
		String [] details = getZoneDetails(driver, zoneName);
		Assert.assertTrue(details[0].equals(zoneName), "Zone details did not match");
		
		for(String s : devices) {
			SeleniumActions.seleniumClick(driver, Zones.getActiveDevices(s));
			new WebDriverWait(driver, 120).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Zones.getAvailableDevices(s))));
		}
		log.info("Device(s) have been added to the available list");
		SeleniumActions.seleniumClick(driver, Zones.getDevicesApplyButton());
		new WebDriverWait(driver, 120).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.templateToastMessage)));
		String toastText = SeleniumActions.seleniumGetText(driver, Connections.templateToastMessage);
		Assert.assertTrue(toastText.equals("Devices reassigned."), "Toast message was not Devices reassigned.  Actual:" + toastText);
		for(String s : devices) {
			new WebDriverWait(driver, 120).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Zones.getAvailableDevices(s))));
		}
	}
	public void swapDeviceFromZone(WebDriver driver, String origZone, String newZone, String... deviceName) {
		getZoneDetails(driver, newZone);
		clickZoneSelectionTowerDevice(driver, origZone);
		for(String s : deviceName) {
			SeleniumActions.seleniumClick(driver, Zones.getAvailableDevices(s));
			Assert.assertTrue(isDeviceInActive(driver, s), "Device is not active");
		}
		log.info("Device has been added to active list.. Saving");
		SeleniumActions.seleniumClick(driver, Zones.getDevicesApplyButton());
		new WebDriverWait(driver, 120).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.templateToastMessage)));
		String toastText = SeleniumActions.seleniumGetText(driver, Connections.templateToastMessage);
		Assert.assertTrue(toastText.equals("Devices reassigned."), "Toast message was not Devices reassigned.  Actual:" + toastText);

		
	}
	
	public void swapConnectionFromZone(WebDriver driver, String origZone, String newZone, String... connectionName) {
		getZoneDetails(driver, newZone);
		clickZoneSelectionTowerConnection(driver, origZone);
		for(String s : connectionName) {
			SeleniumActions.seleniumClick(driver, Zones.getAvailableConnection(s));
			Assert.assertTrue(isConnectionInActive(driver, s ), "Connection was not active" );
		}
		SeleniumActions.seleniumClick(driver, Zones.getConnectionsApplyButton());
		new WebDriverWait(driver, 120).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.templateToastMessage)));
		String toastText = SeleniumActions.seleniumGetText(driver, Connections.templateToastMessage);
		Assert.assertTrue(toastText.equals("Connections reassigned."), "Toast message was not Connections reassigned.  Actual:" + toastText);
	}
	
	public void addConnectionsToZone(WebDriver driver, String zoneName, String... connectionName) {
		String [] details = getZoneDetails(driver, zoneName);
		Assert.assertTrue(details[0].equals(zoneName), "Zone details did not match");

		for(String s : connectionName) {
			SeleniumActions.seleniumClick(driver, Zones.getAvailableConnection(s));
			Assert.assertTrue(isConnectionInActive(driver, s ), "Connection was not active" );
		}
		log.info("Connection has been added to active list. Saving");
		SeleniumActions.seleniumClick(driver, Zones.getConnectionsApplyButton());
		new WebDriverWait(driver, 120).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.templateToastMessage)));
		String toastText = SeleniumActions.seleniumGetText(driver, Connections.templateToastMessage);
		Assert.assertTrue(toastText.equals("Connections reassigned."), "Toast message was not Connections reassigned.  Actual:" + toastText);
		for(String s : connectionName) {
			Assert.assertTrue(isConnectionInActive(driver, s ), "Connection was not active" );	
		}
	}
	
	public boolean isConnectionInActive(WebDriver driver, String name) {
		boolean isAvail = SeleniumActions.seleniumIsDisplayed(driver, Zones.getAvailableConnection(name));
		boolean isActive = SeleniumActions.seleniumIsDisplayed(driver, Zones.getActiveConnection(name));
		if(!isAvail && isActive) {
			return true;
		}
		return false;
	}
	
	public boolean isDeviceInActive(WebDriver driver, String name) {
		boolean isAvail = SeleniumActions.seleniumIsDisplayed(driver, Zones.getAvailableDevices(name));
		boolean isActive = SeleniumActions.seleniumIsDisplayed(driver, Zones.getActiveDevices(name));
		if(!isAvail && isActive) {
			return true;
		}
		return false;
	}
	
	public boolean isDeviceAssignedToZone(String zoneName, String deviceName, WebDriver driver) {
		getZoneDetails(driver, zoneName);
		return isDeviceInActive(driver, deviceName);
	}
	
	public boolean isConnectionAssignedToZone(String zoneName, String connectionName, WebDriver driver) {
		getZoneDetails(driver, zoneName);
		return isConnectionInActive(driver, connectionName);
		
		
	}
	
	
	public void removeConnectionsFromZone(WebDriver driver, String zoneName, String... connectionName) {
		String [] details = getZoneDetails(driver, zoneName);
		Assert.assertTrue(details[0].equals(zoneName), "Zone details did not match");
		for(String s : connectionName) {
		SeleniumActions.seleniumClick(driver, Zones.getActiveConnection(s));
			new WebDriverWait(driver, 120).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Zones.getAvailableConnection(s))));
		}
		log.info("Connection has been added to available list. Saving");
		SeleniumActions.seleniumClick(driver, Zones.getConnectionsApplyButton());
		new WebDriverWait(driver, 120).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Connections.templateToastMessage)));
		String toastText = SeleniumActions.seleniumGetText(driver, Connections.templateToastMessage);
		Assert.assertTrue(toastText.equals("Connections reassigned."), "Toast message was not Connections reassigned.  Actual:" + toastText);
		for(String s : connectionName) {
			new WebDriverWait(driver, 120).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Zones.getAvailableConnection(s))));
		}
	}
	
	public boolean isUserFavouritesInZone(WebDriver driver, String zoneName, String username) {
		getZoneDetails(driver, zoneName);
		boolean isAvail = SeleniumActions.seleniumIsDisplayed(driver, Zones.getUserNameFromZoneFav(username));
		return isAvail;
	}
	
	public void navigateToUserFavourites(WebDriver driver, String zoneName, String username) {
		boolean isAvailable = isUserFavouritesInZone(driver, zoneName, username);
		
		if(isAvailable) {
			SeleniumActions.seleniumClick(driver, Zones.getUserFavouriteButton(username));
			new WebDriverWait(driver, 120).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Zones.getFavouriteModal())));
			
		}
	}
	
	public void setUserFavourite(WebDriver driver, String zoneName, String username, String connectionName, int slot) {
		navigateToUserFavourites( driver,  zoneName,  username);
		SeleniumActions.seleniumDropdown(driver, Users.getFavouriteDropdown(slot), connectionName);
		SeleniumActions.seleniumClick(driver, Users.favouritesPopupSave);
		new WebDriverWait(driver, 120).until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(Zones.getFavouriteModal())));
	}
	
	public String[] getUserFavourites(WebDriver driver, String zoneName, String username) {
		navigateToUserFavourites( driver,  zoneName,  username);
		String[] connectionList = new String[10];
		for(int j=0; j < connectionList.length; j++) {
			WebElement e = driver.findElement(By.xpath(Users.getFavouriteDropdown(j)));
			Select select = new Select(e);
			WebElement option = select.getFirstSelectedOption();
			connectionList[j] = option.getText();
			log.info("Slot:" + j +  ", Connection:" + connectionList[j]);
		}
		SeleniumActions.seleniumClick(driver, Zones.getFavouriteModalCloseButton());
		return connectionList;
	}
	public void unallocateConnection(WebDriver driver, String zoneName, String username, int slot) {
		navigateToUserFavourites( driver,  zoneName,  username);
		SeleniumActions.seleniumDropdown(driver, Users.getFavouriteDropdown(slot), "unallocated");
		SeleniumActions.seleniumClick(driver, Users.favouritesPopupSave);
		new WebDriverWait(driver, 120).until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(Zones.getFavouriteModal())));
	}
	
	

	
}
