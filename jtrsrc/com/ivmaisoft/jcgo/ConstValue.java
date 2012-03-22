/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/ConstValue.java --
 * a part of JCGO translator.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2012 Ivan Maidanski <ivmai@mail.ru>
 * All rights reserved.
 */

/*
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 **
 * This software is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License (GPL) for more details.
 **
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole
 * combination.
 **
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent
 * modules, and to copy and distribute the resulting executable under
 * terms of your choice, provided that you also meet, for each linked
 * independent module, the terms and conditions of the license of that
 * module. An independent module is a module which is not derived from
 * or based on this library. If you modify this library, you may extend
 * this exception to your version of the library, but you are not
 * obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

package com.ivmaisoft.jcgo;

/**
 * This class represents exact value of a java constant expression.
 */

final class ConstValue {

    private static final ConstValue LONG_ZERO = new ConstValue(0, 0);

    private/* final */int low;

    private/* final */int high;

    private/* final */boolean isLong;

    ConstValue(boolean boolValue) {
        high = low = boolValue ? -1 : 0;
        isLong = false;
    }

    ConstValue(int intValue) {
        low = intValue;
        high = intValue < 0 ? -1 : 0;
        isLong = false;
    }

    ConstValue(int low, int high) {
        this.low = low;
        this.high = high;
        this.isLong = true;
    }

    private ConstValue(int low, int high, boolean isLong) {
        this.low = low;
        this.high = high;
        this.isLong = isLong;
    }

    ConstValue(String str) {
        char ch;
        boolean isLong = false;
        if (str.length() > 0
                && ((ch = str.charAt(str.length() - 1)) == 'L' || ch == 'l')) {
            str = str.substring(0, str.length() - 1);
            isLong = true;
        }
        int radix = 10;
        if (str.length() > 1 && str.charAt(0) == '0') {
            radix = 8;
            if (str.charAt(1) == 'x' || str.charAt(1) == 'X') {
                radix = 16;
                str = str.substring(2);
            }
        }
        int len = str.length();
        int low = 0;
        int high = 0;
        for (int i = 0; i < len; i++) {
            ch = str.charAt(i);
            int digit = radix;
            if (ch >= '0' && ch <= '9') {
                digit = ch - '0';
            } else if (ch >= 'A' && ch <= 'Z') {
                digit = ch - ('A' - 10);
            } else if (ch >= 'a' && ch <= 'z') {
                digit = ch - ('a' - 10);
            }
            if (digit >= radix)
                break;
            if (isLong) {
                high = mulHigh(low, radix) + high * radix;
            }
            low = low * radix + digit;
            if (low < digit && low >= 0) {
                high++;
            }
        }
        this.low = low;
        this.high = isLong ? high : low < 0 ? -1 : 0;
        this.isLong = isLong;
    }

    boolean isLong() {
        return isLong;
    }

    boolean isNeg() {
        return high < 0;
    }

    boolean isNonZero() {
        return (low | high) != 0;
    }

    boolean isMinusOne() {
        return (low & high) == -1;
    }

    int getIntValue() {
        return low;
    }

    ConstValue castTo(int type) {
        if (type == Type.BOOLEAN)
            return this;
        if (type == Type.BYTE)
            return new ConstValue((byte) low);
        if (type == Type.CHAR)
            return new ConstValue((char) low);
        if (type == Type.SHORT)
            return new ConstValue((short) low);
        if (type == Type.INT)
            return isLong ? new ConstValue(low) : this;
        if (type == Type.LONG)
            return castToLong();
        return null;
    }

    ConstValue neg() {
        return isLong ? new ConstValue(-low, low != 0 ? ~high : -high)
                : new ConstValue(-low);
    }

    ConstValue bitNot() {
        return new ConstValue(~low, ~high, isLong);
    }

    ConstValue bitAnd(ConstValue value) {
        return value != null ? new ConstValue(low & value.low, high
                & value.high, isLong || value.isLong) : null;
    }

    ConstValue bitOr(ConstValue value) {
        return value != null ? new ConstValue(low | value.low, high
                | value.high, isLong || value.isLong) : null;
    }

