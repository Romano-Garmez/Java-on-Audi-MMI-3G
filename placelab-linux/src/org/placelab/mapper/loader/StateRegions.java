/*
 * Created on Aug 25, 2004
 *
 */
package org.placelab.mapper.loader;

import org.placelab.collections.HashMap;
import org.placelab.collections.Iterator;
import org.placelab.collections.Map;
import org.placelab.core.TwoDCoordinate;


public class StateRegions {
	private static Map one, two;
	
	public static TwoDCoordinate northEast(String state) {
		return (TwoDCoordinate)one.get(state.toUpperCase());
	}

	public static TwoDCoordinate southWest(String state) {
		return (TwoDCoordinate)two.get(state.toUpperCase());
	}
	
	public static Iterator states() {
		return one.keySet().iterator();
	}

	static {
		// load all this shit
		one = new HashMap(101);
		two = new HashMap(101);
		
		one.put("PR", new TwoDCoordinate(18.473477277978784, -67.99001896058125));
		two.put("PR", new TwoDCoordinate(17.974083263256333, -65.21940370090756));
		one.put("MO", new TwoDCoordinate(40.58204558629975, -95.79347183029796));
		two.put("MO", new TwoDCoordinate(36.07925307645152, -88.97408916578992));
		one.put("MN", new TwoDCoordinate(49.28245560933997, -97.3221268910904));
		two.put("MN", new TwoDCoordinate(43.54135031888043, -89.55239832387723));
		one.put("VT", new TwoDCoordinate(44.99277324193492, -73.49965873717413));
		two.put("VT", new TwoDCoordinate(42.82165730949089, -71.38047225181654));
		one.put("DE", new TwoDCoordinate(39.79200311483132, -75.88246038631719));
		two.put("DE", new TwoDCoordinate(38.46795121181343, -74.99910338812732));
		one.put("DC", new TwoDCoordinate(38.95459941458871, -77.13285168131266));
		two.put("DC", new TwoDCoordinate(38.82901733469532, -76.88499569648472));
		one.put("MI", new TwoDCoordinate(47.44283957321492, -90.45227278693777));
		two.put("MI", new TwoDCoordinate(41.77397155829696, -82.34642838354758));
		one.put("GA", new TwoDCoordinate(34.97410293466554, -85.70936972115283));
		two.put("GA", new TwoDCoordinate(30.667698451641385, -80.74464093537253));
		one.put("ME", new TwoDCoordinate(47.28726631107616, -71.35833505501513));
		two.put("ME", new TwoDCoordinate(43.122485373482846, -66.86121855697579));
		one.put("MD", new TwoDCoordinate(39.705991093674264, -79.69585419760308));
		two.put("MD", new TwoDCoordinate(37.972965425291406, -74.97458726872136));
		one.put("MA", new TwoDCoordinate(42.802605237326524, -73.5741352527551));
		two.put("MA", new TwoDCoordinate(41.3784939568814, -69.88997069238742));
		one.put("SD", new TwoDCoordinate(45.75359508182529, -104.50221157236643));
		two.put("SD", new TwoDCoordinate(42.588792937998036, -96.27440040376594));
		one.put("PA", new TwoDCoordinate(42.11918616518377, -80.63289335496557));
		two.put("PA", new TwoDCoordinate(39.76840852505367, -74.6257145046981));
		one.put("SC", new TwoDCoordinate(35.134898000154415, -83.39678270326993));
		two.put("SC", new TwoDCoordinate(32.2266018400479, -78.51603407256837));
		one.put("VA", new TwoDCoordinate(39.248197866333555, -83.64319034932562));
		two.put("VA", new TwoDCoordinate(36.602285371303815, -75.26852278563435));
		one.put("CT", new TwoDCoordinate(42.01267368465566, -73.77259692786177));
		two.put("CT", new TwoDCoordinate(41.05524393297338, -71.70781988460116));
		one.put("CO", new TwoDCoordinate(40.7346922108705, -109.52954932747171));
		two.put("CO", new TwoDCoordinate(37.13978289723921, -101.66584431020817));
		one.put("OR", new TwoDCoordinate(46.142559813865255, -124.69948343814183));
		two.put("OR", new TwoDCoordinate(42.10391993372021, -116.01507084869249));
		one.put("FL", new TwoDCoordinate(30.889530185970024, -87.79415938615931));
		two.put("FL", new TwoDCoordinate(24.64295579223978, -79.85523642507282));
		one.put("IN", new TwoDCoordinate(41.69030844318563, -88.19611456561664));
		two.put("IN", new TwoDCoordinate(37.98578460690722, -84.64213579944243));
		one.put("IL", new TwoDCoordinate(42.44571053996227, -91.6388241018638));
		two.put("IL", new TwoDCoordinate(37.09875508162319, -87.35362983567985));
		one.put("UT", new TwoDCoordinate(41.88792699963529, -114.9373917605326));
		two.put("UT", new TwoDCoordinate(37.15848721710246, -108.31586037030108));
		one.put("OK", new TwoDCoordinate(36.95975893926605, -103.17034816582117));
		two.put("OK", new TwoDCoordinate(33.77807459931043, -94.20348320503987));
		one.put("CA", new TwoDCoordinate(41.83622136999891, -124.68688333336284));
		two.put("CA", new TwoDCoordinate(32.5895989225512, -113.79585787450755));
		one.put("ID", new TwoDCoordinate(48.93141139176232, -118.10791611322088));
		two.put("ID", new TwoDCoordinate(42.07479484990479, -110.79275969448884));
		one.put("OH", new TwoDCoordinate(41.82255167960505, -84.94433167201541));
		two.put("OH", new TwoDCoordinate(38.52665966915527, -80.38749923949314));
		one.put("RI", new TwoDCoordinate(41.96087558480195, -71.91496413951154));
		two.put("RI", new TwoDCoordinate(41.217294271862066, -71.06754509243024));
		one.put("IA", new TwoDCoordinate(43.459678668719754, -96.71032611822724));
		two.put("IA", new TwoDCoordinate(40.51955814691241, -89.98881397991424));
		one.put("LA", new TwoDCoordinate(32.990327503171386, -94.18283354226715));
		two.put("LA", new TwoDCoordinate(29.2868007145486, -89.30917137972128));
		one.put("KY", new TwoDCoordinate(39.09304545468267, -89.44204588816882));
		two.put("KY", new TwoDCoordinate(36.6129467916432, -81.96951197116516));
		one.put("NY", new TwoDCoordinate(44.955124135010024, -79.8552524232766));
		two.put("NY", new TwoDCoordinate(40.52712456931222, -71.86074563183384));
		one.put("NV", new TwoDCoordinate(41.652511581130405, -121.05953284108385));
		two.put("NV", new TwoDCoordinate(35.0503863953742, -112.48952523255892));
		one.put("KS", new TwoDCoordinate(39.892061676543335, -102.35483333841623));
		two.put("KS", new TwoDCoordinate(37.00573039577943, -94.37264864448082));
		one.put("TX", new TwoDCoordinate(36.35360507212443, -106.68038504825391));
		two.put("TX", new TwoDCoordinate(26.02110166972169, -93.36569123189442));
		one.put("WY", new TwoDCoordinate(44.926932068293674, -111.68160346136939));
		two.put("WY", new TwoDCoordinate(41.09186170054093, -103.52644255605736));
		one.put("WV", new TwoDCoordinate(40.58718541766311, -82.72473790815768));
		two.put("WV", new TwoDCoordinate(37.29872591853284, -77.64140378186418));
		one.put("NM", new TwoDCoordinate(36.83287347295189, -109.58382724212836));
		two.put("NM", new TwoDCoordinate(31.822351551707666, -102.64187951552314));
		one.put("HI", new TwoDCoordinate(22.207472947460975, -159.92220993825856));
		two.put("HI", new TwoDCoordinate(19.25314583930958, -154.50344412961064));
		one.put("TN", new TwoDCoordinate(36.6219779486504, -90.2439046580916));
		two.put("TN", new TwoDCoordinate(35.02238500477793, -81.5575651431372));
		one.put("NJ", new TwoDCoordinate(41.197980308865674, -75.64893518110925));
		two.put("NJ", new TwoDCoordinate(38.94487543802059, -73.88293702197335));
		one.put("NH", new TwoDCoordinate(44.816684191519165, -72.59629081933727));
		two.put("NH", new TwoDCoordinate(42.77544408215832, -70.6717787496572));
		one.put("NE", new TwoDCoordinate(42.86917038452485, -104.5529759949014));
		two.put("NE", new TwoDCoordinate(40.05901175252179, -95.28342672950181));
		one.put("ND", new TwoDCoordinate(48.98992955610701, -104.51387572921738));
		two.put("ND", new TwoDCoordinate(46.10856245023034, -96.35551984973519));
		one.put("AZ", new TwoDCoordinate(36.91126955685338, -115.62083551189976));
		two.put("AZ", new TwoDCoordinate(31.5794133330434, -108.59503837230467));
		one.put("NC", new TwoDCoordinate(36.5102066656347, -84.49557794232892));
		two.put("NC", new TwoDCoordinate(33.95346217907232, -75.41898524615951));
		one.put("WI", new TwoDCoordinate(46.724753772952255, -93.02243104365444));
		two.put("WI", new TwoDCoordinate(42.5136221292585, -86.79108512586969));
		one.put("AR", new TwoDCoordinate(36.44759914168947, -94.74910152930202));
		two.put("AR", new TwoDCoordinate(33.183181127667076, -89.56340450143381));
		one.put("WA", new TwoDCoordinate(48.95949267017374, -124.78740737796181));
		two.put("WA", new TwoDCoordinate(45.6436985538289, -116.72250013665321));
		one.put("MT", new TwoDCoordinate(48.840461971635484, -116.54502837720172));
		two.put("MT", new TwoDCoordinate(45.020092582305885, -103.55215591984847));
		one.put("AL", new TwoDCoordinate(34.8971108355916, -88.66861545953702));
		two.put("AL", new TwoDCoordinate(30.28747006478748, -84.79277630230328));
		one.put("AK", new TwoDCoordinate(71.21150078795984, -176.6699995293933));
		two.put("AK", new TwoDCoordinate(51.90235599296104, -129.9717987618868));
		one.put("MS", new TwoDCoordinate(34.949951314759176, -91.8164448844795));
		two.put("MS", new TwoDCoordinate(30.29828025270353, -87.91656129224663));
	}
}
