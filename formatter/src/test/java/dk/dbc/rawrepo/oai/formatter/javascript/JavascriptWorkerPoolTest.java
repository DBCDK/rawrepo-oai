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

import dk.dbc.rawrepo.oai.formatter.javascript.JavascriptWorkerPool.JavaScriptWorker;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class JavascriptWorkerPoolTest {
    
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
}
