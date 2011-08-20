/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)X11InputMethod.java      1.74 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.awt.motif;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Window;
import java.awt.im.InputContext;
import java.awt.im.InputMethodHighlight;
import java.awt.im.spi.InputMethodContext;
import sun.awt.im.InputMethodAdapter;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.FocusEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.awt.event.InputMethodEvent;
import java.awt.font.TextAttribute;
import java.awt.font.TextHitInfo;
import java.lang.Character.Subset;
import java.text.AttributedString;
import java.text.AttributedCharacterIterator;
import sun.awt.motif.MComponentPeer;
import sun.awt.motif.MInputMethodControl;

/**
 * Input Method Adapter for XIM
 *
 * @version 1.74 01/23/03
 * @author JavaSoft International
 */
public class X11InputMethod extends InputMethodAdapter {
    /*
     * The following XIM* values must be the same as those defined in
     * Xlib.h
     */
    private static final int XIMReverse = (1<<0);
    private static final int XIMUnderline = (1<<1);
    private static final int XIMHighlight = (1<<2);
    private static final int XIMPrimary = (1<<5);
    private static final int XIMSecondary = (1<<6);
    private static final int XIMTertiary = (1<<7);

    /*
     * visible position values
     */
    private static final int XIMVisibleToForward = (1<<8);
    private static final int XIMVisibleToBackward = (1<<9);
    private static final int XIMVisibleCenter = (1<<10);
    private static final int XIMVisibleMask = (XIMVisibleToForward|
                                               XIMVisibleToBackward|
                                               XIMVisibleCenter);

    private Locale locale;
    private static boolean isXIMOpened = false;
    private Container clientComponentWindow = null;
    private boolean createXICFailed = false;
    private Component awtFocussedComponent = null;
    private Component xicFocussedComponent = null; // only used while input method is inactive
    private boolean   isActive = false;
    private boolean   isActiveClient = false;
    private static Map[] highlightStyles;
    private boolean disposed = false;

    //reset the XIC if necessary
    private boolean   needResetXIC = false;
    private Component needResetXICClient = null;

    // composition state stuff
    private boolean compositionEnableSupported = true;
    private boolean needCompositionEnable = false;

    // variables to keep track of preedit context.
    private String committedText = null;
    private StringBuffer composedText = null;
    private IntBuffer rawFeedbacks;

    // private data (X11InputMethodData structure defined in
    // awt_InputMethod.c) for native methods
    // this structure needs to be accessed within AWT_LOCK/UNLOCK
    transient private long pData = 0; // accessed by native

    // Initialize highlight mapping table
    static {
        Map styles[] = new Map[4];
        HashMap map;

        // UNSELECTED_RAW_TEXT_HIGHLIGHT
        map = new HashMap(1);
        map.put(TextAttribute.WEIGHT,
                  TextAttribute.WEIGHT_BOLD);
        styles[0] = Collections.unmodifiableMap(map);

        // SELECTED_RAW_TEXT_HIGHLIGHT
        map = new HashMap(1);
        map.put(TextAttribute.SWAP_COLORS,
                  TextAttribute.SWAP_COLORS_ON);
        styles[1] = Collections.unmodifiableMap(map);

        // UNSELECTED_CONVERTED_TEXT_HIGHLIGHT
        map = new HashMap(1);
        map.put(TextAttribute.INPUT_METHOD_UNDERLINE,
                  TextAttribute.UNDERLINE_LOW_ONE_PIXEL);
        styles[2] = Collections.unmodifiableMap(map);

        // SELECTED_CONVERTED_TEXT_HIGHLIGHT
        map = new HashMap(1);
        map.put(TextAttribute.SWAP_COLORS,
                  TextAttribute.SWAP_COLORS_ON);
        styles[3] = Collections.unmodifiableMap(map);

        highlightStyles = styles;
    }

    static {
        initIDs();
    }

    /**
     * Initialize JNI field and method IDs for fields that may be
       accessed from C.
     */
    private static native void initIDs();

