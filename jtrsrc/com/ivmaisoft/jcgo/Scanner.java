package com.ivmaisoft.jcgo;

import java.io.*;
import java.util.*;

/* class Token {
	int kind;    // token kind
	int pos;     // token position in the source text (starting at 0)
	int col;     // token column (starting at 0)
	int line;    // token line (starting at 1)
	String str;  // exact string value
	String val;  // token string value (uppercase if ignoreCase)
} */

class Buffer {

// Portability - use the following for Java 1.0
//	static byte[] buf;  // Java 1.0
// Portability - use the following for Java 1.1
//	static char[] buf;  // Java 1.1

	static char[] buf;  // Java 1.1

	static int bufLen;
	static int pos;
	static final int eof = 65535;

	static void Fill(String name) {
		try {
			File f = new File(name); bufLen = (int) f.length();

// Portability - use the following for Java 1.0
//			BufferedInputStream s = new BufferedInputStream(new FileInputStream(f), bufLen);
//			buf = new byte[bufLen];  // Java 1.0
// Portability - use the following for Java 1.1
//			BufferedReader s = new BufferedReader(new FileReader(f), bufLen);
//			buf = new char[bufLen];  // Java 1.1

			FileReader s = new FileReader(f);
			buf = new char[bufLen];  // Java 1.1

			int n = s.read(buf); pos = 0;
   if (n > 0) { while (n < buf.length) {
   int res = s.read(buf, n, buf.length - n);
   if (res <= 0) break; else n += res; } }
   s.close();
		} catch (IOException e) {
			System.out.println("--- cannot open file " + name);
   if (bufLen >= 0) System.exit(1);
			System.exit(0);
		}
	}

	static void Set(int position) {
   if (position > 0) { pos = prevPrevPos; return; }
		if (position < 0) position = 0; else if (position >= bufLen) position = bufLen;
		pos = position;
	}

   private static int prevPrevPos, prevPos;
   private static boolean ignoreBackslash;
	static int read() {
   prevPrevPos = prevPos; prevPos = pos;
   int c; if (pos == 0) ignoreBackslash = false;
   if (!ignoreBackslash) { if (bufLen - 5 > pos && buf[pos] == '\\') {
   if (buf[pos + 1] == 'u') { int k = 2;
   while (buf[pos + k] == 'u' && bufLen - pos > k + 4) k++;
   if (buf[pos + k] == '0' && buf[pos + k + 1] == '0' &&
   buf[pos + k + 2] >= '0' && buf[pos + k + 2] <= '7' &&
   (((c = buf[pos + k + 3]) >= '0' && c <= '9') ||
   ((c -= 'A' - '0' - 10) >= '0' + 10 && c <= '0' + 0xf) ||
   ((c -= 'a' - 'A') >= '0' + 10 && c <= '0' + 0xf)) &&
   (c = ((buf[pos + k + 2] - '0') << 4) | (c - '0')) != 0 && (c != '\\' ||
   bufLen - pos == k + 4 || buf[pos + k + 4] != 'u')) { pos += k + 4;
   return c; } } ignoreBackslash = true; } } else ignoreBackslash = false;
		if (pos < bufLen) return (int) buf[pos++]; else return eof;
	}
}

public class Scanner {

	public static ErrorStream err;  // error messages

	private static final char EOF = '\0';
	private static final char CR  = '\r';
	private static final char LF  = '\n';
	private static final int noSym = 104;
	private static final int[] start = {
	100,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
	  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 49,  0,  0,  0,  0,  0,
	  0, 78, 40,  0,  1, 67, 81, 29, 56, 57, 58, 69, 59, 70, 23, 68,
	 51, 50, 50, 50, 50, 50, 50, 50, 50, 50, 65, 54, 71, 64, 73, 66,
	 55,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,
	  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1, 62,  7, 63, 82,  1,
	  0,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,
	  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1, 60, 83, 61, 97,  0,
	  0};

