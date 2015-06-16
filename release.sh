#!/bin/bash

mvn release:prepare -Darguments="-Dmaven.test.skip=true"
mvn release:perform -Darguments="-Dmaven.test.skip=true"

