% Script to create multiple learning curves. (Different than the function
% 'plot_learning_curves' which creates just 1 plot) 
% Read test and train error data from .csv files and create learning
% curve plots. 

% TODO: change these parameters 
csvName1 = 'learning_curves/L2_learning.csv';
csvName2 = 'learning_curves/SVM_learning.csv';
modelName1 = 'Logistic Regression';
modelName2 = ' SVM';
xStartTick = 100;
xEndTick = 500;
xStep = 50;
yMin = 85;
yMax = 95;
xAxisOffset = 30;
color1 = [128  142  196] ./ 256;
color2 = [115 251 118] ./ 256;
colorScale = 0.5; % number by which to multiply color to generate lighter color


% Column 1 of M1|M2 is number of examples in training set
% Column 2 is training set percentile
% Column 3 is test set percentile
% Column 4 is the proportion of expert moves ordered in top 5% (train)
% Column 5 is the proportion of expert moves ordered in top 5% (test)
M1 = csvread(csvName1);
M2 = csvread(csvName2);

% Set figure color and position
figure('Color',[1.0 1.0 1.0]);
                           

hold on;
box on;

plot(M1(:,1), M1(:,2), '-r');%, 'Color', color1 .* colorScale);
plot(M1(:,1), M1(:,3), '--r');%, 'Color', color1);
plot(M2(:,1), M2(:,2), '-b');%, 'Color', color2 .* colorScale);
plot(M2(:,1), M2(:,3), '--b');%, 'Color', color2);



% Draw dashed line showing convergence -- assuming that both learning
% curves converge at around the same value! 
convVal=mean([M1(end,2) M1(end,3) M2(end,2) M2(end,3)]); % approximation for convergence percentile value
convLineX = [min(M1(:,1)) max(M1(:,1))];
convLineY = [convVal convVal];
plot(convLineX,convLineY,'--k');

% Titles and label
plotTitle = strcat(modelName1, ' and ', modelName2, ' learning curve for expert-move percentiles');
title(plotTitle, 'FontSize', 20);
xlabel('Number of training examples', 'FontSize', 16);
ylabel('Percentile of expert move among all ordered moves', 'FontSize', 16);

% Configure axes ticks and range
set(gca, 'XTick', xStartTick:xStep:xEndTick); % set the x-tick marks according to input parameters
axis([xStartTick-xAxisOffset xEndTick+xAxisOffset yMin yMax]) % gives x min/max and y min/max for axis scaling: [xmin xmax ymin ymax]

% Configure legend
myLegend = legend(strcat(modelName1, ' training set'), ...
                    strcat(modelName1, ' testing set'), ...
                    strcat(modelName2, ' training set'), ...
                    strcat(modelName2, ' testing set'), ...
                    'Approximate convergence');
set(myLegend,'Location','SouthEast');
set(myLegend, 'Fontsize', 16);

hold off;