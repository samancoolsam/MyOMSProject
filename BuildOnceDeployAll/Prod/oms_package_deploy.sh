# Unzip OMS_Package
echo $1
echo $2

rm -rf OMS_package/

unzip OMS_package-$1-$2.zip

# copy Build Properties 

cp OMS_package/build95.properties .

# delete existing ear fro external_deployments folder

rm -rf /apps/SterlingOMS/Foundation/external_deployments/smcfs.ear

#copy EAR to external_deployments

cp OMS_package/smcfs.ear /apps/SterlingOMS/Foundation/external_deployments

# deleting existing properties files from Foundation folder 

rm -rf  /apps/SterlingOMS/Foundation/properties/customer_overrides.properties
rm -rf  /apps/SterlingOMS/Foundation/properties/customer_overrides_agent.properties

# copy properties to Foundation folder

cp -rf OMS_package/customer_overrides.properties.PRD400.95 /apps/SterlingOMS/Foundation/properties/customer_overrides.properties
cp -rf OMS_package/customer_overrides_agent.properties.PRD400.95 /apps/SterlingOMS/Foundation/properties/customer_overrides_agent.properties

# delete existing extension from Foundation folder

rm -rf /apps/SterlingOMS/Foundation/extensions

# copy extensions to Foundation folder

cp -R OMS_package/extensions /apps/SterlingOMS/Foundation/

# copy repository to Foundation folder

cp -R OMS_package/repository /apps/SterlingOMS/Foundation/

# delete existing jars from foundation folder 

rm -rf /apps/SterlingOMS/Foundation/jar/platform/9_5/*s.jar
rm -rf /apps/SterlingOMS/Foundation/jar/academy/1/academy.jar
rm -rf /apps/SterlingOMS/Foundation/repository/eardata/wsc/war/WEB-INF/lib/academywsc.jar

# copy academy,academywsc, entity & resource jar to foundation folder

cp OMS_package/*s.jar /apps/SterlingOMS/Foundation/jar/platform/9_5/
cp OMS_package/academy.jar /apps/SterlingOMS/Foundation/jar/academy/1/academy.jar
cp OMS_package/academywsc.jar /apps/SterlingOMS/Foundation/repository/eardata/wsc/war/WEB-INF/lib/academywsc.jar

# copy som.zip to foundation folder & update locations.ycfg

cp OMS_package/som.zip /apps/SterlingOMS/Foundation/rcpdrop/windows/9.3/som.zip
cd /apps/SterlingOMS/Foundation/rcpdrop/windows/9.3/
rm -rf som/
unzip som.zip -d som
cd som/plugins/com.yantra.yfc.rcp_1.0.0
/usr/java8_64/bin/jar xf resources.jar locations.ycfg
cp ../../../../../../rcpextn_som/resources/locations.ycfg .
/usr/java8_64/bin/jar uvf resources.jar locations.ycfg
rm -rf locations.ycfg
cd ../../..
zip -r som.zip som

