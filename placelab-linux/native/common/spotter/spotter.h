#ifndef __COMMON_SPOTTER_H__
#define __COMMON_SPOTTER_H__


typedef void (*SawAPFunction)(char *bssid, char *ssid, int rss,int wep,int infrMode);

/* returns 0 on success, -1 on failure */
int  spotter_init();

void spotter_shutdown();

/* returns 0 on success, -1 on failure */
int  spotter_poll(SawAPFunction fn);


#endif /* __COMMON_SPOTTER_H__ */
