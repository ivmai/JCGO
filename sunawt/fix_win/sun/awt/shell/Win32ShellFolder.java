/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)Win32ShellFolder.java    1.30 03/01/23
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
 * Unfortunately, the two concepts run against each other in many instances.
 * Most notably, it is impossible, except in Windows 2000, to get a parent
 * IShellFolder interface from an absolute PIDL (the SHBindToParent function,
 * the important glue, is not available in most versions of the DLL).  Even
 * if this glue were present, there is still no means of getting the parent's
 * PIDL from its IShellFolder interface.  Therefore, navigation through the
 * shell folder tree is tricky.
 * <P>
 * <BR>
 * We have provided only a few ways to get a ShellFolder:
 * <OL>
 * <LI> One can call ShellFolder.getRoots() </LI>
 * <LI> One can call ShellFolder.getShellFolder(File f) </LI>
 * <LI> From an existing ShellFolder, one can call getParentFile() </LI>
 * <LI> From an existing ShellFolder, one can call getChildren() </LI>
 * </OL>
 * Our navigation strategy is different for file system folders than non-file
 * system folders.  File system folders are initialized with their
 * absolute PIDL, and non-file system folders are initialized with their
 * IShellFolder interface and relative PIDL.  Non-file system folders also
 * carry a reference to their parent shell folder.
 *
 * @author Michael Martak
 * @author Leif Samuelsson
 * @since 1.4
 */

final class Win32ShellFolder extends ShellFolder {

    private static native void initIDs();

