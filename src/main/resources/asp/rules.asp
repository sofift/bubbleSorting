Programma ASP per il planner

% Dominio:
%tube(1..N). pos(0..3). color(red..green.. yellow..). step(0..H). capacity(4).
succPos(0,1). succPos(1,2). succPos(2,3).

% domini
tube(1..N)    :- num_tubes(N).
pos(0..Cap-1) :- capacity(Cap).
step(0..H)    :- horizon(H).

% stato in cui si trova una pallina
%ball(T, P, C, S) --> al tempo S, in posizione P nel tubo T si trova la pallina di colore C

% ball(T, P, C, S) :- tube(T), pos(P), color(C), step(S).
ball(T,P,C,0) :- init_ball(T,P,C).

% dominio colori
color(C) :- init_ball(_,_,C).

% azioni
%canMove(From, To, S) --> al tempo S, posso spostare la pallina da From a To?
%move(From, To, S) --> al tempo S, sposta la pallina da From a To

% obiettivo
%mono(T, S) --> al tempo S, il tubo T è vuoto o monocolore
%goal(S) --> al tempo S è stato raggiunto l'obiettivo

% predicati utili
% top_color(T, S, C) :-  --> al tempo S, nel tubo T in cima si trova il colore C

top_pos(T,S,P)   :- nonEmpty(T,S), P = #max { P1 : ball(T,P1,_,S) }.
top_color(T,S,C) :- top_pos(T,S,P), ball(T,P,C,S).

% tubo vuoto (nessuna cima)
%empty(T,S) :- tube(T), step(S), not ball(T,_,_,S).

% height(T, S, H) --> al tempo S, T ha altezza H

height(T, S, H) :- tube(T), step(S), H = #count{P : pos(P), ball(T, P, _, S)}.
full(T, S) :- height(T, S, H), capacity(C), H = C.
empty(T, S) :- height(T, S, H), H = 0.
nonEmpty(T, S) :- height(T, S, H), H>0.
space(T, S, Tot) :- height(T, S, H), capacity(C), Tot = C - H.


canMove(From, To, S) :- tube(From), tube(To), step(S), From != To,
			 nonEmpty(From, S), not full(To, S),
			top_color(From, S, C), top_color(To, S, C).
canMove(From, To, S) :- tube(From), tube(To), step(S), From != To,
			 nonEmpty(From, S), empty(To, S).

% al più una mossa per step
0 { move(F,T,S) : canMove(F,T,S) } 1 :- step(S), S>0.

% vieta rimbalzo immediato
:- move(A,B,S), move(B,A,S+1).



% effetti
% Per ogni pallina presente allo step S, o viene spostata (se è la cima del From mosso), oppure rimane alla sua posizione P a S+1.
stay(T,P,C,S) :- ball(T,P,C,S), step(S), not moved_from(T,P,S).

ball(T,P,C,S+1) :- stay(T,P,C,S), step(S), step(S+1).

% Una pallina è "moved_from" se è esattamente la cima del tubo sorgente in una mossa
moved_from(From,P,S) :- move(From,To,S), top_pos(From,S,P).

% La nuova posizione di cima in To a S+1 è l'altezza attuale + 1
new_top_pos(To,S,NP) :- move(From,To,S), height(To,S,H), NP = H.

% Il colore spostato è il top_color del From a S
moved_color(C,From,S) :- move(From,To,S), top_color(From,S,C).

ball(To,NP,C,S+1) :- move(From,To,S), new_top_pos(To,S,NP), moved_color(C,From,S).

% impedisce mosse all'ultimo step se non esiste S+1
:- move(_,_,S), not step(S+1).


% verifica goal
% un tubo è mono se è vuoto o dello stesso colore
mono(T,S) :- empty(T,S).
mono(T,S) :- nonEmpty(T,S), color(C),
             H = #count{ P : pos(P), ball(T,P,C,S) },
             height(T,S,HT), H = HT.
not_mono(S) :- tube(T), not mono(T,S).

goal(S) :- step(S), not not_mono(S).


% bisogna raggiungere il goal
last(S) :- step(S), not step(S+1).
:- last(S), not goal(S).

% minimizzare il numero di mosse
:~ move(F,T,S). [1@2,F,T,S]

% quale raggiunge prima l'obiettivo
earlier_goal(S) :- goal(S1), step(S1), step(S), S1 < S.
first_goal(S):- goal(S), not earlier_goal(S).
:~ first_goal(S). [S@1,S]






