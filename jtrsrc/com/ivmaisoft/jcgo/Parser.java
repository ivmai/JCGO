package com.ivmaisoft.jcgo;

public class Parser {
	private static final int maxT = 104;

	private static final boolean T = true;
	private static final boolean x = false;
	private static final int minErrDist = 2;
	private static int errDist = minErrDist;

	static Token token;   // last recognized token
	static Token t;       // lookahead token

	

	public static void Error(int n) {
		if (errDist >= minErrDist) Scanner.err.ParsErr(n, t.line, t.col);
		errDist = 0;
	}

	public static void SemError(String msg) {
		if (errDist >= minErrDist) Scanner.err.SemErr(msg, token.line, token.col);
		errDist = 0;
	}

	public static void SemError(int n) {
		if (errDist >= minErrDist) Scanner.err.SemErr(n, token.line, token.col);
		errDist = 0;
	}

	public static boolean Successful() {
		return Scanner.err.count == 0;
	}

	public static String LexString() {
		return token.str;
	}

	public static String LexName() {
		return token.val;
	}

	public static String LookAheadString() {
		return t.str;
	}

	public static String LookAheadName() {
		return t.val;
	}

	private static void Get() {
		for (;;) {
			token = t;
			t = Scanner.Scan();
			if (t.kind <= maxT) {errDist++; return;}

			t = token;
		}
	}

	private static void Expect(int n) {
		if (t.kind == n) Get(); else Error(n);
	}

	private static boolean StartOf(int s) {
		return set[s][t.kind];
	}

	private static Term UnaryWithIdentTailOrDimExprs(Term a) {
		Term z;
		z = Empty.term;
		if (t.kind == 13) {
			Get();
			z = NewInstOrSuperOrMethodInvoke(a);
		} else if (t.kind == 43) {
			Get();
			z = DimensionExpressionSeq(a);
		} else Error(105);
		return z;
	}

	private static Term ExprBracketOptNewArrayDims(Term b, Term c) {
		Term z;
		Term d, f = null;
		d = JavaExpression();
		Expect(44);
		if (t.kind == 43) {
			f = NewArrayBody(b, new DimsList(c, new DimExpr(d)));
		}
		z = f != null ? f : new ArrayCreation(b, new DimsList(c, new DimExpr(d)));
		
		return z;
	}

	private static Term BracketDimSpecs(Term b, Term c) {
		Term z;
		Term d = Empty.term;
		Expect(44);
		if (t.kind == 43) {
			d = DimSpecSeq();
		}
		z = new ArrayCreation(b, c, new DimSpec(d));
		return z;
	}

	private static Term NewArrayTail(Term b, Term c) {
		Term z;
		z = Empty.term;
		if (t.kind == 44) {
			z = BracketDimSpecs(b, c);
		} else if (StartOf(1)) {
			z = ExprBracketOptNewArrayDims(b, c);
		} else Error(106);
		return z;
	}

	private static Term NewArrayBody(Term b, Term c) {
		Term z;
		Expect(43);
		z = NewArrayTail(b, c);
		return z;
	}

	private static Term ExprBracketOptNewArrayBody(Term b) {
		Term z;
		Term d, f = null;
		d = JavaExpression();
		Expect(44);
		if (t.kind == 43) {
			f = NewArrayBody(b, new DimExpr(d));
		}
		z = f != null ? f : new ArrayCreation(b, new DimExpr(d));
		
		return z;
	}

	private static Term BracketDimsArrayInit(Term b) {
		Term z;
		Term c = Empty.term, d;
		if (t.kind == 43) {
			c = DimSpecSeq();
		}
		d = ArrayInitializer();
		z = new AnonymousArray(b, new DimSpec(c), d);
		return z;
	}

	private static Term NewArrayInstanceTail(Term b) {
		Term z;
		z = Empty.term;
		if (t.kind == 44) {
			Get();
			z = BracketDimsArrayInit(b);
			if (t.kind == 43) {
				Get();
				z = DimensionExpressionSeq(z);
			}
		} else if (StartOf(1)) {
			z = ExprBracketOptNewArrayBody(b);
		} else Error(107);
		return z;
	}

	private static Term ArgumentsOptClassBody(Term b) {
		Term z;
		Term d = Empty.term, f = Empty.term;
		
		if (StartOf(1)) {
			d = ArgumentList();
		}
		Expect(12);
		if (t.kind == 28) {
			f = ClassBody();
		}
		z = new InstanceCreation(b, d, f);
		return z;
	}

	private static Term NewInstanceBody(Term b) {
		Term z;
		z = Empty.term;
		if (t.kind == 11) {
			Get();
			z = ArgumentsOptClassBody(b);
		} else if (t.kind == 43) {
			Get();
			z = NewArrayInstanceTail(b);
		} else Error(108);
		return z;
	}

	private static Term NewPrimArrayInstanceTail() {
		Term z;
		Term b;
		b = PrimitiveType();
		Expect(43);
		z = NewArrayInstanceTail(b);
		return z;
	}

	private static Term QualIdentNewInstanceTail() {
		Term z;
		Term b;
		b = QualifiedIdentifier();
		z = NewInstanceBody(new ClassOrIfaceType(b));
		return z;
	}

	private static Term IdentNewInstanceOrPrimArrTail() {
		Term z;
		z = Empty.term;
		if (t.kind == 1 || t.kind == 7) {
			z = QualIdentNewInstanceTail();
		} else if (StartOf(2)) {
			z = NewPrimArrayInstanceTail();
		} else Error(109);
		return z;
	}

	private static Term UnaryWithNewOrStrBody() {
		Term z;
		z = Empty.term;
		if (t.kind == 102) {
			Get();
			z = IdentNewInstanceOrPrimArrTail();
		} else if (t.kind == 34) {
			Get();
			Expect(13);
			Expect(23);
			z = new ClassLiteral(new PrimitiveType(Type.VOID));
			
		} else if (t.kind == 5) {
			Get();
			z = new StringLiteral(token.val);
		} else Error(110);
		return z;
	}

	private static Term UnaryWithPrimitiveTail(Term a) {
		Term z;
		Term d = null;
		Expect(13);
		Expect(23);
		if (t.kind == 13) {
			d = ThisOptMethodAccessTail(new ClassLiteral(a));
		}
		z = d != null ? d : new ClassLiteral(a);
		
		return z;
	}

	private static Term ExprBrackDimExprsUnaryIndents(Term a) {
		Term z;
		Term c, e = null, f = null, g = null;
		
		c = JavaExpression();
		Expect(44);
		if (t.kind == 43) {
			Get();
			e = DimensionExpressionSeq(new ArrayAccess(new Expression(a), c));
		}
		if (t.kind == 13) {
			f = UnaryWithIdentTailSeq(e != null ? e :
new ArrayAccess(new Expression(a), c));
		}
		if (t.kind == 96 || t.kind == 97) {
			g = IncDecOp();
		}
		z = g != null ? new PostfixOp(f != null ? f : e != null ? e :
		     new ArrayAccess(new Expression(a), c), g) : f != null ? f :
		     e != null ? e : new ArrayAccess(new Expression(a), c);
		
		return z;
	}

	private static Term BracketDimsOptUnaryPrim(Term a) {
		Term z;
		Term c = Empty.term, d = null;
		
		Expect(44);
		if (t.kind == 43) {
			c = DimSpecSeq();
		}
		if (t.kind == 13) {
			d = UnaryWithPrimitiveTail(new TypeWithDims(new ClassOrIfaceType(a),
new DimSpec(c)));
		}
		z = d != null ? d : new TypeWithDims(new ClassOrIfaceType(a),
		     new DimSpec(c));
		
		return z;
	}

	private static Term ClassOrThisOrNewInstCreation(Term a) {
		Term z;
		z = Empty.term;
		if (t.kind == 23) {
			Get();
			z = new ClassLiteral(new ClassOrIfaceType(a));
		} else if (t.kind == 102) {
			Get();
			z = InnerNewInstanceCreation(new Expression(a));
		} else if (t.kind == 103) {
			Get();
			z = new This(new ClassOrIfaceType(a));
		} else Error(111);
		return z;
	}

	private static Term UnaryWithIdentQualified(Term a) {
		Term z;
		Term c, d = null;
		c = Identifier();
		if (StartOf(3)) {
			d = UnaryWithIdentBody(new QualifiedName(a, c));
		}
		z = d != null ? d : new Expression(new QualifiedName(a, c));
		
		return z;
	}

	private static Term UnaryWithIdentDotInstanceTail(Term a) {
		Term z;
		Term c, d = null;
		c = ClassOrThisOrNewInstCreation(a);
		if (t.kind == 13) {
			d = ThisOptMethodAccessTail(c);
		}
		z = d != null ? d : c;
		return z;
	}

	private static Term UnaryWithIdentBracketTail(Term a) {
		Term z;
		z = Empty.term;
		if (t.kind == 44) {
			z = BracketDimsOptUnaryPrim(a);
		} else if (StartOf(1)) {
			z = ExprBrackDimExprsUnaryIndents(a);
		} else Error(112);
		return z;
	}

