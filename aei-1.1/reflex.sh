#!/bin/bash

J_PATH="../ArimaaBot"
MATH_PATH="../ArimaaBot/commons-math3-3.0.jar"
java -Xmx1000M -classpath $J_PATH/bin:$J_PATH/:$MATH_PATH ArimaaEngineInterface.ArimaaEngineInterface
