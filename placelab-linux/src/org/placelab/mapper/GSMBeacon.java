/*
 * Created on 13-Aug-2004
 *
 */
package org.placelab.mapper;

import org.placelab.collections.HashMap;
import org.placelab.core.Coordinate;
import org.placelab.core.Types;
import org.placelab.util.StringUtil;

/**
 * 
 *
 */
public class GSMBeacon extends Beacon {
	private String cellId;
	private String areaId;
	private String MCC;
	private String MNC;
	private String networkName;
	private Coordinate position;
	private HashMap keys;

	public GSMBeacon() {
		keys = new HashMap();
		keys.put(Types.TYPE,Types.GSM);
	}

	public void fromHashMap(HashMap map) {
		String id = (String) map.get(Types.ID);
		if(id == null) return;
		String[] values = StringUtil.split(id,':');
		if(values.length != 4) return;
		MCC = values[0];
		MNC = values[1];
		cellId = values[2];
		areaId = values[3];

		String humanName = (String) map.get(Types.HUMANREADABLENAME);
		if(humanName != null) {
		    String vals[] = StringUtil.split(humanName,':');		
		    if(vals != null && vals.length > 0) {
		        networkName = vals[0];
		    }
		}
		position = Types.newCoordinate((String) map.get(Types.LATITUDE), (String) map.get(Types.LONGITUDE));
		keys = map;
	}

	public HashMap toHashMap() {
		return keys;
	}

	public String getId() {
		return MCC+":"+MNC+":"+cellId+":"+areaId;
	}

	public String getType() {
		return Types.GSM;
	}

	public Coordinate getPosition() {
		return position;
	}

	/**
	 * @return Returns the areaId.
	 */
	public String getAreaId() {
		return areaId;
	}
	/**
	 * @param areaId The areaId to set.
	 */
	public void setAreaId(String areaId) {
		keys.put(Types.AREAID,areaId);
		this.areaId = areaId;
	}
	/**
	 * @return Returns the cellId.
	 */
	public String getCellId() {
		return cellId;
	}
	/**
	 * @param cellId The cellId to set.
	 */
	public void setCellId(String cellId) {
		keys.put(Types.CELLID,cellId);
		this.cellId = cellId;
	}
	/**
	 * @return Returns the mCC.
	 */
	public String getMCC() {
		return MCC;
	}
	/**
	 * @param mcc The mCC to set.
	 */
	public void setMCC(String mcc) {
		keys.put(Types.MCC,cellId);
		MCC = mcc;
	}
	/**
	 * @return Returns the mNC.
	 */
	public String getMNC() {
		return MNC;
	}
	/**
	 * @param mnc The mNC to set.
	 */
	public void setMNC(String mnc) {
		keys.put(Types.MNC,cellId);
		MNC = mnc;
	}
	/**
	 * @return Returns the networkName.
	 */
	public String getNetworkName() {
		return networkName;
	}
	/**
	 * @param networkName The networkName to set.
	 */
	public void setNetworkName(String networkName) {
		keys.put(Types.NETWORK_NAME,cellId);
		this.networkName = networkName;
	}
	/**
	 * @param position The position to set.
	 */
	public void setPosition(Coordinate pos) {
		keys.put(Types.LATITUDE,pos.getLatitudeAsString());
		keys.put(Types.LONGITUDE,pos.getLongitudeAsString());
		this.position = pos;
	}
	
	public HashMap getKeyValues() {
		return keys;
	}
	
	public int getMaximumRange() {
	    return 5000; 
	}
}
