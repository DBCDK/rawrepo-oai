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

import dk.dbc.eeconfig.EEConfig;
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
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
@Singleton
public class AccessControl {

    private static final Logger log = LoggerFactory.getLogger(AccessControl.class);

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

    @Resource(lookup = C.DATASOURCE)
    DataSource rawrepoOai;

    @Inject
    @EEConfig.Name(C.FORS_RIGHTS_URL)
    @EEConfig.Default(C.FORS_RIGHTS_URL_DEFAULT)
    public String forsRightsUrl;

    @Inject
    @EEConfig.Name(C.FORS_RIGHTS_RULES)
    @EEConfig.Default(C.FORS_RIGHTS_RULES_DEFAULT)
    public String forsRightsRules;

    @Inject
    @EEConfig.Name(C.NO_AUTHENTICATION)
    @EEConfig.Default(C.NO_AUTHENTICATION_DEFAULT)
    public boolean noAuthentication;

    private HashMap<String, Collection<String>> rightsRules;

    private ForsRightsServiceFromURL forsRightsService;
    private ForsRights forsRights;
    private Set<String> allSets;

    @PostConstruct
    public void init() {
        forsRightsService = ForsRightsServiceFromURL.builder()
                .build(forsRightsUrl);
        forsRights = forsRightsService.forsRights();
        makeAllSets();
        makeRightsRules();
    }

    /**
     * Construct a map from forsright (name,rule) to collection of setSpec's
     * that require authentication (access control)
     */
    private void makeRightsRules() {
        rightsRules = new HashMap<>();
        for (String righsRule : forsRightsRules.split("\\s+")) {
            String[] parts = righsRule.split("=", 2);
            rightsRules.put(parts[0], Arrays.asList(parts[1].split(",")));
        }
        log.debug("rightsRulesMap = " + rightsRules);
    }

    /**
     * Pull all setSpec's from the database into allSets
     *
     * @throws EJBException Wrapper for SQLException
     */
    private void makeAllSets() throws EJBException {
        allSets = new HashSet<>();
        try (Connection connection = rawrepoOai.getConnection() ;
             PreparedStatement stmt = connection.prepareStatement("SELECT setSpec FROM oaisets") ;
             ResultSet resultSet = stmt.executeQuery()) {
            while (resultSet.next()) {
                allSets.add(resultSet.getString(1));
            }
        } catch (SQLException ex) {
            throw new EJBException(ex);
        }
        log.debug("allSets = " + allSets);
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
        if (noAuthentication) {
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
