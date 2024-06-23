import java.io.*;
import java.util.*;


public class C0 {
    BufferedReader source; /* input file */
    int line_number; 
    char ch;               /* next character */
    char pch = ' ';
    char escape;
    String id_string;      /* identifier */
    int literal_value;     /* number */
    
    enum type {Variable, Function};
    Map<String, id_record> symbol_table;
    int variable_count = 0;
    id_record check;

    enum token {
	END_PROGRAM,
	IDENTIFIER, LITERAL,
	ELSE, IF, WHILE,
	COMMA, SEMICOLON,
	LEFT_BRACE, RIGHT_BRACE, LEFT_PAREN, RIGHT_PAREN,
	EQUAL, OROR, ANDAND, OR, AND,
	EQEQ, NOTEQ, LE, LT, GE, GT,
	PLUS, MINUS, STAR, SLASH, PERCENT
    }
    token sy;

    enum operation {
    LCONST, LOAD, STORE, POPUP,
    CALL, JUMP, FJUMP, TJUMP, HALT,
    MULT, DIV, MOD, ADD, SUB, ANDOP, OROP,
    EQOP, NEOP, LEOP, LTOP, GEOP, GTOP
    };

    final int CODE_MAX = 5000;
    int pc = 0;
    code_type code[] = new code_type[CODE_MAX];

    final int Stack_Size = 100;
    int memory_size = variable_count + Stack_Size;
    int memory[] = new int[memory_size];
    int sp, ic;
    int error_count = 0;
    
    void emit(operation op, int param) {
	if (pc >= CODE_MAX) {
	    error("not defined");
	    System.exit(1);
	}
	code[pc] = new code_type();
	code[pc].op_code = op;
	code[pc].operand = param;
	pc++;
    }

    class code_type {
    operation op_code;
    int operand;
    };

    void next_ch() {
	try {
	    ch = (char)source.read();
	    if (ch == '\n')
		line_number++;
	}
	catch (Exception e) {
	    System.out.println("IO error occurred");
	    System.exit(1);
	}
    }

    class id_record {
	type id_class;
	int address;
	int function_id;
	int parameter_count;

	id_record(type a, int b, int c, int d) {
	    this.id_class = a;
	    this.address = b;
	    this.function_id = c;
	    this.parameter_count = d;
	}
    }

    void push(int x) {
	if (sp >= memory_size) {
	    run_error("stack overflow");
	}
	memory[sp] = x;
	sp++;
    }

    int pop() {
	if (sp <= variable_count) {
	    run_error("system error: stack underflow");
	}
	sp--;
	return memory[sp];
    }

    void run_error(String s) {
	System.out.println(s);
	System.exit(1);
    }

