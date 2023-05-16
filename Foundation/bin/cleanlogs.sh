cd /appslog
find . -type f -mtime +1 -exec rm {} \;
touch clean.done
