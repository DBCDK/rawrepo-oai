/*
 * Copyright (C) 2017 DBC A/S (http://dbc.dk/)
 *
 * This is part of dbc-rawrepo-oai-harvester-dw
 *
 * dbc-rawrepo-oai-harvester-dw is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * dbc-rawrepo-oai-harvester-dw is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.rawrepo.oai.setmatcher.javascript;

import dk.dbc.jslib.ClasspathSchemeHandler;
import dk.dbc.jslib.Environment;
import dk.dbc.jslib.ModuleHandler;
import dk.dbc.jslib.SchemeURI;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaScriptWorker {

    private static final Logger log = LoggerFactory.getLogger(JavaScriptWorker.class);

    private static final String OAI_SET_MATCHER_SCRIPT = "javascript/oaiSetMatcher.js";
    private static final String OAI_SET_MATCHER_METHOD = "oaiSetMatcher";
    private final Environment environment;

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

    public JavaScriptWorker() {
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
            InputStream stream = getClass().getClassLoader().getResourceAsStream(OAI_SET_MATCHER_SCRIPT);
            InputStreamReader inputStreamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);

            environment.eval(inputStreamReader, OAI_SET_MATCHER_SCRIPT);

        } catch (Exception ex) {
            log.error("Error initializing javascript", ex);
            throw new RuntimeException("Cannot initlialize javascript", ex);
        }
    }

    /**
     * Run script on content, determining which OAI sets the MarcX record
     * belongs to.
     *
     * @param bibliographicRecordId
     * @param agencyId
     * @param content String containing marcxchange
     * @return
     * @throws Exception
     */
    public String[] getOaiSets(int agencyId, String content, RawRepoRecordFetcher rawrepo) throws Exception {
        return environment.getJavascriptObjectAsStringArray(
                environment.callMethod(OAI_SET_MATCHER_METHOD, new Object[]{agencyId, content, rawrepo})
        );
    }

}
