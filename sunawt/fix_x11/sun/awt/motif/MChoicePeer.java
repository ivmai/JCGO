/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)MChoicePeer.java 1.46 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.awt.motif;

import java.awt.*;
import java.awt.peer.*;
import java.awt.event.ItemEvent;

class MChoicePeer extends MComponentPeer implements ChoicePeer {
    boolean inUpCall=false;

    native void create(MComponentPeer parent);
    native void pReshape(int x, int y, int width, int height);
    native void pSelect(int index, boolean init);
    native void appendItems(String[] items);

    void initialize() {
        Choice opt = (Choice)target;
        int itemCount = opt.countItems();
        String[] items = new String[itemCount];
        for (int i=0; i < itemCount; i++) {
            items[i] = opt.getItem(i);
        }
        if (itemCount > 0) {
            appendItems(items);
            pSelect(opt.getSelectedIndex(), true);
        }
        super.initialize();
    }

    public MChoicePeer(Choice target) {
        super(target);
    }

    public boolean isFocusable() {
        return true;
    }

    public Dimension getMinimumSize() {
        FontMetrics fm = getFontMetrics(target.getFont());
        Choice c = (Choice)target;
        int w = 0;
        for (int i = c.countItems() ; i-- > 0 ;) {
            w = Math.max(fm.stringWidth(c.getItem(i)), w);
        }
        return new Dimension(32 + w, Math.max(fm.getHeight() + 8, 15) + 5);
    }

    public native void setFont(Font f);

    public void add(String item, int index) {
        addItem(item, index);
        // Adding an item can change the size of the Choice, so do
        // a reshape, based on the font size.
        Rectangle r = target.getBounds();
        reshape(r.x, r.y, 0, 0);
    }

    public native void remove(int index);

    public native void removeAll();

    /**
     * DEPRECATED, but for now, called by add(String, int).
     */
    public native void addItem(String item, int index);

    // public native void remove(int index);

    public native void setBackground(Color c);

    public native void setForeground(Color c);

    public void select(int index) {
        if (!inUpCall) {
            pSelect(index, false);
        }
    }

    void notifySelection(String str) {
        Choice c = (Choice)target;
        ItemEvent e = new ItemEvent(c, ItemEvent.ITEM_STATE_CHANGED,
                                str, ItemEvent.SELECTED);
        postEvent(e);
    }


    // NOTE: This method is called by privileged threads.
    //       DO NOT INVOKE CLIENT CODE ON THIS THREAD!
    void action(final int index) {
        final Choice c = (Choice)target;
        inUpCall = false;  /* Used to prevent native selection. */
        MToolkit.executeOnEventHandlerThread(c, new Runnable() {
            public void run() {
                String str;
                synchronized (c) {
                   if (c.getItemCount() <= index) {
                       return;
                   }
                   inUpCall = true;       /* Prevent native selection. */
                   c.select(index);       /* set value in target */
                   str = c.getItem(index);
                   inUpCall = false;
                }
                notifySelection(str);
            }
        });
    }

    /*
     * Print the native component by rendering the Motif look ourselves.
     * ToDo(aim): needs to query native motif for more accurate size and
     * color information.
     */
    public void print(Graphics g) {
        Choice ch = (Choice)target;
        Dimension d = ch.size();
        Color bg = ch.getBackground();
        Color fg = ch.getForeground();

        g.setColor(bg);
        g.fillRect(2, 2, d.width-1, d.height-1);
        draw3DRect(g, bg, 1, 1, d.width-2, d.height-2, true);
        draw3DRect(g, bg, d.width - 18, (d.height / 2) - 3, 10, 6, true);

        g.setColor(fg);
        g.setFont(ch.getFont());
        FontMetrics fm = g.getFontMetrics();
        String lbl = ch.getSelectedItem();
        g.drawString(lbl, 5, (d.height + fm.getMaxAscent()-fm.getMaxDescent())/2);

        target.print(g);
    }

    /**
     * DEPRECATED
     */
    public Dimension minimumSize() {
            return getMinimumSize();
    }

    protected void disposeImpl() {
        freeNativeData();
        super.disposeImpl();
    }

    private native void freeNativeData();
}
