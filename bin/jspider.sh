#!/bin/sh
# $Id: jspider.sh,v 1.9 2003/04/22 16:43:33 vanrogu Exp $
echo ------------------------------------------------------------
echo JSpider startup script

if [ ! -d "$JSPIDER_HOME" ]; then
  echo JSPIDER_HOME does not exist as a valid directory : $JSPIDER_HOME
  echo Defaulting to current directory
  JSPIDER_HOME=..
fi

echo JSPIDER_HOME=$JSPIDER_HOME
echo ------------------------------------------------------------

SEQ=01
DATE=$(date +"D%y%m%d")
OUTPUT=/usr/local/jspider/output_${DATE}_${SEQ}.txt
while [ -f "$OUTPUT" ]
do
  SEQ=`printf %02d $((SEQ + 1))`
  OUTPUT=/usr/local/jspider/output_${DATE}_${SEQ}.txt
done

echo "using output=$OUTPUT"

export JSPIDER_OPTS=
export JSPIDER_OPTS="$JSPIDER_OPTS -Djspider.home=$JSPIDER_HOME"

JAVA_OPTS="-Xmx16G"
JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote"
JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.port=9998"
JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.ssl=false"
JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.authenticate=false"
JAVA_OPTS="$JAVA_OPTS -Djspider.run=${DATE}_${SEQ}"

DEBUG_PORT="4143"
DEBUG="-agentlib:jdwp=transport=dt_socket,address=$DEBUG_PORT,server=y,suspend=n"

DATE=$(date +"D%y%m%d-T%H%M%S")
JFRFILE=$JSPIDER_HOME/output/jspider_${DATE}.jfr
#JFR="-XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:+FlightRecorderOptions=\"filename=$JFRFILE,defaultrecording=true,delay=30s,duration=10m\""
JFR="-XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:StartFlightRecording=filename=$JFRFILE,defaultrecording=true,delay=30s,duration=5m"

nohup java $JAVA_OPTS $JSPIDER_OPTS $DEBUG $JFR -jar $JSPIDER_HOME/lib/jspider-crawler-*-exe.jar $1 $2 > $OUTPUT 2>&1 &
#java $JAVA_OPTS $JSPIDER_OPTS $DEBUG $JFR -jar $JSPIDER_HOME/lib/jspider-crawler-*-exe.jar $1 $2

