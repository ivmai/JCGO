package com.ivmaisoft.jcgo;

public class ErrorStream {

	int count;  // number of errors detected
	public String fileName;

	ErrorStream() {
		count = 0;
	}

	void StoreError(int n, int line, int col, String s) {
		System.out.println(fileName + " (" + line + ", " + col + ") " + s);
	}

	void ParsErr(int n, int line, int col) {
		String s;
		count++;
		switch (n) {
			case 0: {s = "EOF expected"; break;}
			case 1: {s = "IDENTIFIER expected"; break;}
			case 2: {s = "INTEGERLITERAL expected"; break;}
			case 3: {s = "FLOATLITERAL expected"; break;}
			case 4: {s = "CHARLITERAL expected"; break;}
			case 5: {s = "STRINGLITERAL expected"; break;}
			case 6: {s = "SUBEOF expected"; break;}
			case 7: {s = "\"assert\" expected"; break;}
			case 8: {s = "\"package\" expected"; break;}
			case 9: {s = "\";\" expected"; break;}
			case 10: {s = "\"@\" expected"; break;}
			case 11: {s = "\"(\" expected"; break;}
			case 12: {s = "\")\" expected"; break;}
			case 13: {s = "\".\" expected"; break;}
			case 14: {s = "\"import\" expected"; break;}
			case 15: {s = "\"*\" expected"; break;}
			case 16: {s = "\"public\" expected"; break;}
			case 17: {s = "\"private\" expected"; break;}
			case 18: {s = "\"protected\" expected"; break;}
			case 19: {s = "\"static\" expected"; break;}
			case 20: {s = "\"final\" expected"; break;}
			case 21: {s = "\"abstract\" expected"; break;}
			case 22: {s = "\"strictfp\" expected"; break;}
			case 23: {s = "\"class\" expected"; break;}
			case 24: {s = "\"interface\" expected"; break;}
			case 25: {s = "\"extends\" expected"; break;}
			case 26: {s = "\"implements\" expected"; break;}
			case 27: {s = "\",\" expected"; break;}
			case 28: {s = "\"{\" expected"; break;}
			case 29: {s = "\"}\" expected"; break;}
			case 30: {s = "\"synchronized\" expected"; break;}
			case 31: {s = "\"volatile\" expected"; break;}
			case 32: {s = "\"transient\" expected"; break;}
			case 33: {s = "\"native\" expected"; break;}
			case 34: {s = "\"void\" expected"; break;}
			case 35: {s = "\"boolean\" expected"; break;}
			case 36: {s = "\"byte\" expected"; break;}
			case 37: {s = "\"char\" expected"; break;}
			case 38: {s = "\"short\" expected"; break;}
			case 39: {s = "\"int\" expected"; break;}
			case 40: {s = "\"long\" expected"; break;}
			case 41: {s = "\"float\" expected"; break;}
			case 42: {s = "\"double\" expected"; break;}
			case 43: {s = "\"[\" expected"; break;}
			case 44: {s = "\"]\" expected"; break;}
			case 45: {s = "\"throws\" expected"; break;}
			case 46: {s = "\"=\" expected"; break;}
			case 47: {s = "\"break\" expected"; break;}
			case 48: {s = "\"continue\" expected"; break;}
			case 49: {s = "\"do\" expected"; break;}
			case 50: {s = "\"for\" expected"; break;}
			case 51: {s = "\"if\" expected"; break;}
			case 52: {s = "\"return\" expected"; break;}
			case 53: {s = "\"switch\" expected"; break;}
			case 54: {s = "\"throw\" expected"; break;}
			case 55: {s = "\"try\" expected"; break;}
			case 56: {s = "\"while\" expected"; break;}
			case 57: {s = "\":\" expected"; break;}
			case 58: {s = "\"?\" expected"; break;}
			case 59: {s = "\"else\" expected"; break;}
			case 60: {s = "\"default\" expected"; break;}
			case 61: {s = "\"case\" expected"; break;}
			case 62: {s = "\"finally\" expected"; break;}
			case 63: {s = "\"catch\" expected"; break;}
			case 64: {s = "\"%\" expected"; break;}
			case 65: {s = "\"/\" expected"; break;}
			case 66: {s = "\"+\" expected"; break;}
			case 67: {s = "\"-\" expected"; break;}
			case 68: {s = "\"<<\" expected"; break;}
			case 69: {s = "\">>>\" expected"; break;}
			case 70: {s = "\">>\" expected"; break;}
			case 71: {s = "\"instanceof\" expected"; break;}
			case 72: {s = "\"<=\" expected"; break;}
			case 73: {s = "\"<\" expected"; break;}
			case 74: {s = "\">=\" expected"; break;}
			case 75: {s = "\">\" expected"; break;}
			case 76: {s = "\"!=\" expected"; break;}
			case 77: {s = "\"==\" expected"; break;}
			case 78: {s = "\"&\" expected"; break;}
			case 79: {s = "\"^\" expected"; break;}
			case 80: {s = "\"|\" expected"; break;}
			case 81: {s = "\"&&\" expected"; break;}
			case 82: {s = "\"||\" expected"; break;}
			case 83: {s = "\"*=\" expected"; break;}
			case 84: {s = "\"/=\" expected"; break;}
			case 85: {s = "\"%=\" expected"; break;}
			case 86: {s = "\"+=\" expected"; break;}
			case 87: {s = "\"-=\" expected"; break;}
			case 88: {s = "\"<<=\" expected"; break;}
			case 89: {s = "\">>=\" expected"; break;}
			case 90: {s = "\">>>=\" expected"; break;}
			case 91: {s = "\"&=\" expected"; break;}
			case 92: {s = "\"^=\" expected"; break;}
			case 93: {s = "\"|=\" expected"; break;}
			case 94: {s = "\"!\" expected"; break;}
			case 95: {s = "\"~\" expected"; break;}
			case 96: {s = "\"++\" expected"; break;}
			case 97: {s = "\"--\" expected"; break;}
			case 98: {s = "\"null\" expected"; break;}
			case 99: {s = "\"false\" expected"; break;}
			case 100: {s = "\"true\" expected"; break;}
			case 101: {s = "\"super\" expected"; break;}
			case 102: {s = "\"new\" expected"; break;}
			case 103: {s = "\"this\" expected"; break;}
			case 104: {s = "not expected"; break;}
			case 105: {s = "invalid UnaryWithIdentTailOrDimExprs"; break;}
			case 106: {s = "invalid NewArrayTail"; break;}
			case 107: {s = "invalid NewArrayInstanceTail"; break;}
			case 108: {s = "invalid NewInstanceBody"; break;}
			case 109: {s = "invalid IdentNewInstanceOrPrimArrTail"; break;}
			case 110: {s = "invalid UnaryWithNewOrStrBody"; break;}
			case 111: {s = "invalid ClassOrThisOrNewInstCreation"; break;}
			case 112: {s = "invalid UnaryWithIdentBracketTail"; break;}
			case 113: {s = "invalid UnaryWithIdentDotTail"; break;}
			case 114: {s = "invalid UnaryWithIdentBody"; break;}
			case 115: {s = "invalid ThisOptConstrMethodAccessTail"; break;}
			case 116: {s = "invalid NewInstOrSuperOrMethodInvoke"; break;}
			case 117: {s = "invalid UnaryWithParaTail"; break;}
			case 118: {s = "invalid SuperConstrMethodAccess"; break;}
			case 119: {s = "invalid UnaryExpressionTail"; break;}
			case 120: {s = "invalid IncDecOp"; break;}
			case 121: {s = "invalid NegatePlusMinusOp"; break;}
			case 122: {s = "invalid AssignmentOperator"; break;}
			case 123: {s = "invalid EqualCompareOp"; break;}
			case 124: {s = "invalid RelCompareOp"; break;}
			case 125: {s = "invalid ShiftOp"; break;}
			case 126: {s = "invalid PlusMinusOp"; break;}
			case 127: {s = "invalid ModMulDivOp"; break;}
			case 128: {s = "invalid UnaryExpression"; break;}
			case 129: {s = "invalid RelationalExpressionTail"; break;}
			case 130: {s = "invalid ExpressionTail"; break;}
			case 131: {s = "invalid ExprOrLabelStmntOrVarDeclTail"; break;}
			case 132: {s = "invalid TryStatementTail"; break;}
			case 133: {s = "invalid SwitchBlockStatementGroup"; break;}
			case 134: {s = "invalid CommaExprOrExprTail"; break;}
			case 135: {s = "invalid ForVarExprInitTail"; break;}
			case 136: {s = "invalid ForInit"; break;}
			case 137: {s = "invalid AbstractOrStaticOrStrict"; break;}
			case 138: {s = "invalid LocalClassModifier"; break;}
			case 139: {s = "invalid JavaStatement"; break;}
			case 140: {s = "invalid FinalClsDeclOrVarDeclStmtTail"; break;}
			case 141: {s = "invalid VariableInitializer"; break;}
			case 142: {s = "invalid MethodDeclOrFieldDeclTail"; break;}
			case 143: {s = "invalid ConstrOrMethodOrFieldDeclBody"; break;}
			case 144: {s = "invalid PrimitiveType"; break;}
			case 145: {s = "invalid SimpleType"; break;}
			case 146: {s = "invalid SemiOrBlock"; break;}
			case 147: {s = "invalid BlockStatement"; break;}
			case 148: {s = "invalid AccModifier"; break;}
			case 149: {s = "invalid MemberDecl"; break;}
			case 150: {s = "invalid SemiOrClassBodyDecl"; break;}
			case 151: {s = "invalid ClassModifier"; break;}
			case 152: {s = "invalid ClassDeclOrInterfaceDecl"; break;}
			case 153: {s = "invalid TypeDeclaration"; break;}
			case 154: {s = "invalid StarOrIdentOptImportDeclSpec"; break;}
			case 155: {s = "invalid QualifiedIdentifierOrString"; break;}
			case 156: {s = "invalid Identifier"; break;}

			default: s = "Syntax error " + n;
		}
		StoreError(n, line, col, s);
	}

	void SemErr(int n, int line, int col) {
		String s;
		count++;
		switch (n) {
			// for example: case 0: s = "invalid character"; break;
			// perhaps insert application specific error messages here
			default: s = "Semantic error " + n; break;
		}
		StoreError(n, line, col, s);
	}

	void SemErr(String s, int line, int col) {
		count++;
		StoreError(9999, line, col, s);
	}

	public void Exception (String s) {
		System.out.println(s); System.exit(1);
	}

	void Summarize (String s) {
		switch (count) {
			case 0 : System.out.println("No errors detected"); break;
			case 1 : System.out.println("1 error detected"); break;
			default: System.out.println(count + " errors detected"); break;
		}
	}

}
