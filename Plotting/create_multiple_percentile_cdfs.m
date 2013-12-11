% Script to create multiple cdf plots. (Different than the function
% 'plot_percentile_cdf' which creates just 1 plot)

% Read data for the percentile of each expert move from .csv files 
% and create cdf plots.

% TODO: change these parameters 
csvName1 = 'arimaa_l2_500.csv';
csvName2 = 'arimaa_svm_500.csv';
modelName1 = 'L1 Logistic Regression';
modelName2 = ' SVM';
modelColors = ['b' 'r'];

% M is a column vector of percentiles of all expert moves evaluated
M1 = csvread(csvName1);
M2 = csvread(csvName2);
figure('Color',[1.0 1.0 1.0]);

% COMMENTED OUT -- decided to use ecdf instead of cdfplot because ecdf
% returns X,Y data from which we can calculate specific proportions. 
% M = 1.0 - M; % Since we want "proportion in top n percent"
% cdfplot(M);

% Get X,Y data and plot cdf for all models
[X1 Y1] = ecdf(M1);
plot(X1,Y1,modelColors(1));
hold on;
grid off;
[X2 Y2] = ecdf(M2);
plot(X2,Y2,modelColors(2));

% X = [X1 X2]; % generates error due to mismatched dimensions
% Y = [Y1 Y2];
% 
% % Draw dotted lines showing Y-value at X=10% for model 1
% % Draw vertical dotted line
% xVal = 0.10;
% for i=1:size(X,2)
%     vertLineX=[xVal,xVal];
%     yData = Y(:,i);
%     xData = X(:,i);
%     yMax = yData(min(find(xData>xVal)))
%     vertLineY=[0,yMax];
%     plot(vertLineX,vertLineY,strcat('--', modelColors(i)));
% 
%     % Draw horizontal dotted line
%     horizLineX=[0 xVal];
%     horizLineY=[yMax yMax];
%     plot(horizLineX, horizLineY, strcat('--', modelColors(i)));
% 
%     % Draw point where lines intersect
%     plot(xVal, yMax, strcat('.', modelColors(i)));
% end

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Draw dotted lines showing Y-value at X=10%
% Draw vertical dotted line
xVal = 0.10;
vertLineX=[xVal,xVal];
yMax = Y1(min(find(X1>xVal)))
vertLineY=[0,yMax];
plot(vertLineX,vertLineY,'--k');

% Draw horizontal dotted line
horizLineX=[0 xVal];
horizLineY=[yMax yMax];
plot(horizLineX, horizLineY, '--k');

% Draw point where lines intersect
plot(xVal, yMax, '.k');

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

title('Proportion of expert moves evaluated at top of ranking', 'FontSize', 20);
xlabel('Top percent of move ranking', 'FontSize', 16);
ylabel('Proportion of expert moves', 'FontSize', 16);

% Configure legend
myLegend = legend(modelName1, modelName2);
set(myLegend,'Location','SouthEast');
set(myLegend, 'Fontsize', 16);

% Change x axis to display percents (e.g. 20% instead of 0.2)
x_labels=100*str2num(get(gca,'XTickLabel'));
set(gca, 'XTickLabel', sprintf('%d%%|', x_labels))