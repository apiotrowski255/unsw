if [ $# -ne 2 ]
then
	echo "Usage: ./echon.sh <number of lines> <string>"
	exit 1
fi

number=$1

if test "$1" -ge 0 2>/dev/null
then
	:
else
	echo "./echon.sh: argument 1 must be a non-negative integer"
	exit 1
fi

increment=0
while test $increment -lt $number
do
	echo $2
	increment=$(($increment + 1))
done
exit 0
