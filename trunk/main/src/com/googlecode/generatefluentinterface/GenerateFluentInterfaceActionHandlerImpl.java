package com.googlecode.generatefluentinterface;

import com.intellij.codeInsight.generation.MemberChooserObject;
import com.intellij.codeInsight.generation.PsiElementMemberChooserObject;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.lang.reflect.Member;
import java.util.*;

/**
 * <p>
 * <p> Date: 6/1/11 Time: 5:12 PM </p>
 *
 * @author Felix.ZHU
 * @since v
 */
class GenerateFluentInterfaceActionHandlerImpl extends EditorWriteActionHandler {
// ------------------------------ FIELDS ------------------------------

    private static final Logger logger = Logger.getInstance(GenerateFluentInterfaceActionHandlerImpl.class);

// --------------------------- CONSTRUCTORS ---------------------------

    public GenerateFluentInterfaceActionHandlerImpl() {
    }

// -------------------------- OTHER METHODS --------------------------

    @Override
    public void executeWriteAction(final Editor editor, final DataContext dataContext) {
        final Project project = LangDataKeys.PROJECT.getData(dataContext);

        assert project != null;

        PsiClass clazz = getSubjectClass(editor, dataContext);

        assert clazz != null;

        doExecuteAction(project, clazz, editor);
    }

    private void doExecuteAction(final Project project, final PsiClass clazz, final Editor editor) {
        logger.debug("==== do execute action starts here ====");
        final PsiField[] candidateFields = buildShowClassMembers(project, clazz);

        if (candidateFields.length == 0) {
            HintManager.getInstance().showErrorHint(editor, "No members to generate fluent interface have been found.");
            return;
        }

        CommandProcessor.getInstance().executeCommand(project, new Runnable(){
            public void run() {
                new GenerateFluentInterfaceWorker(project, clazz).execute(candidateFields);
            }
        }, "GenerateFluentInterface", null);

    }


    private PsiField[] buildShowClassMembers(final Project project, final PsiClass clazz) {
        PsiField[] allFields = clazz.getFields();
        return allFields;
    }

    @Nullable
    private PsiClass getSubjectClass(Editor editor, DataContext dataContext) {
        PsiFile file = LangDataKeys.PSI_FILE.getData(dataContext);
        if (file == null) {
            return null;
        }

        int offset = editor.getCaretModel().getOffset();
        PsiElement context = file.findElementAt(offset);

        if (context == null) {
            return null;
        }

        PsiClass clazz = PsiTreeUtil.getParentOfType(context, PsiClass.class, false);
        if (clazz == null) {
            return null;
        }

        // must not be an interface
        return clazz.isInterface() ? null : clazz;
    }
}
