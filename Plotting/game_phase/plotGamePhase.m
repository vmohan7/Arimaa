%gamePhaseVector: [1 1 1 1 2 2 2 2 2 2 2 0 0 0 0]
%color: 'r'

function plotGamePhase(gamePhaseVector, titleAddition, color)
    figure;
    if (strcmp(titleAddition, ''))
        titleAddition = 'no args';
    end;
    
    moveNums = 1:size(gamePhaseVector, 2);
    
    % If gamePhaseVector is a matrix, use different colors and a legend
    if (size(gamePhaseVector, 1) > 1)
        plot(moveNums, gamePhaseVector);
        ylim([0 1]);
        % ylim([min(min(gamePhaseVector))-.5 max(max(gamePhaseVector))+.5]);
        legend('Beginning game', 'Middle game', 'End game');
    else
        plot(moveNums, gamePhaseVector, color);
        ylim([min(gamePhaseVector)-.5 max(gamePhaseVector)+.5]);
    end
    
    xlabel('Move Number');
    ylabel('Game Phase encoded as a number');
    title(strcat('Game Phase Performance (', titleAddition, ')'));
end