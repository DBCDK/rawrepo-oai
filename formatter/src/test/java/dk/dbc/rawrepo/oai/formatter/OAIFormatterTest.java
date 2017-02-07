/*
 * Copyright (C) 2017 DBC A/S (http://dbc.dk/)
 *
 * This is part of dbc-rawrepo-oai-formatter
 *
 * dbc-rawrepo-oai-formatter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * dbc-rawrepo-oai-formatter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.rawrepo.oai.formatter;

import dk.dbc.rawrepo.oai.formatter.OAIFormatter.FormatRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class OAIFormatterTest {
    
    List<String> SETS = Arrays.asList("set1", "set2");
    
    @Test
    public void testParseRequest() {
        FormatRequest request = FormatRequest.parse("870970:123456", "oai_dc", SETS);
        assertEquals(870970, request.agencyId);
        assertEquals("123456", request.bibRecId);
        assertEquals("oai_dc", request.format);
        assertEquals(SETS, request.sets);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testParseRequest_throwWhenIdIsNull() {
        FormatRequest.parse(null, "oai_dc", SETS);
    }
    @Test(expected = IllegalArgumentException.class)
    public void testParseRequest_throwWhenIdIsEmpty() {
        FormatRequest.parse("", "oai_dc", SETS);
    }
    @Test(expected = IllegalArgumentException.class)
    public void testParseRequest_throwWhenIdIsWrongFormat() {
        FormatRequest.parse("870970-123456", "oai_dc", SETS);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testParseRequest_throwWhenFormatIsNull() {
        FormatRequest.parse("870970:123456", null, SETS);
    }
    @Test(expected = IllegalArgumentException.class)
    public void testParseRequest_throwWhenFormatIsEmpty() {
        FormatRequest.parse("870970:123456", "", SETS);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testParseRequest_throwWhenSetsIsNull() {
        FormatRequest.parse("870970:123456", "oai_dc", null);
    }
    @Test(expected = IllegalArgumentException.class)
    public void testParseRequest_throwWhenSetsIsEmpty() {
        FormatRequest.parse("870970:123456", "oai_dc", new ArrayList<>());
    }
    
}
