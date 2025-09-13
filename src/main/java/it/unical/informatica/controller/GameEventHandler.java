package it.unical.informatica.controller;

import it.unical.informatica.model.GameLevel;
import it.unical.mat.embasp.languages.IllegalAnnotationException;
import it.unical.mat.embasp.languages.ObjectNotValidException;


public class GameEventHandler {


    @FunctionalInterface
    public interface NewGameHandler {
        void onNewGame(GameLevel level, int levelNumber);
    }


    @FunctionalInterface
    public interface TubeClickHandler {
        void onTubeClicked(int tubeId);
    }


    @FunctionalInterface
    public interface ActionHandler {
        void onAction() throws ObjectNotValidException, IllegalAnnotationException;
    }


    @FunctionalInterface
    public interface AnimationCompleteHandler {
        void onAnimationComplete();
    }


    @FunctionalInterface
    public interface LevelSelectionHandler {
        void onLevelSelected(GameLevel level, int levelNumber);
    }


    @FunctionalInterface
    public interface MoveHandler {
        void onMove(int fromTubeId, int toTubeId);
    }

    public static class EventValidator {


        public static boolean isValidTubeId(int tubeId, int maxTubes) {
            return tubeId >= 0 && tubeId < maxTubes;
        }


        public static boolean isValidLevelNumber(int levelNumber) {
            return levelNumber >= 1 && levelNumber <= 5;
        }


        public static boolean isValidMove(int fromTubeId, int toTubeId, int maxTubes) {
            return isValidTubeId(fromTubeId, maxTubes) &&
                    isValidTubeId(toTubeId, maxTubes) &&
                    fromTubeId != toTubeId;
        }
    }
}