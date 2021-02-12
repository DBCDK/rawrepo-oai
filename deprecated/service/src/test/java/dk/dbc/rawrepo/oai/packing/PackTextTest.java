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
package dk.dbc.rawrepo.oai.packing;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class PackTextTest {


    /**
     * Test of decode method, of class PackText.
     */
    @Test
    public void testEncodeDecode() {
        String original = "{\"foo\":\"2017-01-31T20:21:23.234Z\"}";
        String encoded = PackText.encode(original);
        System.out.println("encoded = " + encoded);
        String decoded = PackText.decode(encoded);
        System.out.println("decoded = " + decoded);

        assertNotEquals(original, encoded);
        assertEquals(original, decoded);
    }

}
