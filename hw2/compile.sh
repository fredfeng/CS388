if [ -d bin ]; then rm -rf bin; fi
mkdir bin
javac  -d bin -cp lib/mallet-deps.jar $(find ./src/* | grep .java)
