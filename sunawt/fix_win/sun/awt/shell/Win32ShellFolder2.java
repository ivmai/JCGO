/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)Win32ShellFolder2.java   1.5 03/05/09
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.awt.shell;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.AccessController;
import java.util.*;
import sun.security.action.GetPropertyAction;

// NOTE: This class supersedes Win32ShellFolder, which will be removed from
//       distribution after version 1.4.2. The old class can be used instead
//       of this one by setting -Dswing.disableFileChooserSpeedFix=true

/**
 * Win32 Shell Folders
 * <P>
 * <BR>
 * There are two fundamental types of shell folders : file system folders
 * and non-file system folders.  File system folders are relatively easy
 * to deal with.  Non-file system folders are items such as My Computer,
 * Network Neighborhood, and the desktop.  Some of these non-file system
 * folders have special values and properties.
 * <P>
 * <BR>
 * Win32 keeps two basic data structures for shell folders.  The first
 * of these is called an ITEMIDLIST.  Usually a pointer, called an
 * LPITEMIDLIST, or more frequently just "PIDL".  This structure holds
 * a series of identifiers and can be either relative to the desktop
 * (an absolute PIDL), or relative to the shell folder that contains them.
 * Some Win32 functions can take absolute or relative PIDL values, and
 * others can only accept relative values.
 * <BR>
 * The second data structure is an IShellFolder COM interface.  Using
 * this interface, one can enumerate the relative PIDLs in a shell
 * folder, get attributes, etc.
 * <BR>
 * All Win32ShellFolder2 objects which are folder types (even non-file
 * system folders) contain an IShellFolder object. Files are named in
 * directories via relative PIDLs.
 *
 * @author Michael Martak
 * @author Leif Samuelsson
 * @author Kenneth Russell
 * @since 1.4 */

final class Win32ShellFolder2 extends ShellFolder {

    private static native void initIDs();

    private static final boolean is98;

    static {
        String osName = System.getProperty("os.name");
        is98 = (osName != null && osName.startsWith("Windows 98"));

        initIDs();
    }

    // Win32 Shell Folder Constants
    public static final int DESKTOP = 0x0000;
    public static final int INTERNET = 0x0001;
    public static final int PROGRAMS = 0x0002;
    public static final int CONTROLS = 0x0003;
    public static final int PRINTERS = 0x0004;
    public static final int PERSONAL = 0x0005;
    public static final int FAVORITES = 0x0006;
    public static final int STARTUP = 0x0007;
    public static final int RECENT = 0x0008;
    public static final int SENDTO = 0x0009;
    public static final int BITBUCKET = 0x000a;
    public static final int STARTMENU = 0x000b;
    public static final int DESKTOPDIRECTORY = 0x0010;
    public static final int DRIVES = 0x0011;
    public static final int NETWORK = 0x0012;
    public static final int NETHOOD = 0x0013;
    public static final int FONTS = 0x0014;
    public static final int TEMPLATES = 0x0015;
    public static final int COMMON_STARTMENU = 0x0016;
    public static final int COMMON_PROGRAMS = 0X0017;
    public static final int COMMON_STARTUP = 0x0018;
    public static final int COMMON_DESKTOPDIRECTORY = 0x0019;
    public static final int APPDATA = 0x001a;
    public static final int PRINTHOOD = 0x001b;
    public static final int ALTSTARTUP = 0x001d;
    public static final int COMMON_ALTSTARTUP = 0x001e;
    public static final int COMMON_FAVORITES = 0x001f;
    public static final int INTERNET_CACHE = 0x0020;
    public static final int COOKIES = 0x0021;
    public static final int HISTORY = 0x0022;

