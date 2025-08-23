package it.unical.informatica.controller;

import it.unical.informatica.model.GameLevel;
import it.unical.mat.embasp.languages.IllegalAnnotationException;
import it.unical.mat.embasp.languages.ObjectNotValidException;

/**
 * Interfacce funzionali per gestire gli eventi del gioco
 */
public class GameEventHandler {

    /**
     * Handler per la selezione di una nuova partita
     */
    @FunctionalInterface
    public interface NewGameHandler {
        void onNewGame(GameLevel level, int levelNumber);
    }

    /**
     * Handler per il click su un tubo
     */
    @FunctionalInterface
    public interface TubeClickHandler {
        void onTubeClicked(int tubeId);
    }

    /**
     * Handler per azioni semplici (restart, menu, hint, etc.)
     */
    @FunctionalInterface
    public interface ActionHandler {
        void onAction() throws ObjectNotValidException, IllegalAnnotationException;
    }

    /**
     * Handler per la fine dell'animazione
     */
    @FunctionalInterface
    public interface AnimationCompleteHandler {
        void onAnimationComplete();
    }

    /**
     * Handler per la selezione di livello
     */
    @FunctionalInterface
    public interface LevelSelectionHandler {
        void onLevelSelected(GameLevel level, int levelNumber);
    }

    /**
     * Handler per le azioni di movimento
     */
    @FunctionalInterface
    public interface MoveHandler {
        void onMove(int fromTubeId, int toTubeId);
    }

    /**
     * Classe di utilità per validare gli eventi
     */
    public static class EventValidator {

        /**
         * Valida un ID di tubo
         */
        public static boolean isValidTubeId(int tubeId, int maxTubes) {
            return tubeId >= 0 && tubeId < maxTubes;
        }

        /**
         * Valida un numero di livello
         */
        public static boolean isValidLevelNumber(int levelNumber) {
            return levelNumber >= 1 && levelNumber <= 5;
        }

        /**
         * Valida una mossa
         */
        public static boolean isValidMove(int fromTubeId, int toTubeId, int maxTubes) {
            return isValidTubeId(fromTubeId, maxTubes) &&
                    isValidTubeId(toTubeId, maxTubes) &&
                    fromTubeId != toTubeId;
        }
    }
}