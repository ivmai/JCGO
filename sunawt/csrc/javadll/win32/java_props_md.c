/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 **
 * Comment: contains win32-specific fixes.
 */

/*
 * @(#)java_props_md.c  1.36 03/04/25
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

#include <windows.h>

#define DllExport __declspec(dllexport)

/* Encodings for Windows language groups. According to
   www.microsoft.com/globaldev/faqs/locales.asp,
   some locales do not have codepages, and are
   supported in Windows 2000/XP solely through Unicode.
   In this case, we use utf-8 encoding */

static char *encoding_names[] = {
    "Cp1250",    /*  0:Latin 2  */
    "Cp1251",    /*  1:Cyrillic */
    "Cp1252",    /*  2:Latin 1  */
    "Cp1253",    /*  3:Greek    */
    "Cp1254",    /*  4:Latin 5  */
    "Cp1255",    /*  5:Hebrew   */
    "Cp1256",    /*  6:Arabic   */
    "Cp1257",    /*  7:Baltic   */
    "Cp1258",    /*  8:Viet Nam */
    "MS874",     /*  9:Thai     */
    "MS932",     /* 10:Japanese */
    "GBK",       /* 11:PRC GBK  */
    "MS949",     /* 12:Korean Extended Wansung */
    "MS950",     /* 13:Chinese (Taiwan, Hongkong, Macau) */
    "UTF-8",     /* 14:Unicode  */
    "MS1361",    /* 15:Korean Johab */
};

/*
 * List mapping from LanguageID to Java locale IDs.
 * The entries in this list should not be construed to suggest we actually have
 * full locale-data and other support for all of these locales; these are
 * merely all of the Windows locales for which we could construct an accurate
 * locale ID.
 *
 * Some of the language IDs below are not yet used by Windows, but were
 * defined by Microsoft for other products, such as Office XP. They may
 * become Windows language IDs in the future.
 *
 */
typedef struct LANGIDtoLocale {
    WORD    langID;
    WORD    encoding;
    char*   javaID;
} LANGIDtoLocale;

