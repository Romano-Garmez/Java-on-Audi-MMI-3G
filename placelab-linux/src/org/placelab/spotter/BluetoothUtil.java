package org.placelab.spotter;

import java.util.Vector;

/**
 * Contains methods to turn bluetooth device codes into human
 * readable representations.
 * 
 */
public class BluetoothUtil {

	//Major Device Classes
	public static final int MISC_MAJOR_CLASS = 0x0000;
	public static final int COMPUTER_MAJOR_CLASS = 0x0100;
	public static final int PHONE_MAJOR_CLASS = 0x0200;
	public static final int NETWORK_MAJOR_CLASS = 0x0300;
	public static final int AUDIO_MAJOR_CLASS = 0x0400;
	public static final int PERIPHERAL_MAJOR_CLASS = 0x0500;
	public static final int IMAGING_MAJOR_CLASS = 0x0600;
	public static final int UNCATEGORIZED_MAJOR_CLASS = 0x1f00;

	//Minor Device Classes
	public static final int UNCATEGORIZED_MINOR_CLASS = 0x00;

	//computer
	public static final int DESKTOP_MINOR_CLASS = 0x04;
	public static final int SERVER_MINOR_CLASS = 0x08;
	public static final int LAPTOP_MINOR_CLASS = 0x0c;
	public static final int HANDHELD_MINOR_CLASS = 0x10;
	public static final int PALM_MINOR_CLASS = 0x14;
	public static final int WEARABLE_MINOR_CLASS = 0x18;
	
	//phone
	public static final int CELLULAR_MINOR_CLASS = 0x04;
	public static final int CORDLESS_MINOR_CLASS = 0x08;
	public static final int SMARTPHONE_MINOR_CLASS = 0x0c;
	public static final int MODEM_MINOR_CLASS = 0x10;
	public static final int ISDN_MINOR_CLASS = 0x14;

	//network
	public static final int ZERO_MINOR_CLASS = 0;
	public static final int FIRST_MINOR_CLASS = 0x20;
	public static final int SECOND_MINOR_CLASS = 0x40;
	public static final int THIRD_MINOR_CLASS = 0x60;
	public static final int FOURTH_MINOR_CLASS = 0x80;
	public static final int FIFTH_MINOR_CLASS = 0xa0;
	public static final int SIXTH_MINOR_CLASS = 0xc0;
	public static final int SEVENTH_MINOR_CLASS = 0xe0;

	//audio
	public static final int HEADSET_MINOR_CLASS = 0x04;
	public static final int HANDSFREE_MINOR_CLASS = 0x08;
	public static final int RESERVED1_MINOR_CLASS = 0x0c;
	public static final int MICROPHONE_MINOR_CLASS = 0x10;
	public static final int LOUDSPEAKER_MINOR_CLASS = 0x14;
	public static final int HEADPHONES_MINOR_CLASS = 0x18;
	public static final int PORTABLEAUDIO_MINOR_CLASS = 0x1c;
	public static final int CARAUDIO_MINOR_CLASS = 0x20;
	public static final int SETTOPBOX_MINOR_CLASS = 0x24;
	public static final int HIFIAUDIO_MINOR_CLASS = 0x28;
	public static final int VCR_MINOR_CLASS = 0x2c;
	public static final int VIDOECAMERA_MINOR_CLASS = 0x30;
	public static final int CAMCORDER_MINOR_CLASS = 0x34;
	public static final int VIDEOMONITOR_MINOR_CLASS = 0x38;
	public static final int VIDEODISPLAY_MINOR_CLASS = 0x3c;
	public static final int VIDEOCONFERENCE_MINOR_CLASS = 0x40;
	public static final int RESERVED2_MINOR_CLASS = 0x44;
	public static final int GAMING_MINOR_CLASS = 0x48;

	//peripheral devices
	//FIX ME
	public static final int JOYSTICK_MINOR_CLASS = 0x04;
	public static final int GAMEPAD_MINOR_CLASS = 0x08;
	public static final int REMOTE_MINOR_CLASS = 0x0c;
	public static final int SENSING_MINOR_CLASS = 0x10;
	public static final int DIGITIZER_MINOR_CLASS = 0x14;
	public static final int CARDREADER_MINOR_CLASS = 0x18;
	public static final int KEYBOARD_MINOR_CLASS = 0x40; 
	public static final int POINTING_MINOR_CLASS = 0x80;
	public static final int KEYBOARDPOINTING_MINOR_CLASS = 0xc0;
		 
