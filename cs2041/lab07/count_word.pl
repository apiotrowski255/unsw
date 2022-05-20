#!/usr/bin/perl

use warnings;

$word = $ARGV[0];

while ($line = <STDIN>){
	chomp $line;
	foreach $str (split/[^a-zA-Z]/, $line) {
		$str = lc($str);
      $count{$str} += 1;
	}
}

if (not defined $count{$word}){
   print $word, " occurred 0 times", "\n";
}  else {
   print $word, " occurred ", $count{$word}, " times", "\n";
}
