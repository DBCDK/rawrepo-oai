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
package dk.dbc.rawrepo.oai;

import dk.dbc.oai.pmh.OAIPMHerrorcodeType;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class OAIException extends RuntimeException {

    private static final long serialVersionUID = -7615459818265486252L;

    private final OAIPMHerrorcodeType code;

    public OAIException(OAIPMHerrorcodeType code, String message) {
        super(message);
        this.code = code;
    }

    public OAIPMHerrorcodeType getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "code: " + code + " " + super.toString();
    }

}
