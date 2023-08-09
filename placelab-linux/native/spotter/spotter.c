#include <stdio.h>
#include "iwlib.h"
#include "../../common/spotter/spotter.h"



struct beacon {
    int has_data;
    
    struct ether_addr bssid;
    struct iw_param nwid;
    struct iw_freq freq;
    int  mode;
    char essid[IW_ESSID_MAX_SIZE+1];    
    int  essid_flags;

    char wep_key[IW_ENCODING_TOKEN_MAX];
    int  wep_key_len;
    int  wep_flags;

    int bitrate;
    struct iw_quality quality;

    struct iw_range range;
    int has_range;
};

/* XXX */
char *
iw_get_ifname(char *	name,	/* Where to store the name */
	      int	nsize,	/* Size of name buffer */
	      char *	buf);	/* Current position in buffer */

/* Prototype for handling display of each single interface on the
 * system - see spotter_enum_devices() */
typedef int (*spotter_enum_handler)(SawAPFunction fn,
				    int	          skfd,
				    char *	  ifname,
				    char *	  args[],
				    int	          count);

int socket_fd_=-1;
char *device_=NULL;





static void
beacon_init(struct beacon *b)
{
    memset(b, 0, sizeof(struct beacon));
    b->has_data = 0;
}


static void
beacon_cleanup(struct beacon *b)
{
    b->has_data = 0;
}


int
spotter_init()
{
    if ((socket_fd_ = iw_sockets_open()) < 0) {
	perror("socket");
	return -1;
    }

    //device_ = strdup(argv[0]);

    device_ = NULL;
    return 0;
}


void
spotter_shutdown()
{
    if (socket_fd_ >= 0) close(socket_fd_);
    socket_fd_ = -1;

    if (device_) free(device_);
    device_ = NULL;
}



static
void spotter_print(SawAPFunction ap_fn, struct beacon *beacon)
{
    char bssid[32];
    sprintf(bssid, "%02x:%02x:%02x:%02x:%02x:%02x",
	    beacon->bssid.ether_addr_octet[0],
	    beacon->bssid.ether_addr_octet[1],
	    beacon->bssid.ether_addr_octet[2],
	    beacon->bssid.ether_addr_octet[3],
	    beacon->bssid.ether_addr_octet[4],
	    beacon->bssid.ether_addr_octet[5]);
    (*ap_fn)(bssid, beacon->essid_flags ? beacon->essid : "any",
	     (beacon->has_range && beacon->quality.level != 0) ?
	     (beacon->quality.level > beacon->range.max_qual.level ?
	      beacon->quality.level - 0x100 :
	      beacon->quality.level) : beacon->quality.level,
	     /*
	      * beacon->mode should probably not be a boolean because a card actually
	      * supports more than ad-hoc or AP mode (refer wireless.h in linux kernel)
	      * but does not really matter for placelab spotter
	      */
	     beacon->mode == IW_MODE_MASTER ? 1 : 0,
	     beacon->wep_flags & IW_ENCODE_DISABLED ? 0 : 1);
}


/*
 * Print one element from the scanning results
 */
static inline int
print_scanning_token(struct iw_event *	event,	/* Extracted token */
		     int		ap_num,	/* AP number */
		     struct iw_range *	iwrange,	/* Range info */
		     int		has_range,
		     struct beacon   *  beacon)
{
    /* Now, let's decode the event */
    switch(event->cmd) {
    case SIOCGIWAP:
	memcpy(&beacon->bssid, (struct ether_addr*)event->u.ap_addr.sa_data,
	       sizeof(struct ether_addr));
	ap_num++;
	break;
    case SIOCGIWNWID:
	beacon->nwid = event->u.nwid;
	break;
    case SIOCGIWFREQ:
	beacon->freq = event->u.freq;
	break;
    case SIOCGIWMODE:
	beacon->mode = event->u.mode;
	break;
    case SIOCGIWESSID:
	{
	    if((event->u.essid.pointer) && (event->u.essid.length))
		memcpy(beacon->essid, event->u.essid.pointer,
		       event->u.essid.length);
	    beacon->essid[event->u.essid.length] = '\0';
	    beacon->essid_flags = event->u.essid.flags;
	}
	break;
    case SIOCGIWENCODE:
	{
	    if(event->u.data.pointer)
		memcpy(beacon->wep_key, event->u.data.pointer,
		       event->u.data.length);
	    else
		event->u.data.flags |= IW_ENCODE_NOKEY;
	    beacon->wep_key_len = event->u.data.length;
	    beacon->wep_flags   = event->u.data.flags;
	}
	break;
    case SIOCGIWRATE:
	beacon->bitrate = event->u.bitrate.value;
	break;
    case IWEVQUAL:
	beacon->quality   = event->u.qual;
	beacon->range     = *iwrange;
	beacon->has_range = has_range;
	break;
#if 0 /* WIRELESS_EXT > 14 */
    case IWEVCUSTOM:
	{
	    char custom[IW_CUSTOM_MAX+1];
	    if((event->u.data.pointer) && (event->u.data.length))
		memcpy(custom, event->u.data.pointer, event->u.data.length);
	    custom[event->u.data.length] = '\0';
	    printf("                    Extra:%s\n", custom);
	}
	break;
#endif /* WIRELESS_EXT > 14 */
    default:
	break;
    }	/* switch(event->cmd) */
    
