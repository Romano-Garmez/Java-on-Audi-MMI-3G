# Doom-on-Audi-MMI-3G
 
# Note: I have now sold my Audi with this infotainment system, so I will not be doing any more work on this project. In case this is useful to anyone else, I'm publishing it anyway.

Not yet functional, still just experimenting.

```
To Do:
- Get built-in JVM to run a helloWorld class
- Get JVM to boot MochaDoom
- Figure out user input/controls
- Any other issues that show up along the way
```

# J9
J9 is the Java virtual machine already installed on every Audi MMI 3G system. Using scripts, you can dump most of J9's libraries to an SD card, but not the J9 executable itself. 
You can, however, find J9 when dumping the ifs-root image: https://www.audizine.com/forum/showthread.php/951168-Extracting-ifs-root-ifs-Image-File 
I've been trying to piece together all of J9 from the bits I can pull from my car, the bits documented online, and other versions of J9 for other platforms.
There's a version of J9 included with some placelab downloads, and IBM has distributed it in some software, so that is included in this repo because it's otherwise almost impossible to track down.
I have been unsuccessful in getting J9 to run even a helloWorld class, as piecing together the lib folder from random other installations results in some incorrect or missing files, but I've gotten fairly close. 

To test this in your car, place the contents of this GitHub repo onto a FAT32 SD card. Start your car or at least the infotainment. WAIT FOR IT TO BOOT FULLY, check that every page is accessible (Nav, phone, media, etc). Then, insert the SD card and it will prompt to run the script. 
As it is right now, all it will do is print a couple of errors to the logs folder, but maybe that information is useful to you.
