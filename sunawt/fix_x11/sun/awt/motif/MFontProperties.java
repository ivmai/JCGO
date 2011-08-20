/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)MFontProperties.java     1.7 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.awt.motif;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Properties;
import sun.awt.FontProperties;


public class MFontProperties extends FontProperties {

    public MFontProperties() {
        super();
    }

    /**
     * Sets the OS name and version from environment information.
     */
    protected void setOsNameAndVersion(){
        super.setOsNameAndVersion();

        if (osName.indexOf("SunOS") != -1 || osName.indexOf("Solaris") != -1) {
            //don't care os name on Solaris
            osName = null;
        }
        else if (osName.indexOf("Linux") != -1) {
            try{
                File f;
                if ((f = new File("/etc/redhat-release")) != null && f.canRead()){
                    osName = "Redhat";
                    osVersion = getVersionString(f);
                }
                else
                if ((f = new File("/etc/turbolinux-release")) != null && f.canRead()){
                    osName = "Turbo";
                    osVersion = getVersionString(f);
                }
                else
                if ((f = new File("/etc/sun-release")) != null && f.canRead()){
                    osName = "Sun";
                    osVersion = getVersionString(f);
                }
                else
                if ((f = new File("/etc/SuSE-release")) != null && f.canRead()){
                    osName = "SuSE";
                    osVersion = getVersionString(f);
                }
            }
            catch (Exception e){
            }
        }
        return;
    }

    private String getVersionString(File f){
        BufferedReader br;
        String line;
        char[] chars;
        int    len, p, q;
        try {
            br = new BufferedReader(new InputStreamReader (new FileInputStream (f.getPath())));
            line = br.readLine();
            br.close();
            if (line == null)
                return null;
            chars = line.toCharArray();
            len = chars.length;
            p = q = 0;
            while (p < len && !Character.isDigit(chars[p])) {p++;}
            if (p < len){
                q = p;
                while (q < len
                       && (chars[q] == '.' || Character.isDigit(chars[q]))){
                    q++;
                }
                return new String(chars, p, q - p);
            }
        }
        catch (Exception e){
        }
        return null;
    }

    // overrides FontProperties.getFallbackFamilyName
    // REMIND: remove this method and references to it from the next feature release.
    public String getFallbackFamilyName(String fontName, String defaultFallback) {
        // maintain compatibility with old font.properties files, which
        // either had aliases for TimesRoman & Co. or defined mappings for them.
        String compatibilityName = getCompatibilityFamilyName(fontName);
        if (compatibilityName != null) {
            return compatibilityName;
        }
        return defaultFallback;
    }
}
