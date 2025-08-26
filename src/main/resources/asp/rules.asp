


% =========================
%  RULES (planner ASP)
% =========================

% --- DOMINI ESPLICITI (niente variabili negli intervalli) ---

succ(0,1). succ(1,2). succ(2,3).


% Colori: derivati dall'istanza
color(C) :- init_ball(_,_,C).

% --- Stato iniziale ---
ball(T,P,C,0) :- init_ball(T,P,C).

% --- Altezza / pieno / vuoto ---
% --- Vuoto / non-vuoto / pieno (senza aggregati) ---
has_ball(T,S)  :- ball(T,_,_,S).
empty(T,S)     :- tube(T), step(S), not has_ball(T,S).
nonempty(T,S)  :- has_ball(T,S).

% capacità=4: pieno se esistono tutte e 4 le posizioni occupate
full(T,S) :- ball(T,0,_,S), ball(T,1,_,S), ball(T,2,_,S), ball(T,3,_,S).


% --- Cima del tubo (safe) ---
% --- Cima del tubo (senza #max) ---
% P è cima se esiste una pallina in P e non esiste pallina in posizione maggiore
higher(T,S,P) :- ball(T,P2,_,S), pos(P), pos(P2), P2 > P.
top_pos(T,S,P)   :- ball(T,P,_,S), not higher(T,S,P).
top_color(T,S,C) :- top_pos(T,S,P), ball(T,P,C,S).



% --- Precondizioni di mossa ---
% Arrivo vuoto: sempre lecito se il sorgente non è vuoto
canMove(F,T,S) :-
  step(S), tube(F), tube(T), F != T,
  nonempty(F,S), empty(T,S).

% Arrivo non pieno: serve stesso colore in cima
canMove(F,T,S) :-
  step(S), tube(F), tube(T), F != T,
  nonempty(F,S), not full(T,S),
  top_color(F,S,C), top_color(T,S,C).

% --- Scelta: al più una mossa per step ---
move(F,T,S) | no_move(F,T,S) :- canMove(F,T,S), next(S,S1).
% al più UNA mossa allo stesso step S (niente doppie mosse)
:- move(F1,T1,S), move(F2,T2,S), F1 != F2.
:- move(F1,T1,S), move(F2,T2,S), T1 != T2.

% No rimbalzo immediato
:- move(A,B,S), move(B,A,S1), next(S,S1).

% --- Transizioni (S -> S1) restano uguali fin qui ---
next(S,S1) :- step(S), S1 = S+1, step(S1).

moved_from(F,P,S) :- move(F,_,S), top_pos(F,S,P).

stay(T,P,C,S) :- ball(T,P,C,S), step(S), not moved_from(T,P,S).
ball(T,P,C,S1) :- stay(T,P,C,S), next(S,S1).

% --- Nuova cima senza contare: se T è vuoto va in 0, altrimenti succ(cima) ---
new_top_pos(T,S,0)   :- move(_,T,S), empty(T,S).
new_top_pos(T,S,NP)  :- move(_,T,S), top_pos(T,S,P), succ(P,NP).

moved_color(C,F,S)   :- move(F,_,S), top_color(F,S,C).
ball(T,NP,C,S1)      :- move(F,T,S), new_top_pos(T,S,NP), moved_color(C,F,S), next(S,S1).


% --- Goal: tutti i tubi vuoti o monocolore ---
% --- Goal: vuoto o monocolore (senza #count) ---
% "Due colori" se nello stesso tubo esistono due palline di colori diversi
two_colors(T,S) :- ball(T,_,C1,S), ball(T,_,C2,S), C1 != C2.

mono(T,S) :- empty(T,S).
mono(T,S) :- nonempty(T,S), not two_colors(T,S).

% nota: qui S è vincolato da step(S) per la safety
not_mono(S) :- step(S), tube(T), not mono(T,S).
goal(S)     :- step(S), not not_mono(S).

% definisci solved(S) come alias del tuo goal(S)
solved(S) :- goal(S).

% richiedi che allo step finale il goal valga
:- finalStep(K), not solved(K).




% --- Ottimizzazione: meno mosse, e goal il prima possibile ---
% (livello 1) minimizza il numero di mosse
:~ move(F,T,S), step(S), tube(F), tube(T). [1@1,F,T,S]

% (livello 2) a parità di mosse, preferisci il goal più precoce
earlier_goal(S) :- goal(S1), step(S1), step(S), S1 < S.
first_goal(S)    :- goal(S), not earlier_goal(S).
:~ first_goal(S). [S@2,S]


% ===== DEBUG PROJECTION (compatibile) =====
%show_ball0(T,P,C)      :- ball(T,P,C,0).
%show_top0(T,C)         :- top_color(T,0,C).
%show_move(F,T,S)       :- move(F,T,S).
%first_goal_step(S)     :- first_goal(S).
%show_goal_ball(T,P,C,S):- first_goal(S), ball(T,P,C,S).

% (A) Vincolo di goal: obbliga a raggiungere la configurazione risolta
:- finalStep(K), not solved(K).

% (B) Proietta le mosse in output (stesso piano, solo per stampa)
show_move(F,T,S) :- move(F,T,S).

% (C) Se usi clingo: mostra solo le mosse
#show show_move/3.

