/*
*	(c) Copyright IBM Corp. 1998, 2003 All Rights Reserved
*/
/******************************************************\
		Portable socket library header.
\******************************************************/

#ifndef j9socket_h
#define j9socket_h

#include <stddef.h>

/* Socket types, stream & datagram */
#define J9SOCK_STREAM 0
#define J9SOCK_DGRAM 1
#define J9SOCK_AFINET 2
#define J9SOCK_DEFPROTOCOL 0
#define J9SOCK_INADDR_ANY (U_32)0	
#define J9SOCK_NOFLAGS (U_32)0			/* The default flag argument value, as in a recv */
#define J9SOCK_INADDR_LEN 4				/* The length of a binary internet address */

/* Portable defines for socket levels */
#define J9_SOL_SOCKET 1
#define J9_IPPROTO_TCP 2
#define J9_IPPROTO_IP 3

/* Portable defines for socket options */
#define J9_SO_LINGER 1
#define J9_SO_KEEPALIVE 2
#define J9_TCP_NODELAY 3
#define J9_MCAST_TTL 4
#define J9_MCAST_ADD_MEMBERSHIP 5
#define J9_MCAST_DROP_MEMBERSHIP 6
#define J9_MCAST_INTERFACE 7
#define J9_SO_REUSEADDR 8
#define J9_SO_REUSEPORT 9

#define J9_SO_SNDBUF 11
#define J9_SO_RCVBUF 12
#define J9_SO_BROADCAST 13 /*[PR1FLSKTU] Support datagram broadcasts */

/* Portable defines for socket read options */
#define J9SOCK_MSG_PEEK 1

/* Socket Errors */
#define J9SOCKERR_BADSOCKET -1						/* generic error */
#define J9SOCKERR_NOTINITIALIZED -2				/* socket library uninitialized */
#define J9SOCKERR_BADAF -3								/* bad address family */
#define J9SOCKERR_BADPROTO -4						/* bad protocol */
#define J9SOCKERR_BADTYPE -5							/* bad type */
#define J9SOCKERR_SYSTEMBUSY -6					/* system busy handling requests */
#define J9SOCKERR_SYSTEMFULL -7					/* too many sockets */
#define J9SOCKERR_NOTCONNECTED -8				/* socket is not connected */
#define J9SOCKERR_INTERRUPTED	-9					/* the call was cancelled */
#define J9SOCKERR_TIMEOUT	-10						/* the operation timed out */
#define J9SOCKERR_CONNRESET -11					/* the connection was reset */
#define J9SOCKERR_WOULDBLOCK	 -12				/* the socket is marked as nonblocking operation would block */
#define J9SOCKERR_ADDRNOTAVAIL -13				/* address not available */
#define J9SOCKERR_ADDRINUSE -14					/* address already in use */
#define J9SOCKERR_NOTBOUND -15					/* the socket is not bound */
#define J9SOCKERR_UNKNOWNSOCKET -16		/* resolution of fileDescriptor to socket failed */
#define J9SOCKERR_INVALIDTIMEOUT -17			/* the specified timeout is invalid */
#define J9SOCKERR_FDSETFULL -18					/* Unable to create an FDSET */
#define J9SOCKERR_TIMEVALFULL -19				/* Unable to create a TIMEVAL */
#define J9SOCKERR_REMSOCKSHUTDOWN -20	/* The remote socket has shutdown gracefully */
#define J9SOCKERR_NOTLISTENING -21				/* listen() was not invoked prior to accept() */
#define J9SOCKERR_NOTSTREAMSOCK -22			/* The socket does not support connection-oriented service */
#define J9SOCKERR_ALREADYBOUND -23			/* The socket is already bound to an address */
#define J9SOCKERR_NBWITHLINGER -24			/* The socket is marked non-blocking & SO_LINGER is non-zero */
#define J9SOCKERR_ISCONNECTED -25				/* The socket is already connected */
#define J9SOCKERR_NOBUFFERS -26					/* No buffer space is available */
#define J9SOCKERR_HOSTNOTFOUND -27			/* Authoritative Answer Host not found */
#define J9SOCKERR_NODATA -28							/* Valid name, no data record of requested type */
#define J9SOCKERR_BOUNDORCONN -29			/* The socket has not been bound or is already connected */
#define J9SOCKERR_OPNOTSUPP -30					/* The socket does not support the operation */
#define J9SOCKERR_OPTUNSUPP -31					/* The socket option is not supported */
#define J9SOCKERR_OPTARGSINVALID -32			/* The socket option arguments are invalid */
#define J9SOCKERR_SOCKLEVELINVALID -33		/* The socket level is invalid */
#define J9SOCKERR_TIMEOUTFAILURE -34			
#define J9SOCKERR_SOCKADDRALLOCFAIL -35	/* Unable to allocate the sockaddr structure */
#define J9SOCKERR_FDSET_SIZEBAD -36			/* The calculated maximum size of the file descriptor set is bad */
#define J9SOCKERR_UNKNOWNREADFLAG -37	/* The read flag is unknown */
#define J9SOCKERR_MSGSIZE -38						/* The datagram was too big to fit the specified buffer & was truncated. */
#define J9SOCKERR_NORECOVERY -39				/* The operation failed with no recovery possible */
#define J9SOCKERR_ARGSINVALID -40					/* The arguments are invalid */
#define J9SOCKERR_BADDESC -41						/* The socket argument is not a valid file descriptor */
#define J9SOCKERR_NOTSOCK -42						/* The socket argument is not a socket */
#define J9SOCKERR_HOSTENTALLOCFAIL -43	/* Unable to allocate the hostent structure */
#define J9SOCKERR_TIMEVALALLOCFAIL -44		/* Unable to allocate the timeval structure */
#define J9SOCKERR_LINGERALLOCFAIL -45			/* Unable to allocate the linger structure */
#define J9SOCKERR_IPMREQALLOCFAIL -46		/* Unable to allocate the ipmreq structure */
#define J9SOCKERR_FDSETALLOCFAIL -47			/* Unable to allocate the fdset structure */
#define J9SOCKERR_OPFAILED -48


/* Platform Constants */
typedef struct j9socket_struct *j9socket_t;
typedef struct j9sockaddr_struct *j9sockaddr_t;
typedef struct j9hostent_struct *j9hostent_t;
typedef struct j9fdset_struct *j9fdset_t;
typedef struct j9timeval_struct *j9timeval_t;
typedef struct j9linger_struct *j9linger_t;
typedef struct j9ipmreq_struct *j9ipmreq_t;

#endif     /* j9socket_h */

