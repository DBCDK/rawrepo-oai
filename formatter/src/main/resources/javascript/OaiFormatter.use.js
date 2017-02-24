/* global XmlUtil, MarcXchangeToOaiMarcX, MarcXchangeToOaiDc, Log, XmlNamespaces */

/** @file Module that formats MarcX. */

use( "Log" );
use( "XmlUtil" );
use( "MarcXchangeToOaiDc" );
use( "MarcXchangeToOaiMarcX" );

EXPORTED_SYMBOLS = [ 'OaiFormatter' ];

/**
 * Module with functions that creates simple Dublin Core elements for OAI harvest of records from RawRepo.
 *
 * This module contains functions to create dc record
 *
 * @type {namespace}
 * @namespace
 */

var OaiFormatter = function() {

    Log.info( "Entering OaiFormatter module" );


    /**
     * Formats MarcX records, either producing a Dublin Core record
     * or MarcX record(s)
     *
     * @function
     * @type {function}
     * @param {String[]} records Array consisting of record, and its ancestors (ordered from closest ancestor)
     * @param {String} format The format to return
     * @param {String[]} allowedSets Names of allowed OAI sets
     * @returns {String} DC or MarcX record(s)
     * @name OaiFormatter.formatRecords
     */
    function formatRecords( records, format, allowedSets ) {

        //lowercasing to make matching easier and avoid errors if input changes
        for ( var i = 0; i < allowedSets.length; i++ ) {
            allowedSets[ i ] = allowedSets[ i ].toLowerCase();
        }

        switch( format ) {
            case 'oai_dc':
                var higherLevelIdentifiers = [ ];
                for ( var k = 0; k < records.length - 1; k++ ) {
                    var higherLevelId = MarcXchangeToOaiDc.getHigherLevelIdentifier( records[ k ] );
                    higherLevelIdentifiers.push( higherLevelId );
                }
                // Format the first record (if more records, the first one is volume)
                return XmlUtil.toXmlString( MarcXchangeToOaiDc.createDcXml( records[0], higherLevelIdentifiers ) );
            case 'marcx':
                var marcXCollection = XmlUtil.createDocument( "collection", XmlNamespaces.marcx );
                var bkmRecordAllowed = ( allowedSets.indexOf( 'bkm' ) > -1 );
                
                // Traverse from head to volume
                for ( var j = records.length - 1; j >= 0; j-- ) {
                    var content = MarcXchangeToOaiMarcX.removeLocalFieldsIfAny( records[ j ] );
                    if ( bkmRecordAllowed ) {
                        var marcXDoc = MarcXchangeToOaiMarcX.createMarcXmlWithRightRecordType( content );
                    } else {
                        marcXDoc = MarcXchangeToOaiMarcX.createMarcXmlWithoutBkmFields( content );
                    }
                    XmlUtil.appendChild( marcXCollection, marcXDoc );
                }                
                
                return XmlUtil.toXmlString( marcXCollection );
            
            default:
                throw Error( "Format: " + format + " not allowed" );
        }    
    }

    /**
     * Used for validating format
     * 
     * @returns {Array} list of allowed formats
     */
    function getAllowedFormats( ) {
        return [ 'oai_dc', 'marcx' ];
    }


    Log.info( "Leaving OaiFormatter module" );

    return {
        formatRecords: formatRecords,
        getAllowedFormats: getAllowedFormats
    };
}();