	private static Term UnaryWithIdentDotTail(Term a) {
		Term z;
		z = Empty.term;
		if (t.kind == 101) {
			Get();
			z = SuperConstrMethodAccess(new Expression(a));
		} else if (t.kind == 23 || t.kind == 102 || t.kind == 103) {
			z = UnaryWithIdentDotInstanceTail(a);
		} else if (t.kind == 1 || t.kind == 7) {
			z = UnaryWithIdentQualified(a);
		} else Error(113);
		return z;
	}

	private static Term UnaryWithIdentArgsBody(Term a) {
		Term z;
		Term c = Empty.term, e = null, f = null, g = null;
		
		if (StartOf(1)) {
			c = ArgumentList();
		}
		Expect(12);
		if (t.kind == 43) {
			Get();
			e = DimensionExpressionSeq(new MethodInvocation(a, c));
		}
		if (t.kind == 13) {
			f = UnaryWithIdentTailSeq(e != null ? e : new MethodInvocation(a, c));
		}
		if (t.kind == 96 || t.kind == 97) {
			g = IncDecOp();
		}
		z = g != null ? new PostfixOp(f != null ? f : e != null ? e :
		     new MethodInvocation(a, c), g) : f != null ? f : e != null ? e :
		     new MethodInvocation(a, c);
		
		return z;
	}

	private static Term UnaryWithIdentBody(Term a) {
		Term z;
		z = Empty.term;
		if (t.kind == 96) {
			Get();
			z = new PostfixOp(new Expression(a),
			     new LexTerm(LexTerm.INCREMENT, token.val));
			
		} else if (t.kind == 97) {
			Get();
			z = new PostfixOp(new Expression(a),
			     new LexTerm(LexTerm.DECREMENT, token.val));
			
		} else if (t.kind == 11) {
			Get();
			z = UnaryWithIdentArgsBody(a);
		} else if (t.kind == 13) {
			Get();
			z = UnaryWithIdentDotTail(a);
		} else if (t.kind == 43) {
			Get();
			z = UnaryWithIdentBracketTail(a);
		} else Error(114);
		return z;
	}

	private static Term ThisOptMethodAccessTail(Term a) {
		Term z;
		Term b, c = null;
		b = UnaryWithIdentTailSeq(a);
		if (t.kind == 96 || t.kind == 97) {
			c = IncDecOp();
		}
		z = c != null ? new PostfixOp(b, c) : b;
		return z;
	}

	private static Term ThisOptConstrMethodAccessTail() {
		Term z;
		z = Empty.term;
		if (t.kind == 11) {
			Get();
			z = ExplicitConstrInvoke(Empty.term, new This());
		} else if (t.kind == 13) {
			z = ThisOptMethodAccessTail(new This());
		} else Error(115);
		return z;
	}

	private static Term InnerSuperConstrInvocation(Term a) {
		Term z;
		Term e = Empty.term;
		Expect(11);
		if (StartOf(1)) {
			e = ArgumentList();
		}
		Expect(12);
		z = new ConstructorCall(a, new Super(), e);
		return z;
	}

	private static Term InnerNewInstanceCreation(Term a) {
		Term z;
		Term d, f = Empty.term, h = Empty.term;
		
		d = Identifier();
		Expect(11);
		if (StartOf(1)) {
			f = ArgumentList();
		}
		Expect(12);
		if (t.kind == 28) {
			h = ClassBody();
		}
		z = new InstanceCreation(a, d, f, h);
		return z;
	}

	private static Term NewInstOrSuperOrMethodInvoke(Term a) {
		Term z;
		z = Empty.term;
		if (t.kind == 23) {
			Get();
			z = new ClassLiteral(new ClassOrIfaceType(a));
		} else if (t.kind == 102) {
			Get();
			z = InnerNewInstanceCreation(a);
		} else if (t.kind == 101) {
			Get();
			z = InnerSuperConstrInvocation(a);
		} else if (t.kind == 1 || t.kind == 7) {
			z = FieldMethodInvocation(a);
		} else Error(116);
		return z;
	}

	private static Term DimensionExpressionSeq(Term a) {
		Term z;
		Term c, e = null;
		c = JavaExpression();
		Expect(44);
		if (t.kind == 43) {
			Get();
			e = DimensionExpressionSeq(new ArrayAccess(a, c));
		}
		z = e != null ? e : new ArrayAccess(a, c);
		
		return z;
	}

	private static Term PrimaryMethodInvoke(Term a, Term c) {
		Term z;
		Term e = Empty.term;
		Expect(11);
		if (StartOf(1)) {
			e = ArgumentList();
		}
		Expect(12);
		z = new MethodInvocation(a, c, e);
		return z;
	}

	private static Term UnaryWithIdentTailSeq(Term a) {
		Term z;
		Term c, d = null;
		Expect(13);
		c = NewInstOrSuperOrMethodInvoke(a);
		if (t.kind == 13) {
			d = UnaryWithIdentTailSeq(c);
		}
		z = d != null ? d : c;
		return z;
	}

	private static Term FieldMethodInvocation(Term a) {
		Term z;
		Term c, d = null, e = null;
		c = Identifier();
		if (t.kind == 11) {
			d = PrimaryMethodInvoke(a, c);
		}
		if (t.kind == 43) {
			Get();
			e = DimensionExpressionSeq(d != null ?
d : new PrimaryFieldAccess(a, c));
		}
		z = e != null ? e : d != null ? d : new PrimaryFieldAccess(a, c);
		
		return z;
	}

	private static Term ArgumentList() {
		Term z;
		Term a, c = null;
		a = JavaExpression();
		if (t.kind == 27) {
			Get();
			c = ArgumentList();
		}
		z = c != null ? (Term) (new ParameterList(new Argument(a), c)) :
		     new Argument(a);
		
		return z;
	}

	private static Term SuperMethodAccessTail(Term a) {
		Term z;
		Term c, d = null, e = null;
		c = FieldMethodInvocation(a);
		if (t.kind == 13) {
			d = UnaryWithIdentTailSeq(c);
		}
		if (t.kind == 96 || t.kind == 97) {
			e = IncDecOp();
		}
		z = e != null ? new PostfixOp(d != null ? d : c, e) : d != null ? d : c;
		
		return z;
	}

	private static Term ExplicitConstrInvoke(Term a, Term c) {
		Term z;
		Term e = Empty.term;
		if (StartOf(1)) {
			e = ArgumentList();
		}
		Expect(12);
		z = new ConstructorCall(a, c, e);
		return z;
	}

	private static Term UnaryWithParaComplexTail(Term a) {
		Term z;
		Term b, c = null, d = null;
		b = UnaryWithIdentTailOrDimExprs(a);
		if (t.kind == 13) {
			c = UnaryWithIdentTailSeq(b);
		}
		if (t.kind == 96 || t.kind == 97) {
			d = IncDecOp();
		}
		z = d != null ? new PostfixOp(c != null ? c : b, d) : c != null ? c : b;
		
		return z;
	}

	private static Term PostfixOptUnaryExpr(Term b) {
		Term z;
		Term d, e = null;
		d = IncDecOp();
		if (StartOf(4)) {
			e = UnaryExpressionTail();
		}
		z = e != null ? (Term) (new CastExpression(b, new PrefixOp(d, e))) :
		     new PostfixOp(new ParenExpression(b), d);
		
		return z;
	}

	private static Term CastPlusMinusUnary(Term b) {
		Term z;
		Term d, e; if (!b.isType()) return null;
		
		d = NegatePlusMinusOp();
		e = UnaryExpression();
		z = new CastExpression(b, new UnaryExpression(d, e));
		
		return z;
	}

	private static Term UnaryWithParaTail(Term b) {
		Term z;
		z = Empty.term; Term d;
		if (StartOf(5)) {
			z = CastPlusMinusUnary(b);
		} else if (t.kind == 96 || t.kind == 97) {
			z = PostfixOptUnaryExpr(b);
		} else if (t.kind == 13 || t.kind == 43) {
			z = UnaryWithParaComplexTail(new ParenExpression(b));
		} else if (StartOf(4)) {
			d = UnaryExpressionTail();
			z = new CastExpression(b, d);
		} else Error(117);
		return z;
	}

	private static Term UnaryWithIdent() {
		Term z;
		Term a, b = null;
		a = Identifier();
		if (StartOf(3)) {
			b = UnaryWithIdentBody(new QualifiedName(a));
		}
		z = b != null ? b : new Expression(new QualifiedName(a));
		
		return z;
	}

	private static Term UnaryWithNewOrStr() {
		Term z;
		Term a, b = null;
		a = UnaryWithNewOrStrBody();
		if (t.kind == 13) {
			b = ThisOptMethodAccessTail(a);
		}
		z = b != null ? b : a;
		return z;
	}

	private static Term UnaryWithPrimitive() {
		Term z;
		Term a, b = null, c = null;
		a = PrimitiveType();
		if (t.kind == 43) {
			b = DimSpecSeq();
		}
		if (t.kind == 13) {
			c = UnaryWithPrimitiveTail(b != null ? new TypeWithDims(a, b) : a);
		}
		z = c != null ? c : b != null ? new TypeWithDims(a, b) : a;
		
		return z;
	}

	private static Term ThisOptConstrMethodAccess() {
		Term z;
		Term b = null;
		Expect(103);
		if (t.kind == 11 || t.kind == 13) {
			b = ThisOptConstrMethodAccessTail();
		}
		z = b != null ? b : new This();
		return z;
	}

