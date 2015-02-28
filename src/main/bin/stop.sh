#!/bin/bash
pid=`ps aux | grep "com.xx_dev.apn.socks.SocksServer" | grep java | awk '{print $2}' | sort | head -1`
kill $pid
