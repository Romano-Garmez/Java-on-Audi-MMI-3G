#!/bin/bash

if [ ! -d '../docs' ] ; then
    mkdir ../docs
fi

jdoc="javadoc"

if [ $1 ] ; then
    jdoc=$1
fi

$jdoc -quiet -sourcepath ../src -d ../docs -classpath "../lib/jdbm-0.12.jar:../lib/RXTXcomm.jar:../lib/bluetooth/BlueCove.jar:../lib/mysql.jar:../lib/swt/win32/swt.jar:../midpbuild/lib/series60.zip:../lib/jface.jar:../lib/runtime.jar:../lib/openhash.jar" -subpackages org.placelab