    ConstValue bitXor(ConstValue value) {
        return value != null ? new ConstValue(low ^ value.low, high
                ^ value.high, isLong || value.isLong) : null;
    }

    ConstValue isEqual(ConstValue value) {
        return value != null ? new ConstValue(low == value.low
                && high == value.high) : null;
    }

    ConstValue isLessThan(ConstValue value) {
        return value != null ? new ConstValue(high < value.high
                || (high == value.high && ((low ^ value.low) >= 0 ? low
                        - value.low : value.low) < 0)) : null;
    }

    ConstValue isLessOrEqual(ConstValue value) {
        return value != null ? new ConstValue(high < value.high
                || (high == value.high && ((low ^ value.low) >= 0 ? value.low
                        - low : low) >= 0)) : null;
    }

    private ConstValue castToLong() {
        return isLong ? this : new ConstValue(low, high);
    }

    private ConstValue plus(ConstValue value) {
        if (value != null) {
            int resLow = low + value.low;
            if (!isLong && !value.isLong)
                return new ConstValue(resLow);
            int resHigh = high + value.high;
            if (((resLow ^ low) >= 0 ? resLow - low : low) < 0) {
                resHigh++;
            }
            value = new ConstValue(resLow, resHigh);
        }
        return value;
    }

    private ConstValue minus(ConstValue value) {
        if (value != null) {
            int resLow = low - value.low;
            if (!isLong && !value.isLong)
                return new ConstValue(resLow);
            int resHigh = high - value.high;
            if (((low ^ resLow) >= 0 ? low - resLow : resLow) < 0) {
                resHigh--;
            }
            value = new ConstValue(resLow, resHigh);
        }
        return value;
    }

    private ConstValue times(ConstValue value) {
        return value != null ? (isLong || value.isLong ? new ConstValue(low
                * value.low, mulHigh(low, value.low) + low * value.high + high
                * value.low) : new ConstValue(low * value.low)) : null;
    }

    private static int mulHigh(int valueA, int valueB) {
        int highA = valueA >>> 16;
        int highB = valueB >>> 16;
        valueA &= 0xffff;
        int lowB = valueB & 0xffff;
        valueA = ((valueA * lowB) >>> 16) + valueA * highB;
        lowB *= highA;
        valueA += lowB;
        highA = highA * highB + (valueA >>> 16);
        if ((valueA ^ lowB) >= 0) {
            lowB = valueA - lowB;
        }
        if (lowB < 0) {
            highA += 0x10000;
        }
        return highA;
    }

    private ConstValue divide(ConstValue value) {
        if (value != null) {
            if (!isLong && !value.isLong)
                return value.low != 0 ? new ConstValue(low / value.low) : null;
            if (value.isNonZero()) {
                ConstValue res = LONG_ZERO;
                if (isNonZero()) {
                    ConstValue remainder = isNeg() ? castToLong().neg()
                            : castToLong();
                    ConstValue shiftedValue = value.isNeg() ? value
                            .castToLong().neg() : value.castToLong();
                    ConstValue one = new ConstValue(1, 0);
                    ConstValue shiftedOne = one;
                    while (!shiftedValue.isNeg()) {
                        shiftedValue = shiftedValue.shiftLeft(one);
                        shiftedOne = shiftedOne.shiftLeft(one);
                    }
                    do {
                        if (shiftedValue.isNeg() ? shiftedValue.isEqual(
                                remainder).isNonZero() : shiftedValue
                                .isLessOrEqual(remainder).isNonZero()
                                || remainder.isNeg()) {
                            remainder = remainder.minus(shiftedValue);
                            res = res.plus(shiftedOne);
                            if (!remainder.isNonZero())
                                break;
                        }
                        shiftedOne = shiftedOne.fillShiftRight(one);
                        if (!shiftedOne.isNonZero())
                            break;
                        shiftedValue = shiftedValue.fillShiftRight(one);
                    } while (true);
                    if (isNeg() != value.isNeg()) {
                        res = res.neg();
                    }
                }
                return res;
            }
        }
        return null;
    }

