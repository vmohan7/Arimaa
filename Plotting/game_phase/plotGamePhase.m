%gamePhaseVector: [1 1 1 1 2 2 2 2 2 2 2 0 0 0 0]
%color: 'r'

function plotGamePhase(gamePhaseVector, titleAddition, color, subplotIndex)
    AXIS_LABEL_SIZE = 16;
    TITLE_SIZE = 18;
    
    % Amount by which to shift plots downward to accomodate for super title.
    Y_DOWNSHIFT = -0.02; 

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
        % Plot 4 figures on a single plot
        handle = subplot(2, 2, subplotIndex);
        plot(moveNums, gamePhaseVector, color, 'LineSmoothing','on');
        ylim([min(gamePhaseVector)-.5 max(gamePhaseVector)+.5]);
    end
    
    xlabel('Move Number', 'FontSize', AXIS_LABEL_SIZE);
    yLabelStrings = {'Beginning', 'Middle', 'End'};
    set(gca, 'YTickLabel', yLabelStrings, 'YTick', 0:numel(yLabelStrings)-1);
    set(gca, 'FontSize', AXIS_LABEL_SIZE);    
    
    title(strcat('Game ID: ', titleAddition), 'FontSize', TITLE_SIZE);
    set(gcf,'color','w');
    ct = camtarget;
    camtarget([ct(1)+0.001*ct(1) ct(2)+0.001 ct(3)]);
    subplotPos = get(handle,'position');
    set(handle, 'position', subplotPos + [0 Y_DOWNSHIFT 0 Y_DOWNSHIFT]);

    % ylabel('Game Phase (0 = beginning, 1 = middle, 2 = end)', 'FontSize', AXIS_LABEL_SIZE);
    % title(strcat('Game Phase Performance (Game ID: ', titleAddition, ')'), 'FontSize', TITLE_SIZE);
end