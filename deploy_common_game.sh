#!/bin/bash

if [ $# -eq 0 ]; then
	echo " You should specify a target to deploy to."
	exit 1;
fi

case "$1" in 
	"dice" )
	    target="root@58.215.164.153:~/dice/lib"
	    ;;
	"zhajinhua" )
	    target="root@58.215.184.18:~/zhajinhua/lib"
	    ;;
	"traffic" ) 
	    target="root@58.215.184.18:~/game/lib"
	    ;;
	*)
	    echo " No support for this target yet, add it in this script yourself :-) "
	    ;;
esac

# fix your location if needed
COMMON_JAVA_JAR=./target/Common_Java-1.0-SNAPSHOT.jar
scp ${COMMON_JAVA_JAR} ${target} 
	
