% fid = fopen('kmeans_heuristic_reduced_ternary.csv');

fid = fopen('kmeans_heuristic_ternary.csv');
SUPER_TITLE_SIZE = 24;

% All plots will be subplots on one figure
figure;
hold on;

% ignore the first this many columns in the csv (perhaps if the first
% column is the game id)
columnOffset = 1;
tline = fgetl(fid);
colors = ['k', 'b', 'g', 'r'];
subplotIndex = 0;

while ischar(tline)
    
    % Create new figure of 4 plots if current figure is full
    if (subplotIndex == 0 || subplotIndex > 4)
        subplotIndex = 1;
        hold off; figure; hold on;
        % "Super title" for all subplots
        annotation('textbox', [0 0.9 1 0.1], ...
            'String', 'Game Phase Assignments', ...
            'EdgeColor', 'none', ...
            'HorizontalAlignment', 'center', ...
            'FontSize', SUPER_TITLE_SIZE);
    end
    
    vec = str2num(tline);
    titleAddition = '';
    
    if (columnOffset ~= 0)
        titleAddition = num2str(vec(1:columnOffset));
    end;

    plotGamePhase(vec(1+columnOffset:end), titleAddition, colors(color), subplotIndex);
    tline = fgetl(fid);
    color = mod(color, numel(colors)) + 1;
    subplotIndex = subplotIndex + 1;
end


fclose(fid);
disp('Type close all to programatically close all figures');