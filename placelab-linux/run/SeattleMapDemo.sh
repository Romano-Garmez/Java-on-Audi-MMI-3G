#!/bin/sh
BASE=`basename $0`
DIR=`echo $0 | sed -e s/$BASE$//g`
cd $DIR;  . placelab.env
../j9/bin/j9 $NLIBS -Dplacelab.ini="$PLACELAB_INI" org.placelab.demo.mapview.MapDemo --maps ../placelabdata/data/seattlemaps.zip --mapname "University District" --log seattle.log
