#!/bin/ksh

# Show start screen
{SDPATH}/utils/showScreen {SDPATH}/screens/scriptStart.png

#put code commands here

./script2.sh "$@" > {SDPATH}/logs/helloworldOUT.txt 2> {SDPATH}/logs/helloworldERR.txt


# Show end screen
{SDPATH}/utils/showScreen {SDPATH}/screens/scriptDone.png