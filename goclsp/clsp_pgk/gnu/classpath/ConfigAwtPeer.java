/*
 * @(#) $(JCGO)/goclsp/clsp_pgk/gnu/classpath/ConfigAwtPeer.java --
 * Compile-time Java AWT peer configuration constants (Gtk).
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2007 Ivan Maidanski <ivmai@ivmaisoft.com>
 * All rights reserved. Distributed under the Terms of GNU GPL.
 **
 * Not a part of GNU Classpath.
 */

package gnu.classpath;

interface ConfigAwtPeer
{

 // String default_awt_peer_toolkit = "gnu.java.awt.peer.headless.HeadlessToolkit";
 String default_awt_peer_toolkit = "gnu.java.awt.peer.gtk.GtkToolkit";
 // String default_awt_peer_toolkit = "gnu.java.awt.peer.qt.QtToolkit";
 // String default_awt_peer_toolkit = "gnu.java.awt.peer.x.XToolkit";
}
