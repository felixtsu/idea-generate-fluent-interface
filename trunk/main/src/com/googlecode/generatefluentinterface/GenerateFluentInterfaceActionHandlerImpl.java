package com.googlecode.generatefluentinterface;

import com.intellij.codeInsight.generation.PsiFieldMember;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.ide.util.MemberChooser;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
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


// --------------------------- CONSTRUCTORS ---------------------------

    public GenerateFluentInterfaceActionHandlerImpl() {
    }

// -------------------------- OTHER METHODS --------------------------

    @Override
    public void executeWriteAction(final Editor editor, final DataContext dataContext) {
        final Project project = LangDataKeys.PROJECT.getData(dataContext);

        assert project != null;

        PsiClass clazz = getSubjectClass(editor, dataContext);

        if (clazz == null) {
            return;
        }

        if (clazz.isInterface()) {
            HintManager.getInstance().showErrorHint(editor, "Can't generate fluent interface methods in an interface.");
        }

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

        return clazz;

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

        chooseMemberAndRun(project,
                editor, psiClass,
                psiFieldMembers.toArray(new PsiFieldMember[psiFieldMembers.size()])
        );
    }

    private Collection<PsiField> pickupCandidateFields(final PsiClass psiClass) {
        final List<PsiField> fields = getCandidateFields(psiClass);
        return filterExistCandidates(psiClass, fields);
    }

    private List<PsiField> getCandidateFields(final PsiClass psiClass) {
        PsiField[] allFields = psiClass.getAllFields();
        List<PsiField> candidateFields = new LinkedList<PsiField>();
        for (PsiField field : allFields) {
            PsiModifierList psiModifierList = field.getModifierList();
            if (psiModifierList != null && isCandidateField(psiModifierList)) {
                candidateFields.add(field);
            }
        }

        return candidateFields;
    }

    private boolean isCandidateField(final PsiModifierList psiModifierList) {
        return (!(psiModifierList.hasModifierProperty(PsiModifier.FINAL)) &&
                !(psiModifierList.hasModifierProperty(PsiModifier.STATIC)));
    }

    private Collection<PsiField> filterExistCandidates(final PsiClass psiClass, final List<PsiField> candidateFields) {
        Collection<PsiField> result = new LinkedList<PsiField>();

        FiMethodTester tester = new FiMethodTester(psiClass);

        for (PsiField candidateField : candidateFields) {
            if (!tester.hasReadWriteMethod(candidateField)) {
                result.add(candidateField);
            }
        }
        return result;
    }

    private void chooseMemberAndRun(final Project project,
                                    final Editor editor, final PsiClass clazz,
                                    final PsiFieldMember[] classMembers) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (project.isDisposed()) {
                    return;
                }

                GenerateFluentInterfaceMemberChooser chooser
                        = new GenerateFluentInterfaceMemberChooser(classMembers, project);
                chooser.setTitle("Generate Fluent Interface Members");
                chooser.selectElements(classMembers);

                chooser.show();


                if (chooser.getExitCode() == MemberChooser.OK_EXIT_CODE) {
                    final List<PsiFieldMember> list = chooser.getSelectedElements();
                    final String setterPrefix = chooser.getSetterPrefix();
                    final boolean generateGetter = chooser.generateGetters();
                    if (list == null) {
                        return;
                    }

                    GenerateFluentInterfaceApplicationComponent applicationComponent
                            = ApplicationManager.getApplication()
                            .getComponent(GenerateFluentInterfaceApplicationComponent.class);

                    applicationComponent.updateIsGeneratingGetters(generateGetter);
                    applicationComponent.updateSetterPrefix(setterPrefix);

                    final List<PsiField> chosenFields = new LinkedList<PsiField>();
                    for (PsiFieldMember classMember : list) {
                        chosenFields.add(classMember.getElement());
                    }

                    executeGenerateLater(project,
                            editor, clazz,
                            chosenFields.toArray(new PsiField[chosenFields.size()]),
                            setterPrefix,
                            generateGetter
                    );
                }
            }
        });
    }

    private void executeGenerateLater(final Project project,
                                      final Editor editor, final PsiClass clazz,
                                      final PsiField[] chosenFields,
                                      final String setterPrefix,
                                      final boolean generateGetter) {
        CommandProcessor.getInstance().executeCommand(project, new Runnable() {
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    public void run() {
                        new GenerateFluentInterfaceWorker(project, editor, clazz,
                                setterPrefix, generateGetter)
                                .execute(chosenFields);
                    }
                });
            }
        }, "GenerateFluentInterface", null);
    }
}