	//Imaging
	public static final int DISPLAY_MINOR_CLASS = 0x10;
	public static final int CAMERA_MINOR_CLASS = 0x20;
	public static final int SCANNER_MINOR_CLASS = 0x40;
	public static final int PRINTER_MINOR_CLASS = 0x80;
		
	//Service Classes
	public static final int LIMITED_DISCOVERY_SERVICE = 0x2000;
	public static final int RESERVED1_SERVICE = 0x4000;
	public static final int RESERVED2_SERVICE = 0x8000;
	public static final int POSITIONING_SERVICE = 0x10000;
	public static final int NETWORKING_SERVICE = 0x20000;
	public static final int RENDERING_SERVICE = 0x40000;
	public static final int CAPTURING_SERVICE = 0x80000;
	public static final int OBJECT_TRANSFER_SERVICE = 0x100000;
	public static final int AUDIO_SERVICE = 0x200000;
	public static final int TELEPHONY_SERVICE = 0x400000;
	public static final int INFORMATION_SERVICE = 0x800000;

	public static String getMajorDeviceClass(int majorClass) {
		if (majorClass == MISC_MAJOR_CLASS)
			return "Miscellaneous";
		else if (majorClass == COMPUTER_MAJOR_CLASS)
			return "Computer";
		else if (majorClass == PHONE_MAJOR_CLASS)
			return "Phone";
		else if (majorClass == NETWORK_MAJOR_CLASS)
			return "LANAccessPoint";
		else if (majorClass == AUDIO_MAJOR_CLASS)
			return "AudioVideo";
		else if (majorClass == PERIPHERAL_MAJOR_CLASS)
			return "Peripheral";
		else if (majorClass == IMAGING_MAJOR_CLASS)
			return "Imaging";
		else if (majorClass == UNCATEGORIZED_MAJOR_CLASS)
			return "Uncategorized";
		else
			return "Unknown";
	}