	private static Term SuperConstrMethodAccess(Term a) {
		Term z;
		z = Empty.term;
		if (t.kind == 11) {
			Get();
			z = ExplicitConstrInvoke(a, new Super());
		} else if (t.kind == 13) {
			Get();
			z = SuperMethodAccessTail(new Super(a));
		} else Error(118);
		return z;
	}

	private static Term UnaryWithPara() {
		Term z;
		Term b, d = null;
		b = JavaExpression();
		Expect(12);
		if (StartOf(6)) {
			d = UnaryWithParaTail(b);
		}
		z = d != null ? d : new ParenExpression(b);
		return z;
	}

	private static Term UnaryExpressionTail() {
		Term z;
		z = Empty.term;
		switch (t.kind) {
		case 98: {
			Get();
			z = new LexTerm(LexTerm.xNULL, token.val);
			break;
		}
		case 99: {
			Get();
			z = new LexTerm(LexTerm.FALSE, token.val);
			break;
		}
		case 100: {
			Get();
			z = new LexTerm(LexTerm.TRUE, token.val);
			break;
		}
		case 2: {
			Get();
			z = new IntLiteral(token.val);
			break;
		}
		case 3: {
			Get();
			z = new FloatLiteral(token.val);
			break;
		}
		case 4: {
			Get();
			z = new CharacterLiteral(token.val);
			break;
		}
		case 11: {
			Get();
			z = UnaryWithPara();
			break;
		}
		case 101: {
			Get();
			z = SuperConstrMethodAccess(Empty.term);
			break;
		}
		case 103: {
			z = ThisOptConstrMethodAccess();
			break;
		}
		case 35: case 36: case 37: case 38: case 39: case 40: case 41: case 42: {
			z = UnaryWithPrimitive();
			break;
		}
		case 5: case 34: case 102: {
			z = UnaryWithNewOrStr();
			break;
		}
		case 1: case 7: {
			z = UnaryWithIdent();
			break;
		}
		default: Error(119);
		}
		return z;
	}

	private static Term IncDecOp() {
		Term z;
		z = Empty.term;
		if (t.kind == 96) {
			Get();
			z = new LexTerm(LexTerm.INCREMENT, token.val);
		} else if (t.kind == 97) {
			Get();
			z = new LexTerm(LexTerm.DECREMENT, token.val);
		} else Error(120);
		return z;
	}

	private static Term NegatePlusMinusOp() {
		Term z;
		z = Empty.term;
		if (t.kind == 94) {
			Get();
			z = new LexTerm(LexTerm.NOT, token.val);
		} else if (t.kind == 95) {
			Get();
			z = new LexTerm(LexTerm.BITNOT, token.val);
		} else if (t.kind == 66) {
			Get();
			z = new LexTerm(LexTerm.PLUS, token.val);
		} else if (t.kind == 67) {
			Get();
			z = new LexTerm(LexTerm.MINUS, token.val);
		} else Error(121);
		return z;
	}

	private static Term AssignmentOperator() {
		Term z;
		z = Empty.term;
		switch (t.kind) {
		case 46: {
			Get();
			z = new LexTerm(LexTerm.EQUALS, token.val);
			break;
		}
		case 83: {
			Get();
			z = new LexTerm(LexTerm.TIMES_EQUALS, token.val);
			break;
		}
		case 84: {
			Get();
			z = new LexTerm(LexTerm.DIVIDE_EQUALS, token.val);
			break;
		}
		case 85: {
			Get();
			z = new LexTerm(LexTerm.MOD_EQUALS, token.val);
			break;
		}
		case 86: {
			Get();
			z = new LexTerm(LexTerm.PLUS_EQUALS, token.val);
			break;
		}
		case 87: {
			Get();
			z = new LexTerm(LexTerm.MINUS_EQUALS, token.val);
			break;
		}
		case 88: {
			Get();
			z = new LexTerm(LexTerm.SHLEFT_EQUALS, token.val);
			break;
		}
		case 89: {
			Get();
			z = new LexTerm(LexTerm.SHRIGHT_EQUALS, token.val);
			break;
		}
		case 90: {
			Get();
			z = new LexTerm(LexTerm.FLSHIFT_EQUALS, token.val);
			break;
		}
		case 91: {
			Get();
			z = new LexTerm(LexTerm.BITAND_EQUALS, token.val);
			break;
		}
		case 92: {
			Get();
			z = new LexTerm(LexTerm.XOR_EQUALS, token.val);
			break;
		}
		case 93: {
			Get();
			z = new LexTerm(LexTerm.BITOR_EQUALS, token.val);
			break;
		}
		default: Error(122);
		}
		return z;
	}

	private static Term EqualCompareOp() {
		Term z;
		z = Empty.term;
		if (t.kind == 76) {
			Get();
			z = new LexTerm(LexTerm.NE, token.val);
		} else if (t.kind == 77) {
			Get();
			z = new LexTerm(LexTerm.EQ, token.val);
		} else Error(123);
		return z;
	}

	private static Term RelCompareOp() {
		Term z;
		z = Empty.term;
		if (t.kind == 72) {
			Get();
			z = new LexTerm(LexTerm.LE, token.val);
		} else if (t.kind == 73) {
			Get();
			z = new LexTerm(LexTerm.LT, token.val);
		} else if (t.kind == 74) {
			Get();
			z = new LexTerm(LexTerm.GE, token.val);
		} else if (t.kind == 75) {
			Get();
			z = new LexTerm(LexTerm.GT, token.val);
		} else Error(124);
		return z;
	}

	private static Term RelCompareShiftExprSeq(Term a) {
		Term z;
		Term b, c, d = null;
		b = RelCompareOp();
		c = ShiftExpression();
		if (StartOf(7)) {
			d = RelCompareShiftExprSeq(new RelationalOp(a, b, c));
		}
		z = d != null ? d : new RelationalOp(a, b, c);
		
		return z;
	}

	private static Term InstanceOfTail(Term a) {
		Term z;
		Term c, d = Empty.term;
		c = SimpleType();
		if (t.kind == 43) {
			d = DimSpecSeq();
		}
		z = new InstanceOf(a, c, d);
		
		return z;
	}

	private static Term ShiftOp() {
		Term z;
		z = Empty.term;
		if (t.kind == 68) {
			Get();
			z = new LexTerm(LexTerm.SHIFT_LEFT, token.val);
		} else if (t.kind == 69) {
			Get();
			z = new LexTerm(LexTerm.FILLSHIFT_RIGHT, token.val);
		} else if (t.kind == 70) {
			Get();
			z = new LexTerm(LexTerm.SHIFT_RIGHT, token.val);
		} else Error(125);
		return z;
	}

	private static Term PlusMinusOp() {
		Term z;
		z = Empty.term;
		if (t.kind == 66) {
			Get();
			z = new LexTerm(LexTerm.PLUS, token.val);
		} else if (t.kind == 67) {
			Get();
			z = new LexTerm(LexTerm.MINUS, token.val);
		} else Error(126);
		return z;
	}

	private static Term ModMulDivOp() {
		Term z;
		z = Empty.term;
		if (t.kind == 64) {
			Get();
			z = new LexTerm(LexTerm.MOD, token.val);
		} else if (t.kind == 15) {
			Get();
			z = new LexTerm(LexTerm.TIMES, token.val);
		} else if (t.kind == 65) {
			Get();
			z = new LexTerm(LexTerm.DIVIDE, token.val);
		} else Error(127);
		return z;
	}

	private static Term ModMulDivUnaryExprSeq(Term a) {
		Term z;
		Term b, c, d = null;
		b = ModMulDivOp();
		c = UnaryExpression();
		if (t.kind == 15 || t.kind == 64 || t.kind == 65) {
			d = ModMulDivUnaryExprSeq(new BinaryOp(a, b, c));
		}
		z = d != null ? d : new BinaryOp(a, b, c);
		
		return z;
	}

	private static Term UnaryExpression() {
		Term z;
		Term a, b; z = Empty.term;
		if (StartOf(5)) {
			a = NegatePlusMinusOp();
			b = UnaryExpression();
			z = new UnaryExpression(a, b);
			
		} else if (StartOf(8)) {
			z = OptPrefixUnaryExpr();
		} else Error(128);
		return z;
	}

	private static Term PlusMinusMultiplicativeExprSeq(Term a) {
		Term z;
		Term b, c, d = null;
		b = PlusMinusOp();
		c = MultiplicativeExpression();
		if (t.kind == 66 || t.kind == 67) {
			d = PlusMinusMultiplicativeExprSeq(new BinaryOp(a, b, c));
		}
		z = d != null ? d : new BinaryOp(a, b, c);
		
		return z;
	}

	private static Term MultiplicativeExpression() {
		Term z;
		Term a, b = null;
		a = UnaryExpression();
		if (t.kind == 15 || t.kind == 64 || t.kind == 65) {
			b = ModMulDivUnaryExprSeq(a);
		}
		z = b != null ? b : a;
		return z;
	}

	private static Term ShiftAdditiveExprSeq(Term a) {
		Term z;
		Term b, c, d = null;
		b = ShiftOp();
		c = AdditiveExpression();
		if (t.kind == 68 || t.kind == 69 || t.kind == 70) {
			d = ShiftAdditiveExprSeq(new BinaryOp(a, b, c));
		}
		z = d != null ? d : new BinaryOp(a, b, c);
		
		return z;
	}