static LANGIDtoLocale langIDMap[] = {
    /* Fallback locales to use when the country code */
    /* doesn't match anything we have.               */
    { 0x01,    6, "ar" },
    { 0x02,    1, "bg" },
    { 0x03,    2, "ca" },
    { 0x04,   11, "zh" },
    { 0x05,    0, "cs" },
    { 0x06,    2, "da" },
    { 0x07,    2, "de" },
    { 0x08,    3, "el" },
    { 0x09,    2, "en" },
    { 0x0a,    2, "es" },
    { 0x0b,    2, "fi" },
    { 0x0c,    2, "fr" },
    { 0x0d,    5, "iw" },
    { 0x0e,    0, "hu" },
    { 0x0f,    2, "is" },
    { 0x10,    2, "it" },
    { 0x11,   10, "ja" },
    { 0x12,   12, "ko" },
    { 0x13,    2, "nl" },
    { 0x14,    2, "no" },
    { 0x15,    0, "pl" },
    { 0x16,    2, "pt" },
    { 0x17,    2, "rm" },
    { 0x18,    0, "ro" },
    { 0x19,    1, "ru" },
    { 0x1a,    0, "sh" },
    { 0x1b,    0, "sk" },
    { 0x1c,    0, "sq" },
    { 0x1d,    2, "sv" },
    { 0x1e,    9, "th" },
    { 0x1f,    4, "tr" },
    { 0x20,    2, "ur" },
    { 0x21,    2, "in" },
    { 0x22,    1, "uk" },
    { 0x23,    1, "be" },
    { 0x24,    0, "sl" },
    { 0x25,    7, "et" },
    { 0x26,    7, "lv" },
    { 0x27,    7, "lt" },
    { 0x29,    6, "fa" },
    { 0x2a,    8, "vi" },
    { 0x2b,   14, "hy" },
    { 0x2c,    4, "az" },
    { 0x2d,    2, "eu" },
    { 0x2f,    1, "mk" },
    { 0x31,    2, "ts" },
    { 0x32,    2, "tn" },
    { 0x34,    2, "xh" },
    { 0x35,    2, "zu" },
    { 0x36,    2, "af" },
    { 0x37,   14, "ka" },
    { 0x38,    2, "fo" },
    { 0x39,   14, "hi" },
    { 0x3a,    2, "mt" },
    { 0x3c,    2, "gd" },
    { 0x3d,    2, "yi" },
    { 0x3e,    2, "ms" },
    { 0x3f,    1, "kk" },
    { 0x40,    1, "ky" },
    { 0x41,    2, "sw" },
    { 0x43,    1, "uz" },
    { 0x44,    1, "tt" },
    { 0x46,   14, "pa" },
    { 0x47,   14, "gu" },
    { 0x49,   14, "ta" },
    { 0x4a,   14, "te" },
    { 0x4b,   14, "kn" },
    { 0x4e,   14, "mr" },
    { 0x4f,   14, "sa" },
    { 0x50,    1, "mn" },
    { 0x56,    2, "gl" },
    /* mappings for real Windows LCID values */
    { 0x0401,  6, "ar_SA" },
    { 0x0402,  1, "bg_BG" },
    { 0x0403,  2, "ca_ES" },
    { 0x0404, 13, "zh_TW" },
    { 0x0405,  0, "cs_CZ" },
    { 0x0406,  2, "da_DK" },
    { 0x0407,  2, "de_DE" },
    { 0x0408,  3, "el_GR" },
    { 0x0409,  2, "en_US" },
    { 0x040a,  2, "es_ES" },  /* (traditional sort) */
    { 0x040b,  2, "fi_FI" },
    { 0x040c,  2, "fr_FR" },
    { 0x040d,  5, "iw_IL" },
    { 0x040e,  0, "hu_HU" },
    { 0x040f,  2, "is_IS" },
    { 0x0410,  2, "it_IT" },
    { 0x0411, 10, "ja_JP" },
    { 0x0412, 12, "ko_KR" },
    { 0x0413,  2, "nl_NL" },
    { 0x0414,  2, "no_NO" },
    { 0x0415,  0, "pl_PL" },
    { 0x0416,  2, "pt_BR" },
    { 0x0417,  2, "rm_CH" },
    { 0x0418,  0, "ro_RO" },
    { 0x0419,  1, "ru_RU" },
    { 0x041a,  0, "hr_HR" },
    { 0x041b,  0, "sk_SK" },
    { 0x041c,  0, "sq_AL" },
    { 0x041d,  2, "sv_SE" },
    { 0x041e,  9, "th_TH" },
    { 0x041f,  4, "tr_TR" },
    { 0x0420,  6, "ur_PK" },
    { 0x0421,  2, "in_ID" },
    { 0x0422,  1, "uk_UA" },
    { 0x0423,  1, "be_BY" },
    { 0x0424,  0, "sl_SI" },
    { 0x0425,  7, "et_EE" },
    { 0x0426,  7, "lv_LV" },
    { 0x0427,  7, "lt_LT" },
    { 0x0429,  6, "fa_IR" },
    { 0x042a,  8, "vi_VN" },
    { 0x042b, 14, "hy_AM" },  /* Armenian  */
    { 0x042c,  4, "az_AZ" },  /* Azeri_Latin */
    { 0x042d,  2, "eu_ES" },
    { 0x042f,  1, "mk_MK" },
    { 0x0431,  2, "ts" },     /* (country?) */
    { 0x0432,  2, "tn_BW" },
    { 0x0434,  2, "xh" },     /* (country?) */
    { 0x0435,  2, "zu" },     /* (country?) */
    { 0x0436,  2, "af_ZA" },
    { 0x0437, 14, "ka_GE" },  /* Georgian   */
    { 0x0438,  2, "fo_FO" },
    { 0x0439, 14, "hi_IN" },
    { 0x043a,  2, "mt_MT" },
    { 0x043c,  2, "gd_GB" },
    { 0x043d,  2, "yi" },     /* (country?) */
    { 0x043e,  2, "ms_MY" },
    { 0x043f,  1, "kk_KZ" },  /* Kazakh */
    { 0x0440,  1, "ky_KG" },  /* Kyrgyz     */
    { 0x0441,  2, "sw_KE" },
    { 0x0443,  1, "uz_UZ" },  /* Uzbek_Cyrillic*/
    { 0x0444,  1, "tt" },     /* Tatar, no ISO-3166 abbreviation */
    { 0x0446, 14, "pa_IN" },  /* Punjabi   */
    { 0x0447, 14, "gu_IN" },  /* Gujarati  */
    { 0x0449, 14, "ta_IN" },  /* Tamil     */
    { 0x044a, 14, "te_IN" },  /* Telugu    */
    { 0x044b, 14, "kn_IN" },  /* Kannada   */
    { 0x044e, 14, "mr_IN" },  /* Marathi   */
    { 0x044f, 14, "sa_IN" },  /* Sanskrit  */
    { 0x0450,  1, "mn_MN" },  /* Mongolian */
    { 0x0456,  2, "gl_ES" },  /* Galician  */
    { 0x0801,  6, "ar_IQ" },
    { 0x0804, 11, "zh_CN" },
    { 0x0807,  2, "de_CH" },
    { 0x0809,  2, "en_GB" },
    { 0x080a,  2, "es_MX" },
    { 0x080c,  2, "fr_BE" },
    { 0x0810,  2, "it_CH" },
    { 0x0812, 15, "ko_KR" },  /* Korean(Johab)*/
    { 0x0813,  2, "nl_BE" },
    { 0x0814,  2, "no_NO_NY" },
    { 0x0816,  2, "pt_PT" },
    { 0x0818,  0, "ro_MD" },
    { 0x0819,  1, "ru_MD" },
    { 0x081a,  0, "sh_YU" },
    { 0x081d,  2, "sv_FI" },
    { 0x082c,  1, "az_AZ" },  /* Azeri_Cyrillic */
    { 0x083c,  2, "ga_IE" },
    { 0x083e,  2, "ms_BN" },
    { 0x0843,  4, "uz_UZ" },  /* Uzbek_Latin  */
    { 0x0c01,  6, "ar_EG" },
    { 0x0c04, 13, "zh_HK" },
    { 0x0c07,  2, "de_AT" },
    { 0x0c09,  2, "en_AU" },
    { 0x0c0a,  2, "es_ES" },  /* (modern sort) */
    { 0x0c0c,  2, "fr_CA" },
    { 0x0c1a,  1, "sr_YU" },
    { 0x1001,  6, "ar_LY" },
    { 0x1004, 11, "zh_SG" },
    { 0x1007,  2, "de_LU" },
    { 0x1009,  2, "en_CA" },
    { 0x100a,  2, "es_GT" },
    { 0x100c,  2, "fr_CH" },
    { 0x1401,  6, "ar_DZ" },
    { 0x1404, 13, "zh_MO" },
    { 0x1407,  2, "de_LI" },
    { 0x1409,  2, "en_NZ" },
    { 0x140a,  2, "es_CR" },
    { 0x140c,  2, "fr_LU" },
    { 0x1801,  6, "ar_MA" },
    { 0x1809,  2, "en_IE" },
    { 0x180a,  2, "es_PA" },
    { 0x180c,  2, "fr_MC" },
    { 0x1c01,  6, "ar_TN" },
    { 0x1c09,  2, "en_ZA" },
    { 0x1c0a,  2, "es_DO" },
    { 0x2001,  6, "ar_OM" },
    { 0x2009,  2, "en_JM" },
    { 0x200a,  2, "es_VE" },
    { 0x2401,  6, "ar_YE" },
    { 0x2409,  2, "en" }, /* ("Caribbean" }, which could be any of many countries) */
    { 0x240a,  2, "es_CO" },
    { 0x2801,  6, "ar_SY" },
    { 0x2809,  2, "en_BZ" },
    { 0x280a,  2, "es_PE" },
    { 0x2c01,  6, "ar_JO" },
    { 0x2c09,  2, "en_TT" },
    { 0x2c0a,  2, "es_AR" },
    { 0x3001,  6, "ar_LB" },
    { 0x3009,  2, "en_ZW" },
    { 0x300a,  2, "es_EC" },
    { 0x3401,  6, "ar_KW" },
    { 0x3409,  2, "en_PH" },
    { 0x340a,  2, "es_CL" },
    { 0x3801,  6, "ar_AE" },
    { 0x380a,  2, "es_UY" },
    { 0x3c01,  6, "ar_BH" },
    { 0x3c0a,  2, "es_PY" },
    { 0x4001,  6, "ar_QA" },
    { 0x400a,  2, "es_BO" },
    { 0x440a,  2, "es_SV" },
    { 0x480a,  2, "es_HN" },
    { 0x4c0a,  2, "es_NI" },
    { 0x500a,  2, "es_PR" }
};

