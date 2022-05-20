#!/usr/bin/perl
#this perl programs reads from stdin
#it converts 01234 to < and
# 6789 to >. It leaves 5 untouched.

use warnings;

while ($line = <STDIN>){
	$line =~ s/[01234]/</g;
	$line =~ s/[6789]/>/g;
	print "$line";
}