    // Win32 shell folder attributes
    public static final int ATTRIB_CANCOPY          = 0x00000001;
    public static final int ATTRIB_CANMOVE          = 0x00000002;
    public static final int ATTRIB_CANLINK          = 0x00000004;
    public static final int ATTRIB_CANRENAME        = 0x00000010;
    public static final int ATTRIB_CANDELETE        = 0x00000020;
    public static final int ATTRIB_HASPROPSHEET     = 0x00000040;
    public static final int ATTRIB_DROPTARGET       = 0x00000100;
    public static final int ATTRIB_LINK             = 0x00010000;
    public static final int ATTRIB_SHARE            = 0x00020000;
    public static final int ATTRIB_READONLY         = 0x00040000;
    public static final int ATTRIB_GHOSTED          = 0x00080000;
    public static final int ATTRIB_HIDDEN           = 0x00080000;
    public static final int ATTRIB_FILESYSANCESTOR  = 0x10000000;
    public static final int ATTRIB_FOLDER           = 0x20000000;
    public static final int ATTRIB_FILESYSTEM       = 0x40000000;
    public static final int ATTRIB_HASSUBFOLDER     = 0x80000000;
    public static final int ATTRIB_VALIDATE         = 0x01000000;
    public static final int ATTRIB_REMOVABLE        = 0x02000000;
    public static final int ATTRIB_COMPRESSED       = 0x04000000;
    public static final int ATTRIB_BROWSABLE        = 0x08000000;
    public static final int ATTRIB_NONENUMERATED    = 0x00100000;
    public static final int ATTRIB_NEWCONTENT       = 0x00200000;

    // IShellFolder::GetDisplayNameOf constants
    public static final int SHGDN_NORMAL            = 0;
    public static final int SHGDN_INFOLDER          = 1;
    public static final int SHGDN_INCLUDE_NONFILESYS= 0x2000;
    public static final int SHGDN_FORADDRESSBAR     = 0x4000;
    public static final int SHGDN_FORPARSING        = 0x8000;

    /*
     * We keep track of shell folders through the IShellFolder
     * interface of their parents plus their relative PIDL.
     */
    private long pIShellFolder = 0;
    private long relativePIDL = 0;
    /*
     * This is cached as a concession to getFolderType(), which needs
     * an absolute PIDL.
     */
    private long absolutePIDL = 0;

    /*
     * The following are for caching various shell folder properties.
     */
    private long pIShellIcon = -1L;
    private String folderType = null;
    private String displayName = null;
    private Image smallIcon = null;
    private Image largeIcon = null;

    /*
     * The following is to identify the My Documents folder as being special
     */
    private boolean isPersonal;


    /**
     * Create a non-file system special shell folder, such as the
     * desktop or Network Neighborhood.
     */
    Win32ShellFolder2(ShellFolder parent, int shellFolderType, boolean isSpecial/*ignored*/) throws IOException {
        // Desktop is parent of DRIVES and NETWORK, not necessarily
        // other special shell folders.
        super(parent,
              (getFileSystemPath(shellFolderType) == null)
                ? ("ShellFolder: 0x"+Integer.toHexString(shellFolderType)) : getFileSystemPath(shellFolderType));
        if (shellFolderType == DESKTOP) {
            initDesktop();
        } else {
            initSpecial(getDesktop().getIShellFolder(), shellFolderType);
        }
    }

    /**
     * Create a system shell folder
     */
    Win32ShellFolder2(Win32ShellFolder2 parent, long pIShellFolder, long relativePIDL, String path) {
        super(parent, (path != null) ? path : "ShellFolder: ");
        this.pIShellFolder = pIShellFolder;
        this.relativePIDL = relativePIDL;
    }


    /**
     * Creates a shell folder with a parent and relative PIDL
     */
    Win32ShellFolder2(Win32ShellFolder2 parent, long relativePIDL) {
        super(parent, getFileSystemPath(parent.getIShellFolder(), relativePIDL));
        this.relativePIDL = relativePIDL;
        String absolutePath = getAbsolutePath();
    }

    // Initializes the desktop shell folder
    private native void initDesktop();
    // Initializes a special, non-file system shell folder
    // from one of the above constants
    private native void initSpecial(long desktopIShellFolder,
        int shellFolderType);

    /** Marks this folder as being the My Documents (Personal) folder */
    public void setIsPersonal() {
        isPersonal = true;
    }

