% =========================
%  RULES (planner ASP) - NO { } AND NO #AGGREGATES
%  Compatible with DLV2 syntax constraints reported by user
% =========================

% --- Successor on positions (capacity 4 assumed: 0..3) ---
succ(0,1). succ(1,2). succ(2,3).

% NOTE: pos/step/tube/init_ball facts are expected from Java.
% Example domains (from Java):
%   pos(0). pos(1). pos(2). pos(3).
%   step(0). step(1). ... step(H).
%   tube(1)..tube(N).
%   init_ball(T,P,C).

% --- Colors from initial state ---
color(C) :- init_ball(_,_,C).

% --- Initial state at step 0 ---
ball(T,P,C,0) :- init_ball(T,P,C), tube(T).

% --- Step successor ---
next(S,S1) :- step(S), S1 = S + 1, step(S1).

% --- Activate only reached steps to reduce grounding ---
active(0).
active(S1) :- move(_,_,S), next(S,S1).

% --- Tube state predicates (guarded by step) ---
has_ball(T,S) :- ball(T,_,_,S).
empty(T,S) :- tube(T), step(S), not has_ball(T,S).
nonempty(T,S) :- has_ball(T,S).
full(T,S) :- ball(T,0,_,S), ball(T,1,_,S), ball(T,2,_,S), ball(T,3,_,S).

% --- Top of tube ---
higher(T,S,P) :- step(S), tube(T), pos(P), pos(P2), ball(T,P2,_,S), P2 > P.
top_pos(T,S,P) :- step(S), tube(T), pos(P), ball(T,P,_,S), not higher(T,S,P).
top_color(T,S,C) :- step(S), tube(T), top_pos(T,S,P), ball(T,P,C,S).

% --- Move preconditions (guarded by active step) ---
canMove(F,T,S) :-
    active(S), step(S), tube(F), tube(T), F != T,
    nonempty(F,S), empty(T,S).

canMove(F,T,S) :-
    active(S), step(S), tube(F), tube(T), F != T,
    nonempty(F,S), not full(T,S),
    top_color(F,S,C), top_color(T,S,C).

% --- Guess move WITHOUT {} choice (complementary defaults) ---
move(F,T,S) :- canMove(F,T,S), not not_move(F,T,S).
not_move(F,T,S) :- canMove(F,T,S), not move(F,T,S).

% --- At most one move per step ---
:- move(F1,T1,S), move(F2,T2,S), F1 != F2.
:- move(F1,T1,S), move(F2,T2,S), T1 != T2.

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

% --- Goal: each tube is monochrome (no aggregates) ---
% We detect a violation if two different colors appear in the same tube
% --- Pieno e monocolore (niente aggregati)
mono_full(T,S) :- ball(T,0,C,S), ball(T,1,C,S), ball(T,2,C,S), ball(T,3,C,S).

% --- Buono = vuoto oppure pieno e mono
good(T,S) :- empty(T,S).
good(T,S) :- mono_full(T,S).

% --- Tutti i tubi buoni => goal
not_all_good(S) :- active(S), tube(T), not good(T,S).
goal(S) :- active(S), not not_all_good(S).



solved(S) :- goal(S).

% --- Stop after the first goal (hard prune) ---
:- goal(S), move(_,_,S).
:- goal(S), next(S,S1), move(_,_,S1).

% --- Require reachability of a goal ---
exists_goal :- goal(S).
:- not exists_goal.

% --- Optimization: minimize moves; prefer earliest goal ---
:~ move(F,T,S). [1@1,F,T,S]

earlier_goal(S) :- goal(S1), step(S1), step(S), S1 < S.
first_goal(S) :- goal(S), not earlier_goal(S).
:~ first_goal(S). [S@2,S]


% --- Projection ---
show_move(F,T,S) :- move(F,T,S).
#show show_move/3.
