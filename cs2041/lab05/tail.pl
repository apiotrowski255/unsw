#!/usr/bin/perl

use warnings;

$size = @ARGV;
$N = 10;
$NumLinesInFile = 0;
$diff = 0;


if (@ARGV > 0 && $ARGV[0] =~ /^-\d+$/){
	$N = -$ARGV[0];
	shift @ARGV;

}

if ($size == 0){
	#read from stdin
	$temp = <STDIN>;
	chomp($temp);
	$ARGV[0] = $temp;
} 
	#check if we need to add the name of the file
	foreach $file (@ARGV) {
		if(@ARGV != 1){		
			print "==> $file <==" , "\n";
		}
		open my $info, '<', $file or die "Could not open $file: $!";

		@stuff = <$info>;
		$NumLinesInFile = @stuff;

		close $info;

		$diff = $NumLinesInFile - $N;

		foreach $line (@stuff){
			if($diff > 0){
				$diff--;
			} else {
				print $line;
			}
		} 
	}


