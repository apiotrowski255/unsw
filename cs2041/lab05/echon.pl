#!/usr/bin/perl

#This program strictly takes only two inputs, first input has to be a number, second input can be a string

use warnings;

if(@ARGV ne 2){
	print "$0 ", "<number of lines> ", "<string>", "\n";
	exit
}

$Number = $ARGV[0];
$String = $ARGV[1];

if($Number !~ /^\d+$/){
	print "$0", ": ", "argument 1 must be a non-negative integer", "\n";
	exit
}


while ($Number > 0){
	print "$String", "\n";
	$Number--;
}
