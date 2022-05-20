#!/usr/bin/perl

use warnings;

$course = $ARGV[0];

print $course, "\n";

$web = "http://www.handbook.unsw.edu.au/postgraduate/courses/2017/";
$web2 = "http://www.handbook.unsw.edu.au/undergraduate/courses/2017/";
$html = ".html";

$websiteP = $web . $course . $html;
$websiteU = $web2 . $course . $html;
print "website to enter is ", $websiteP, "\n";
print "or ", "$websiteU", "\n"; 


open F, "wget -q -O- $websiteU $websiteP|" or die;
while ($line = <F>) {
	if ($line =~ m/Prerequisite.*/){
		$line =~ s/Excluded.*//;
		$line =~ s/[A-Z]{4}[\d]{4}//g;
		print $line;
	}
}