	private static Term AdditiveExpression() {
		Term z;
		Term a, b = null;
		a = MultiplicativeExpression();
		if (t.kind == 66 || t.kind == 67) {
			b = PlusMinusMultiplicativeExprSeq(a);
		}
		z = b != null ? b : a;
		return z;
	}

	private static Term RelationalExpressionTail(Term a) {
		Term z;
		z = Empty.term;
		if (t.kind == 71) {
			Get();
			z = InstanceOfTail(a);
		} else if (StartOf(7)) {
			z = RelCompareShiftExprSeq(a);
		} else Error(129);
		return z;
	}

	private static Term ShiftExpression() {
		Term z;
		Term a, b = null;
		a = AdditiveExpression();
		if (t.kind == 68 || t.kind == 69 || t.kind == 70) {
			b = ShiftAdditiveExprSeq(a);
		}
		z = b != null ? b : a;
		return z;
	}

	private static Term EqualCompareRelationalExprSeq(Term a) {
		Term z;
		Term b, c, d = null;
		b = EqualCompareOp();
		c = RelationalExpression();
		if (t.kind == 76 || t.kind == 77) {
			d = EqualCompareRelationalExprSeq(new RelationalOp(a, b, c));
		}
		z = d != null ? d : new RelationalOp(a, b, c);
		
		return z;
	}

	private static Term RelationalExpression() {
		Term z;
		Term a, b = null;
		a = ShiftExpression();
		if (StartOf(9)) {
			b = RelationalExpressionTail(a);
		}
		z = b != null ? b : a;
		return z;
	}

	private static Term BitAndEqualityExpressionSeq(Term a) {
		Term z;
		Term b, c, d = null;
		Expect(78);
		b = new LexTerm(LexTerm.BITAND, token.val);
		c = EqualityExpression();
		if (t.kind == 78) {
			d = BitAndEqualityExpressionSeq(new BinaryOp(a, b, c));
		}
		z = d != null ? d : new BinaryOp(a, b, c);
		
		return z;
	}

	private static Term EqualityExpression() {
		Term z;
		Term a, b = null;
		a = RelationalExpression();
		if (t.kind == 76 || t.kind == 77) {
			b = EqualCompareRelationalExprSeq(a);
		}
		z = b != null ? b : a;
		return z;
	}

	private static Term XorBitwiseAndExpressionSeq(Term a) {
		Term z;
		Term b, c, d = null;
		Expect(79);
		b = new LexTerm(LexTerm.XOR, token.val);
		c = BitwiseAndExpression();
		if (t.kind == 79) {
			d = XorBitwiseAndExpressionSeq(new BinaryOp(a, b, c));
		}
		z = d != null ? d : new BinaryOp(a, b, c);
		
		return z;
	}

	private static Term BitwiseAndExpression() {
		Term z;
		Term a, b = null;
		a = EqualityExpression();
		if (t.kind == 78) {
			b = BitAndEqualityExpressionSeq(a);
		}
		z = b != null ? b : a;
		return z;
	}

	private static Term BitOrBitwiseXorExpressionSeq(Term a) {
		Term z;
		Term b, c, d = null;
		Expect(80);
		b = new LexTerm(LexTerm.BITOR, token.val);
		c = BitwiseXorExpression();
		if (t.kind == 80) {
			d = BitOrBitwiseXorExpressionSeq(new BinaryOp(a, b, c));
		}
		z = d != null ? d : new BinaryOp(a, b, c);
		
		return z;
	}

	private static Term BitwiseXorExpression() {
		Term z;
		Term a, b = null;
		a = BitwiseAndExpression();
		if (t.kind == 79) {
			b = XorBitwiseAndExpressionSeq(a);
		}
		z = b != null ? b : a;
		return z;
	}

	private static Term AndBitwiseOrExpressionSeq(Term a) {
		Term z;
		Term b, c, d = null;
		Expect(81);
		b = new LexTerm(LexTerm.AND, token.val);
		c = BitwiseOrExpression();
		if (t.kind == 81) {
			d = AndBitwiseOrExpressionSeq(new CondOrAndOperation(a, b, c));
		}
		z = d != null ? d : new CondOrAndOperation(a, b, c);
		
		return z;
	}

	private static Term BitwiseOrExpression() {
		Term z;
		Term a, b = null;
		a = BitwiseXorExpression();
		if (t.kind == 80) {
			b = BitOrBitwiseXorExpressionSeq(a);
		}
		z = b != null ? b : a;
		return z;
	}

	private static Term OrCondAndExpressionSeq(Term a) {
		Term z;
		Term b, c, d = null;
		Expect(82);
		b = new LexTerm(LexTerm.OR, token.val);
		c = CondAndExpression();
		if (t.kind == 82) {
			d = OrCondAndExpressionSeq(new CondOrAndOperation(a, b, c));
		}
		z = d != null ? d : new CondOrAndOperation(a, b, c);
		
		return z;
	}

	private static Term CondAndExpression() {
		Term z;
		Term a, b = null;
		a = BitwiseOrExpression();
		if (t.kind == 81) {
			b = AndBitwiseOrExpressionSeq(a);
		}
		z = b != null ? b : a;
		return z;
	}

	private static Term ExpressionTail(Term a) {
		Term z;
		z = Empty.term;
		if (t.kind == 58) {
			Get();
			z = CondExprTail(a);
		} else if (StartOf(10)) {
			z = AssignmentOpExpr(a);
		} else Error(130);
		return z;
	}

	private static Term OptForVarExprInitTailSemi(Term a) {
		Term z;
		Term b = null;
		if (StartOf(11)) {
			b = ForVarExprInitTail(a);
		}
		Expect(9);
		z = new ExprStatement(b != null ? b : a);
		return z;
	}

	private static Term ExprOrLabelStmntOrVarDeclTail(Term a) {
		Term z;
		Term c; z = Empty.term;
		if (t.kind == 57) {
			Get();
			c = JavaStatement();
			z = new LabeledStatement(a, c);
		} else if (StartOf(12)) {
			z = OptForVarExprInitTailSemi(a);
		} else Error(131);
		return z;
	}

	private static Term CatchClause() {
		Term z;
		Term c = Empty.term, d, e, h = Empty.term;
		Expect(63);
		Expect(11);
		if (t.kind == 20) {
			c = FinalModifier();
		}
		if (t.kind == 10) {
			AnnotationGroup();
		}
		d = QualifiedIdentifier();
		e = Identifier();
		Expect(12);
		Expect(28);
		if (StartOf(13)) {
			h = BlockStatementSeq();
		}
		Expect(29);
		z = new CatchStatement(c, new ClassOrIfaceType(d),
		     new VariableIdentifier(e), h);
		
		return z;
	}

	private static Term CatchClauseSeq() {
		Term z;
		Term a, b = null;
		a = CatchClause();
		if (t.kind == 63) {
			b = CatchClauseSeq();
		}
		z = b != null ? new CatchSeq(a, b) : a;
		return z;
	}

	private static Term CatchClausesOptFinally(Term b) {
		Term z;
		Term c, e = Empty.term;
		c = CatchClauseSeq();
		if (t.kind == 62) {
			Get();
			e = JavaBlock();
		}
		z = new TryStatement(b, c, e);
		return z;
	}

	private static Term TryStatementTail(Term b) {
		Term z;
		Term d; z = Empty.term;
		if (t.kind == 62) {
			Get();
			d = JavaBlock();
			z = new TryStatement(b, d);
		} else if (t.kind == 63) {
			z = CatchClausesOptFinally(b);
		} else Error(132);
		return z;
	}

	private static Term ConditionalExpression() {
		Term z;
		Term a, b = null;
		a = CondOrExpression();
		if (t.kind == 58) {
			Get();
			b = CondExprTail(a);
		}
		z = b != null ? b : a;
		return z;
	}

	private static Term SwitchBlockStatementGroup() {
		Term z;
		Term b, c = Empty.term; z = Empty.term;
		
		if (t.kind == 60) {
			Get();
			Expect(57);
			if (StartOf(13)) {
				c = BlockStatementSeq();
			}
			z = new CaseStatement(c);
		} else if (t.kind == 61) {
			Get();
			b = ConditionalExpression();
			Expect(57);
			if (StartOf(13)) {
				c = BlockStatementSeq();
			}
			z = new CaseStatement(new Expression(b), c);
			
		} else Error(133);
		return z;
	}

	private static Term SwitchBlockStatementGroupSeq() {
		Term z;
		Term a, b = null;
		a = SwitchBlockStatementGroup();
		if (t.kind == 60 || t.kind == 61) {
			b = SwitchBlockStatementGroupSeq();
		}
		z = b != null ? new Seq(a, b) : a;
		return z;
	}

	private static Term OptPrefixUnaryExpr() {
		Term z;
		Term a = null, b;
		if (t.kind == 96 || t.kind == 97) {
			a = IncDecOp();
		}
		b = UnaryExpressionTail();
		z = a != null ? new PrefixOp(a, b) : b;
		return z;
	}

	private static Term AssignmentOpExpr(Term a) {
		Term z;
		Term b, c;
		b = AssignmentOperator();
		c = JavaExpression();
		z = new Assignment(a, b, c);
		return z;
	}

