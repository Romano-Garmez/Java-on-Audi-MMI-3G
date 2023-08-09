#!/bin/sh
BASE=`basename $0`
DIR=`echo $0 | sed -e s/$BASE$//g`
cd $DIR
./RunAny.sh org.placelab.core.Version
