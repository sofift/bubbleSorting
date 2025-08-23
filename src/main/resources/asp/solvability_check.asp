% ==========================================
% BUBBLE SORTING GAME - SOLVABILITY CHECK
% File: solvability_check.asp
%
% Questo file contiene le regole ASP per verificare
% se una configurazione iniziale del gioco è risolvibile
% ==========================================

% ==========================================
% DEFINIZIONI BASE
% ==========================================

% Definisce le posizioni valide all'interno di un tubo (0 = fondo, capacità-1 = cima)
position(0..Cap-1) :- tube_capacity(Cap).

% Definisce gli step temporali per la ricerca della soluzione (max 50 mosse)
step(0..50).

% ==========================================
% STATO INIZIALE
% ==========================================

% Lo stato iniziale è definito dai fatti initial_ball(Tubo, Posizione, Colore)
% che vengono generati automaticamente da Java

% Determina l'altezza di ogni tubo nello stato iniziale
initial_height(Tube, Height) :-
    tube(Tube),
    Height = #max{Pos+1 : initial_ball(Tube, Pos, _); 0}.

% ==========================================
% REGOLE DI MOVIMENTO
% ==========================================

% Un movimento è possibile se:
% 1. Il tubo sorgente non è vuoto
% 2. Il tubo destinazione ha spazio
% 3. La pallina in cima al tubo sorgente può essere posizionata su quello destinazione
possible_move(FromTube, ToTube, Step) :-
    step(Step),
    tube(FromTube),
    tube(ToTube),
    FromTube != ToTube,
    height(FromTube, Step, HeightFrom),
    height(ToTube, Step, HeightTo),
    HeightFrom > 0,                                    % Tubo sorgente non vuoto
    tube_capacity(Cap),
    HeightTo < Cap,                                    % Tubo destinazione ha spazio
    ball_at_top(FromTube, Step, Color),               % Colore della pallina da spostare
    can_place_color(ToTube, Step, Color).             % Può essere posizionata

% Determina il colore della pallina in cima a un tubo
ball_at_top(Tube, Step, Color) :-
    height(Tube, Step, Height),
    Height > 0,
    ball(Tube, Height-1, Step, Color).

% Verifica se un colore può essere posizionato in un tubo
% Può essere posizionato se il tubo è vuoto o se la pallina in cima è dello stesso colore
can_place_color(Tube, Step, Color) :-
    height(Tube, Step, 0).                            % Tubo vuoto

can_place_color(Tube, Step, Color) :-
    height(Tube, Step, Height),
    Height > 0,
    ball_at_top(Tube, Step, Color).                   % Stesso colore della pallina in cima

% ==========================================
% GENERAZIONE DELLA SEQUENZA DI MOSSE
% ==========================================

% Genera esattamente una mossa per ogni step (se possibile)
0 { move(FromTube, ToTube, Step) : possible_move(FromTube, ToTube, Step) } 1 :- step(Step), Step > 0.

% ==========================================
% PROPAGAZIONE DELLO STATO
% ==========================================

% Stato iniziale (Step 0)
ball(Tube, Pos, 0, Color) :-
    initial_ball(Tube, Pos, Color).

height(Tube, 0, Height) :-
    initial_height(Tube, Height).

% Propagazione dello stato per gli step successivi
% Se non c'è movimento che coinvolge un tubo, mantiene lo stesso stato
ball(Tube, Pos, Step, Color) :-
    ball(Tube, Pos, Step-1, Color),
    step(Step),
    Step > 0,
    not affected_tube(Tube, Step).

height(Tube, Step, Height) :-
    height(Tube, Step-1, Height),
    step(Step),
    Step > 0,
    not affected_tube(Tube, Step).

% Un tubo è "affected" se è coinvolto in un movimento
affected_tube(Tube, Step) :-
    move(FromTube, ToTube, Step),
    (Tube = FromTube ; Tube = ToTube).

% Aggiorna lo stato dopo un movimento
% Tubo sorgente: rimuove la pallina dalla cima
ball(Tube, Pos, Step, Color) :-
    move(Tube, _, Step),
    ball(Tube, Pos, Step-1, Color),
    height(Tube, Step-1, Height),
    Pos < Height - 1.

height(Tube, Step, Height-1) :-
    move(Tube, _, Step),
    height(Tube, Step-1, Height),
    Height > 0.

% Tubo destinazione: aggiunge la pallina in cima
ball(Tube, Pos, Step, Color) :-
    move(FromTube, Tube, Step),
    ball(Tube, Pos, Step-1, Color).

ball(Tube, Height, Step, Color) :-
    move(FromTube, Tube, Step),
    height(Tube, Step-1, Height),
    ball_at_top(FromTube, Step-1, Color).

height(Tube, Step, Height+1) :-
    move(_, Tube, Step),
    height(Tube, Step-1, Height).

% ==========================================
% CONDIZIONI DI VITTORIA
% ==========================================

% Un tubo è completato se:
% 1. È vuoto, oppure
% 2. È pieno e contiene solo palline dello stesso colore
tube_completed(Tube, Step) :-
    height(Tube, Step, 0).

tube_completed(Tube, Step) :-
    height(Tube, Step, Cap),
    tube_capacity(Cap),
    ball(Tube, 0, Step, Color),
    all_same_color(Tube, Step, Color).

% Verifica che tutte le palline in un tubo siano dello stesso colore
all_same_color(Tube, Step, Color) :-
    tube(Tube),
    step(Step),
    color(Color),
    height(Tube, Step, Height),
    Height > 0,
    #count{Pos : ball(Tube, Pos, Step, Color)} = Height.

% Il gioco è vinto quando tutti i tubi sono completati
game_won(Step) :-
    step(Step),
    #count{Tube : tube(Tube), not tube_completed(Tube, Step)} = 0.

% ==========================================
% OTTIMIZZAZIONE
% ==========================================

% Preferisce sequenze di mosse più corte
#minimize { 1,Step : move(_,_,Step) }.

% Evita mosse inutili (spostare una pallina e poi riportarla indietro)
:- move(A, B, Step), move(B, A, Step+1), Step > 0.

% ==========================================
% VERIFICA DI RISOLVIBILITÀ
% ==========================================

% Il livello è risolvibile se esiste almeno uno step in cui il gioco è vinto
solvable :- game_won(Step).

% Per il check di risolvibilità, cerchiamo solo di verificare l'esistenza di una soluzione
#show solvable/0.

% Per il debugging, mostra anche informazioni aggiuntive
#show game_won/1.
#show move/3.