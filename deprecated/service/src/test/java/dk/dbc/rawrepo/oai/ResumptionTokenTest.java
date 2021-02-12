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
import dk.dbc.oai.pmh.ListRecordsType;
import dk.dbc.oai.pmh.OAIPMH;
import dk.dbc.oai.pmh.ObjectFactory;
import dk.dbc.oai.pmh.ResumptionTokenType;
import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class ResumptionTokenTest {

    @Test
    public void testEncryptDecrypt() throws Exception {
        ObjectNode jsonOriginal = (ObjectNode) new ObjectMapper().readTree("{\"foo\":\"bar\"}");

        String token = ResumptionToken.encode(jsonOriginal, 1);
        assertNotEquals(jsonOriginal.toString(), token);

        ObjectNode json = ResumptionToken.decode(token);
        assertEquals(jsonOriginal.toString(), json.toString());
    }

    @Test
    public void testResumptionTokenTimeout() throws Exception {
        try {
            ObjectNode jsonOriginal = (ObjectNode) new ObjectMapper().readTree("{\"foo\":\"bar\"}");

            String token = ResumptionToken.encode(jsonOriginal, -1);
            ResumptionToken.decode(token);
            fail("Expected exception");
        } catch (OAIException e) {
        }
    }

    @Test
    public void testResumptionTokenVerbatim() throws Exception {
        ObjectNode json = (ObjectNode) new ObjectMapper().readTree("{\"foo\":\"bar\"}");
        assertEquals("{\"foo\":\"bar\"}", json.toString());
    }

    @Test
    public void testResumptionTokenVerbatimSyntaxError() throws Exception {
        try {
            ResumptionToken.decode("{\"foo\":\"bar}");
            fail("Expected exception");
        } catch (OAIException e) {
        }
    }
    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    @Test
    public void testXmlExpiration() throws Exception {
        ObjectNode jsonOriginal = (ObjectNode) new ObjectMapper().readTree("{\"foo\":\"bar\"}");

        long now = Instant.now().getEpochSecond();
        ResumptionTokenType token = ResumptionToken.toToken(jsonOriginal, 0);

        OAIPMH oaipmh = OBJECT_FACTORY.createOAIPMH();
        ListRecordsType getRecord = OBJECT_FACTORY.createListRecordsType();
        oaipmh.setListRecords(getRecord);
        getRecord.setResumptionToken(token);
        JAXBContext context = JAXBContext.newInstance(OAIPMH.class);
        StringWriter writer = new StringWriter();
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(oaipmh, writer);
        String xml = writer.getBuffer().toString();
        System.out.println("XML is:\n" + xml);
        int start = xml.indexOf("expirationDate=\"") + "expirationDate=\"".length();
        int end = xml.indexOf("\"", start);
        String timestamp = xml.substring(start, end);
        System.out.println("timestamp = " + timestamp);

        assertTrue("Timestamp should be in ISO_INSTANT ending with Z", timestamp.endsWith("Z"));
        TemporalAccessor parse = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault()).parse(timestamp);
        long epochSecond = Instant.from(parse).getEpochSecond();
        long diff = Math.abs(now - epochSecond);
        System.out.println("diff = " + diff);
        assertTrue("Difference between expirationdate and now should be 10 sec or less", diff <= 10);
    }

    @Test
    public void testXml() throws Exception {
        ObjectNode jsonOriginal = (ObjectNode) new ObjectMapper().readTree("{\"foo\":\"bar\"}");

        ResumptionTokenType token = ResumptionToken.toToken(jsonOriginal, 1);

        OAIPMH oaipmh = OBJECT_FACTORY.createOAIPMH();
        ListRecordsType getRecord = OBJECT_FACTORY.createListRecordsType();
        oaipmh.setListRecords(getRecord);
        getRecord.setResumptionToken(token);

        ObjectNode json = ResumptionToken.decode(token.getValue());
        assertEquals(jsonOriginal.toString(), json.toString());
    }
}
