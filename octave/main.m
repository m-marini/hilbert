clear all;

file = "../kpis.csv"
popIdx = 1;
techIdx = 2;
deathsOIdx = 3;
deathsSIdx = 7;
deathsHIdx = 19;
birthsIdx = 8;
kfIdx = 9;
lambdaSIdx = 12;
keIdx = 18;
khIdx = 20;

data = csvread(file,1,0);

rows = 2;
cols = 2;

subplot(rows, cols, 1);
plot(data(:,[popIdx]));
grid on;
grid minor;
title "Population";

subplot(rows, cols, 2);
plot(data(:,[kfIdx, khIdx]));
grid on;
grid minor;
legend("Kf", "Kh");
title "K*";

subplot(rows, cols, 3);
plot(data(:,[birthsIdx]));
grid on;
grid minor;
title "Births";

subplot(rows, cols, 4);
plot(data(:,[deathsOIdx, deathsSIdx, deathsHIdx]));
grid on;
grid minor;
legend("Overpopulation", "Starvation", "Helath");
title "Deaths";


