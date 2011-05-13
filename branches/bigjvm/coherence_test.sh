#!/bin/sh

CLASS_PATH=".:azul-memory-test-0.0.1.jar:coherence.jar"

COMMON_OPTIONS="-Dtime=1200 -DwarmUptime=120 -DwarmUpCount=1"
COMMON_OPTIONS="$COMMON_OPTIONS -DinitCacheSize=8000000 -DmaxCacheSize=8000000"
COMMON_OPTIONS="$COMMON_OPTIONS -DrecordSize=1024 -Ddispersion=256 -DbulkSize=1 -DreadersCount=14 -DwritersCount=2"
COMMON_OPTIONS="$COMMON_OPTIONS -DsampleSize=1382400 -DbufferSize=2 -DloggersCount=2 -DwritersOps=128 -DreadersOps=164.57"

JVM_OPTIONS="-server -Xms1G -Xmx1G -DuseEhcache=false -DuseCoherence=true -DuseSmartRecord=true -XX:PretenureSizeThreshold=1048576 -XX:+PrintGCDetails -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCTimeStamps -XX:+PrintFlagsFinal $COMMON_OPTIONS"

date=11052011

java -Xmx15G -Xms15G -server -XX:+UseG1GC -XX:G1HeapRegionSize=8m -XX:+PrintGCDetails -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCTimeStamps -XX:+PrintFlagsFinal -Xloggc:cache_server1_4clients_${date}_g1gc.log -cp .:coherence.jar -Dtangosol.coherence.cacheconfig=cache-config.xml com.tangosol.net.DefaultCacheServer &> cache_server1_4_g1_${date}_out.txt &

sleep 5

java -Xmx15G -Xms15G -server -XX:+UseG1GC -XX:G1HeapRegionSize=8m -XX:+PrintGCDetails -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCTimeStamps -XX:+PrintFlagsFinal -Xloggc:cache_server2_4clients_${date}_g1gc.log -cp .:coherence.jar -Dtangosol.coherence.cacheconfig=cache-config.xml com.tangosol.net.DefaultCacheServer &> cache_server2_4_g1_${date}_out.txt &

sleep 5

java -cp $CLASS_PATH $COMMON_OPTIONS -DuseSmartRecord=true -Dtangosol.coherence.cacheconfig=cache.xml dataloader

sleep 5

iostat -c 1 >client_4_g1_cpu_${date}.txt &

for i in `seq 3`; do
	TEST_OPTIONS="-Dmode=heap -DoutputDir=client_4_g1_${date}_N${i} -Xloggc:client${i}_${date}_g1gc.log";
	java -cp "$CLASS_PATH" $JVM_OPTIONS $TEST_OPTIONS -Dtangosol.coherence.cacheconfig=cache.xml azul.test.Main &> client${i}_4_g1_${date}_out.txt &
	sleep 1;
done

TEST_OPTIONS="-Dmode=heap -DoutputDir=client_4_g1_${date}_N4 -Xloggc:client4_${date}_g1gc.log";
java -cp "$CLASS_PATH" $JVM_OPTIONS $TEST_OPTIONS -Dtangosol.coherence.cacheconfig=cache.xml azul.test.Main &> client4_4_g1_${date}_out.txt

kill `ps ax | awk '{if($5 == "iostat"){print $1}}'`

sleep 20

kill `ps ax | awk '{if($5 == "java"){print $1}}'`

sleep 20

java -Xmx15G -Xms15G -server -XX:+UseG1GC -XX:G1HeapRegionSize=8m -XX:+PrintGCDetails -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCTimeStamps -XX:+PrintFlagsFinal -Xloggc:cache_server1_4clients_rj_${date}_g1gc.log -cp .:coherence.jar -Dtangosol.coherence.cacheconfig=cache-config-rj.xml com.tangosol.net.DefaultCacheServer &> cache_server1_4_g1_rj_${date}_out.txt &

sleep 5

java -Xmx15G -Xms15G -server -XX:+UseG1GC -XX:G1HeapRegionSize=8m -XX:+PrintGCDetails -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCTimeStamps -XX:+PrintFlagsFinal -Xloggc:cache_server2_4clients_rj_${date}_g1gc.log -cp .:coherence.jar -Dtangosol.coherence.cacheconfig=cache-config-rj.xml com.tangosol.net.DefaultCacheServer &> cache_server2_4_g1_rj_${date}_out.txt &

sleep 5

java -cp $CLASS_PATH $COMMON_OPTIONS -DuseSmartRecord=true -Dtangosol.coherence.cacheconfig=cache.xml dataloader

sleep 5

iostat -c 1 >client_4_g1_rj_cpu_${date}.txt &

for i in `seq 3`; do
	TEST_OPTIONS="-Dmode=heap -DoutputDir=client_4_g1_rj_${date}_N${i} -Xloggc:client${i}_rj_${date}_g1gc.log";
	java -cp "$CLASS_PATH" $JVM_OPTIONS $TEST_OPTIONS -Dtangosol.coherence.cacheconfig=cache.xml azul.test.Main &> client${i}_4_g1_rj_${date}_out.txt &
	sleep 1;
done

