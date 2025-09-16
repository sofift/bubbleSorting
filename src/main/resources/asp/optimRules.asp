succ(0,1). succ(1,2). succ(2,3).
color(C) :- init_ball(_,_,C).

ball(T,P,C,0) :- init_ball(T,P,C), tube(T).

next(S,S1) :- step(S), S1 = S + 1, step(S1).

active(0).
active(S1) :- move(_,_,S), next(S,S1).

has_ball(T,S) :- ball(T,_,_,S).
empty(T,S) :- tube(T), active(S), not has_ball(T,S).
nonempty(T,S) :- has_ball(T,S).

full(T,S) :- tube(T), active(S), #count{P : ball(T,P,_,S)} = 4.

higher(T,S,P) :- active(S), tube(T), pos(P), pos(P2), ball(T,P2,_,S), P2 > P.
top_pos(T,S,P) :- active(S), tube(T), pos(P), ball(T,P,_,S), not higher(T,S,P).
top_color(T,S,C) :- active(S), tube(T), top_pos(T,S,P), ball(T,P,C,S).

canMove(F,T,S) :-
    active(S), active(S), tube(F), tube(T), F != T,
    nonempty(F,S), empty(T,S).

canMove(F,T,S) :-
    active(S), active(S), tube(F), tube(T), F != T,
    nonempty(F,S), not full(T,S),
    top_color(F,S,C), top_color(T,S,C).

{ move(F,T,S) : canMove(F,T,S) }.

:- step(S), #count{F,T : move(F,T,S)} > 1.

:- move(A,B,S), next(S,S1), move(B,A,S1).

moved_from(F,P,S) :- move(F,_,S), top_pos(F,S,P).
stay(T,P,C,S) :- active(S), ball(T,P,C,S), not moved_from(T,P,S).
ball(T,P,C,S1) :- stay(T,P,C,S), next(S,S1).

new_top_pos(T,S,0) :- move(_,T,S), empty(T,S).
new_top_pos(T,S,NP) :- move(_,T,S), top_pos(T,S,P), succ(P,NP).
moved_color(C,F,S) :- move(F,_,S), top_color(F,S,C).
ball(T,NP,C,S1) :- move(F,T,S), new_top_pos(T,S,NP), moved_color(C,F,S), next(S,S1).

present_color(T,S,C) :- ball(T,_,C,S).
mono_full(T,S) :- tube(T), active(S),
                  #count{P : ball(T,P,_,S)} = 4,
                  #count{C : present_color(T,S,C)} = 1.

good(T,S) :- empty(T,S).
good(T,S) :- mono_full(T,S).

bad(T,S) :- tube(T), active(S), not good(T,S).
goal(S)  :- active(S), #count{T : bad(T,S)} = 0.

solved(S) :- goal(S).

:- goal(S), move(_,_,S).
:- goal(S), next(S,S1), move(_,_,S1).

exists_goal :- goal(S).
:- not exists_goal.

:~ move(F,T,S). [1@1,F,T,S]

first_goal(S) :- goal(S), #min{S1 : goal(S1)} = S.
:~ first_goal(S). [S@2,S]

show_move(F,T,S) :- move(F,T,S).
#show show_move/3.