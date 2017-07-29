#! /bin/bash

N=30
#train_file=langs_users/all.bottomBigram.dictionFeats.noUND.noGER.top15-K500.train
#train_file=langs_users/all.bottomBigram.dictionFeats.noUND.top15-K500-N$N.train
train_file=../langs_users/all.bottomBigram.dictionFeats.noUND.top15-K500-N$N.train

#train_model=langs_users/all.bottomBigram.dictionFeats.noUND.noGER.top15-K500.model
#train_model=langs_users/all.bottomBigram.dictionFeats.noUND.top15-K500-N$N.model
train_model=../langs_users/all.bottomBigram.dictionFeats.noUND.top15-K500-N$N.model

C=1
#confusion_file=confusion_matrix.users.dictionFeats.noUND.noGER-C$C-K500.txt
#confusion_file=confusion_matrix.users.dictionFeats.noUND-C$C-K500-N$N.txt
confusion_file=../output/confusion_matrix.users.dictionFeats.noUND-C$C-K500-N$N.txt

echo ../svm_multiclass/svm_multiclass_learn -c $C $train_file $train_model
#time ../svm_multiclass/svm_multiclass_learn -c $C $train_file $train_model

if [ -f $confusion_file ]; then
	rm $confusion_file
fi
# Print out the header to the confusion file
printf "lang\t" >> $confusion_file
for lang in `cat ../to_use_lang.txt`; do
	printf "$lang\t" >> $confusion_file
done
echo >> $confusion_file

#echo "lang	lav	por	swe	rum	kor	fre	fin	nor	mlt	pol	spa	dan	ita	eng	scc	dut	ger" >> $confusion_file
for lang in `cat ../to_use_lang.txt`; do
	echo Language is: $lang
	test_file=../langs_users/$lang.bottomBigram-K500-N$N.svm
	pred_file=../langs_users/$lang.bottomBigram-K500-N$N.pred
	../svm_multiclass/svm_multiclass_classify $test_file $train_model $pred_file
	./create_confusion.pl $pred_file >> $confusion_file
done

echo vi $confusion_file
vi $confusion_file
