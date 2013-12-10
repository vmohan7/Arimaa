function h = plot_percentile_histogram(csvname, ymax)

% Read data for the percentile of each expert move from a .csv file 
% and create a histogram.

% M is a column vector of percentiles of all expert moves evaluated
M = csvread(csvname);

figure('Color',[1.0 1.0 1.0]);
nBins = 100;

[n,x] = hist(M, nBins);
bar(x, n./sum(n),1,'hist'); % normalize y axis to give proportions instead of counts
h = gca; % get a handle to the current axis
axis([0 1 0 ymax]) % gives x min/max and y min/max for axis scaling: [xmin xmax ymin ymax]


title('Percentile rankings of expert moves', 'FontSize', 20);
xlabel('Percentile of expert move among all ordered moves', 'FontSize', 16);
ylabel('Proportion', 'FontSize', 16);
