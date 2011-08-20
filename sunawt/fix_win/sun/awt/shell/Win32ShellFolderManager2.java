/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)Win32ShellFolderManager2.java    1.2 03/02/18
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.awt.shell;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.AccessController;
import java.util.*;
import sun.security.action.LoadLibraryAction;

// NOTE: This class supersedes Win32ShellFolderManager, which will be removed
//       from distribution after version 1.4.2. The old class can be used instead
//       of this one by setting -Dswing.disableFileChooserSpeedFix=true

/**
 * @author Michael Martak
 * @author Leif Samuelsson
 * @author Kenneth Russell
 * @since 1.4
 */

public class Win32ShellFolderManager2 extends ShellFolderManager {

    static {
        // Load library here
        AccessController.doPrivileged(new LoadLibraryAction("awt"));
    }

    public ShellFolder createShellFolder(File file) throws FileNotFoundException {
        return createShellFolder(getDesktop(), file);
    }

    static Win32ShellFolder2 createShellFolder(Win32ShellFolder2 parent, File file) throws FileNotFoundException {
        long pIDL;
        try {
            pIDL = parent.parseDisplayName(file.getCanonicalPath());
        } catch (IOException e) {
            pIDL = 0;
        }

        if (pIDL == 0) {
            // Shouldn't happen but watch for it anyway
            throw new FileNotFoundException("File " + file.getAbsolutePath() + " not found");
        }
        Win32ShellFolder2 folder = createShellFolderFromRelativePIDL(parent, pIDL);
        Win32ShellFolder2.releasePIDL(pIDL);
        return folder;
    }

    static Win32ShellFolder2 createShellFolderFromRelativePIDL(Win32ShellFolder2 parent, long pIDL) {
        // Walk down this relative pIDL, creating new nodes for each of the entries
        while (pIDL != 0) {
            long curPIDL = Win32ShellFolder2.copyFirstPIDLEntry(pIDL);
            if (curPIDL != 0) {
                parent = new Win32ShellFolder2(parent, curPIDL);
                pIDL = Win32ShellFolder2.getNextPIDLEntry(pIDL);
            }
        }
        return parent;
    }

    // Special folders
    private static Win32ShellFolder2 desktop;
    private static Win32ShellFolder2 drives;
    private static Win32ShellFolder2 recent;
    private static Win32ShellFolder2 network;
    private static Win32ShellFolder2 personal;

    private static String osName = System.getProperty("os.name");
    private static boolean isXP = (osName != null && osName.startsWith("Windows XP"));

    static Win32ShellFolder2 getDesktop() {
        if (desktop == null) {
            try {
                desktop = new Win32ShellFolder2(null, Win32ShellFolder2.DESKTOP, true);
            } catch (IOException e) {
                desktop = null;
            }
        }
        return desktop;
    }

    static Win32ShellFolder2 getDrives() {
        if (drives == null) {
            try {
                drives = new Win32ShellFolder2(getDesktop(), Win32ShellFolder2.DRIVES, true);
            } catch (IOException e) {
                drives = null;
            }
        }
        return drives;
    }

    static Win32ShellFolder2 getRecent() {
        if (recent == null) {
            try {
                String path = Win32ShellFolder2.getFileSystemPath(Win32ShellFolder2.RECENT);
                if (path != null) {
                    recent = (Win32ShellFolder2) createShellFolder(getDesktop(), new File(path));
                }
            } catch (IOException e) {
                recent = null;
            }
        }
        return recent;
    }

    static Win32ShellFolder2 getNetwork() {
        if (network == null) {
            try {
                network = new Win32ShellFolder2(getDesktop(), Win32ShellFolder2.NETWORK, true);
            } catch (IOException e) {
                network = null;
            }
        }
        return network;
    }

