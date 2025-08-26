
% =========================
%  RULES (planner ASP) FIX
% =========================

succ(0,1). succ(1,2). succ(2,3).

% --- Colori
color(C) :- init_ball(_,_,C).

% --- Stato iniziale
ball(T,P,C,0) :- init_ball(T,P,C).

% --- Stato tubo
has_ball(T,S) :- ball(T,_,_,S).
empty(T,S) :- tube(T), step(S), not has_ball(T,S).
nonempty(T,S) :- has_ball(T,S).
full(T,S) :- ball(T,0,_,S), ball(T,1,_,S), ball(T,2,_,S), ball(T,3,_,S).

% --- Cima del tubo
higher(T,S,P) :- ball(T,P2,_,S), pos(P), pos(P2), P2 > P.
top_pos(T,S,P) :- ball(T,P,_,S), not higher(T,S,P).
top_color(T,S,C) :- top_pos(T,S,P), ball(T,P,C,S).

% --- Precondizioni mosse
canMove(F,T,S) :-
    step(S), tube(F), tube(T), F != T,
    nonempty(F,S), empty(T,S).
canMove(F,T,S) :-
    step(S), tube(F), tube(T), F != T,
    nonempty(F,S), not full(T,S),
    top_color(F,S,C), top_color(T,S,C).

% --- Scelta mossa
move(F,T,S) | no_move(F,T,S) :- canMove(F,T,S), next(S,S1).
:- move(F1,T1,S), move(F2,T2,S), F1 != F2.
:- move(F1,T1,S), move(F2,T2,S), T1 != T2.
:- move(A,B,S), move(B,A,S1), next(S,S1).

% --- Transizioni
next(S,S1) :- step(S), S1=S+1, step(S1).
moved_from(F,P,S) :- move(F,_,S), top_pos(F,S,P).
stay(T,P,C,S) :- ball(T,P,C,S), step(S), not moved_from(T,P,S).
ball(T,P,C,S1) :- stay(T,P,C,S), next(S,S1).
new_top_pos(T,S,0) :- move(_,T,S), empty(T,S).
new_top_pos(T,S,NP) :- move(_,T,S), top_pos(T,S,P), succ(P,NP).
moved_color(C,F,S) :- move(F,_,S), top_color(F,S,C).
ball(T,NP,C,S1) :- move(F,T,S), new_top_pos(T,S,NP), moved_color(C,F,S), next(S,S1).

% --- Goal
two_colors(T,S) :- ball(T,_,C1,S), ball(T,_,C2,S), C1!=C2.
mono(T,S) :- empty(T,S).
mono(T,S) :- nonempty(T,S), not two_colors(T,S).
not_mono(S) :- step(S), tube(T), not mono(T,S).
goal(S) :- step(S), not not_mono(S).
solved(S) :- goal(S).

% --- Vincolo: obbliga ad arrivare a soluzione almeno una volta
:- not exists_goal.
exists_goal :- goal(S).

% --- Ottimizzazione
:~ move(F,T,S), step(S), tube(F), tube(T). [1@1,F,T,S]
earlier_goal(S) :- goal(S1), step(S1), step(S), S1 < S.
first_goal(S) :- goal(S), not earlier_goal(S).
:~ first_goal(S). [S@2,S]

% --- Proiezione
show_move(F,T,S) :- move(F,T,S).
#show show_move/3.
