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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import dk.dbc.forsrights.client.ForsRights;
import dk.dbc.forsrights.client.ForsRightsException;
import dk.dbc.forsrights.client.ForsRightsServiceFromURL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class AccessControl {

    private static final Logger log = LoggerFactory.getLogger(AccessControl.class);
    private final Timer authenticate;

    public static class Response {

        private final Set<String> allowedSets;
        private final String id;

        public Response(Set<String> allowedSets, String id) {
            this.allowedSets = allowedSets;
            this.id = id;
        }

        public Set<String> getAllowedSets() {
            return allowedSets;
        }

        public String getId() {
            return id;
        }
    }

    private final OAIConfiuration config;

    public AccessControl(OAIConfiuration config, DataSource rawrepoOai, MetricRegistry metrics) throws SQLException {
        this.config = config;
        this.authenticate = metrics.timer(getClass().getCanonicalName() + "/authenticate");

        forsRightsService = ForsRightsServiceFromURL.builder()
                .build(config.getForsRightsUrl());
        forsRights = forsRightsService.forsRights();
        allSets = makeAllSets(rawrepoOai);
        rightsRules = makeRightsRules(config.getForsRightsRules());
    }

    private final HashMap<String, Collection<String>> rightsRules;

    private final ForsRightsServiceFromURL forsRightsService;
    private final ForsRights forsRights;
    private final Set<String> allSets;

    /**
     * Construct a map from forsright (name,rule) to collection of setSpec's
     * that require authentication (access control)
     */
    private static HashMap<String, Collection<String>> makeRightsRules(String rules) {
        HashMap<String, Collection<String>> rightsRules = new HashMap<>();
        for (String rightsRule : rules.split("\\s+")) {
            String[] parts = rightsRule.split("=", 2);
            rightsRules.put(parts[0], Arrays.asList(parts[1].split(",")));
        }
        log.debug("rightsRulesMap = " + rightsRules);
        return rightsRules;
    }

    /**
     * Pull all setSpec's from the database into allSets
     *
     * @throws EJBException Wrapper for SQLException
     */
    private static HashSet<String> makeAllSets(DataSource rawrepoOai) throws SQLException {
        HashSet<String> allSets = new HashSet<>();
        try (Connection connection = rawrepoOai.getConnection() ;
             PreparedStatement stmt = connection.prepareStatement("SELECT setSpec FROM oaisets") ;
             ResultSet resultSet = stmt.executeQuery()) {
            while (resultSet.next()) {
                allSets.add(resultSet.getString(1));
            }
        }
        log.debug("allSets = " + allSets);
        return allSets;
    }

    /**
     * Return a immutable set of all known setSpecs
     *
     * @return unmodifiable set of setSpec
     */
    public Set<String> getAllSets() {
        return Collections.unmodifiableSet(allSets);
    }

    /**
     * Authenticate user and generate access control object
     *
     * @param identity Colon/slash seperated tripple user:group:pass
     * @param ip       string containing ip number
     * @return Response with user id & allowed setSpecs
     */
    public Response authenticate(String identity, String ip) {
        try (Timer.Context time = authenticate.time()) {
            if (config.getNoAuthentication()) {
                return new Response(allSets, ip);
            }
            String id = ip;
            ForsRights.RightSet rights = null;
            try {
                if (identity != null) {
                    String[] authParts = identity.split("[:/]", 3);
                    if (authParts.length != 3) {
                        log.info("Invalid authentication: " + identity);
                        return null;
                    }
                    id = authParts[1];
                    rights = forsRights.lookupRight(authParts[0], authParts[1], authParts[2], null);
                } else {
                    rights = forsRights.lookupRight(null, null, null, ip);
                }

                HashSet<String> set = new HashSet<>(allSets);

                for (Map.Entry<String, Collection<String>> entry : rightsRules.entrySet()) {
                    String[] parts = entry.getKey().split(",", 2);
                    if (!rights.hasRight(parts[0], parts[1])) {
                        set.removeAll(entry.getValue());
                    }
                }

                return new Response(set, id);
            } catch (ForsRightsException ex) {
                log.error("Exception: " + ex.getMessage());
                log.debug("Exception: ", ex);
                throw new ServerErrorException(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR, "Cound not authenticate");
            }
        }
    }
}
