% plot_percentile_histogram('HistogramData/SVM_500.csv');
% plot_percentile_histogram('HistogramData/L1_500.csv');
% plot_percentile_histogram('HistogramData/L2_500.csv');

ymax = 0.3; % The max value of the y axis for the histogram
plot_percentile_histogram('../Testing/nb_hists.csv', ymax);

% plot_learning_curves('learn.csv');
plot_percentile_cdf('../Testing/nb_hists.csv');