TEST_OPTIONS="-Dmode=heap -DoutputDir=client_4_g1_rj_${date}_N4 -Xloggc:client4_rj_${date}_g1gc.log";
java -cp "$CLASS_PATH" $JVM_OPTIONS $TEST_OPTIONS -Dtangosol.coherence.cacheconfig=cache.xml azul.test.Main &> client4_4_g1_rj_${date}_out.txt

kill `ps ax | awk '{if($5 == "iostat"){print $1}}'`

sleep 20

kill `ps ax | awk '{if($5 == "java"){print $1}}'`

sleep 20

java -Xmx15G -Xms15G -server -XX:NewSize=256m -XX:MaxNewSize=256m -XX:SurvivorRatio=8 -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=20 -verbose:gc -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCDetails -XX:+PrintTenuringDistribution -XX:+PrintGCTimeStamps -XX:+PrintFlagsFinal -cp .:coherence.jar -Dtangosol.coherence.cacheconfig=cache-config.xml com.tangosol.net.DefaultCacheServer >cache_server1_4clients_${date}_cmsgc.log &

sleep 5

java -Xmx15G -Xms15G -server -XX:NewSize=256m -XX:MaxNewSize=256m -XX:SurvivorRatio=8 -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=20 -verbose:gc -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCDetails -XX:+PrintTenuringDistribution -XX:+PrintGCTimeStamps -XX:+PrintFlagsFinal -cp .:coherence.jar -Dtangosol.coherence.cacheconfig=cache-config.xml com.tangosol.net.DefaultCacheServer >cache_server2_4clients_${date}_cmsgc.log &

sleep 5

java -cp $CLASS_PATH $COMMON_OPTIONS -DuseSmartRecord=true -Dtangosol.coherence.cacheconfig=cache.xml dataloader

sleep 5

iostat -c 1 >client_4_cms_cpu_${date}.txt &

for i in `seq 3`; do
	TEST_OPTIONS="-Dmode=heap -DoutputDir=client_4_cms_${date}_N${i} -Xloggc:client${i}_${date}_cmsgc.log";
	java -cp "$CLASS_PATH" $JVM_OPTIONS $TEST_OPTIONS -Dtangosol.coherence.cacheconfig=cache.xml azul.test.Main &> client${i}_4_cms_${date}_out.txt &
	sleep 1;
done

TEST_OPTIONS="-Dmode=heap -DoutputDir=client_4_cms_${date}_N4 -Xloggc:client4_${date}_cmsgc.log";
java -cp "$CLASS_PATH" $JVM_OPTIONS $TEST_OPTIONS -Dtangosol.coherence.cacheconfig=cache.xml azul.test.Main &> client4_4_cms_${date}_out.txt

kill `ps ax | awk '{if($5 == "iostat"){print $1}}'`

sleep 20

kill `ps ax | awk '{if($5 == "java"){print $1}}'`

sleep 20

java -Xmx15G -Xms15G -server -XX:NewSize=256m -XX:MaxNewSize=256m -XX:SurvivorRatio=8 -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=20 -verbose:gc -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCDetails -XX:+PrintTenuringDistribution -XX:+PrintGCTimeStamps -XX:+PrintFlagsFinal -cp .:coherence.jar -Dtangosol.coherence.cacheconfig=cache-config-rj.xml com.tangosol.net.DefaultCacheServer >cache_server1_4clients_rj_${date}_cmsgc.log &

sleep 5

java -Xmx15G -Xms15G -server -XX:NewSize=256m -XX:MaxNewSize=256m -XX:SurvivorRatio=8 -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=20 -verbose:gc -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCDetails -XX:+PrintTenuringDistribution -XX:+PrintGCTimeStamps -XX:+PrintFlagsFinal -cp .:coherence.jar -Dtangosol.coherence.cacheconfig=cache-config-rj.xml com.tangosol.net.DefaultCacheServer >cache_server2_4clients_rj_${date}_cmsgc.log &

sleep 5

java -cp $CLASS_PATH $COMMON_OPTIONS -DuseSmartRecord=true -Dtangosol.coherence.cacheconfig=cache.xml dataloader

sleep 5

iostat -c 1 >client_4_cms_rj_cpu_${date}.txt &

for i in `seq 3`; do
	TEST_OPTIONS="-Dmode=heap -DoutputDir=client_4_cms_rj_${date}_N${i} -Xloggc:client${i}_rj_${date}_cmsgc.log";
	java -cp "$CLASS_PATH" $JVM_OPTIONS $TEST_OPTIONS -Dtangosol.coherence.cacheconfig=cache.xml azul.test.Main &> client${i}_4_cms_rj_${date}_out.txt &
	sleep 1;
done

TEST_OPTIONS="-Dmode=heap -DoutputDir=client_4_cms_rj_${date}_N4 -Xloggc:client4_rj_${date}_cmsgc.log";
java -cp "$CLASS_PATH" $JVM_OPTIONS $TEST_OPTIONS -Dtangosol.coherence.cacheconfig=cache.xml azul.test.Main &> client4_4_cms_rj_${date}_out.txt

kill `ps ax | awk '{if($5 == "iostat"){print $1}}'`

sleep 20

kill `ps ax | awk '{if($5 == "java"){print $1}}'`

