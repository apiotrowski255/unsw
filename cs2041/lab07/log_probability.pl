#!/usr/bin/perl

use warnings;

if(@ARGV != 1){
   print "usage, <String>", "\n";
   exit;
}

$word = $ARGV[0];
$word = lc($word);
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
   printf("log((%d+1)/%6d) = %8.4f %s\n", $foundWords, $totalWords, log(($foundWords + 1)/$totalWords), $fileName);
   close ($info);

}
