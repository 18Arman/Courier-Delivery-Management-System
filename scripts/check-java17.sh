#!/bin/zsh

export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"

echo "java:"
java -version
echo
echo "maven:"
mvn -version

