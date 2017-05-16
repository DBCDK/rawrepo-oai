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

import dk.dbc.rawrepo.RecordId;
import java.util.Objects;

/*
 * @author DBC {@literal <dbc.dk>}
 */
public class MarcXChangeWrapper {    
    public final String content;
    public final Record[] children;
    
    public MarcXChangeWrapper(String content, RecordId[] children) {
        this.content = content;
        this.children = new Record[children.length];
        for (int i = 0; i < children.length; i++) {
            RecordId child = children[i];
            this.children[i] = new Record(child.getBibliographicRecordId(), child.getAgencyId());
        }
    }
    
    public static class Record {
        public final String recId;
        public final int agencyId;
        
        public Record(String recId, int agencyId) {
            this.recId = recId;
            this.agencyId = agencyId;            
        }
        
        @Override
        public boolean equals(Object o) {
            if(!(o instanceof Record)) {
                return false;
            }
            Record other = (Record) o;
            if(this.agencyId == other.agencyId && this.recId.equals(other.recId)) {
                return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 71 * hash + Objects.hashCode(this.recId);
            hash = 71 * hash + this.agencyId;
            return hash;
        }
    }
}