	private static Token t;        // current token
	private static char strCh;     // current input character (original)
	private static char ch;        // current input character (for token)
	private static char lastCh;    // last input character
	private static int pos;        // position of current character
	private static int line;       // line number of current character
	private static int lineStart;  // start position of current line
	private static BitSet ignore;  // set of characters to be ignored by the scanner

	static void Init (String fileName, ErrorStream e) {
		Buffer.Fill(fileName);
		e.fileName = fileName;
		pos = -1; line = 1; lineStart = 0; lastCh = 0;
		NextCh();
		ignore = new BitSet(128);
		ignore.set(9); ignore.set(10); ignore.set(12); ignore.set(13); 
		ignore.set(32); 
		err = e;
	}

	static void Init (String fileName) {
		Init(fileName, new ErrorStream());
	}

	private static void NextCh() {
		lastCh = ch;
		strCh = (char) Buffer.read(); pos++;
		ch = strCh;
		if (ch == '\uffff') ch = EOF;
   if (ch == '\t') lineStart += (pos - lineStart) % 8 - 7;
   if (ch >= 0x7f && ch != 0xffff) { ch = 0x7f; return; }
		else if (ch == CR) {line++; lineStart = pos + 1;}
		else if (ch == LF) {
			if (lastCh != CR) line++;
			lineStart = pos + 1;
		} else if (ch > '\u007f') {
			Scanner.err.StoreError(0, line, pos - lineStart + 1, "invalid character in source file");
			Scanner.err.count++; ch = ' ';
		}
	}

	private static boolean Comment0() {
		int level = 1; 
		NextCh();
		if (ch == '*') {
			NextCh();
			for(;;) {
				if (ch == '*') {
					NextCh();
					if (ch == '/') {
						level--;
						if (level == 0) {NextCh(); return true;}
						NextCh();
					}
					} else if (ch == EOF) return false;
					else NextCh();
				}
		} else {
			if (ch == CR || ch == LF) {line--;}
			pos = pos - 2; Buffer.Set(pos+1); NextCh();
		}
		return false;
	}
	private static boolean Comment1() {
		int level = 1; 
		NextCh();
		if (ch == '/') {
			NextCh();
			for(;;) {
   if (ch == 13) { NextCh(); return true; }
				if (ch == 10) {
					level--;
					if (level == 0) {NextCh(); return true;}
					NextCh();
					} else if (ch == EOF) return false;
					else NextCh();
				}
		} else {
			if (ch == CR || ch == LF) {line--;}
			pos = pos - 2; Buffer.Set(pos+1); NextCh();
		}
		return false;
	}


