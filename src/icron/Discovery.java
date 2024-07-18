package icron;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import extra.Ssh;
import extra.StartupTestCase;
import extra.StartupTestCase2;
import methods.PeripheralsMethods;
import objects.Peripherals;
import testNG.Utilities;

public class Discovery extends StartupTestCase {

	final static Logger log = Logger.getLogger(Discovery.class);
	PeripheralsMethods perMethods = new PeripheralsMethods();
	
	@Test
	public void test01_discoverLexDevicesByMac () {
		String text = perMethods.getDiscovertedDeviceDetails(driver, getLexMac(), Peripherals.DISCOVERY.MAC);
		Assert.assertTrue(text.equals(getLexMac()), "MAC addresses did not match. Expected:" + getLexMac() + " , actual:" + text);
		text = perMethods.getDiscovertedDeviceDetails(driver, getLexMac(), Peripherals.DISCOVERY.TYPE);
		Assert.assertTrue(text.equals("Local"), "Extender Type did not match. Expected: Local , actual:" + text);
		
		text = perMethods.getDiscovertedDeviceDetails(driver, getLexMac(), Peripherals.DISCOVERY.STATE);
		Assert.assertTrue(text.equals("Unbonded"), "Extender state did not match. Expected: Unbonded , actual:" + text);
	}
	@Test
	public void test02_discoverRex1ByMac () {
		
		//rex1
		String text = perMethods.getDiscovertedDeviceDetails(driver, getRex1Mac(), Peripherals.DISCOVERY.MAC);
		Assert.assertTrue(text.equals(getRex1Mac()), "MAC addresses did not match. Expected:" + getLexMac() + " , actual:" + text);
		
		text = perMethods.getDiscovertedDeviceDetails(driver, getRex1Mac(), Peripherals.DISCOVERY.TYPE);
		Assert.assertTrue(text.equals("Remote"), "Extender type did not match. Expected Remote , actual:" + text);
		
		text = perMethods.getDiscovertedDeviceDetails(driver, getRex1Mac(), Peripherals.DISCOVERY.STATE);
		Assert.assertTrue(text.equals("Unbonded"), "Extender state did not match. Expected: Unbonded , actual:" + text);		
	}
	
	@Test
	public void test03_discoverRex2ByMac () {
		
		//rex1
		String text = perMethods.getDiscovertedDeviceDetails(driver, getRex1Mac(), Peripherals.DISCOVERY.MAC);
		Assert.assertTrue(text.equals(getRex1Mac()), "MAC addresses did not match. Expected:" + getLexMac() + " , actual:" + text);
		
		text = perMethods.getDiscovertedDeviceDetails(driver, getRex1Mac(), Peripherals.DISCOVERY.TYPE);
		Assert.assertTrue(text.equals("Remote"), "Extender type did not match. Expected Remote , actual:" + text);
		
		text = perMethods.getDiscovertedDeviceDetails(driver, getRex1Mac(), Peripherals.DISCOVERY.STATE);
		Assert.assertTrue(text.equals("Unbonded"), "Extender state did not match. Expected: Unbonded , actual:" + text);		
	}
	
	@Test
	public void test04_changeLexIp() {
		String newIp = Ssh.getFreeIp();
		perMethods.editDeviceNetworkDiscover(driver, getLexMac(), newIp, "10.211.128.1", "255.255.248.0");
		String search = perMethods.getDiscovertedDeviceDetails(driver, getLexMac(), Peripherals.DISCOVERY.IP);
		Assert.assertTrue(search.equals(newIp), "Device IP was not changed. Expected " + newIp + " , actual:" + search);
		setLexIp(newIp);
	}
	@Test
	public void test05_changeRex1Ip() {
		String newIp = Ssh.getFreeIp();
		perMethods.editDeviceNetworkDiscover(driver, getRex1Mac(), newIp, "10.211.128.1", "255.255.248.0");
		String search = perMethods.getDiscovertedDeviceDetails(driver, getRex1Mac(), Peripherals.DISCOVERY.IP);
		Assert.assertTrue(search.equals(newIp), "Device IP was not changed. Expected " + newIp + " , actual:" + search);
		setRex1Ip(newIp);
	}
	@Test
	public void test06_changeRex2Ip() {
		String newIp = Ssh.getFreeIp();
		perMethods.editDeviceNetworkDiscover(driver, getRex2Mac(), newIp, "10.211.128.1", "255.255.248.0");
		String search = perMethods.getDiscovertedDeviceDetails(driver, getRex2Mac(), Peripherals.DISCOVERY.IP);
		Assert.assertTrue(search.equals(newIp), "Device IP was not changed. Expected " + newIp + " , actual:" + search);
		setRex2Ip(newIp);
	}
	
	@Test 
	public void test07_bondLexToTX() {
		perMethods.editBonding(driver, getLexMac(), "LEX1", singleTxName);
		boolean isBonded = perMethods.isBonded(driver, getLexMac());
		Assert.assertTrue(isBonded == true, "Device was not bonded");
	}
	@Test
	public void test08_bondRex1ToRX() {
		perMethods.editBonding(driver, getRex1Mac(), "REX1", singleRxName);
		boolean isBonded = perMethods.isBonded(driver, getRex1Mac());
		Assert.assertTrue(isBonded == true, "Device was not bonded");
	}

	@Test
	public void test09_bondRex2ToRX() {
		perMethods.editBonding(driver, getRex2Mac(), "REX2", dualRxName);
		boolean isBonded = perMethods.isBonded(driver, getRex2Mac());
		Assert.assertTrue(isBonded == true, "Device was not bonded");
	}
	
	@AfterClass
	public void afterClass() {
		
		try {
			cleanUpLogin();
			perMethods.settingsUnbondDevice(driver, getLexMac());
			String state = perMethods.getDiscovertedDeviceDetails(driver, getLexMac(), Peripherals.DISCOVERY.STATE);
			Assert.assertTrue(state.equals("Unbonded"), "Device state was bonded. Should have been unbonded");
			
			perMethods.settingsUnbondDevice(driver, getRex1Mac());
			 state = perMethods.getDiscovertedDeviceDetails(driver, getRex1Mac(), Peripherals.DISCOVERY.STATE);
			Assert.assertTrue(state.equals("Unbonded"), "Device state was bonded. Should have been unbonded");
			
			perMethods.settingsUnbondDevice(driver, getRex2Mac());
			 state = perMethods.getDiscovertedDeviceDetails(driver, getRex2Mac(), Peripherals.DISCOVERY.STATE);
			Assert.assertTrue(state.equals("Unbonded"), "Device state was bonded. Should have been unbonded");
			
			cleanUpLogout();
			super.afterClass();
			
		}catch(Exception e) {
			Utilities.captureScreenShot(driver, this.getClass().getName() + "_afterClass", "After Class");
			e.printStackTrace();
			cleanUpLogout();
		}
		
		
		
	}
	
}
