#!/bin/bash
conf="conf/masters"
input=`find . -maxdepth 1 -name "*.json"`
jarFile="target/KVStore.jar"
remoteDir="~/ds/fusion"
masters=()

## compile project to jar 
if [[ ${1} == "-p" ]]
then 
	mvn "-Dmaven.test.skip" "package"
fi

cd "./target"
jar=`find . -name *dependen*.jar`
if [[ -n "$jar" ]]
then
	mv $jar "KVStore.jar"
fi
cd "../"

function readFile {
	while read user;do
		read host
		master=$user@$host
		echo $master
		masters+=\ $master
	done <${1} 
}

readFile $conf

for master in $masters;
do 
	ssh $master 'mkdir -p' $remoteDir
	scp $jarFile $master:$remoteDir
	scp $input $master:$remoteDir
done

