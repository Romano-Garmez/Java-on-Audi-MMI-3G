#!/bin/ksh
sdcard=`ls /mnt|grep sdcard.*t`
echo Using card $sdcard

dstPath=/mnt/$sdcard

mount -u $dstPath
echo remounted for full access

cd $dstPath
./run.sh
