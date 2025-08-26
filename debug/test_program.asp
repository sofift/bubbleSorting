% Test semplice per verificare DLV2
person(john).
person(mary).
likes(X,Y) :- person(X), person(Y), X != Y.
