% successore pos
succ(0,1). succ(1,2). succ(2,3).


% stato iniziale
ball(T,P,C,0) :- init_ball(T,P,C), tube(T).

% step temporale successivo
next(S,S1) :- step(S), S1 = S + 1, step(S1).

% attivazione degli step -->  propaga fino all'orizzonte
active(0).
active(S1) :- active(S), next(S,S1).

% stato dei tubi
has_ball(T,S) :- ball(T,_,_,S).
empty(T,S)    :- tube(T), active(S), not has_ball(T,S).
nonempty(T,S) :- has_ball(T,S).
full(T,S) :- tube(T), active(S), #count{P : ball(T,P,_,S)} = 4.

% ball al top di un tubo
higher(T,S,P)    :- active(S), tube(T), pos(P), pos(P2), ball(T,P2,_,S), P2 > P. % indiv. la posizione che non è top
top_pos(T,S,P)   :- active(S), tube(T), pos(P), ball(T,P,_,S), not higher(T,S,P).
top_color(T,S,C) :- active(S), tube(T), top_pos(T,S,P), ball(T,P,C,S).

% mosse pos. valide
canMove(F,T,S) :-
    active(S), tube(F), tube(T), F != T,
    nonempty(F,S), empty(T,S).

canMove(F,T,S) :-
    active(S), tube(F), tube(T), F != T,
    nonempty(F,S), not full(T,S),
    top_color(F,S,C), top_color(T,S,C).

% scelta
{ move(F,T,S) : canMove(F,T,S) } :- active(S), not goal(S).

% al più una mossa per step
:- step(S), #count{F,T : move(F,T,S)} > 1.
:- move(A,B,S), next(S,S1), move(B,A,S1).   % non effettuare il backtrak nello step suc

% transizioni di stato
moved_from(F,P,S) :- move(F,_,S), top_pos(F,S,P). % indico la pallina effettivamente spostata
stay(T,P,C,S)     :- active(S), ball(T,P,C,S), not moved_from(T,P,S).   % ball che non cambiano posizione e non è una pall. da spostare
ball(T,P,C,S1)    :- stay(T,P,C,S), next(S,S1). % propaga palline e posizioni

new_top_pos(T,S,0)  :- move(_,T,S), empty(T,S).                         % se il tubo è vuoto la pallina sarà in pos 0
new_top_pos(T,S,NP) :- move(_,T,S), top_pos(T,S,P), succ(P,NP).         % se il tubo non è vuoto prende la posizione della pallina in cima, messo che ci sia un successore
moved_color(C,F,S)  :- move(F,_,S), top_color(F,S,C).                   % aggiorna il colore pallina in cima
ball(T,NP,C,S1)     :- move(F,T,S), new_top_pos(T,S,NP), moved_color(C,F,S), next(S,S1).        % propaga la pallina in cima

% monocromia
present_color(T,S,C) :- ball(T,_,C,S).  % seleziona i colori presenti in un tubo allo step S
mono_full(T,S) :- tube(T), active(S),
    #count{P : ball(T,P,_,S)} = 4,
    #count{C : present_color(T,S,C)} = 1.   % tubo T pieno e monocromatico

% tubo accettato
good(T,S) :- empty(T,S).
good(T,S) :- mono_full(T,S).

% goal: tutti i tubi sono in obiettivo --> o vuoti o pieni monocromatici
goal(S) :- active(S), #count{T : tube(T), not good(T,S)} = 0.

% stop reale dopo il primo goal
:- goal(S), move(_,_,S).            % nessuna mossa nello stesso step del goal
:- goal(S), next(S,S1), move(_,_,S1). % né nello step successivo

% richiedi almeno un goal raggiungibile
exists_goal :- goal(S).
:- not exists_goal.


% proiezione
show_move(F,T,S) :- move(F,T,S).
#show show_move/3.