	private static void CheckLiteral(StringBuffer buf) {
		t.val = buf.toString();
		switch (t.val.charAt(0)) {
			case 'a': {
				if (t.val.equals("abstract")) t.kind = 21;
				else if (t.val.equals("assert")) t.kind = 7;
				break;}
			case 'b': {
				if (t.val.equals("boolean")) t.kind = 35;
				else if (t.val.equals("break")) t.kind = 47;
				else if (t.val.equals("byte")) t.kind = 36;
				break;}
			case 'c': {
				if (t.val.equals("case")) t.kind = 61;
				else if (t.val.equals("catch")) t.kind = 63;
				else if (t.val.equals("char")) t.kind = 37;
				else if (t.val.equals("class")) t.kind = 23;
				else if (t.val.equals("continue")) t.kind = 48;
				break;}
			case 'd': {
				if (t.val.equals("default")) t.kind = 60;
				else if (t.val.equals("do")) t.kind = 49;
				else if (t.val.equals("double")) t.kind = 42;
				break;}
			case 'e': {
				if (t.val.equals("else")) t.kind = 59;
				else if (t.val.equals("extends")) t.kind = 25;
				break;}
			case 'f': {
				if (t.val.equals("false")) t.kind = 99;
				else if (t.val.equals("final")) t.kind = 20;
				else if (t.val.equals("finally")) t.kind = 62;
				else if (t.val.equals("float")) t.kind = 41;
				else if (t.val.equals("for")) t.kind = 50;
				break;}
			case 'i': {
				if (t.val.equals("if")) t.kind = 51;
				else if (t.val.equals("implements")) t.kind = 26;
				else if (t.val.equals("import")) t.kind = 14;
				else if (t.val.equals("instanceof")) t.kind = 71;
				else if (t.val.equals("int")) t.kind = 39;
				else if (t.val.equals("interface")) t.kind = 24;
				break;}
			case 'l': {
				if (t.val.equals("long")) t.kind = 40;
				break;}
			case 'n': {
				if (t.val.equals("native")) t.kind = 33;
				else if (t.val.equals("new")) t.kind = 102;
				else if (t.val.equals("null")) t.kind = 98;
				break;}
			case 'p': {
				if (t.val.equals("package")) t.kind = 8;
				else if (t.val.equals("private")) t.kind = 17;
				else if (t.val.equals("protected")) t.kind = 18;
				else if (t.val.equals("public")) t.kind = 16;
				break;}
			case 'r': {
				if (t.val.equals("return")) t.kind = 52;
				break;}
			case 's': {
				if (t.val.equals("short")) t.kind = 38;
				else if (t.val.equals("static")) t.kind = 19;
				else if (t.val.equals("strictfp")) t.kind = 22;
				else if (t.val.equals("super")) t.kind = 101;
				else if (t.val.equals("switch")) t.kind = 53;
				else if (t.val.equals("synchronized")) t.kind = 30;
				break;}
			case 't': {
				if (t.val.equals("this")) t.kind = 103;
				else if (t.val.equals("throw")) t.kind = 54;
				else if (t.val.equals("throws")) t.kind = 45;
				else if (t.val.equals("transient")) t.kind = 32;
				else if (t.val.equals("true")) t.kind = 100;
				else if (t.val.equals("try")) t.kind = 55;
				break;}
			case 'v': {
				if (t.val.equals("void")) t.kind = 34;
				else if (t.val.equals("volatile")) t.kind = 31;
				break;}
			case 'w': {
				if (t.val.equals("while")) t.kind = 56;
				break;}
		}
	}

