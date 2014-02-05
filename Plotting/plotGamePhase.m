%gamePhaseVector: [1 1 1 1 2 2 2 2 2 2 2 0 0 0 0]
%color: 'r'

function plotGamePhase(gamePhaseVector, titleAddition, color)
    figure;
    if (strcmp(titleAddition, ''))
        titleAddition = 'no args';
    end;
    
    moveNums = 1:numel(gamePhaseVector);
    plot(moveNums, gamePhaseVector, color);
    xlabel('Move Number');
    ylabel('Game Phase encoded as a number');
    title(strcat('Game Phase Performance (', titleAddition, ')'));
    ylim([min(gamePhaseVector)-.5 max(gamePhaseVector)+.5]);
end