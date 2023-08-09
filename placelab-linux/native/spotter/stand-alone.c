#include "../../common/spotter/spotter.h"
#include <stdio.h>



void
saw_ap(char *bssid, char *ssid, int rss, int ap, int wep)
{
	printf("%s\t%s\t%d\t%d\t%d\n", bssid, ssid, rss, ap, wep);
	/*printf("Saw BSSID: %s SSID: %s RSS: %d Mode: %s WEP: %s\n",
	  bssid, ssid, rss, (ap ? "Managed" : "Ad-hoc"), (wep ?"on":"off"));*/
}


int main()
{
	if (spotter_init() < 0) return 1;
	spotter_poll(saw_ap);
	spotter_shutdown();
	return 0;
}
