/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)ShellFolder.java 1.18 03/02/17
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.awt.shell;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.*;
import java.io.FileNotFoundException;
import java.security.AccessController;
import java.util.*;
import sun.security.action.GetPropertyAction;

/**
 * @author Michael Martak
 * @since 1.4
 */

public abstract class ShellFolder extends File {

    // System property for falling back from fixes for bugs 4679673, 4712307.
    // -Dswing.disableFileChooserSpeedFix
    private static boolean disableFileChooserSpeedFixProperty;

    static {
        String prop = (String)AccessController.doPrivileged(
                new GetPropertyAction("swing.disableFileChooserSpeedFix"));
        disableFileChooserSpeedFixProperty =
                (prop != null && !prop.equalsIgnoreCase("false"));
    }

    public static boolean disableFileChooserSpeedFix() {
        return disableFileChooserSpeedFixProperty;
    }


    protected ShellFolder parent;

    /**
     * Create a file system shell folder from a file
     */
    ShellFolder(ShellFolder parent, String pathname) {
        super((pathname != null) ? pathname : "ShellFolder");
        this.parent = parent;
    }

    /**
     * @return Whether this is a file system shell folder
     */
    public boolean isFileSystem() {
        return (!getPath().startsWith("ShellFolder"));
    }

    /**
     * This method must be implemented to make sure that no instances
     * of <code>ShellFolder</code> are ever serialized. If <code>isFileSystem()</code> returns
     * <code>true</code>, then the object should be representable with an instance of
     * <code>java.io.File</code> instead. If not, then the object is most likely
     * depending on some internal (native) state and cannot be serialized.
     *
     * @returns a <code>java.io.File</code> replacement object, or <code>null</code>
     * if no suitable replacement can be found.
     */
    protected abstract Object writeReplace() throws java.io.ObjectStreamException;

    /**
     * Returns the path for this object's parent,
     * or <code>null</code> if this object does not name a parent
     * folder.
     *
     * @return  the path as a String for this object's parent,
     * or <code>null</code> if this object does not name a parent
     * folder
     *
     * @see java.io.File#getParent()
     * @since 1.4
     */
    public String getParent() {
        if (parent == null && isFileSystem()) {
            return super.getParent();
        }
        if (parent != null) {
            return (parent.getPath());
        } else {
            return null;
        }
    }

    /**
     * Returns a File object representing this object's parent,
     * or <code>null</code> if this object does not name a parent
     * folder.
     *
     * @return  a File object representing this object's parent,
     * or <code>null</code> if this object does not name a parent
     * folder
     *
     * @see java.io.File#getParentFile()
     * @since 1.4
     */
    public File getParentFile() {
        if (parent != null) {
            return parent;
        } else if (isFileSystem()) {
            return super.getParentFile();
        } else {
            return null;
        }
    }

    public File[] listFiles() {
        return listFiles(true);
    }

    public File[] listFiles(boolean includeHiddenFiles) {
        File[] files = super.listFiles();

        if (!includeHiddenFiles) {
            Vector v = new Vector();
            int nameCount = (files == null) ? 0 : files.length;
            for (int i = 0; i < nameCount; i++) {
                if (!files[i].isHidden()) {
                    v.addElement(files[i]);
                }
            }
            files = (File[])v.toArray(new File[v.size()]);
        }

        return files;
    }


    /**
     * @return Whether this shell folder is a link
     */
    public abstract boolean isLink();

    /**
     * @return The shell folder linked to by this shell folder, or null
     * if this shell folder is not a link
     */
    public abstract ShellFolder getLinkLocation() throws FileNotFoundException;

    /**
     * @return The name used to display this shell folder
     */
    public abstract String getDisplayName();

    /**
     * @return The type of shell folder as a string
     */
    public abstract String getFolderType();

    /**
     * @return The executable type as a string
     */
    public abstract String getExecutableType();

    /**
     * Compares this ShellFolder with the specified ShellFolder for order.
     *
     * @see #compareTo(Object)
     */
    public int compareTo(File file2) {
        if (file2 == null || !(file2 instanceof ShellFolder)
            || ((file2 instanceof ShellFolder) && ((ShellFolder)file2).isFileSystem())) {

            if (isFileSystem()) {
                return super.compareTo(file2);
            } else {
                return -1;
            }
        } else {
            if (isFileSystem()) {
                return 1;
            } else {
                return getName().compareTo(file2.getName());
            }
        }
    }

