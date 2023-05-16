echo "copy latest extensions customer_overrides.properties,academy,resource & entity jars started on app servers"

for i in ulapoms80{1..9}e ulapoms81{0..8}e ;do ssh ${i} "hostname -A;rm -rf /apps/SterlingOMS/Foundation/extensions";done

for i in ulapoms80{1..9}e ulapoms81{0..8}e ;do scp -r /apps/SterlingOMS/Foundation/extensions ${i}:/apps/SterlingOMS/Foundation/;done

for i in ulapoms80{1..9}e ulapoms81{0..8}e ;do scp /apps/SterlingOMS/Foundation/properties/customer_overrides.properties ${i}:/apps/SterlingOMS/Foundation/properties;done

for i in ulapoms80{1..9}e ulapoms81{0..8}e ;do scp /apps/SterlingOMS/Foundation/jar/platform/9_5/resources.jar ${i}:/apps/SterlingOMS/Foundation/jar/platform/9_5;done

for i in ulapoms80{1..9}e ulapoms81{0..8}e ;do scp /apps/SterlingOMS/Foundation/jar/platform/9_5/entities.jar ${i}:/apps/SterlingOMS/Foundation/jar/platform/9_5;done

for i in ulapoms80{1..9}e ulapoms81{0..8}e ;do scp /apps/SterlingOMS/Foundation/jar/academy/1/academy.jar ${i}:/apps/SterlingOMS/Foundation/jar/academy/1/academy.jar;done

for i in ulapoms80{1..9}e ulapoms81{0..8}e ;do scp /apps/SterlingOMS/Foundation/rcpdrop/windows/9.3/som.zip ${i}:/apps/SterlingOMS/Foundation/rcpdrop/windows/9.3/som.zip;done

for i in ulapoms80{1..9}e ulapoms81{0..8}e ;do ssh ${i} "hostname -A;cd /apps/SterlingOMS/ && chmod -R 755 /apps/SterlingOMS/";done

echo "copy latest extensions customer_overrides.properties,academy,resource & entity jars started on app servers completed at $(date)"