    static Win32ShellFolder2 getPersonal() {
        if (personal == null) {
            try {
                String path = Win32ShellFolder2.getFileSystemPath(Win32ShellFolder2.PERSONAL);
                if (path != null) {
                    Win32ShellFolder2 desktop = getDesktop();
                    personal = desktop.getChildByPath(path);
                    if (personal == null) {
                        personal = (Win32ShellFolder2)createShellFolder(getDesktop(), new File(path));
                    }
                    if (personal != null) {
                        personal.setIsPersonal();
                    }
                }
            } catch (IOException e) {
                personal = null;
            }
        }
        return personal;
    }


    private static File[] roots;

    /**
     * @param key a <code>String</code>
     *  "fileChooserDefaultFolder":
     *    Returns a <code>File</code> - the default shellfolder for a new filechooser
     *  "roots":
     *    Returns a <code>File[]</code> - containing the root(s) of the displayable hierarchy
     *  "fileChooserComboBoxFolders":
     *    Returns a <code>File[]</code> - an array of shellfolders representing the list to
     *    show by default in the file chooser's combobox
     *   "fileChooserShortcutPanelFolders":
     *    Returns a <code>File[]</code> - an array of shellfolders representing well-known
     *    folders, such as Desktop, Documents, History, Network, Home, etc.
     *    This is used in the shortcut panel of the filechooser on Windows 2000
     *    and Windows Me.
     *  "fileChooserIcon nn":
     *    Returns an <code>Image</code> - icon nn from resource 124 in comctl32.dll (Windows only).
     *
     * @return An Object matching the key string.
     */
    public Object get(String key) {
        if (key.equals("fileChooserDefaultFolder")) {
            File file = getPersonal();
            if (file == null) {
                file = getDesktop();
            }
            return file;
        } else if (key.equals("roots")) {
            // Should be "History" and "Desktop" ?
            if (roots == null) {
                File desktop = getDesktop();
                if (desktop != null) {
                    roots = new File[] { desktop };
                } else {
                    roots = (File[])super.get(key);
                }
            }
            return roots;
        } else if (key.equals("fileChooserComboBoxFolders")) {
            Win32ShellFolder2 desktop = getDesktop();

            if (desktop != null) {
                ArrayList folders = new ArrayList();
                Win32ShellFolder2 drives = getDrives();

                folders.add(desktop);
                // Add all second level folders
                File[] secondLevelFolders = desktop.listFiles();
                Arrays.sort(secondLevelFolders);
                for (int j = 0; j < secondLevelFolders.length; j++) {
                    Win32ShellFolder2 folder = (Win32ShellFolder2)secondLevelFolders[j];
                    if (!folder.isFileSystem() || folder.isDirectory()) {
                        folders.add(folder);
                        // Add third level for "My Computer"
                        if (folder.equals(drives)) {
                            File[] thirdLevelFolders = folder.listFiles();
                            if (thirdLevelFolders != null) {
                                Arrays.sort(thirdLevelFolders, driveComparator);
                                for (int k = 0; k < thirdLevelFolders.length; k++) {
                                    folders.add(thirdLevelFolders[k]);
                                }
                            }
                        }
                    }
                }
                return folders.toArray(new File[folders.size()]);
            } else {
                return super.get(key);
            }
        } else if (key.equals("fileChooserShortcutPanelFolders")) {
            File[] folders = new File[] {
                getRecent(), getDesktop(), getPersonal(), getDrives(), getNetwork()
            };
            // Remove null references
            ArrayList list = new ArrayList();
            for (int i = 0; i < folders.length; i++) {
                if (folders[i] != null) {
                    list.add(folders[i]);
                }
            }
            return list.toArray(new File[list.size()]);
        } else if (key.startsWith("fileChooserIcon ")) {
            int i = -1;
            String name = key.substring(key.indexOf(" ")+1);
            try {
                i = Integer.parseInt(name);
            } catch (NumberFormatException ex) {
                if (name.equals("ListView")) {
                    i = (isXP) ? 21 : 2;
                } else if (name.equals("DetailsView")) {
                    i = (isXP) ? 23 : 3;
                } else if (name.equals("UpFolder")) {
                    i = (isXP) ? 28 : 8;
                } else if (name.equals("NewFolder")) {
                    i = (isXP) ? 31 : 11;
                }
            }
            if (i >= 0) {
                return Win32ShellFolder2.getFileChooserIcon(i);
            }
        }
        return null;
    }

