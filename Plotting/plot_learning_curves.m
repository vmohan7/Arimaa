function M = plot_learning_curves(csvName, modelName, xStartTick, xEndTick, xStep)

% Read test and train error data from a .csv file and create a learning
% curve plot. 

% Column 1 of M is number of examples in training set
% Column 2 is training set percentile
% Column 3 is test set percentile
% Column 4 is the proportion of expert moves ordered in top 5% (train)
% Column 5 is the proportion of expert moves ordered in top 5% (test)
M = csvread(csvName);

%% Hard-coded desired level of performance
top5PercentPerformance = .95;
linspaceSteps = 2;
levelOfPerformanceY = linspace(top5PercentPerformance, top5PercentPerformance, linspaceSteps);
levelOfPerformanceX = linspace(xStartTick, xEndTick, linspaceSteps);


%% Plot "percentile or expert move" learning curve

figure('Color',[1.0 1.0 1.0]);

% plot(M(:,1), M(:,[2 3]), 'LineWidth', 1.15, 'LineSmoothing','on');
plot(M(:,1), M(:,[2 3]), 'LineWidth', 1);

plotTitle = strcat(modelName, ' learning curve for expert-move percentiles');
title(plotTitle, 'FontSize', 20);
xlabel('Number of training examples', 'FontSize', 16);
set(gca, 'XTick', xStartTick:xStep:xEndTick); % set the x-tick marks according to input parameters
ylabel('Percentile of expert move among all ordered moves', 'FontSize', 16);
myLegend = legend('Training set', 'Testing set');
set(myLegend,'Location','SouthEast');
set(myLegend, 'Fontsize', 16);


%% Plot "proportion in top 5%" learning curve
figure('Color',[1.0 1.0 1.0]);

plot(M(:,1), M(:,[4 5]), levelOfPerformanceX, levelOfPerformanceY, 'LineWidth', 1);

plotTitle = strcat(modelName, ' learning curve for proportion of expert moves in top 5%');
title(plotTitle, 'FontSize', 20);
xlabel('Number of training examples', 'FontSize', 16);
set(gca, 'XTick', xStartTick:xStep:xEndTick); % set the x-tick marks according to input parameters
ylabel('Proportion of expert moves ranked in top 5% of moves', 'FontSize', 16);
myLegend = legend('Training set', 'Testing set', 'Desired performance');
set(myLegend,'Location','SouthEast');
set(myLegend, 'Fontsize', 16);