#!/bin/bash

cd "./target"
jar=`find . -name *dependen*.jar`
echo $jar
mv $jar "Fusion.jar"
