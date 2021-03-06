/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2017 The ZAP Development Team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.script;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Map;

import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link ScriptVars}.
 */
public class ScriptVarsUnitTest {

    @Before
    public void setUp() {
        ScriptVars.clear();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowToModifyReturnedGlobalVariables() {
        // Given
        Map<String, String> vars = ScriptVars.getGlobalVars();
        // When
        vars.put(createKey(), createValue());
        // Then = UnsupportedOperationException
    }

    @Test
    public void shouldSetGlobalVariable() {
        // Given
        String key = createKey();
        String value = createValue();
        // When
        ScriptVars.setGlobalVar(key, value);
        // Then
        assertThat(ScriptVars.getGlobalVars(), hasEntry(key, value));
        assertThat(ScriptVars.getGlobalVar(key), is(equalTo(value)));
    }

    @Test
    public void shouldClearGlobalVariableWithNullValue() {
        // Given
        String key = createKey();
        String value = createValue();
        ScriptVars.setGlobalVar(key, value);
        // When
        ScriptVars.setGlobalVar(key, null);
        // Then
        assertThat(ScriptVars.getGlobalVars(), not(hasEntry(key, value)));
        assertThat(ScriptVars.getGlobalVar(key), is(nullValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotSetGlobalVariableWithNullKey() {
        // Given
        String key = null;
        // When
        ScriptVars.setGlobalVar(key, createValue());
        // Then = IllegalArgumentException
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotSetGlobalVariableIfMoreThanAllowed() {
        // Given
        for (int i = 0; i <= ScriptVars.MAX_GLOBAL_VARS; i++) {
            ScriptVars.setGlobalVar(createKey(), createValue());
        }
        // When
        ScriptVars.setGlobalVar(createKey(), createValue());
        // Then = IllegalArgumentException
    }

    @Test
    public void shouldHaveNoScriptVariablesByDefault() {
        // Given
        String scriptName = "ScriptName";
        // When
        Map<String, String> vars = ScriptVars.getScriptVars(scriptName);
        // Then
        assertThat(vars, is(notNullValue()));
        assertThat(vars.size(), is(equalTo(0)));
    }

    @Test
    public void shouldReturnNullForNoScriptVariableSet() {
        // Given
        String scriptName = "ScriptName";
        String key = createKey();
        // When
        String value = ScriptVars.getScriptVar(scriptName, key);
        // Then
        assertThat(value, is(nullValue()));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowToModifyReturnedScriptVariables() {
        // Given
        String scriptName = "ScriptName";
        Map<String, String> vars = ScriptVars.getScriptVars(scriptName);
        // When
        vars.put(createKey(), createValue());
        // Then = UnsupportedOperationException
    }

    @Test
    public void shouldSetScriptVariableUsingScriptContext() {
        // Given
        String key = createKey();
        String value = createValue();
        String scriptName = "ScriptName";
        ScriptContext scriptContext = createScriptContextWithName(scriptName);
        // When
        ScriptVars.setScriptVar(scriptContext, key, value);
        // Then
        assertThat(ScriptVars.getScriptVars(scriptName), hasEntry(key, value));
        assertThat(ScriptVars.getScriptVar(scriptContext, key), is(equalTo(value)));
    }

    @Test
    public void shouldClearScriptVariableWithNullValueUsingScriptContext() {
        // Given
        String key = createKey();
        String value = createValue();
        String scriptName = "ScriptName";
        ScriptContext scriptContext = createScriptContextWithName(scriptName);
        ScriptVars.setScriptVar(scriptContext, key, value);
        // When
        ScriptVars.setScriptVar(scriptContext, key, null);
        // Then
        assertThat(ScriptVars.getScriptVars(scriptName), not(hasEntry(key, value)));
        assertThat(ScriptVars.getScriptVar(scriptContext, key), is(nullValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotSetScriptVariableUsingNullScriptContext() {
        // Given
        ScriptContext scriptContext = null;
        // When
        ScriptVars.setScriptVar(scriptContext, createKey(), createValue());
        // Then = IllegalArgumentException
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotSetScriptVariableUsingNullScriptNameInScriptContext() {
        // Given
        ScriptContext scriptContext = createScriptContextWithName(null);
        // When
        ScriptVars.setScriptVar(scriptContext, createKey(), createValue());
        // Then = IllegalArgumentException
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotSetScriptVariableUsingNonStringScriptNameInScriptContext() {
        // Given
        ScriptContext scriptContext = createScriptContextWithName(10);
        // When
        ScriptVars.setScriptVar(scriptContext, createKey(), createValue());
        // Then = IllegalArgumentException
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotSetScriptVariableWithNullKeyUsingScriptContext() {
        // Given
        ScriptContext scriptContext = createScriptContextWithName("ScriptName");
        // When
        ScriptVars.setScriptVar(scriptContext, null, createValue());
        // Then = IllegalArgumentException
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotSetScriptVariableWithInvalidKeyLengthUsingScriptContext() {
        // Given
        ScriptContext scriptContext = createScriptContextWithName("ScriptName");
        // When
        ScriptVars.setScriptVar(scriptContext, createKeyWithInvalidLength(), createValue());
        // Then = IllegalArgumentException
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotSetScriptVariableWithInvalidValueLengthUsingScriptContext() {
        // Given
        ScriptContext scriptContext = createScriptContextWithName("ScriptName");
        // When
        ScriptVars.setScriptVar(scriptContext, createKey(), createValueWithInvalidLength());
        // Then = IllegalArgumentException
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotSetScriptVariableIfMoreThanAllowedUsingScriptContext() {
        // Given
        ScriptContext scriptContext = createScriptContextWithName("ScriptName");
        for (int i = 0; i <= ScriptVars.MAX_SCRIPT_VARS; i++) {
            ScriptVars.setScriptVar(scriptContext, createKey(), createValue());
        }
        // When
        ScriptVars.setScriptVar(scriptContext, createKey(), createValue());
        // Then = IllegalArgumentException
    }

    @Test
    public void shouldNotReturnScriptVariablesFromOtherScriptsUsingScriptContext() {
        // Given
        ScriptContext scriptContext1 = createScriptContextWithName("ScriptName1");
        ScriptContext scriptContext2 = createScriptContextWithName("ScriptName2");
        String key = createKey();
        // When
        ScriptVars.setScriptVar(scriptContext1, key, createValue());
        // Then
        assertThat(ScriptVars.getScriptVar(scriptContext2, key), is(nullValue()));
    }

    @Test
    public void shouldSetScriptVariableUsingScriptName() {
        // Given
        String key = createKey();
        String value = createValue();
        String scriptName = "ScriptName";
        // When
        ScriptVars.setScriptVar(scriptName, key, value);
        // Then
        assertThat(ScriptVars.getScriptVars(scriptName), hasEntry(key, value));
        assertThat(ScriptVars.getScriptVar(scriptName, key), is(equalTo(value)));
    }

    @Test
    public void shouldClearScriptVariableWithNullValueUsingScriptName() {
        // Given
        String key = createKey();
        String value = createValue();
        String scriptName = "ScriptName";
        ScriptVars.setScriptVar(scriptName, key, value);
        // When
        ScriptVars.setScriptVar(scriptName, key, null);
        // Then
        assertThat(ScriptVars.getScriptVars(scriptName), not(hasEntry(key, value)));
        assertThat(ScriptVars.getScriptVar(scriptName, key), is(nullValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotSetScriptVariableUsingNullScriptName() {
        // Given
        String scriptName = null;
        // When
        ScriptVars.setScriptVar(scriptName, createKey(), createValue());
        // Then = IllegalArgumentException
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowToModifyReturnedScriptVariablesSet() {
        // Given
        String scriptName = "ScriptName";
        ScriptVars.setScriptVar(scriptName, createKey(), createValue());
        Map<String, String> vars = ScriptVars.getScriptVars(scriptName);
        // When
        vars.put(createKey(), createValue());
        // Then = UnsupportedOperationException
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotSetScriptVariableWithNullKeyUsingScriptName() {
        // Given
        String key = null;
        // When
        ScriptVars.setScriptVar("ScriptName", key, createValue());
        // Then = IllegalArgumentException
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotSetScriptVariableWithInvalidKeyLengthUsingScriptName() {
        // Given
        String key = createKeyWithInvalidLength();
        // When
        ScriptVars.setScriptVar("ScriptName", key, createValue());
        // Then = IllegalArgumentException
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotSetScriptVariableWithInvalidValueLengthUsingScriptName() {
        // Given
        String value = createValueWithInvalidLength();
        // When
        ScriptVars.setScriptVar("ScriptName", createKey(), value);
        // Then = IllegalArgumentException
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotSetScriptVariableIfMoreThanAllowedUsingScriptName() {
        // Given
        String scriptName = "ScriptName";
        for (int i = 0; i <= ScriptVars.MAX_SCRIPT_VARS; i++) {
            ScriptVars.setScriptVar(scriptName, createKey(), createValue());
        }
        // When
        ScriptVars.setScriptVar(scriptName, createKey(), createValue());
        // Then = IllegalArgumentException
    }

    @Test
    public void shouldNotReturnScriptVariablesFromOtherScriptsUsingScriptName() {
        // Given
        String scriptName1 = "ScriptName1";
        String scriptName2 = "ScriptName2";
        String key = createKey();
        // When
        ScriptVars.setScriptVar(scriptName1, key, createValue());
        // Then
        assertThat(ScriptVars.getScriptVar(scriptName2, key), is(nullValue()));
    }

    @Test
    public void shouldClearGlobalAndScriptVariables() {
        // Given
        String scriptName = "ScriptName";
        ScriptVars.setScriptVar(scriptName, createKey(), createValue());
        ScriptVars.setGlobalVar(createKey(), createValue());
        // When
        ScriptVars.clear();
        // Then
        assertThat(ScriptVars.getGlobalVars().size(), is(equalTo(0)));
        assertThat(ScriptVars.getScriptVars(scriptName).size(), is(equalTo(0)));
    }

    @Test
    public void shouldClearGlobalVariables() {
        // Given
        String scriptName = "ScriptName";
        ScriptVars.setScriptVar(scriptName, createKey(), createValue());
        ScriptVars.setGlobalVar(createKey(), createValue());
        // When
        ScriptVars.clearGlobalVars();
        // Then
        assertThat(ScriptVars.getGlobalVars().size(), is(equalTo(0)));
        assertThat(ScriptVars.getScriptVars(scriptName).size(), is(equalTo(1)));
    }

    @Test
    public void shouldClearScriptVariables() {
        // Given
        String scriptName1 = "ScriptName1";
        String scriptName2 = "ScriptName2";
        ScriptVars.setScriptVar(scriptName1, createKey(), createValue());
        ScriptVars.setScriptVar(scriptName2, createKey(), createValue());
        ScriptVars.setGlobalVar(createKey(), createValue());
        // When
        ScriptVars.clearScriptVars(scriptName1);
        // Then
        assertThat(ScriptVars.getScriptVars(scriptName1).size(), is(equalTo(0)));
        assertThat(ScriptVars.getGlobalVars().size(), is(equalTo(1)));
        assertThat(ScriptVars.getScriptVars(scriptName2).size(), is(equalTo(1)));
    }

    private static String createKey() {
        return "Key-" + Math.random();
    }

    private static String createValue() {
        return "Value-" + Math.random();
    }

    private static String createKeyWithInvalidLength() {
        return StringUtils.repeat("A", ScriptVars.MAX_KEY_SIZE + 1);
    }

    private static String createValueWithInvalidLength() {
        return StringUtils.repeat("A", ScriptVars.MAX_VALUE_SIZE + 1);
    }

    private static ScriptContext createScriptContextWithName(Object scriptName) {
        ScriptContext context = new SimpleScriptContext();
        context.setAttribute(ExtensionScript.SCRIPT_NAME_ATT, scriptName, ScriptContext.ENGINE_SCOPE);
        return context;
    }
}
