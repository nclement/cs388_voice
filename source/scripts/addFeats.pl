#! /usr/bin/perl

#################################################################################
# This will take a single SVM file that has the user information tagged at the 
# end of the line (after a '#').
#
# Since this could be a file, it will strip off all folder directory information 
# and the final '.txt' from the file name
#
# The extra file is of the format:
#     <USERID>||<VAR_1>|<VAR_2>|...|<VAR_n>
# where <VAR_i> is any number
#################################################################################

my $styleFeatsFile = shift;
my $svm_file = shift;

die "usage: addFeats <sytleFeats.out> <SVM_FILE> > OUTPUT" if !$styleFeatsFile || !$svm_file;

open(FEATS, $styleFeatsFile) or die "Could not find style features file (looking for $styleFeatsFile): $!\n";

my %user_feats = ();
while(<FEATS>) {
	/^(\S+)\|\|(.*)$/;
	$user_feats{$1} = $2;
	#print STDERR "user $1 has feats $2\n";
}
close(FEATS);

open(SVMS, $svm_file) or die "Could not open svm file $svm_file: $!\n";

# Loop over all the svm file entries
while(<SVMS>) {
	/^(\d+) (.*) \# (\S+)$/;
	my $lang = $1;
	my $svmVars = $2;
	my $filename = $3;
	$filename =~ /.*\/?(P\d+)(:?.txt)?$/;
	my $user = $1;

	my @extra_feats = split(/\|/,$user_feats{$user});
	print "$lang ";
	my $i=1;
	for (; $i<=$#extra_feats+1; $i++) {
		print "$i:$extra_feats[$i-1] ";
	}
	$i--;
	#print STDERR "feats ($i)", $#extra_feats,": $user_feats{$user}\n";
	#print STDERR "vars: $svmVars\n";
	my @svm_feats = split(" ", $svmVars);
	if($#svm_feats == 0) {
		$svmVars =~ /(\d+):(\S+)/;
		#print STDERR "...special! with svmVars=$svmVars, num=$1 and 2=$2\n";
		my $new_num = $1+$i;
		print "$new_num:$2 ";
	}
	else {
		#print STDERR "size of $#svm_feats\n";
		foreach (@svm_feats) {
			chomp;
			/(\d+):(\S+)/;
			my $new_num = $1+$i;
			print "$new_num:$2 ";
		}
	}
	print "# $user\n";
}