	private static Term CondExprTail(Term a) {
		Term z;
		Term c, e;
		c = JavaExpression();
		Expect(57);
		e = ConditionalExpression();
		z = new CondExpression(a, c, e);
		return z;
	}

	private static Term StatementExpression() {
		Term z;
		Term a, b = null;
		a = OptPrefixUnaryExpr();
		if (StartOf(10)) {
			b = AssignmentOpExpr(a);
		}
		z = b != null ? b : a;
		return z;
	}

	private static Term CommaExprOrExprTail(Term a) {
		Term z;
		Term c; z = Empty.term;
		if (t.kind == 27) {
			Get();
			c = StatementExpression();
			z = new ExpressionList(a, c);
		} else if (t.kind == 58) {
			Get();
			z = CondExprTail(a);
		} else if (StartOf(10)) {
			z = AssignmentOpExpr(a);
		} else Error(134);
		return z;
	}

	private static Term ForExprOnlyInitTail(Term a) {
		Term z;
		Term a2, c = null;
		a2 = CommaExprOrExprTail(a);
		if (t.kind == 27) {
			Get();
			c = ExpressionList();
		}
		z = c != null ? new ExpressionList(a2, c) : a2;
		
		return z;
	}

	private static Term ForVarExprInitTail(Term a) {
		Term z;
		z = Empty.term; Term b;
		if (t.kind == 1 || t.kind == 7) {
			b = VariableDeclaratorList();
			z = new LocalVariableDecl(a, b);
		} else if (StartOf(14)) {
			z = ForExprOnlyInitTail(a);
		} else Error(135);
		return z;
	}

	private static Term CondOrExpression() {
		Term z;
		Term a, b = null;
		a = CondAndExpression();
		if (t.kind == 82) {
			b = OrCondAndExpressionSeq(a);
		}
		z = b != null ? b : a;
		return z;
	}

	private static Term ForVarExprInit() {
		Term z;
		Term a, b = null;
		if (t.kind == 10) {
			AnnotationGroup();
		}
		a = CondOrExpression();
		if (StartOf(11)) {
			b = ForVarExprInitTail(a);
		}
		z = b != null ? b : a;
		return z;
	}

	private static Term ForFinalInit() {
		Term z;
		Term b, c = Empty.term, d;
		if (t.kind == 10) {
			AnnotationGroup();
		}
		b = SimpleType();
		if (t.kind == 43) {
			c = DimSpecSeq();
		}
		d = VariableDeclaratorList();
		z = new LocalVariableDecl(new AccModifier(AccModifier.FINAL), b, c, d);
		
		return z;
	}

	private static Term ExpressionList() {
		Term z;
		Term a, c = null;
		a = StatementExpression();
		if (t.kind == 27) {
			Get();
			c = ExpressionList();
		}
		z = c != null ? new ExpressionList(a, c) : a;
		return z;
	}

	private static Term ForInit() {
		Term z;
		z = Empty.term;
		if (t.kind == 20) {
			Get();
			z = ForFinalInit();
		} else if (StartOf(15)) {
			z = ForVarExprInit();
		} else Error(136);
		return z;
	}

	private static Term ExprOrLabeledStmntOrVarDecl() {
		Term z;
		Term a;
		a = CondOrExpression();
		z = ExprOrLabelStmntOrVarDeclTail(a);
		return z;
	}

	private static Term WhileStatement() {
		Term z;
		Term c, e;
		Expect(11);
		c = JavaExpression();
		Expect(12);
		e = JavaStatement();
		z = new WhileStatement(c, e);
		return z;
	}

	private static Term TryStatement() {
		Term z;
		Term b;
		b = JavaBlock();
		z = TryStatementTail(b);
		return z;
	}

	private static Term ThrowStatement() {
		Term z;
		Term b;
		b = JavaExpression();
		Expect(9);
		z = new ThrowStatement(b);
		return z;
	}

	private static Term SynchronizedStatement() {
		Term z;
		Term c, e;
		Expect(11);
		c = JavaExpression();
		Expect(12);
		e = JavaBlock();
		z = new SynchroStatement(c, e);
		return z;
	}

	private static Term SwitchStatement() {
		Term z;
		Term c, f = Empty.term;
		Expect(11);
		c = JavaExpression();
		Expect(12);
		Expect(28);
		if (t.kind == 60 || t.kind == 61) {
			f = SwitchBlockStatementGroupSeq();
		}
		Expect(29);
		z = new SwitchStatement(c, f);
		return z;
	}

	private static Term ReturnStatement() {
		Term z;
		Term b = Empty.term;
		if (StartOf(1)) {
			b = JavaExpression();
		}
		Expect(9);
		z = new ReturnStatement(b);
		return z;
	}

	private static Term IfThenOptElseStatement() {
		Term z;
		Term c, e, g = Empty.term;
		Expect(11);
		c = JavaExpression();
		Expect(12);
		e = JavaStatement();
		if (t.kind == 59) {
			Get();
			g = JavaStatement();
		}
		z = new IfThenElse(c, e, g);
		return z;
	}

	private static Term ForStatement() {
		Term z;
		Term c = Empty.term, e = Empty.term, g = Empty.term, i;
		
		Expect(11);
		if (StartOf(16)) {
			c = ForInit();
		}
		Expect(9);
		if (StartOf(1)) {
			e = JavaExpression();
		}
		Expect(9);
		if (StartOf(8)) {
			g = ExpressionList();
		}
		Expect(12);
		i = JavaStatement();
		z = new ForStatement(c, e, g, i);
		return z;
	}

	private static Term DoStatement() {
		Term z;
		Term b, e;
		b = JavaStatement();
		Expect(56);
		Expect(11);
		e = JavaExpression();
		Expect(12);
		Expect(9);
		z = new DoStatement(b, e);
		return z;
	}

	private static Term ContinueStatement() {
		Term z;
		Term b = Empty.term;
		if (t.kind == 1 || t.kind == 7) {
			b = Identifier();
		}
		Expect(9);
		z = new ContinueStatement(b);
		return z;
	}

	private static Term BreakStatement() {
		Term z;
		Term b = Empty.term;
		if (t.kind == 1 || t.kind == 7) {
			b = Identifier();
		}
		Expect(9);
		z = new BreakStatement(b);
		return z;
	}

	private static Term AssertionStatement() {
		Term z;
		Term b, d = Empty.term;
		b = JavaExpression();
		if (t.kind == 57) {
			Get();
			d = JavaExpression();
			d = new Argument(d);
		}
		Expect(9);
		z = new AssertionStatement(b, d);
		return z;
	}

	private static Term AbstractOrStaticOrStrict() {
		Term z;
		z = Empty.term;
		if (t.kind == 21) {
			Get();
			z = new AccModifier(AccModifier.ABSTRACT);
		} else if (t.kind == 19) {
			Get();
			z = new AccModifier(AccModifier.STATIC);
		} else if (t.kind == 22) {
			Get();
			z = new AccModifier(AccModifier.STRICT);
		} else Error(137);
		return z;
	}

	private static Term LocalClassModifiersNoFinal() {
		Term z;
		Term a, b = null;
		a = AbstractOrStaticOrStrict();
		if (t.kind == 20 || t.kind == 21 || t.kind == 22) {
			b = LocalClassModifierSeq();
		}
		z = b != null ? new Seq(a, b) : a;
		return z;
	}

	private static Term LocalClassModifier() {
		Term z;
		z = Empty.term;
		if (t.kind == 20) {
			Get();
			z = new AccModifier(AccModifier.FINAL);
		} else if (t.kind == 21) {
			Get();
			z = new AccModifier(AccModifier.ABSTRACT);
		} else if (t.kind == 22) {
			Get();
			z = new AccModifier(AccModifier.STRICT);
		} else Error(138);
		return z;
	}

	private static Term LocalClassModifierSeq() {
		Term z;
		Term a, b = null;
		a = LocalClassModifier();
		if (t.kind == 20 || t.kind == 21 || t.kind == 22) {
			b = LocalClassModifierSeq();
		}
		z = b != null ? new Seq(a, b) : a;
		return z;
	}

	private static Term FinalLocalVarDeclTail() {
		Term z;
		Term b, c = Empty.term, d;
		b = SimpleType();
		if (t.kind == 43) {
			c = DimSpecSeq();
		}
		d = VariableDeclaratorList();
		Expect(9);
		z = new ExprStatement(new LocalVariableDecl(
		     new AccModifier(AccModifier.FINAL), b, c, d));
		
		return z;
	}

	private static Term OptModifiersLocalClassDecl() {
		Term z;
		Term a = null, b;
		if (t.kind == 20 || t.kind == 21 || t.kind == 22) {
			a = LocalClassModifierSeq();
		}
		Expect(23);
		b = ClassDeclaration();
		z = new TypeDeclaration(a != null ?
		     new Seq(new AccModifier(AccModifier.FINAL), a) :
		     (Term) (new AccModifier(AccModifier.FINAL)), b);
		
		return z;
	}

