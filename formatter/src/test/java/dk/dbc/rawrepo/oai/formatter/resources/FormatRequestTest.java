
package dk.dbc.rawrepo.oai.formatter.resources;

import dk.dbc.rawrepo.oai.formatter.resources.OaiFormatterResource.FormatRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;


public class FormatRequestTest {
    
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
