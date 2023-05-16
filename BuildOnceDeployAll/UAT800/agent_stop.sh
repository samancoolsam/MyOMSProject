echo "Stopping agents on UAT800"

for i in ulagoms80{1..9}e ulagoms81{0..6}e ;do ssh ${i} "hostname -A; /apps/SterlingFiles/build/svnbuild/killAgents.sh ";done