    static {
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

    /*
     * We keep track of non-file system shell folders through their
     * IShellFolder interface.
     */
    private long pIShellFolder = 0;
    /*
     * Relative PIDL is useful for a non-file system folder for caching,
     * but is not entirely necessary.  A relative PIDL can be derived
     * from a parent IShellFolder's BindToObject method.
     */
    private long relativePIDL = 0;

    /*
     * We keep track of file system shell folders through their
     * absolute PIDL.
     */
    private long pIDL = 0;

    /*
     * The following are for caching various shell folder properties.
     */
    private long attributes = -1L;
    private String folderType = null;
    private String displayName = null;
    private Image smallIcon = null;
    private Image largeIcon = null;


    /**
     * Create a non-file system special shell folder, such as the
     * desktop or Network Neighborhood.
     */
    Win32ShellFolder(ShellFolder parent, int shellFolderType) throws IOException {
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
     * Create a file system shell folder from a file path.
     */
    public Win32ShellFolder(ShellFolder parent, String absolutePath) throws FileNotFoundException {
        super(parent, absolutePath);
        try {
            boolean initAttributes = true;
            if (absolutePath.length() == 3 && absolutePath.endsWith(":\\")) {
                // It is generally better to initialize attributes here for
                // speed, but we also need to avoid touching removable drives
                // unnecessarily.
                initAttributes = false;
            }
            pIDL = initFile(getDesktopIShellFolder(), absolutePath, initAttributes);
        } catch (IOException e) {
            throw new FileNotFoundException(absolutePath + " (" + e.getMessage() + ")");
        }
    }

    /**
     * Create a file system shell folder from a file.
     */
    public Win32ShellFolder(ShellFolder parent, File file) throws FileNotFoundException {
        this(parent, file.getAbsolutePath());
    }

    /**
     * Create a system shell folder
     */
    Win32ShellFolder(Win32ShellFolder parent, long pIShellFolder, long relativePIDL, String path) {
        super(parent, (path != null) ? path : "ShellFolder: ");
        this.pIShellFolder = pIShellFolder;
        this.relativePIDL = relativePIDL;
    }

    // Initializes the desktop shell folder
    private native void initDesktop();
    // Initializes a special, non-file system shell folder
    // from one of the above constants
    private native void initSpecial(long desktopIShellFolder,
        int shellFolderType);
    // Initializes a file system shell folder from an absolute
    // path.  Returns the absoulte PIDL of the file.  This value
    // must be released using releasePIDL().
    private native long initFile(long desktopIShellFolder, String absolutePath,
                                 boolean initAttributes) throws IOException;


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
            Win32ShellFolder drives = Win32ShellFolderManager.getDrives();
            if (drives != null) {
                File[] driveRoots = drives.listFiles();
                if (driveRoots != null) {
                    for (int i = 0; i < driveRoots.length; i++) {
                        if (driveRoots[i] instanceof Win32ShellFolder) {
                            Win32ShellFolder sf = (Win32ShellFolder)driveRoots[i];
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
        if (pIDL != 0 && pIShellFolder == 0) {
            releasePIDL(pIDL);
            pIDL = 0;

            if (relativePIDL != 0) {
                releasePIDL(relativePIDL);
                relativePIDL = 0;
            }
        } else {
            // Can't release pIShellFolder and relativePIDL because
            // they are shared between instances, and reference counting
            // would not be cost effective.
            pIShellFolder = 0;
        }
    }

    // Release a PIDL object
    private native void releasePIDL(long pIDL);

    /**
     * Accessor for IShellFolder (non-file system shell folders only)
     */
    public long getIShellFolder() {
        return pIShellFolder;
    }

    /**
     * Get the parent ShellFolder's IShellFolder interface (non-file system
     * shell folders only)
     */
    public long getParentIShellFolder() {
        Win32ShellFolder parent = (Win32ShellFolder)getParentFile();
        // Parent should only be null if this is the desktop IShellFolder
        if (parent == null) {
            if (equals(getDesktop())) {
                return getIShellFolder();
            }
            return 0;
        }
        return parent.getIShellFolder();
    }

    // Derive a relative PIDL from a parent IShellFolder's BindToObject method
    // and comparing interface pointers.  Returns the relative PIDL.
    private long getRelativePIDL(long parentIShellFolder,
        long pIShellFolder) {
        if (parentIShellFolder == 0 || pIShellFolder == 0) {
            return 0;
        }
        long enumObjects = getEnumObjects(parentIShellFolder, true);
        if (enumObjects == 0) {
            return 0;
        }
        try {
            long childPIDL = 0;
            do {
                childPIDL = getNextChild(enumObjects);
                if (childPIDL != 0) {
                    long childIShellFolder =
                        bindToObject(parentIShellFolder, childPIDL);
                    if (childIShellFolder == pIShellFolder) {
                        return childPIDL;
                    }
                    releasePIDL(childPIDL);
                }
            } while (childPIDL != 0);
        } finally {
            releaseEnumObjects(enumObjects);
        }
        return 0;
    }

    /**
     * Accessor for relative PIDL (non-file system shell folders only)
     */
    public long getRelativePIDL() {
        if (relativePIDL == 0) {
            relativePIDL = getRelativePIDL(
                getParentIShellFolder(), getIShellFolder());
        }
        return relativePIDL;
    }

    /**
     * Accessor for absolute PIDL (file system shell folders only)
     */
    public long getPIDL() {
        return pIDL;
    }

    /**
     * Helper function to return the desktop
     */
    public Win32ShellFolder getDesktop() {
        return Win32ShellFolderManager.getDesktop();
    }

    /**
     * Helper function to return the desktop IShellFolder interface
     */
    public long getDesktopIShellFolder() {
        return getDesktop().getIShellFolder();
    }

    /**
     * Check to see if two ShellFolder objects are the same
     */
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Win32ShellFolder)) {
            if (getPIDL() != 0) {
                return super.equals(o);
            } else {
                return false;
            }
        }
        Win32ShellFolder rhs = (Win32ShellFolder)o;
        ShellFolder parent = (ShellFolder)getParentFile();
        if (parent != null && !parent.equals(rhs.getParentFile())) {
            return false;
        }
        if (isFileSystem()) {
            return getAbsolutePath().equals(rhs.getAbsolutePath());
        }
        return (getIShellFolder() == rhs.getIShellFolder());
    }

    /**
     * @return Whether this is a file system shell folder
     */
    public boolean isFileSystem() {
        return (getPIDL() != 0 || !getPath().startsWith("ShellFolder: "));
    }

    // Initialize and cache attributes for a file system shell folder
    private native void initAttributes(long pIDL);

    // Return whether a non-file system shell folder has the supplied
    // attribute
    private native boolean hasAttribute(long parentIShellFolder,
        long relativePIDL, int attribute);
    private native boolean hasAttribute(boolean dummy);

    /**
     * Return whether the given attribute flag is set for this object
     */
    public boolean hasAttribute(int attribute) {
        boolean result = false;
        if (getPIDL() != 0) {
            if (attributes == -1L) {
                initAttributes(getPIDL());
            }
            result = (attributes & attribute) != 0;
        }
        if (pIShellFolder != 0) {
            result = result || hasAttribute(getParentIShellFolder(), getRelativePIDL(),
                                            attribute);
        }
        return result;
    }

    // Return whether this non-file system shell folder behaves
    // like a standard file system object.  If this returns true,
    // this shell folder represents a valid file system object.
    private boolean behavesLikeFileSystem(long parentIShellFolder, long relativePIDL) {
        return hasAttribute(parentIShellFolder, relativePIDL, ATTRIB_FILESYSTEM);
    }

    // Return the path to the underlying file system object
    static native String getFileSystemPath(long parentIShellFolder, long relativePIDL);
    static native String getFileSystemPath(int csidl) throws IOException;

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
        if (parent == null) {
            if (getPIDL() != 0) {
                String parentName = getParent();
                // Check if we are a root
                if (parentName == null) { // We are a root
                    String filename = getAbsolutePath();
                    // Check if we are a network path
                    if (filename.startsWith("\\")) {
                        parent = Win32ShellFolderManager.getNetwork();
                    } else {
                        parent = Win32ShellFolderManager.getDrives();
                    }
                } else if (isNetworkRoot(parentName)) {
                    parent = Win32ShellFolderManager.getNetwork();
                } else { // We are not a root
                    try {
                        parent = new Win32ShellFolder(null, super.getParentFile());
                    } catch (FileNotFoundException ignore) { }
                }
            }
        }
        return parent;
    }

    public boolean isDirectory() {
        return (hasAttribute(ATTRIB_HASSUBFOLDER) || (isFileSystem() && super.isDirectory()));
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
    private native long bindToObject(long parentIShellFolder, long pIDL);



    /**
     * @return An array of shell folders that are children of this shell folder
     * object, null if this shell folder is empty.
     */
    public File[] listFiles(boolean includeHiddenFiles) {
        Win32ShellFolder desktop = Win32ShellFolderManager.getDesktop();
        Win32ShellFolder personal = Win32ShellFolderManager.getPersonal();
        File[] children = null;

        if (getPIDL() != 0) {
            File[] tmpChildren = super.listFiles(includeHiddenFiles);
            if (tmpChildren != null) {
                children = new File[tmpChildren.length];
                int j = 0;
                for (int i = 0; i < tmpChildren.length; i++) {
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }
                    try {
                        File child = new Win32ShellFolder(this, tmpChildren[i]);
                        children[j++] = child;
                    } catch (FileNotFoundException ignore) { }
                }
                if (j < tmpChildren.length) {
                    tmpChildren = new File[j];
                    System.arraycopy(children, 0, tmpChildren, 0, j);
                    children = tmpChildren;
                }
            }
        } else {
            ArrayList list = new ArrayList();
            long pIShellFolder = getIShellFolder();
            if (pIShellFolder != 0) {
                long pEnumObjects = getEnumObjects(pIShellFolder, includeHiddenFiles);
                if (pEnumObjects != 0) {
                    long childPIDL = 0;
                    do {
                        childPIDL = getNextChild(pEnumObjects);
                        if (childPIDL != 0) {
                            Win32ShellFolder childFolder = null;
                            if (hasAttribute(pIShellFolder, childPIDL, ATTRIB_FILESYSTEM)) {
                                String path = getFileSystemPath(pIShellFolder, childPIDL);
                                if (path == null || !new File(path).isAbsolute()) {
                                    // Some folders like "Infrared Recipient" are reported
                                    // as being in the filesystem but don't have a real path.
                                    continue;
                                }
                                if (this == desktop
                                    && personal != null
                                    && path.equalsIgnoreCase(personal.getAbsolutePath())) {

                                    childFolder = personal;
                                } else {
                                    try {
                                        childFolder = new Win32ShellFolder(this, path);
                                    } catch (FileNotFoundException e) {
                                        continue;
                                    }
                                }
                            } else if (hasAttribute(pIShellFolder, childPIDL, ATTRIB_FILESYSANCESTOR)) {
                                long childIShellFolder = bindToObject(pIShellFolder, childPIDL);
                                childFolder = new Win32ShellFolder(this, childIShellFolder, childPIDL, null);
                            } else {
                                //System.out.println("Losing a non-file");
                            }
                            if (childFolder != null) {
                                list.add(childFolder);
                            }
                        } else {
                            //System.out.println("Losing a NULL childPIDL");
                        }
                    } while (childPIDL != 0);
                }
                releaseEnumObjects(pEnumObjects);
                children = (ShellFolder[])list.toArray(new ShellFolder[list.size()]);;
            }
        }
        return children;
    }


    /**
     * Look for (possibly special) child folder by it's path
     *
     * @return The child shellfolder, or null if not found.
     */
    Win32ShellFolder getChildByPath(String filePath) {
        long pIShellFolder = getIShellFolder();
        long pEnumObjects =  getEnumObjects(pIShellFolder, true);
        Win32ShellFolder child = null;
        long childPIDL = 0;

        while ((childPIDL = getNextChild(pEnumObjects)) != 0) {
            if (hasAttribute(pIShellFolder, childPIDL, ATTRIB_FILESYSTEM)) {
                String path = getFileSystemPath(pIShellFolder, childPIDL);
                if (path != null && path.equalsIgnoreCase(filePath)) {
                    long childIShellFolder = bindToObject(pIShellFolder, childPIDL);
                    child = new Win32ShellFolder(this, childIShellFolder, childPIDL, path);
                    break;
                }
            }
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
        String linkLocation = null;
        if (getPIDL() != 0) {
            linkLocation = getLinkLocation(getDesktopIShellFolder(), getPIDL());
        } else {
            linkLocation = getLinkLocation(getParentIShellFolder(), getRelativePIDL());
        }
        if (linkLocation == null) {
            return null;
        }
        return new Win32ShellFolder(null, linkLocation);
    }

    // Return the display name of a non-file system shell folder
    private native String getDisplayName(long parentIShellFolder,
        long relativePIDL);
    private native String getDisplayName(boolean dummy);

    /**
     * @return The name used to display this shell folder
     */
    public String getDisplayName() {
        if (displayName == null && getPIDL() == 0) {
            displayName = getDisplayName(getParentIShellFolder(), getRelativePIDL());
        }
        return displayName;
    }

    // Return the folder type of a shell folder
    private native String getFolderType(long pIDL);

    /**
     * @return The type of shell folder as a string
     */
    public String getFolderType() {
        if (folderType == null && getPIDL() == 0) {
            folderType = getFolderType(getRelativePIDL());
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

    // Return the icon of a file system shell folder
    private native long getIcon(long pIDL, boolean getLargeIcon);
    // Return the icon of a non-file system shell folder
    private native long getIcon(long parentIShellFolder,
                                long relativePIDL, boolean getLargeIcon);
    // Return the bits from an HICON.  This has a side effect of setting
    // the imageHash variable for efficient caching / comparing.
    private native int[] getIconBits(long hIcon, int iconSize);
    // Dispose the HICON
    private native void disposeIcon(long hIcon);

    public static native int[] getFileChooserBitmapBits();


    private int[] getIconBits(boolean getLargeIcon) {
        // Get the HICON
        long hIcon = 0;
        if (getPIDL() != 0) {
            hIcon = getIcon(getPIDL(), getLargeIcon);
        } else {
            hIcon = getIcon(getParentIShellFolder(),
                            getRelativePIDL(), getLargeIcon);
        }
        // From an HICON, call GetIconInfo to get the underlying HBITMAPs,
        // then call GetDIBits.
        if (hIcon == 0) {
            return null;
        }
        // Get the bits.  This has the side effect of setting the imageHash
        // value for this object.
        int[] bits = getIconBits(hIcon, getLargeIcon ? 32 : 16);
        // Dispose the HICON
        disposeIcon(hIcon);
        return bits;
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


    /**
     * @return The icon image used to display this shell folder
     */
    public Image getIcon(boolean getLargeIcon) {
        Image icon = getLargeIcon ? largeIcon : smallIcon;

        if (icon == null) {
            int size = getLargeIcon ? 32 : 16;
            int[] iconBits = getIconBits(getLargeIcon);
            if (iconBits == null) {
                return super.getIcon(getLargeIcon);
            }
            BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            img.setRGB(0, 0, size, size, iconBits, 0, size);
            if (getLargeIcon) {
                icon = largeIcon = img;
            } else {
                icon = smallIcon = img;
            }
        }
        return icon;
    }


    /**
     * Returns the canonical form of this abstract pathname.  Equivalent to
     * <code>new&nbsp;Win32ShellFolder(getParentFile(), this.{@link java.io.File#getCanonicalPath}())</code>.
     *
     * @see java.io.File#getCanonicalFile
     */
    public File getCanonicalFile() throws IOException {
        if (getPIDL() != 0) {
            return new Win32ShellFolder(parent, super.getCanonicalPath());
        } else {
            return this;
        }
    }

    private List topFolderList = null;

    /**
     * Compares this object with the specified object for order.
     *
     * @see sun.awt.shell.ShellFolder#compareTo(File)
     */
    public int compareTo(File file2) {
        if (file2 == null || !(file2 instanceof Win32ShellFolder)
            || ((file2 instanceof Win32ShellFolder) && ((Win32ShellFolder)file2).getPIDL() != 0)) {

            if (getPIDL() != 0) {
                return super.compareTo(file2);
            } else {
                return -1;
            }
        } else {
            if (getPIDL() != 0) {
                return 1;
            } else {
                if (topFolderList == null) {
                    topFolderList = new ArrayList();
                    topFolderList.add(Win32ShellFolderManager.getPersonal());
                    topFolderList.add(Win32ShellFolderManager.getDesktop());
                    topFolderList.add(Win32ShellFolderManager.getDrives());
                    topFolderList.add(Win32ShellFolderManager.getNetwork());
                }
                int i1 = topFolderList.indexOf(this);
                int i2 = topFolderList.indexOf(file2);

                if (i1 >= 0 && i2 >= 0) {
                    return (i1 - i2);
                } else if (i1 >= 0) {
                    return -1;
                } else if (i2 >= 0) {
                    return 1;
                } else {
                    return getName().compareTo(file2.getName());
                }
            }
        }
    }
}
