package dk.dbc.rawrepo.oai.packing;


import java.io.ByteArrayOutputStream;

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

/**
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class BitOutputStream {

    private int data;
    private int bit;
    private final ByteArrayOutputStream bos;

    public BitOutputStream() {
        this.data = 0;
        this.bit = 0x80;
        this.bos = new ByteArrayOutputStream();
    }

    public void addOne() {
        data |= bit;
        addZero();
    }

    public void addZero() {
        bit >>= 1;
        if (bit == 0) {
            bos.write(data);
            data = 0;
            bit = 0x80;
        }
    }

    public void add(long bits) {
        while (bits > 1) {
            if (( bits & 1 ) == 1) {
                addOne();
            } else {
                addZero();
            }
            bits >>= 1;
        }
    }

    public byte[] toByteArray() {
        if (bit != 0x80) {
            bos.write(data);
            data = 0;
            bit = 0x80;
        }
        return bos.toByteArray();
    }

}
