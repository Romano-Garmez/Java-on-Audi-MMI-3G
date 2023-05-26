##
## Path setup:
##
sdcard=`ls /mnt|grep sdcard.*t`

# Full path
SDPath=/mnt/$sdcard



## Java path setup:
export JAVA_HOME=$SDPath/j9
export PATH=$JAVA_HOME/bin:$PATH

#ETCDIR=/lsd

## Elektrobit network setup:
#NETCONNECT=/mnt/hdisk/netconnect.sh

## Benchmark time logger:
#TIMELOGGER=/usr/apps/bench/TimeLogger

##
## JVM general setup:
##

## Setup library paths for Java generally, J9 and system:
export LD_LIBRARY_PATH=.:/proc/boot:/lib:/lib/dll:/usr/lib:/usr/lib/dll:$JAVA_HOME/bin:$BASEDIR
export EMP_PARAMS="xres=800,yres=480,disp=lvds,head=1,edid=/lsd/audi800x480_B2.edid"

VMOPTIONS="$VMOPTIONS -Djava.library.path=$LD_LIBRARY_PATH"
VMOPTIONS="$VMOPTIONS -Dcom.ibm.oti.vm.bootstrap.library.path=$LD_LIBRARY_PATH"

## JVM memory management parameters:
VMOPTIONS="$VMOPTIONS -Xmca8k -Xmco8k -Xmo16384k -Xmoi0 -Xmn512k -Xmx18432k"

##
## HMI framework setup:
##

## 
#VMOPTIONS="$VMOPTIONS -DbinSizes=1-33:33-60-15,1-600:2-4-2,1-64:64-30-5,1-600:17-4-2,1-600:18-4-2,1-90:120-32-2,1-600:50-20-1,3-60:60-7-7,3-100:40-3-1,3-74:245-2-2,3-450:45-10-1,3-450:300-2-2,3-512:512-9-1"

## uncomment to disable release label in status line
#VMOPTIONS="$VMOPTIONS -DenableVersionLabel"

## uncomment one or more from the following lines for custom LSD bundles
## and atip.properties / lastmode.properties configurations in the LSD directory:
#VMOPTIONS="$VMOPTIONS -Dlsd.bundles=$BASEDIR/bundles.properties"
#VMOPTIONS="$VMOPTIONS -Datip.properties=$BASEDIR/atip.properties"
#VMOPTIONS="$VMOPTIONS -Dlastmode.properties=$BASEDIR/lastmode.properties"
#VMOPTIONS="$VMOPTIONS -Dde.audi.tghu.traceConfig=$ETCDIR/traceConfig.properties"

## enable Profiling
#VMOPTIONS="$VMOPTIONS -DNoTraceClient=true -DProf -DKLOG=Ext.Benchmark=5,Fw.Domain=5"

## disable trace client connection
#
# depending on trace scope setting the trace client connection can have a severe performance impact
# for all profiling activities, the trace client connection should be disabled
#
#VMOPTIONS="$VMOPTIONS -DNoTraceClient=true"


## adapt and uncomment to change domain startup timeouts
VMOPTIONS="$VMOPTIONS -Dstartup.max.prepare.wait=10000"
VMOPTIONS="$VMOPTIONS -Dstartup.max.domain.wait=10000"

## TMC specific settings
VMOPTIONS="$VMOPTIONS -DNoTmcReadOut"
VMOPTIONS="$VMOPTIONS -DTmcWindowSize=15"

