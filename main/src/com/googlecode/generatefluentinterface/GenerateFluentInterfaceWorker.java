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
    private String setterPrefix;
    private boolean generateGetter;


// --------------------------- CONSTRUCTORS ---------------------------

    public GenerateFluentInterfaceWorker(final Project project,
                                         final PsiClass clazz, final String setterPrefix, final boolean generateGetter) {
        this.elementFactory = JavaPsiFacade.getElementFactory(project);
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
        psiClass.add(psiMethod);
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
