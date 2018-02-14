#!/bin/bash
rm -r "submissions"
unzip "submissions.zip" -d "submissions"
##create test cases
temps=`find tem -name "*.java"`
echo $temps
cp $temps .

`javac CreateTests.java`
`java CreateTests`


##unzip all files
zips=`find submissions -name "*.zip"`

for z in $zips;
do
	name=${z%.zip}
	mkdir $name
	unzip $z -d $name
	class=`find $name -name "*.java"`
	cp $class .
	`javac TestSort.java`
	`timeout 5 java TestSort $name`
done
