#!/bin/bash
unzip "submissions.zip" -d "submissions"
##unzip all files
zips=`find submissions -name "*.zip"`
mkdir -p "test"
eid=$1
echo "grading..." $eid
for z in $zips;
do
	name=${z%.zip}
	echo $name
	if [[ ! $name = *$eid* ]]
	then 
		continue
	fi
	mkdir -p $name
	unzip $z -d $name
	class=`find $name -name "*.java"`
	cp $class "test"
	cp "Diff.java" "test"
	cd "test"
	css=`find . -name "*.java"`
	compile1=`javac BookServer.java`
	compile2=`javac BookClient.java`
	javac Diff.java
	if [[ ! -z $compile1 || ! -z $compile2 ]]
	then 
		score=0
		echo "$name: $score" >> "scores.txt" 
		cd ../
		echo "cannot compile" > "$name/out.txt"
		echo "cannot compile"
		continue
	else 
		score=44
		echo "compile done"
	fi

	for i in `seq 1 5`
	do 
		sleep 2
		cp ../test$i/* .
		java "BookServer" "input.txt" &
		spid=$!
		sleep 1
		if ps -p $spid 
		then  
			java "BookClient" "command.txt" "1" &
			cpid=$!
			sleep 1
			java Diff "out_1.txt" "expected_out.txt" &
			sleep 0.5
			if [ ! -f diff.txt ]
			then
				echo "test $i success" 
				score=$((score+6))
			else 
				echo "test $i fail"
				cp "out_1.txt" "$name/test1_out.txt"
				cp "expected_out.txt" $name
				cd ..
				rm "diff.txt"
				cd "test"
			fi
		fi
		rm *.txt
		kill $cpid
		kill $spid
	done

	for i in `seq 6 6`
	do 
		sleep 2
		cp ../test$i/* .
		java "BookServer" "input.txt" &
		spid=$!
		sleep 1
		if ps -p $spid 
		then
			java "BookClient" "command1.txt" "1" &
			cpid1=$!
			java "BookClient" "command2.txt" "2" &
			cpid2=$!
			sleep 1
			java Diff "inventory.txt" "expected_inventory.txt" &
			sleep 0.5
			if [ ! -f diff.txt ]
			then
				echo "test 6 success"
				score=$((score+6))
			else
				echo "fail"
				cp "inventory.txt" $name
				cp "expected_inventory.txt" $name
				rm "diff.txt"
			fi
		fi
		rm *.txt
		kill $cpid1
		kill $cpid2
		kill $spid 
	done
	cd "../"
	echo "$name: $score" >> "scores.txt" 
	echo "$name: $score" 
done
