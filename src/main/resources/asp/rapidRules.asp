

% =========================
%  RULES (planner ASP) - OTTIMIZZATO con aggregati e choice rules
%  Mantenendo la logica originale
% =========================
% =========================
% RULES (planner ASP) - OTTIMIZZATO per PRIMA SOLUZIONE
% Focus su velocità invece che ottimalità
% =========================

% --- Successor on positions (capacity 4 assumed: 0..3) ---
succ(0,1). succ(1,2). succ(2,3).

% --- Colors from initial state ---
color(C) :- init_ball(_,_,C).

% --- Initial state at step 0 ---
ball(T,P,C,0) :- init_ball(T,P,C), tube(T).

% --- Step successor ---
next(S,S1) :- step(S), S1 = S + 1, step(S1).

% --- Attivazione progressiva (SEMPLIFICATA) ---
active(0).
active(S1) :- active(S), next(S,S1).

% --- Tube state predicates ---
has_ball(T,S) :- ball(T,_,_,S).
empty(T,S) :- tube(T), active(S), not has_ball(T,S).
nonempty(T,S) :- has_ball(T,S).

% OTTIMIZZATO: full usando aggregato #count
full(T,S) :- tube(T), active(S), #count{P : ball(T,P,_,S)} = 4.

% --- Top of tube ---
higher(T,S,P) :- active(S), tube(T), pos(P), pos(P2), ball(T,P2,_,S), P2 > P.
top_pos(T,S,P) :- active(S), tube(T), pos(P), ball(T,P,_,S), not higher(T,S,P).
top_color(T,S,C) :- active(S), tube(T), top_pos(T,S,P), ball(T,P,C,S).

% --- Move preconditions (guarded by active step) ---
canMove(F,T,S) :-
    active(S), tube(F), tube(T), F != T,
    nonempty(F,S), empty(T,S).

canMove(F,T,S) :-
    active(S), tube(F), tube(T), F != T,
    nonempty(F,S), not full(T,S),
    top_color(F,S,C), top_color(T,S,C).

% OTTIMIZZATO: Choice rule con forzatura per performance
{ move(F,T,S) : canMove(F,T,S) } :- active(S), not goal(S).

% --- At most one move per step (OTTIMIZZATO con aggregato) ---
:- step(S), #count{F,T : move(F,T,S)} > 1.

% --- No immediate backtracking ---
:- move(A,B,S), next(S,S1), move(B,A,S1).

% --- State transitions (only for active steps) ---
moved_from(F,P,S) :- move(F,_,S), top_pos(F,S,P).
stay(T,P,C,S) :- active(S), ball(T,P,C,S), not moved_from(T,P,S).
ball(T,P,C,S1) :- stay(T,P,C,S), next(S,S1).

new_top_pos(T,S,0) :- move(_,T,S), empty(T,S).
new_top_pos(T,S,NP) :- move(_,T,S), top_pos(T,S,P), succ(P,NP).
moved_color(C,F,S) :- move(F,_,S), top_color(F,S,C).
ball(T,NP,C,S1) :- move(F,T,S), new_top_pos(T,S,NP), moved_color(C,F,S), next(S,S1).

% OTTIMIZZATO: Goal usando aggregati per monochrome check
mono_full(T,S) :- tube(T), active(S),
    #count{P : ball(T,P,_,S)} = 4,
    #count{C : ball(T,_,C,S)} = 1.

% --- Buono = vuoto oppure pieno e mono
good(T,S) :- empty(T,S).
good(T,S) :- mono_full(T,S).

% OTTIMIZZATO: Goal usando aggregato per "all good"
goal(S) :- active(S), #count{T : tube(T), not good(T,S)} = 0.

% --- Stop after the first goal (hard prune) ---
:- goal(S), move(_,_,S).

% --- Require reachability of a goal ---
exists_goal :- goal(S).
:- not exists_goal.

% --- Projection ---
show_move(F,T,S) :- move(F,T,S).
#show show_move/3.