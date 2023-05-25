package org.placelab.demo.mapview;

import org.placelab.test.Harness;

/**
 * 
 *
 */
public class DemoTests extends Harness {

	private static final String NO_GUI="nogui";
	
	public void setupAllTests(String[] argv) {
		boolean doGUITests=true;
		addTest(new RadianConversionTests());
		addTest(new SectionedFileParserTests());
		addTest(new WadDataTests());
		
		if (argv.length>1) {
			for (int i=0; i<argv.length;++i) {
				String arg=argv[i];
				if (arg==NO_GUI) {
					doGUITests=false;
				}
			}
		}
		if (doGUITests) {
			addTest(new MapViewTests());
		}
	}

}
