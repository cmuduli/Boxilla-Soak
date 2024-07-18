package northbound.get.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import methods.DevicesMethods;
import northbound.get.Appliances;

public class ApplianceConfig {

	private Map<String, String> deviceCodes = new HashMap<String, String> ();
	final static Logger log = Logger.getLogger(ApplianceConfig.class);
	
	
	public ApplianceConfig() {
		//set device details
		deviceCodes.put("300-067-001", "EMD2000PE-T");
		deviceCodes.put("300-069-001", "EMD2002PE-T");
		deviceCodes.put("300-015-001", "DTX1000-T");
		deviceCodes.put("300-013-001", "DTX1000-R");
		deviceCodes.put("300-070-001", "EMD200DV-T");
		deviceCodes.put("300-046-001", "EMD2000SE-R");
		deviceCodes.put("300-071-001", "EMD200EDV-T");
		deviceCodes.put("300-060-001", "EMD2002SE-R");
		deviceCodes.put("300-061-002", "EMD2002SE-T");
		deviceCodes.put("300-047-001", "EMD2000SE-T");
		deviceCodes.put("300-066-001", "EMD2000PE-R");
		deviceCodes.put("300-068-001", "EMD2002PE-R");
		deviceCodes.put("300-073-001", "EMD200DP-T-N");
	}
	
	public String getDeviceDeviceName(int position) {
		return "message.devices[" + position + "].name";
	}
	public String getDeviceIp(int position) {
		return "message.devices[" + position + "].ip";
	}
	public String getDeviceMac(int position) {
		return "message.devices[" + position + "].mac";
	}
	public String getDeivceSerialNumber(int position) {
		return "message.devices[" + position + "].serialno";
	}
	public String getDeviceSoftwareVersion(int position) {
		return "message.devices[" + position + "].swversion";
	}
	public String getDeivceModel(int position) {
		return "message.devices[" + position + "].model";
	}

	
	public String convertMpn(String mpn) {
		String convert = deviceCodes.get(mpn);
		log.info("MPN:" + convert);
		return convert;
	}
	
	public String getUri(String boxillaIp) {
		return "https://" + boxillaIp  + "/bxa-api/devices/kvm";
	}
	public class GetAppliance {
		public String device_type;
		public String[] device_names;
	}
	
	public String[] setApplianceDetails(String ip, String deviceUserName, String devicePassword) {
		DevicesMethods devices = new DevicesMethods();
		String[] details = new String[3]; 
		details[0] = devices.getSerialNumber(ip, deviceUserName, devicePassword);
		String mpn = "";
		details[1] = devices.getDeviceSwVersion(ip, deviceUserName, devicePassword);
		 mpn = devices.getMpn(ip, deviceUserName, devicePassword);
		details[2] = convertMpn(mpn);
		return details;
	}
}
