package com.googlecode.generatefluentinterface;

import com.intellij.psi.*;

/**
* <p>
* <p> Date: 6/7/11 Time: 5:42 PM </p>
*
* @author Felix.ZHU
* @since v
*/
class FiMethodTester {
    private PsiMethod[] classPsiMethods;
    private PsiClass psiClass;

    FiMethodTester(final PsiClass psiClass) {
        this.psiClass = psiClass;
        this.classPsiMethods = psiClass.getMethods();
    }

    public boolean hasReadWriteMethod(final PsiField candidateField) {
        String fieldName = candidateField.getName();
        boolean readMethodFound = false, writeMethodFound = false;
        for (PsiMethod method : classPsiMethods) {
            if (isSameNamePublicNonFinalInstanceMethod(fieldName, method)) {
                if (isReadMethod(method, candidateField.getType())) {
                    readMethodFound = true;
                } else if (isWriteMethod(method, candidateField.getType())) {
                    writeMethodFound = true;
                }
            }
        }
        return readMethodFound && writeMethodFound;
    }

    private boolean isWriteMethod(final PsiMethod method, final PsiType type) {
        PsiType returnType = method.getReturnType();

        if (returnType == null) {
            return false;
        }

        if(!(returnType.getCanonicalText().equals(psiClass.getQualifiedName()))){
            return false;
        }

        PsiParameterList psiParameterList = method.getParameterList();
        PsiParameter[] psiParameter = psiParameterList.getParameters();
        if (psiParameter.length != 1) {
            return false;
        }
        return psiParameter[0].getType().equals(type);
    }

    private boolean isReadMethod(final PsiMethod method, final PsiType fieldType) {
        if (!(fieldType.equals(method.getReturnType()))) {
            return false;
        }

        return method.getParameterList().getParametersCount() == 0;
    }

    private boolean isSameNamePublicNonFinalInstanceMethod(final String fieldName, final PsiMethod method) {
        return method.getName().equals(fieldName)
                && method.hasModifierProperty(PsiModifier.PUBLIC)
                && !(method.hasModifierProperty(PsiModifier.FINAL))
                && !(method.hasModifierProperty(PsiModifier.STATIC));
    }
}
