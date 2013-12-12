
csvName = 'nb_3dhist.csv';
moveBucketSize = 10; % granularity of buckets for the moves
percentBucketSize = .1; % granularity of buckets for the percentiles
numMoveBuckets = 70 / moveBucketSize;
numPercentBuckets = 1 / percentBucketSize;

M = csvread(csvName);

moveNums = M(:,1);

percentiles = M(:,2);
percentiles(percentiles == 1.0) = .99999999; % replace 1.0 with .99999999

moveNumFreqs = 1:max(moveNums);
for i=1:length(moveNumFreqs)
   moveNumFreqs(i) = sum(moveNums == i);    
end

% Set figure color and position
figure('Color',[1.0 1.0 1.0]);


moveNumsBinEdges = 1:moveBucketSize:max(moveNums);
percentileBinEdges = 0:percentBucketSize:1;

% Rows represent move number
% Columns represent percentile
countsMatrix = hist3([moveNums percentiles], 'Edges', {moveNumsBinEdges, percentileBinEdges});

for move = 1:length(moveNumsBinEdges)
    countsMatrix(move, :) = countsMatrix(move, :) / sum(countsMatrix(move, :));
end

% Print only the first numMoveBuckets buckets. (exclude outliers where data is sparse.) 
% Do not include the last percentile bucket (which is effectively all 0s).
bar3(transpose(countsMatrix(1:numMoveBuckets,1:length(percentileBinEdges)-1)));

% Set angle of axes labels, set titles, and set font sizes
xAngle = 25;
yAngle = -23;
titleFontSize = 24;
axesFontSize = 18;
tickFontSize = 14;

title('Histogram of expert move ranking', 'FontSize', titleFontSize);
xlabel('Move number within a game', 'FontSize', axesFontSize, 'Rotation', xAngle);
ylabel(sprintf('Percentile (discretized) of expert \nmove among all ordered moves'), 'FontSize', axesFontSize, 'Rotation', yAngle);
zlabel(sprintf('Proportion of expert moves, \nnormalized by move number'), 'FontSize', axesFontSize);

% Change y axis to display percents (e.g. 20% instead of 0.2), shift to center of bucket
y_labels = (numPercentBuckets) * str2num(get(gca,'YTickLabel')) - numPercentBuckets / 2;

% Change x axis to display accurate move buckets
x_labels = moveBucketSize * str2num(get(gca,'XTickLabel'));

set(gca, 'XTickLabel', sprintf('%d|', x_labels), ...
         'YTickLabel', sprintf('%d%%|', y_labels), ...
         'FontSize', tickFontSize);

colormap('Summer');

% set camera view (azimuthal angle, elevation)
az = 135;
el = 25;
view([az,el])

axis tight

