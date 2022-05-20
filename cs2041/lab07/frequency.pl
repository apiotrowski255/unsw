#!/usr/bin/perl

use warnings;

if(@ARGV != 1){
   print "usage, <String>", "\n";
   exit;
}

$word = $ARGV[0];
#print "$word\n";

foreach $file (glob "lyrics/*.txt"){
   #print "$file\n";
   open my $info, '<' , $file;

   $totalWords = 0;
   $foundWords = 0;

   while ($line = <$info>){
      chomp $line;
      @words = split /[^a-zA-Z]/, $line;
      foreach $str (@words){
         $str = lc($str);
         if($str){
            $totalWords += 1;
            if($str eq $word){
               $foundWords += 1;
            }
         }
      }
   }

   $fileName = $file;
   $fileName =~ s/\.txt//;
   $fileName =~ s|lyrics/||;
   $fileName =~ s/_/ /g;   
   printf("%4d/%6d = %.9f %s\n", $foundWords, $totalWords, $foundWords/$totalWords, $fileName);
   close ($info);

}
