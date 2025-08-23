% ============================================================================
% GAME SOLVER - Risolve automaticamente il Bubble Sorting Game
% ============================================================================

% Definisce i passi temporali del gioco
#const max_steps = 20.
step(0..max_steps).

% Stato iniziale: le palline partono dalla configurazione iniziale
ball(T, P, C, 0) :- initial_ball(T, P, C).

% ============================================================================
% GENERAZIONE DELLE MOSSE
% ============================================================================

% Genera una mossa per ogni passo (escludendo il passo 0)
{ move(T1, T2, S) : tube(T1), tube(T2), T1 != T2 } = 1 :- step(S), S > 0, S <= max_steps.

% ============================================================================
% DEFINIZIONI AUSILIARIE
% ============================================================================

% Trova la pallina in cima a un tubo
top_ball(T, P, C, S) :- ball(T, P, C, S),
                        P = #max{Pos : ball(T, Pos, _, S)}.

% Conta le palline in un tubo
ball_count(T, Count, S) :- tube(T), step(S),
                           Count = #count{P : ball(T, P, _, S)}.

% Un tubo è vuoto
empty_tube(T, S) :- tube(T), step(S), ball_count(T, 0, S).

% Un tubo è pieno
full_tube(T, S) :- ball_count(T, Cap, S), tube_capacity(Cap).

% Un tubo è completo (tutte le palline dello stesso colore)
complete_tube(T, S) :- tube(T), step(S), ball_count(T, Count, S), Count > 0,
                       #count{C : ball(T, _, C, S)} = 1.

% ============================================================================
% VINCOLI DELLE MOSSE
% ============================================================================

% Non si può spostare da un tubo vuoto
:- move(T1, T2, S), empty_tube(T1, S-1).

% Non si può spostare in un tubo pieno
:- move(T1, T2, S), full_tube(T2, S-1).

% Le palline devono avere lo stesso colore (se il tubo destinazione non è vuoto)
:- move(T1, T2, S),
   top_ball(T1, _, C1, S-1),
   top_ball(T2, _, C2, S-1),
   C1 != C2.

% ============================================================================
% AGGIORNAMENTO DELLO STATO
% ============================================================================

% Se non c'è mossa che coinvolge un tubo, il suo stato rimane uguale
ball(T, P, C, S) :- ball(T, P, C, S-1), step(S), S > 0,
                    not moved_from(T, S), not moved_to(T, S).

% Marca i tubi coinvolti nelle mosse
moved_from(T1, S) :- move(T1, T2, S).
moved_to(T2, S) :- move(T1, T2, S).

% Rimuovi la pallina in cima dal tubo sorgente
removed_ball(T1, P, C, S) :- move(T1, T2, S), top_ball(T1, P, C, S-1).

% Le altre palline del tubo sorgente rimangono
ball(T1, P, C, S) :- ball(T1, P, C, S-1), move(T1, T2, S),
                     not removed_ball(T1, P, C, S).

% Aggiungi la pallina al tubo destinazione
ball(T2, P, C, S) :- move(T1, T2, S),
                     removed_ball(T1, _, C, S),
                     ball_count(T2, P, S-1).

% Le altre palline del tubo destinazione rimangono
ball(T2, P, C, S) :- ball(T2, P, C, S-1), move(T1, T2, S).

% ============================================================================
% CONDIZIONE DI VITTORIA
% ============================================================================

% Il gioco è vinto quando tutti i tubi sono completi o vuoti
game_won(S) :- step(S),
               tube(T) : (complete_tube(T, S) ; empty_tube(T, S)).

% Deve essere raggiunta la vittoria
:- not game_won(S) : step(S).

% Ottimizzazione: minimizza il numero di passi
won_at_step(S) :- game_won(S), not game_won(S-1).
#minimize { S : won_at_step(S) }.

% ============================================================================
% OUTPUT
% ============================================================================

% Mostra le mosse nella soluzione
#show move/3.

% Debug: mostra lo stato finale
#show ball/4.
#show complete_tube/2.
#show game_won/1.