	private static Term JavaStatement() {
		Term z;
		z = Empty.term;
		switch (t.kind) {
		case 9: {
			Get();
			z = new ExprStatement();
			break;
		}
		case 28: {
			z = JavaBlock();
			break;
		}
		case 7: {
			Get();
			z = AssertionStatement();
			break;
		}
		case 47: {
			Get();
			z = BreakStatement();
			break;
		}
		case 48: {
			Get();
			z = ContinueStatement();
			break;
		}
		case 49: {
			Get();
			z = DoStatement();
			break;
		}
		case 50: {
			Get();
			z = ForStatement();
			break;
		}
		case 51: {
			Get();
			z = IfThenOptElseStatement();
			break;
		}
		case 52: {
			Get();
			z = ReturnStatement();
			break;
		}
		case 53: {
			Get();
			z = SwitchStatement();
			break;
		}
		case 30: {
			Get();
			z = SynchronizedStatement();
			break;
		}
		case 54: {
			Get();
			z = ThrowStatement();
			break;
		}
		case 55: {
			Get();
			z = TryStatement();
			break;
		}
		case 56: {
			Get();
			z = WhileStatement();
			break;
		}
		case 1: case 2: case 3: case 4: case 5: /* case 7: */ case 11: case 34: case 35: case 36: case 37: case 38: case 39: case 40: case 41: case 42: case 66: case 67: case 94: case 95: case 96: case 97: case 98: case 99: case 100: case 101: case 102: case 103: {
			z = ExprOrLabeledStmntOrVarDecl();
			break;
		}
		default: Error(139);
		}
		return z;
	}

	private static Term ModifiersLocClassDeclNoFinal() {
		Term z;
		Term a = Empty.term, b;
		if (t.kind == 19 || t.kind == 21 || t.kind == 22) {
			a = LocalClassModifiersNoFinal();
		}
		Expect(23);
		b = ClassDeclaration();
		z = new TypeDeclaration(a, b);
		return z;
	}

	private static Term FinalClsDeclOrVarDeclStmtTail() {
		Term z;
		z = Empty.term;
		if (StartOf(17)) {
			z = OptModifiersLocalClassDecl();
		} else if (StartOf(18)) {
			z = FinalLocalVarDeclTail();
		} else Error(140);
		return z;
	}

	private static Term VariableDeclarator() {
		Term z;
		Term a, b = Empty.term, d = Empty.term;
		a = Identifier();
		if (t.kind == 43) {
			b = DimSpecSeq();
		}
		if (t.kind == 46) {
			Get();
			d = VariableInitializer();
		}
		z = new VariableDeclarator(new VariableIdentifier(a), b, d);
		
		return z;
	}

	private static Term ArrayInitializerList() {
		Term z;
		Term a, c = null;
		a = VariableInitializer();
		if (t.kind == 27) {
			Get();
			if (StartOf(19)) {
				c = ArrayInitializerList();
			}
		}
		z = c != null ? (Term) (new VarInitializers(new ArrElementInit(a), c)) :
		     new ArrElementInit(a);
		
		return z;
	}

	private static Term JavaExpression() {
		Term z;
		Term a, b = null;
		a = CondOrExpression();
		if (StartOf(20)) {
			b = ExpressionTail(a);
		}
		z = new Expression(b != null ? b : a);
		return z;
	}

	private static Term ArrayInitializer() {
		Term z;
		Term b = Empty.term;
		Expect(28);
		if (StartOf(19)) {
			b = ArrayInitializerList();
		}
		Expect(29);
		z = new ArrayInitializer(b);
		return z;
	}

	private static Term VariableDeclaratorList() {
		Term z;
		Term a, c = null;
		a = VariableDeclarator();
		if (t.kind == 27) {
			Get();
			c = VariableDeclaratorList();
		}
		z = c != null ? new VariableDeclareList(a, c) : a;
		
		return z;
	}

	private static Term VariableInitializer() {
		Term z;
		z = Empty.term;
		if (t.kind == 28) {
			z = ArrayInitializer();
		} else if (StartOf(1)) {
			z = JavaExpression();
		} else Error(141);
		return z;
	}

	private static Term FieldDeclTail(Term a, Term b, Term c) {
		Term z;
		Term d = Empty.term, f = Empty.term, h = null;
		
		if (t.kind == 43) {
			d = DimSpecSeq();
		}
		if (t.kind == 46) {
			Get();
			f = VariableInitializer();
		}
		if (t.kind == 27) {
			Get();
			h = VariableDeclaratorList();
		}
		Expect(9);
		z = new FieldDeclaration(a, b, h != null ?
		     (Term) (new VariableDeclareList(new VariableDeclarator(
		     new VariableIdentifier(c), d, f), h)) :
		     new VariableDeclarator(new VariableIdentifier(c), d, f));
		
		return z;
	}

	private static Term MethodDeclTail(Term a, Term b, Term c) {
		Term z;
		Term e = Empty.term, g = Empty.term, h = Empty.term, i;
		
		Expect(11);
		if (StartOf(21)) {
			e = FormalParamList();
		}
		Expect(12);
		if (t.kind == 43) {
			g = DimSpecSeq();
		}
		if (t.kind == 45) {
			h = ThrowsDeclaration();
		}
		i = SemiOrBlock();
		z = new MethodDeclaration(a, b, c, e, g, h, i);
		return z;
	}

	private static Term MethodDeclOrFieldDeclTail(Term a, Term b, Term c) {
		Term z;
		z = Empty.term;
		
		if (t.kind == 11) {
			z = MethodDeclTail(a, b, c);
		} else if (StartOf(22)) {
			z = FieldDeclTail(a, b, c);
		} else Error(142);
		return z;
	}

	private static Term MethodDeclOrFieldDeclBody(Term a) {
		Term z;
		Term a2 = Empty.term, b = Empty.term, c;
		
		if (t.kind == 13) {
			Get();
			a2 = QualifiedIdentifier();
		}
		if (t.kind == 43) {
			b = DimSpecSeq();
		}
		c = Identifier();
		z = MethodDeclOrFieldDeclTail(new ClassOrIfaceType(new QualifiedName(a, a2)), b, c);
		return z;
	}

	private static Term ConstructorDeclBody(Term a) {
		Term z;
		Term c = Empty.term, e = Empty.term, g = Empty.term;
		
		Expect(11);
		if (StartOf(21)) {
			c = FormalParamList();
		}
		Expect(12);
		if (t.kind == 45) {
			e = ThrowsDeclaration();
		}
		Expect(28);
		if (StartOf(13)) {
			g = BlockStatementSeq();
		}
		Expect(29);
		z = new ConstrDeclaration(a, c, e, g);
		return z;
	}

	private static Term ConstrOrMethodOrFieldDeclBody(Term a) {
		Term z;
		z = Empty.term;
		if (t.kind == 11) {
			z = ConstructorDeclBody(a);
		} else if (StartOf(23)) {
			z = MethodDeclOrFieldDeclBody(a);
		} else Error(143);
		return z;
	}

	private static Term PrimitiveType() {
		Term z;
		z = Empty.term;
		switch (t.kind) {
		case 35: {
			Get();
			z = new PrimitiveType(Type.BOOLEAN);
			break;
		}
		case 36: {
			Get();
			z = new PrimitiveType(Type.BYTE);
			break;
		}
		case 37: {
			Get();
			z = new PrimitiveType(Type.CHAR);
			break;
		}
		case 38: {
			Get();
			z = new PrimitiveType(Type.SHORT);
			break;
		}
		case 39: {
			Get();
			z = new PrimitiveType(Type.INT);
			break;
		}
		case 40: {
			Get();
			z = new PrimitiveType(Type.LONG);
			break;
		}
		case 41: {
			Get();
			z = new PrimitiveType(Type.FLOAT);
			break;
		}
		case 42: {
			Get();
			z = new PrimitiveType(Type.DOUBLE);
			break;
		}
		default: Error(144);
		}
		return z;
	}

	private static Term DimSpecSeq() {
		Term z;
		Term c = Empty.term;
		Expect(43);
		Expect(44);
		if (t.kind == 43) {
			c = DimSpecSeq();
		}
		z = new DimSpec(c);
		return z;
	}

	private static Term SimpleType() {
		Term z;
		z = Empty.term; Term a;
		if (t.kind == 1 || t.kind == 7) {
			a = QualifiedIdentifier();
			z = new ClassOrIfaceType(a);
		} else if (StartOf(2)) {
			z = PrimitiveType();
		} else Error(145);
		return z;
	}

	private static Term FinalModifier() {
		Term z;
		Expect(20);
		z = new AccModifier(AccModifier.FINAL);
		return z;
	}

	private static Term FormalParam() {
		Term z;
		Term a = Empty.term, b, c = Empty.term, d, e = Empty.term;
		
		if (t.kind == 20) {
			a = FinalModifier();
		}
		if (t.kind == 10) {
			AnnotationGroup();
		}
		b = SimpleType();
		if (t.kind == 43) {
			c = DimSpecSeq();
		}
		d = Identifier();
		if (t.kind == 43) {
			e = DimSpecSeq();
		}
		z = new FormalParameter(a, b, c, new VariableIdentifier(d), e);
		
		return z;
	}

	private static Term SemiOrBlock() {
		Term z;
		z = Empty.term;
		if (t.kind == 9) {
			Get();
			z = new Block();
		} else if (t.kind == 28) {
			z = JavaBlock();
		} else Error(146);
		return z;
	}