    /* May have changed */
    return(ap_num);
}



static int
print_scanning_info(SawAPFunction ap_fn,
		    int           skfd,
		    char *        ifname,
                    char *        args[],         /* Command line args */
                    int           count)          /* Args count */
{
    struct iwreq          wrq;
    unsigned char         buffer[IW_SCAN_MAX_DATA];       /* Results */
    struct timeval        tv;                             /* Select timeout */
    int                   timeout = 5000000;              /* 5s */
    
    /* Avoid "Unused parameter" warning */
    args = args; count = count;
 
    /* Init timeout value -> 250ms*/
    tv.tv_sec = 0;
    tv.tv_usec = 250000;
 
    /*
     * Here we should look at the command line args and set the IW_SCAN_ flags
     * properly
     */
    wrq.u.param.flags = IW_SCAN_DEFAULT;
    wrq.u.param.value = 0;                /* Later */

    /* Initiate Scanning */
    if(iw_set_ext(skfd, ifname, SIOCSIWSCAN, &wrq) < 0) {
	if(errno != EPERM) {
	    /*fprintf(stderr,"%-8.8s Interface doesn't support scanning: %s\n\n",
	      ifname, strerror(errno));*/
	    
	    return(-1);
	}
	/* If we don't have the permission to initiate the scan, we may
	 * still have permission to read left-over results.
	 * But, don't wait !!! */
#if 0
	/* Not cool, it display for non wireless interfaces... */
	fprintf(stderr, "%-8.8s  (Could not trigger scanning, just reading left-over results)\n", ifname);
#endif
	tv.tv_usec = 0;
    }
    timeout -= tv.tv_usec;

    /* Forever */
    while(1) {
	fd_set		rfds;		/* File descriptors for select */
	int		last_fd;	/* Last fd */
	int		ret;
	
	/* Guess what? We must re-generate rfds each time */
	FD_ZERO(&rfds);
	last_fd = -1;

	/* In here, add the rtnetlink fd in the list */
	
	/* Wait until something happens */
	ret = select(last_fd + 1, &rfds, NULL, NULL, &tv);

	/* Check if there was an error */
	if(ret < 0) {
	    if(errno == EAGAIN || errno == EINTR)
		continue;
	    /* fprintf(stderr, "Unhandled signal - exiting...\n"); */
	    return(-1);
	}

	/* Check if there was a timeout */
	if(ret == 0) {
	    /* Try to read the results */
	    wrq.u.data.pointer = buffer;
	    wrq.u.data.flags = 0;
	    wrq.u.data.length = sizeof(buffer);
	    if(iw_get_ext(skfd, ifname, SIOCGIWSCAN, &wrq) < 0) {
		/* Check if results not available yet */
		if(errno == EAGAIN) {
		    /* Restart timer for only 100ms*/
		    tv.tv_sec = 0;
		    tv.tv_usec = 100000;
		    timeout -= tv.tv_usec;
		    if(timeout > 0)
			continue;	/* Try again later */
		}

		/* Bad error */
		/*fprintf(stderr, "%-8.8s  Failed to read scan data : %s\n\n",
		  ifname, strerror(errno));*/
		return(-2);
	    }
	    else
		/* We have the results, go to process them */
		break;
	}

	/* In here, check if event and event type
	 * if scan event, read results. All errors bad & no reset timeout */
    }

    if(wrq.u.data.length) {
	struct iw_event		iwe;
	struct stream_descr	stream;
	int			ap_num = 1;
	int			ret;
	struct iw_range		range;
	int			has_range;
	struct beacon           beacon;
	
#if 0
	/* Debugging code. In theory useless, because it's debugged ;-) */
	int	i;
	printf("Scan result:\n");
	for(i = 0; i < wrq.u.data.length; i++)
	    printf("%02X  ", buffer[i]);
	printf("\n");
#endif
	has_range = (iw_get_range_info(skfd, ifname, &range) >= 0);
	iw_init_event_stream(&stream, buffer, wrq.u.data.length);

	beacon_init(&beacon);
	do {
	    /* Extract an event and print it */
	    ret = iw_extract_event_stream(&stream, &iwe);
	    if(ret > 0) {
		if (iwe.cmd == SIOCGIWAP) {
		    /* this is a new record; let's output the old one */
		    if (beacon.has_data) spotter_print(ap_fn, &beacon);
		    beacon_cleanup(&beacon);
		    beacon_init(&beacon);
		}
		ap_num = print_scanning_token(&iwe, ap_num, &range, has_range,
					      &beacon);
		beacon.has_data = 1;
	    }
	} while(ret > 0);

	if (beacon.has_data) spotter_print(ap_fn, &beacon);
	beacon_cleanup(&beacon);
	beacon_init(&beacon);
    }

    return(0);
}