	public static String getMinorDeviceClass(int majorClass, int minorClass) {
		switch (majorClass) {

		case MISC_MAJOR_CLASS:
			return "Miscellaneous";
		
		case COMPUTER_MAJOR_CLASS:
			if (minorClass == UNCATEGORIZED_MINOR_CLASS)
				return "Uncategorized";
			else if (minorClass == DESKTOP_MINOR_CLASS)
				return "Desktop";
			else if (minorClass == SERVER_MINOR_CLASS)
				return "Server";
			else if (minorClass == LAPTOP_MINOR_CLASS)
				return "Laptop";
			else if (minorClass == HANDHELD_MINOR_CLASS)
				return "Handheld";
			else if (minorClass == PALM_MINOR_CLASS)
				return "Palm";
			else if (minorClass == WEARABLE_MINOR_CLASS)
				return "Wearable";
			break;
		
		case PHONE_MAJOR_CLASS:
			if (minorClass == UNCATEGORIZED_MINOR_CLASS)
				return "Uncategorized";
			if (minorClass == CELLULAR_MINOR_CLASS)
				return "Cellular";
			else if (minorClass == CORDLESS_MINOR_CLASS)
				return "Cordless";
			else if (minorClass == SMARTPHONE_MINOR_CLASS)
				return "Smart Phone";
			else if (minorClass == MODEM_MINOR_CLASS)
				return "Modem";
			else if (minorClass == ISDN_MINOR_CLASS)
				return "ISDN";
			break;
		
		case NETWORK_MAJOR_CLASS:
			if (minorClass == ZERO_MINOR_CLASS)
				return "Fully available";
			else if (minorClass == FIRST_MINOR_CLASS)
				return "1-17% utilized";
			else if (minorClass == SECOND_MINOR_CLASS)
				return "17-33% utilized";
			else if (minorClass == THIRD_MINOR_CLASS)
				return "33-50% utilized";
			else if (minorClass == FOURTH_MINOR_CLASS)
				return "50-67% utilized";
			else if (minorClass == FIFTH_MINOR_CLASS)
				return "67-83% utilized";
			else if (minorClass == SIXTH_MINOR_CLASS)
				return "83-99% utilized";
			else if (minorClass == SEVENTH_MINOR_CLASS)
				return "No Service Available";
			break;
		
		case AUDIO_MAJOR_CLASS:
			if (minorClass == UNCATEGORIZED_MINOR_CLASS)
				return "Uncategorized";
			else if (minorClass == HEADSET_MINOR_CLASS)
				return "Wearable Headset";
			else if (minorClass == HANDSFREE_MINOR_CLASS)
				return "Hands-Free";
			else if (minorClass == RESERVED1_MINOR_CLASS)
				return "Reserved1";
			else if (minorClass == MICROPHONE_MINOR_CLASS)
				return "Microphone";
			else if (minorClass == LOUDSPEAKER_MINOR_CLASS)
				return "Loudspeaker";
			else if (minorClass == HEADPHONES_MINOR_CLASS)
				return "Headphones";
			else if (minorClass == PORTABLEAUDIO_MINOR_CLASS)
				return "Portable Audio";
			else if (minorClass == CARAUDIO_MINOR_CLASS)
				return "Car Audio";
			else if (minorClass == SETTOPBOX_MINOR_CLASS)
				return "Set-top Box";
			else if (minorClass == HIFIAUDIO_MINOR_CLASS)
				return "HiFi Audio";
			else if (minorClass == VCR_MINOR_CLASS)
				return "VCR";
			else if (minorClass == VIDOECAMERA_MINOR_CLASS)
				return "Video Camera";
			else if (minorClass == CAMCORDER_MINOR_CLASS)
				return "Camcorder";
			else if (minorClass == VIDEOMONITOR_MINOR_CLASS)
				return "Video Monitor";
			else if (minorClass == VIDEODISPLAY_MINOR_CLASS)
				return "Video Display and Loudspeaker";
			else if (minorClass == VIDEOCONFERENCE_MINOR_CLASS)
				return "Video Conferencing";
			else if (minorClass == RESERVED2_MINOR_CLASS)
				return "Reserved2";
			else if (minorClass == GAMING_MINOR_CLASS)
				return "Gaming/Toy";
			break;

		case PERIPHERAL_MAJOR_CLASS:
			if(minorClass == UNCATEGORIZED_MINOR_CLASS)
				return "Uncategorized";
			else if(minorClass == JOYSTICK_MINOR_CLASS)
				return "Joystick";
			else if(minorClass == GAMEPAD_MINOR_CLASS)
				return "Gamepad";
			else if(minorClass == REMOTE_MINOR_CLASS)
				return "Remote Control";
			else if(minorClass == SENSING_MINOR_CLASS)
				return "Sensing Device";
			else if(minorClass == DIGITIZER_MINOR_CLASS)
				return "Digitizer Tablet";
			else if(minorClass == CARDREADER_MINOR_CLASS)
				return "Card Reader";
			else if(minorClass == KEYBOARD_MINOR_CLASS)
				return "Keyboard";
			else if(minorClass == POINTING_MINOR_CLASS)
				return "Pointing";
			else if(minorClass == KEYBOARDPOINTING_MINOR_CLASS)
				 return "Combo Keyboard Pointing";			
			break;
		
		case IMAGING_MAJOR_CLASS:
			if(minorClass == DISPLAY_MINOR_CLASS)
				return "Display";
			else if(minorClass == CAMERA_MINOR_CLASS)
				return "Camera";
			else if(minorClass == SCANNER_MINOR_CLASS)
				return "Scanner";
			else if(minorClass == PRINTER_MINOR_CLASS)
				return "Printer";			
			break;
		
		case UNCATEGORIZED_MAJOR_CLASS:
			return "Uncategorized";
		default:
			break;
		}

		return "Unknown";
	}

	public static String[] getServiceClasses(int serviceClass) {
		Vector services = new Vector();

		if ((serviceClass & LIMITED_DISCOVERY_SERVICE) > 0)
			services.addElement("Limited Discoverable");
		if ((serviceClass & RESERVED1_SERVICE) > 0)
			services.addElement("Reserved1");
		if ((serviceClass & RESERVED2_SERVICE) > 0)
			services.addElement("Reserved2");
		if ((serviceClass & POSITIONING_SERVICE) > 0)
			services.addElement("Positioning");
		if ((serviceClass & NETWORKING_SERVICE) > 0)
			services.addElement("Networking");
		if ((serviceClass & RENDERING_SERVICE) > 0)
			services.addElement("Rendering");
		if ((serviceClass & CAPTURING_SERVICE) > 0)
			services.addElement("Capturing");
		if ((serviceClass & OBJECT_TRANSFER_SERVICE) > 0)
			services.addElement("ObjectTransfer");
		if ((serviceClass & AUDIO_SERVICE) > 0)
			services.addElement("Audio");
		if ((serviceClass & TELEPHONY_SERVICE) > 0)
			services.addElement("Telephony");
		if ((serviceClass & INFORMATION_SERVICE) > 0)
			services.addElement("Information");

		String[] result = new String[services.size()];
		for (int i = 0; i < services.size(); i++) {
			result[i] = (String) services.elementAt(i);
		}

		return result;
	}

}