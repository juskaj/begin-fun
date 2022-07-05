grammar BeginFun;

program:
    line+ EOF;

line: statement | functionDeclaration;

statement:
    matchStatement |
    assignment |
    deconstructingAssignment |
    ifStatement |
    ifElseStatement |
    functionStatement |
    returnStatement |
    whileLoopStatement |
    writeToFileStatement |
    arrayDeclaration |
    systemFunctionCall;

functionDeclaration: 'fun' IDENTIFIER '(' (CONST_DECLARATION IDENTIFIER)? (',' CONST_DECLARATION IDENTIFIER)* ')' (('begin fun' functionBlock 'end fun') |
 ('=>' CONST_DECLARATION 'begin fun' functionBlock 'end fun' '=>' expression));

functionBlock: statement* (statement* returnStatement statement*)*;

functionStatement: functionCall ';';

functionCall: IDENTIFIER '(' (expression',')* expression? ')';

arrayDeclaration: (CONST_DECLARATION array IDENTIFIER ';') | (CONST_DECLARATION array IDENTIFIER '=' '[' constant (',' constant)* ']' ';');
array: '[' INTEGER ']';
getFromArray: IDENTIFIER '[' INTEGER ']';

matchStatement: CONST_DECLARATION IDENTIFIER '=' '{' matchPart* 'default' '=>' expression ';' '}' ';';
matchPart: matchNum | matchString;
matchString: 'matchstring' IDENTIFIER '=' STRING '=>' expression ';';
matchNum: 'matchnum' expression booleanBinaryOp expression '=>' expression ';';

writeToFileStatement: 'write' '(' STRING ',' (STRING | expression) ',' BOOLEAN ')' ';';

returnStatement: 'break fun' ('=>' expression)?;

whileLoopStatement: 'while' '(' expression ')' 'do' block 'end while';

assignment:
    (CONST_DECLARATION IDENTIFIER '=' expression ';') | (IDENTIFIER  '=' expression ';') | getFromArray '=' expression ';';

deconstructingAssignment:
    '{' (IDENTIFIER ',')* IDENTIFIER '}' '<=' '{' (expression',')* expression '}' ';'
    ;

ifElseStatement: 'if' '(' expression ')' 'then' 'begin if' block 'else' block 'end if';

systemFunctionCall:
    PRINT '(' expression ')' ';'                        #printFunctionCall
    ;

ifStatement: 'if' '(' expression ')' 'then' 'begin if' block 'end if';
block: statement*;

constant: INTEGER | DECIMAL | BOOLEAN | STRING ;

expression
 : constant                                             #constantExpression
 | IDENTIFIER                                           #identifierExpression
 | '(' expression ')'                                   #parenthesesExpression
 | booleanUnaryOp expression                            #booleanUnaryOpExpression
 | expression booleanBinaryOp expression                #booleanBinaryOpExpression
 | expression numericMultiOp expression                 #numericMultiOpExpression
 | expression numericAddOp expression                   #numericAddOpExpression
 | expression stringBinaryOp expression                 #stringBinaryOpExpression
 | functionCall                                         #functionCallExpression
 | getFromArray                                         #getFromArrayExpression
 ;

booleanUnaryOp : '!' ;

booleanBinaryOp : '||' | '&&' | '>' | '<' | '>=' | '<=' | '!=' | '==' ;

numericMultiOp : '*' | '/' | '%' ;

numericAddOp : '+' | '-' ;

stringBinaryOp : '..' ;

PRINT : 'print';

INTEGER : '^-'?[1-9]([0-9]+)? | [0-9];
DECIMAL : [0-9]+ '.' [0-9]+ ;
BOOLEAN : 'true' | 'false' ;
STRING : ["] ( ~["\r\n\\] | '\\' ~[\r\n] )* ["] ;
CONST_DECLARATION : 'Int' | 'String' | 'Bool' | 'Decimal';
IDENTIFIER : [a-zA-Z_][a-zA-Z_0-9]* ;
COMMENT : ( '//' ~[\r\n]* | '/*' .*? '*/' ) -> skip ;

WS : [ \t\f\r\n]+ -> skip ;