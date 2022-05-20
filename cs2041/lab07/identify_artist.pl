#!/usr/bin/perl

use warnings;

if(@ARGV != 1){
   print "Usage, <file>.txt", "\n";
   exit;
}

$wordFile = $ARGV[0];

my %count;
foreach $file (glob "lyrics/*.txt"){
   open my %info, '<', %file;

   close (%info);
}