    /**
     * Constructs an X11InputMethod instance. It initializes the XIM
     * environment if it's not done yet.
     *
     * @exception AWTException if XOpenIM() failed.
     */
    public X11InputMethod() throws AWTException {
        // supports only the locale in which the VM is started
        locale = X11InputMethodDescriptor.getSupportedLocale();
        if (initXIM() == false) {
            throw new AWTException("Cannot open X Input Method");
        }
    }

    protected void finalize() throws Throwable {
        dispose();
        super.finalize();
    }

    protected void awtLock() {
        AWTLockAccess.awtLock();
    }

    protected void awtUnlock() {
        AWTLockAccess.awtUnlock();
    }

    /**
     * Invokes openIM() that invokes XOpenIM() if it's not opened yet.
     * @return  true if openXIM() is successful or it's already been opened.
     */
    private synchronized boolean initXIM() {
        if (isXIMOpened == false)
            isXIMOpened = openXIM();
        return isXIMOpened;
    }

    /**
     * Does nothing - this adapter doesn't use the input method context.
     *
     * @see java.awt.im.spi.InputMethod#setInputMethodContext
     */
    public void setInputMethodContext(InputMethodContext context) {
    }

    /**
     * Set locale to input. If input method doesn't support specified locale,
     * false will be returned and its behavior is not changed.
     *
     * @param lang locale to input
     * @return the true is returned when specified locale is supported.
     */
    public boolean setLocale(Locale lang) {
        if (lang.equals(locale)) {
            return true;
        }
        // special compatibility rule for Japanese and Korean
        if (locale.equals(Locale.JAPAN) && lang.equals(Locale.JAPANESE) ||
                locale.equals(Locale.KOREA) && lang.equals(Locale.KOREAN)) {
            return true;
        }
        return false;
    }

    /**
     * Returns current input locale.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Does nothing - XIM doesn't let you specify which characters you expect.
     *
     * @see java.awt.im.spi.InputMethod#setCharacterSubsets
     */
    public void setCharacterSubsets(Subset[] subsets) {
    }

    /**
     * Dispatch event to input method. InputContext dispatch event with this
     * method. Input method set consume flag if event is consumed in
     * input method.
     *
     * @param e event
     */
    public void dispatchEvent(AWTEvent e) {
    }


    private final void resetXICifneeded(){
        if (needResetXIC){
            if (getClientComponent() != needResetXICClient){
                resetXIC();

                // Restore the composition state if needed
                if (compositionEnableSupported && needCompositionEnable) {
                    try {
                        setCompositionEnabled(true);
                    } catch (UnsupportedOperationException e) {
                        compositionEnableSupported = false;
                    }
                }
            }
            needResetXICClient = null;
            needResetXIC = false;
            needCompositionEnable = false;
        }
    }

    /**
     * Activate input method.
     */
    public synchronized void activate() {
        if (createXICFailed)
            return;

        resetXICifneeded();

        clientComponentWindow = getClientComponentWindow();
        if (clientComponentWindow == null)
            return;

        if (xicFocussedComponent != null){
            if (xicFocussedComponent != awtFocussedComponent)
                setXICFocus(getPeer(xicFocussedComponent), false, isActiveClient);
            xicFocussedComponent = null;
        }

        if (pData == 0) {
            MComponentPeer peer = getPeer(clientComponentWindow);
            if (peer == null) {
                return;
            }
            MComponentPeer tc = null;
            if (peer instanceof MInputMethodControl) {
                tc = ((MInputMethodControl)peer).getTextComponent();
            }
            if (!createXICNative(peer, tc)) {
                createXICFailed = true;
                return;
            }
            disposed = false;
            if (peer instanceof MInputMethodControl) {
                ((MInputMethodControl)peer).addInputMethod(this);
            }
        }
        setXICFocus(getPeer(awtFocussedComponent), true, haveActiveClient());
        isActive = true;
    }

