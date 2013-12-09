function M = plot_percentile_histogram(csvname)

% Read data for the percentile of each expert move from a .csv file 
% and create a histogram.

% M is a column vector of percentiles of all expert moves evaluated
M = csvread(csvname);

%% Plot "percentile or expert move" learning curve

figure('Color',[1.0 1.0 1.0]);
nBins = 25;
% xValues = 0.00:.04:1.00;

hist(M, nBins);

title('Percentile rankings of expert moves', 'FontSize', 20);
xlabel('Percentile of expert move among all ordered moves', 'FontSize', 16);
ylabel('Count', 'FontSize', 16);