    private ConstValue mod(ConstValue value) {
        ConstValue value2 = divide(value);
        return value2 != null ? minus(value2.times(value)) : null;
    }

    private ConstValue shiftLeft(ConstValue value) {
        return value != null ? (isLong ? ((value.low & 0x20) != 0 ? new ConstValue(
                0, low << value.low) : new ConstValue(low << value.low,
                ((value.low & 0x1f) != 0 ? low >>> (-value.low) : 0)
                        | (high << value.low)))
                : new ConstValue(low << value.low))
                : null;
    }

    private ConstValue shiftRight(ConstValue value) {
        return value != null ? (isLong ? ((value.low & 0x20) != 0 ? new ConstValue(
                high >> value.low, high < 0 ? -1 : 0) : new ConstValue(
                ((value.low & 0x1f) != 0 ? high << (-value.low) : 0)
                        | (low >>> value.low), high >> value.low))
                : new ConstValue(low >> value.low))
                : null;
    }

    private ConstValue fillShiftRight(ConstValue value) {
        return value != null ? (isLong ? ((value.low & 0x20) != 0 ? new ConstValue(
                high >>> value.low, 0) : new ConstValue(
                ((value.low & 0x1f) != 0 ? high << (-value.low) : 0)
                        | (low >>> value.low), high >>> value.low))
                : new ConstValue(low >>> value.low))
                : null;
    }

    ConstValue comparisonOp(ConstValue value, int sym) {
        if (value != null) {
            if (sym == LexTerm.EQ)
                return isEqual(value);
            if (sym == LexTerm.NE)
                return isEqual(value).bitNot();
            if (sym == LexTerm.LT)
                return isLessThan(value);
            if (sym == LexTerm.LE)
                return isLessOrEqual(value);
            if (sym == LexTerm.GT)
                return value.isLessThan(this);
            if (sym == LexTerm.GE)
                return value.isLessOrEqual(this);
        }
        return null;
    }

    ConstValue arithmeticOp(ConstValue value, int sym) {
        if (value != null) {
            if (sym == LexTerm.BITAND)
                return bitAnd(value);
            if (sym == LexTerm.BITOR)
                return bitOr(value);
            if (sym == LexTerm.XOR)
                return bitXor(value);
            if (sym == LexTerm.PLUS)
                return plus(value);
            if (sym == LexTerm.MINUS)
                return minus(value);
            if (sym == LexTerm.TIMES)
                return times(value);
            if (sym == LexTerm.DIVIDE)
                return divide(value);
            if (sym == LexTerm.MOD)
                return mod(value);
            if (sym == LexTerm.SHIFT_LEFT)
                return shiftLeft(value);
            if (sym == LexTerm.SHIFT_RIGHT)
                return shiftRight(value);
            if (sym == LexTerm.FILLSHIFT_RIGHT)
                return fillShiftRight(value);
        }
        return null;
    }

    String outputBoolean() {
        return LexTerm.outputBoolean(isNonZero());
    }

    String stringOutput() {
        if (!isLong)
            return low != 0 ? (low >= 0x1000000 || low <= -0x1000000 ? "(jint)0x"
                    + Integer.toHexString(low) + "L"
                    : "(jint)" + Integer.toString(low)
                            + (low >= 30000 || low <= -30000 ? "L" : ""))
                    : "0";
        return "JLONG_C("
                + ((low < 0 ? high + 1 : high) != 0 || low >= 0x1000000
                        || low <= -0x1000000 ? "0x"
                        + (high != 0 ? Integer.toHexString(high)
                                + toPaddedHex(low, false) : Integer
                                .toHexString(low)) : Integer.toString(low))
                + ")";
    }

    static String toPaddedHex(int intValue, boolean upperCase) {
        char[] chars = new char[8];
        int pos = 8;
        while (pos-- > 0) {
            chars[pos] = (char) (((intValue & 0xf) < 10 ? '0'
                    : upperCase ? 'A' - 10 : 'a' - 10) + (intValue & 0xf));
            intValue >>>= 4;
        }
        return new String(chars);
    }
}
