#!/bin/sh

cd `dirname $0`
export DATACLEANER_HOME=`pwd`
echo "Using DATACLEANER_HOME: $DATACLEANER_HOME"

export DATACLEANER_LIB_HOME=$DATACLEANER_HOME
echo "Using DATACLEANER_LIB_HOME: $DATACLEANER_LIB_HOME"

mkdir -p $DATACLEANER_HOME
cd $DATACLEANER_HOME

# verify that a Java 21 (or later) runtime is available
JAVA_VERSION_OUTPUT=`java -version 2>&1`
JAVA_MAJOR_VERSION=`echo "$JAVA_VERSION_OUTPUT" | head -1 | sed -E 's/.*version "([0-9]+).*/\1/'`
if [ "$JAVA_MAJOR_VERSION" -lt 21 ]; then
	echo "ERROR: DataCleaner requires Java 21 or later."
	echo "Found: $JAVA_VERSION_OUTPUT"
	echo "Please install a Java 21+ runtime and make sure it is on the PATH."
	exit 1
fi

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
