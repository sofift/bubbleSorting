% ============================================================================
% LEVEL CHECKER - Verifica risolubilità del Bubble Sorting Game
% ============================================================================

% Definisce la posizione di una pallina in un determinato stato
% ball(Tube, Position, Color, Step) - pallina nel tubo, posizione, colore, al passo
ball(T, P, C, 0) :- initial_ball(T, P, C).

% Definisce quando un tubo è vuoto a un certo passo
empty_tube(T, S) :- tube(T), step(S), not has_ball(T, S).
has_ball(T, S) :- ball(T, _, _, S).

% Definisce quando un tubo è completo (solo un colore)
complete_tube(T, C, S) :- tube(T), color(C), step(S),
                          ball(T, _, C, S),
                          not mixed_tube(T, S).

% Un tubo è misto se contiene colori diversi
mixed_tube(T, S) :- ball(T, _, C1, S), ball(T, _, C2, S), C1 != C2.

% Conta le palline in un tubo
tube_count(T, Count, S) :- tube(T), step(S),
                           Count = #count{P : ball(T, P, _, S)}.

% Un tubo è pieno quando ha raggiunto la capacità massima
full_tube(T, S) :- tube_count(T, Cap, S), tube_capacity(Cap).

% Definisce i possibili passi del gioco
step(0..max_steps).
#const max_steps = 50.

% ============================================================================
% REGOLE DI MOVIMENTO
% ============================================================================

% Genera possibili mosse: move(FromTube, ToTube, Step)
% Una mossa è possibile se:
% 1. I tubi sono diversi
% 2. Il tubo sorgente non è vuoto
% 3. Il tubo destinazione non è pieno
% 4. Se il tubo destinazione non è vuoto, il colore in cima deve corrispondere
{move(T1, T2, S) : tube(T1), tube(T2), T1 != T2} 1 :- step(S), S > 0.

% Trova il colore in cima a un tubo
top_color(T, C, S) :- ball(T, P, C, S),
                      P = #max{Pos : ball(T, Pos, _, S)}.

% Trova la posizione più alta occupata in un tubo
max_pos(T, P, S) :- tube(T), step(S),
                    P = #max{Pos : ball(T, Pos, _, S)}.

% Trova la prossima posizione libera in un tubo
next_free_pos(T, P, S) :- tube(T), step(S),
                          tube_count(T, Count, S),
                          P = Count.

% Vincoli per le mosse valide
:- move(T1, T2, S), empty_tube(T1, S-1).
:- move(T1, T2, S), full_tube(T2, S-1).
:- move(T1, T2, S), top_color(T1, C1, S-1), top_color(T2, C2, S-1), C1 != C2.

% ============================================================================
% AGGIORNAMENTO DELLO STATO
% ============================================================================

% Se non c'è mossa al passo S, lo stato rimane uguale
ball(T, P, C, S) :- ball(T, P, C, S-1), step(S), S > 0, not changed(T, S).

% Marca i tubi che cambiano a causa di una mossa
changed(T1, S) :- move(T1, T2, S).
changed(T2, S) :- move(T1, T2, S).

% Rimuove la pallina in cima dal tubo sorgente
:- move(T1, T2, S), ball(T1, P, C, S-1), ball(T1, P, C, S),
   P = #max{Pos : ball(T1, Pos, _, S-1)}.

% Aggiunge la pallina al tubo destinazione
ball(T2, P, C, S) :- move(T1, T2, S),
                     top_color(T1, C, S-1),
                     next_free_pos(T2, P, S-1).

% ============================================================================
% CONDIZIONE DI VITTORIA
% ============================================================================

% Un tubo è completo quando contiene solo palline dello stesso colore
complete_tube(T, C, S) :- tube(T), color(C), step(S),
                          ball(T, _, C, S),
                          not mixed_tube(T, S).

% Il gioco è risolto quando tutti i tubi sono completi o vuoti
game_solved(S) :- step(S),
                  tube(T) : (complete_tube(T, _, S) ; empty_tube(T, S)).

% Verifica che esista almeno un passo in cui il gioco è risolto
solvable :- game_solved(S).

% Il livello è risolvibile se esiste una sequenza di mosse che porta alla vittoria
:- not solvable.

% ============================================================================
% OTTIMIZZAZIONI E VINCOLI AGGIUNTIVI
% ============================================================================

% Evita mosse inutili: non spostare una pallina se è già nel posto giusto
% e tutte le palline sotto sono dello stesso colore
:- move(T1, T2, S),
   complete_tube(T1, C, S-1),
   ball_count(T1, Count, S-1),
   Count = #count{C2 : color(C2)}.

% Non fare mosse che annullano la mossa precedente
:- move(T1, T2, S), move(T2, T1, S+1), S < max_steps.

% ============================================================================
% OUTPUT E DEBUG
% ============================================================================

#show solvable/0.
#show game_solved/1.