% Expects the format of the csv to look like this: 
% <game-id (optional)>,<prob-beginning (move 1)>,<prob-middle (move 1)>, \
% <prob-end (move 1)>,<prob-beginning (move 2)>,...

% fid = fopen('kmeans_3_probabilities.csv');

fid = fopen('kmeans_heuristic_probabilities.csv');

% ignore the first this many columns in the csv (perhaps if the first
% column is the game id)
columnOffset = 1;

tline = fgetl(fid);
numPhases = 3;


while ischar(tline)
    
    probsVector = str2num(tline);
    
    titleAddition = '';
    if (columnOffset ~= 0)
        titleAddition = num2str(probsVector(1:columnOffset));
    end;
    
    probsVector = probsVector(1+columnOffset:end);
    
    probsLength = length(probsVector);
    assert(mod(probsLength, numPhases) == 0);

    % Construct matrix from vector for easier plotting
    probsMatrix = zeros(numPhases, probsLength / 3);    
    for i=1:probsLength
       column = 1 + floor((i-1) / numPhases);
       row = 1 + mod(i-1, numPhases);
       probsMatrix(row, column) = probsVector(i);
    end
    
    plotGamePhase(probsMatrix, titleAddition, '');
    
    tline = fgetl(fid);
end

fclose(fid);
disp('Type close all to programatically close all figures');