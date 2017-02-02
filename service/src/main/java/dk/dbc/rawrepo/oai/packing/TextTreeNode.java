package dk.dbc.rawrepo.oai.packing;


import java.util.Comparator;

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
public class TextTreeNode {

    private final TextTreeNode left;
    private final TextTreeNode right;
    private final int value;
    private final long total;

    public TextTreeNode(TextTreeNode left, TextTreeNode right) {
        this.left = left;
        this.right = right;
        this.value = Integer.max(left.value, right.value);
        this.total = left.total + right.total;
    }

    public TextTreeNode(int value, long total) {
        this.left = null;
        this.right = null;
        this.value = value;
        this.total = total;
    }

    public TextTreeNode getLeft() {
        return left;
    }

    public TextTreeNode getRight() {
        return right;
    }

    public boolean isLeaf() {
        return left == null && right == null;
    }

    public int getValue() {
        return value;
    }
    public static final Comparator<TextTreeNode> SORT = new Comparator<TextTreeNode>() {
        @Override
        public int compare(TextTreeNode l, TextTreeNode r) {
            int ret = Long.compare(l.total, r.total);
            if (ret == 0) {
                ret = Integer.compare(l.value, r.value);
            }
            return ret;
        }
    };

    @Override
    public String toString() {
        return "Node{" + ( isLeaf() ? "leaf " : "" ) + "value=" + value + ", total=" + total + '}';
    }

    public int getChar(BitInputStream bis) {
        TextTreeNode n = this;
        while (!n.isLeaf()) {
            if (bis.next()) {
                n = n.getRight();
            } else {
                n = n.getLeft();
            }
        }
        return n.getValue();
    }

}
