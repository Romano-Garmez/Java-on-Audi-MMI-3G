#include "../../common/spotter/spotter.h"
#include <stdio.h>



int found_ap = 0;

void
saw_ap(char *bssid, char *ssid, int rss, int ap, int wep)
{
    found_ap = 1;
}


void error_msg()
{
    printf
	("Your device cannot support WiFi spotting.  This may be due to one\n"
	 "of the following reasons:\n"
	 "  1. Your machine does not have a WiFi interface configured\n"
	 "  2. Your machine does have a WiFi interface but,\n"
	 "     (a) Your kernel has an old (< v14) version of Wireless\n"
	 "         Extensions.  (You can fix this by upgrading the Wireless\n"
	 "         Extensions package in your kernel to the latest version),\n"
	 "         or\n"
	 "     (b) Your WiFi card or driver cannot support the SCAN command\n"
	 "\n"
	 "You can still use Place Lab in its log-file mode, but you won't\n"
	 "be able to run any of the sample programs that rely on live\n"
	 "WiFi spotting.\n"
	 );
}


int main()
{
    if (spotter_init() < 0) {
	error_msg();
	return 1;
    }
    if (spotter_poll(saw_ap) < 0) {
	error_msg();
	return 1;
    }
    if (!found_ap) {
	printf("I could not detect any WiFi beacons in your environment.  Either you\nare in an area with no WiFi coverage, or your device is not capable of\nscanning for WiFi beacons.  In either case, you will not be able to\nuse Place Lab to do \"live\" spotting.  You can still use Place Lab in its\nlog-file mode.\n");
    } else {
		printf("It looks like your device can run Place Lab\n");
	}
	spotter_shutdown();
	return 0;
}