    /**
     * Deactivate input method.
     */
    public synchronized void deactivate(boolean isTemporary) {
        boolean   isAc =  haveActiveClient();
        if (isTemporary){
            xicFocussedComponent = awtFocussedComponent;
            isActiveClient = isAc;
            //turn the status window off...
            turnoffStatusWindow();
        } else {
            setXICFocus(getPeer(awtFocussedComponent), false, isAc);
            xicFocussedComponent = null;
        }
        isActive = false;
    }

    // implements java.awt.im.spi.InputMethod.hideWindows
    public void hideWindows() {
        // ? ? ? need real implementation
    }

    /**
     * @see java.awt.Toolkit#mapInputMethodHighlight
     */
    static Map mapInputMethodHighlight(InputMethodHighlight highlight) {
        int index;
        int state = highlight.getState();
        if (state == InputMethodHighlight.RAW_TEXT) {
            index = 0;
        } else if (state == InputMethodHighlight.CONVERTED_TEXT) {
            index = 2;
        } else {
            return null;
        }
        if (highlight.isSelected()) {
            index += 1;
        }
        return highlightStyles[index];
    }

    /**
     * @see sun.awt.im.InputMethodAdapter#setAWTFocussedComponent
     */
    protected void setAWTFocussedComponent(Component component) {
        if (component == null) {
            return;
        }
        if (isActive) {
            // deactivate/activate are being suppressed during a focus change -
            // this may happen when an input method window is made visible
            boolean ac = haveActiveClient();
            setXICFocus(getPeer(awtFocussedComponent), false, ac);
            setXICFocus(getPeer(component), true, ac);
        }
        awtFocussedComponent = component;
    }

    /**
     * @see sun.awt.im.InputMethodAdapter#stopListening
     */
    protected void stopListening() {
        // It is desirable to disable XIM by calling XSetICValues with
        // XNPreeditState == XIMPreeditDisable.  But Solaris 2.6 and
        // Solaris 7 do not implement this correctly without a patch,
        // so just call resetXIC here.  Prior endComposition call commits
        // the existing composed text.
        endComposition();
        if (needResetXIC) {
            resetXIC();
            needResetXICClient = null;
            needResetXIC = false;
        }
    }

    /**
     * Returns the Window instance in which the client component is
     * contained. If not found, null is returned. (IS THIS POSSIBLE?)
     */
    // NOTE: This method may be called by privileged threads.
    //       DO NOT INVOKE CLIENT CODE ON THIS THREAD!
    private Window getClientComponentWindow() {
        Component client = getClientComponent();
        Container container;

        if (client instanceof Container) {
            container = (Container) client;
        } else {
            // SECURITY: Use _NoClientCode(), because this thread may
            //           be privileged
            container = MComponentPeer.getParent_NoClientCode(client);
        }

        while (container != null && !(container instanceof java.awt.Window)) {
            // SECURITY: Use _NoClientCode(), because this thread may
            //           be privileged
            container = MComponentPeer.getParent_NoClientCode(container);
        }
        return (Window) container;
    }

    /**
     * Returns peer of the given client component. If the given client component
     * doesn't have peer, peer of the native container of the client is returned.
     */
    private MComponentPeer getPeer(Component client) {
        MComponentPeer peer = (MComponentPeer)MToolkit.targetToPeer(client);
        if (peer != null)
            return peer;

        Container nativeContainer = MToolkit.getNativeContainer(client);
        peer = (MComponentPeer)MToolkit.targetToPeer(nativeContainer);
        return peer;
    }

    /**
     * Changes the status area configuration that is to be requested
     * by Frame or Dialog.
     */
    void configureStatus() {
        if (disposed) {
            return;
        }

        MComponentPeer peer = getPeer((Window) clientComponentWindow);
        MComponentPeer tc = ((MInputMethodControl)peer).getTextComponent();
        if (tc != null) {
            configureStatusAreaNative(tc);
        }
    }


