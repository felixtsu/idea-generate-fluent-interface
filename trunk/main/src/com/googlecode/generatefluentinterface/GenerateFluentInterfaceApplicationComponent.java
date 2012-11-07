package com.googlecode.generatefluentinterface;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * <p> Date: 12-11-7 Time: ÏÂÎç5:05 </p>
 *
 * @author huaibi.zdj
 */

@State(
        name = "GenerateFluentInterfaceState",
        storages = {
                @Storage(id = "default", file = "$APP_CONFIG$/generateFluentInterface.xml")
        }
)
public class GenerateFluentInterfaceApplicationComponent
        implements ApplicationComponent,
        PersistentStateComponent<GenerateFluentInterfaceApplicationComponent.State> {
// ------------------------------ FIELDS ------------------------------

    private State state = new State();

// --------------------------- CONSTRUCTORS ---------------------------

    public GenerateFluentInterfaceApplicationComponent() {
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    @Override
    public State getState() {
        return state;
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BaseComponent ---------------------

    public void initComponent() {
        // TODO: insert component initialization logic here
    }

    public void disposeComponent() {
        // TODO: insert component disposal logic here
    }

// --------------------- Interface NamedComponent ---------------------

    @NotNull
    public String getComponentName() {
        return "GenerateFluentInterfaceApplicationComponent";
    }

// --------------------- Interface PersistentStateComponent ---------------------


    @Override
    public void loadState(final State o) {
        this.state = o;
    }

// -------------------------- OTHER METHODS --------------------------

    public String getSetterPrefix() {
        return this.state.getSetterPrefix();
    }

    public boolean isGeneratingGetters() {
        return this.state.isGenerateGetters();
    }

    public void updateSetterPrefix(String setterPrefix) {
        this.state.setSetterPrefix(setterPrefix);
    }

    public void updateIsGeneratingGetters(boolean b) {
        this.state.setGenerateGetters(b);
    }

// -------------------------- INNER CLASSES --------------------------

    public static class State {
        private String setterPrefix;
        private boolean generateGetters;

        public String getSetterPrefix() {
            return setterPrefix;
        }

        public void setSetterPrefix(final String setterPrefix) {
            this.setterPrefix = setterPrefix;
        }

        public boolean isGenerateGetters() {
            return generateGetters;
        }

        public void setGenerateGetters(final boolean generateGetters) {
            this.generateGetters = generateGetters;
        }
    }
}
