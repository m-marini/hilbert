clear all;

file = "../kpis.csv"
popIdx = 1;
techIdx = 2;
foodDeathsTdx = 7
kfIdx = 9;
lambdaSIdx = 12;

data = csvread(file,1,0);

plot(1-exp(-data(:,[techIdx])));
grid on
grid minor
