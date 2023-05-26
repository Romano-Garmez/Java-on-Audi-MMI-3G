##
## Path setup:
##

## Java path setup:
export JAVA_HOME={SDPATH}/j9
export PATH=$JAVA_HOME/bin:$PATH

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
## Launch:
##

## add extra command line options to VM options
VMOPTIONS="$VMOPTIONS $@"

##
## Setup boot class path / conditionally include TestServer and DSITracer:
##
BOOTCLASSPATH=-Xbootclasspath

## actual launch of J9 with JAR/JXE specific settings:

	## start the jar variant:

	## compile class path for VM call
	#JARS=$(ls $BASEDIR/*.jar)
	JARS=$(ls {SDPATH}/j9/jars/*.jar)
	CLASSPATH={SDPATH}/test
	for jar in $JARS; do
		BOOTCLASSPATH="$BOOTCLASSPATH::$jar"
	done
	set -x
	{SDPATH}/j9/bin/j9 $VMOPTIONS -cp "$CLASSPATH" helloworld
	set +x
