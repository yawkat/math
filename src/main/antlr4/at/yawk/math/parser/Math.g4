grammar Math;

WS: (' ' | '\t')+ -> channel(HIDDEN);

math: expressionOpen EOF;

VariableName: 'a'..'z'+;
Integer: '0'..'9'+;

vector : '(' (rows+=expressionOpen ',')+ rows+=expressionOpen ')';
functionCall : name=VariableName '(' ((parameters+=expressionOpen ',')* parameters+=expressionOpen)? ')';
variableAccess : name=VariableName;
expressionClosed : parenthesesExpression | Integer | functionCall | variableAccess | vector;

parenthesesExpression : '(' value=expressionOpen ')';

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