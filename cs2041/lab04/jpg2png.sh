#!/bin/sh
string=".png"

for f in *
do
	if [ ${f: -4} == ".jpg" ]
	then
		# echo "$f"
		# echo "needs to be converted to"
		endfile=$(echo $f | cut -f1 -d '.')
		endfile="${endfile}$string"
		# echo "$endfile"
		if [ -f "$endfile" ]
		then
			echo "$endfile already exists"
		else
			convert "$f" "$endfile"
		fi
	fi
done
