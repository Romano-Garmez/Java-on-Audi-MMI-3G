#!/bin/sh

if [ $# -eq 1 ] ; then
	JARNAME=$1
else
	JARNAME="placelab.jar"
fi

echo This builds the placelab jar. It assumes jar is on your path, 
echo and that you run it from the util directory

rm -rf tmp
mkdir tmp

cp -r ../bin/at tmp/
cp -r ../bin/org tmp/
cp -r ../bin/javax tmp/
cd tmp;  jar -xf ../../lib/jdbm-0.12.jar; cd ..
cd tmp;  jar -xf ../../lib/hsqldb.jar; cd ..
cd tmp;  jar -xf ../../lib/jface.jar; cd ..
cd tmp;  jar -xf ../../lib/openhash.jar; cd ..
cp ../native/win32/xp_spotter/Debug/Spotter.dll tmp/spotter.dll
cp ../native/linux/spotter/libspotter.so tmp/
cp ../native/wince/ndis_spotter/ARMV4Dbg/Spotter.dll tmp/spotter_ce.dll
cp ../native/mac/spotter/libspotter.jnilib tmp/
cd tmp ; jar -cf $JARNAME org jdbm at bamboo com diva ostore seda soss javax spotter.dll libspotter.so spotter_ce.dll libspotter.jnilib ; cd ..
mv tmp/$JARNAME .

rm -rf tmp
