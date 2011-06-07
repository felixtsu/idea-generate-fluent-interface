package com.googlecode.generatefluentinterface;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;

/**
 * <p>
 * <p> Date: 6/2/11 Time: 10:28 AM </p>
 *
 * @author Felix.ZHU
 * @since v
 */
class GenerateFluentInterfaceWorker {
// ------------------------------ FIELDS ------------------------------

    private PsiElementFactory elementFactory;
    private PsiClass psiClass;
    private CodeStyleManager codeStyleManager;




// --------------------------- CONSTRUCTORS ---------------------------

    public GenerateFluentInterfaceWorker(final Project project,
                                         final PsiClass clazz) {
        this.elementFactory = JavaPsiFacade.getElementFactory(project);
        this.psiClass = clazz;
        this.codeStyleManager = CodeStyleManager.getInstance(project);
    }

// -------------------------- OTHER METHODS --------------------------

    public void execute(final PsiField[] candidateFields) {
        for (PsiField candidateField : candidateFields) {
            createMethodFromText(buildReadMethodText(candidateField));
            createMethodFromText(buildWriteMethodText(candidateField));
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
        psiClass.add(psiMethod);
    }

    private String buildWriteMethodText(final PsiField candidateField) {
        return "public " + psiClass.getName() + " "
                + candidateField.getName() + "(" + "final " + candidateField.getType().getCanonicalText() + " " + candidateField.getName() + "){"
                + "this." + candidateField.getName() + " = " + candidateField.getName() + "; return this;" +
                "}";
    }
}
