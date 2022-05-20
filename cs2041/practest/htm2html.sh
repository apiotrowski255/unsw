#!/bin/sh
string=".html"

for f in *
do
	if [ ${f: -4} = ".htm" ]
	then
		endfile=$(echo $f | cut -f1 -d'.')
		endfile=$"{endfile}$string"
		if [ -f "$endfile" ]
		then
			echo "$endfile already exists"
		else
			convert "$f" "$endfile"
		fi
	fi
done
