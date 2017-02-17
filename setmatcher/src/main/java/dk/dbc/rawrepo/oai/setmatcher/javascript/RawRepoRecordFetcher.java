/*
 * Copyright (C) 2017 DBC A/S (http://dbc.dk/)
 *
 * This is part of dbc-rawrepo-oai-harvester-dw
 *
 * dbc-rawrepo-oai-harvester-dw is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * dbc-rawrepo-oai-harvester-dw is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.rawrepo.oai.setmatcher.javascript;

import dk.dbc.rawrepo.RawRepoDAO;
import dk.dbc.rawrepo.RawRepoException;
import dk.dbc.rawrepo.Record;
import java.io.UnsupportedEncodingException;

/**
 * Used as a wrapper around RawRepo, to be injected in javascript business logic.
 *
 * @author DBC {@literal <dbc.dk>}
 */
public class RawRepoRecordFetcher {
    
    private final RawRepoDAO rawrepoDao;
    
    public RawRepoRecordFetcher(RawRepoDAO rawrepoDao) {
        this.rawrepoDao = rawrepoDao;        
    }
    
    /**
     *
     * @param agencyId
     * @param bibRecId
     * @return
     * @throws dk.dbc.rawrepo.RawRepoException
     * @throws java.io.UnsupportedEncodingException
     */
    public String fetchUnmerged(int agencyId, String bibRecId) throws RawRepoException, UnsupportedEncodingException{
        
        Record record = rawrepoDao.fetchRecord(bibRecId, agencyId);
        if(record.isOriginal()) {
            return null;
        }
        return new String(record.getContent(), "UTF-8");
    }
    
}
