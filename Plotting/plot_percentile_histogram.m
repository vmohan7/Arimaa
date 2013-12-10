function M = plot_percentile_histogram(csvname)

% Read data for the percentile of each expert move from a .csv file 
% and create a histogram.

% M is a column vector of percentiles of all expert moves evaluated
M = csvread(csvname);

%% Plot "percentile or expert move" learning curve

figure('Color',[1.0 1.0 1.0]);
nBins = 100;

[n,x] = hist(M, nBins);
bar(x, n./sum(n),1,'hist'); % normalize y axis to give proportions instead of counts
axis([0 1 0 0.3]) % gives x min/max and y min/max for axis scaling: [xmin xmax ymin ymax]


title('Percentile rankings of expert moves', 'FontSize', 20);
xlabel('Percentile of expert move among all ordered moves', 'FontSize', 16);
ylabel('Count', 'FontSize', 16);
