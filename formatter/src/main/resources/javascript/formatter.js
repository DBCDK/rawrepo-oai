/*
 * Copyright (C) 2017 DBC A/S (http://dbc.dk/)
 *
 * This is part of dbc-rawrepo-oai-formatter
 *
 * dbc-rawrepo-oai-formatter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * dbc-rawrepo-oai-formatter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/* global XmlUtil, Log */

use( "Log" );
use( "XmlUtil" );
use( "MarcXchangeToOaiDc" );

/**
 * Formats a MarcX record, either producing a Dublin Core record
 * or a MarcX record
 * 
 * @param {String} content The MarcX document
 * @param {String} format The format to return
 * @param {Array} allowedSets List of strings
 * @returns {String} DC or MarcX
 */
var format = function( content, format, allowedSets ) {

    //lowercasing to make matching easier and avoid errors if input changes
    for ( var i = 0; i < allowedSets.length; i++ ) {
        allowedSets[ i ] = allowedSets[ i ].toLowerCase();
    }

    switch( format ) {
        case 'oai_dc':
            return XmlUtil.toXmlString( MarcXchangeToOaiDc.createDcXml( content ) );
        case 'marcx':
            content = MarcXchangeToOaiMarcX.removeLocalFieldsIfAny( content );
            var bkmRecordAllowed = ( allowedSets.indexOf( 'bkm' ) > -1 );
            if ( bkmRecordAllowed ) {
                var marcXDoc = XmlUtil.fromString( content );
            } else {
                marcXDoc = MarcXchangeToOaiMarcX.createMarcXmlWithoutBkmFields( content );
            }
            return XmlUtil.toXmlString( marcXDoc );

        default:
            throw Error( "Format: " + format + " not allowed" );
    }    
};

/**
 * Used for validating format
 * 
 * @returns {Array} list of allowed formats
 */
var allowedFormats = function() {
    return [ 'oai_dc', 'marcx' ];
};