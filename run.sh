#!/bin/ksh
sdcard=`ls /mnt|grep sdcard.*t`

# Full path
SDPath=/mnt/$sdcard

# Mount in read/write mode
mount -u $SDPath

# Show start screen
$SDPath/utils/showScreen $SDPath/screens/scriptStart.png

#put code commands here
#cp -r /java $SDPath/java
#cp -r /j9 $SDPath/j9

#$SDPath/j9/bin/j9 -help > $SDPath/logs/j9help.txt 2> $SDPath/logs/j9helperr.txt

#$SDPath/j9/bin/j9 -Dcom.ibm.oti.vm.bootstrap.library.path=.:/proc/boot:/lib:/lib/dll:/usr/lib:/usr/lib/dll:/j9/bin -Djava.library.path=.:/proc/boot:/lib:/lib/dll:/usr/lib:/usr/lib/dll:/j9/bin "-Xbootclasspath:$SDPath/j9/bin/j9vm/jclSC150/vm.jar" -cp "$SDPath/test" helloworld > $SDPath/logs/helloworldOUT.txt 2> $SDPath/logs/helloworldERR.txt

$SDPath/j9/bin/j9 -Dcom.ibm.oti.vm.bootstrap.library.path=$SDPath/j9/bin -Djava.library.path=$SDPath/j9/bin -cp "$SDPath/j9/bin/j9vm/jclSC150/vm.jar:$SDPath/test" helloworld > $SDPath/logs/helloworldOUT.txt 2> $SDPath/logs/helloworldERR.txt

# Show end screen
$SDPath/utils/showScreen $SDPath/screens/scriptDone.png