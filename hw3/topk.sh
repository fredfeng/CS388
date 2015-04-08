#java -cp bin/:lib/stanford-parser.jar edu.utexas.nlp.ActiveLearner --trainBank wsj/00/wsj_0001.mrg --candidateBank wsj/00/wsj_0002.mrg --testBank wsj/20/wsj_2095.mrg --nextTrainBank wsj/20/wsj_2001.mrg --nextCandidatePool pool --selectionFunction random

echo "------------TREE(K=5)-------------------------"
java -mx4500m -cp bin/:lib/stanford-parser.jar:lib/commons-cli-1.2.jar edu.utexas.nlp.ActiveLearner -selectionFunction TREE -initBank wsj/init/init.mrg -trainBank wsj/0103.mrg -testBank wsj/20.mrg -candidateBank wsj/top5.msg -K 5

echo "------------TREE(K=10)-------------------------"
java -mx4500m -cp bin/:lib/stanford-parser.jar:lib/commons-cli-1.2.jar edu.utexas.nlp.ActiveLearner -selectionFunction TREE -initBank wsj/init/init.mrg -trainBank wsj/0103.mrg -testBank wsj/20.mrg -candidateBank wsj/top10.msg

echo "------------TREE(K=15)-------------------------"
java -mx4500m -cp bin/:lib/stanford-parser.jar:lib/commons-cli-1.2.jar edu.utexas.nlp.ActiveLearner -selectionFunction TREE -initBank wsj/init/init.mrg -trainBank wsj/0103.mrg -testBank wsj/20.mrg -candidateBank wsj/top15.msg -K 15

echo "------------TREE(K=20)-------------------------"
java -mx4500m -cp bin/:lib/stanford-parser.jar:lib/commons-cli-1.2.jar edu.utexas.nlp.ActiveLearner -selectionFunction TREE -initBank wsj/init/init.mrg -trainBank wsj/0103.mrg -testBank wsj/20.mrg -candidateBank wsj/top20.msg -K 20

