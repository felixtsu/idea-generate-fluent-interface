package com.googlecode.generatefluentinterface;

import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.sixrr.rpp.intention.PsiElementPredicate;
import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * <p> Date: 6/2/11 Time: 10:28 AM </p>
 *
 * @author Felix.ZHU
 * @since v
 */
class GenerateFluentInterfaceWorker {
// ------------------------------ FIELDS ------------------------------

    private Project project;
    private PsiElementFactory elementFactory;
    private PsiClass psiClass;
    private CodeStyleManager codeStyleManager;
    private String setterPrefix;
    private boolean generateGetter;
    private Editor editor;


// --------------------------- CONSTRUCTORS ---------------------------

    public GenerateFluentInterfaceWorker(final Project project,
                                         final Editor editor,
                                         final PsiClass clazz,
                                         final String setterPrefix,
                                         final boolean generateGetter) {
        this.project = project;
        this.elementFactory = JavaPsiFacade.getElementFactory(project);
        this.editor = editor;
        this.psiClass = clazz;
        this.codeStyleManager = CodeStyleManager.getInstance(project);
        this.setterPrefix = setterPrefix;
        this.generateGetter = generateGetter;
    }

// -------------------------- OTHER METHODS --------------------------

    public void execute(final PsiField[] candidateFields) {
        if (generateGetter) {
            generateGetterMethods(candidateFields);
        }

        generateSetterMethods(candidateFields);
    }

    private void generateSetterMethods(final PsiField[] candidateFields) {
        for (PsiField candidateField : candidateFields) {
            createMethodFromText(buildWriteMethodText(candidateField));
        }
    }

    private void generateGetterMethods(final PsiField[] candidateFields) {
        for (PsiField candidateField : candidateFields) {
            createMethodFromText(buildReadMethodText(candidateField));
        }
    }

    private String buildReadMethodText(final PsiField candidateField) {
        return "public " + candidateField.getType().getCanonicalText() + " "
                + candidateField.getName() + "() " +
                "{ " +
                "return this." + candidateField.getName() + ";" +
                "}";
    }

    private void createMethodFromText(String methodText) {
        PsiMethod psiMethod = elementFactory.createMethodFromText(methodText, null);
        codeStyleManager.reformat(psiMethod);

        PsiElement element = findMatchingElement(psiClass.getContainingFile(), editor);
        psiClass.addAfter(psiMethod, element);
    }

    private static final PsiElementPredicate predicate = new PsiElementPredicate() {
        @Override
        public boolean satisfiedBy(final PsiElement psiElement) {
            if (psiElement.getParent() == null) {
                return true;
            } else {
                return psiElement instanceof PsiMethod || psiElement instanceof PsiField;
            }
        }
    };

    @Nullable
    PsiElement findMatchingElement(PsiFile file,
                                   Editor editor) {
        final CaretModel caretModel = editor.getCaretModel();
        final int position = caretModel.getOffset();
        PsiElement element = file.findElementAt(position);
        while (element != null) {
            if (predicate.satisfiedBy(element)) {
                return element;
            } else {
                element = element.getPrevSibling();
            }
        }

        return file;

    }

    private String buildWriteMethodText(final PsiField candidateField) {
        return "public " + psiClass.getName() + " "
                + constructSetterName(candidateField) + "(" + "final " + candidateField.getType().getCanonicalText() + " " + candidateField.getName() + "){"
                + "this." + candidateField.getName() + " = " + candidateField.getName() + "; return this;" +
                "}";
    }

    private String constructSetterName(final PsiField candidateField) {
        final String fieldName = candidateField.getName();
        if (setterPrefix.equals("")) {
            return fieldName;
        } else {
            return setterPrefix + upperFirstLetter(fieldName);
        }
    }

    private String upperFirstLetter(final String name) {
        char[] firstLetter = new char[]{name.charAt(0)};
        return new String(firstLetter).toUpperCase() + name.substring(1);
    }
}