    /**
     * Creates an input method event from the arguments given
     * and posts it on the AWT event queue. For arguments,
     * see InputMethodEvent. Called by input method.
     *
     * @see java.awt.event.InputMethodEvent#InputMethodEvent
     */
    private void postInputMethodEvent(int id,
                                      AttributedCharacterIterator text,
                                      int committedCharacterCount,
                                      TextHitInfo caret,
                                      TextHitInfo visiblePosition,
                                      long when) {
        Component source = getClientComponent();
        if (source != null) {
            InputMethodEvent event = new InputMethodEvent(source,
                id, when, text, committedCharacterCount, caret, visiblePosition);
            MToolkit.postEvent(MToolkit.targetToAppContext(source), (AWTEvent)event);
        }
    }

    private void postInputMethodEvent(int id,
                                      AttributedCharacterIterator text,
                                      int committedCharacterCount,
                                      TextHitInfo caret,
                                      TextHitInfo visiblePosition) {
        postInputMethodEvent(id, text, committedCharacterCount,
                             caret, visiblePosition, EventQueue.getMostRecentEventTime());
    }

    /**
     * Dispatches committed text from XIM to the awt event queue. This
     * method is invoked from the event handler in canvas.c in the
     * AWT-Motif thread context.
     * @param   str     committed text
     * @param   long    when
     */
    // NOTE: This method may be called by privileged threads.
    //       This functionality is implemented in a package-private method
    //       to insure that it cannot be overridden by client subclasses.
    //       DO NOT INVOKE CLIENT CODE ON THIS THREAD!
    void dispatchCommittedText(String str, long when) {
        if (str == null)
            return;
        if (composedText == null) {
            AttributedString attrstr = new AttributedString(str);
            postInputMethodEvent(InputMethodEvent.INPUT_METHOD_TEXT_CHANGED,
                                 attrstr.getIterator(),
                                 str.length(),
                                 null,
                                 null,
                                 when);
        } else {
            // if there is composed text, wait until the preedit
            // callback is invoked.
            committedText = str;
        }
    }

    private void dispatchCommittedText(String str) {
        dispatchCommittedText(str, EventQueue.getMostRecentEventTime());
    }

