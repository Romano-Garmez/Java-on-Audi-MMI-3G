@echo off
..\j9\bin\j9.exe -Djava.library.path="../native/" -Xbootclasspath:../j9/lib/classes.zip -Dplacelab.ini="../placelabdata/placelab.ini" -cp "../lib/placelab.jar;../jdbm-0.12.jar;../lib/mysql.jar;../lib/swt.jar;../lib/jface.jar;../lib/hsqldb.jar;../lib/database_enabler.jar;../lib/runtime.jar;../lib/RXTXcomm.jar" org.placelab.example.PlaceLabExample seattle.log
pause
