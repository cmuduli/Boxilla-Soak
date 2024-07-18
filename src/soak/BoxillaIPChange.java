package soak;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import extra.Ssh;
import extra.StartupTestCase;
import extra.StartupTestCase2;
import methods.SystemMethods;
import objects.Landingpage;
import testNG.Utilities;

public class BoxillaIPChange extends StartupTestCase2 {

	private String extraIP;
	private SystemMethods methods = new SystemMethods();
	final static Logger log = Logger.getLogger(BoxillaIPChange.class);

	public BoxillaIPChange() throws IOException {
		extraIP = prop.getProperty("extraIP2");
	}
	
//	@Test
//	public void boxillaNIC2IpChange() throws InterruptedException {
//		String ip1 = "198.222.22.21";
//		String ip2 = "194.181.12.12";
//		
//		methods.addDualNic(driver, ip2, "255.255.255.0", "194.181.12.12");
//		Ssh ssh = new Ssh("root", "barrow1admin_12", boxillaManager);
//		ssh.loginToServer();
//		String output = ssh.sendCommand("ifconfig");
//		ssh.disconnect();
//		Assert.assertTrue(output.contains(ip2), "ifconfig did not contain new IP, actual:" + output );
//		
//		log.info("rever ip back");
//		methods.addDualNic(driver, ip1, "255.255.255.0", "198.222.22.1");
//		Ssh ssh2 = new Ssh("root", "barrow1admin_12", boxillaManager);
//		ssh2.loginToServer();
//		String output2 = ssh2.sendCommand("ifconfig");
//		ssh2.disconnect();
//		Assert.assertTrue(output2.contains(ip1), "ifconfig did not contain new IP, actual:" + output2 );
//	}
	
	@Test
	public void boxillaIPChange() throws InterruptedException { // Change Boxilla IP
		log.info("Test Case-92  - Change IP address of Boxilla unit");
		methods.navigateToSystemSettings(driver);
		// Change IP address to extra IP stored in excel
		methods.changeNetwork(driver, extraIP, boxillaManager, extraIP); 
		log.info("Changing IP address back to old IP");
		methods.timer(driver);
		//Thread.sleep(60000);
		methods.navigateToSystemSettings(driver);
		// Change IP address back to old IP
		methods.changeNetwork(driver, boxillaManager, boxillaManager, extraIP);
		log.info("Changing IP address of Boxilla - Test Case-92 Completed");
		if(methods.getIpFail() == true) {
			methods.setIpFail(false);
			Assert.fail("ip change failed");
		}
	}
	
	@AfterMethod(alwaysRun = true)
	public void logout(ITestResult result) throws Exception {
		// Taking screen shot on failure
		//String url = "https://" + boxillaManager + "/";
		String results = "";
		//print result
		if (ITestResult.FAILURE == result.getStatus()) {
		  results = "FAIL";
		  methods.changeNetwork(driver, boxillaManager, boxillaManager, extraIP);
		}
		super.logout(result);
			
	}
	
}
