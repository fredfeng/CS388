if [ -d bin ]; then rm -rf bin; fi
mkdir bin
javac  -d bin $(find ./src/* | grep .java)
