#!/usr/local/bin/perl

# Expected input something like: 
# [(slice(132L, 151L, None), slice(275L, 292L, None)), (slice(156L, 175L, None), slice(310L, 327L, None))]

#use strict;
#use warnings;
use Data::Dumper qw(Dumper);

#print "input was: ". Dumper \@ARGV;
#print "argv[0]: ". $ARGV[0]. "\n";

# How many "slice" did we find?
my $cnt = () = $ARGV[0] =~ /slice/g;
my ($topy, $bottomy, $topx, $bottomx, $ty2, $by2, $tx2, $bx2) = "";

#process input:
if ($cnt > 0) {
	# extract the slices and get the coordinates of the top-left and bottom-right of the selection:
	if ($cnt == 4) {
		($topy, $bottomy, $topx, $bottomx, $ty2, $by2, $tx2, $bx2) = $ARGV[0] =~ /slice\((\d+).*?(\d+).*?slice\((\d+).*?(\d+).*?slice\((\d+).*?(\d+).*?slice\((\d+).*?(\d+).*/g;
		#print $topy. " ". $bottomy. " ". $topx. " ". $bottomx. " ". $ty2. " ". $by2. " ". $tx2. " ". $bx2. "\n";
	} elsif ($cnt == 2) {
		($topy, $bottomy, $topx, $bottomx) = $ARGV[0] =~ /slice\((\d+).*?(\d+).*?slice\((\d+).*?(\d+).*?/g;
		#print $topy. " ". $bottomy. " ". $topx. " ". $bottomx. " ". $ty2. " ". $by2. " ". $tx2. " ". $bx2. "\n";
	} else {
		print "no match";
	}
}
print $topx. " ". $topy. " ";
if ($tx2 ne "") {
	print $tx2. " ". $ty2;
}
print "\n";
