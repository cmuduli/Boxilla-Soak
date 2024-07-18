package device;

import org.apache.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import extra.StartupTestCase;
import extra.StartupTestCase2;
import methods.DevicesMethods;
import methods.DiscoveryMethods;

public class Unmanage extends StartupTestCase {
	
	private DiscoveryMethods discoveryMethods = new DiscoveryMethods();
	private DevicesMethods device = new DevicesMethods();
	final static Logger log = Logger.getLogger(Unmanage.class);
	private String deviceName = "";
	private String deviceMac = "";
	
	@Test
	public void test01_unmanageManage() throws InterruptedException {
		log.info("Unmanaging device");
		device.unManageDevice(driver, rxIp);
		log.info("Device unmanaged. Waiting 60 seconds for device to reboot then trying to manage again");
		Thread.sleep(120000);
		discoveryMethods.discoverDevices(driver);
		discoveryMethods.stateAndIPcheck(driver, deviceMac, prop.getProperty("ipCheck"),
				rxIp, rxSingle.getGateway(),rxSingle.getNetmask());
		discoveryMethods.manageApplianceAutomatic(driver, deviceName, deviceMac,
				prop.getProperty("ipCheck"));
		log.info("Device re-managed successfully");
		
	}
	
	@BeforeClass(alwaysRun = true)
	public void beforeClass() throws InterruptedException {
		
		if(StartupTestCase.isEmerald) {
			deviceName = rxEmerald.getDeviceName();
			deviceMac = rxEmerald.getMac();
		}else {
			deviceName = rxSingle.getDeviceName();
			rxSingle.getMac();
		}
		
	}

}
