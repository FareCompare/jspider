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

export JSPIDER_OPTS=
export JSPIDER_OPTS="$JSPIDER_OPTS -Djspider.home=$JSPIDER_HOME"
export JSPIDER_OPTS="$JSPIDER_OPTS -Djava.util.logging.config.file=$JSPIDER_HOME/common/conf/logging/logging.properties"
export JSPIDER_OPTS="$JSPIDER_OPTS -Dlog4j.configuration=conf/logging/log4j.xml"

export JSPIDER_CLASSPATH=
export JSPIDER_CLASSPATH="$JSPIDER_HOME/lib/jspider.jar"
export JSPIDER_CLASSPATH="$JSPIDER_CLASSPATH:$JSPIDER_HOME/lib/velocity-dep-1.3.1.jar"
export JSPIDER_CLASSPATH="$JSPIDER_CLASSPATH:$JSPIDER_HOME/lib/commons-lang-2.6.jar"
export JSPIDER_CLASSPATH="$JSPIDER_CLASSPATH:$JSPIDER_HOME/lib/commons-logging.jar"
export JSPIDER_CLASSPATH="$JSPIDER_CLASSPATH:$JSPIDER_HOME/lib/log4j-1.2.8.jar"
export JSPIDER_CLASSPATH="$JSPIDER_CLASSPATH:$JSPIDER_HOME/lib/mysql-connector-java-5.1.28.jar"
export JSPIDER_CLASSPATH="$JSPIDER_CLASSPATH:$JSPIDER_HOME/lib/c3p0-0.9.1.2.jar"
export JSPIDER_CLASSPATH="$JSPIDER_CLASSPATH:$JSPIDER_HOME/common"
export JSPIDER_CLASSPATH="$JSPIDER_CLASSPATH:$CLASSPATH"

JAVA_OPTS="-Xmx16G"
JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote"
JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.port=9998"
JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.ssl=false"
JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.authenticate=false"

DEBUG_PORT="4143"
DEBUG="-agentlib:jdwp=transport=dt_socket,address=$DEBUG_PORT,server=y,suspend=n"

DATE=$(date +"D%y%m%d-T%H%M%S")
JFRFILE=$JSPIDER_HOME/output/jspider_${DATE}.jfr
#JFR="-XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:+FlightRecorderOptions=\"filename=$JFRFILE,defaultrecording=true,delay=30s,duration=10m\""
JFR="-XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:StartFlightRecording=filename=$JFRFILE,defaultrecording=true,delay=30s,duration=5m"

java $JAVA_OPTS $DEBUG $JFR -cp $JSPIDER_CLASSPATH:$CLASSPATH $JSPIDER_OPTS net.javacoding.jspider.JSpider $1 $2
