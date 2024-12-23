#!/bin/sh

cd `dirname $0`
export DATACLEANER_HOME=`pwd`
echo "Using DATACLEANER_HOME: $DATACLEANER_HOME"

export DATACLEANER_LIB_HOME=$DATACLEANER_HOME
echo "Using DATACLEANER_LIB_HOME: $DATACLEANER_LIB_HOME"

mkdir -p $DATACLEANER_HOME
cd $DATACLEANER_HOME

macos=false
case "`uname`" in
	Darwin*) macos=true;;
esac

DATACLEANER_JAVA_OPTS="$JAVA_OPTS -Xmx1024m"
if $macos; then
	DATACLEANER_JAVA_OPTS="$DATACLEANER_JAVA_OPTS -Xdock:name=DataCleaner --add-exports java.desktop/com.apple.eawt=ALL-UNNAMED"
fi
echo "Using DATACLEANER_JAVA_OPTS: $DATACLEANER_JAVA_OPTS"

exec java $DATACLEANER_JAVA_OPTS -jar $DATACLEANER_LIB_HOME/DataCleaner.jar "$@"