    /**
     * Does <code>dir</code> represent a "computer" such as a node on the network, or
     * "My Computer" on the desktop.
     */
    public boolean isComputerNode(File dir) {
        if (dir != null && dir == getDrives()) {
            return true;
        } else {
            String path = dir.getAbsolutePath();
            return (path.startsWith("\\\\") && path.indexOf("\\", 2) < 0);      //Network path
        }
    }

    public boolean isFileSystemRoot(File dir) {
        //Note: Removable drives don't "exist" but are listed in "My Computer"
        if (dir != null) {
            Win32ShellFolder2 drives = getDrives();
            if (dir instanceof Win32ShellFolder2) {
                Win32ShellFolder2 sf = (Win32ShellFolder2)dir;
                if (sf.isFileSystem()) {
                    if (sf.parent != null) {
                        return sf.parent.equals(drives);
                    }
                    // else fall through ...
                } else {
                    return false;
                }
            }
            String path = dir.getPath();
            return (path.length() == 3
                    && path.charAt(1) == ':'
                    && Arrays.asList(drives.listFiles()).contains(dir));
        }
        return false;
    }

    private Comparator driveComparator = new Comparator() {
        public int compare(Object o1, Object o2) {
            return ((ShellFolder)o1).getPath().compareTo(((ShellFolder)o2).getPath());
        }
    };


    public void sortFiles(List files) {
        Collections.sort(files, fileComparator);
    }

    private static List topFolderList = null;
    static int compareShellFolders(Win32ShellFolder2 sf1, Win32ShellFolder2 sf2) {
        boolean special1 = sf1.isSpecial();
        boolean special2 = sf2.isSpecial();

        if (special1 || special2) {
            if (topFolderList == null) {
                ArrayList tmpTopFolderList = new ArrayList();
                tmpTopFolderList.add(Win32ShellFolderManager2.getPersonal());
                tmpTopFolderList.add(Win32ShellFolderManager2.getDesktop());
                tmpTopFolderList.add(Win32ShellFolderManager2.getDrives());
                tmpTopFolderList.add(Win32ShellFolderManager2.getNetwork());
                topFolderList = tmpTopFolderList;
            }
            int i1 = topFolderList.indexOf(sf1);
            int i2 = topFolderList.indexOf(sf2);
            if (i1 >= 0 && i2 >= 0) {
                return (i1 - i2);
            } else if (i1 >= 0) {
                return -1;
            } else if (i2 >= 0) {
                return 1;
            }
        }

        // Non-file shellfolders sort before files
        if (special1 && !special2) {
            return -1;
        } else if (special2 && !special1) {
            return  1;
        }

        return compareNames(sf1.getAbsolutePath(), sf2.getAbsolutePath());
    }

    static int compareFiles(File f1, File f2) {
        if (f1 instanceof Win32ShellFolder2) {
            return f1.compareTo(f2);
        }
        if (f2 instanceof Win32ShellFolder2) {
            return -1 * f2.compareTo(f1);
        }
        return compareNames(f1.getName(), f2.getName());
    }

    static int compareNames(String name1, String name2) {
        // First ignore case when comparing
        int diff = name1.toLowerCase().compareTo(name2.toLowerCase());
        if (diff != 0) {
            return diff;
        } else {
            // May differ in case (e.g. "mail" vs. "Mail")
            // We need this test for consistent sorting
            return name1.compareTo(name2);
        }
    }

    private Comparator fileComparator = new Comparator() {
        public int compare(Object a, Object b) {
            return compare((File)a, (File)b);
        }

        public int compare(File f1, File f2) {
            return compareFiles(f1, f2);
        }
    };
}
