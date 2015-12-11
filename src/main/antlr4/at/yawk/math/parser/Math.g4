grammar Math;

WS: (' ' | '\t')+ -> channel(HIDDEN);

math: expressionOpen EOF;

VariableName: 'a'..'z'+;
Integer: '0'..'9'+;

vector : '(' (expressionOpen ',')+ expressionOpen ')';
functionCall : VariableName '(' ((expressionOpen ',')+ expressionOpen)? ')';
variableAccess : VariableName;
expressionClosed : '(' expressionOpen ')' | Integer | functionCall | variableAccess | vector;

expressionOpen : expressionOpenLowPriority;
expressionOpenLowPriority : addition | expressionOpenMediumPriority;
expressionOpenMediumPriority : multiplication | expressionOpenHighPriority;
expressionOpenHighPriority : exponentiation | expressionClosed;

Plus: '+';
Minus: '-';
addition: (items+=expressionOpenMediumPriority operations+=(Plus | Minus))+ items+=expressionOpenMediumPriority;
Multiply: '*';
Divide: '/';
multiplication: (items+=expressionOpenHighPriority operations+=(Multiply | Divide))+ items+=expressionOpenHighPriority;
exponentiation: base=expressionClosed '^' exponent=expressionClosed;