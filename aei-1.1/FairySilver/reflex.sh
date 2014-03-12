#!/bin/bash

#-Xmx1000M
J_PATH="../../ArimaaBot"
MATH_PATH="../../ArimaaBot/commons-math3-3.0.jar"
java -classpath $J_PATH/bin:$J_PATH/:$MATH_PATH ArimaaEngineInterface.ArimaaEngineInterface