    void interpret(boolean trace) {
	Scanner sc = new Scanner(System.in);
	ic = 0;
	sp = variable_count;
	int o1 = 0, o2 = 0;
	for (;;) {
	    operation instruction = code[ic].op_code;
	    int argument = code[ic].operand;
	    if (trace) {
		System.out.print("ic=" + String.format("%4d", ic) +
				 ", sp=" + String.format("%5d", sp) +
				 ", code=(" +
				 String.format("%-6s", instruction) +
				 String.format("%6d", argument) + ")");
		if (sp > variable_count) {
		    int vari = pop();
		    push(vari);
		    System.out.print(", top=" + String.format("%10d", vari));
		}
		System.out.println();
	    }
	    ic++;
	    int val = 0;
	    switch (instruction) {
	    case LCONST:
		push(argument);
		continue;
	    case LOAD:
		o1 = memory[argument];
		if (argument < 0 || argument >= variable_count) {
		    run_error("load error");
		}
		push(o1);
		continue;
	    case STORE:
		o1 = pop();
		if (argument < 0 || argument >= variable_count) {
		    run_error("store error");
		}
		memory[argument] = o1;
		push(o1);
		continue;
	    case POPUP:
		o1 = pop();
		continue;
	    case CALL:
		switch(argument) {
		case 0:
		    System.out.print("getd: ");
		    push(sc.nextInt());
		    continue;
		case 1:
		    int width = pop();
		    val = pop();
		    String s = String.format("%d", val);
		    int d = width - s.length();
		    while (d > 0) {
			System.out.print(" ");
			d--;
		    }
		    System.out.print(s);
		    push(val);
		    continue;
		case 2:
		    System.out.println();
		    push(0);
		    continue;
		case 3:
		    val = pop();
		    char c = (char)val;
		    System.out.print(c);
		    push(val);
		    continue;
		default:
		    run_error("not existed");
		}
		continue;
	    case JUMP:
		ic = argument;
		continue;
	    case FJUMP:
		o1 = pop();
		if (o1 == 0) {
		    ic = argument;
		}
		continue;
	    case TJUMP:
		o1 = pop();
		if (o1 != 0) {
		    ic = argument;
		}
		continue;
	    case HALT:
		if(sp == variable_count) {
		    return;
		} else {
		    run_error("sp is not equal to variable_count");
		}
		continue;
	    case MULT:
		o2 = pop();
		o1 = pop();
		push(o1 * o2);
		continue;
	    case ADD:
		o2 = pop();
		o1 = pop();
		push(o1 + o2);
		continue;
	    case SUB:
		o2 = pop();
		o1 = pop();
		push(o1 - o2);
		continue;
	    case ANDOP:
		o2 = pop();
		o1 = pop();
		push(o1 & o2);
		continue;
	    case OROP:
		o2 = pop();
		o1 = pop();
		push(o1 | o2);
		continue;
	    case DIV:
		o2 = pop();
		o1 = pop();
		if (o2 == 0) {
		    run_error("division by zero");
		}
		push(o1/o2);
		continue;
	    case MOD:
		o2 = pop();
		o1 = pop();
		if (o2 == 0) {
		    run_error("modulus by zero");
		}
		push(o1%o2);
		continue;
	    case EQOP:
		o2 = pop();
		o1 = pop();
		if (o1 == o2) {
		    push(1);
		} else {
		    push(0);
		}
		continue;
	    case NEOP:
		o2 = pop();
		o1 = pop();
		if (o1 != o2) {
		    push(1);
		} else {
		    push(0);
		}
		continue;
	    case LEOP:
		o2 = pop();
		o1 = pop();
		if (o1 <= o2) {
		    push(1);
		} else {
		    push(0);
		}
		continue;
	    case LTOP:
		o2 = pop();
		o1 = pop();
		if (o1 < o2) {
		    push(1);
		} else {
		    push(0);
		}
		continue;
	    case GEOP:
		o2 = pop();
		o1 = pop();
		if (o1 >= o2) {
		    push(1);
		} else {
		    push(0);
		}
		continue;
	    case GTOP:
		o2 = pop();
		o1 = pop();
		if (o1 > o2) {
		    push(1);
		} else {
		    push(0);
		}
		continue;
	    default:
		run_error("system error: undefined op code");
	    }
	}
    }		
    