    /**
     * Updates composed text with XIM preedit information and
     * posts composed text to the awt event queue. The args of
     * this method correspond to the XIM preedit callback
     * information. The XIM highlight attributes are translated via
     * fixed mapping (i.e., independent from any underlying input
     * method engine). This method is invoked in the AWT-Motif
     * (event loop) thread context.
     */
    // NOTE: This method may be called by privileged threads.
    //       This functionality is implemented in a package-private method
    //       to insure that it cannot be overridden by client subclasses.
    //       DO NOT INVOKE CLIENT CODE ON THIS THREAD!
     void dispatchComposedText(String chgText,
                                           int chgStyles[],
                                           int chgOffset,
                                           int chgLength,
                                           int caretPosition,
                                           long when) {
      if (disposed) {
          return;
      }

      //Workaround for deadlock bug on solaris2.6_zh bug#4170760
      if (chgText == null
          && chgStyles == null
          && chgOffset == 0
          && chgLength == 0
          && caretPosition == 0
          && composedText == null
          && committedText == null)
          return;
      /* synchronized (this) */ {
        if (composedText == null) {
            // TODO: avoid reallocation of those buffers
            composedText = new StringBuffer(INITIAL_SIZE);
            rawFeedbacks = new IntBuffer(INITIAL_SIZE);
        }
        if (chgLength > 0) {
            if (chgText == null && chgStyles != null) {
                rawFeedbacks.replace(chgOffset, chgStyles);
            } else {
                if (chgLength == composedText.length()) {
                    // optimization for the special case to replace the
                    // entire previous text
                    composedText = new StringBuffer(INITIAL_SIZE);
                    rawFeedbacks = new IntBuffer(INITIAL_SIZE);
                } else {
                    if (composedText.length() > 0) {
                        if (chgOffset+chgLength < composedText.length()) {
                            String text;
                            text = composedText.toString().substring(chgOffset+chgLength,
                                                                     composedText.length());
                            composedText.setLength(chgOffset);
                            composedText.append(text);
                        } else {
                            // in case to remove substring from chgOffset
                            // to the end
                            composedText.setLength(chgOffset);
                        }
                        rawFeedbacks.remove(chgOffset, chgLength);
                    }
                }
            }
        }
        if (chgText != null) {
            composedText.insert(chgOffset, chgText);
            if (chgStyles != null)
                rawFeedbacks.insert(chgOffset, chgStyles);
        }

        if (composedText.length() == 0) {
            composedText = null;
            rawFeedbacks = null;

            // if there is any outstanding committed text stored by
            // dispatchCommittedText(), it has to be sent to the
            // client component.
            if (committedText != null) {
                dispatchCommittedText(committedText, when);
                committedText = null;
                return;
            }

            // otherwise, send null text to delete client's composed
            // text.
            postInputMethodEvent(InputMethodEvent.INPUT_METHOD_TEXT_CHANGED,
                                 null,
                                 0,
                                 null,
                                 null,
                                 when);

            return;
        }

        // Now sending the composed text to the client
        int composedOffset;
        AttributedString inputText;

        // if there is any partially committed text, concatenate it to
        // the composed text.
        if (committedText != null) {
            composedOffset = committedText.length();
            inputText = new AttributedString(committedText + composedText);
            committedText = null;
        } else {
            composedOffset = 0;
            inputText = new AttributedString(composedText.toString());
        }

        int currentFeedback;
        int nextFeedback;
        int startOffset = 0;
        int currentOffset;
        int visiblePosition = 0;
        TextHitInfo visiblePositionInfo = null;

        rawFeedbacks.rewind();
        currentFeedback = rawFeedbacks.getNext();
        rawFeedbacks.unget();
        while ((nextFeedback = rawFeedbacks.getNext()) != -1) {
            if (visiblePosition == 0) {
                visiblePosition = nextFeedback & XIMVisibleMask;
                if (visiblePosition != 0) {
                    int index = rawFeedbacks.getOffset() - 1;

                    if (visiblePosition == XIMVisibleToBackward)
                        visiblePositionInfo = TextHitInfo.leading(index);
                    else
                        visiblePositionInfo = TextHitInfo.trailing(index);
                }
            }
            nextFeedback &= ~XIMVisibleMask;
            if (currentFeedback != nextFeedback) {
                rawFeedbacks.unget();
                currentOffset = rawFeedbacks.getOffset();
                inputText.addAttribute(TextAttribute.INPUT_METHOD_HIGHLIGHT,
                                       convertVisualFeedbackToHighlight(currentFeedback),
                                       composedOffset + startOffset,
                                       composedOffset + currentOffset);
                startOffset = currentOffset;
                currentFeedback = nextFeedback;
            }
        }
        currentOffset = rawFeedbacks.getOffset();
        if (currentOffset >= 0) {
            inputText.addAttribute(TextAttribute.INPUT_METHOD_HIGHLIGHT,
                                   convertVisualFeedbackToHighlight(currentFeedback),
                                   composedOffset + startOffset,
                                   composedOffset + currentOffset);
        }

        postInputMethodEvent(InputMethodEvent.INPUT_METHOD_TEXT_CHANGED,
                             inputText.getIterator(),
                             composedOffset,
                             TextHitInfo.leading(caretPosition),
                             visiblePositionInfo,
                             when);
      }
    }

    /*
     * Subclasses should override disposeImpl() instead of dispose(). Client
     * code should always invoke dispose(), never disposeImpl().
     */
    protected synchronized void disposeImpl() {
        if (clientComponentWindow != null) {
            MComponentPeer peer = getPeer(clientComponentWindow);
            if (peer instanceof MInputMethodControl)
                ((MInputMethodControl)peer).removeInputMethod(this);
            clientComponentWindow = null;
        }

        disposeXIC();

        awtLock();
        composedText = null;
        committedText = null;
        rawFeedbacks = null;
        awtUnlock();

        awtFocussedComponent = null;
        xicFocussedComponent = null;
    }

