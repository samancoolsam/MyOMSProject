echo "parm1=$1"

if [ ! -f ./setenv ]
then
    echo ERROR: setenv does not exist.
    exit 1
fi
. ./setenv

if [ x${ANT_HOME}x == xx ]
then
        echo ERROR: ANT_HOME is not set in setenv.
        exit 1
fi

${ANT_HOME}/bin/ant -buildfile /apps/SterlingFiles/deploy-only/cdt.xml -Dbuild.cdt=$1 -Dserver=PRD400

if [ $? -ne 0 ]
then
        exit 1
fi
