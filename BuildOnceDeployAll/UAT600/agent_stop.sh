echo "Stopping agents on UAT600"

for i in ulagoms60{1..4}e ;do ssh ${i} "hostname -A; /apps/SterlingFiles/build/svnbuild/killAgents.sh ";done
