#!/usr/bin/env bash

ARTIFACT=function-aws
MAINCLASS=com.example.demo.DemoApplication
VERSION=0.0.1-SNAPSHOT
FEATURE=../../../../spring-graal-native/target/spring-graal-native-0.7.0.BUILD-SNAPSHOT.jar

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

rm -rf target
mkdir -p target/native-image

echo "Packaging $ARTIFACT with Maven"
mvn -DskipTests package > target/native-image/output.txt

JAR="$ARTIFACT-$VERSION.jar"
rm -f $ARTIFACT
echo "Unpacking $JAR"
cd target/native-image
jar -xvf ../$JAR >/dev/null 2>&1
cp -R META-INF BOOT-INF/classes

LIBPATH=`find BOOT-INF/lib | tr '\n' ':'`
CP=BOOT-INF/classes:$LIBPATH:$FEATURE

if [ ! -f "$FEATURE" ]; then
    printf "${RED}FAILURE${NC}: $FEATURE does not exist, please build the root project before building this sample.\n"
    exit 1
fi

GRAALVM_VERSION=`native-image --version`
echo "Compiling $ARTIFACT with $GRAALVM_VERSION"
{ time native-image \
  --verbose \
  --no-server \
  --no-fallback \
  --initialize-at-build-time \
  -H:+PrintMethodHistogram \
  -H:+TraceClassInitialization \
  -H:Name=$ARTIFACT \
  -H:+ReportExceptionStackTraces \
  -Dspring.graal.remove-unused-autoconfig=true \
  -Dspring.graal.remove-yaml-support=true \
  -cp $CP $MAINCLASS >> output.txt ; } 2>> output.txt

if [[ -f $ARTIFACT ]]
then
  printf "${GREEN}SUCCESS${NC}\n"
  mv ./$ARTIFACT ..
  exit 0
else
  cat output.txt
  printf "${RED}FAILURE${NC}: an error occurred when compiling the native-image.\n"
  exit 1
fi