    /**
     * Frees all X Window resources associated with this object.
     *
     * @see java.awt.im.spi.InputMethod#dispose
     */
    public final void dispose() {
        boolean call_disposeImpl = false;

        if (!disposed) {
            synchronized (this) {
                if (!disposed) {
                    disposed = call_disposeImpl = true;
                }
            }
        }

        if (call_disposeImpl) {
            disposeImpl();
        }
    }

    /**
     * Returns null.
     *
     * @see java.awt.im.spi.InputMethod#getControlObject
     */
    public Object getControlObject() {
        return null;
    }

    /**
     * @see java.awt.im.spi.InputMethod#removeNotify
     */
    public synchronized void removeNotify() {
        if (MToolkit.targetToPeer(getClientComponent()) != null) {
            dispose();
        } else {
            // We do not have to dispose XICs in case of lightweight component.
            resetXIC();
        }
    }

    /**
     * @see java.awt.im.spi.InputMethod#setCompositionEnabled(boolean)
     */
    public void setCompositionEnabled(boolean enable) {
        setCompositionEnabledNative(enable);
    }

    /**
     * @see java.awt.im.spi.InputMethod#isCompositionEnabled
     */
    public boolean isCompositionEnabled() {
        return isCompositionEnabledNative();
    }

    /**
     * Ends any input composition that may currently be going on in this
     * context. Depending on the platform and possibly user preferences,
     * this may commit or delete uncommitted text. Any changes to the text
     * are communicated to the active component using an input method event.
     *
     * <p>
     * A text editing component may call this in a variety of situations,
     * for example, when the user moves the insertion point within the text
     * (but outside the composed text), or when the component's text is
     * saved to a file or copied to the clipboard.
     *
     */
    public void endComposition() {
        if (disposed) {
            return;
        }

        // Remember composition state before calling resetXIC, if possible.
        boolean isCompositionEnabled = false;
        if (compositionEnableSupported) {
            try {
                isCompositionEnabled = isCompositionEnabled();
            } catch (UnsupportedOperationException e) {
                compositionEnableSupported = false;
            }
        }

        if (haveActiveClient()
            && composedText == null
            && committedText == null){
            needResetXIC = true;
            needResetXICClient = getClientComponent();
            needCompositionEnable = isCompositionEnabled;
            return;
        }

        String text = resetXIC();
        needResetXIC = false;

        // Remove any existing composed text by posting an InputMethodEvent
        // with null composed text.  It would be desirable to wait for a
        // dispatchComposedText call from X input method engine, but some
        // input method does not conform to the XIM specification and does
        // not call the preedit callback to erase preedit text on calling
        // XmbResetIC.  To work around this problem, do it here by ourselves.
        awtLock();
        try {
            composedText = null;
            postInputMethodEvent(InputMethodEvent.INPUT_METHOD_TEXT_CHANGED,
                                 null,
                                 0,
                                 null,
                                 null);

            if (text != null && text.length() > 0) {
                dispatchCommittedText(text);
            }
        }
        finally {
            awtUnlock();
        }

        // Restore the preedit state if it was enabled
        if (compositionEnableSupported && isCompositionEnabled) {
            try {
                setCompositionEnabled(true);
            } catch (UnsupportedOperationException e) {
                compositionEnableSupported = false;
            }
        }
    }

    /**
     * Changes the internal XIC configurations. This is required the
     * case that addition or elimination of text components has
     * happened in the containment hierarchy. This method is invoked
     * by Frame or Dialog.
     */
    synchronized void reconfigureXIC(MInputMethodControl control) {
        if (!disposed) {
            // Some IM servers require to reset XIC before destroying
            // the XIC. I.e., Destroying XIC doesn't reset the internal
            // state of the IM server. endComposition() takes care of
            // resetting XIC and preedit synchronization. However,
            // there is no client at this point. It is assumed that
            // the previous client is still available for dispatching
            // committed text which maintains client's composition
            // context.
            endComposition();
            resetXICifneeded();
            reconfigureXICNative((MComponentPeer) control, control.getTextComponent());
        }
    }

