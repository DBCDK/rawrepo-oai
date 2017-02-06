/*
 * dbc-rawrepo-solr-indexer
 * Copyright (C) 2015 Dansk Bibliotekscenter a/s, Tempovej 7-11, DK-2750 Ballerup,
 * Denmark. CVR: 15149043*
 *
 * This file is part of dbc-rawrepo-solr-indexer.
 *
 * dbc-rawrepo-solr-indexer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * dbc-rawrepo-solr-indexer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with dbc-rawrepo-solr-indexer.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.rawrepo.oai.formatter;

import dk.dbc.jslib.ClasspathSchemeHandler;
import dk.dbc.jslib.Environment;
import dk.dbc.jslib.ModuleHandler;
import dk.dbc.jslib.SchemeURI;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
@Stateless
public class JavaScriptWorker {

    private static final Logger log = LoggerFactory.getLogger(JavaScriptWorker.class);
    private static final String FORMATTER_SCRIPT = "javascript/formatter.js";
    private static final String FORMATTER_METHOD = "format";

    /**
     * Std search path
     */
    private static final String[] searchPaths = new String[]{
        "classpath:javascript/",
        "classpath:javascript/javacore/",
        "classpath:javascript/jscommon/config/",
        "classpath:javascript/jscommon/convert/",
        "classpath:javascript/jscommon/devel/",
        "classpath:javascript/jscommon/external/",
        "classpath:javascript/jscommon/io/",
        "classpath:javascript/jscommon/marc/",
        "classpath:javascript/jscommon/net/",
        "classpath:javascript/jscommon/system/",
        "classpath:javascript/jscommon/util/",
        "classpath:javascript/jscommon/xml/"
    };

    private Environment environment;

    @PostConstruct
    public void create() {
        try {
            environment = new Environment();
            ModuleHandler mh = new ModuleHandler();
            mh.registerNonCompilableModule("Tables"); // Unlikely we need this module.

            // Classpath searchpath
            ClasspathSchemeHandler classpath = new ClasspathSchemeHandler(getClass().getClassLoader());
            mh.registerHandler("classpath", classpath);
            for (String searchPath : searchPaths) {
                mh.addSearchPath(new SchemeURI(searchPath));
            }

            // Use system
            environment.registerUseFunction(mh);

            // Evaluate script
            InputStream stream = getClass().getClassLoader().getResourceAsStream(FORMATTER_SCRIPT);
            InputStreamReader inputStreamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        
            environment.eval(inputStreamReader, FORMATTER_SCRIPT);
        } catch (Exception ex) {
            log.error("Error initializing javascript", ex);
            throw new RuntimeException("Cannot initlialize javascript", ex);
        }
    }
    
  

    /**
     * Run script on content, adding data to solrInputDocument
     *
     * @param solrInputDocument target
     * @param content           String containing marcxchange
     * @param mimetype          mimetype of marcxchange
     * @throws Exception
     */
    public String format(String content, String format, List<String>sets) throws Exception {
        return (String) environment.callMethod(FORMATTER_METHOD, new Object[]{content, format, sets});
    }

}