    /**
     * Compares this object with the specified object for order.
     *
     * @see java.util.Comparable#compareTo(Object)
     */
    public int compareTo(Object o) {
        return compareTo((File)o);
    }

    /**
     * @param getLargeIcon whether to return large icon (ignored in base implementation)
     * @return The icon used to display this shell folder
     */
    public Image getIcon(boolean getLargeIcon) {
        return null;
    }


    // Static

    private static ShellFolderManager shellFolderManager;

    private static final ShellFolderManager shellFolderManager() {
        if (shellFolderManager != null)
            return shellFolderManager;

        ShellFolderManager manager;
        Class managerClass = (Class)Toolkit.getDefaultToolkit().
                        getDesktopProperty("Shell.shellFolderManager");
        if (managerClass != null) {
            try {
                manager = (ShellFolderManager)managerClass.newInstance();
            } catch (InstantiationException e) {
                throw new Error("Could not instantiate Shell Folder Manager: "
                + managerClass.getName());
            } catch (IllegalAccessException e) {
                throw new Error ("Could not access Shell Folder Manager: "
                + managerClass.getName());
            }
        } else {
            manager = new ShellFolderManager();
        }

        synchronized (ShellFolder.class) {
            if (shellFolderManager == null)
                shellFolderManager = manager;
        }
        return shellFolderManager;
    }

    /**
     * Return a shell folder from a file object
     * @exception FileNotFoundException if file does not exist
     */
    public static ShellFolder getShellFolder(File file) throws FileNotFoundException {
        if (file instanceof ShellFolder) {
            return (ShellFolder)file;
        }
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        return shellFolderManager().createShellFolder(file);
    }

    /**
     * @param key a <code>String</code>
     * @return An Object matching the string <code>key</code>.
     * @see ShellFolderManager#get(String)
     */
    public static Object get(String key) {
        return shellFolderManager().get(key);
    }

    /**
     * Does <code>dir</code> represent a "computer" such as a node on the network, or
     * "My Computer" on the desktop.
     */
    public static boolean isComputerNode(File dir) {
        return shellFolderManager().isComputerNode(dir);
    }

    /**
     * @return Whether this is a file system root directory
     */
    public static boolean isFileSystemRoot(File dir) {
        return shellFolderManager().isFileSystemRoot(dir);
    }


    // Override File methods

    public static void sortFiles(List files) {
        shellFolderManager().sortFiles(files);
    }

    public boolean isAbsolute() {
        return (!isFileSystem() || super.isAbsolute());
    }

    public File getAbsoluteFile() {
        return (isFileSystem() ? super.getAbsoluteFile() : this);
    }

    public boolean canRead() {
        return (isFileSystem() ? super.canRead() : true);
    }

    /**
     * Returns true if folder allows creation of children.
     * True for the "Desktop" folder, but false for the "My Computer"
     * folder.
     */
    public boolean canWrite() {
        return (isFileSystem() ? super.canWrite() : false);
    }

    public boolean exists() {
        // Assume top-level drives exist, because state is uncertain for
        // removable drives.
        return (!isFileSystem() || isFileSystemRoot(this) || super.exists()) ;
    }

    public boolean isDirectory() {
        return (isFileSystem() ? super.isDirectory() : true);
    }

    public boolean isFile() {
        return (isFileSystem() ? super.isFile() : !isDirectory());
    }

    public long lastModified() {
        return (isFileSystem() ? super.lastModified() : 0L);
    }

    public long length() {
        return (isFileSystem() ? super.length() : 0L);
    }

    public boolean createNewFile() throws IOException {
        return (isFileSystem() ? super.createNewFile() : false);
    }

    public boolean delete() {
        return (isFileSystem() ? super.delete() : false);
    }

    public void deleteOnExit() {
        if (isFileSystem()) {
            super.deleteOnExit();
        } else {
            // Do nothing
        }
    }

    public boolean mkdir() {
        return (isFileSystem() ? super.mkdir() : false);
    }

    public boolean mkdirs() {
        return (isFileSystem() ? super.mkdirs() : false);
    }

    public boolean renameTo(File dest) {
        return (isFileSystem() ? super.renameTo(dest) : false);
    }

    public boolean setLastModified(long time) {
        return (isFileSystem() ? super.setLastModified(time) : false);
    }

    public boolean setReadOnly() {
        return (isFileSystem() ? super.setReadOnly() : false);
    }

    public String toString() {
        return (isFileSystem() ? super.toString() : getDisplayName());
    }
}