	private static Term ThrowsDeclaration() {
		Term z;
		Expect(45);
		z = ClassTypeList();
		return z;
	}

	private static Term FormalParamList() {
		Term z;
		Term a, c = null;
		a = FormalParam();
		if (t.kind == 27) {
			Get();
			c = FormalParamList();
		}
		z = c != null ? new FormalParamList(a, c) : a;
		
		return z;
	}

	private static Term ExtendsInterfaceTypes() {
		Term z;
		Expect(25);
		z = ClassTypeList();
		return z;
	}

	private static Term BlockStatement() {
		Term z;
		z = Empty.term;
		if (t.kind == 20) {
			Get();
			z = FinalClsDeclOrVarDeclStmtTail();
		} else if (StartOf(24)) {
			z = ModifiersLocClassDeclNoFinal();
		} else if (StartOf(25)) {
			z = JavaStatement();
		} else Error(147);
		return z;
	}

	private static Term BlockStatementSeq() {
		Term z;
		Term a, b = null;
		a = BlockStatement();
		if (StartOf(13)) {
			b = BlockStatementSeq();
		}
		z = b != null ? new Seq(a, b) : a;
		return z;
	}

	private static Term PrimitiveMethodFieldDecl() {
		Term z;
		Term a, b = Empty.term, c;
		a = PrimitiveType();
		if (t.kind == 43) {
			b = DimSpecSeq();
		}
		c = Identifier();
		z = MethodDeclOrFieldDeclTail(a, b, c);
		return z;
	}

	private static Term ConstrMethodFieldDecl() {
		Term z;
		Term a;
		a = Identifier();
		z = ConstrOrMethodOrFieldDeclBody(a);
		return z;
	}

	private static Term VoidMethodDecl() {
		Term z;
		Term b, d = Empty.term, f = Empty.term, g;
		b = Identifier();
		Expect(11);
		if (StartOf(21)) {
			d = FormalParamList();
		}
		Expect(12);
		if (t.kind == 45) {
			f = ThrowsDeclaration();
		}
		g = SemiOrBlock();
		z = new MethodDeclaration(new PrimitiveType(Type.VOID), b, d, f, g);
		
		return z;
	}

	private static Term JavaBlock() {
		Term z;
		Term b = Empty.term;
		Expect(28);
		if (StartOf(13)) {
			b = BlockStatementSeq();
		}
		Expect(29);
		z = new Block(b);
		return z;
	}

	private static Term AccModifier() {
		Term z;
		z = Empty.term;
		switch (t.kind) {
		case 16: {
			Get();
			z = new AccModifier(AccModifier.PUBLIC);
			break;
		}
		case 17: {
			Get();
			z = new AccModifier(AccModifier.PRIVATE);
			break;
		}
		case 18: {
			Get();
			z = new AccModifier(AccModifier.PROTECTED);
			break;
		}
		case 19: {
			Get();
			z = new AccModifier(AccModifier.STATIC);
			break;
		}
		case 20: {
			Get();
			z = new AccModifier(AccModifier.FINAL);
			break;
		}
		case 30: {
			Get();
			z = new AccModifier(AccModifier.SYNCHRONIZED);
			break;
		}
		case 31: {
			Get();
			z = new AccModifier(AccModifier.VOLATILE);
			break;
		}
		case 32: {
			Get();
			z = new AccModifier(AccModifier.TRANSIENT);
			break;
		}
		case 33: {
			Get();
			z = new AccModifier(AccModifier.NATIVE);
			break;
		}
		case 21: {
			Get();
			z = new AccModifier(AccModifier.ABSTRACT);
			break;
		}
		case 22: {
			Get();
			z = new AccModifier(AccModifier.STRICT);
			break;
		}
		case 10: {
			Annotation();
			break;
		}
		default: Error(148);
		}
		return z;
	}

	private static Term MemberDecl() {
		Term z;
		z = Empty.term; Term a;
		switch (t.kind) {
		case 28: {
			a = JavaBlock();
			z = new StaticInitializer(a);
			break;
		}
		case 23: {
			Get();
			z = ClassDeclaration();
			break;
		}
		case 24: {
			Get();
			z = InterfaceDeclaration();
			break;
		}
		case 34: {
			Get();
			z = VoidMethodDecl();
			break;
		}
		case 1: case 7: {
			z = ConstrMethodFieldDecl();
			break;
		}
		case 35: case 36: case 37: case 38: case 39: case 40: case 41: case 42: {
			z = PrimitiveMethodFieldDecl();
			break;
		}
		default: Error(149);
		}
		return z;
	}

	private static Term ModifierSeq() {
		Term z;
		Term a, b = null;
		a = AccModifier();
		if (StartOf(26)) {
			b = ModifierSeq();
		}
		z = b != null ? new Seq(a, b) : a;
		return z;
	}

	private static Term ClassBodyDecl() {
		Term z;
		Term a = Empty.term, b;
		if (StartOf(26)) {
			a = ModifierSeq();
		}
		b = MemberDecl();
		z = new TypeDeclaration(a, b);
		return z;
	}

	private static Term SemiOrClassBodyDecl() {
		Term z;
		z = Empty.term;
		if (t.kind == 9) {
			Get();
		} else if (StartOf(27)) {
			z = ClassBodyDecl();
		} else Error(150);
		return z;
	}

	private static Term SemiOrClassBodyDeclSeq() {
		Term z;
		Term a, b = null;
		a = SemiOrClassBodyDecl();
		if (StartOf(28)) {
			b = SemiOrClassBodyDeclSeq();
		}
		z = b != null ? new Seq(a, b) : a;
		return z;
	}

	private static Term ClassTypeList() {
		Term z;
		Term a, c = null;
		a = QualifiedIdentifier();
		if (t.kind == 27) {
			Get();
			c = ClassTypeList();
		}
		z = c != null ? (Term) (new Seq(new ClassOrIfaceType(a), c)) :
		     new ClassOrIfaceType(a);
		
		return z;
	}

	private static Term ClassBody() {
		Term z;
		Term b = Empty.term;
		Expect(28);
		if (StartOf(28)) {
			b = SemiOrClassBodyDeclSeq();
		}
		Expect(29);
		z = new Seq(b, Empty.term);
		return z;
	}

	private static Term ImplementsTypes() {
		Term z;
		Expect(26);
		z = ClassTypeList();
		return z;
	}

	private static Term ExtendsType() {
		Term z;
		Term b;
		Expect(25);
		b = QualifiedIdentifier();
		z = new ClassOrIfaceType(b);
		return z;
	}

	private static Term InterfaceDeclaration() {
		Term z;
		Term b, c = Empty.term, d;
		b = Identifier();
		if (t.kind == 25) {
			c = ExtendsInterfaceTypes();
		}
		d = ClassBody();
		z = new IfaceDeclaration(b, c, d);
		return z;
	}

	private static Term ClassDeclaration() {
		Term z;
		Term b, c = Empty.term, d = Empty.term, e;
		b = Identifier();
		if (t.kind == 25) {
			c = ExtendsType();
		}
		if (t.kind == 26) {
			d = ImplementsTypes();
		}
		e = ClassBody();
		z = new ClassDeclaration(b, c, d, e);
		return z;
	}

	private static Term ClassModifier() {
		Term z;
		z = Empty.term;
		switch (t.kind) {
		case 16: {
			Get();
			z = new AccModifier(AccModifier.PUBLIC);
			break;
		}
		case 17: {
			Get();
			z = new AccModifier(AccModifier.PRIVATE);
			break;
		}
		case 18: {
			Get();
			z = new AccModifier(AccModifier.PROTECTED);
			break;
		}
		case 19: {
			Get();
			z = new AccModifier(AccModifier.STATIC);
			break;
		}
		case 20: {
			Get();
			z = new AccModifier(AccModifier.FINAL);
			break;
		}
		case 21: {
			Get();
			z = new AccModifier(AccModifier.ABSTRACT);
			break;
		}
		case 22: {
			Get();
			z = new AccModifier(AccModifier.STRICT);
			break;
		}
		case 10: {
			Annotation();
			break;
		}
		default: Error(151);
		}
		return z;
	}

	private static Term ClassDeclOrInterfaceDecl() {
		Term z;
		z = Empty.term;
		if (t.kind == 23) {
			Get();
			z = ClassDeclaration();
		} else if (t.kind == 24) {
			Get();
			z = InterfaceDeclaration();
		} else Error(152);
		return z;
	}

	private static Term ClassModifierSeq() {
		Term z;
		Term a, b = null;
		a = ClassModifier();
		if (StartOf(29)) {
			b = ClassModifierSeq();
		}
		z = b != null ? new Seq(a, b) : a;
		return z;
	}

	private static Term ClassInterfaceDeclaration() {
		Term z;
		Term a = Empty.term, b;
		if (StartOf(29)) {
			a = ClassModifierSeq();
		}
		b = ClassDeclOrInterfaceDecl();
		z = new TypeDeclaration(a, b);
		return z;
	}

	private static Term TypeDeclaration() {
		Term z;
		z = Empty.term;
		if (t.kind == 9) {
			Get();
		} else if (StartOf(30)) {
			z = ClassInterfaceDeclaration();
		} else Error(153);
		return z;
	}

