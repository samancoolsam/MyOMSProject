#!/bin/ksh
echo "Rsync process started at $(date)"

cd /apps/SterlingFiles/deploy-only/Rsync-DeployAll

./copyAppServer.sh >> /apps/SterlingFiles/deploy-only/Rsync-DeployAll/rsync.log &

./copyAgentServer.sh >> /apps/SterlingFiles/deploy-only/Rsync-DeployAll/rsync.log &

echo "Rsync process completed at $(date)"
