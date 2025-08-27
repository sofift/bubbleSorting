% =========================
%  FACTS (istanza di test)
% =========================
% Parametri dominio
num_tubes(6).
capacity(4).
horizon(40).     % alza se non trova piano

% --- LIVELLO A: già risolto (garantisce un modello, 0 mosse) ---
init_ball(1,0,red).    init_ball(1,1,red).    init_ball(1,2,red).    init_ball(1,3,red).
init_ball(2,0,green).  init_ball(2,1,green).  init_ball(2,2,green).  init_ball(2,3,green).
init_ball(3,0,blue).   init_ball(3,1,blue).   init_ball(3,2,blue).   init_ball(3,3,blue).
init_ball(4,0,yellow). init_ball(4,1,yellow). init_ball(4,2,yellow). init_ball(4,3,yellow).
% Tubi vuoti
% (5) e (6) vuoti

% --- LIVELLO B: esempio più interessante (commenta A e decommenta questo blocco)
% init_ball(1,0,red).   init_ball(1,1,green). init_ball(1,2,red).   init_ball(1,3,green).
% init_ball(2,0,blue).  init_ball(2,1,yellow).init_ball(2,2,blue).  init_ball(2,3,yellow).
% init_ball(3,0,red).   init_ball(3,1,yellow).init_ball(3,2,green). init_ball(3,3,blue).
% init_ball(4,0,green). init_ball(4,1,blue).  init_ball(4,2,yellow).init_ball(4,3,red).
% (5) e (6) vuoti

% =========================
%  RULES (planner ASP)
% =========================

% --- DOMINI ESPLICITI (niente variabili negli intervalli) ---
tube(1).  tube(2).  tube(3).  tube(4).  tube(5).  tube(6).

pos(0). pos(1). pos(2). pos(3).   % 0=fondo, 3=cima

step(0).  step(1).  step(2).  step(3).  step(4).  step(5).  step(6).  step(7).  step(8).  step(9).
step(10). step(11). step(12). step(13). step(14). step(15). step(16). step(17). step(18). step(19).
step(20). step(21). step(22). step(23). step(24). step(25). step(26). step(27). step(28). step(29).
step(30). step(31). step(32). step(33). step(34). step(35). step(36). step(37). step(38). step(39).
step(40).
