echo "Stopping agents on UAT700"

for i in ulagoms70{1..5}e ;do ssh ${i} "hostname -A; /apps/SterlingFiles/build/svnbuild/killAgents.sh ";done
