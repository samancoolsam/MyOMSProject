echo "copy latest extensions customer_overrides.properties,academy,resource,entity jars & agent startup scripts started on agent servers"

for i in ulagoms80{1..9}e ulagoms81{0..6}e ;do ssh ${i} "hostname -A;rm -rf /apps/SterlingOMS/Foundation/extensions";done

for i in ulagoms80{1..9}e ulagoms81{0..6}e ;do scp -r /apps/SterlingOMS/Foundation/extensions ${i}:/apps/SterlingOMS/Foundation/;done

for i in ulagoms80{1..9}e ulagoms81{0..6}e ;do scp /apps/SterlingOMS/Foundation/properties/customer_overrides_agent.properties ${i}:/apps/SterlingOMS/Foundation/properties/customer_overrides.properties;done

for i in ulagoms80{1..9}e ulagoms81{0..6}e ;do scp /apps/SterlingOMS/Foundation/jar/platform/9_5/resources.jar ${i}:/apps/SterlingOMS/Foundation/jar/platform/9_5;done

for i in ulagoms80{1..9}e ulagoms81{0..6}e ;do scp /apps/SterlingOMS/Foundation/jar/platform/9_5/entities.jar ${i}:/apps/SterlingOMS/Foundation/jar/platform/9_5;done

for i in ulagoms80{1..9}e ulagoms81{0..6}e ;do scp /apps/SterlingOMS/Foundation/jar/academy/1/academy.jar ${i}:/apps/SterlingOMS/Foundation/jar/academy/1/academy.jar;done

for i in ulagoms80{1..9}e ulagoms81{0..6}e ;do ssh ${i} "hostname -A;cd /apps/SterlingOMS/ && chmod -R 755 /apps/SterlingOMS/";done

for i in ulagoms80{1..9}e ulagoms81{0..6}e ;do scp /apps/SterlingOMS/Foundation/bin/startSterlingServers* ${i}:/apps/SterlingOMS/Foundation/bin/;done

echo "copy latest extensions customer_overrides.properties,academy,resource,entity jars & agent startup scripts completed at $(date)"
