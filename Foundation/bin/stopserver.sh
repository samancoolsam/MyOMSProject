echo "Stopping $1 ...."
#kill $(ps -ef | grep java | grep -v wrapper | grep $1 | awk '{print $2}')
kill $(ps -ef | grep java | grep $1 | awk '{print $2}')