	static Token Scan() {
		while (ignore.get((int)ch)) NextCh();
		if (ch == '/' && Comment0()  || ch == '/' && Comment1() ) return Scan();
		t = new Token();
		t.pos = pos; t.col = pos - lineStart + 1; t.line = line;
		StringBuffer buf = new StringBuffer();
		int state = start[ch];
		loop: for (;;) {
			buf.append(strCh);
			NextCh();
			switch (state) {
				case 0:
					{t.kind = noSym; break loop;} // NextCh already done
				case 1:
					if ((ch == '$'
					  || ch >= '0' && ch <= '9'
					  || ch >= 'A' && ch <= 'Z'
					  || ch == '_'
					  || ch >= 'a' && ch <= 'z')) {break;}
					else if ((ch == 92)) {state = 2; break;}
					else {t.kind = 1; CheckLiteral(buf); break loop;}
				case 2:
					if (ch == 'u') {state = 3; break;}
					else {t.kind = noSym; break loop;}
				case 3:
					if ((ch >= '0' && ch <= '9'
					  || ch >= 'A' && ch <= 'F'
					  || ch >= 'a' && ch <= 'f')) {state = 4; break;}
					else if (ch == 'u') {break;}
					else {t.kind = noSym; break loop;}
				case 4:
					if ((ch >= '0' && ch <= '9'
					  || ch >= 'A' && ch <= 'F'
					  || ch >= 'a' && ch <= 'f')) {state = 5; break;}
					else {t.kind = noSym; break loop;}
				case 5:
					if ((ch >= '0' && ch <= '9'
					  || ch >= 'A' && ch <= 'F'
					  || ch >= 'a' && ch <= 'f')) {state = 6; break;}
					else {t.kind = noSym; break loop;}
				case 6:
					if ((ch >= '0' && ch <= '9'
					  || ch >= 'A' && ch <= 'F'
					  || ch >= 'a' && ch <= 'f')) {state = 1; break;}
					else {t.kind = noSym; break loop;}
				case 7:
					if (ch == 'u') {state = 8; break;}
					else {t.kind = noSym; break loop;}
				case 8:
					if ((ch >= '0' && ch <= '9'
					  || ch >= 'A' && ch <= 'F'
					  || ch >= 'a' && ch <= 'f')) {state = 9; break;}
					else if (ch == 'u') {break;}
					else {t.kind = noSym; break loop;}
				case 9:
					if ((ch >= '0' && ch <= '9'
					  || ch >= 'A' && ch <= 'F'
					  || ch >= 'a' && ch <= 'f')) {state = 10; break;}
					else {t.kind = noSym; break loop;}
				case 10:
					if ((ch >= '0' && ch <= '9'
					  || ch >= 'A' && ch <= 'F'
					  || ch >= 'a' && ch <= 'f')) {state = 11; break;}
					else {t.kind = noSym; break loop;}
				case 11:
					if ((ch >= '0' && ch <= '9'
					  || ch >= 'A' && ch <= 'F'
					  || ch >= 'a' && ch <= 'f')) {state = 1; break;}
					else {t.kind = noSym; break loop;}
				case 12:
					if ((ch >= '0' && ch <= '9'
					  || ch >= 'A' && ch <= 'F'
					  || ch >= 'a' && ch <= 'f')) {state = 13; break;}
					else {t.kind = noSym; break loop;}
				case 13:
					if ((ch >= '0' && ch <= '9'
					  || ch >= 'A' && ch <= 'F'
					  || ch >= 'a' && ch <= 'f')) {break;}
					else if ((ch == 'L'
					  || ch == 'l')) {state = 14; break;}
					else {t.kind = 2; break loop;}
				case 14:
					{t.kind = 2; break loop;}
				case 15:
					if ((ch == 'D'
					  || ch == 'F'
					  || ch == 'd'
					  || ch == 'f')) {state = 28; break;}
					else if ((ch >= '0' && ch <= '9')) {break;}
					else if (ch == '.') {state = 16; break;}
					else if ((ch == 'E'
					  || ch == 'e')) {state = 20; break;}
					else {t.kind = noSym; break loop;}
				case 16:
					if ((ch == 'D'
					  || ch == 'F'
					  || ch == 'd'
					  || ch == 'f')) {state = 28; break;}
					else if ((ch >= '0' && ch <= '9')) {break;}
					else if ((ch == 'E'
					  || ch == 'e')) {state = 17; break;}
					else {t.kind = 3; break loop;}
				case 17:
					if ((ch >= '0' && ch <= '9')) {state = 19; break;}
					else if ((ch == '+'
					  || ch == '-')) {state = 18; break;}
					else {t.kind = noSym; break loop;}
				case 18:
					if ((ch >= '0' && ch <= '9')) {state = 19; break;}
					else {t.kind = noSym; break loop;}
				case 19:
					if ((ch == 'D'
					  || ch == 'F'
					  || ch == 'd'
					  || ch == 'f')) {state = 28; break;}
					else if ((ch >= '0' && ch <= '9')) {break;}
					else {t.kind = 3; break loop;}
				case 20:
					if ((ch >= '0' && ch <= '9')) {state = 22; break;}
					else if ((ch == '+'
					  || ch == '-')) {state = 21; break;}
					else {t.kind = noSym; break loop;}
				case 21:
					if ((ch >= '0' && ch <= '9')) {state = 22; break;}
					else {t.kind = noSym; break loop;}
				case 22:
					if ((ch == 'D'
					  || ch == 'F'
					  || ch == 'd'
					  || ch == 'f')) {state = 28; break;}
					else if ((ch >= '0' && ch <= '9')) {break;}
					else {t.kind = 3; break loop;}
				case 23:
					if ((ch >= '0' && ch <= '9')) {state = 24; break;}
					else {t.kind = 13; break loop;}
				case 24:
					if ((ch == 'D'
					  || ch == 'F'
					  || ch == 'd'
					  || ch == 'f')) {state = 28; break;}
					else if ((ch >= '0' && ch <= '9')) {break;}
					else if ((ch == 'E'
					  || ch == 'e')) {state = 25; break;}
					else {t.kind = 3; break loop;}
				case 25:
					if ((ch >= '0' && ch <= '9')) {state = 27; break;}
					else if ((ch == '+'
					  || ch == '-')) {state = 26; break;}
					else {t.kind = noSym; break loop;}
				case 26:
					if ((ch >= '0' && ch <= '9')) {state = 27; break;}
					else {t.kind = noSym; break loop;}
				case 27:
					if ((ch == 'D'
					  || ch == 'F'
					  || ch == 'd'
					  || ch == 'f')) {state = 28; break;}
					else if ((ch >= '0' && ch <= '9')) {break;}
					else {t.kind = 3; break loop;}
				case 28:
					{t.kind = 3; break loop;}
				case 29:
					if ((ch == 92)) {state = 30; break;}
					else if ((ch <= 9
					  || ch >= 11 && ch <= 12
					  || ch >= 14 && ch <= '&'
					  || ch >= '(' && ch <= '['
					  || ch >= ']')) {state = 31; break;}
					else {t.kind = noSym; break loop;}
				case 30:
					if ((ch == '"'
					  || ch == 39
					  || ch == 92
					  || ch == 'b'
					  || ch == 'f'
					  || ch == 'n'
					  || ch == 'r'
					  || ch == 't')) {state = 31; break;}
					else if ((ch >= '0' && ch <= '3')) {state = 32; break;}
					else if ((ch >= '4' && ch <= '7')) {state = 34; break;}
					else if (ch == 'u') {state = 35; break;}
					else {t.kind = noSym; break loop;}
				case 31:
					if (ch == 39) {state = 39; break;}
					else {t.kind = noSym; break loop;}
				case 32:
					if ((ch >= '0' && ch <= '7')) {state = 33; break;}
					else if (ch == 39) {state = 39; break;}
					else {t.kind = noSym; break loop;}
				case 33:
					if ((ch >= '0' && ch <= '7')) {state = 31; break;}
					else if (ch == 39) {state = 39; break;}
					else {t.kind = noSym; break loop;}
				case 34:
					if ((ch >= '0' && ch <= '7')) {state = 31; break;}
					else if (ch == 39) {state = 39; break;}
					else {t.kind = noSym; break loop;}
				case 35:
					if ((ch >= '0' && ch <= '9'
					  || ch >= 'A' && ch <= 'F'
					  || ch >= 'a' && ch <= 'f')) {state = 36; break;}
					else if (ch == 'u') {break;}
					else {t.kind = noSym; break loop;}
				case 36:
					if ((ch >= '0' && ch <= '9'
					  || ch >= 'A' && ch <= 'F'
					  || ch >= 'a' && ch <= 'f')) {state = 37; break;}
					else {t.kind = noSym; break loop;}
				case 37:
					if ((ch >= '0' && ch <= '9'
					  || ch >= 'A' && ch <= 'F'
					  || ch >= 'a' && ch <= 'f')) {state = 38; break;}
					else {t.kind = noSym; break loop;}
				case 38:
					if ((ch >= '0' && ch <= '9'
					  || ch >= 'A' && ch <= 'F'
					  || ch >= 'a' && ch <= 'f')) {state = 31; break;}
					else {t.kind = noSym; break loop;}
				case 39:
					{t.kind = 4; break loop;}
				case 40:
					if ((ch == '"')) {state = 48; break;}
					else if ((ch == 92)) {state = 41; break;}
					else if ((ch <= 9
					  || ch >= 11 && ch <= 12
					  || ch >= 14 && ch <= '!'
					  || ch >= '#' && ch <= '['
					  || ch >= ']')) {break;}
					else {t.kind = noSym; break loop;}
				case 41:
					if ((ch == '"'
					  || ch == 39
					  || ch == 92
					  || ch == 'b'
					  || ch == 'f'
					  || ch == 'n'
					  || ch == 'r'
					  || ch == 't')) {state = 40; break;}
					else if ((ch >= '0' && ch <= '3')) {state = 42; break;}
					else if ((ch >= '4' && ch <= '7')) {state = 43; break;}
					else if (ch == 'u') {state = 44; break;}
					else {t.kind = noSym; break loop;}
				case 42:
					if ((ch == '"')) {state = 48; break;}
					else if ((ch == 92)) {state = 41; break;}
					else if ((ch <= 9
					  || ch >= 11 && ch <= 12
					  || ch >= 14 && ch <= '!'
					  || ch >= '#' && ch <= '/'
					  || ch >= '8' && ch <= '['
					  || ch >= ']')) {state = 40; break;}
					else if ((ch >= '0' && ch <= '7')) {state = 52; break;}
					else {t.kind = noSym; break loop;}
				case 43:
					if ((ch == '"')) {state = 48; break;}
					else if ((ch == 92)) {state = 41; break;}
					else if ((ch <= 9
					  || ch >= 11 && ch <= 12
					  || ch >= 14 && ch <= '!'
					  || ch >= '#' && ch <= '['
					  || ch >= ']')) {state = 40; break;}
					else {t.kind = noSym; break loop;}
				case 44:
					if ((ch >= '0' && ch <= '9'
					  || ch >= 'A' && ch <= 'F'
					  || ch >= 'a' && ch <= 'f')) {state = 45; break;}
					else if (ch == 'u') {break;}
					else {t.kind = noSym; break loop;}
				case 45:
					if ((ch >= '0' && ch <= '9'
					  || ch >= 'A' && ch <= 'F'
					  || ch >= 'a' && ch <= 'f')) {state = 46; break;}
					else {t.kind = noSym; break loop;}
				case 46:
					if ((ch >= '0' && ch <= '9'
					  || ch >= 'A' && ch <= 'F'
					  || ch >= 'a' && ch <= 'f')) {state = 47; break;}
					else {t.kind = noSym; break loop;}
				case 47:
					if ((ch >= '0' && ch <= '9'
					  || ch >= 'A' && ch <= 'F'
					  || ch >= 'a' && ch <= 'f')) {state = 40; break;}
					else {t.kind = noSym; break loop;}
				case 48:
					{t.kind = 5; break loop;}
				case 49:
					{t.kind = 6; break loop;}
				case 50:
					if ((ch >= '0' && ch <= '9')) {break;}
					else if ((ch == 'D'
					  || ch == 'F'
					  || ch == 'd'
					  || ch == 'f')) {state = 28; break;}
					else if ((ch == 'L'
					  || ch == 'l')) {state = 14; break;}
					else if (ch == '.') {state = 16; break;}
					else if ((ch == 'E'
					  || ch == 'e')) {state = 20; break;}
					else {t.kind = 2; break loop;}
				case 51:
					if ((ch >= '0' && ch <= '7')) {state = 53; break;}
					else if ((ch == 'D'
					  || ch == 'F'
					  || ch == 'd'
					  || ch == 'f')) {state = 28; break;}
					else if ((ch >= '8' && ch <= '9')) {state = 15; break;}
					else if ((ch == 'L'
					  || ch == 'l')) {state = 14; break;}
					else if ((ch == 'X'
					  || ch == 'x')) {state = 12; break;}
					else if (ch == '.') {state = 16; break;}
					else if ((ch == 'E'
					  || ch == 'e')) {state = 20; break;}
					else {t.kind = 2; break loop;}
				case 52:
					if ((ch == '"')) {state = 48; break;}
					else if ((ch == 92)) {state = 41; break;}
					else if ((ch <= 9
					  || ch >= 11 && ch <= 12
					  || ch >= 14 && ch <= '!'
					  || ch >= '#' && ch <= '['
					  || ch >= ']')) {state = 40; break;}
					else {t.kind = noSym; break loop;}
				case 53:
					if ((ch >= '0' && ch <= '7')) {break;}
					else if ((ch == 'D'
					  || ch == 'F'
					  || ch == 'd'
					  || ch == 'f')) {state = 28; break;}
					else if ((ch >= '8' && ch <= '9')) {state = 15; break;}
					else if ((ch == 'L'
					  || ch == 'l')) {state = 14; break;}
					else if (ch == '.') {state = 16; break;}
					else if ((ch == 'E'
					  || ch == 'e')) {state = 20; break;}
					else {t.kind = 2; break loop;}
				case 54:
					{t.kind = 9; break loop;}
				case 55:
					{t.kind = 10; break loop;}
				case 56:
					{t.kind = 11; break loop;}
				case 57:
					{t.kind = 12; break loop;}
				case 58:
					if (ch == '=') {state = 86; break;}
					else {t.kind = 15; break loop;}
				case 59:
					{t.kind = 27; break loop;}
				case 60:
					{t.kind = 28; break loop;}
				case 61:
					{t.kind = 29; break loop;}
				case 62:
					{t.kind = 43; break loop;}
				case 63:
					{t.kind = 44; break loop;}
				case 64:
					if (ch == '=') {state = 80; break;}
					else {t.kind = 46; break loop;}
				case 65:
					{t.kind = 57; break loop;}
				case 66:
					{t.kind = 58; break loop;}
				case 67:
					if (ch == '=') {state = 88; break;}
					else {t.kind = 64; break loop;}
				case 68:
					if (ch == '=') {state = 87; break;}
					else {t.kind = 65; break loop;}
				case 69:
					if (ch == '=') {state = 89; break;}
					else if (ch == '+') {state = 98; break;}
					else {t.kind = 66; break loop;}
				case 70:
					if (ch == '=') {state = 90; break;}
					else if (ch == '-') {state = 99; break;}
					else {t.kind = 67; break loop;}
				case 71:
					if (ch == '<') {state = 72; break;}
					else if (ch == '=') {state = 76; break;}
					else {t.kind = 73; break loop;}
				case 72:
					if (ch == '=') {state = 91; break;}
					else {t.kind = 68; break loop;}
				case 73:
					if (ch == '>') {state = 74; break;}
					else if (ch == '=') {state = 77; break;}
					else {t.kind = 75; break loop;}
				case 74:
					if (ch == '>') {state = 75; break;}
					else if (ch == '=') {state = 92; break;}
					else {t.kind = 70; break loop;}
				case 75:
					if (ch == '=') {state = 93; break;}
					else {t.kind = 69; break loop;}
				case 76:
					{t.kind = 72; break loop;}
				case 77:
					{t.kind = 74; break loop;}
				case 78:
					if (ch == '=') {state = 79; break;}
					else {t.kind = 94; break loop;}
				case 79:
					{t.kind = 76; break loop;}
				case 80:
					{t.kind = 77; break loop;}
				case 81:
					if (ch == '&') {state = 84; break;}
					else if (ch == '=') {state = 94; break;}
					else {t.kind = 78; break loop;}
				case 82:
					if (ch == '=') {state = 95; break;}
					else {t.kind = 79; break loop;}
				case 83:
					if (ch == '|') {state = 85; break;}
					else if (ch == '=') {state = 96; break;}
					else {t.kind = 80; break loop;}
				case 84:
					{t.kind = 81; break loop;}
				case 85:
					{t.kind = 82; break loop;}
				case 86:
					{t.kind = 83; break loop;}
				case 87:
					{t.kind = 84; break loop;}
				case 88:
					{t.kind = 85; break loop;}
				case 89:
					{t.kind = 86; break loop;}
				case 90:
					{t.kind = 87; break loop;}
				case 91:
					{t.kind = 88; break loop;}
				case 92:
					{t.kind = 89; break loop;}
				case 93:
					{t.kind = 90; break loop;}
				case 94:
					{t.kind = 91; break loop;}
				case 95:
					{t.kind = 92; break loop;}
				case 96:
					{t.kind = 93; break loop;}
				case 97:
					{t.kind = 95; break loop;}
				case 98:
					{t.kind = 96; break loop;}
				case 99:
					{t.kind = 97; break loop;}
				case 100:
					{t.kind = 0; break loop;}
			}
		}
		t.str = buf.toString();
		t.val = t.str;
		return t;
	}
}