## media specific settings
## DVD menu timeout (millis) while driving (default: 7000 = 7sec)
## VMOPTIONS="$VMOPTIONS -Ddvd.menu.timeout=7000"
## disable velocity threshold (always show video content)
##VMOPTIONS="$VMOPTIONS -Dmedia.disable.velocity.threshold=true"
## enable device-/media-list debug messages
##VMOPTIONS="$VMOPTIONS -Dmedia.debug.mmsource=true"
## enable tracklist debug messages
##VMOPTIONS="$VMOPTIONS -Dmedia.debug.mmcontent=true"
## disable the content filter so that the Audio Folder is no longer shown
##VMOPTIONS="$VMOPTIONS -Dmedia.dev.mmfilter=off"
## enable transfer control debug messages
##VMOPTIONS="$VMOPTIONS -Dmedia.debug.transfer=true"
## enable the import "media scan" wait-screen
##VMOPTIONS="$VMOPTIONS -Dmedia.dev.importscanscreen=on"
## disable the "deletion running" screen
##VMOPTIONS="$VMOPTIONS -Dmedia.dev.deletiondisclaimer=off"
## use fixed HDD last-mode
##VMOPTIONS="$VMOPTIONS -Dmedia.lastmode=fixed"
## manually activate aux regardless of coding
#VMOPTIONS="$VMOPTIONS -Dmedia.config.aux=installed"
## manually activate usb regardless of coding
#VMOPTIONS="$VMOPTIONS -Dmedia.config.usb=installed"
## manually activate bluetooth regardless of coding
#VMOPTIONS="$VMOPTIONS -Dmedia.config.bt=notinstalled"
## configure used window radius for file-based content
##VMOPTIONS="$VMOPTIONS -Dmedia.debug.datacontent.windowradius=20"
## configure used window threshold for file-based content
##VMOPTIONS="$VMOPTIONS -Dmedia.debug.datacontent.windowthreshold=7"

## navigation specific settings
## enable sensor data replay handling
##VMOPTIONS="$VMOPTIONS -DETC_SENSOR_DATA_REPLAY=true"

##
## JDSI specific settings:
##

VMOPTIONS="$VMOPTIONS -Djdsi.3SoftOSGi=true"
VMOPTIONS="$VMOPTIONS -Ddsi.debuglevel=2"
VMOPTIONS="$VMOPTIONS -Djdsi.noDispatcher"
VMOPTIONS="$VMOPTIONS -Ddsi.channel.priority=+1"
VMOPTIONS="$VMOPTIONS -Ddsi.decoder.priority=+1"
VMOPTIONS="$VMOPTIONS -Ddsi.maxPacketLength=16384"

## setup dsi.channel property according to DSI_MODE enviroment variable:
if [ x$DSI_MODE == x ]; then
	## default channel when nothing is set
	VMOPTIONS="$VMOPTIONS -Ddsi.channel=msgpassing"
elif [ $DSI_MODE == m ]; then
	VMOPTIONS="$VMOPTIONS -Ddsi.channel=msgpassing"
elif [ $DSI_MODE == s ]; then
	VMOPTIONS="$VMOPTIONS -Ddsi.channel=socket"
fi


##
## Graphic adapter specific setup:
##

if [ -f $BASEDIR/libhybrid.so ]; then
	## Nvidia specific options go here:
	VMOPTIONS="$VMOPTIONS -Danimation.target=NVIDIA"
	VMOPTIONS="$VMOPTIONS -Dshowcombi=true"
elif [ -f $BASEDIR/liba2d.so ]; then
	## Coral specific options go there:
	VMOPTIONS="$VMOPTIONS"
fi

##
## Launch:
##


## add extra command line options to VM options
VMOPTIONS="$VMOPTIONS $@"

##
## Setup boot class path / conditionally include TestServer and DSITracer:
##
BOOTCLASSPATH=-Xbootclasspath
MODULAR=no

BOOTCLASSPATH="$BOOTCLASSPATH:$JXE"

## actual launch of J9 with JAR/JXE specific settings:

	## start the jar variant:

	## compile class path for VM call
	#JARS=$(ls $BASEDIR/*.jar)
	JARS=$(ls $SDPath/j9/bin/j9vm/jclSC150/*.jar)
	CLASSPATH=$SDPath/test
	for jar in $JARS; do
		CLASSPATH="$CLASSPATH:$jar"
	done
	set -x
	$SDPath/j9/bin/j9 $VMOPTIONS -cp "$CLASSPATH" helloworld
	set +x