    void get_token() { 
	while(ch == ' ' || ch == '\n' || ch == '\t' || ch == '\r') {
	    next_ch();
	}
	if (ch == 65535) {
	    sy = token.END_PROGRAM;
	    return;
	}
	if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || ch == '_') {
	    id_string = "";
	    while ((ch >= 'A' && ch <= 'Z') ||
		   (ch >= 'a' && ch <= 'z') ||
		   (ch >= '0' && ch <= '9') || ch == '_') {
		id_string += ch;
		next_ch();
	    }
	    /* id_string が予約語と同じかどうかを順次検査する */
	    if (id_string.equals("else")) {
		sy = token.ELSE;
		return;
	    }
	    else if (id_string.equals("if")) {
		sy = token.IF;
		return;
	    }
	    else if (id_string.equals("while")) {
		sy = token.WHILE;
		return;
	    }
	    else {
		sy = token.IDENTIFIER;
		return;
	    }
	}
	else if (ch >= '0' && ch <= '9') {
	    int v = 0;
	    while (ch >= '0' && ch <= '9') {
		if (v == 214748364 && ((ch - '0') >= 8 && (ch - '0') <= 9)) {
		    error("too large integer literal");
		    while(ch >= '0' && ch <= '9') {
			next_ch();
		    }
		    literal_value = 0;
		    sy = token.LITERAL;
		    return;
		}
		else if (v > 214748364) {
		    error("too large integer literal");
		    while(ch >= '0' && ch <= '9') {
			next_ch();
		    }
		    literal_value = 0;
		    next_ch();
		    sy = token.LITERAL;
		    return;
		}
		v = v * 10 + (ch - '0');
		next_ch();
	    }
	    literal_value = v;
	    sy = token.LITERAL;
	    return;
	}
	else {
	    if (ch == ',') {
		next_ch();
		sy = token.COMMA;
		return;
	    }
	    else if (ch == ';') {
		next_ch();
		sy = token.SEMICOLON;
		return;
	    }
	    else if (ch == '{') {
		next_ch();
		sy = token.LEFT_BRACE;
		return;
	    }
	    else if (ch == '}') {
		next_ch();
		sy = token.RIGHT_BRACE;
		return;
	    }
	    else if (ch == '(') {
		next_ch();
		sy = token.LEFT_PAREN;
		return;
	    }
	    else if (ch == ')') {
		next_ch();
		sy = token.RIGHT_PAREN;
		return;
	    }
	    else if (ch == '=') {
		next_ch();
		if (ch == '=') {
		    next_ch();
		    sy = token.EQEQ;
		    return;
		}
		sy = token.EQUAL;
		return;
	    }
	    else if (ch == '|') {
		next_ch();
		if (ch == '|') {
		    next_ch();
		    sy = token.OROR;
		    return;
		}
		sy = token.OR;
		return;
	    }
	    else if (ch == '&') {
		next_ch();
		if (ch == '&') {
		    next_ch();
		    sy = token.ANDAND;
		    return;
		}
		sy = token.AND;
		return;
	    }
	    else if (ch == '!') {   /* ch == NOTEQ */
		next_ch();
		if (ch == '=') {
		    next_ch();
		    sy = token.NOTEQ;
		    return;
		}
		error("exclamation mark not followed by equal");
		get_token();
		return;
		/* ch != NOTEQ */
	    }
	    else if (ch  == '<') {
		next_ch();
		if (ch == '=') {
		    next_ch();
		    sy = token.LE;
		    return;
		}
		sy = token.LT;
		return;
	    }
	    else if (ch == '>') {
		next_ch();
		if (ch == '=') {
		    next_ch();
		    sy = token.GE;
		    return;
		}
		sy = token.GT;
		return;
	    }
	    else if (ch == '+') {
		next_ch();
		sy = token.PLUS;
		return;
	    }
	    else if (ch == '-') {
		next_ch();
		sy = token.MINUS;
		return;
	    }
	    else if (ch == '*') {
		next_ch();
		sy = token.STAR;
		return;
	    }
	    else if (ch == '/') { /* ch == '/' */
		next_ch();
		if (ch == '*') {
		    next_ch();
		    pch = ch;
		    while (true) {
			next_ch();
			if (pch == '*' && ch == '/') {
			    next_ch();
			    get_token();
			    return;
			}
			else if (ch == 65535) {
			    error("comment not terminated");
			    sy = token.END_PROGRAM;
			    return;
			}
			pch = ch;
		    }
		}
		sy = token.SLASH;
		return;
	    }	    
	    else if (ch == '%') {
		next_ch();
		sy = token.PERCENT;
		return;
	    }
	    else {
		error("invalid character");
		next_ch();
		get_token();
		return;
	    }
	}
    }

    void statement() {
	if (sy == token.SEMICOLON) {
	    polish("empty statement");
	    polish_newline();
	    get_token();
	} else if (sy == token.IF) {
	    polish("if");
	    get_token();
	    if (sy == token.LEFT_PAREN) {
		polish("statement:");
		get_token();
		int if_start = pc;
		expression();
		if (sy == token.RIGHT_PAREN) {
		    polish_newline();
		    polish("  ");
		    get_token();
		    int pc_save1 = pc;
		    emit(operation.FJUMP, 0);
		    statement();
		    if (sy == token.ELSE) {
			int pc_save2 = pc;
			emit(operation.JUMP, pc);
			code[pc_save1].operand = pc;
			polish("else part");
			polish_newline();
			polish("  ");
			get_token();
			statement();
			code[pc_save2].operand = pc;
		    } else {
			code[pc_save1].operand = pc;
		    }
		    polish("end if statement");
		    polish_newline();
		} else {
		    error("right parenthesis expected");
		    get_token();
		}
	    } else {
		error("left parenthesis expected");
		get_token();
	    }
	} else if (sy == token.WHILE) {
	    polish("while");
	    get_token();
	    if (sy == token.LEFT_PAREN) {
		get_token();
		polish("statement:");
		int while_start = pc;
		expression();
		polish_newline();
		if (sy == token.RIGHT_PAREN) {
		    polish("  ");
		    get_token();
		    int pc_save = pc;
		    emit(operation.FJUMP, 0);
		    statement();
		    emit(operation.JUMP, while_start);
		    code[pc_save].operand = pc;
		    polish("end while statement");
		    polish_newline();
		} else {
		    error("right parenthesis expected");
		    get_token();
		}
	    } else {
		error("left parenthesis expected");
		get_token();
	    }
	} else if (sy == token.LEFT_BRACE) {
	    get_token();
	    while (sy != token.RIGHT_BRACE) {
		if (sy == token.END_PROGRAM) {
		    error("too few right braces at end of statement list");
		    return;
		}
		statement();
	    }
	    get_token();
	} else {
	    expression();
	    if (sy == token.SEMICOLON) {
		emit(operation.POPUP, 0);
		get_token();
		polish_newline();
	    } else {
		error("semicolon expected");
		get_token();
	    }
	}		
    }

    void expression() {
	logical_or_expression();
	if (sy == token.EQUAL) {
	    if (code[pc-1].op_code != operation.LOAD) {
		error("assignment to non-variable");
	    }
	    int opsave = code[pc-1].operand;
	    pc--;
	    get_token();
	    expression();
	    emit(operation.STORE, opsave);
	    polish("=");
	}
    }

    void logical_or_expression() {
	logical_and_expression();
	while (sy == token.OROR) {
	    int pc_save = pc;
	    emit(operation.TJUMP, 0);
	    get_token();
	    logical_and_expression();
	    emit(operation.TJUMP, pc+3);
	    emit(operation.LCONST, 0);
	    emit(operation.JUMP, pc+2);
	    emit(operation.LCONST, 1);
	    code[pc_save].operand = pc-1;
	    polish("||");
	}
    }

    void logical_and_expression() {
	bit_or_expression();
	while (sy == token.ANDAND) {
	    int pc_save = pc;
	    emit(operation.FJUMP, 0);
	    get_token();
	    bit_or_expression();
	    emit(operation.FJUMP, pc+3);
	    emit(operation.LCONST, 1);
	    emit(operation.JUMP, pc+2);
	    emit(operation.LCONST, 0);
	    code[pc_save].operand = pc-1;
	    polish("&&");
	}
    }

    void bit_or_expression() {
	bit_and_expression();
	while (sy == token.OR) {
	    get_token();
	    bit_and_expression();
	    polish("|");
	    emit(operation.OROP, 0);
	}
    }

    void bit_and_expression() {
	equality_expression();
	while (sy == token.AND) {
	    get_token();
	    equality_expression();
	    polish("&");
	    emit(operation.ANDOP, 0);
	}
    }

    void equality_expression() {
	relational_expression();
	while (sy == token.EQEQ || sy == token.NOTEQ) {
	    if (sy == token.EQEQ) {
		get_token();
		relational_expression();
		polish("==");
		emit(operation.EQOP, 0);
	    } else {
		get_token();
		relational_expression();
		polish("!=");
		emit(operation.NEOP, 0);
	    }
	}	    
    }

    void relational_expression() {
	additive_expression();
	while (sy == token.LT || sy == token.GT || sy == token.LE || sy == token.GE) {
	    if (sy == token.LT) {
		get_token();
		additive_expression();
		polish("<");
		emit(operation.LTOP, 0);
	    } else if (sy == token.GT) {
		get_token();
		additive_expression();
		polish(">");
		emit(operation.GTOP, 0);
	    } else if (sy == token.LE) {
		get_token();
		additive_expression();
		polish("<=");
		emit(operation.LEOP, 0);
	    } else {
		get_token();
		additive_expression();
		polish(">=");
		emit(operation.GEOP, 0);
	    }
	}
    }

    void additive_expression() {
	multiplicative_expression();
	while (sy == token.PLUS || sy == token.MINUS) {
	    if(sy == token.PLUS) {
		get_token();
		multiplicative_expression();
		polish("+");
		emit(operation.ADD, 0);
	    } else {
		get_token();
		multiplicative_expression();
		polish("-");
		emit(operation.SUB, 0);
	    }
	}
    }

    void multiplicative_expression() {
	unary_expression();
	while (sy == token.STAR || sy == token.SLASH || sy == token.PERCENT) {
	    if (sy == token.STAR) {
		get_token();
		unary_expression();
		polish("*");
		emit(operation.MULT, 0);
	    } else if (sy == token.SLASH) {
		get_token();
		unary_expression();
		polish("/");
		emit(operation.DIV, 0);
	    } else {
		get_token();
		unary_expression();
		polish("%");
		emit(operation.MOD, 0);
	    }
	}
    }

    void unary_expression() {
	if (sy == token.MINUS) {
	    emit(operation.LCONST, 0);
	    get_token();
	    unary_expression();
	    emit(operation.SUB, 0);
	    polish("u-");
	} else {
	    primary_expression();
	}
    }
    void primary_expression() {
	if (sy == token.LITERAL) {
	    polish(literal_value + "");
	    emit(operation.LCONST, literal_value);
	    get_token();
	} else if (sy == token.IDENTIFIER) {
	    get_token();
	    if (sy == token.LEFT_PAREN) {
		String funcname = id_string;
		polish(id_string);
		id_record check = lookup_function(id_string);
		get_token();
		int count = 0;
		if (sy == token.RIGHT_PAREN) {
		    if (check.parameter_count != count) {
			error(funcname + ": number of parameters mismatch");
		    }
		    emit(operation.CALL, check.function_id);
		    polish("call-" + count);
		    get_token();
		} else {
		    expression();
		    count++;
		    while (sy == token.COMMA) {
			get_token();
			expression();
			count++;
		    }
		    if (sy == token.RIGHT_PAREN) {
			if (check.parameter_count != count) {
			    error(funcname + ": number of parameters mismatch");
			}
			emit(operation.CALL, check.function_id);
			polish("call-" + count);
			get_token();
		    } else {
			error(") expected");
		    }
		}
	    } else {
		polish(id_string);/* variable reference */
		id_record check = lookup_variable(id_string);
		emit(operation.LOAD, check.address);
		



		
	    }
	} else if (sy == token.LEFT_PAREN) {
	    get_token();
	    expression();
	    if (sy == token.RIGHT_PAREN) {
		get_token();
	    } else {
		error("right parenthesis expected");
	    }
	} else {
	    error("unrecognized element in expression");
	    get_token();
	}
    }

    final boolean debug_parse = false;
    void polish(String s) {
	if (debug_parse) {
	    System.out.print(s + " ");
	}
    }
    
    void polish_newline() {
	if (debug_parse) {
	    System.out.println();
	}
    }

    id_record search(String name) {
        id_record id;
	id = symbol_table.get(name);
	if (id != null) {
	    return id;
	} else {
	    id_record x = new id_record(type.Variable, variable_count, -1, -1);
	    variable_count++;
	    symbol_table.put(name, x);
	    return x;
	}
    }
    
    id_record lookup_variable(String name) {
	id_record re;
	re = search(name);
	if (re.id_class != type.Variable) {
	    error(name + ": function is used as a variable");
	}
	return re;
    }

    id_record lookup_function(String name) {
	id_record re;
	re = search(name);
	if (re.id_class != type.Function) {
	    error(name + ": variable is used as a function");
	}
	return re;
    }
    
    void init_symbol_table() {
	symbol_table = new TreeMap<String, id_record>();
	variable_count = 0;
	id_record x;
	x = new id_record(type.Function, -1, 0, 0);
	symbol_table.put("getd", x);
	x = new id_record(type.Function, -1, 1, 2);
	symbol_table.put("putd", x);
	x = new id_record(type.Function, -1, 2, 0);
	symbol_table.put("newline", x);
	x = new id_record(type.Function, -1, 3, 1);
	symbol_table.put("putchar", x);
    }
    
    public static void main(String[] args) throws Exception {
	C0 c0_instance = new C0();
	c0_instance.driver(args);
    }

    void driver(String[] args) throws Exception {
	if (args.length == 1) {
	    source = new BufferedReader(new FileReader(new File(args[0])));
	}
	else {
	    source = new BufferedReader(new InputStreamReader(System.in));
	    if (args.length != 0) {
		error("multiple source file is not supported");
	    }
	}
	init_symbol_table();
	line_number = 1;
	ch = ' ';
	get_token();
	statement();
	if (sy != token.END_PROGRAM) {
	    error("extra text at the end of the program");
	}
	emit(operation.HALT, 0);
	/* print_code(); */
	if (error_count == 0) {
	    interpret(false);
	}
    }

    void print_code() {
	for (int i = 0; i < pc; i++) {
	    System.out.println(String.format("%5d", i) + ": " +
			       String.format("%-6s", code[i].op_code) +
			       String.format("%6d", code[i].operand));
	}
    }
    
    void error(String s) {
	error_count++;
	System.out.println(String.format("%4d", line_number) + ": " + s);
    }
}

