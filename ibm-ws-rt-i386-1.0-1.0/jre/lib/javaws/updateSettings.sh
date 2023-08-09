#!/bin/sh

#
# @(#)src/scripts/updateSettings.sh, package, pxi32rt23, 20070106 1.4
# ===========================================================================
# Licensed Materials - Property of IBM
# "Restricted Materials of IBM"
#
# IBM SDK, Java(tm) 2 Technology Edition, v5.0
# (C) Copyright IBM Corp. 2005. All Rights Reserved
# ===========================================================================
#


if [ $# == 0 ]; then
   echo " "
   echo "    Usage: updateSettings.sh <JAVA_HOME>"
   echo "    JAVA_HOME specifies the absolute path of installed jre location." 
   echo "    Ex: updateSettings.sh /home/test/IBMJava2-VER/jre"
else
    if $1/bin/java -version > /dev/null 2>&1  
    then

#=================================================
# Add Java Web Start entry to $HOME/.mailcap
#-------------------------------------------------
    echo "Updating $HOME/.mailcap ..."
    MAILCAP_FILE="$HOME/.mailcap"
    MIME_TYPE=application/x-java-jnlp-file
    LATEST_JAVAWS_PATH=$1/bin
    echo_args="-e "
    MC_COMMENT="# Java Web Start"
    MC_TEXT=

    if [ -w ${MAILCAP_FILE} ]; then
# Remove existing entry, if present
        MC_TEXT=`grep -v "${MIME_TYPE}" ${MAILCAP_FILE} | \
                 grep -v "${MC_COMMENT}"`
    fi
# Add new entry
    if [ -w `dirname ${MAILCAP_FILE}` ]; then
        MC_TEXT="${MC_TEXT}\n${MC_COMMENT}"
        MC_TEXT="${MC_TEXT}\n${MIME_TYPE}; $LATEST_JAVAWS_PATH/javaws %s"
        echo ${echo_args}"${MC_TEXT}" > ${MAILCAP_FILE}
    else
        echo "WARNING - cannot write to file:"
        echo "       ${MAILCAP_FILE}"
        echo "Check permissions."
    fi

#=================================================
# Add Java Web Start entry to $HOME/.mime.types
#-------------------------------------------------
    echo "Updating $HOME/.mime.types ..."
    MIME_FILE="$HOME/.mime.types"

    NS_COMMENT1="#--Netscape Communications Corporation MIME Information"
    NS_COMMENT2="#Do not delete the above line. It is used to identify the file type."
    NS_COMMENT3="#mime types added by Netscape Helper"
    JNLP_ENTRY="type=${MIME_TYPE} desc=\"Java Web Start\" exts=\"jnlp\""

# Create the file if it does not exist
    if [ ! -w ${MIME_FILE} ]; then
        if [ -w `dirname ${MIME_FILE}` ]; then
            echo "${NS_COMMENT1}"  > ${MIME_FILE}
            echo "${NS_COMMENT2}" >> ${MIME_FILE}
            echo "${NS_COMMENT3}" >> ${MIME_FILE}
        else
            echo "WARNING - cannot write to file:"
            echo "       ${MIME_FILE}"
            echo "Check permissions."
            return
        fi
    fi
# Add the jnlp entry if it does not already exist.
    if [ -z "`grep ${MIME_TYPE} ${MIME_FILE}`" ]; then
        echo ${JNLP_ENTRY} >> ${MIME_FILE}
    fi
    
    else 
        echo "    Incorrect path specified."
	echo "    Please specify the absolute path of the installed jre location"	
        echo "    Ex: updateSettings.sh /home/test/IBMJava2-ver/jre"
    fi
fi
