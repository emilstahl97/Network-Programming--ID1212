DIR=$(dirname $0)

javac -cp $DIR/activation.jar:$DIR/javax.mail-1.6.2.jar:$DIR/jsoup-1.13.1.jar *.java &&
java -cp $DIR/activation.jar:$DIR/javax.mail-1.6.2.jar:$DIR/jsoup-1.13.1.jar: Server