#! /bin/bash

# usage: run_classifier_single <TRAIN_FILE> <MODEL_FILE> <TEST_FILE> <PRED_FILE> <C>
train_file=$1
train_model=$2
test_file=$3
pred_file=$4
C=$5

echo "LEARN"
echo ./svm_multiclass/svm_multiclass_learn -c $C $train_file $train_model
../svm_multiclass/svm_multiclass_learn -c $C $train_file $train_model

echo "CLASSIFY"
echo ./svm_multiclass/svm_multiclass_classify $test_file $train_model $pred_file
../svm_multiclass/svm_multiclass_classify $test_file $train_model $pred_file
