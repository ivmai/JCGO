package com.ivmaisoft.jcgo;

class Token {
	int kind;    // token kind
	int pos;     // token position in the source text (starting at 0)
	int col;     // token column (starting at 0)
	int line;    // token line (starting at 1)
	String str;  // exact string value
	String val;  // token string value (uppercase if ignoreCase)
}
