package methods;

import org.apache.log4j.Logger;
import org.eclipse.jetty.util.log.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import autoRefresh.AutoRefreshUser;
import extra.SeleniumActions;
import objects.Landingpage;

public class DashboardMethods {
	
	final static Logger log = Logger.getLogger(DashboardMethods.class);
	
	public String getConnectionNameFromNetworkBWTable(WebDriver driver) {
		navigateToDashboard(driver);
		WebElement ele = driver.findElement(By.xpath(Landingpage.getNetworkBwTableConName()));
		Actions action = new Actions(driver);
		action.moveToElement(ele);
		//Thread.sleep(3000);
		String name = SeleniumActions.seleniumGetText(driver, Landingpage.getNetworkBwTableConName());
		log.info("Connection name from network bandwidth table:" + name);
		return name;
		
	}
	public String getDashboardBandwidth(WebDriver driver) {
		navigateToDashboard(driver);
		String bw = SeleniumActions.seleniumGetText(driver, Landingpage.getDashboardBandwith());
		log.info("Bandwidth from performance tab on dashboard:" + bw);
		return bw;
	}
	
	public void navigateToDashboard(WebDriver driver) {
		Landingpage.dashboard(driver).click();
		new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Landingpage.getDashboardHeading())));
	}
	
}
