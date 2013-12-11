function M = plot_percentile_cdf(csvname)

% Read data for the percentile of each expert move from a .csv file 
% and create a cdf.

% M is a column vector of percentiles of all expert moves evaluated
M = csvread(csvname);
figure('Color',[1.0 1.0 1.0]);

% COMMENTED OUT -- decided to use ecdf instead of cdfplot because ecdf
% returns X,Y data from which we can calculate specific proportions. 
% M = 1.0 - M; % Since we want "proportion in top n percent"
% cdfplot(M);

% Get X,Y data and plot cdf
[X Y] = ecdf(M);
plot(X,Y);

hold on;
grid off;

% Draw dotted lines showing Y-value at X=10%

% Draw vertical dotted line
xVal = 0.10;
vertLineX=[xVal,xVal];
yMax = Y(min(find(X>xVal)))
vertLineY=[0,yMax];
plot(vertLineX,vertLineY,'--k');

% Draw horizontal dotted line
horizLineX=[0 xVal];
horizLineY=[yMax yMax];
plot(horizLineX, horizLineY, '--k');

% Draw point where lines intersect
plot(xVal, yMax, '.k');

% TODO: come up with better title
title('Proportion of expert moves in top n percent', 'FontSize', 20);
xlabel('n', 'FontSize', 16);
ylabel('Proportion', 'FontSize', 16);

% Change x axis to display percents (e.g. 20% instead of 0.2)
x_labels=100*str2num(get(gca,'XTickLabel'));
set(gca, 'XTickLabel', sprintf('%d%%|', x_labels))