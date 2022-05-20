#!/usr/bin/perl

use warnings;

#$pods = 0;
#$name = '';
#$i = 0;
#$oldLine = '';

for $line (<STDIN>) {	
	$line =~ tr/A-Z/a-z/;
	$line =~ s/ *$//;
	$line =~ s/s$//g;
	$line =~ s/ +/ /g;
	$line =~ m/(\d+) (.+)/;
	#print $1, "\n";
	#print $2, "\n";
	$data{$2}{"indivs"} += $1;
	$data{$2}{"pods"} += 1;	
	#print $data{$2}, "\n";
}

foreach $key (sort keys(%data)) {
	print $key , " observations: ", $data{$key}{"pods"}," pods, " ,$data{$key}{"indivs"}, " individuals","\n";
}



