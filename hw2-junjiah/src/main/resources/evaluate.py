#!/usr/bin/python
import sys

with open('data/sample.out', 'rb') as f:
    golden_standards = set(f.readlines())

with open(sys.argv[1], 'rb') as f:
    annotated_results = set(f.readlines())

retrieved_and_relevant = golden_standards.intersection(annotated_results)
precision = len(retrieved_and_relevant) / float(len(annotated_results))
recall = len(retrieved_and_relevant) / float(len(golden_standards))
f1_score = 2 * precision * recall / (precision + recall)

print "precision: %f" % precision
print "recall: %f" % recall
print "f1 score: %f" % f1_score
