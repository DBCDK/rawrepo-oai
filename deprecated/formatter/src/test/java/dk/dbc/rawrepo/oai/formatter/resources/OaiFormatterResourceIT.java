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
package dk.dbc.rawrepo.oai.formatter.resources;

import dk.dbc.rawrepo.oai.formatter.javascript.MarcXChangeWrapper;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * @author DBC {@literal <dbc.dk>}
 */
public class OaiFormatterResourceIT {

    public OaiFormatterResourceIT() {
    }

    @Test
    public void testFetchRecordCollection_withHeadVolume() throws Exception {
        Client client = ClientBuilder.newClient();
        OaiFormatterResource resource = new OaiFormatterResource("http://localhost:" + System.getProperty("wiremock.port"), client, null);
        MarcXChangeWrapper[] all = resource.fetchRecordCollection(870970, "44816687");
        assertEquals(2, all.length);
        assertEquals(0, all[0].children.length);
        assertTrue(all[0].content.contains("<datafield ind1='0' ind2='0' tag='001'><subfield code='a'>44816687</subfield><subfield code='b'>870970</subfield>"));
        assertEquals(6, all[1].children.length);
        assertTrue(all[1].content.contains("<datafield ind1='0' ind2='0' tag='001'><subfield code='a'>44783851</subfield><subfield code='b'>870970</subfield>"));
    }
}