/*
 * Based on iw_enum_devices()
 * Enumerate devices and call specified routine
 * The new way just use /proc/net/wireless, so get all wireless interfaces,
 * whether configured or not. This is the default if available.
 * The old way use SIOCGIFCONF, so get only configured interfaces (wireless
 * or not).
 *
 * RETURN: 0 if at least one of the calls returned 0
 */
static int
spotter_enum_devices(spotter_enum_handler fn,
		     SawAPFunction        ap_fn,
		     int                  skfd,
		     char *               args[],
		     int                  count)
{
    char   buff[1024];
    FILE * fh;
    struct ifconf ifc;
    struct ifreq *ifr;
    int    i, retval=-1;
    
#ifndef IW_RESTRIC_ENUM
    /* Check if /proc/net/wireless is available */
    fh = fopen(PROC_NET_DEV, "r");
#else
    /* Check if /proc/net/wireless is available */
    fh = fopen(PROC_NET_WIRELESS, "r");
#endif

    if(fh != NULL) {
	/* Success : use data from /proc/net/wireless */
	
	/* Eat 2 lines of header */
	fgets(buff, sizeof(buff), fh);
	fgets(buff, sizeof(buff), fh);
	
	/* Read each device line */
	while(fgets(buff, sizeof(buff), fh)) {
	    char	name[IFNAMSIZ + 1];
	    char *s;
	    
	    /* Extract interface name */
	    s = iw_get_ifname(name, sizeof(name), buff);
	    
	    if(s) {
		int ret;
		/* Got it, print info about this interface */
		ret = (*fn)(ap_fn, skfd, name, args, count);
		if (ret >= 0) retval = 0;
	    }
	}
	
	fclose(fh);
    }
    else {
	/* Get list of configured devices using "traditional" way */
	ifc.ifc_len = sizeof(buff);
	ifc.ifc_buf = buff;
	if(ioctl(skfd, SIOCGIFCONF, &ifc) < 0) {
	    return -1;
	}
	ifr = ifc.ifc_req;
	
	/* Print them */
	for(i = ifc.ifc_len / sizeof(struct ifreq); --i >= 0; ifr++) {
	    int ret;
	    ret = (*fn)(ap_fn, skfd, ifr->ifr_name, args, count);
	    if (ret >= 0) retval = 0;
	}
    }
    return retval;
}


int
spotter_poll(SawAPFunction ap_fn)
{
    if (device_)
	return print_scanning_info(ap_fn, socket_fd_, device_, NULL, 0);
    else
	return spotter_enum_devices(print_scanning_info, ap_fn, socket_fd_,
				    NULL, 0);
}



#if 0
int main()
{
    int i;
    spotter_init(0, NULL);
    for (i=0; i < 5; i++) {
	printf("ROUND %d ----------------------------------------\n", i);
	spotter_poll();
    }
    spotter_shutdown();
    return 0;
}
#endif
