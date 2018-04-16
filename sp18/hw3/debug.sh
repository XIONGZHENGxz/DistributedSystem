#!/bin/bash
unzip "submissions.zip" -d "submissions"
##unzip all files
zips=`find submissions -name "*.zip"`
mkdir -p "test"

for z in $zips;
do
	name=${z%.zip}
	mkdir $name
	unzip $z -d $name
	class=`find $name -name "*.java"`
	cp $class "test"
	cp ../Diff.java "test"
	cd "test"
	javac "*.java"
	score=0
	for i in `seq 1 6`
	do 
		sleep 1
		lsof -i -P -n | grep 9000
		cp ../test$i/* .
		java "BookServer" "input.txt" &
		spid=$!
		sleep 1
		if [ ps -p $spid ]	
		then  
			java "BookClient" "command.txt" "1"
			cpid=$!
			sleep 1
			`java Diff "out_1.txt" "expected_out.txt"`
			if [ -s diff.txt ]
			then
				score=$((score+1))
			else 
				rm "diff.txt"
			fi
		fi
		rm *.txt
		lsof -i -P -n | grep 9000
		echo $spid
		kill $spid
	done

	for i in `seq 7 8`
	do 
		sleep 1
		cp ../test$i/* .
		java "BookServer" "input.txt" &
		spid=$!
		sleep 1
		if [ ps -p $spid ]
		then
			java "BookClient" "command1.txt" "1" &
			cpid1=$!
			java "BookClient" "command2.txt" "2" &
			cpid2=$!
			sleep 1
			`java Diff "inventory.txt" "expected_inventory.txt"`
			if [ -s diff.txt ]
			then
				score=$((score+1))
			fi
		fi
		rm *.txt
		kill $spid 
	done
	cd "../"
	score=$((score*10))
	echo "$name: $score" >> "scores.txt" 
	echo "$name: $score" 
done
