package extra;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import testNG.Utilities;

public class SeleniumActions {
	
	final static Logger log = Logger.getLogger(SeleniumActions.class);
	static int screenShotcounter = 0;
	static ArrayList<String> screenshotList = new ArrayList<String>();
	
	private static List<WebElement> getCorrectElement(WebDriver driver, String location) {
		List<WebElement> e = null;
			e = driver.findElements(By.xpath(location));
			if(e.size() == 0) {
				e = driver.findElements(By.linkText(location));
				return e;
			}
			return e;
	}
	//DONT NOW USE. CAUSES ISSUES WITH BOXILLA
	private static void highlightElement(WebDriver driver, WebElement ele) {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].setAttribute('style', 'border: 4px solid red;');", ele);
	}
	public static void takeScreenshot(WebDriver driver) {
		boolean run = false;
		if(run) {
			screenShotcounter++;
			String fileName = "./test-output/Screenshots/" + "giffy" + screenShotcounter + ".png";
			screenshotList.add(fileName);
			Utilities.captureScreenShot(driver, "giffy" + screenShotcounter, "test");
		}
	}
	
	public static List<WebElement> getListOfElements(WebDriver driver, String xpath) {
		List<WebElement> e = null;
		e = driver.findElements(By.xpath(xpath));
		return e;
	}
	public static void exectuteJavaScriptClick(WebDriver driver, WebElement ele) {
		log.info("Clicking using javascript");
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].click()", ele);
		takeScreenshot(driver);
		
	}
	
	public static void refreshPage(WebDriver driver) throws InterruptedException {
		Thread.sleep(1000);
		driver.navigate().refresh();
		Thread.sleep(1000);
		takeScreenshot(driver);
		
	}
	public static String seleniumDropdownGetText(WebDriver driver, String xpath) {
		Select select = new Select(driver.findElement(By.xpath(xpath)));
		WebElement option = select.getFirstSelectedOption();
		String optionText = option.getText();
		takeScreenshot(driver);
		return optionText;
		
	}
	public static void seleniumDropdown(WebDriver driver, String xpath, String value) {
		List<WebElement> elements = getCorrectElement(driver, xpath);
		
		for(WebElement e : elements) {
			try {
				if(e.isDisplayed()) {
					log.info("Selecting " + value + " from " + xpath);
					Select select = new Select(e);
					select.selectByVisibleText(value);
					takeScreenshot(driver);
					break;
				}
			}catch(Exception e1){
				System.out.println("Not the correct element");
			}
		}
	}
	public static boolean seleniumIsDisplayed(WebDriver driver, String xpath) {
		log.info("Checking if " + xpath + " is displayed");
		try {
			WebElement e = driver.findElement(By.xpath(xpath));
			e.isDisplayed();
			takeScreenshot(driver);
			return true;
		}catch(Exception e1) {
			return false;
		}

		
	}
	public static boolean seleniumIsEnabled(WebDriver driver, String xpath) {
		log.info("Checking if " + xpath + " is enabled");
		try {
			WebElement e = driver.findElement(By.xpath(xpath));
			takeScreenshot(driver);
			return e.isEnabled();
		}catch(Exception e) {
			takeScreenshot(driver);
			return false;
		}
	}
	
	public static String getAttribute(WebDriver driver, String xpath, String attribute) {
		log.info("Getting attribute:" + attribute + " from element " + xpath);
		WebElement e = driver.findElement(By.xpath(xpath));
		String attReturn = e.getAttribute(attribute);
		log.info(attribute + ":" + attReturn);
		return attReturn;
	}
	
	public static void seleniumClick(WebDriver driver, String xpath) {
		List<WebElement> elements = getCorrectElement(driver, xpath);
		int counter = 0;
		for(WebElement e : elements) {
			while(counter < 21) {
			try {
				log.info("Clicking " + xpath);
				//((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", e);
				e.click();
				takeScreenshot(driver);
				log.info("Clicked successfully");
				counter = 21;
			}catch(Exception ex) {
				log.info("Unable to click. May cause a test failure if no success message:" + xpath);
				//ex.printStackTrace();
				counter ++;
				if(counter > 20) {
					throw new AssertionError("Unable to click " + xpath);
				}
			}
			}
		}
	}
	public static String seleniumGetText(WebDriver driver, String xpath) {
		List<WebElement> elements = getCorrectElement(driver, xpath);
		String text = "";
		for(WebElement e : elements) {
			try {
				log.info("Getting text from  " + xpath);
				text = e.getText();
				takeScreenshot(driver);
			}catch(Exception ex) {

			}
		}
		return text;
	}
	
	/**
	 * Get text will not always work. This method can be used instead
	 * @param driver
	 * @param xpath
	 * @return
	 */
	public static String seleniumGetInnerText(WebDriver driver, String xpath) {
		WebElement e = driver.findElement(By.xpath(xpath));
		String text = e.getAttribute("innerText");
		if(text.equals("") || text == null) 
			throw new AssertionError("Text was empty or null: ");
		log.info("Inner text:" + text);
		return text;
	}
	
	public static void seleniumSendKeys(WebDriver driver, String xpath, String text) {
		List<WebElement> elements = getCorrectElement(driver, xpath);
		for(WebElement e : elements) {
			try {
				//e.clear();
				e.sendKeys(text);
				log.info("Sending " + text + " to " + xpath);
				takeScreenshot(driver);
				break;
			}catch(Exception ex) {

			}
		}
	}
	
	public static void seleniumSendKeysClear(WebDriver driver, String xpath) {
		List<WebElement> elements = driver.findElements(By.xpath(xpath));
		for(WebElement e : elements) {
			try {
				e.clear();
				takeScreenshot(driver);
				break;
			}catch(Exception ex) {

			}
		}
	}
	
	public static void dragAndDrop(WebDriver driver, WebElement source, WebElement destination) {
		Actions builder = new Actions(driver);

		Action dragAndDrop = builder.clickAndHold(source)
		   .moveToElement(destination)
		   .release(destination)
		   .build();

		dragAndDrop.perform();
	}
	
	public static void dragAndDrop(WebDriver driver, String source, String destination) {
		List<WebElement> sourceElements = driver.findElements(By.xpath(source));
		List<WebElement> destinationElements = driver.findElements(By.xpath(destination));
		WebElement sourceEle = null;
		WebElement desEle = null;
		for(WebElement e : sourceElements) {
			if(e.isDisplayed()) {
				sourceEle = e;
			}
		}
		for(WebElement d : destinationElements) {
			if(d.isDisplayed()) {
				desEle = d;
			}
		}
		
		Actions builder = new Actions(driver);

		Action dragAndDrop = builder.clickAndHold(sourceEle)
		   .moveToElement(desEle)
		   .release(desEle)
		   .build();

		dragAndDrop.perform();
		try {
			Thread.sleep(1000);
		}catch(Exception e) {
			
		}
	}
	
	public static WebElement getElement(WebDriver driver, String xpath) {
		return driver.findElement(By.xpath(xpath));
	}
	
//	public static void seleniumClick(WebDriver driver, WebElement element) {
//			log.info("Clicking " + element.);
//			element.click();
//			
//	}

}
