% fid = fopen('kmeans_3_probabilities.csv');

fid = fopen('kmeans_heuristic_probabilities.csv');

% ignore the first this many columns in the csv (perhaps if the first
% column is the game id)
columnOffset = 1;

tline = fgetl(fid);
numPhases = 3;


while ischar(tline)
    
    probs = str2num(tline);
    
    for i=2:numPhases
       tline = fgetl(fid);
       probs = [probs ; str2num(tline)];
    end
    
    titleAddition = '';
    
    if (columnOffset ~= 0)
        titleAddition = num2str(vec(1:columnOffset));
    end;
    
    plotGamePhase(probs(:,(1+columnOffset:end)), titleAddition, '');
    
    tline = fgetl(fid);
end

fclose(fid);
disp('Type close all to programatically close all figures');