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

import dk.dbc.jslib.ClasspathSchemeHandler;
import dk.dbc.jslib.Environment;
import dk.dbc.jslib.ModuleHandler;
import dk.dbc.jslib.SchemeURI;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class JavascriptWorkerPool {
    
    private static final Logger log = LoggerFactory.getLogger(JavascriptWorkerPool.class);
            
    private final ArrayBlockingQueue<JavaScriptWorker> pool;
    
    public JavascriptWorkerPool( int poolSize ) {
        
        log.info("Initializing JavaScript worker pool, size={}", poolSize);
        
        this.pool = new ArrayBlockingQueue<>(poolSize);
        for (int i = 0; i < poolSize; i++) {
            JavaScriptWorker worker = new JavaScriptWorker(pool);
            this.pool.add(worker);
        }
    }
    
    public JavaScriptWorker borrowWorker() throws InterruptedException {
        return pool.take();
    }
    
    public static final class JavaScriptWorker implements AutoCloseable {
        
        
        
        private static final Logger log = LoggerFactory.getLogger(JavaScriptWorker.class);
        
        private final ArrayBlockingQueue<JavaScriptWorker> pool;
        private static final String FORMATTER_SCRIPT = "javascript/formatter.js";
        private static final String FORMATTER_METHOD = "format";
        private static final String ALLOWED_FORMATS_METHOD = "allowedFormats";
        private final Set<String> allowedFormats;
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
        
        public JavaScriptWorker(ArrayBlockingQueue<JavaScriptWorker> pool) {
            this.pool = pool;
            
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

                allowedFormats = getAllowedFormats();
                log.info("Allowed formats: {}", allowedFormats);

            } catch (Exception ex) {
                log.error("Error initializing javascript", ex);
                throw new RuntimeException("Cannot initlialize javascript", ex);
            }
        }
        
        /**
         * Run script on content, adding data to solrInputDocument
         *
         * @param content String containing marcxchange
         * @param format The format to return
         * @param sets List of sets this request is allowed to see
         * @return
         * @throws Exception
         */
        public String format(String content, String format, List<String> sets) throws Exception {
            if (!allowedFormats.contains(format)) {
                throw new IllegalArgumentException("Format '" + format + "' not allowed. Formats allowed: " + allowedFormats);
            }
            return (String) environment.callMethod(FORMATTER_METHOD, new Object[]{content, format, sets});
        }
        
        public HashSet<String> getAllowedFormats() throws Exception {
             return new HashSet<>(
                Arrays.asList(((ScriptObjectMirror) environment.callMethod(ALLOWED_FORMATS_METHOD, new Object[]{})).to(String[].class))
            );
        }

        @Override
        public void close() throws Exception {
            pool.add(this);
        }
        
    }
    
}
