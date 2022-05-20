#!/usr/bin/perl
#
use warnings;

if(@ARGV > 1){
   exit;
}
if(@ARGV == 0){
 exit;
}

open(my $fh, '<:encoding(UTF-8)', $ARGV[0]) or die "Could not open file $ARGV[0] $!";

@lines = ();
while(my $row = <$fh>){
   chomp $row;
   $lines[@lines] = $row;
}

$num = @lines;


if($num > 0){
   if($num % 2 == 0){
      $num = $num / 2 - 1;
      print $lines[$num], "\n";
      $num += 1;
      print $lines[$num], "\n";
   } else {
      $num -= 1;
      $num = $num / 2;
      print $lines[$num], "\n";

   }
}
