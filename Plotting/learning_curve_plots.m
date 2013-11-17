% Read test and train error data from a .csv file and create a learning
% curve plot. 

% Column 1 of M is number of examples in training set
% Column 2 is training set percentile
% Column 3 is test set percentile
% Column 4 is the proportion of expert moves ordered in top 5% (train)
% Column 5 is the proportion of expert moves ordered in top 5% (test)
M = csvread('learn.csv');

%% Plot "percentile or expert move" learning curve

figure('Color',[1.0 1.0 1.0]);

% plot(M(:,1), M(:,[2 3]), 'LineWidth', 1.15, 'LineSmoothing','on');
plot(M(:,1), M(:,[2 3]), 'LineWidth', 1);

title('Naive Bayes learning curve for percentile of expert move', 'FontSize', 20);
xlabel('Number of training examples', 'FontSize', 16);
ylabel('Percentile of expert move among all ordered moves', 'FontSize', 16);
myLegend = legend('Training set', 'Testing set');
set(myLegend,'Location','SouthEast');
set(myLegend, 'Fontsize', 16);


%% Plot "proportion in top 5%" learning curve
figure('Color',[1.0 1.0 1.0]);

plot(M(:,1), M(:,[4 5]), 'LineWidth', 1);

title('Naive Bayes learning curve for proportion of expert moves in top 5%', 'FontSize', 20);
xlabel('Number of training examples', 'FontSize', 16);
ylabel('Proportion of expert moves ranked in top 5% of moves', 'FontSize', 16);
myLegend = legend('Training set', 'Testing set');
set(myLegend,'Location','SouthEast');
set(myLegend, 'Fontsize', 16);