    /**
     * This method is implemented to make sure that no instances
     * of <code>ShellFolder</code> are ever serialized. If <code>isFileSystem()</code> returns
     * <code>true</code>, then the object is representable with an instance of
     * <code>java.io.File</code> instead. If not, then the object depends
     * on native PIDL state and should not be serialized.
     *
     * @returns a <code>java.io.File</code> replacement object. If the folder
     * is a not a normal directory, then returns the first non-removable
     * drive (normally "C:\").
     */
    protected Object writeReplace() throws java.io.ObjectStreamException {
        if (isFileSystem()) {
            return new File(getPath());
        } else {
            Win32ShellFolder2 drives = Win32ShellFolderManager2.getDrives();
            if (drives != null) {
                File[] driveRoots = drives.listFiles();
                if (driveRoots != null) {
                    for (int i = 0; i < driveRoots.length; i++) {
                        if (driveRoots[i] instanceof Win32ShellFolder2) {
                            Win32ShellFolder2 sf = (Win32ShellFolder2)driveRoots[i];
                            if (sf.isFileSystem() && !sf.hasAttribute(ATTRIB_REMOVABLE)) {
                                return new File(sf.getPath());
                            }
                        }
                    }
                }
            }
            // Ouch, we have no hard drives. Return something "valid" anyway.
            return new File("C:\\");
        }
    }


    /**
     * Finalizer to clean up any COM objects or PIDLs used by this object.
     */
    protected void finalize() throws Throwable {
        if (relativePIDL != 0) {
            releasePIDL(relativePIDL);
            relativePIDL = 0;
        }
        if (absolutePIDL != 0) {
            releasePIDL(absolutePIDL);
            absolutePIDL = 0;
        }
        if (pIShellFolder != 0) {
            releaseIShellFolder(pIShellFolder);
            pIShellFolder = 0;
        }
    }


    // Given a (possibly multi-level) relative PIDL (with respect to
    // the desktop, at least in all of the usage cases in this code),
    // return a pointer to the next entry. Does not mutate the PIDL in
    // any way. Returns 0 if the null terminator is reached.
    // Needs to be accessible to Win32ShellFolderManager2
    static native long getNextPIDLEntry(long pIDL);

    // Given a (possibly multi-level) relative PIDL (with respect to
    // the desktop, at least in all of the usage cases in this code),
    // copy the first entry into a newly-allocated PIDL. Returns 0 if
    // the PIDL is at the end of the list.
    // Needs to be accessible to Win32ShellFolderManager2
    static native long copyFirstPIDLEntry(long pIDL);

    // Given a parent's absolute PIDL and our relative PIDL, build an absolute PIDL
    private static native long combinePIDLs(long ppIDL, long pIDL);

    // Release a PIDL object
    // Needs to be accessible to Win32ShellFolderManager2
    static native void releasePIDL(long pIDL);

    // Release an IShellFolder object
    private static native void releaseIShellFolder(long pIShellFolder);

    /**
     * Accessor for IShellFolder
     */
    public long getIShellFolder() {
        if (pIShellFolder == 0) {
            /* assertion: isDirectory() && parent != null */
            long parentIShellFolder = getParentIShellFolder();
            if (parentIShellFolder == 0) {
                throw new InternalError("Parent IShellFolder was null for " + getAbsolutePath());
            }
            // We are a directory with a parent and a relative PIDL.
            // We want to bind to the parent so we get an IShellFolder instance associated with us.
            pIShellFolder = bindToObject(parentIShellFolder, relativePIDL);
            if (pIShellFolder == 0) {
                throw new InternalError("Unable to bind " + getAbsolutePath() + " to parent");
            }
        }
        return pIShellFolder;
    }

    /**
     * Get the parent ShellFolder's IShellFolder interface
     */
    public long getParentIShellFolder() {
        Win32ShellFolder2 parent = (Win32ShellFolder2)getParentFile();
        if (parent == null) {
            // Parent should only be null if this is the desktop, whose
            // relativePIDL is relative to its own IShellFolder.
            return getIShellFolder();
        }
        return parent.getIShellFolder();
    }

