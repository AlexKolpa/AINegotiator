md negotiator\group1
del /Q negotiator\group1\*
copy bin\negotiator\group1\* negotiator\group1\

java -jar negosimulator.jar