    /**
     * Performs mapping from an XIM visible feedback value to Java IM highlight.
     * @return Java input method highlight
     */
    private InputMethodHighlight convertVisualFeedbackToHighlight(int feedback) {
        InputMethodHighlight highlight;

        switch (feedback) {
        case XIMUnderline:
            highlight = InputMethodHighlight.UNSELECTED_CONVERTED_TEXT_HIGHLIGHT;
            break;
        case XIMReverse:
            highlight = InputMethodHighlight.SELECTED_CONVERTED_TEXT_HIGHLIGHT;
            break;
        case XIMHighlight:
            highlight = InputMethodHighlight.SELECTED_RAW_TEXT_HIGHLIGHT;
            break;
        case XIMPrimary:
            highlight = InputMethodHighlight.UNSELECTED_CONVERTED_TEXT_HIGHLIGHT;
            break;
        case XIMSecondary:
            highlight = InputMethodHighlight.SELECTED_CONVERTED_TEXT_HIGHLIGHT;
            break;
        case XIMTertiary:
            highlight = InputMethodHighlight.SELECTED_RAW_TEXT_HIGHLIGHT;
            break;
        default:
            highlight = InputMethodHighlight.SELECTED_RAW_TEXT_HIGHLIGHT;
            break;
        }
        return highlight;
    }

    // initial capacity size for string buffer, etc.
    private static final int INITIAL_SIZE = 64;

    /**
     * IntBuffer is an inner class that manipulates an int array and
     * provides UNIX file io stream-like programming interfaces to
     * access it. (An alternative would be to use ArrayList which may
     * be too expensive for the work.)
     */
    private final class IntBuffer {
        private int[] intArray;
        private int size;
        private int index;

        IntBuffer(int initialCapacity) {
            intArray = new int[initialCapacity];
            size = 0;
            index = 0;
        }

        void insert(int offset, int[] values) {
            int newSize = size + values.length;
            if (intArray.length < newSize) {
                int[] newIntArray = new int[newSize * 2];
                System.arraycopy(intArray, 0, newIntArray, 0, size);
                intArray = newIntArray;
            }
            System.arraycopy(intArray, offset, intArray, offset+values.length,
                             size - offset);
            System.arraycopy(values, 0, intArray, offset, values.length);
            size += values.length;
            if (index > offset)
                index = offset;
        }

        void remove(int offset, int length) {
            if (offset + length != size)
                System.arraycopy(intArray, offset+length, intArray, offset,
                                 size - offset - length);
            size -= length;
            if (index > offset)
                index = offset;
        }

        void replace(int offset, int[] values) {
            System.arraycopy(values, 0, intArray, offset, values.length);
        }

        void removeAll() {
            size = 0;
            index = 0;
        }

        void rewind() {
            index = 0;
        }

        int getNext() {
            if (index == size)
                return -1;
            return intArray[index++];
        }

        void unget() {
            if (index != 0)
                index--;
        }

        int getOffset() {
            return index;
        }

        public String toString() {
            StringBuffer s = new StringBuffer();
            for (int i = 0; i < size;) {
                s.append(intArray[i++]);
                if (i < size)
                    s.append(",");
            }
            return s.toString();
        }
    }

    /*
     * Native methods
     */
    private native boolean openXIM();
    private native boolean createXICNative(MComponentPeer peer, MComponentPeer tc);
    private native void reconfigureXICNative(MComponentPeer peer,
                                            MComponentPeer tc);
    private native void setXICFocus(MComponentPeer peer,
                                    boolean value, boolean active);
    private native String resetXIC();
    private native void disposeXIC();
    private native void closeXIM();
    private native void configureStatusAreaNative(MComponentPeer tc);
    private native void setCompositionEnabledNative(boolean enable);
    private native boolean isCompositionEnabledNative();
    private native void turnoffStatusWindow();
}
