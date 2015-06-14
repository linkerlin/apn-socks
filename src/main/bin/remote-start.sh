#!/bin/bash
pid=`ps aux | grep "com.xx_dev.apn.socks.remote.SocksServer" | grep java | awk '{print $2}' | sort | head -1`

if [ -n "$pid" ]; then
    echo "Stop old apn-socks remote: $pid"
    kill $pid
else
    echo "No old apn-socks remote"
fi

sleep 2

for jar in `ls lib/*.jar`
do
    jars="$jars:""$jar"
done
java $JAVA_OPTS -cp $jars com.xx_dev.apn.socks.remote.SocksServer