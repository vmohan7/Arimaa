function M = plot_percentile_histogram(csvname)

% Read data for the percentile of each expert move from a .csv file 
% and create a histogram.

% Column 1 of M is the percentiles of all expert moves evaluated
%M = csvread('percentiles.csv');
M = [1,2,3,4,5,6,7,8,7,6,5,6,7,4,5,1];

%% Plot "percentile or expert move" learning curve

figure('Color',[1.0 1.0 1.0]);

% plot(M(:,1), M(:,[2 3]), 'LineWidth', 1.15, 'LineSmoothing','on');
hist(M);

title('Percentile rankings of expert moves', 'FontSize', 20);
xlabel('Percentile of expert move among all ordered moves', 'FontSize', 16);
ylabel('Count', 'FontSize', 16);
