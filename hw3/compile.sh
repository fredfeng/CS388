if [ -d bin ]; then rm -rf bin; fi
mkdir bin
javac  -d bin -cp lib/stanford-parser.jar:lib/commons-cli-1.2.jar  $(find ./src/* | grep .java)
