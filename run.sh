#!/bin/ksh
sdcard=`ls /mnt|grep sdcard.*t`

# Full path
SDPath=/mnt/$sdcard

# Mount in read/write mode
mount -u {SDPATH}

# Show start screen
{SDPATH}/utils/showScreen {SDPATH}/screens/scriptStart.png

#put code commands here
#cp -r /java {SDPATH}/java
#cp -r /j9 {SDPATH}/j9

#{SDPATH}/j9/bin/j9 -help > {SDPATH}/logs/j9help.txt 2> {SDPATH}/logs/j9helperr.txt

#{SDPATH}/j9/bin/j9 -Dcom.ibm.oti.vm.bootstrap.library.path=.:/proc/boot:/lib:/lib/dll:/usr/lib:/usr/lib/dll:/j9/bin -Djava.library.path=.:/proc/boot:/lib:/lib/dll:/usr/lib:/usr/lib/dll:/j9/bin "-Xbootclasspath:{SDPATH}/j9/bin/j9vm/jclSC150/vm.jar" -cp "{SDPATH}/test" helloworld > {SDPATH}/logs/helloworldOUT.txt 2> {SDPATH}/logs/helloworldERR.txt
./script2.sh "$@" > {SDPATH}/logs/helloworldOUT.txt 2> {SDPATH}/logs/helloworldERR.txt


# Show end screen
{SDPATH}/utils/showScreen {SDPATH}/screens/scriptDone.png