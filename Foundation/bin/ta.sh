#!/bin/sh
# Licensed Materials - Property of IBM
# IBM Sterling Selling and Fulfillment Suite
# (C) Copyright IBM Corp. 2001, 2013 All Rights Reserved.
# US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.

. /apps/SterlingOMS/Foundation/bin/tmp.sh

USE_AGENT_JAVA=1
export USE_AGENT_JAVA

    AGENT_JAVA_OPTS="-Dvendor=shell -DvendorFile=/apps/SterlingOMS/Foundation/properties/servers.properties -DCACHE_PS=true"
export AGENT_JAVA_OPTS

AGENT_LOG="-DBSC_APP_LOG_FILE=${1}-$$ -DLOG_DIR=/appslog/triggeragent"

    ${JAVA} -classpath /apps/SterlingOMS/Foundation/jar/bootstrapper.jar:/apps/SterlingOMS/Foundation/jar/commons_collections/3_2/commons-collections-3.2.jar ${AGENT_JAVA_OPTS} ${AGENT_LOG} com.sterlingcommerce.woodstock.noapp.NoAppLoader -class com.yantra.ycp.agent.server.YCPAgentTrigger  -f /apps/SterlingOMS/Foundation/properties/AGENTDynamicclasspath.cfg -invokeargs -criteria "$@"
