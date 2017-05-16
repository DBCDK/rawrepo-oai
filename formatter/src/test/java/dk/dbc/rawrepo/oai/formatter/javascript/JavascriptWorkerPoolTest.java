/*
 * Copyright (C) 2017 DBC A/S (http://dbc.dk/)
 *
 * This is part of dbc-rawrepo-oai-formatter-dw
 *
 * dbc-rawrepo-oai-formatter-dw is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * dbc-rawrepo-oai-formatter-dw is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.rawrepo.oai.formatter.javascript;

import dk.dbc.rawrepo.RecordId;
import dk.dbc.rawrepo.oai.formatter.javascript.JavascriptWorkerPool.JavaScriptWorker;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class JavascriptWorkerPoolTest {
    
    private static String HEAD = "<marcx:record format=\"danMARC2\" type=\"Bibliographic\" xmlns:marcx=\"info:lc/xmlns/marcxchange-v1\">" +
        "<marcx:leader>00000c    2200000   4500</marcx:leader>" +
            "<marcx:datafield ind1=\"0\" ind2=\"0\" tag=\"001\">" +
                "<marcx:subfield code=\"a\">123456</marcx:subfield>" +
                "<marcx:subfield code=\"b\">870970</marcx:subfield>" +
            "</marcx:datafield>" +
            "<marcx:datafield ind1=\"0\" ind2=\"0\" tag=\"004\">" +
                "<marcx:subfield code=\"r\">c</marcx:subfield>" +
                "<marcx:subfield code=\"a\">b</marcx:subfield>" +
            "</marcx:datafield>" +
            "<marcx:datafield ind1=\"0\" ind2=\"0\" tag=\"014\">" +
                "<marcx:subfield code=\"a\">44783851</marcx:subfield>" +
            "</marcx:datafield>" +
            "<marcx:datafield ind1=\"0\" ind2=\"0\" tag=\"245\">" +
                "<marcx:subfield code=\"g\">4. bok</marcx:subfield>" +
            "</marcx:datafield>" +
        "</marcx:record>";
    
    private static String VOLUME = "<marcx:record format=\"danMARC2\" type=\"Bibliographic\" xmlns:marcx=\"info:lc/xmlns/marcxchange-v1\">" +
        "<marcx:leader>00000c    2200000   4500</marcx:leader>" +
            "<marcx:datafield ind1=\"0\" ind2=\"0\" tag=\"001\">" +
                "<marcx:subfield code=\"a\">234567</marcx:subfield>" +
                "<marcx:subfield code=\"b\">870970</marcx:subfield>" +
            "</marcx:datafield>" +
            "<marcx:datafield ind1=\"0\" ind2=\"0\" tag=\"004\">" +
                "<marcx:subfield code=\"r\">c</marcx:subfield>" +
                "<marcx:subfield code=\"a\">b</marcx:subfield>" +
            "</marcx:datafield>" +
            "<marcx:datafield ind1=\"0\" ind2=\"0\" tag=\"014\">" +
                "<marcx:subfield code=\"a\">44783851</marcx:subfield>" +
            "</marcx:datafield>" +
            "<marcx:datafield ind1=\"0\" ind2=\"0\" tag=\"245\">" +
                "<marcx:subfield code=\"g\">4. bok</marcx:subfield>" +
            "</marcx:datafield>" +
        "</marcx:record>";
    
    final JavascriptWorkerPool pool = new JavascriptWorkerPool(1);

    /**
     * Just testing that we are able to reach the javascript
     * @throws java.lang.Exception
     */
    @Test
    public void testGetAllowedFormats() throws Exception {
        try(JavaScriptWorker w = pool.borrowWorker()) {
            assertTrue(w.getAllowedFormats().size() > 0);
        }        
    }
    
    /**
     * Testing integration between Java and JavaScript.
     * JavaScript processes a Java MarcXChangeWrapper[] array
     * without throwing exception.
     * @throws Exception 
     */
    @Test
    public void testFormat() throws Exception {
        try(JavaScriptWorker w = pool.borrowWorker()) {
            MarcXChangeWrapper[] records = new MarcXChangeWrapper[] {
                new MarcXChangeWrapper(VOLUME, new RecordId[]{new RecordId("123456", 870970)}),
                new MarcXChangeWrapper(HEAD, new RecordId[]{new RecordId("234567", 870970)})
            };
        
            w.format(records, "marcx", Arrays.asList("nat"));
        }  
    }
}
