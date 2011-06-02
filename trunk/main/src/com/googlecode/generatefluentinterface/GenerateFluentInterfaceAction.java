package com.googlecode.generatefluentinterface;

import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;

/**
 * <p>
 * <p> Date: 6/1/11 Time: 4:41 PM </p>
 *
 * @author Felix.ZHU
 * @since v
 */
public class GenerateFluentInterfaceAction extends EditorAction {

    protected GenerateFluentInterfaceAction() {
        super(new GenerateFluentInterfaceActionHandlerImpl());
    }
}
