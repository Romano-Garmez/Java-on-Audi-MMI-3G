

CROSS=0
ifeq ($(CROSS),1)
	GCC=/usr/local/arm/3.3.2/bin/arm-linux-gcc
	CFLAGS=-mcpu=xscale -mtune=xscale
	LIBS=
	GCCDIR=/usr/local/arm/3.3.2
else
	GCC=gcc
	CFLAGS=
endif


JAVA=/usr/java/j2sdk1.4.2_03
INCLUDE=-I $(JAVA)/include -I $(JAVA)/include/linux

%.o:  %.c
	rm -f $@; $(GCC) -c -g -Wall $(INCLUDE) -o $@ $<


OBJ = \
	spotter.o \
	jni.o \
	iwlib.o

STANDALONE_OBJ = \
	spotter.o \
	stand-alone.o \
	iwlib.o

SPOTTER_TEST_OBJ = \
	spotter.o \
	spotter-test.o \
	iwlib.o

all: libspotter.so

jni.o: ../../common/spotter/jni.c
	rm -f $@; $(GCC) -c -g -Wall $(INCLUDE) -o $@ $<

libspotter.so: $(OBJ)
	$(GCC) -shared -fPIC -o libspotter.so $(OBJ) -lm

spotter: $(STANDALONE_OBJ)
	$(GCC) -o spotter -static $(STANDALONE_OBJ) -lm

will-spotter-work: $(SPOTTER_TEST_OBJ)
	$(GCC) -o $@ -static $(SPOTTER_TEST_OBJ) -lm

clean:
	rm -f $(OBJ) stand-alone.o libspotter.so spotter spotter-test