/*
 * binary-search our list of LANGID values.  If we don't find the
 * one we're looking for, mask out the country code and try again
 * with just the primary language ID
 */
static int
getLocaleEntryIndex(LANGID langID)
{
    int index = -1;
    int tries = 0;
    do {
        int lo, hi, mid;
        lo = 0;
        hi = sizeof(langIDMap) / sizeof(LANGIDtoLocale);
        while (index == -1 && lo < hi) {
            mid = (lo + hi) / 2;
            if (langIDMap[mid].langID == langID) {
                index = mid;
            } else if (langIDMap[mid].langID > langID) {
                hi = mid;
            } else {
                lo = mid + 1;
            }
        }
        langID = PRIMARYLANGID(langID);
        ++tries;
    } while (index == -1 && tries < 2);

    return index;
}

static char *
getEncodingInternal(int index)
{
    char * ret = encoding_names[langIDMap[index].encoding];

    /* Traditional Chinese Windows should use MS950_HKSCS as the */
    /* default encoding, if HKSCS patch has been installed.      */
    /* "old" MS950 0xfa41 -> u+e001                              */
    /* "new" MS950 0xfa41 -> u+92db                              */
    if (strcmp(ret, "MS950") == 0) {
        char  mbChar[2] = { (char)0xfa, (char)0x41 };
        WCHAR  unicodeChar;
        MultiByteToWideChar(CP_ACP, 0, mbChar, 2, &unicodeChar, 1);
        if (unicodeChar == 0x92db) {
            ret = "MS950_HKSCS";
        }
    } else {
        /* SimpChinese Windows should use GB18030 as the default     */
        /* encoding, if gb18030 patch has been installed (on windows */
        /* 2000/XP, (1)Codepage 54936 will be available              */
        /* (2)simsun18030.ttc will exist under system fonts dir).    */
        if (strcmp(ret, "GBK") == 0 && IsValidCodePage(54936)) {
            ret = "GB18030";
        }
    }

    return ret;
}

/* Exported entries for AWT */
DllExport const char *
getEncodingFromLangID(LANGID langID)
{
    int index = getLocaleEntryIndex(langID);

    if (index != (-1)) {
        return getEncodingInternal(index);
    } else {
        return "Cp1252";
    }
}

DllExport const char *
getJavaIDFromLangID(LANGID langID)
{
    int index = getLocaleEntryIndex(langID);

    if (index != (-1)) {
        return langIDMap[index].javaID;
    } else {
        return NULL;
    }
}
