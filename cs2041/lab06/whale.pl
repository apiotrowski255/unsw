#!/usr/bin/perl
use warnings;

$num_args = @ARGV + 1;
if ($num_args != 2){
	print "\n Only one argument\n";
	exit;
}

$name = $ARGV[0];

$pods = 0;
$indiv = 0;
$i = 0;

while ($line = <STDIN>){
	#print $line;
	if($line =~ m/$name/){
		#print $line;
		$pods += 1;
		($i) = ($line =~ m/([0-9]+)/);
		$indiv += $i;
	}
}

print $name, " observations: ", $pods, " pods, ", $indiv, " individuals", "\n";
