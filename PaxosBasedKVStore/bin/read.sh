#!/bin/bash

function readFile {
	while read user;do
		read host
		master=$user@$host 
		masters+=
		masters+=\ $master
	done <${1} 
}

