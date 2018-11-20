/*
 * Copyright (C) 2017 DBC A/S (http://dbc.dk/)
 *
 * This is part of dbc-rawrepo-oai-service
 *
 * dbc-rawrepo-oai-service is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * dbc-rawrepo-oai-service is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc;

import io.dropwizard.configuration.UndefinedEnvironmentVariableException;
import org.apache.commons.text.StrBuilder;
import org.apache.commons.text.StrLookup;
import org.apache.commons.text.StrMatcher;
import org.apache.commons.text.StrSubstitutor;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class RequireSubstitution extends StrSubstitutor {

    public RequireSubstitution() {
        super(EXPANDER);
        setEnableSubstitutionInVariables(true);
        setEscapeChar('\\');
    }

    @Override
    protected String resolveVariable(String variableName, StrBuilder buf, int startPos, int endPos) {
        String resolved = getVariableResolver().lookup(variableName);
        if (resolved == null) {
            StrMatcher variablePrefixMatcher = getVariablePrefixMatcher();
            StrMatcher valueDelimiterMatcher = getValueDelimiterMatcher();
            char[] buffer = buf.toCharArray();
            int start = startPos;
            start += variablePrefixMatcher.isMatch(buffer, start);
            start += variableName.length();
            int matchLength = valueDelimiterMatcher.isMatch(buffer, start);
            if (matchLength == 0) {
                throw new UndefinedEnvironmentVariableException(
                        "The environment variable '" + variableName +
                        "' is not defined; could not substitute the expression '" +
                        buf.substring(startPos, endPos) + "'.");
            }
        }
        return resolved;
    }

    private static final Expander EXPANDER = new Expander();

    private static class Expander extends StrLookup<Object> {

        @Override
        public String lookup(String key) {
            return System.getProperty(key, System.getenv(key));
        }
    }

}