	private static Term StarOrIdentOptImportDeclSpec() {
		Term z;
		z = Empty.term;
		if (t.kind == 15) {
			Get();
			z = new LexTerm(LexTerm.TIMES, token.val);
		} else if (t.kind == 1 || t.kind == 7) {
			z = IdentOptImportDeclSpec();
		} else Error(154);
		return z;
	}

	private static Term IdentOptImportDeclSpec() {
		Term z;
		Term a, c = Empty.term;
		a = Identifier();
		if (t.kind == 13) {
			Get();
			c = StarOrIdentOptImportDeclSpec();
		}
		z = new QualifiedName(a, c);
		return z;
	}

	private static Term ImportDeclaration() {
		Term z;
		Term b;
		Expect(14);
		b = IdentOptImportDeclSpec();
		Expect(9);
		z = new ImportDeclaration(b);
		return z;
	}

	private static Term QualifiedIdentifierOrString() {
		Term z;
		z = Empty.term;
		if (t.kind == 5) {
			Get();
			z = new StringLiteral(token.val);
		} else if (t.kind == 1 || t.kind == 7) {
			z = QualifiedIdentifier();
		} else Error(155);
		return z;
	}

	private static void Annotation() {
		Expect(10);
		QualifiedIdentifier();
		if (t.kind == 11) {
			Get();
			QualifiedIdentifierOrString();
			Expect(12);
		}
	}

	private static void AnnotationGroup() {
		Annotation();
		if (t.kind == 10) {
			AnnotationGroup();
		}
	}

	private static Term QualifiedIdentifier() {
		Term z;
		Term a, c = Empty.term;
		a = Identifier();
		if (t.kind == 13) {
			Get();
			c = QualifiedIdentifier();
		}
		z = new QualifiedName(a, c);
		return z;
	}

	private static Term Identifier() {
		Term z;
		z = Empty.term;
		if (t.kind == 7) {
			Get();
			z = new LexTerm(LexTerm.ID, token.val);
		} else if (t.kind == 1) {
			Get();
			z = new LexTerm(LexTerm.ID, token.val);
		} else Error(156);
		return z;
	}

	private static Term TypeDeclarationSeq() {
		Term z;
		Term a, b = null;
		a = TypeDeclaration();
		if (StartOf(31)) {
			b = TypeDeclarationSeq();
		}
		z = b != null ? new Seq(a, b) : a;
		return z;
	}

	private static Term ImportDeclarationSeq() {
		Term z;
		Term a, b = null;
		a = ImportDeclaration();
		if (t.kind == 14) {
			b = ImportDeclarationSeq();
		}
		z = b != null ? new Seq(a, b) : a;
		return z;
	}

	private static Term PackageSpecifier() {
		Term z;
		Term b;
		Expect(8);
		b = QualifiedIdentifier();
		Expect(9);
		z = new PackageDeclaration(b);
		return z;
	}

	private static void comivmaisoftjcgo() {
		Term a = Empty.term, b = Empty.term, c;
		if (t.kind == 8) {
			a = PackageSpecifier();
		}
		if (t.kind == 14) {
			b = ImportDeclarationSeq();
		}
		c = TypeDeclarationSeq();
		if (t.kind == 6) {
			Get();
		}
		Expect(0);
		new CompilationUnit(a, b, c);
	}



	static void Parse() {
		t = new Token();
		Get();
		comivmaisoftjcgo();

	}

	private static boolean[][] set = {
	{T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x,
	 x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x},
	{x,T,T,T, T,T,x,T, x,x,x,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,T,T, T,T,T,T, T,T,T,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x,
	 x,x,x,x, x,x,T,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,T,T, T,T,T,T, T,T,T,T, x,x},
	{x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,T, T,T,T,T, T,T,T,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x,
	 x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x},
	{x,x,x,x, x,x,x,x, x,x,x,T, x,T,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x,
	 x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, T,T,x,x, x,x,x,x, x,x},
	{x,T,T,T, T,T,x,T, x,x,x,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,T,T, T,T,T,T, T,T,T,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x,
	 x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,T,T, T,T,T,T, x,x},
	{x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x,
	 x,x,x,x, x,x,T,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,T,T, x,x,x,x, x,x,x,x, x,x},
	{x,T,T,T, T,T,x,T, x,x,x,T, x,T,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,T,T, T,T,T,T, T,T,T,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x,
	 x,x,x,x, x,x,T,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,T,T, T,T,T,T, T,T,T,T, x,x},
	{x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x,
	 x,x,x,x, x,x,x,x, x,x,x,x, T,T,T,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x},
	{x,T,T,T, T,T,x,T, x,x,x,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,T,T, T,T,T,T, T,T,T,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x,
	 x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, T,T,T,T, T,T,T,T, x,x},
	{x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x,
	 x,x,x,x, x,x,x,x, x,x,x,T, T,T,T,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x},
	{x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,T,x, x,x,x,x, x,x,x,x, x,x,x,x,
	 x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,T, T,T,T,T, T,T,T,T, T,T,x,x, x,x,x,x, x,x,x,x, x,x},
	{x,T,x,x, x,x,x,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,T,x, x,x,x,x, x,x,x,x, x,x,T,x,
	 x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,T, T,T,T,T, T,T,T,T, T,T,x,x, x,x,x,x, x,x,x,x, x,x},
	{x,T,x,x, x,x,x,T, x,T,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,T,x, x,x,x,x, x,x,x,x, x,x,T,x,
	 x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,T, T,T,T,T, T,T,T,T, T,T,x,x, x,x,x,x, x,x,x,x, x,x},
	{x,T,T,T, T,T,x,T, x,T,x,T, x,x,x,x, x,x,x,T, T,T,T,T, x,x,x,x, T,x,T,x, x,x,T,T, T,T,T,T, T,T,T,x, x,x,x,T, T,T,T,T, T,T,T,T, T,x,x,x,
	 x,x,x,x, x,x,T,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,T,T, T,T,T,T, T,T,T,T, x,x},
	{x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,T,x, x,x,x,x, x,x,x,x, x,x,T,x,
	 x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,T, T,T,T,T, T,T,T,T, T,T,x,x, x,x,x,x, x,x,x,x, x,x},
	{x,T,T,T, T,T,x,T, x,x,T,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,T,T, T,T,T,T, T,T,T,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x,
	 x,x,x,x, x,x,T,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,T,T, T,T,T,T, T,T,T,T, x,x},
	{x,T,T,T, T,T,x,T, x,x,T,T, x,x,x,x, x,x,x,x, T,x,x,x, x,x,x,x, x,x,x,x, x,x,T,T, T,T,T,T, T,T,T,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x,
	 x,x,x,x, x,x,T,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,T,T, T,T,T,T, T,T,T,T, x,x},
	{x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, T,T,T,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x,
	 x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x},
	{x,T,x,x, x,x,x,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,T, T,T,T,T, T,T,T,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x,
	 x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x},
	{x,T,T,T, T,T,x,T, x,x,x,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, T,x,x,x, x,x,T,T, T,T,T,T, T,T,T,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x,
	 x,x,x,x, x,x,T,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,T,T, T,T,T,T, T,T,T,T, x,x},
	{x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,T,x, x,x,x,x, x,x,x,x, x,x,T,x,
	 x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,T, T,T,T,T, T,T,T,T, T,T,x,x, x,x,x,x, x,x,x,x, x,x},
	{x,T,x,x, x,x,x,T, x,x,T,x, x,x,x,x, x,x,x,x, T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,T, T,T,T,T, T,T,T,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x,
	 x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x},
	{x,x,x,x, x,x,x,x, x,T,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,T, x,x,T,x, x,x,x,x, x,x,x,x, x,x,x,x,
	 x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x},
	{x,T,x,x, x,x,x,T, x,x,x,x, x,T,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x,
	 x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x},
	{x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,T, x,T,T,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x,
	 x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x},
	{x,T,T,T, T,T,x,T, x,T,x,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, T,x,T,x, x,x,T,T, T,T,T,T, T,T,T,x, x,x,x,T, T,T,T,T, T,T,T,T, T,x,x,x,
	 x,x,x,x, x,x,T,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,T,T, T,T,T,T, T,T,T,T, x,x},
	{x,x,x,x, x,x,x,x, x,x,T,x, x,x,x,x, T,T,T,T, T,T,T,x, x,x,x,x, x,x,T,T, T,T,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x,
	 x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x},
	{x,T,x,x, x,x,x,T, x,x,T,x, x,x,x,x, T,T,T,T, T,T,T,T, T,x,x,x, T,x,T,T, T,T,T,T, T,T,T,T, T,T,T,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x,
	 x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x},
	{x,T,x,x, x,x,x,T, x,T,T,x, x,x,x,x, T,T,T,T, T,T,T,T, T,x,x,x, T,x,T,T, T,T,T,T, T,T,T,T, T,T,T,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x,
	 x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x},
	{x,x,x,x, x,x,x,x, x,x,T,x, x,x,x,x, T,T,T,T, T,T,T,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x,
	 x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x},
	{x,x,x,x, x,x,x,x, x,x,T,x, x,x,x,x, T,T,T,T, T,T,T,T, T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x,
	 x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x},
	{x,x,x,x, x,x,x,x, x,T,T,x, x,x,x,x, T,T,T,T, T,T,T,T, T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x,
	 x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x}

	};
}
