package dk.dbc.rawrepo.oai.packing;


import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Iterator;

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
public final class PackText {

    private static final TextTreeNode TREE = TreeBuilder.tree();
    private static final long[] VALUES = makeValues(TREE);

    private static long[] makeValues(TextTreeNode tree) {
        long[] tmp = new long[256];
        traverse(tmp, tree, 0, 1);
        return tmp;
    }

    private PackText() {
    }

    private static void traverse(long[] values, TextTreeNode node, long value, long bit) {
        if (node.isLeaf()) {
            values[node.getValue()] = value | bit;
        } else {
            traverse(values, node.getLeft(), value, bit << 1);
            traverse(values, node.getRight(), value | bit, bit << 1);
        }
    }

    public static String decode(String base64) {
        byte[] bytes = Base64.getDecoder().decode(base64);
        BitInputStream bis = new BitInputStream(bytes);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for ( ; ;) {
            int c = TREE.getChar(bis);
            if (c == 0) {
                break;
            }
            bos.write(c);
        }
        return new String(bos.toByteArray(), StandardCharsets.UTF_8);
    }

    public static String encode(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        BitOutputStream bos = new BitOutputStream();
        for (int i = 0 ; i < bytes.length ; i++) {
            long bits = VALUES[0xff & (int) bytes[i]];
            bos.add(bits);
        }
        bos.add(VALUES[0]);
        return Base64.getEncoder().encodeToString(bos.toByteArray());
    }

    private static final class TreeBuilder {

        private final long[] values = new long[256];

        private TreeBuilder() {
            Arrays.fill(values, 1);
            put(0x02, "\u0001-\u007f");
            put(0x04, "A-Z_\u0000");
            put(0x08, "a-z");
            put(0x40, "0-9a-fTZ");
            put(0x60, "-\":{}., \n");
        }

        static TextTreeNode tree() {
            TreeBuilder treeBuilder = new TreeBuilder();
            ArrayList<TextTreeNode> list = new ArrayList<>();
            for (int i = 0 ; i < 256 ; i++) {
                list.add(new TextTreeNode(i, treeBuilder.values[i]));
            }
            Collections.sort(list, TextTreeNode.SORT);
            while (list.size() > 1) {
                Collections.sort(list, TextTreeNode.SORT);
                Iterator<TextTreeNode> iterator = list.iterator();
                TextTreeNode left = iterator.next();
                iterator.remove();
                TextTreeNode right = iterator.next();
                iterator.remove();
                list.add(new TextTreeNode(left, right));
            }
            return list.get(0);
        }

        private void put(long value, String content) {
            byte[] bytes = content.getBytes(StandardCharsets.ISO_8859_1);
            int last = 0;
            for (int i = 0 ; i < bytes.length ; i++) {
                if (bytes[i] == '-' && i != 0) {
                    i++;
                    int next = 0xff & (int) bytes[i];
                    while (last <= next) {
                        last++;
                        values[last] = value;
                    }
                } else {
                    last = 0xff & (int) bytes[i];
                    values[last] = value;
                }
            }
        }
    }

}
