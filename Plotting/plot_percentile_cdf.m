function M = plot_percentile_cdf(csvname)

% Read data for the percentile of each expert move from a .csv file 
% and create a cdf.

% M is a column vector of percentiles of all expert moves evaluated
M = csvread(csvname);

M = 1.0 - M; % Since we want "proportion in top n percent"

figure('Color',[1.0 1.0 1.0]);

cdfplot(M);

grid off;

% TODO: come up with better title
title('Proportion of expert moves in top n percent', 'FontSize', 20);
xlabel('n', 'FontSize', 16);
ylabel('Proportion', 'FontSize', 16);

% Change x axis to display percents (e.g. 20% instead of 0.2)
x_labels=100*str2num(get(gca,'XTickLabel'));
set(gca, 'XTickLabel', sprintf('%d%%|', x_labels))