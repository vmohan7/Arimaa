% fid = fopen('kmeans_3.csv');

fid = fopen('kmeans_heuristic.csv');

% ignore the first this many columns in the csv (perhaps if the first
% column is the game id)
columnOffset = 1;

tline = fgetl(fid);
colors = ['k', 'b', 'g', 'r'];
color = 1;

while ischar(tline)
    vec = str2num(tline);
    titleAddition = '';
    
    if (columnOffset ~= 0)
        titleAddition = num2str(vec(1:columnOffset));
    end;
    
    plotGamePhase(vec(1+columnOffset:end), titleAddition, colors(color));
    tline = fgetl(fid);
    color = mod(color, numel(colors)) + 1;
end

fclose(fid);
disp('Type close all to programatically close all figures');