    /**
     * Accessor for relative PIDL
     */
    public long getRelativePIDL() {
        if (relativePIDL == 0) {
            throw new InternalError("Should always have a relative PIDL");
        }
        return relativePIDL;
    }

    private long getAbsolutePIDL() {
        if (parent == null) {
            // This is the desktop
            return getRelativePIDL();
        } else {
            if (absolutePIDL == 0) {
                absolutePIDL = combinePIDLs(((Win32ShellFolder2)parent).getAbsolutePIDL(), getRelativePIDL());
            }

            return absolutePIDL;
        }
    }

    /**
     * Helper function to return the desktop
     */
    public Win32ShellFolder2 getDesktop() {
        return Win32ShellFolderManager2.getDesktop();
    }

    /**
     * Helper function to return the desktop IShellFolder interface
     */
    public long getDesktopIShellFolder() {
        return getDesktop().getIShellFolder();
    }

    private static boolean pathsEqual(String path1, String path2) {
        // Same effective implementation as Win32FileSystem
        return path1.equalsIgnoreCase(path2);
    }

    /**
     * Check to see if two ShellFolder objects are the same
     */
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Win32ShellFolder2)) {
            // Short-circuit circuitous delegation path
            if (!(o instanceof File)) {
                return super.equals(o);
            }
            return pathsEqual(getPath(), ((File) o).getPath());
        }
        Win32ShellFolder2 rhs = (Win32ShellFolder2) o;
        if ((parent == null && rhs.parent != null) ||
            (parent != null && rhs.parent == null)) {
            return false;
        }

        if (isFileSystem() && rhs.isFileSystem()) {
            return pathsEqual(getPath(), rhs.getPath());
        }

        if (parent == null || parent == rhs.parent || parent.equals(rhs.parent)) {
            return pidlsEqual(getParentIShellFolder(), relativePIDL, rhs.relativePIDL);
        }

        return false;
    }

    private static boolean pidlsEqual(long pIShellFolder, long pidl1, long pidl2) {
        return (compareIDs(pIShellFolder, pidl1, pidl2) == 0);
    }
    private static native int compareIDs(long pParentIShellFolder, long pidl1, long pidl2);

    /**
     * @return Whether this is a file system shell folder
     */
    public boolean isFileSystem() {
        return hasAttribute(ATTRIB_FILESYSTEM);
    }

    /**
     * Return whether the given attribute flag is set for this object
     */
    public boolean hasAttribute(int attribute) {
        // Caching at this point doesn't seem to be cost efficient
        return (getAttributes0(getParentIShellFolder(), getRelativePIDL(), attribute) & attribute) != 0;
    }

    /**
     * Returns the queried attributes specified in attrsMask.
     *
     * Could plausibly be used for attribute caching but have to be
     * very careful not to touch network drives and file system roots
     * with a full attrsMask
     */
    private static native int getAttributes0(long pParentIShellFolder, long pIDL, int attrsMask);

    // Return the path to the underlying file system object
    private static String getFileSystemPath(long parentIShellFolder, long relativePIDL) {
        return getDisplayNameOf(parentIShellFolder, relativePIDL, SHGDN_NORMAL | SHGDN_FORPARSING);
    }
    // Needs to be accessible to Win32ShellFolderManager2
    static native String getFileSystemPath(int csidl) throws IOException;
    private static native String getFileSystemPath(boolean dummy);

    // Return whether the path is a network root.
    // Path is assumed to be non-null
    private static boolean isNetworkRoot(String path) {
        return (path.equals("\\\\") || path.equals("\\") || path.equals("//") || path.equals("/"));
    }

    /**
     * @return The parent shell folder of this shell folder, null if
     * there is no parent
     */
    public File getParentFile() {
        return parent;
    }

    public boolean isDirectory() {
        // Folders with SFGAO_BROWSABLE have "shell extension" handlers and are
        // not traversable in JFileChooser. An exception is "My Documents" on
        // Windows 98.
        return ((hasAttribute(ATTRIB_HASSUBFOLDER) || hasAttribute(ATTRIB_FOLDER))
                && (!hasAttribute(ATTRIB_BROWSABLE) ||
                    (is98 && equals(Win32ShellFolderManager2.getPersonal()))));
    }

    /*
     * Functions for enumerating an IShellFolder's children
     */
    // Returns an IEnumIDList interface for an IShellFolder.  The value
    // returned must be released using releaseEnumObjects().
    private long getEnumObjects(long pIShellFolder, boolean includeHiddenFiles) {
        boolean isDesktop = (pIShellFolder == getDesktopIShellFolder());
        return getEnumObjects(pIShellFolder, isDesktop, includeHiddenFiles);
    }
    // Returns an IEnumIDList interface for an IShellFolder.  The value
    // returned must be released using releaseEnumObjects().
    private native long getEnumObjects(long pIShellFolder, boolean isDesktop,
                                       boolean includeHiddenFiles);
    // Returns the next sequential child as a relative PIDL
    // from an IEnumIDList interface.  The value returned must
    // be released using releasePIDL().
    private native long getNextChild(long pEnumObjects);
    // Releases the IEnumIDList interface
    private native void releaseEnumObjects(long pEnumObjects);

    // Returns the IShellFolder of a child from a parent IShellFolder
    // and a relative PIDL.  The value returned must be released
    // using releaseIShellFolder().
    private static native long bindToObject(long parentIShellFolder, long pIDL);

    /**
     * @return An array of shell folders that are children of this shell folder
     * object, null if this shell folder is empty.
     */
    public File[] listFiles(boolean includeHiddenFiles) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkRead(getPath());
        if (!isDirectory())
            return null;
        if (isLink() && !hasAttribute(ATTRIB_FOLDER))
            return new File[0];

        Win32ShellFolder2 desktop = Win32ShellFolderManager2.getDesktop();
        Win32ShellFolder2 personal = Win32ShellFolderManager2.getPersonal();
        File[] children = null;

        // If we are a directory, we have a parent and (at least) a
        // relative PIDL. We must first ensure we are bound to the
        // parent so we have an IShellFolder to query.
        long pIShellFolder = getIShellFolder();
        // Now we can enumerate the objects in this folder.
        ArrayList list = new ArrayList();
        long pEnumObjects = getEnumObjects(pIShellFolder, includeHiddenFiles);
        if (pEnumObjects != 0) {
            long childPIDL = 0;
            int testedAttrs = ATTRIB_FILESYSTEM | ATTRIB_FILESYSANCESTOR;
            do {
                if (Thread.currentThread().isInterrupted())
                    return new File[0];
                childPIDL = getNextChild(pEnumObjects);
                boolean flag1 = true;
                if (childPIDL != 0 &&
                    (getAttributes0(pIShellFolder, childPIDL, testedAttrs) & testedAttrs) != 0) {
                    Win32ShellFolder2 childFolder = null;
                    if (this.equals(desktop)
                        && personal != null
                        && pidlsEqual(pIShellFolder, childPIDL, personal.relativePIDL)) {
                        childFolder = personal;
                    } else {
                        childFolder = new Win32ShellFolder2(this, childPIDL);
                        flag1 = false;
                    }
                    list.add(childFolder);
                }
                if (flag1)
                    releasePIDL(childPIDL);
            } while (childPIDL != 0);
            releaseEnumObjects(pEnumObjects);
        }
        return (ShellFolder[])list.toArray(new ShellFolder[list.size()]);
    }


    /**
     * Look for (possibly special) child folder by it's path
     *
     * @return The child shellfolder, or null if not found.
     */
    Win32ShellFolder2 getChildByPath(String filePath) {
        long pIShellFolder = getIShellFolder();
        long pEnumObjects =  getEnumObjects(pIShellFolder, true);
        Win32ShellFolder2 child = null;
        long childPIDL = 0;

        while ((childPIDL = getNextChild(pEnumObjects)) != 0) {
            if (getAttributes0(pIShellFolder, childPIDL, ATTRIB_FILESYSTEM) != 0) {
                String path = getFileSystemPath(pIShellFolder, childPIDL);
                if (path != null && path.equalsIgnoreCase(filePath)) {
                    long childIShellFolder = bindToObject(pIShellFolder, childPIDL);
                    child = new Win32ShellFolder2(this, childIShellFolder, childPIDL, path);
                    break;
                }
            }
            releasePIDL(childPIDL);
        }

        releaseEnumObjects(pEnumObjects);
        return child;
    }


    /**
     * @return Whether this shell folder is a link
     */
    public boolean isLink() {
        return hasAttribute(ATTRIB_LINK);
    }

    /**
     * @return Whether this shell folder is marked as hidden
     */
    public boolean isHidden() {
        return hasAttribute(ATTRIB_HIDDEN);
    }


    // Return the link location of a shell folder
    private native String getLinkLocation(long parentIShellFolder,
        long relativePIDL);

    /**
     * @return The shell folder linked to by this shell folder, or null
     * if this shell folder is not a link
     */
    public ShellFolder getLinkLocation() throws FileNotFoundException {
        if (!isLink()) {
            return null;
        }
        String linkLocation = getLinkLocation(getParentIShellFolder(), getRelativePIDL());
        if (linkLocation == null) {
            return null;
        }
        return Win32ShellFolderManager2.createShellFolder((Win32ShellFolder2) parent, new File(linkLocation));
    }

    // Parse a display name into a PIDL relative to the current IShellFolder.
    long parseDisplayName(String name) throws FileNotFoundException {
        try {
            return parseDisplayName0(getIShellFolder(), name);
        } catch (IOException e) {
            throw new FileNotFoundException("Could not find file " + name);
        }
    }
    private static native long parseDisplayName0(long pIShellFolder, String name) throws IOException;

    // Return the display name of a shell folder
    private static native String getDisplayNameOf(long parentIShellFolder,
                                                  long relativePIDL,
                                                  int attrs);

    /**
     * @return The name used to display this shell folder
     */
    public String getDisplayName() {
        if (displayName == null) {
            displayName = getDisplayNameOf(getParentIShellFolder(), getRelativePIDL(), SHGDN_NORMAL);
        }
        return displayName;
    }

    // Return the folder type of a shell folder
    private static native String getFolderType(long pIDL);

    /**
     * @return The type of shell folder as a string
     */
    public String getFolderType() {
        if (folderType == null) {
            folderType = getFolderType(getAbsolutePIDL());
        }
        return folderType;
    }

    // Return the executable type of a file system shell folder
    private native String getExecutableType(byte[] path_bytes);

    /**
     * @return The executable type as a string
     */
    public String getExecutableType() {
        if (!isFileSystem()) {
            return null;
        }
        return getExecutableType(getAbsolutePath().getBytes());
    }



    // Icons

    private static Map smallSystemImages = new HashMap();
    private static Map largeSystemImages = new HashMap();
    private static Map smallLinkedSystemImages = new HashMap();
    private static Map largeLinkedSystemImages = new HashMap();

    private static native long getIShellIcon(long pIShellFolder);
    private static native int getIconIndex(long parentIShellIcon, long relativePIDL);

    // Return the icon of a file system shell folder in the form of an HICON
    private static native long getIcon(String absolutePath, boolean getLargeIcon);
    private static native long extractIcon(long parentIShellFolder, long relativePIDL,
                                           boolean getLargeIcon);
    // Return the bits from an HICON.  This has a side effect of setting
    // the imageHash variable for efficient caching / comparing.
    private native int[] getIconBits(long hIcon, int iconSize);
    // Dispose the HICON
    private native void disposeIcon(long hIcon);

    public static native int[] getFileChooserBitmapBits();

    private long getIShellIcon() {
        if (pIShellIcon == -1L) {
            pIShellIcon = getIShellIcon(getIShellFolder());
        }
        return pIShellIcon;
    }


    static int[] fileChooserBitmapBits = null;
    static Image[] fileChooserIcons = new Image[47];

    static Image getFileChooserIcon(int i) {
        if (fileChooserIcons[i] != null) {
            return fileChooserIcons[i];
        } else {
            if (fileChooserBitmapBits == null) {
                fileChooserBitmapBits = getFileChooserBitmapBits();
            }
            if (fileChooserBitmapBits != null) {
                int nImages = fileChooserBitmapBits.length / (16*16);
                int[] bitmapBits = new int[16 * 16];
                for (int y = 0; y < 16; y++) {
                    for (int x = 0; x < 16; x++) {
                        bitmapBits[y * 16 + x] = fileChooserBitmapBits[y * (nImages * 16) + (i * 16) + x];
                    }
                }
                BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
                img.setRGB(0, 0, 16, 16, bitmapBits, 0, 16);
                fileChooserIcons[i] = img;
            }
        }
        return fileChooserIcons[i];
    }


    private Image makeIcon(long hIcon, boolean getLargeIcon) {
        if (hIcon != 0L && hIcon != -1L) {
            // Get the bits.  This has the side effect of setting the imageHash value for this object.
            int size = getLargeIcon ? 32 : 16;
            int[] iconBits = getIconBits(hIcon, size);
            if (iconBits != null) {
                BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                img.setRGB(0, 0, size, size, iconBits, 0, size);
                return img;
            }
        }
        return null;
    }


    /**
     * @return The icon image used to display this shell folder
     */
    public Image getIcon(boolean getLargeIcon) {
        Image icon = getLargeIcon ? largeIcon : smallIcon;
        if (icon == null) {
            long parentIShellIcon = (parent != null) ? ((Win32ShellFolder2)parent).getIShellIcon() : 0L;
            long relativePIDL = getRelativePIDL();

            if (isFileSystem()) {
                // These are cached per type (using the index in the system image list)
                int index = getIconIndex(parentIShellIcon, relativePIDL);
                if (index > 0) {
                    Map imageCache;
                    if (isLink()) {
                        imageCache = getLargeIcon ? largeLinkedSystemImages : smallLinkedSystemImages;
                    } else {
                        imageCache = getLargeIcon ? largeSystemImages : smallSystemImages;
                    }
                    icon = (Image)imageCache.get(new Integer(index));
                    if (icon == null) {
                        long hIcon = getIcon(getAbsolutePath(), getLargeIcon);
                        icon = makeIcon(hIcon, getLargeIcon);
                        disposeIcon(hIcon);
                        if (icon != null) {
                            imageCache.put(new Integer(index), icon);
                        }
                    }
                }
            }

            if (icon == null) {
                // These are only cached per object
                long hIcon = extractIcon(getParentIShellFolder(), getRelativePIDL(), getLargeIcon);
                icon = makeIcon(hIcon, getLargeIcon);
                disposeIcon(hIcon);
            }

            if (getLargeIcon) {
                largeIcon = icon;
            } else {
                smallIcon = icon;
            }
        }
        if (icon == null) {
            icon = super.getIcon(getLargeIcon);
        }
        return icon;
    }


    /**
     * Returns the canonical form of this abstract pathname.  Equivalent to
     * <code>new&nbsp;Win32ShellFolder2(getParentFile(), this.{@link java.io.File#getCanonicalPath}())</code>.
     *
     * @see java.io.File#getCanonicalFile
     */
    public File getCanonicalFile() throws IOException {
        return this;
    }

    private List topFolderList = null;

    /*
     * Indicates whether this is a special folder (includes My Documents)
     */
    public boolean isSpecial() {
        return isPersonal || !isFileSystem() || (this == getDesktop());
    }

    /**
     * Compares this object with the specified object for order.
     *
     * @see sun.awt.shell.ShellFolder#compareTo(File)
     */
    public int compareTo(File file2) {
        if (!(file2 instanceof Win32ShellFolder2)) {
            if (isFileSystem() && !isSpecial()) {
                return super.compareTo(file2);
            } else {
                return -1; // Non-file shellfolders sort before files
            }
        }
        return Win32ShellFolderManager2.compareShellFolders(this, (Win32ShellFolder2) file2);
    }
}
