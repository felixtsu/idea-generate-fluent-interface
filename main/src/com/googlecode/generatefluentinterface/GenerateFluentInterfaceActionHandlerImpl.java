package com.googlecode.generatefluentinterface;

import com.intellij.codeInsight.generation.PsiFieldMember;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.ide.util.MemberChooser;
import com.intellij.ide.util.MemberChooserBuilder;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.SpeedSearchBase;
import com.intellij.ui.TreeSpeedSearch;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * <p> Date: 6/1/11 Time: 5:12 PM </p>
 *
 * @author Felix.ZHU
 * @since v
 */
class GenerateFluentInterfaceActionHandlerImpl extends EditorWriteActionHandler {
// ------------------------------ FIELDS ------------------------------

    private static final Logger logger = Logger.getInstance("#com.googlecode.generatefluentinterface.GenerateFluentInterfaceActionHandlerImpl");

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

    private void doExecuteAction(final Project project, final PsiClass psiClass, final Editor editor) {
        final Collection<PsiField> candidateFields = pickupCandidateFields(psiClass);

        if (candidateFields.size() == 0) {
            HintManager.getInstance().showErrorHint(editor, "No members to generate fluent interface have been found.");
            return;
        }

        List<PsiFieldMember> psiFieldMembers = new LinkedList<PsiFieldMember>();
        for (PsiField candidateField : candidateFields) {
            psiFieldMembers.add(new PsiFieldMember(candidateField));
        }

        chooseMemberAndRun(project, psiClass, psiFieldMembers.toArray(new PsiFieldMember[psiFieldMembers.size()]));
    }

    private Collection<PsiField> pickupCandidateFields(final PsiClass psiClass) {
        final List<PsiField> privateNonFinalInstanceFields = getPrivateNonFinalInstanceFields(psiClass);

        return filterThoseAlreadyHave(psiClass, privateNonFinalInstanceFields);
    }

    private List<PsiField> getPrivateNonFinalInstanceFields(final PsiClass psiClass) {
        PsiField[] allFields = psiClass.getFields();
        List<PsiField> candidateFields = new LinkedList<PsiField>();
        for (PsiField field : allFields) {
            PsiModifierList psiModifierList = field.getModifierList();
            if (psiModifierList != null && isPrivateNonFinalInstance(psiModifierList)) {
                candidateFields.add(field);
            }
        }

        return candidateFields;
    }

    private boolean isPrivateNonFinalInstance(final PsiModifierList psiModifierList) {
        return psiModifierList.hasModifierProperty(PsiModifier.PRIVATE) &&
                !(psiModifierList.hasModifierProperty(PsiModifier.FINAL)) &&
                !(psiModifierList.hasModifierProperty(PsiModifier.STATIC));
    }

    private Collection<PsiField> filterThoseAlreadyHave(final PsiClass psiClass, final List<PsiField> candidateFields) {
        Collection<PsiField> result = new LinkedList<PsiField>();

        FiMethodTester tester = new FiMethodTester(psiClass);

        for (PsiField candidateField : candidateFields) {
            if (!tester.hasReadWriteMethod(candidateField)) {
                result.add(candidateField);
            }
        }
        return result;
    }

    private void chooseMemberAndRun(final Project project, final PsiClass clazz, final PsiFieldMember[] classMembers) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (project.isDisposed()) {
                    return;
                }

                MemberChooserBuilder<PsiFieldMember> builder = new MemberChooserBuilder<PsiFieldMember>(project);
                builder.copyJavadocVisible(false);
                builder.allowEmptySelection(false);
                builder.allowMultiSelection(true);
                builder.setTitle("generate fluent interface members");

                MemberChooser<PsiFieldMember> chooser
                        = builder.createBuilder(classMembers);
                chooser.selectElements(classMembers);

                logger.info("result = " + SpeedSearchBase.class.isAssignableFrom(TreeSpeedSearch.class));


                chooser.show();


                if (chooser.getExitCode() == MemberChooser.OK_EXIT_CODE) {
                    final List<PsiFieldMember> list = chooser.getSelectedElements();
                    if (list == null) {
                        return;
                    }

                    final List<PsiField> chosenFields = new LinkedList<PsiField>();
                    for (PsiFieldMember classMember : list) {
                        chosenFields.add(classMember.getElement());
                    }

                    executeGenerateLater(project, clazz, chosenFields.toArray(new PsiField[chosenFields.size()]));
                }
            }
        });
    }

    private void executeGenerateLater(final Project project, final PsiClass clazz, final PsiField[] chosenFields) {
        CommandProcessor.getInstance().executeCommand(project, new Runnable() {
                    public void run() {
                        ApplicationManager.getApplication().runWriteAction(new Runnable() {
                            public void run() {
                                new GenerateFluentInterfaceWorker(project, clazz).execute(chosenFields);
                            }
                        });
                    }
                }, "GenerateFluentInterface", null);
    }
}
