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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.dbc.oai.pmh.OAIPMHerrorcodeType;
import dk.dbc.oai.pmh.ObjectFactory;
import dk.dbc.oai.pmh.ResumptionTokenType;
import dk.dbc.rawrepo.oai.packing.PackText;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import javax.xml.datatype.XMLGregorianCalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class ResumptionToken {

    private static final Logger log = LoggerFactory.getLogger(ResumptionToken.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    /**
     * Encode a json object wit a timeout
     *
     * @param obj        json
     * @param validHours timeout
     * @return base64 encoded string
     */
    public static String encode(ObjectNode obj, int validHours) {
        if (obj == null) {
            return null;
        }
        long epoch = Instant.now().plus(validHours, ChronoUnit.HOURS).toEpochMilli() / 1000L;
        String format = String.format("%x%s", epoch, obj.toString());
        return PackText.encode(format);
    }

    /**
     * Decode a base64 string (or a json string) into a json object
     *
     * @param token resumption token
     * @return object
     * @throws java.io.IOException Unlikely, ByteArrayInputStream is broken
     */
    public static ObjectNode decode(String token) throws IOException {
        if (!token.startsWith("{")) {
            long now = Instant.now().toEpochMilli() / 1000L;
            String decoded = PackText.decode(token);
            int indexOf = decoded.indexOf('{');
            long epoch = Long.parseLong(decoded.substring(0, indexOf), 16);
            if (epoch < now) {
                throw new OAIException(OAIPMHerrorcodeType.BAD_RESUMPTION_TOKEN, "ResumptionToken expired");
            }
            token = decoded.substring(indexOf);
        }
        try {
            return (ObjectNode) OBJECT_MAPPER.readTree(token);
        } catch (IOException ex) {
            log.error("Exception:" + ex.getMessage());
            log.debug("Exception:", ex);
            throw new OAIException(OAIPMHerrorcodeType.BAD_RESUMPTION_TOKEN, "bad token");
        }
    }

    /**
     * Construct a ResumptionToken from a json object
     *
     * @param obj        json value
     * @param validHours timeout
     * @return Resumption token to add to an oaipmh request
     */
    public static ResumptionTokenType toToken(ObjectNode obj, int validHours) {
        if (obj == null) {
            return null;
        }
        ResumptionTokenType token = OBJECT_FACTORY.createResumptionTokenType();

        Instant timeout = Instant.now()
                .plus(validHours, ChronoUnit.HOURS)
                .truncatedTo(ChronoUnit.SECONDS);

        XMLGregorianCalendar date = OAIResource.gregorianTimestamp(timeout);

        token.setExpirationDate(date);

        token.setValue(encode(obj, validHours));
        return token;
    }

}
