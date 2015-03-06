echo Running 1st experiemnt-HMM
time ./atis.sh hmm posMallet/atis/atis.txt

echo Running 2nd experiemnt-HMM plus
time ./atis.sh hmm posMalletFea/atis/atis.txt

echo Running 3rd experiemnt-CRF 
time ./atis.sh crf posMallet/atis/atis.txt

echo Running 4th experiemnt-CRF plus
time ./atis.sh crf posMalletFea/atis/atis.txt
