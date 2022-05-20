#!/bin/sh
input=`ls -l`

j=11 #contains the name

echo -n "Small files:"
#find all files with less than 25 lines
while test $j -lt 500
do
	name=`echo $input | cut -d" " -f$j`
	if [ "$name" == "" ]
	then
		j=999
	else
		lines=`wc -l $name | cut -d" " -f1`
		if [ "$lines" -lt 10 ]
		then
		echo -n " "
		echo -n $name
		fi
		j=$(($j + 9))
	fi
done
echo ""

echo -n "Medium-sized files:"

j=11
while test $j -lt 500
do
	name=`echo $input | cut -d" " -f$j`
	if [ "$name" == "" ]
	then
		j=999
	else
		lines=`wc -l $name | cut -d" " -f1`
		if [ "$lines" -ge 10 ]
		then
			if [ "$lines" -lt 100 ]
			then
				echo -n " "
				echo -n $name
			fi
		fi
		j=$(($j + 9))
	fi
done
echo ""

echo -n "Large files:"

j=11
while test $j -lt 500
do
	name=`echo $input | cut -d" " -f$j`
	if [ "$name" == "" ]
	then 
		j=999
	else
		lines=`wc -l $name | cut -d" " -f1`
		if [ "$lines" -ge 100 ]
		then
			echo -n " "
			echo -n $name
		fi
	j=$(($j + 9))
	fi
done
echo ""
