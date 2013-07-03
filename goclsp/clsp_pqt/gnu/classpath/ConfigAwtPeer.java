/*
 * @(#) $(JCGO)/goclsp/clsp_pqt/gnu/classpath/ConfigAwtPeer.java --
 * Compile-time Java AWT peer configuration constants (Qt).
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2010 Ivan Maidanski <ivmai@mail.ru>
 * Distributed under the Terms of GNU Classpath License.
 **
 * Not a part of GNU Classpath.
 */

package gnu.classpath;

interface ConfigAwtPeer
{

 // String default_awt_peer_toolkit = "gnu.java.awt.peer.headless.HeadlessToolkit";
 // String default_awt_peer_toolkit = "gnu.java.awt.peer.gtk.GtkToolkit";
 String default_awt_peer_toolkit = "gnu.java.awt.peer.qt.QtToolkit";
 // String default_awt_peer_toolkit = "gnu.java.awt.peer.x.XToolkit";
}
